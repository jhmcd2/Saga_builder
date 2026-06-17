#!/usr/bin/env bash
#
# csdk-installer.sh

set -euo pipefail

#### Configuration / globals
LOGFILE="/tmp/csdk_install.log"
ANSWER_FILE="/tmp/csdk_answerfile"   # legacy; kept for compatibility
ENV_FILE=".env"
MIN_SPACE_GB=100
DOMAIN=""
STATE_FILE=""          # set after SCRIPT_DIR is resolved below

# Resolve directory (symlink-safe)
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SCRIPT_SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SCRIPT_SOURCE")" && pwd)"
  SCRIPT_SOURCE="$(readlink "$SCRIPT_SOURCE")"
  [[ "$SCRIPT_SOURCE" != /* ]] && SCRIPT_SOURCE="$DIR/$SCRIPT_SOURCE"
done
SCRIPT_DIR="$(cd -P "$(dirname "$SCRIPT_SOURCE")" && pwd)"

# Persistent install-state file — lives next to the installer so it survives
# across sessions and /tmp cleanups.
STATE_FILE="$SCRIPT_DIR/.csdk_install_state"

INSTALLER_DIR="$SCRIPT_DIR/installer_script"

REPO_DIR="$SCRIPT_DIR/repository"
REPO_ARCHIVE_ZIP="$SCRIPT_DIR/repository.zip"
REPO_ARCHIVE_TAR="$SCRIPT_DIR/repository.tar.gz"

# Runtime chosen engine (symbolic) and actual command (may include sudo)
CONTAINER_ENGINE=""        # "podman" or "docker"
CONTAINER_ENGINE_CMD=""    # "podman", "sudo podman", or "docker"
PODMAN_MODE=""             # "rootful" or "rootless" (only when podman selected)

# Unzip Repository.zip automatically
# Path to the zip file (assumed to be next to the script)
ZIP_FILE="${SCRIPT_DIR}/repository.zip"

DEST_DIR="${SCRIPT_DIR}/repository"

#### Utility functions
log() {
  echo "[$(date +'%F %T')] $*" | tee -a "$LOGFILE"
}

pause() {
  read -rp "Press Enter to continue..."
}

fatal() {
  echo "ERROR: $*" | tee -a "$LOGFILE"
  exit 1
}

check_free_space() {
  local free_gb
  free_gb=$(df -BG / | awk 'NR==2 {gsub("G","",$4); print $4}')
  if (( free_gb < MIN_SPACE_GB )); then
    echo "WARNING: Only ${free_gb}GB free. Recommended: ${MIN_SPACE_GB}GB+." | tee -a "$LOGFILE"
    echo "A) Continue"
    echo "B) Abort installation"
    read -rp "Choose [A/B]: " choice
    case "$choice" in
      A|a) log "User chose to continue with low disk space." ;;
      B|b) log "User aborted installation due to disk space."; exit 1 ;;
      *) echo "Invalid choice."; exit 1 ;;
    esac
  else
    log "Disk space check passed: ${free_gb}GB free."
  fi
}

###############################################################################
# Install-state helpers
# The state file is a simple KEY=VALUE store written next to the installer.
# It records which steps have completed so later steps can enforce ordering.
###############################################################################

# write_state KEY VALUE
write_state() {
  local key="$1" val="$2"
  # Remove any existing entry for this key, then append the new one
  if [[ -f "$STATE_FILE" ]]; then
    sed -i "/^${key}=/d" "$STATE_FILE"
  fi
  echo "${key}=${val}" >> "$STATE_FILE"
  log "State updated: ${key}=${val}"
}

# read_state KEY  →  prints the stored value, or empty string
read_state() {
  local key="$1"
  if [[ -f "$STATE_FILE" ]]; then
    grep "^${key}=" "$STATE_FILE" 2>/dev/null | cut -d'=' -f2-
  fi
}

###############################################################################
# CS CLI prerequisite guard
# Called before installing cs-cli.  Blocks (with a clear warning) if the local
# Maven repository has not been set up first, because cs-cli upgrade will
# otherwise attempt to reach the remote Artifactory server.
###############################################################################

check_cscli_prereqs() {
  local maven_installed
  maven_installed="$(read_state MAVEN_REPO_INSTALLED)"

  if [[ "$maven_installed" != "true" ]]; then
    echo ""
    echo "╔══════════════════════════════════════════════════════════════════╗"
    echo "║  !! WARNING: INSTALLATION ORDER ISSUE DETECTED                !!║"
    echo "╠══════════════════════════════════════════════════════════════════╣"
    echo "║                                                                  ║"
    echo "║  You are attempting to install CS CLI before the local Maven     ║"
    echo "║  repository has been installed.                                  ║"
    echo "║                                                                  ║"
    echo "║  Without the local Maven repository configured first, CS CLI     ║"
    echo "║  will attempt to download artifacts from a REMOTE Artifactory    ║"
    echo "║  server, which may not be reachable in your environment.         ║"
    echo "║                                                                  ║"
    echo "║  REQUIRED INSTALLATION ORDER:                                    ║"
    echo "║    STEP 1 →  Install Artifacts  →  local-maven-repository        ║"
    echo "║    STEP 2 →  Install Artifacts  →  cs-cli          (this step)   ║"
    echo "║                                                                  ║"
    echo "║  Please return to the menu and complete Step 1 first.            ║"
    echo "╚══════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "  R) Return to menu and install local-maven-repository first  [RECOMMENDED]"
    echo "  C) Continue anyway  (CS CLI will attempt remote server access)"
    echo ""
    read -rp "Choose [R/C]: " prereq_choice
    case "$prereq_choice" in
      C|c)
        echo ""
        echo "[WARN] Proceeding without local Maven repository."
        echo "       CS CLI upgrade may fail if the remote server is unreachable."
        echo ""
        log "User bypassed cs-cli prereq guard (remote access mode)."
        return 0
        ;;
      *)
        echo ""
        echo "Returning to menu.  Please install 'local-maven-repository' first."
        echo ""
        log "User returned to menu from cs-cli prereq guard."
        return 1
        ;;
    esac
  fi

  return 0   # prereqs satisfied
}

###############################################################################
# CS CLI global.yml auto-generator
# Called after cs-cli installs successfully.  Reads the Maven repo path
# recorded during local-maven-repository installation and writes
# global.yml so the user never has to touch YAML manually.
###############################################################################

generate_cscli_config() {
  local maven_repo_path
  maven_repo_path="$(read_state MAVEN_REPO_PATH)"

  if [[ -z "$maven_repo_path" ]]; then
    echo ""
    echo "[WARN] Cannot auto-generate CS CLI config: Maven repository path was not recorded."
    echo "       Run 'Install Artifacts → local-maven-repository' and then reinstall cs-cli,"
    echo "       or create \$HOME/.common-services/cs-cli/config/global.yml manually."
    return 1
  fi

  local cs_home="${COMMON_SERVICES_HOME:-$HOME/.common-services}"
  local config_dir="$cs_home/cs-cli/config"
  local config_file="$config_dir/global.yml"

  if [[ -f "$config_file" ]]; then
    echo ""
    echo "[INFO] CS CLI config already exists at:"
    echo "       $config_file"
    echo "       Leaving existing file intact.  Delete it and reinstall cs-cli to regenerate."
    return 0
  fi

  mkdir -p "$config_dir" || {
    echo "[ERROR] Could not create config directory: $config_dir"
    return 1
  }

  cat > "$config_file" <<EOF
# CS CLI global configuration
# Auto-generated by CSDK installer on $(date)
# Source Maven repository: ${maven_repo_path}
#
# This file was created automatically to point CS CLI at the local
# bundled Maven repository.  Edit only if you need to add additional
# remote repositories or change credentials.

credentials:
  local_csdk:
    username: "local"
    password: "local"
    password_format: plain

maven:
  repositories:
    - name: local-csdk
      release:
        url: "file://${maven_repo_path}"
        credentials: local_csdk
      snapshot:
        url: "file://${maven_repo_path}"
        credentials: local_csdk

docker:
  repository_url: ""
EOF

  echo ""
  echo "╔══════════════════════════════════════════════════════════════════╗"
  echo "║  CS CLI configuration written successfully                       ║"
  echo "╠══════════════════════════════════════════════════════════════════╣"
  echo "║                                                                  ║"
  printf  "║  Config : %-54s ║\n" "$config_file"
  printf  "║  Repo   : file://%-48s ║\n" "$maven_repo_path"
  echo "║                                                                  ║"
  echo "║  CS CLI will use the LOCAL bundled repository —                  ║"
  echo "║  no remote server access required.                               ║"
  echo "╚══════════════════════════════════════════════════════════════════╝"
  echo ""

  write_state CSCLI_CONFIG_WRITTEN true
  write_state CSCLI_CONFIG_PATH "$config_file"
  log "CS CLI global.yml written: $config_file (repo: $maven_repo_path)"
  return 0
}

#### Compatibility and prerequisites
check_maven_and_java() {
  log "Checking Java and Maven..."

  #
  # ---- Java checks (FATAL if broken) ----
  #
  if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: Java not found on PATH. Java (JDK) is required." | tee -a "$LOGFILE"
    return 1
  fi

  if ! command -v javac >/dev/null 2>&1; then
    echo "ERROR: JDK required (javac not found). Install a full JDK." | tee -a "$LOGFILE"
    return 1
  fi

  # Get Java installation path by examining 'java' command
  JAVA_DIR=$(dirname "$(dirname "$(readlink -f "$(which java)")")")

  if [[ ! -x "${JAVA_DIR}/bin/java" ]]; then
    echo "ERROR: Java executable not found in $JAVA_DIR" | tee -a "$LOGFILE"
    return 1
  fi

  log "Java verified successfully (JAVA_DIR=$JAVA_DIR)"

  #
  # ---- Maven checks (NON-FATAL) ----
  #
  if command -v mvn >/dev/null 2>&1; then
    log "Maven already available on PATH."
    return 0
  fi

  echo ""
  echo "########################################################"
  echo " Maven not found on this system."
  echo " Attempting automatic Maven installation (non-fatal)..."
  echo "########################################################"
  echo ""

  if [[ -x "$INSTALLER_DIR/install_maven.sh" ]]; then
    if "$INSTALLER_DIR/install_maven.sh" "$SCRIPT_DIR"; then
      log "Maven installer completed."
    else
      echo "WARNING: Maven installer exited with an error." | tee -a "$LOGFILE"
    fi
  else
    echo "WARNING: Maven installer not found at $INSTALLER_DIR/install_maven.sh" | tee -a "$LOGFILE"
  fi

  #
  # ---- Best-effort verification (never fatal) ----
  #
  if [[ -x /opt/maven/current/bin/mvn ]] && \
     /opt/maven/current/bin/mvn -v >/dev/null 2>&1; then
    log "Maven verified successfully."
  else
    echo "WARNING: Maven is still unavailable. Continuing without Maven." | tee -a "$LOGFILE"
  fi

  return 0
}


# Validate only the selected engine (called after the user chooses engine & mode)
validate_selected_engine() {
  if [[ -z "$CONTAINER_ENGINE" || -z "$CONTAINER_ENGINE_CMD" ]]; then
    fatal "Container engine not configured"
  fi

  # Validate executable is present in PATH (or reachable via sudo)
  if [[ "$CONTAINER_ENGINE_CMD" == sudo* ]]; then
    # ensure sudo is present and podman exists for rootful
    if ! command -v sudo >/dev/null 2>&1; then
      fatal "sudo not found but rootful Podman selected."
    fi
    if ! sudo -n true 2>/dev/null; then
      # If sudo requires a password, that's fine; we will attempt commands and sudo will prompt.
      log "Note: sudo requires a password for privileged Podman operations."
    fi
    # check podman exists for root user
    if ! sudo command -v podman >/dev/null 2>&1; then
      fatal "podman not found for root (sudo). Please install podman for rootful usage."
    fi
  else
    # Non-sudo check: command must exist in PATH
    local exe
    exe="$(echo "$CONTAINER_ENGINE_CMD" | awk '{print $1}')"
    if ! command -v "$exe" >/dev/null 2>&1; then
      fatal "Required container executable '$exe' not found. Please install it or choose another engine."
    fi
  fi

  # Extra checks for Podman rootless socket if rootless chosen
  if [[ "$CONTAINER_ENGINE" == "podman" && "$PODMAN_MODE" == "rootless" ]]; then
    # check user socket
    local sock="/run/user/$(id -u)/podman/podman.sock"
    if [[ ! -S "$sock" ]]; then
      echo "WARNING: Podman rootless socket not detected at $sock."
      echo "Rootless Podman may still work (it may create socket on first use), but"
      echo "networking / privileged port binding limitations may apply."
      read -rp "Continue with rootless Podman anyway? [y/N]: " cont
      case "$cont" in
        y|Y) log "User chose to proceed with rootless Podman despite socket missing." ;;
        *) fatal "Aborting because rootless Podman socket not available." ;;
      esac
    fi
  fi

  log "Container engine validation passed: $CONTAINER_ENGINE_CMD"
}

#### Repository extraction (preserve behavior)
prepare_repository() {
  if [[ -d "$REPO_DIR" ]]; then
    log "Repository directory found."
    return
  fi

  if [[ -f "$REPO_ARCHIVE_ZIP" ]]; then
    log "Extracting repository from $REPO_ARCHIVE_ZIP..."
    unzip -q "$REPO_ARCHIVE_ZIP" -d "$SCRIPT_DIR"
  elif [[ -f "$REPO_ARCHIVE_TAR" ]]; then
    log "Extracting repository from $REPO_ARCHIVE_TAR..."
    tar -xzf "$REPO_ARCHIVE_TAR" -C "$SCRIPT_DIR"
  else
    fatal "Repository directory or archive not found."
  fi

  if [[ ! -d "$REPO_DIR" ]]; then
    fatal "Extraction failed, repository directory still missing."
  fi
}

#### Engine selection UI + logic
show_engine_banner() {
  cat <<'EOF'
===========================================
CSDK Installer - Container Engine Selection
===========================================

This installer is Podman-first (recommended).  You may choose Podman or Docker.
Podman is the recommended engine moving forward.

If you choose Podman, you'll be asked whether to use:
  - Rootful (runs under root via sudo)  <-- DEFAULT (recommended for compatibility)
  - Rootless (runs without root; safer, but some features may be limited)

Notes:
 - Rootful Podman gives behavior closest to Docker (privileged ports, volume mounts).
 - Rootless Podman avoids using root but may have limitations (network, privileged ports).
 - Docker generally requires root privileges on many systems.
 - The installer will validate only the engine/mode you choose.

EOF
}

choose_container_engine() {
  show_engine_banner

  echo "Select container engine:"
  echo "  1) Podman (recommended) [default]"
  echo "  2) Docker"
  read -rp "Enter 1 or 2 (default 1): " ENGINE_CHOICE

  case "$ENGINE_CHOICE" in
    2) CONTAINER_ENGINE="docker" ;;
    1|"" ) CONTAINER_ENGINE="podman" ;;
    *)
      echo "Invalid choice. Defaulting to Podman."
      CONTAINER_ENGINE="podman"
      ;;
  esac

  if [[ "$CONTAINER_ENGINE" == "podman" ]]; then
    echo ""
    echo "Select Podman mode:"
    echo "  1) Rootful (run with sudo)   [default]"
    echo "  2) Rootless (run as current user)"
    read -rp "Enter 1 or 2 (default 1): " MODE_CHOICE

    case "$MODE_CHOICE" in
      2) PODMAN_MODE="rootless" ;;
      1|"" ) PODMAN_MODE="rootful" ;;
      *) echo "Invalid choice. Defaulting to rootful."; PODMAN_MODE="rootful" ;;
    esac

    if [[ "$PODMAN_MODE" == "rootful" ]]; then
      CONTAINER_ENGINE_CMD="sudo podman"
    else
      CONTAINER_ENGINE_CMD="podman"
    fi
  else
    PODMAN_MODE=""
    CONTAINER_ENGINE_CMD="docker"
  fi

  # >>> ADD THESE TWO LINES <<<
  ENGINE_CMD="$CONTAINER_ENGINE_CMD"
  ENGINE_CMD_RAW="$CONTAINER_ENGINE_CMD"
  # <<< END ADDITION >>>

  log "Selected engine: $CONTAINER_ENGINE (cmd: $CONTAINER_ENGINE_CMD ${PODMAN_MODE:+, mode: $PODMAN_MODE})"
  validate_selected_engine
}


#### Menus (consolidated)
main_menu() {
  clear
  while true; do
    # -- Menu header 
    printf '\n%s\n' '==== CSDK Installer Main Menu ===='

    # -- Menu options (aligned, wrapped) 
    printf '%-2s %s\n' 'A)' \
      'Install Images -- helper for moving the CSDK Docker/Podman images from a bundled
      archive into a local container engine, optionally retagging them for a
      private registry and pushing them there.'

    printf '%-2s %s\n' 'B)' \
      'Install Artifacts -- one-stop installer for the various artifacts that make up
      the Common Services Development Kit.'

    printf '%-2s %s\n' 'C)' \
      'Quick Hub Deployment -- stands up a Common Services Hub with minimal required
      values.'

    printf '%-2s %s\n' 'D)' \
      'Configure Maven Settings -- configure Maven to access remote repositories.
      (Requires the Maven settings script to be located in /opt/maven/conf.)'
    
    #printf '%-2s %s\n' 'E)' 'Uninstall'   # reserved for future use

    printf '%-2s %s\n' 'E)' 'Exit'
read -rp $'\nChoose option: ' opt

    case "$opt" in
      A|a) images_menu ;;
      B|b) artifacts_menu ;;
      C|c) hub_install ;;
      #F|f) uninstall_csdk ;;
      D|d) configure_maven ;;
      E|e) log "Exiting installer."; exit 0 ;;
      *) echo "Invalid option."; pause ;;
    esac
  done
}

images_menu() {
  local images=(
  "cs/aoco-cs:1.2.13"
  "cs/cssim:3.2.12"
  "cs/cssim-ui-server:1.0.12"
  "cs/ubi9/java17/csbase:2.7"
  "jet:0.11.0"
  "jetwave:0.3.0"
  "silkwave-ir:3.2.5"
  )

  while true; do
    ###########################################################################
    # Unzip image repository 
    ###########################################################################
    local tar_file="csdk-docker-2025-07.tgz"
    local target_dir="csdk-docker-2025-07"

    # Check if the tar file exists
    if [[ -f "$tar_file" ]]; then
      if [[ ! -d "$target_dir" ]]; then
        echo "Extracting '$tar_file' into '$target_dir'..."
        mkdir -p "$target_dir"
        if tar -xvzf "$tar_file" -C "$target_dir"; then
          echo "Extraction complete."
        else
          echo "ERROR: Extraction failed."
          rm -rf "$target_dir"
          return 1
        fi
      else
        echo "Directory '$target_dir' already exists. Skipping extraction."
      fi
    else
      echo "Tar file '$tar_file' not found."
      return 1
    fi   
    ###########################################################################
    # Docker special behavior
    ###########################################################################
    if [[ "$CONTAINER_ENGINE_CMD" == "docker" ]]; then
      echo "==== Docker Image Installation ===="
      echo
      echo "Docker installs ALL CSDK images at once."
      echo
            echo "The following images will be installed:"
      echo

      for img in "${images[@]}"; do
        echo " - $img"
      done
      echo
      read -rp "Do you want to install ALL images now? (Y/N): " yn

      if [[ "$yn" =~ ^[Yy]$ ]]; then
        choice=1   # symbolic only -- keeps logic consistent
      else
        choice="b"
      fi
    else
      #########################################################################
      # Podman (normal per-image menu)
      #########################################################################
      echo "==== Install Images ===="
      for i in "${!images[@]}"; do
        echo "$((i+1))) ${images[$i]}"
      done
      echo "B) Back"
      read -rp "Choose image to install: " choice
    fi

    ###########################################################################
    # Handle choice
    ###########################################################################

    if [[ "$CONTAINER_ENGINE_CMD" == "docker" && "$choice" == "1" ]]; then

      log "Installing ALL images (engine: docker)"
      if [[ -x "$INSTALLER_DIR/deploy_csdk_images.sh" ]]; then
        # IMPORTANT: no image argument -> installs all
        "$INSTALLER_DIR/deploy_csdk_images.sh" "$CONTAINER_ENGINE_CMD" -l
      else
        echo "ERROR: $INSTALLER_DIR/deploy_csdk_images.sh not found or not executable." | tee -a "$LOGFILE"
      fi
      pause
      return

    elif [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#images[@]} )); then

      local img="${images[$((choice-1))]}"
      log "Installing image: $img (engine: $CONTAINER_ENGINE_CMD)"
      if [[ -x "$INSTALLER_DIR/deploy_csdk_images.sh" ]]; then
        "$INSTALLER_DIR/deploy_csdk_images.sh" "$CONTAINER_ENGINE_CMD" -l "$img"
      else
        echo "ERROR: $INSTALLER_DIR/deploy_csdk_images.sh not found or not executable." | tee -a "$LOGFILE"
      fi
      pause

    elif [[ "$choice" == "B" || "$choice" == "b" ]]; then
      return

    else
      echo "Invalid choice."
      pause
    fi
  done
}


artifacts_menu() {
  local artifacts=(
    "cs-cli"
    "java-example-code"
    "mfcodegen"
    "mfsign"
    "pycs-src-dist"
    "local-maven-repository"
    "csbuild-image"
    "caci-community-bundle"
  )

  local arch="$(uname -m)"
  case "$arch" in
    x86_64) arch_out="linux-amd64" ;;
    aarch64) arch_out="darwin-arm64" ;; # adjust if needed
    *) arch_out="$arch" ;;
  esac

  while true; do
    clear
    echo "==== Install Artifacts (from $REPO_DIR) ===="
    for i in "${!artifacts[@]}"; do
      echo "$((i+1))) ${artifacts[$i]}"
    done
    echo "B) Back"
    read -rp "Choose artifact to install: " choice

    if [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#artifacts[@]} )); then
      local art="${artifacts[$((choice-1))]}"
      log "Installing artifact: $art (engine: $CONTAINER_ENGINE_CMD)"
      if [[ -x "$INSTALLER_DIR/install_csdk_artifact.sh" ]]; then
        if [[ "$art" == "cs-cli" ]]; then
          # ── Prerequisite guard ──────────────────────────────────────────────
          if ! check_cscli_prereqs; then
            pause
            continue
          fi
          # ── Install ─────────────────────────────────────────────────────────
          "$INSTALLER_DIR/install_csdk_artifact.sh" "$CONTAINER_ENGINE_CMD" --repo "$REPO_DIR" "$art" "$arch_out"
          local cli_exit=$?
          # ── Auto-generate global.yml on success ──────────────────────────────
          if [[ $cli_exit -eq 0 ]]; then
            generate_cscli_config
            write_state CSCLI_INSTALLED true
          fi
        elif [[ "$art" == "local-maven-repository" ]]; then
          echo ""
          echo "This will copy the entire Maven repository to a target directory."
          echo "The target directory must NOT already exist."
          echo ""
          read -rp "Enter target directory path (e.g., /opt/csdk/maven-repo or ~/my-maven-repo): " target_dir
          if [[ -n "$target_dir" ]]; then
            # Expand tilde if present
            target_dir="${target_dir/#\~/$HOME}"
            if [[ -e "$target_dir" ]]; then
              echo "ERROR: Target directory already exists: $target_dir" | tee -a "$LOGFILE"
              echo "Please choose a different location or remove the existing directory."
            else
              "$INSTALLER_DIR/install_csdk_artifact.sh" "$CONTAINER_ENGINE_CMD" --repo "$REPO_DIR" "$art" "$target_dir"
              local repo_exit=$?
              # ── Record path so cs-cli config generation can use it ─────────
              if [[ $repo_exit -eq 0 ]]; then
                write_state MAVEN_REPO_INSTALLED true
                write_state MAVEN_REPO_PATH "$target_dir"
                echo ""
                echo "[OK] Maven repository path recorded."
                echo "     When you install cs-cli next, the installer will"
                echo "     automatically configure it to use this local repository."
                echo ""
              fi
            fi
          else
            echo "ERROR: Target directory required for local-maven-repository installation." | tee -a "$LOGFILE"
          fi
        else
          "$INSTALLER_DIR/install_csdk_artifact.sh" "$CONTAINER_ENGINE_CMD" --repo "$REPO_DIR" "$art"
        fi
      else
        echo "ERROR: $INSTALLER_DIR/install_csdk_artifact.sh not found or not executable." | tee -a "$LOGFILE"
      fi
      pause
    elif [[ "$choice" == "B" || "$choice" == "b" ]]; then
      return
    else
      echo "Invalid choice."; pause
    fi
  done
}

run_with_sudo() {
    # $1 = script path, $2| = arguments for that script
    local script_path=$1
    shift

    local exit_code=0
    if [[ $EUID -ne 0 ]]; then
        echo "Not root -- invoking ${script_path} with sudo..."
        sudo "$script_path" "$@" || exit_code=$?
    else
        echo "Running ${script_path} as root..."
        "$script_path" "$@" || exit_code=$?
    fi
    return $exit_code
}

hub_install() {
  read -rp "Enter domain for Hub installation: " DOMAIN
  export DOMAIN
  if [[ -z "$DOMAIN" ]]; then
      echo "ERROR: DOMAIN is not set."
      return 1
  fi

  echo "Running Hub installer for $CONTAINER_ENGINE (domain: $DOMAIN, engine: $CONTAINER_ENGINE_CMD, mode: $PODMAN_MODE)"

  local hub_exit=0
  if [[ "$CONTAINER_ENGINE" == "podman" ]]; then
      run_with_sudo "$INSTALLER_DIR/Hub_install_podman.sh" "$DOMAIN" || hub_exit=$?
  else
      run_with_sudo "$INSTALLER_DIR/Hub_install_docker.sh" "$DOMAIN" || hub_exit=$?
  fi

  if [[ $hub_exit -ne 0 ]]; then
      echo "WARNING: Hub installer exited with code $hub_exit. Check logs for details." | tee -a "$LOGFILE"
  fi
  # Always return to the main menu regardless of hub script exit code
  return 0
}



uninstall_csdk() {
  log "Uninstall routine not implemented yet. Placeholder."
  echo "(Placeholder for uninstall steps; will be implemented after image/hub conversion)"
  pause
}

configure_maven() {
    echo ""
    echo "============================"
    echo "   Maven Configuration"
    echo "============================"
    echo ""

    MAVEN_CFG_SCRIPT="$INSTALLER_DIR/configure_maven.sh"

    if [ ! -f "$MAVEN_CFG_SCRIPT" ]; then
        echo "[ERROR] configure_maven.sh not found at:"
        echo "        $MAVEN_CFG_SCRIPT"
        echo "Make sure it exists in csdk-dist-2025-09/installer_script/"
        return 1
    fi

    # Ensure executable
    chmod +x "$MAVEN_CFG_SCRIPT" 2>/dev/null || true

    echo "Running Maven configuration script..."
    echo ""

    # Execute the script (must run with sudo/root for /opt/maven/conf)
    if sudo "$MAVEN_CFG_SCRIPT"; then
        echo ""
        echo "[OK] Maven configuration completed successfully."
    else
        echo ""
        echo "[ERROR] Maven configuration script exited with failure."
        echo "Check output above for details."
    fi

    echo ""
    read -p "Press Enter to return to the main menu..."
}

#### Main execution flow
# preserve original behavior: run compatibility checks and create answer file if missing
#if [[ ! -f "$ANSWER_FILE" ]]; then
  # Basic checks (mvn/java) performed before engine selection to preserve original behavior
#  prepare_repository
#  check_maven_and_java
#  log "Creating answer file $ANSWER_FILE"
#  touch "$ANSWER_FILE"
#fi
if ! command -v tar >/dev/null 2>&1; then
    echo "ERROR: 'tar' is not installed. Installing now..."
    sudo dnf install -y tar || { echo "Installation failed"; exit 1; }
fi
prepare_repository
check_maven_and_java
# Select & validate engine + mode BEFORE showing menus (so menus can pass engine to child scripts)
choose_container_engine

main_menu
