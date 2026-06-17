#!/usr/bin/env bash

###############################################################################
# deploy_csdk_images.sh
#   Podman/Docker compatible image deployer — OCI layout, one image at a time.
#
#   Docker path:  python3 parses manifest.json, extracts one image's blobs
#                 into a staging area, and pipes a tar to docker load.
#
#   Podman path:  podman pull oci:/dir:ref   (natively understands OCI layout)
#
# Usage:
#   deploy_csdk_images.sh "<ENGINE_CMD>" -l [image:tag]
#   deploy_csdk_images.sh "<ENGINE_CMD>" -l -r <registry> [-p] [image:tag]
#
# Examples:
#   deploy_csdk_images.sh "docker"      -l "pycs/pycs-client:2.10.1"
#   deploy_csdk_images.sh "sudo podman" -l "cs/csta:1.3.1"
#   deploy_csdk_images.sh "docker"      -l -r my.registry.com -p
###############################################################################

###############################################################################
# Image list
###############################################################################
declare -a SOURCE_IMAGES_ARR=(
  "cs/aoco-cs:1.2.13"
  "cs/cssim:3.2.12"
  "cs/cssim-ui-server:1.0.12"
  "cs/ubi9/java17/csbase:2.7"
  "jet:0.11.0"
  "jetwave:0.3.0"
  "silkwave-ir:3.2.5"
)

# Registry prefix baked into the OCI layout's manifest.json RepoTags
SOURCE_REG="company.docker.registry"

# OCI layout directory name (lives alongside this script or one level up)
OCI_DIR_NAME="csdk-docker-2025-07"

###############################################################################
# Logging
###############################################################################
log()  { echo "[INFO ] $*"; }
warn() { echo "[WARN ] $*" >&2; }
err()  { echo "[ERROR] $*" >&2; exit 1; }

###############################################################################
# Usage
###############################################################################
usage() {
  cat <<EOF

usage: deploy_csdk_images.sh "<ENGINE_CMD>" -l [-r <registry> [-p]] [image:tag]

  ENGINE_CMD    "docker" | "podman" | "sudo podman"

  -l            Load image(s) from OCI layout directory: $OCI_DIR_NAME/

  -r <registry> Re-tag loaded image(s) from:
                  $SOURCE_REG/<image>:<tag>
                to:
                  <registry>/<image>:<tag>

  -p            Push to registry specified by -r (must be logged in first)

  [image:tag]   Optional.  Load/process only this one image.
                Example:  "cs/csta:1.3.1"
                If omitted, all images in the list are processed.

EOF
}

###############################################################################
# Engine argument — must be first positional arg
###############################################################################
[[ -z "$1" ]] && { usage; exit 1; }

ENGINE_CMD_RAW="$1"
shift
read -r -a ENGINE_CMD <<< "$ENGINE_CMD_RAW"
ENGINE_BIN="${ENGINE_CMD[-1]}"   # "docker" or "podman"

# ---------------------------------------------------------------------------
# Docker: prefer non-sudo, fall back to sudo docker automatically
# ---------------------------------------------------------------------------
select_docker_cmd() {
  if ! command -v docker &>/dev/null; then
    err "Docker not found on PATH."
  fi
  if docker info &>/dev/null 2>&1; then
    ENGINE_CMD=("docker")
    return
  fi
  if command -v sudo &>/dev/null && sudo docker info &>/dev/null 2>&1; then
    ENGINE_CMD=("sudo" "docker")
    return
  fi
  cat <<'ERRMSG'
[ERROR] Docker is installed but cannot be accessed.
        Fix options:
          sudo usermod -aG docker $USER
          newgrp docker
ERRMSG
  exit 1
}

[[ "$ENGINE_BIN" == "docker" ]] && select_docker_cmd

engine() { "${ENGINE_CMD[@]}" "$@"; }

###############################################################################
# Podman v4+ check
###############################################################################
if [[ "$ENGINE_BIN" == "podman" ]]; then
  PODMAN_VER=$(engine --version 2>/dev/null | grep -oE '[0-9]+(\.[0-9]+)+' | head -n1)
  [[ -z "$PODMAN_VER" ]] && err "Failed to query podman version."
  PODMAN_MAJOR=$(echo "$PODMAN_VER" | cut -d. -f1)
  (( PODMAN_MAJOR < 4 )) && err "Podman v4+ required. Detected: $PODMAN_VER"
fi

###############################################################################
# python3 check — needed only for Docker path
###############################################################################
PYTHON_CMD=""
check_python3() {
  if   command -v python3 &>/dev/null; then PYTHON_CMD="python3"
  elif command -v python  &>/dev/null; then PYTHON_CMD="python"
  else
    err "python3 is required for Docker image loading but was not found.
        Install with:  sudo dnf install python3    (RHEL/Fedora)
                       sudo apt install python3    (Debian/Ubuntu)"
  fi
}

###############################################################################
# Parse flags  (-l, -r <reg>, -p)
###############################################################################
LOAD="false"
PUSH="false"
TARGET_REG=""

while getopts "lpr:" arg; do
  case $arg in
    l) LOAD="true" ;;
    p) PUSH="true" ;;
    r) TARGET_REG="${OPTARG}" ;;
    *) usage; exit 1 ;;
  esac
done
shift $((OPTIND - 1))

SPECIFIC_IMAGE="${1:-}"

###############################################################################
# Locate the OCI layout directory
###############################################################################
find_oci_directory() {
  local script_dir
  script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

  local candidates=(
    "$script_dir/$OCI_DIR_NAME"
    "$script_dir/../$OCI_DIR_NAME"
    "./$OCI_DIR_NAME"
    "../$OCI_DIR_NAME"
  )

  for dir in "${candidates[@]}"; do
    if [[ -d "$dir" && -f "$dir/oci-layout" ]]; then
      realpath "$dir"
      return 0
    fi
  done
  return 1
}

###############################################################################
# DOCKER load — extract one image from manifest.json using python3
#
# Docker cannot read OCI layout directly.  Instead we:
#   1. Use python3 to parse manifest.json and find the single matching entry.
#   2. Write a temp manifest.json containing only that entry.
#   3. Hard-link (or copy) the needed blobs into a staging directory.
#   4. Tar the staging directory and pipe it straight to docker load.
#
# No extra tools.  No system packages.  No skopeo.
###############################################################################
load_docker() {
  local image="$1"      # e.g. "pycs/pycs-client:2.10.1"
  local oci_dir="$2"
  local full_ref="${SOURCE_REG}/${image}"

  # Staging area — cleaned up automatically on exit or error
  local stage
  stage=$(mktemp -d) || err "Could not create temp directory."
  trap 'rm -rf "$stage"' RETURN

  # ------------------------------------------------------------------
  # Step 1 — python3 finds the manifest entry and returns blob paths
  # ------------------------------------------------------------------
  local blob_list
  blob_list=$(
    $PYTHON_CMD - "$oci_dir" "$full_ref" "$stage" <<'PYEOF'
import json, sys, os

oci_dir   = sys.argv[1]
target    = sys.argv[2]   # full ref e.g. company.docker.registry/pycs/pycs-client:2.10.1
out_dir   = sys.argv[3]

manifest_path = os.path.join(oci_dir, "manifest.json")
if not os.path.isfile(manifest_path):
    print(f"manifest.json not found in {oci_dir}", file=sys.stderr)
    sys.exit(1)

with open(manifest_path) as fh:
    manifest = json.load(fh)

entry = None
for e in manifest:
    if target in e.get("RepoTags", []):
        entry = e
        break

if entry is None:
    print(f"Image '{target}' not found in manifest.json", file=sys.stderr)
    sys.exit(2)

# Write a single-entry manifest into the staging directory
with open(os.path.join(out_dir, "manifest.json"), "w") as fh:
    json.dump([entry], fh)

# Print every blob path that needs to be included in the tar
print(entry["Config"])
for layer in entry.get("Layers", []):
    print(layer)
PYEOF
  ) || {
    warn "Failed to locate '$image' in manifest.json — image may not exist in this layout."
    return 1
  }

  # ------------------------------------------------------------------
  # Step 2 — hard-link blobs into staging (falls back to copy)
  # ------------------------------------------------------------------
  while IFS= read -r blob_path; do
    [[ -z "$blob_path" ]] && continue

    local src="${oci_dir}/${blob_path}"
    local dst="${stage}/${blob_path}"

    if [[ ! -f "$src" ]]; then
      warn "Blob not found: $src"
      return 1
    fi

    mkdir -p "$(dirname "$dst")"
    # Hard-link is instant and uses no extra disk space; copy as fallback
    ln "$src" "$dst" 2>/dev/null || cp "$src" "$dst"
  done <<< "$blob_list"

  # ------------------------------------------------------------------
  # Step 3 — build the tar in-memory and pipe directly to docker load
  # ------------------------------------------------------------------
  log "Streaming '$image' into Docker..."

  if ! tar -C "$stage" -cf - . | engine load; then
    warn "docker load failed for: $image"
    return 1
  fi

  log "Loaded OK: $image"
}

###############################################################################
# PODMAN load — stage image blobs from manifest.json and pipe to podman load
#
# Background: "podman pull oci:/dir:ref" requires the ref to exactly match the
# org.opencontainers.image.ref.name annotation in index.json.  The OCI layout
# shipped with CSDK uses "company.docker.registry/<image>:<tag>" as the tag in
# manifest.json but may not carry that string as an index annotation, causing
# "no descriptor found for reference" errors.  "podman load" also does NOT
# accept OCI layout directory arguments — it expects a Docker-format tar on
# stdin.  The fix is to use the same python3 staging approach as load_docker():
# extract the single image's blobs into a temp dir and pipe a tar to
# "podman load", which accepts Docker-format tars from stdin.
###############################################################################
load_podman() {
  local image="$1"
  local oci_dir="$2"
  local full_ref="${SOURCE_REG}/${image}"

  # Staging area — cleaned up automatically on return
  local stage
  stage=$(mktemp -d) || err "Could not create temp directory."
  trap 'rm -rf "$stage"' RETURN

  # ------------------------------------------------------------------
  # Step 1 — python3 finds the manifest entry and returns blob paths
  # ------------------------------------------------------------------
  local blob_list
  blob_list=$(
    $PYTHON_CMD - "$oci_dir" "$full_ref" "$stage" <<'PYEOF'
import json, sys, os

oci_dir   = sys.argv[1]
target    = sys.argv[2]   # full ref e.g. company.docker.registry/pycs/pycs-client:2.10.1
out_dir   = sys.argv[3]

manifest_path = os.path.join(oci_dir, "manifest.json")
if not os.path.isfile(manifest_path):
    print(f"manifest.json not found in {oci_dir}", file=sys.stderr)
    sys.exit(1)

with open(manifest_path) as fh:
    manifest = json.load(fh)

entry = None
for e in manifest:
    if target in e.get("RepoTags", []):
        entry = e
        break

if entry is None:
    print(f"Image '{target}' not found in manifest.json", file=sys.stderr)
    sys.exit(2)

# Write a single-entry manifest into the staging directory
with open(os.path.join(out_dir, "manifest.json"), "w") as fh:
    json.dump([entry], fh)

# Print every blob path that needs to be included in the tar
print(entry["Config"])
for layer in entry.get("Layers", []):
    print(layer)
PYEOF
  ) || {
    warn "Failed to locate '$image' in manifest.json — image may not exist in this layout."
    return 1
  }

  # ------------------------------------------------------------------
  # Step 2 — hard-link blobs into staging (falls back to copy)
  # ------------------------------------------------------------------
  while IFS= read -r blob_path; do
    [[ -z "$blob_path" ]] && continue

    local src="${oci_dir}/${blob_path}"
    local dst="${stage}/${blob_path}"

    if [[ ! -f "$src" ]]; then
      warn "Blob not found: $src"
      return 1
    fi

    mkdir -p "$(dirname "$dst")"
    ln "$src" "$dst" 2>/dev/null || cp "$src" "$dst"
  done <<< "$blob_list"

  # ------------------------------------------------------------------
  # Step 3 — build the tar in-memory and pipe directly to podman load
  # ------------------------------------------------------------------
  log "Streaming '$image' into Podman..."

  if ! tar -C "$stage" -cf - . | engine load; then
    warn "podman load failed for: $image"
    return 1
  fi

  log "Loaded OK: $image"
}

###############################################################################
# Dispatch — pick the right load function based on engine
###############################################################################
load_one_image() {
  local image="$1"
  local oci_dir="$2"

  if [[ "$ENGINE_BIN" == "docker" ]]; then
    load_docker "$image" "$oci_dir"
  else
    load_podman "$image" "$oci_dir"
  fi
}

###############################################################################
# LOAD block
###############################################################################
if [[ "$LOAD" == "true" ]]; then

  # Both Docker and Podman paths now use python3 to stage blobs from manifest.json
  check_python3

  OCI_DIR=$(find_oci_directory) \
    || err "$OCI_DIR_NAME not found.  Checked script dir, parent dir, and current dir."

  log "OCI layout directory: $OCI_DIR"

  [[ -f "$OCI_DIR/index.json" ]]  || err "index.json missing in $OCI_DIR — is this a valid OCI layout?"
  [[ -d "$OCI_DIR/blobs" ]]       || err "blobs/ missing in $OCI_DIR — is this a valid OCI layout?"
  [[ -f "$OCI_DIR/manifest.json" ]] || err "manifest.json missing in $OCI_DIR — Docker load requires this file."

  if [[ -n "$SPECIFIC_IMAGE" ]]; then
    images_to_load=("$SPECIFIC_IMAGE")
  else
    images_to_load=("${SOURCE_IMAGES_ARR[@]}")
  fi

  LOAD_ERRORS=0
  for IMAGE in "${images_to_load[@]}"; do
    load_one_image "$IMAGE" "$OCI_DIR" || (( LOAD_ERRORS++ )) || true
  done

  (( LOAD_ERRORS > 0 )) && warn "$LOAD_ERRORS image(s) failed to load."

fi

###############################################################################
# RETAG + PUSH block
###############################################################################
if [[ -n "$TARGET_REG" ]]; then
  log "Re-tagging images for registry: $TARGET_REG"

  if [[ -n "$SPECIFIC_IMAGE" ]]; then
    images_to_process=("$SPECIFIC_IMAGE")
  else
    images_to_process=("${SOURCE_IMAGES_ARR[@]}")
  fi

  for SOURCE_IMAGE in "${images_to_process[@]}"; do
    ORIG_IMAGE="${SOURCE_REG}/${SOURCE_IMAGE}"
    NEW_IMAGE="${TARGET_REG}/${SOURCE_IMAGE}"

    if ! engine image inspect "$ORIG_IMAGE" >/dev/null 2>&1; then
      warn "Image not found locally (load it first): $ORIG_IMAGE"
      continue
    fi

    log "Tagging:  $ORIG_IMAGE  ->  $NEW_IMAGE"
    if ! engine tag "$ORIG_IMAGE" "$NEW_IMAGE"; then
      warn "Tag failed: $ORIG_IMAGE"
      continue
    fi

    if [[ "$PUSH" == "true" ]]; then
      log "Pushing:  $NEW_IMAGE"
      if ! engine push "$NEW_IMAGE"; then
        warn "Push failed: $NEW_IMAGE"
        continue
      fi
      log "Pushed OK: $NEW_IMAGE"
    fi

    engine rmi "$ORIG_IMAGE" >/dev/null 2>&1 || true
  done
fi

log "Done."
exit 0