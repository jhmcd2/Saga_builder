#!/usr/bin/env bash
#
# install_csdk_artifact.sh
# Clean, menu-safe, Podman/Docker compatible artifact installer
#

###############################################################################
# Safety & UX helpers
###############################################################################

SCRIPT_NAME="$(basename "$0")"

log()  { echo "[INFO ] $*"; }
warn() { echo "[WARN ] $*" >&2; }
err()  { echo "[ERROR] $*" >&2; }

die() {
  err "$*"
  err "Artifact installation aborted."
  return 1
}

pause_hint() {
  echo ""
  echo "Returning to installer menu..."
}

###############################################################################
# Engine handling
###############################################################################

ENGINE_CMD_RAW="docker"

if [[ $# -gt 0 ]]; then
  case "$1" in
    podman|docker|sudo\ podman)
      ENGINE_CMD_RAW="$1"
      shift
      ;;
  esac
fi

read -r -a ENGINE_CMD_ARR <<< "$ENGINE_CMD_RAW"
ENGINE_BIN="${ENGINE_CMD_ARR[-1]}"

engine() {
  "${ENGINE_CMD_ARR[@]}" "$@"
}

validate_engine() {
  if ! command -v "$ENGINE_BIN" >/dev/null 2>&1; then
    { die "Container engine '$ENGINE_BIN' not found on PATH"; return 1; }
  fi

  if [[ "$ENGINE_BIN" == "podman" ]]; then
    local v
    v="$(engine --version 2>/dev/null | grep -oE '[0-9]+\.[0-9]+' | head -n1)"
    [[ -z "$v" ]] && { die "Unable to determine Podman version"; return 1; }
    (( ${v%%.*} < 4 )) && { die "Podman v4+ required (found $v)"; return 1; }
  fi

  return 0
}

###############################################################################
# Platform detection
###############################################################################

detect_platform() {
  local os="$(uname -s)"
  local arch="$(uname -m)"
  
  case "$os" in
    Linux)
      case "$arch" in
        x86_64) echo "linux-amd64" ;;
        aarch64|arm64) echo "linux-arm64" ;;
        *) echo "linux-amd64" ;; # default
      esac
      ;;
    Darwin)
      case "$arch" in
        arm64) echo "darwin-arm64" ;;
        x86_64) echo "darwin-amd64" ;;
        *) echo "darwin-arm64" ;; # default for modern Macs
      esac
      ;;
    *)
      echo "linux-amd64" # default fallback
      ;;
  esac
}

###############################################################################
# Artifact metadata
###############################################################################

UNPACK_OPTIONS=(
  cs-cli
  java-example-code
  mfcodegen
  mfsign
  pycs-src-dist
  local-maven-repository
  csbuild-image
  caci-community-bundle
)

ARTIFACTS=(
  "cs.cs-cli:cs-cli:0.30.1:bz2:{linux-amd64,darwin-arm64}"
  "cs.example:cs-example:3.2.0:zip"
  "cs.dsl:mfcodegen-dist:2.4.2:zip"
  "cs.dsl:mf-sign:1.1:zip:standalone"
  "cs:pycs-dist:2026-03:zip"
  "N/A"
  "company.docker.registry/cs/csbuild:rh9min-jdk17-mvn3.9.6-cscli0.30.1"
  "cs.orca:caci-us-cmty:20260312T185928-744baa73:bundle"
)

DESCRIPTIONS=(
  "Command-line interface for Common Services"
  "Java example Maven project"
  "Code generator for CS DSL"
  "Message signing utility"
  "Python CS source distribution"
  "Offline Maven repository bundle"
  "Docker build image for CS development"
  "Orca CACI community bundle"
)

TARGETS=( "." "." "." "." "." "DIRECTORY""."".")

###############################################################################
# Usage
###############################################################################

usage() {
  echo ""
  echo "Usage:"
  echo "  $SCRIPT_NAME \"<ENGINE_CMD>\" [--repo <repo_dir>] <artifact> [classifier]"
  echo ""
  echo "Available artifacts:"
  for i in "${!UNPACK_OPTIONS[@]}"; do
    printf "  %-24s %s\n" "${UNPACK_OPTIONS[$i]}" "${DESCRIPTIONS[$i]}"
  done
  echo ""
}

###############################################################################
# Argument parsing
###############################################################################

LOCAL_REPO=""
ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo)
      [[ -z "${2:-}" ]] && { usage; exit 0; }
      LOCAL_REPO="$2"
      shift 2
      ;;
    *)
      ARGS+=("$1")
      shift
      ;;
  esac
done

[[ ${#ARGS[@]} -lt 1 ]] && { usage; exit 0; }

ART_NAME="${ARGS[0]}"
CLASSIFIER="${ARGS[1]:-}"

###############################################################################
# Artifact lookup
###############################################################################

artifact_index=-1
for i in "${!UNPACK_OPTIONS[@]}"; do
  [[ "${UNPACK_OPTIONS[$i]}" == "$ART_NAME" ]] && artifact_index="$i"
done

(( artifact_index < 0 )) && { usage; exit 0; }

###############################################################################
# Special case: local-maven-repository
###############################################################################

install_local_maven_repo() {
  local target="${CLASSIFIER}"

  [[ -z "$target" ]] && die "Target directory required for local Maven repository"
  [[ -e "$target" ]] && die "Target directory already exists"

  log "Installing local Maven repository to $target"

  if ! mkdir -p "$target" 2>/dev/null; then
    echo "Need sudo to create the target directory. Attempting to use sudo..."
    sudo mkdir -p "$target" || die "Failed to create target directory: $target"
    echo "You will need to use sudo to access installed components."
  fi

  local source_repo="${LOCAL_REPO}"

  if [[ -z "$source_repo" ]]; then
    local script_dir
    script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

    for dir in "$script_dir/../repository" "./repository" "../repository"; do
      if [[ -d "$dir" ]]; then
        source_repo="$dir"
        break
      fi
    done

    [[ -z "$source_repo" ]] && die "Repository directory not found. Use --repo to specify location."
  fi

  [[ ! -d "$source_repo" ]] && die "Repository directory missing: $source_repo"

  log "Copying repository from: $source_repo"

  if ! cp -a "$source_repo"/. "$target/" 2>/dev/null; then
    echo "Need sudo to copy files. Attempting to use sudo..."
    sudo cp -a "$source_repo"/. "$target/" || die "Copy failed. Check permissions."
    echo "You will need to use sudo to access installed components."
  fi

  log "Local Maven repository installed successfully"
  return 0
}

###############################################################################
# Standard artifact install
###############################################################################

install_standard_artifact() {
  local spec="${ARTIFACTS[$artifact_index]}"
  IFS=':' read -r group id ver type classifier <<< "$spec"

  # Handle classifier auto-detection for cs-cli
  if [[ "$classifier" =~ ^\{.*\}$ ]]; then
    if [[ -z "$CLASSIFIER" ]]; then
      # Auto-detect platform
      local detected_platform="$(detect_platform)"
      log "Auto-detected platform: $detected_platform"
      
      # Validate detected platform is in the allowed list
      local classifier_opts="${classifier:1:-1}"  # Remove { }
      if [[ ",$classifier_opts," == *",$detected_platform,"* ]]; then
        classifier="$detected_platform"
        log "Using classifier: $classifier"
      else
        { die "Detected platform '$detected_platform' not available. Options: $classifier_opts"; return 1; }
      fi
    else
      # Use provided classifier
      classifier="$CLASSIFIER"
    fi
  fi

  local repo
  if [[ -n "$LOCAL_REPO" ]]; then
    repo="$LOCAL_REPO"
  else
    command -v mvn >/dev/null 2>&1 || { die "Maven not found and --repo not supplied"; return 1; }
    repo="$(mvn -q help:evaluate -Dexpression=settings.localRepository -DforceStdout 2>/dev/null)"
    if [[ $? -ne 0 || -z "$repo" ]]; then
      { die "Failed to query Maven for local repository location"; return 1; }
    fi
  fi

  local classpart="${classifier:+-$classifier}"
  local path="${group//./\/}/$id/$ver/$id-$ver$classpart.$type"
  local full="$repo/$path"

  if [[ ! -f "$full" ]]; then
    log "Artifact not found locally, fetching via Maven..."
    local artifact_spec="$group:$id:$ver:$type${classifier:+:$classifier}"
    mvn -q dependency:get -Dartifact="$artifact_spec" -Dtransitive=false \
      || { die "Maven fetch failed for $artifact_spec"; return 1; }
  fi

  [[ ! -f "$full" ]] && { die "Artifact not found after fetch: $full"; return 1; }

  local target="${TARGETS[$artifact_index]}"
  [[ "$target" != "." ]] && mkdir -p "$target"

  case "$type" in
    zip)
      log "Extracting $type archive..."
      unzip -o "$full" -d "$target" >/dev/null \
        || { die "Failed to extract zip archive"; return 1; }
      ;;
    bz2)
      local out="${id}${classifier:+-$classifier}"
      log "Decompressing $type archive to $out..."
      bunzip2 -c "$full" > "$out" || { die "Failed to decompress bz2"; return 1; }
      chmod +x "$out" || warn "Could not set executable permission on $out"
      ;;
    *)
      log "Copying artifact..."
      cp "$full" "$target/" || { die "Failed to copy artifact"; return 1; }
      ;;
  esac

  log "Artifact '$ART_NAME' installed successfully"
  return 0
}

###############################################################################
# Main flow
###############################################################################

main() {
  validate_engine || return 0

  if [[ "$ART_NAME" == "local-maven-repository" ]]; then
    install_local_maven_repo || true
  else
    install_standard_artifact || true
  fi

  pause_hint
  return 0
}

main
exit 0
