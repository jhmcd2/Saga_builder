#!/usr/bin/env bash
# Hub_install_podman.sh
# Podman-only Hub installer that:
#  - auto-discovers distribution base dir
#  - loads images (version-agnostic tar matching)
#  - writes env files (IRB writeEnv verbatim)
#  - writes compose files (IRB heredocs, image substituted)
#  - runs podman compose up -d per service
#
# Usage: Hub_install_podman.sh <DOMAIN> [--dry-run]
set -euo pipefail

LOG_DIR="/var/log/csdk"
LOG_FILE="${LOG_DIR}/hub_podman_install.log"
WORK_PARENT_DIR="szcsdk/csdk-dist-2025-09/repository/CSHUB/apps"   # top-level location to generate per-service compose dirs
DRY_RUN=0
KEYSTORE_DIR="/opt/server_keystore"
debug=0

mkdir -p "$KEYSTORE_DIR"
mkdir -p "$LOG_DIR"
touch "$LOG_FILE"
mkdir -p "$WORK_PARENT_DIR"

# Simple logger
log() {
  printf '%s %s\n' "$(date -u '+%Y-%m-%dT%H:%M:%SZ')" "$*" | tee -a "$LOG_FILE"
}

# Clear screen if not in debug mode
cls() {
  if [ $debug = 0 ]; then
    clear
  fi
}

# Collect input with confirmation loop
collect() {
  exitLoop=0
  while [ $exitLoop = 0 ]; do
    read -p "$1 => " retVal
    retVal=$(echo $retVal | sed 's/ //g')
    cls
    _yn=0; yesNo "You entered ${retVal}.  Is this correct" || _yn=$?
    if [ $_yn = 1 ]; then
      exitLoop=1
    else
      read -p "Press [Enter] to try again."
      cls
    fi
  done
}

# Input validation for yes/no questions
yesNo() {
  accept=0
  while [ $accept = 0 ]; do
    cls
    read -p "${1}? [y/N] => " yN
    cls
    case $yN in
      y|Y|yes|YES|Yes)
        accept=1; return 1 ;;
      n|N|no|NO|No|"")
        accept=1; return 0 ;;
      *)
        echo "ERROR:"
        echo "      You entered ${yN}.  Valid responses are y,n,Y,N,Yes,No,YES,NO, or No."
        echo ""
        read -p "Press [Enter] to try again"
        ;;
    esac
  done
}

# Display formatted error messages
errorMsg() {
  cls
  echo "ERROR:"
  echo "      ${1}"
  echo ""
  echo ""
  if [ ! -z "$(echo $2 | sed 's/ //g')" ]; then
    read -p "Press [Enter] key to ${2}"
  else
    exit 1
  fi
}

# Display menu title with underline
menuTitle() {
  titleLen=$(echo $1 | wc -c)
  echo $1 | sed "s/_/ /g"
  printf "%0.s═" $(seq 2 $titleLen)
  echo ""
}

# Menu creation function
makeMenu() {
  idx=0
  menuItem="NotSet"
  menuList=($@)

  menuTitle "${mnuTitle}"
  while [ $menuItem == "NotSet" ]; do
    while [ ${menuList[${idx}]} != "Exit" ] && [ ${menuList[${idx}]} != "NoExit" ]; do
      echo "$(( $idx + 1 )). ${menuList[${idx}]}" | sed 's/?/ /g'
      idx=$(( $idx + 1 ))
    done

    if [ ${menuList[-1]} == "Exit" ]; then
      echo "e. Exit"
      echo ""
      read -p "Enter menu option [1-${idx},e] => " mnuInp
    else
      echo ""
      read -p "Enter menu option [1-${idx}] => " mnuInp
    fi

    cls
    if [ -z "$mnuInp" ]; then
      echo "ERROR:"; echo "      Responses cannot be blank."
      read -p "      Press the [Enter] key to retry."
      idx=0; cls; menuTitle "${mnuTitle}"
    elif [ ${menuList[-1]} == "Exit" ] && [ "$mnuInp" = "e" ]; then
      menuItem="Exit"
    elif [ -z "$(echo $mnuInp | tr -d [:digit:])" ]; then
      if [ $mnuInp -lt 1 ] || [ $mnuInp -gt $idx ]; then
        echo "ERROR:"; echo "      $mnuInp is an invalid response."
        read -p "      Press the [Enter] key to retry."
        idx=0; cls; menuTitle "${mnuTitle}"
      else
        menuItem=${menuList[$(( $mnuInp - 1 ))]}
      fi
    else
      echo "ERROR:"; echo "      $mnuInp is an invalid response."
      read -p "      Press the [Enter] key to retry."
      idx=0; cls; menuTitle "${mnuTitle}"
    fi
  done
}

# Alternate/custom classification value entry
otherClass() {
  cls
  retVal=NotSet
  retExit=0
  while [ $retExit = 0 ]; do
    menuTitle "Other ${1}"
    echo ""
    read -p "Enter $1 value => " retVal
    cls
    _yn=0; yesNo "You entered ${retVal}.  Is this correct" || _yn=$?
    if [ $_yn = 0 ]; then
      cls; read -p "Press [Enter] key to try again"; cls
    else
      retExit=1
    fi
  done
}

# Select file from cert directory via menu
selectFile() {
  unset tmpArray
  unset tmpIdx
  tmpIdx=0
  mnuTitle=$1
  if [ $# -lt 3 ] || [ -z "${3:-}" ]; then
    if [ $# -lt 2 ] || [ -z "${2:-}" ]; then
      for file in $(find $certDir -maxdepth 1 -type f); do
        strippedFile=$(basename $file)
        tmpArray[$tmpIdx]=$(echo $strippedFile)
        tmpIdx=$(( ${tmpIdx:-0} + 1 ))
      done
    else
      for file in $(find $certDir -maxdepth 1 -type f | grep -v "$2"); do
        strippedFile=$(basename $file)
        tmpArray[$tmpIdx]=$(echo $strippedFile)
        tmpIdx=$(( ${tmpIdx:-0} + 1 ))
      done
    fi
  else
    for file in $(find $certDir -maxdepth 1 -type f | grep -v "$2" | grep -v "$3"); do
      strippedFile=$(basename $file)
      tmpArray[$tmpIdx]=$(echo $strippedFile)
        tmpIdx=$(( ${tmpIdx:-0} + 1 ))
    done
  fi
  makeMenu ${tmpArray[@]} "NoExit"
}

# Prompt user for password (silent input)
getPasswd() {
  read -s -p "Enter $1 password => " PW
  echo ""
}

# Verify keystore/truststore is the expected type
chkStoreType() {
  # call with filename ($1) store password ($2) and expected type ($3)
  tmpFile=/tmp/csdk_store_check.txt
  echo "${2}" | keytool -list -keystore "${1}" &> $tmpFile || true
  cls
  typeNum=$(grep -i "type: ${3}" $tmpFile | wc -l)
  if [ $typeNum = 0 ]; then
    rm -f $tmpFile
    errorMsg "File $1 is not expected store type ${3}." "retry."
  else
    rm -f $tmpFile
  fi
}

# Ensure permissions on a directory are at least 755
fixDirPerms() {
  perms=$(stat -c %a "${1}")
  for (( char = 0 ; char < ${#perms}; ++char)); do
    if [ $char = 0 ] && [ ${perms:$char:1} -lt 7 ]; then chmod u+rwx "$1"; fi
    if [ $char = 1 ] && [ ${perms:$char:1} -lt 5 ]; then chmod g+rx "$1"; fi
    if [ $char = 2 ] && [ ${perms:$char:1} -lt 5 ]; then chmod o+rx "$1"; fi
  done
}

# Set Classification, Owner, Rel To, SCI Controls
setClass() {
  cls
  mnuTitle="Select Classification"
  makeMenu U R C S TS NoExit
  case $menuItem in
    U)
      classification=$menuItem
      mnuTitle="Select Owner"
      makeMenu $(cat ${ownerFile}) "Other" "NoExit"
      if [ $menuItem == "Other" ]; then otherClass "Owner"; owner=$retVal
      else owner=$menuItem; fi
      ;;
    R|C|S|TS)
      classification=$menuItem
      mnuTitle="Select Rel To"
      makeMenu $(cat ${relToFile}) "Other" "NoExit"
      if [ $menuItem == "Other" ]; then otherClass "Rel To"; relTo=$retVal
      else relTo=$menuItem; fi

      mnuTitle="Select Owner"
      makeMenu $(cat ${ownerFile}) "Other" "NoExit"
      if [ $menuItem == "Other" ]; then otherClass "Owner"; owner=$retVal
      else owner=$menuItem; fi

      mnuTitle="Select SCI Controls"
      makeMenu $(cat ${sciFile}) "[None]" "Other" "NoExit"
      if [ $menuItem == "Other" ]; then otherClass "SCI Controls"; sciControl=$retVal
      elif [ $menuItem == "[None]" ]; then sciControl=
      else sciControl=$menuItem; fi
      ;;
  esac
}

usage() {
  cat <<EOF
Usage: $0 [--dry-run]
  --dry-run Generate files and show commands but do NOT run podman compose up
EOF
  exit 1
}

# --- args ---
# Accept optional positional domain arg (passed by csdk-installer.sh), then flags
DOMAIN_ARG=""
while [ $# -gt 0 ]; do
  case "$1" in
    --dry-run) DRY_RUN=1 ;;
    debug) debug=1; set -x ;;
    *)
      # Treat first non-flag arg as the domain (matches Docker installer behaviour)
      if [ -z "$DOMAIN_ARG" ] && [[ "$1" != --* ]]; then
        DOMAIN_ARG="$1"
      else
        usage
      fi
      ;;
  esac
  shift
done

if [ "$(id -u)" -ne 0 ]; then
  log "ERROR: This script must be run as root."
  exit 2
fi

# Auto-discover distribution directory (works regardless of versioned dir name)
INSTALLER_DIR="$(cd "$(dirname "$0")" && pwd)"
DIST_DIR="$(dirname "$INSTALLER_DIR")"    # csdk-dist-YYYY-MM
IMAGE_DIR="${DIST_DIR}/repository/CSHUB/images"

log "Installer dir: ${INSTALLER_DIR}"
log "Distribution dir: ${DIST_DIR}"
log "Image dir: ${IMAGE_DIR}"
log "Work parent dir (compose output): ${WORK_PARENT_DIR}"
log "dry-run=${DRY_RUN}"

# Basic host info — matches Docker installer method exactly
FQDN=$(hostname -f)
IP=$(hostname -i | sed 's/ //g')
hubHost=$IP
shortName=$(hostname -s)

# Podman compose command
if command -v podman >/dev/null 2>&1; then
  if podman compose version >/dev/null 2>&1; then
    COMPOSE_CMD="podman compose"
  elif command -v podman-compose >/dev/null 2>&1; then
    COMPOSE_CMD="podman-compose"
  else
    COMPOSE_CMD="podman compose"
  fi
else
  log "ERROR: podman not found in PATH."
  exit 3
fi
log "Compose command: ${COMPOSE_CMD}"

# Services (required and optional)
REQUIRED_SERVICES=(common-services cs-sensorsim lasso rffs silkwave)
OPTIONAL_SERVICES=(appgeo-proxy)
ALL_SERVICES=("${REQUIRED_SERVICES[@]}" "${OPTIONAL_SERVICES[@]}")

# Storage for discovered image specs (associative array)
declare -A IMG_SPEC

# Helper: find image tar for a service by matching prefix 'iceroad-image-<svc>'
find_image_tar() {
  local svc="$1"
  # Look for common patterns (case-insensitive)
  local pattern="iceroad-image-${svc}_*.tar*"
  # Also fallback to pattern without underscore or with -ir
  local alt1="iceroad-image-${svc}*.tar*"
  local found=""
  if [ -d "${IMAGE_DIR}" ]; then
    # First try the underscore pattern
    found="$(ls "${IMAGE_DIR}"/${pattern} 2>/dev/null | head -n1 || true)"
    if [ -z "$found" ]; then
      found="$(ls "${IMAGE_DIR}"/${alt1} 2>/dev/null | head -n1 || true)"
    fi
  fi
  echo "$found"
}


# loadImg: load the tarball into podman and attempt to determine image spec (repo:tag)
loadImg() {
  local svc="$1"
  log "Loading image for service: ${svc}"
  local tarfile
  tarfile="$(find_image_tar "${svc}")"
  if [ -z "$tarfile" ]; then
    log "ERROR: image tarball for '${svc}' not found in ${IMAGE_DIR}"
    return 1
  fi
  log "Found tarball: ${tarfile}"

  # Podman load; capture output
  if [ "${DRY_RUN}" -eq 1 ]; then
    log "[DRY-RUN] Would run: podman load -i ${tarfile}"
    # We cannot determine image spec in dry-run; set placeholder
    IMG_SPEC["$svc"]="iceroad/${svc}:latest"
    return 0
  fi

  # Load and capture potential "Loaded image" output
  local load_out
  if load_out="$(podman load -i "${tarfile}" 2>&1)"; then
    log "podman load output: $(echo "${load_out}" | tr '\n' ' ' | cut -c1-300)..." 
    # Try to parse standard "Loaded image: repo:tag" or "Loaded image(s): repo:tag"
    local parsed
    parsed="$(printf '%s\n' "${load_out}" | awk -F': ' '/Loaded image/ {print $2; exit}')"
    if [ -n "${parsed}" ]; then
      IMG_SPEC["$svc"]="${parsed}"
      log "Detected image spec for ${svc}: ${parsed}"
      return 0
    fi

    # Fallback: try to infer by looking for image name containing svc keyword
    parsed="$(podman images --format '{{.Repository}}:{{.Tag}} {{.ID}}' | grep -i "${svc//-/_}" | awk '{print $1}' | head -n1 || true)"
    if [ -n "${parsed}" ]; then
      IMG_SPEC["$svc"]="${parsed}"
      log "Inferred image spec for ${svc}: ${parsed}"
      return 0
    fi

    # Final fallback: use filename-based spec (not ideal)
    local base
    base="$(basename "${tarfile}")"
    IMG_SPEC["$svc"]="${base%.tar*}"
    log "Fallback image spec for ${svc}: ${IMG_SPEC[$svc]}"
    return 0
  else
    log "ERROR: podman load failed for ${tarfile}"
    return 1
  fi
}

# writeEnv: exact IRB writeEnv() reproduced verbatim (copied from IRB.sh).
# Source: IRB.sh (writeEnv function provided). :contentReference[oaicite:3]{index=3}
writeEnv() {
  # params: serviceName targetDir
  local svc="$1"; local target="$2"
  mkdir -p "$target"
  case $svc in
        appgeo-proxy)
        cat << EOF > ${target}/${svc}.env
#============================================================
TRANSPORT_AG                 = $transport
SW_HUB_USER_AG               = client
SW_HUB_PASS_AG               = manager
SW_HUB_HOST_AG               = $agIP
SW_HUB_PORT_AG               = tcp:61616,ssl:61617
SW_HUB_DOMAIN_AG             = $agDomain
TRANSPORT_CS                 = $transport
SW_HUB_USER_CS               = client
SW_HUB_PASS_CS               = manager
SW_HUB_HOST_CS               = $hubHost
SW_HUB_PORT_CS               = tcp:61616,ssl:61617
SW_HUB_DOMAIN_CS             = $swDomain
KS_CERT_ALIAS                = $certAlias
KS_PASSWORD                  = $ksPw
KS_PATH                      = /opt/server_keystore/$keyStoreFile
TS_PASSWORD                  = $tsPw
TS_PATH                      = /opt/server_keystore/$trustStoreFile
SIGNING_ENABLED              = false
VALIDATION_ENABLED           = false
DSIG_ALLOW_NO_SIG            = true
USE_TEST_CERTS               = false
CLASS_BASE_CLASS             = U
CLASS_BASE_SCI               =
CLASS_BASE_DISSEM            =
CLASS_BASE_REL               =
CLASS_BASE_OWNER             = $owner
CLASS_BASE_AUTHID            =
CLASS_BASE_CLASSBY           =
CLASS_BASE_CLASSRSN          =
CLASS_BASE_DRVFRM            =
CLASS_BASE_DECLASSDATE       =
CLASS_BASE_DECLASSEXCPT      =
CLASS_BASE_CREATOR           =
CLASS_BASE_CTRLACCESS        =
CLASS_BASE_ALTLABEL          =
CLASS_DEFAULT_CLASS          = $classification
CLASS_DEFAULT_SCI            = $sciControl
CLASS_DEFAULT_DISSEM         =
CLASS_DEFAULT_REL            = $relTo
CLASS_DEFAULT_OWNER          = $owner
CLASS_DEFAULT_AUTHID         =
CLASS_DEFAULT_CLASSBY        =
CLASS_DEFAULT_CLASSRSN       =
CLASS_DEFAULT_DRVFRM         =
CLASS_DEFAULT_DECLASSDATE    =
CLASS_DEFAULT_DECLASSEXCPT   =
CLASS_DEFAULT_CREATOR        =
CLASS_DEFAULT_CTRLACCESS     =
CLASS_DEFAULT_ALTLABEL       =
#============================================================
EOF
        ;;
      common-services)
        if [ $sslEnabled = "true" ]
        then
          storePath=/opt/server_keystore/
        else
          storePath=
        fi

        if [ $classification = "U" ]
        then
          cat << EOF > ${target}/${svc}.env
#============================================================
# Critical Paramaters
TS_PATH=${storePath}$trustStoreFile
TS_PASSWORD=$tsPw
TS_PEM_PATH=${storePath}$trustStoreFile
KS_PEM_PATH=${storePath}$serverCertFile
KS_CERT_TYPE=PKCS12
KS_PATH=${storePath}$keyStoreFile
KS_CERT_ALIAS=$certAlias
KS_P12_PATH=${storePath}$keyStoreFile
KS_PASSWORD=$ksPw
SW_HUB_DOMAIN=$swDomain
SW_HUB_HOST=$hubHost
SW_TRANSPORT=$transport
SW_HUB_PORT=tcp:61616,ssl:61617
HOSTNAME=$hubHost
CLASS_DEFAULT_LEVEL=U
CLASS_MAX_HOST_LEVEL=$classification
CLASS_DEFAULT_OWNER_PRODUCER=$owner
CLASS_OWNER_PRODUCER_IF_NOT_SET=$owner
CLASS_DEFAULT_RELTO=
CLASS_MAX_HOST_RELTO=
CLASS_MAX_HOST_SCI=$sciControl
CSMON_USE_TLS=$sslEnabled
#============================================================
EOF
        else
          cat << EOF > ${target}/${svc}.env
#============================================================
# Critical Paramaters
TS_PATH=${storePath}$trustStoreFile
TS_PASSWORD=$tsPw
TS_PEM_PATH=${storePath}$trustStoreFile
KS_PEM_PATH=${storePath}$serverCertFile
KS_CERT_TYPE=PKCS12
KS_PATH=${storePath}$keyStoreFile
KS_CERT_ALIAS=$certAlias
KS_P12_PATH=${storePath}$keyStoreFile
KS_PASSWORD=$ksPw
SW_HUB_DOMAIN=$swDomain
SW_HUB_HOST=$hubHost
SW_TRANSPORT=$transport
SW_HUB_PORT=tcp:61616,ssl:61617
HOSTNAME=$hubHost
CLASS_DEFAULT_LEVEL=U
CLASS_MAX_HOST_LEVEL=$classification
CLASS_DEFAULT_OWNER_PRODUCER=$owner
CLASS_OWNER_PRODUCER_IF_NOT_SET=$owner
CLASS_DEFAULT_RELTO=
CLASS_MAX_HOST_RELTO=$relTo
CLASS_MAX_HOST_SCI=$sciControl
CSMON_USE_TLS=$sslEnabled
#============================================================
EOF
        fi
        ;;
      cs-sensorsim)
        if [ $sslEnabled = "true" ]
        then
          hubPort=61617
        else
          hubPort=61616
        fi
        cat << EOF > ${target}/${svc}.env
#============================================================
# Critical Paramaters
TS_PATH=/opt/server_keystore/$trustStoreFile
TS_PASSWORD=$tsPw
TS_PEM_PATH=/opt/server_keystore/$trustStoreFile
KS_PEM_PATH=/opt/server_keystore/$serverCertFile
KS_CERT_TYPE=PKCS12
KS_PATH=/opt/server_keystore/$keyStoreFile
KS_CERT_ALIAS=$certAlias
KS_P12_PATH=/opt/server_keystore/$keyStoreFile
KS_PASSWORD=$ksPw
SW_HUB_DOMAIN=$swDomain
SW_HUB_HOST=$hubHost
SW_HUB_PORT=$hubPort
TRANSPORT=$transport
HOSTNAME=localhost
CLASSIFICATION=$classification
CLASS_SCIOUT=$sciControl
CLASS_SCICONT=$sciControl
CLASS_OWNERPRODUCER=$owner
CLASS_RELEASETO=$relTo
SCENARIO_FILE=SpecialSignal.xml
SUFFIX=$shortName
#============================================================
EOF
        sed -i "s/,/ /g" ${target}/${svc}.env
        ;;
      lasso)
        cat << EOF > ${target}/${svc}.env
#============================================================
# Critical Paramaters
TS_PATH=/opt/server_keystore/$trustStoreFile
TS_PASSWORD=$tsPw
TS_PEM_PATH=/opt/server_keystore/$trustStoreFile
KS_PEM_PATH=/opt/server_keystore/$serverCertFile
KS_CERT_TYPE=PKCS12
KS_PATH=/opt/server_keystore/$keyStoreFile
KS_CERT_ALIAS=$certAlias
KS_P12_PATH=/opt/server_keystore/$keyStoreFile
KS_PASSWORD=$ksPw
SW_HUB_DOMAIN=$swDomain
SW_HUB_HOST=$hubHost
SW_TRANSPORT=$transport
SW_HUB_PORT=tcp:61616,ssl:61617
HOSTNAME=$hubHost
CLASS_DEFAULT_LEVEL=U
CLASS_MAX_HOST_LEVEL=$classification
CLASS_DEFAULT_OWNER_PRODUCER=$owner
CLASS_DEFAULT_RELTO=
CLASS_MAX_HOST_RELTO=$relTo
CLASS_MAX_HOST_SCI=$sciControl
#============================================================
EOF
        ;;
      rffs)
        if [ $sslEnabled = "false" ]
        then
          tsPw=NA
          ksPw=NA
          certAlias=NA
        fi
        cat << EOF > ${target}/${svc}.env
#============================================================
# Critical Paramaters
TS_PATH=/opt/server_keystore/$trustStoreFile
TS_PASSWORD=$tsPw
TS_PEM_PATH=/opt/server_keystore/$trustStoreFile
KS_PEM_PATH=/opt/server_keystore/$serverCertFile
KS_CERT_TYPE=PKCS12
KS_PATH=/opt/server_keystore/$keyStoreFile
KS_CERT_ALIAS=$certAlias
KS_P12_PATH=/opt/server_keystore/$keyStoreFile
KS_PASSWORD=$ksPw
SW_HUB_DOMAIN=$swDomain
SW_HUB_HOST=$hubHost
SW_TRANSPORT=$transport
SW_HUB_PORT=tcp:61616,ssl:61617
HOSTNAME=$hubHost
CLASS_DEFAULT_LEVEL=U
CLASS_DEFAULT_RELTO=
CLASS_DEFAULT_OWNER_PRODUCER=$owner
CLASS_MAX_HOST_LEVEL=$classification
CLASS_MAX_HOST_RELTO=$relTo
CLASS_MAX_HOST_SCI=$sciControl
SIGNING_ENABLED=false
VALIDATION_ENABLED=false
VALIDATION_VERIFY_TRUST=false
DSIG_ALLOW_NO_SIG=true
DSIG_ALLOW_INVAL_SIG=true
#============================================================
EOF
        ;;
      silkwave)
        cat << EOF > ${target}/${svc}.env
#============================================================
# Hub info
SW_HUB_DOMAIN=$swDomain
SW_CALLBACK_HOST=$hubHost

# Classification
CLASS_SW_HUB_CLASS=$classification
CLASS_SW_HUB_SCI=$sciControl
CLASS_SW_HUB_REL=$relTo
CLASS_SW_HUB_OWNER=$owner
CLASS_SW_BASE_OWNER=$owner

# Run Mode
TCP_ENABLED=true
SSL_ENABLED=$sslEnabled
STOMP_SSL_ENABLED=$sslEnabled
STOMP_TCP_ENABLED=true

# Server Keys
KS_PATH=/opt/server_keystore/$keyStoreFile
KS_TYPE=PKCS12
KS_PASSWORD=$ksPw
KS_CERT_ALIAS=$certAlias
TS_PATH=/opt/server_keystore/$trustStoreFile
TS_TYPE=JKS
TS_PASSWORD=$tsPw

# Java Configs
SW_MIN_HEAP_SIZE_GB=1
SW_MAX_HEAP_SIZE_GB=2
#============================================================
EOF
        ;;
  esac
}

# Classification reference files 
relToFile="${DIST_DIR}/repository/CSHUB/classification/relto.txt"
sciFile="${DIST_DIR}/repository/CSHUB/classification/sci.txt"
ownerFile="${DIST_DIR}/repository/CSHUB/classification/owner.txt"
certDir="/opt/server_keystore"

# SSL validation state
sslEnabled="false"
transport="tcp"
ksValid=0
svrCertValid=0
tsValid=0

# Default IRB variables (overwritten by interactive prompts below)
agIP=""
agDomain=""
hubHost="${IP}"
swDomain=""
certAlias=""
ksPw=""
keyStoreFile=""
trustStoreFile=""
serverCertFile=""
tsPw=""
classification="U"
sciControl=""
owner=""
relTo=""

cls

# --Get SW domain-- #
if [ -z "$DOMAIN_ARG" ] || [ "$DOMAIN_ARG" = 'debug' ]; then
  collect "Enter SW Domain"
  swDomain=$retVal
else
  swDomain="$DOMAIN_ARG"
fi
DOMAIN="$swDomain"
log "Domain/FQDN: ${FQDN}, SW Domain: ${DOMAIN}, dry-run=${DRY_RUN}"

# --Set domain's classification level-- #
setClass

# --Determine if AppGeo Proxy is being installed-- #
_yn=0; yesNo "Deploy AppGeo Proxy" || _yn=$?
if [ $_yn = 1 ]; then
  WANT_APPGEO=1
  collect "Enter appgeo domain"
  agDomain=$retVal
  collect "Enter appgeo server IP"
  agIP=$retVal
  loopExit=0
  while [ $loopExit = 0 ]; do
    if [[ $agIP =~ ^(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.)(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$ ]]; then
      loopExit=1
    else
      errorMsg "$agIP is not a valid IPv4 IP address" "try again."
      cls
      collect "Enter appgeo server IP"
      agIP=$retVal
    fi
  done
else
  WANT_APPGEO=0
  agDomain="${FQDN}"
  agIP="${IP}"
fi

# --Determine if SSL will be enabled-- #
_yn=0; yesNo "Enable SSL" || _yn=$?
if [ $_yn = 1 ]; then
  transport="ssl"
  sslEnabled="true"
  hubHost="${FQDN}"

  # Determine Certificate Directory
  _yn=0; yesNo "The current certificate directory is ${certDir}/.  Use this directory" || _yn=$?
  if [ $_yn = 0 ]; then
    loopExit=0
    while [ $loopExit = 0 ]; do
      clear
      read -p "Enter new directory path => " certDir
      clear
      if [ ! -e "$certDir" ]; then
        errorMsg "$certDir does not exist." "enter a different certificate directory."
      elif [ -z "$( ls -A $certDir )" ]; then
        errorMsg "$certDir is empty." "enter a different certificate directory."
      else
        loopExit=1
      fi
    done
  else
    if [ ! -e "$certDir" ]; then
      errorMsg "$certDir does not exist." "enter a different certificate directory."
      loopExit=0
      while [ $loopExit = 0 ]; do
        clear
        read -p "Enter new directory path => " certDir
        clear
        if [ ! -e "$certDir" ]; then
          errorMsg "$certDir does not exist." "enter a different certificate directory."
        elif [ -z "$( ls -A $certDir )" ]; then
          errorMsg "$certDir is empty." "enter a different certificate directory."
        else
          loopExit=1
        fi
      done
    fi
  fi
  KEYSTORE_DIR="$certDir"
  fixDirPerms "$certDir"

  # Select server certificate (PEM)
  while [ $svrCertValid = 0 ]; do
    selectFile Select_Certificate_Authority_File_[PEM_format]
    serverCert="${certDir}/${menuItem}"
    serverCertFile="${menuItem}"
    if openssl x509 -inform PEM -in "$serverCert" -noout 2>/dev/null; then
      svrCertValid=1; cls
    else
      errorMsg "$serverCert is not a PEM formatted certificate." "retry."; cls
    fi
  done

  # Select keystore and validate
  while [ $ksValid = 0 ]; do
    selectFile Select_Keystore_File "$serverCert"
    keyStore="${certDir}/${menuItem}"
    keyStoreFile="${menuItem}"
    getPasswd keystore
    ksPw="${PW}"
    if echo "${ksPw}" | keytool -list -keystore "$keyStore" &>/dev/null; then
      ksValid=1; cls
    else
      errorMsg "Either password or keystore file is invalid." "retry."; cls
    fi
  done
  chkStoreType "$keyStore" "$ksPw" PKCS12

  # Select truststore and validate
  clear
  while [ $tsValid = 0 ]; do
    selectFile Select_Truststore_File "$serverCert" "$keyStore"
    trustStore="${certDir}/${menuItem}"
    trustStoreFile="${menuItem}"
    getPasswd truststore
    tsPw="${PW}"
    if echo "${tsPw}" | keytool -list -keystore "$trustStore" &>/dev/null; then
      tsValid=1; clear
    else
      errorMsg "Either password or truststore file is invalid." "retry."; clear
    fi
  done
  chkStoreType "$trustStore" "$tsPw" JKS

  # Extract cert alias from keystore
  tmpFile=/tmp/csdk_ks_list.txt
  echo "${ksPw}" | keytool -v -list -keystore "${keyStore}" &> $tmpFile || true
  cls
  certAlias=$(grep "Alias name:" $tmpFile | awk '{print $3}')
  rm -f $tmpFile
fi

# --- Ensure firewalls are disabled (matches Docker installer) ---
for fwService in iptables firewalld nftables; do
  if systemctl is-active --quiet $fwService 2>/dev/null; then
    log "Stopping and disabling ${fwService}"
    systemctl stop --quiet $fwService 2>/dev/null || true
    systemctl disable --quiet $fwService 2>/dev/null || true
  fi
done

# --- Load images for required services ---
# The original Hub installer loaded images first; mirror that behavior.
SERVICES_TO_LOAD=("${REQUIRED_SERVICES[@]}")
if [ "${WANT_APPGEO}" -eq 1 ]; then
  SERVICES_TO_LOAD+=("appgeo-proxy")
fi

log "Loading images for: ${SERVICES_TO_LOAD[*]}"
for svc in "${SERVICES_TO_LOAD[@]}"; do
  if loadImg "${svc}"; then
    log "Loaded image for ${svc}: ${IMG_SPEC[$svc]}"
  else
    log "ERROR: Could not load image for ${svc}. Aborting."
    exit 4
  fi
done

# --- Generate compose dirs, env files (using writeEnv), and compose files ---
# IRB wrote compose per-app; we'll replicate and substitute image spec into the compose
write_compose_appgeo_proxy() {
  local dir="$1"
  mkdir -p "$dir"
  cat > "${dir}/docker-compose.yaml" <<EOF
version: '3.5'

services:
 appgeo-proxy:
    image: ${IMG_SPEC["appgeo-proxy"]}
    privileged: true
    container_name: appgeo-proxy
    hostname: appgeo-proxy
    command: ["/tmp/container_exec.sh"]
    env_file: ./appgeo-proxy.env
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: "1"
          memory: 2G
        reservations:
          cpus: "0.5"
          memory: 1G
EOF
  log "Wrote compose for appgeo-proxy -> ${dir}/docker-compose.yaml"
}

write_compose_common_services() {
  local dir="$1"
  mkdir -p "$dir"
  # The IRB block used ${3} as keystore mount; we will document mount separately; keep same shape
  cat > "${dir}/docker-compose.yaml" <<EOF
version: '3.5'

services:
 common-services:
    image: ${IMG_SPEC["common-services"]}
    privileged: true
    container_name: common-services
    hostname: common-services
    command: ["/tmp/container_exec_shell.sh"]
    env_file: ./common-services.env
    ports:
      - "5154:5154"
    volumes:
      - ${KEYSTORE_DIR}:/opt/server_keystore
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: "1"
          memory: 3G
        reservations:
          cpus: "0.5"
          memory: 512M
EOF
  log "Wrote compose for common-services -> ${dir}/docker-compose.yaml"
}

write_compose_cs_sensorsim() {
  local dir="$1"
  mkdir -p "$dir"
  cat > "${dir}/docker-compose.yaml" <<EOF
version: '3.5'

services:
 cs-sensorsim:
    image: ${IMG_SPEC["cs-sensorsim"]}
    privileged: true
    container_name: cs-sensorsim
    hostname: cs-sensorsim
    network_mode: host
    command: ["/tmp/container_exec.sh"]
    env_file: ./cs-sensorsim.env
    volumes:
      - ${KEYSTORE_DIR}:/opt/server_keystore
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: "1"
          memory: 2G
        reservations:
          cpus: "0.5"
          memory: 128M
EOF
  log "Wrote compose for cs-sensorsim -> ${dir}/docker-compose.yaml"
}

write_compose_lasso() {
  local dir="$1"
  mkdir -p "$dir"
  cat > "${dir}/docker-compose.yaml" <<EOF
version: '3.5'

services:
 lasso:
    image: ${IMG_SPEC["lasso"]}
    privileged: true
    container_name: lasso
    hostname: lasso
    command: ["/tmp/container_exec.sh"]
    env_file: ./lasso.env
    volumes:
      - ${KEYSTORE_DIR}:/opt/server_keystore
      - /var/staging/h/MapData/data:/h/MapData/data
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: "1"
          memory: 2G
        reservations:
          cpus: "0.5"
          memory: 1G
EOF
  log "Wrote compose for lasso -> ${dir}/docker-compose.yaml"
}

write_compose_rffs() {
  local dir="$1"
  mkdir -p "$dir"
  cat > "${dir}/docker-compose.yaml" <<EOF
version: '3.5'

services:
 rffs:
    image: ${IMG_SPEC["rffs"]}
    privileged: true
    container_name: rffs
    hostname: rffs
    command: ["/tmp/container_exec_shell.sh"]
    env_file: ./rffs.env
    volumes:
      - ${KEYSTORE_DIR}:/opt/server_keystore
      - /var/staging/h/MapData/data:/h/MapData/data
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: "2"
          memory: 2G
        reservations:
          cpus: "0.5"
          memory: 128M
EOF
  log "Wrote compose for rffs -> ${dir}/docker-compose.yaml"
}

write_compose_silkwave() {
  local dir="$1"
  mkdir -p "$dir"
  cat > "${dir}/docker-compose.yaml" <<EOF
version: '3.2.4'

services:
  silkwave:
    image: ${IMG_SPEC["silkwave"]}
    privileged: true
    env_file: ./silkwave.env
    container_name: silkwave
    hostname: silkwave
    ports:
      - "8123:8123"
      - "8443:8443"
      - "8444:8444"
      - "8501:8501"
      - "8601:8601"
      - "9552:9552/udp"
      - "61616:61616"
      - "61617:61617"
      - "61623:61623"
    volumes:
      - ${KEYSTORE_DIR}:/opt/server_keystore
    restart: unless-stopped
    extra_hosts:
      - "${FQDN}:${IP}"
    deploy:
      resources:
        limits:
          cpus: "2"
          memory: 5G
        reservations:
          cpus: "1"
          memory: 2G
EOF
  log "Wrote compose for silkwave -> ${dir}/docker-compose.yaml"
}

# Create per-service dirs, env files, and compose files
log "Generating env and compose artifacts in ${WORK_PARENT_DIR}"
# required order: common-services first
for svc in "${REQUIRED_SERVICES[@]}"; do
  svcdir="${WORK_PARENT_DIR}/${svc}"
  # write env using IRB writeEnv (populates ${svc}.env in svcdir)
  writeEnv "$svc" "$svcdir"
  # write compose (requires image spec to already be set)
  if [ -z "${IMG_SPEC[$svc]:-}" ]; then
    log "ERROR: image spec for ${svc} not found (missing load). Aborting."
    exit 5
  fi
  case "$svc" in
    common-services) write_compose_common_services "$svcdir" ;;
    cs-sensorsim) write_compose_cs_sensorsim "$svcdir" ;;
    lasso) write_compose_lasso "$svcdir" ;;
    rffs) write_compose_rffs "$svcdir" ;;
    silkwave) write_compose_silkwave "$svcdir" ;;
    *) log "No compose writer for $svc" ;;
  esac
done

# optional appgeo
if [ "${WANT_APPGEO:-0}" -eq 1 ]; then
  svc="appgeo-proxy"
  svcdir="${WORK_PARENT_DIR}/${svc}"
  writeEnv "$svc" "$svcdir"
  if [ -z "${IMG_SPEC[$svc]:-}" ]; then
    log "ERROR: image spec for ${svc} not found (missing load). Aborting."
    exit 5
  fi
  write_compose_appgeo_proxy "$svcdir"
else
  log "Skipping appgeo-proxy per user choice."
fi

# Top-level .env placeholder (not required but helpful)
if [ ! -f "${WORK_PARENT_DIR}/.env" ]; then
  cat > "${WORK_PARENT_DIR}/.env" <<EOF
# top-level env for hub compose artifacts
FQDN=${FQDN}
IP=${IP}
EOF
fi

# --- Extract map data (matches Docker installer) ---
log "Extracting Map Data..."
umask 0022
mkdir -p /var/staging/h/MapData/data
if ls "${DIST_DIR}/repository/CSHUB/maps/"*gz 1>/dev/null 2>&1; then
  tar --warning=no-timestamp -xf "${DIST_DIR}/repository/CSHUB/maps/"*gz -C /var/staging/h/MapData/data 2>&1 | tee -a "${LOG_FILE}" || log "WARNING: map extraction returned non-zero"
  chmod -R u=rX,g=rX,o=rX /var/staging/h
  chown -R root:root /var/staging/h
  log "Map data extracted to /var/staging/h/MapData/data"
else
  log "WARNING: No map archives found in ${DIST_DIR}/repository/CSHUB/maps/ — skipping map extraction"
fi

# --- Run podman compose for each service, in order ---
run_compose_dir() {
  local dir="$1"
  local file="${dir}/docker-compose.yaml"
  if [ ! -f "$file" ]; then
    log "Compose file not found: ${file}, skipping"
    return 0
  fi
  if [ "${DRY_RUN}" -eq 1 ]; then
    log "[DRY-RUN] Would run: ${COMPOSE_CMD} -f ${file} pull"
    log "[DRY-RUN] Would run: ${COMPOSE_CMD} -f ${file} up -d"
    return 0
  fi
  log "Pulling images for ${file} (best-effort)"
  set +e
  ${COMPOSE_CMD} -f "${file}" pull 2>&1 | tee -a "${LOG_FILE}"
  set -e
  log "Bringing up ${file}"
  set +e
  ${COMPOSE_CMD} -f "${file}" up -d 2>&1 | tee -a "${LOG_FILE}"
  local up_exit=$?
  set -e
  if [ ${up_exit} -ne 0 ]; then
    log "ERROR: compose up failed for ${file} (exit ${up_exit}). See ${LOG_FILE}"
    ${COMPOSE_CMD} -f "${file}" ps >> "${LOG_FILE}" 2>&1 || true
    podman ps -a >> "${LOG_FILE}" 2>&1 || true
    return ${up_exit}
  fi
  return 0
}

# Compose order
for svc in "${REQUIRED_SERVICES[@]}"; do
  run_compose_dir "${WORK_PARENT_DIR}/${svc}" || log "Warning: bring-up returned non-zero for ${svc}"
done
if [ "${WANT_APPGEO:-0}" -eq 1 ]; then
  run_compose_dir "${WORK_PARENT_DIR}/appgeo-proxy" || log "Warning: bring-up returned non-zero for appgeo-proxy"
fi

log "Hub install podman run completed."
if [ "${DRY_RUN}" -eq 1 ]; then
  log "Dry-run: files written under ${WORK_PARENT_DIR}. No containers started."
fi
log "Check ${LOG_FILE} for details."

# Print short summary for operator
cat <<EOF
Hub Podman installer finished.
Generated compose dirs: $(ls -1 "${WORK_PARENT_DIR}" | tr '\n' ' ')
To view logs: sudo tail -n 200 ${LOG_FILE}
To list containers: sudo podman ps -a
EOF

exit 0