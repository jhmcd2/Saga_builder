#!/usr/bin/env bash
# configure_maven.sh
# Callable script to configure Maven settings
# - Auto-detects Maven conf directory OR falls back to user-level ~/.m2/settings.xml
# - Backs up original to settingsORIGINAL.xml (if not already backed up)
# - Writes a new settings.xml with a tag <!-- CSDK-MAVEN-CONFIGURED -->
# - Prompts user for GitLab token and Artifactory info
# - Creates a Pom template at ../Pom_template/pom.xml
#
# Usage: sudo ./configure_maven.sh (or without sudo for user-level config)
set -euo pipefail

# ----------------------------
# Maven settings location logic
# ----------------------------

# If caller wants to force a specific Maven conf dir, they can export MAVEN_CONF_DIR
# e.g., MAVEN_CONF_DIR=/usr/share/maven/conf sudo ./configure_maven.sh
MAVEN_CONF_DIR="${MAVEN_CONF_DIR:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIST_DIR="$(dirname "$SCRIPT_DIR")"

# Determine the "real" home to use for user-level settings when running under sudo.
# If we are root via sudo, prefer the invoking user's home.
REAL_HOME="${HOME:-/root}"
if [[ -n "${SUDO_USER:-}" && "${SUDO_USER}" != "root" ]]; then
  REAL_HOME="$(getent passwd "$SUDO_USER" | cut -d: -f6 2>/dev/null || echo "$REAL_HOME")"
fi

detect_maven_conf_dir() {
  # 1) Explicit override
  if [[ -n "$MAVEN_CONF_DIR" && -d "$MAVEN_CONF_DIR" ]]; then
    echo "$MAVEN_CONF_DIR"
    return 0
  fi

  # 2) Env vars
  for base in "${M2_HOME:-}" "${MAVEN_HOME:-}"; do
    [[ -n "$base" && -d "$base/conf" ]] && { echo "$base/conf"; return 0; }
  done

  # 3) Infer from mvn location
  if command -v mvn >/dev/null 2>&1; then
    local mvn_path mvn_real maven_home
    mvn_path="$(command -v mvn)"
    mvn_real="$(readlink -f "$mvn_path" 2>/dev/null || echo "$mvn_path")"
    # Typical: <MAVEN_HOME>/bin/mvn
    maven_home="$(cd "$(dirname "$mvn_real")/.." && pwd 2>/dev/null || true)"
    [[ -n "$maven_home" && -d "$maven_home/conf" ]] && { echo "$maven_home/conf"; return 0; }
  fi

  # 4) Common system paths
  local candidates=(
    "/etc/maven"
    "/usr/share/maven/conf"
    "/usr/local/share/maven/conf"
    "/opt/maven/conf"
    "/opt/apache-maven/conf"
  )
  for d in "${candidates[@]}"; do
    [[ -d "$d" ]] && { echo "$d"; return 0; }
  done

  return 1
}

# Try to find a global Maven conf dir; if not found, use user-level ~/.m2
if MAVEN_CONF_DIR_DETECTED="$(detect_maven_conf_dir)"; then
  MAVEN_CONF_DIR="$MAVEN_CONF_DIR_DETECTED"
  MAVEN_SETTINGS="${MAVEN_CONF_DIR}/settings.xml"
  MAVEN_SETTINGS_ORIG="${MAVEN_CONF_DIR}/settingsORIGINAL.xml"
  SETTINGS_SCOPE="global (${MAVEN_CONF_DIR})"
else
  MAVEN_CONF_DIR=""  # no global conf
  MAVEN_SETTINGS="${REAL_HOME}/.m2/settings.xml"
  MAVEN_SETTINGS_ORIG="${REAL_HOME}/.m2/settingsORIGINAL.xml"
  SETTINGS_SCOPE="user (${REAL_HOME}/.m2)"
fi

# ----------------------------
# Configurable defaults
# ----------------------------
DEFAULT_OFFLINE_REPO="/opt/csdk/repository"
POM_TEMPLATE_DIR="${DIST_DIR}/Pom_template"
POM_TEMPLATE_PATH="${POM_TEMPLATE_DIR}/pom.xml"
TAG="<!-- CSDK-MAVEN-CONFIGURED -->"

# Helpers
info() { echo -e "\e[34m[INFO]\e[0m $*"; }
warn() { echo -e "\e[33m[WARN]\e[0m $*"; }
err()  { echo -e "\e[31m[ERROR]\e[0m $*" >&2; exit 1; }

# ----------------------------
# Initial checks
# ----------------------------

info "Using Maven settings scope: ${SETTINGS_SCOPE}"

# Ensure destination directory exists (global conf dir or ~/.m2)
mkdir -p "$(dirname "${MAVEN_SETTINGS}")"

# Warn about permissions if configuring global settings
if [[ -n "$MAVEN_CONF_DIR" ]] && [ "$(id -u)" -ne 0 ]; then
  warn "Running with sudo/root is recommended when writing to system-wide Maven settings: ${MAVEN_CONF_DIR}"
  warn "Alternatively, this script can configure user-level settings at ~/.m2/settings.xml without sudo."
fi

# Check for existing settings.xml
if [ ! -f "${MAVEN_SETTINGS}" ]; then
  warn "No settings.xml found at ${MAVEN_SETTINGS}."
  read -p "Create a default empty settings.xml (yes/no)? [yes] " create_default
  create_default=${create_default:-yes}
  if [ "${create_default}" != "yes" ] && [ "${create_default}" != "y" ]; then
    err "Aborting - no settings.xml to base from."
  fi
  mkdir -p "$(dirname "${MAVEN_SETTINGS}")"
  echo "<settings></settings>" > "${MAVEN_SETTINGS}"
  info "Created minimal ${MAVEN_SETTINGS}."
fi

# Backup original if not already backed up
if [ ! -f "${MAVEN_SETTINGS_ORIG}" ]; then
  cp -p "${MAVEN_SETTINGS}" "${MAVEN_SETTINGS_ORIG}"
  info "Backed up original settings.xml to ${MAVEN_SETTINGS_ORIG}"
else
  info "Backup already exists at ${MAVEN_SETTINGS_ORIG} — not overwriting."
fi

# If file already contains our tag, prompt whether to proceed
if grep -qF "${TAG}" "${MAVEN_SETTINGS}" 2>/dev/null || grep -qF "${TAG}" "${MAVEN_SETTINGS_ORIG}" 2>/dev/null; then
  info "Detected previous CSDK Maven configuration tag."
  read -p "Do you want to reconfigure/overwrite settings.xml? (this will replace current settings.xml) [no] " rep
  rep=${rep:-no}
  if [ "${rep}" != "yes" ] && [ "${rep}" != "y" ]; then
    info "Exiting without changes."
    exit 0
  fi
fi

# ----------------------------
# Configuration menu function
# ----------------------------

create_maven_settings() {
  # Interactive prompts for Maven settings
  info "=== Create New Maven Settings (Guided) ==="
  
  # 1) Offline repository path
  read -p "Enter local offline repository path [${DEFAULT_OFFLINE_REPO}]: " offline_repo
  offline_repo="${offline_repo:-${DEFAULT_OFFLINE_REPO}}"
  info "Using offline repository: ${offline_repo}"

  # Create the offline repo URL for the mirror
  offline_repo_url="file://${offline_repo}"

  # 2) GitLab settings (optional)
  ADD_GITLAB=false
  GITLAB_GROUP_REPO_URL=""
  GITLAB_TOKEN=""
  
  read -p "Do you want to enable GitLab Maven repository access? (y/N) " use_gitlab
  use_gitlab=${use_gitlab:-N}
  if [[ "$use_gitlab" =~ ^[Yy]$ ]]; then
    ADD_GITLAB=true
    echo "Enter GitLab group Maven repository URL:"
    read -p "  [https://gitlab.evoforge.org/api/v4/groups/3506/-/packages/maven]: " GITLAB_GROUP_REPO_URL
    GITLAB_GROUP_REPO_URL=${GITLAB_GROUP_REPO_URL:-https://gitlab.evoforge.org/api/v4/groups/3506/-/packages/maven}

    echo "Enter GitLab Personal Access Token:"
    read -s -p "GitLab token: " GITLAB_TOKEN
    echo
  fi

  # 3) Artifactory settings (optional)
  ADD_ARTIFACTORY=false
  ARTIFACTORY_URL=""
  
  read -p "Do you want to enable Artifactory access? (y/N) " use_art
  use_art=${use_art:-N}
  if [[ "$use_art" =~ ^[Yy]$ ]]; then
    ADD_ARTIFACTORY=true
    read -p "Enter Artifactory repository URL [https://artifactory.adv.evoforge.org/artifactory/jblocks-libs-release]: " ARTIFACTORY_URL
    ARTIFACTORY_URL=${ARTIFACTORY_URL:-https://artifactory.adv.evoforge.org/artifactory/jblocks-libs-release}
  fi

  # ----------------------------
  # Build settings.xml
  # ----------------------------
  
  info "Building new settings.xml..."

  # Compose <servers> block based on inputs
  servers_block=""
  if [ "${ADD_GITLAB}" = true ]; then
    # We'll store token in settings.xml httpHeaders property.
    # GitLab expects the Private-Token header for authentication.
    servers_block="${servers_block}
    <server>
      <id>gitlab-maven-slate</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Private-Token</name>
            <value>${GITLAB_TOKEN}</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
    <server>
      <id>gitlab-maven-mws-access</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Private-Token</name>
            <value>${GITLAB_TOKEN}</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>"
  fi

  if [ "${ADD_ARTIFACTORY}" = true ]; then
    servers_block="${servers_block}
    <server>
      <id>jblocks-artifactory</id>
      <username></username>
      <password></password>
    </server>"
  fi

  # Compose <mirrors> to prefer local offline repo (mirrorOf="*")
  mirrors_block="<mirrors>
    <mirror>
      <id>csdk-offline-mirror</id>
      <name>CSDK offline mirror</name>
      <url>${offline_repo_url}</url>
      <mirrorOf>*</mirrorOf>
    </mirror>
  </mirrors>"

  # Compose <profiles> to include remote repos as a profile (so user can enable/disable)
  remote_repos_block=""
  if [ "${ADD_GITLAB}" = true ]; then
    remote_repos_block="${remote_repos_block}
          <repository>
            <id>gitlab-maven-slate</id>
            <url>${GITLAB_GROUP_REPO_URL}</url>
          </repository>
          <repository>
            <id>gitlab-maven-mws-access</id>
            <url>https://gitlab.evoforge.org/api/v4/groups/3341/-/packages/maven</url>
          </repository>"
  fi
  if [ "${ADD_ARTIFACTORY}" = true ]; then
    remote_repos_block="${remote_repos_block}
          <repository>
            <id>jblocks-artifactory</id>
            <url>${ARTIFACTORY_URL}</url>
          </repository>"
  fi

  profiles_block=""
  if [ -n "${remote_repos_block}" ]; then
    profiles_block="<profiles>
    <profile>
      <id>csdk-remote-repos</id>
      <repositories>
${remote_repos_block}
      </repositories>
    </profile>
  </profiles>"
  fi

  # Final settings.xml - include tag so scripts can detect file was modified
  cat > "${MAVEN_SETTINGS}" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
${TAG}
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <!-- Local repository used as an offline mirror / primary source for CSDK -->
  <localRepository>${offline_repo}</localRepository>

  ${mirrors_block}

  <servers>
${servers_block}
  </servers>

  ${profiles_block}

  <!-- By default no activeProfiles are forced. You can activate the 'csdk-remote-repos'
       profile when you want Maven to consult remote (GitLab/Artifactory) repositories:
       mvn -Pcsdk-remote-repos <goals>
  -->
</settings>
EOF

  info "Wrote new settings.xml at ${MAVEN_SETTINGS} (tag: ${TAG})."

  # ----------------------------
  # Create POM template
  # ----------------------------
  
  info "Creating POM template at ${POM_TEMPLATE_PATH}..."
  mkdir -p "${POM_TEMPLATE_DIR}"

  cat > "${POM_TEMPLATE_PATH}" <<'POM_EOF'
<!--
  Template pom.xml for CSDK-based projects.

  - This file was autogenerated by configure_maven.sh
  - It contains repository entries for GitLab and Artifactory (if enabled).
  - Copy this file into your project's root, update groupId/artifactId/version,
    and modify dependencies as needed.

  To use remote repositories (if configured in Maven settings):
    - Activate the remote repos profile:
        mvn -Pcsdk-remote-repos clean install
    - Or add the appropriate <repository> entries directly into your project's pom.xml.

  Note: Do NOT put tokens/credentials in this pom.xml. Credentials belong in ~/.m2/settings.xml (or /opt/maven/conf/settings.xml).
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <!-- CSDK-POM-TEMPLATE -->
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.example.csdk</groupId>
  <artifactId>csdk-sample-project</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <name>csdk-sample-project</name>
  <description>Template POM for CSDK projects. Update and use for development.</description>

  <repositories>
    <!-- GitLab group repo (example) -->
    <repository>
      <id>gitlab-maven-slate</id>
      <url>https://gitlab.evoforge.org/api/v4/groups/3506/-/packages/maven</url>
    </repository>

    <!-- Additional GitLab group (example) -->
    <repository>
      <id>gitlab-maven-mws-access</id>
      <url>https://gitlab.evoforge.org/api/v4/groups/3341/-/packages/maven</url>
    </repository>

    <!-- Artifactory example -->
    <repository>
      <id>jblocks-artifactory</id>
      <url>https://artifactory.adv.evoforge.org/artifactory/jblocks-libs-release</url>
    </repository>
  </repositories>

  <properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
  </properties>

  <dependencies>
    <!-- Add your dependencies here -->
  </dependencies>

  <!-- Helpful instructions -->
  <!--
    How to use:
      1) Copy this file to your project directory as pom.xml
      2) Update groupId / artifactId / version and dependencies
      3) If you want Maven to use remote repositories configured by the installer:
         mvn -Pcsdk-remote-repos clean install
  -->
</project>
POM_EOF

  info "POM template created: ${POM_TEMPLATE_PATH}"

  # Set permissive permissions (but keep owner)
  chmod 644 "${MAVEN_SETTINGS}" 2>/dev/null || true
  chmod 644 "${POM_TEMPLATE_PATH}" 2>/dev/null || true

  cat <<SUMMARY

Maven configuration completed.

Files created/updated:
 - Backup of original settings: ${MAVEN_SETTINGS_ORIG}
 - New settings.xml:            ${MAVEN_SETTINGS}
 - Template POM:                ${POM_TEMPLATE_PATH}

Notes:
 - The new settings.xml contains the tag: ${TAG}
   This allows other scripts to detect whether Maven settings were configured by CSDK.
 - The settings.xml uses the local repo as a mirror (mirrorOf="*") to prefer offline artifacts.
 - Remote repos (GitLab/Artifactory) are placed into a profile 'csdk-remote-repos'.
   Activate it with: mvn -Pcsdk-remote-repos <goals>

If you want the remote repos to be consulted automatically without passing -P,
edit ${MAVEN_SETTINGS} and move the <repositories> entries into your global POM
or set <activeProfiles><activeProfile>csdk-remote-repos</activeProfile></activeProfiles>.

SUMMARY
}

# ----------------------------
# Main menu loop
# ----------------------------

while true; do
    echo "--------------------------------------------"
    echo "        Configure Maven Settings"
    echo "--------------------------------------------"
    echo "1) Create new settings.xml (guided)"
    echo "2) Edit existing settings.xml"
    echo "3) View current settings"
    echo "4) Return to Installer Main Menu"
    echo ""
    read -rp "Enter choice: " mode

    case "$mode" in
        1)
            # Create settings.xml
            create_maven_settings
            ;;

        2)
            # Edit settings.xml
            if command -v nano >/dev/null 2>&1; then
                nano "${MAVEN_SETTINGS}"
            elif command -v vi >/dev/null 2>&1; then
                vi "${MAVEN_SETTINGS}"
            else
                warn "No editor found (nano/vi). Please edit manually: ${MAVEN_SETTINGS}"
            fi
            ;;

        3)
            # View settings.xml
            if [ -f "${MAVEN_SETTINGS}" ]; then
                cat "${MAVEN_SETTINGS}"
            else
                warn "Settings file does not exist: ${MAVEN_SETTINGS}"
            fi
            echo ""
            read -p "Press Enter to continue..."
            ;;

        4)
            echo "Returning to main installer menu..."
            exit 0
            ;;

        *)
            echo "Invalid option. Press Enter to continue."
            read
            ;;
    esac
done

