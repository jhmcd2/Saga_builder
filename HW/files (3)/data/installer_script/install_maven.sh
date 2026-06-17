#!/usr/bin/env bash
set -euo pipefail

HOME=$1 
MAVEN_VERSION="3.9.12"
INSTALL_DIR="/opt/maven"
REPO_MAVEN_DIR="$HOME/repository/maven"
LOCAL_ARCHIVE="${REPO_MAVEN_DIR}/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
DOWNLOAD_URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

echo "-------------------------------------------------------"
echo "         Apache Maven Installer (Custom CSDK)          "
echo "-------------------------------------------------------"
echo ""

if [[ -f "$LOCAL_ARCHIVE" ]]; then
    echo "Using local Maven archive:"
    echo "  $LOCAL_ARCHIVE"
    ARCHIVE="$LOCAL_ARCHIVE"
else
    echo "Local archive not found, downloading Maven:"
    echo "  $DOWNLOAD_URL"
    ARCHIVE="/tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    curl -L -o "$ARCHIVE" "$DOWNLOAD_URL"
fi

echo "Extracting Maven to ${INSTALL_DIR} ..."
sudo mkdir -p "$INSTALL_DIR"
sudo tar -xzf "$ARCHIVE" -C "$INSTALL_DIR"

sudo ln -sfn "${INSTALL_DIR}/apache-maven-${MAVEN_VERSION}" "${INSTALL_DIR}/current"
sudo ln -sfn "${INSTALL_DIR}/current/bin/mvn" /usr/local/bin/mvn

cat <<EOF > "${REPO_MAVEN_DIR}/info.txt"
Maven installed:
  Version: ${MAVEN_VERSION}
  Installed into: ${INSTALL_DIR}
  Symlink: /usr/local/bin/mvn
Install timestamp: $(date)
EOF

echo ""
echo "Maven installation completed!"

# IMPORTANT: Always call mvn via the absolute path
sudo /usr/local/bin/mvn -version

echo "Returning..."
