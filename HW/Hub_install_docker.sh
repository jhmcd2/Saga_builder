#!/bin/bash
clear

#╔════════════╗#
#║ Variables  ║#
#╚════════════╝#

agProxy=0
debug=0

# File/Directory locations #
binDir=$(dirname $0)
if [ $binDir == "." ]
then
  binDir=$(pwd)
fi
workDir=$(dirname $binDir)                       # csdk-dist-2025-09
certDir=${workDir}/repository/CSHUB             # Certificates
workTempDir=${workDir}/tmp
workAppDir=${workDir}/repository/CSHUB/apps
relToFile=${workDir}/repository/CSHUB/classification/relto.txt
sciFile=${workDir}/repository/CSHUB/classification/sci.txt
ownerFile=${workDir}/repository/CSHUB/classification/owner.txt
imagesDir=${workDir}/repository/CSHUB/images
mapsDir=${workDir}/repository/CSHUB/maps
dockerDir=${workDir}/repository/CSHUB/docker
DATA_DIR="${workDir}/data"
LOG_DIR="${workDir}/logs"
KEYSTORE_DIR="${workDir}/keystore"
MAP_DATA_DIR="${workDir}/maps"


# Host info #
FQDN=$(hostname -f)
IP=$(hostname -i | sed 's/ //g')
hubHost=$IP
shortName=$(hostname -s)
source /etc/os-release
osRelease=$(echo $PLATFORM_ID | cut -d: -f2)

# SSL Options #
sslEnabled=false
transport=tcp
ksValid=0
svrCertValid=0
tsValid=0

if [ -f /proc/sys/crypto/fips_enabled ]
then
  if [ $(cat /proc/sys/crypto/fips_enabled) != 0 ]
  then
    ktoolOpt=$(echo "-J-Dcom.redhat.fips=false")
  fi
fi


#╔═══════════╗#
#║ Functions ║#
#╚═══════════╝#

 # Check to see if an RPM is installed #
checkRPM () {

  rpm -qa | grep $1 &> /dev/null
  return $?

}

 # Clear screen if not in debug mode #
cls () {

  if [ $debug = 0 ]
  then
    clear
  fi

}

 # Verify keystore/truststore is the expected type #
chkStoreType () {
  # call with filename ($1) store password ($2) and expected type ($3)
  tmpFile=out.txt
  echo "${2}" | keytool -list -keystore ${1} ${ktoolOpt} &> $tmpFile
  cls
  typeNum=$(grep -i "type: ${3}" $tmpFile | wc -l)
  if [ $typeNum = 0 ]
  then
    rm -f $tmpFile
    errorMsg "File $1 is not expected store type ${3}."
  else
    rm -f $tmpFile
  fi
}

 # Collect input and perform input validation
collect () {

  exitLoop=0
  while [ $exitLoop = 0 ]
  do
    read -p "$1 => " retVal
    retVal=$(echo $retVal | sed 's/ //g')
    cls
    yesNo "You entered ${retVal}.  Is this correct"
    if [ $? = 1 ]
    then
      exitLoop=1
    else
      read -p "Press [Enter] to try again."
      cls
    fi
  done
}

 # Display formatted error messages by calling errorMsg "<message>", then exit or continue with custom text.
errorMsg () {

  cls
  echo "ERROR:"
  echo "      ${1}"
  echo ""
  echo ""
  echo ""
  echo ""
  if [ ! -z $(echo $2 | sed 's/ //g') ]
  then
    read -p "Press [Enter] key to ${2}"
  else
    exit
  fi

}

 # Ensure Permissions on a directory are at least 755
fixDirPerms () {
  # call with file path/name

  perms=$(stat -c %a ${1})
  for (( char = 0 ; char < ${#perms}; ++char))
  do
    if [ $char = 0 ] && [ ${perms:$char:1} -lt 7 ]
    then
      chmod u+rwx $1
    fi

    if [ $char = 1 ] && [ ${perms:$char:1} -lt 5 ]
    then
      chmod g+rx $1
    fi

    if [ $char = 2 ] && [ ${perms:$char:1} -lt 5 ]
    then
      chmod o+rx $1
    fi
  done
}

 # Prompt user for password
getPasswd () {
  # call with optional password description ($1)
  read -s -p "Enter $1 password => " PW
}

 # Load delivered images into docker
loadImg () {

  echo "  - $1"
  IMG=$(ls ${workDir}/repository/CSHUB/images/* | grep -i iceroad-image-$1)

  docker load -q < $IMG &> /dev/null &
  PID=$!
  spinner $PID
  wait $PID
  if [ $? = 0 ]
  then
    echo "    └ Loaded"
  else
    errorMsg "${IMG} failed to load"
  fi

}

 # Yum install RPMs by calling loadPkg <RPM package name> [local] [<path to rpm if local>]
loadPkg () {

  echo "Installing ${1}..."
  # Check to see if package is already installed, install if not
  checkRPM $1
  if [ $? = 0 ]
  then
    echo "  - Already Installed"
  else
    if [ ! -z $2 ] && [ $2 = "local" ]
    then
      rpm -Uvh --nodigest --nofiledigest $3 > /dev/null 2>&1 &
      PID=$!
      spinner $PID
      wait $PID
      returnCode=$?
    else
      yum install -y -q $1 > /dev/null 2>&1 &
      PID=$!
      spinner $PID
      wait $PID
      returnCode=$?
    fi

    # If yum install failed, call errorMsg function
    if [ $returnCode != 0 ]
    then
      errorMsg "${1} failed to install"
    else
      echo "  - Installed"
    fi
  fi

}

 # Menu Creation Function #
makeMenu () {
  # Input Arguments: Menu Items, including "Exit" or "NoExit" as the last item #
  # Output: 'menuItem' variable is set to argument selected from menu #

  idx=0
  menuItem="NotSet"
  menuList=($@)

  menuTitle "${mnuTitle}"
  while [ $menuItem == "NotSet" ]
  do
    while [ ${menuList[${idx}]} != "Exit" ] && [ ${menuList[${idx}]} != "NoExit" ]
    do
      echo "$(( $idx + 1 )). ${menuList[${idx}]}" | sed 's/?/ /g'
      idx=$(( $idx + 1 ))
    done

    if [ ${menuList[-1]} == "Exit" ]
    then
      echo "e. Exit"
      echo ""
      read -p "Enter menu option [1-${idx},e] => " mnuInp
    else
      echo ""
      read -p "Enter menu option [1-${idx}] => " mnuInp
    fi

    cls
    if [ -z $mnuInp ]
    then
      echo "ERROR:"
      echo "      Responses cannot be blank."
      read -p "      Press the [Enter] key to retry."
      idx=0
      cls
      menuTitle "${mnuTitle}"
    elif [ ${menuList[-1]} == "Exit" ] && [ $mnuInp = "e" ]
    then
      menuItem="Exit"
    elif [ -z $( echo $mnuInp | tr -d [:digit:] ) ]
    then
      if [ $mnuInp -lt 1 ] || [ $mnuInp -gt $idx ]
      then
        echo "ERROR:"
        echo "      $mnuInp is an invalid response."
        read -p "      Press the [Enter] key to retry."
        idx=0
        cls
        menuTitle "${mnuTitle}"
      else
        menuItem=${menuList[$(( $mnuInp - 1 ))]}
      fi
    else
      echo "ERROR:"
      echo "      $mnuInp is an invalid response."
      read -p "      Press the [Enter] key to retry."
      idx=0
      cls
      menuTitle "${mnuTitle}"
    fi
  done

}

 # Display menu's title
menuTitle () {
  # Shows the title ($1) and underlines it #

  titleLen=$(echo $1 | wc -c)
  echo $1 | sed "s/_/ /g"
  printf "%0.s═" $(seq 2 $titleLen)
  echo ""
}

 # Alternate Classification Settings
otherClass () {

  cls
  retVal=NotSet
  retExit=0
  while [ $retExit = 0 ]
  do
    menuTitle "Other ${1}"
    echo ""
    read -p "Enter $1 value => " retVal
    cls
    yesNo "You entered ${retVal}.  Is this correct"
    if [ $? = 0 ]
    then
      cls
      read -p "Press [Enter] key to try again"
      cls
    else
      retExit=1
    fi
  done

}

 # Select file from menu
selectFile () {
  #call with menu title ($1) and optionally items to exclude ($2)
  unset tmpArray
  unset tmpIdx
  mnuTitle=$1
  if [ -z $3 ]
  then
    if [ -z $2 ]
    then
      for file in $(find $certDir -maxdepth 1 -type f)
      do
        strippedFile=$(basename $file)
        tmpArray[$tmpIdx]=$(echo $strippedFile)
        tmpIdx=$(( $tmpIdx + 1 ))
      done
    else
      for file in $(find $certDir -maxdepth 1 -type f | grep -v $2)
      do
        strippedFile=$(basename $file)
        tmpArray[$tmpIdx]=$(echo $strippedFile)
        tmpIdx=$(( $tmpIdx + 1 ))
      done
    fi
  else
    for file in $(find $certDir -maxdepth 1 -type f | grep -v $2 | grep -v $3)
    do
      strippedFile=$(basename $file)
      tmpArray[$tmpIdx]=$(echo $strippedFile)
      tmpIdx=$(( $tmpIdx + 1 ))
    done
  fi
  makeMenu ${tmpArray[@]} "NoExit"
}

 # Set Classification and Rel To variables
setClass () {
  cls
  mnuTitle="Select Classification"
  makeMenu U R C S TS NoExit
  case $menuItem in
     U)
       classification=$menuItem

       mnuTitle="Select Owner"
       makeMenu $(cat ${ownerFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Owner"
         owner=$retVal
       else
         owner=$menuItem
       fi
       ;;
     R)
       classification=$menuItem
       mnuTitle="Select Rel To"
       makeMenu $(cat ${relToFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Rel To"
         relTo=$retVal
       else
         relTo=$menuItem
       fi

       mnuTitle="Select Owner"
       makeMenu $(cat ${ownerFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Owner"
         owner=$retVal
       else
         owner=$menuItem
       fi

       mnuTitle="Select SCI Controls"
       makeMenu $(cat ${sciFile}) "[None]" "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "SCI Controls"
         sciControl=$retVal
       elif [ $menuItem == "[None]" ]
       then
         sciControl=
       else
         sciControl=$menuItem
       fi
       ;;
     C)
       classification=$menuItem
       mnuTitle="Select Rel To"
       makeMenu $(cat ${relToFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Rel To"
         relTo=$retVal
       else
         relTo=$menuItem
       fi

       mnuTitle="Select Owner"
       makeMenu $(cat ${ownerFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Owner"
         owner=$retVal
       else
         owner=$menuItem
       fi

       mnuTitle="Select SCI Controls"
       makeMenu $(cat ${sciFile}) "[None]" "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "SCI Controls"
         sciControl=$retVal
       elif [ $menuItem == "[None]" ]
       then
         sciControl=
       else
         sciControl=$menuItem
       fi
       ;;
     S)
       classification=$menuItem
       mnuTitle="Select Rel To"
       makeMenu $(cat ${relToFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Rel To"
         relTo=$retVal
       else
         relTo=$menuItem
       fi

       mnuTitle="Select Owner"
       makeMenu $(cat ${ownerFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Owner"
         owner=$retVal
       else
         owner=$menuItem
       fi

       mnuTitle="Select SCI Controls"
       makeMenu $(cat ${sciFile}) "[None]" "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "SCI Controls"
         sciControl=$retVal
       elif [ $menuItem == "[None]" ]
       then
         sciControl=
       else
         sciControl=$menuItem
       fi
       ;;
    TS)
       classification=$menuItem
       mnuTitle="Select Rel To"
       makeMenu $(cat ${relToFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Rel To"
         relTo=$retVal
       else
         relTo=$menuItem
       fi

       mnuTitle="Select Owner"
       makeMenu $(cat ${ownerFile}) "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "Owner"
         owner=$retVal
       else
         owner=$menuItem
       fi

       mnuTitle="Select SCI Controls"
       makeMenu $(cat ${sciFile}) "[None]" "Other" "NoExit"
       if [ $menuItem == "Other" ]
       then
         otherClass "SCI Controls"
         sciControl=$retVal
       elif [ $menuItem == "[None]" ]
       then
         sciControl=
       else
         sciControl=$menuItem
       fi
       ;;
  esac

}

 # Install and setup Docker/Docker Compose
setupDockerCompose () {

  echo "Installing Docker Compose..."
  if [ ! -e $workTempDir ]
  then
    mkdir -p $workTempDir/docker-ce
  fi

  tar -xvf ${workDir}/docker/*${osRelease}*gz -C $workTempDir/docker-ce > /dev/null 2>&1 &
  PID=$!
  spinner $PID
  sleep 5 &
  PID=$!
  spinner $PID
  pushd ${workTempDir}/docker-ce/ 2>&1 > /dev/null
  yum localinstall *.rpm -y --nogpgcheck &> /dev/null &
  PID=$!
  spinner $PID
  popd 2>&1 > /dev/null

  docker compose version &> /dev/null
  if [ $? = 0 ]
  then
    echo "  - Installed"
    rm -rf $workTempDir
  else
    errorMsg "Docker Compose failed to install correctly."
  fi

}

 # Spinner displayed while waiting
spinner() {

  if [ $debug = 0 ]
  then
    i=1
    sp="/-\|"
    echo -n ' '
    while [ -d /proc/$1 ]
    do
      printf "\b${sp:i++%${#sp}:1}"
    done
    printf "\b"
  fi

}

# Start Services
svcStart () {

  echo "  - ${1}"
  systemctl enable $2 > /dev/null 2>&1
  if [ $? = 0 ]
  then
    echo "    ├ Enabled"
  else
    echo "    ├ Failed to enable"
  fi
  systemctl start $2 > /dev/null 2>&1
  sleep 2
  systemctl status $2 > /dev/null 2>&1
  if [ $? = 0 ]
  then
    echo "    └ Started"
  else
    errorMsg "${1} failed to start"
  fi

}

# Write Docker Compose files #
writeCompose () {
   
  #imgPath=$(docker images | awk '{print $1}' | grep "${1}")
  #imgVersion=$(docker images | grep "$imgPath" | awk '{print $2}')
#imgSpec=$(echo ${imgPath}:$imgVersion)
  imgSpec=$(docker images --format "{{.Repository}}:{{.Tag}}" \
              | grep "^${1}:" | head -n1)

  if [ -z "$imgSpec" ]; then
      echo "Image for $1 not found"
      return
  fi

  case $1 in
        appgeo-proxy)
          cat << EOF > ${2}/docker-compose.yaml
version: '3.5'

services:
 appgeo-proxy:
    image: $imgSpec
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
        ;;
        common-services)
          #highVer=$(for version in $(echo -e "1.2.3\n$imgVersion");do echo $version;done | sort -V | tail -n1)
          highVer=$(echo "$imgVersion" | cut -d'-' -f1)
          if [ $highVer = "1.2.3" ]
          then
            #command=/tmp/container_exec.sh
            command=/tmp/container_exec_shell.sh
          else
            command=/tmp/container_exec_shell.sh
          fi
          cat << EOF > ${2}/docker-compose.yaml
version: '3.5'

services:
 common-services:
    image: $imgSpec
    privileged: true
    container_name: common-services
    hostname: common-services
    command: ["$command"]
    env_file: ./common-services.env
    ports:
      - "5154:5154"
    volumes:
      - ${3}:/opt/server_keystore
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
        ;;
        cs-sensorsim)
          cat << EOF > ${2}/docker-compose.yaml
version: '3.5'

services:
 cs-sensorsim:
    image: $imgSpec
    privileged: true
    container_name: cs-sensorsim
    hostname: cs-sensorsim
    network_mode: host
    command: ["/tmp/container_exec.sh"]
    env_file: ./cs-sensorsim.env
    volumes:
      - ${3}:/opt/server_keystore
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
        ;;
        lasso)
          cat << EOF > ${2}/docker-compose.yaml
version: '3.5'

services:
 lasso:
    image: $imgSpec
    privileged: true
    container_name: lasso
    hostname: lasso
    command: ["/tmp/container_exec.sh"]
    env_file: ./lasso.env
    volumes:
      - ${3}:/opt/server_keystore
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
        ;;
        rffs)
          #highVer=$(for version in $(echo -e "1.1.13\n$imgVersion");do echo $version;done | sort -V | tail -n1)
            imgSpec=$(docker images --format "{{.Repository}}:{{.Tag}}" \
            | grep "^${1}:" | head -n1)

          # Extract just the numeric part of the tag (e.g. 1.1.14)
          highVer=$(echo "$imgVersion" | cut -d'-' -f1)
          if [ $highVer = "1.1.13" ]
          then
            command=/tmp/container_exec.sh
          else
            command=/tmp/container_exec_shell.sh
          fi
          cat << EOF > ${2}/docker-compose.yaml
version: '3.5'

services:
 rffs:
    image: $imgSpec
    privileged: true
    container_name: rffs
    hostname: rffs
    command: ["$command"]
    env_file: ./rffs.env
    volumes:
      - ${3}:/opt/server_keystore
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
        ;;
        silkwave)
          cat << EOF > ${2}/docker-compose.yaml
version: '3.2.4'

services:
  silkwave:
    image: $imgSpec
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
      - ${3}:/opt/server_keystore
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
        ;;
  esac
}

# Write environment files #
writeEnv () {

  case $1 in
        appgeo-proxy)
        cat << EOF > ${2}/${1}.env
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
          cat << EOF > ${2}/${1}.env
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
          cat << EOF > ${2}/${1}.env
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
        cat << EOF > ${2}/${1}.env
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
        sed -i "s/,/ /g" ${2}/${1}.env
        ;;
      lasso)
        cat << EOF > ${2}/${1}.env
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
        cat << EOF > ${2}/${1}.env
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
        cat << EOF > ${2}/${1}.env
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

# Input validation for yes/no questions #
yesNo () {

  accept=0
  while [ $accept = 0 ]
  do
    cls
    read -p "${1}? [y/N] => " yN
    cls
    case $yN in
      y|Y|yes|YES|Yes)
        accept=1
        return 1
        ;;
      n|N|no|NO|No)
        accept=1
        return 0
        ;;
      *)
        echo "ERROR:"
        echo "      You entered ${yN}.  Valid responses are y,n,Y,N,Yes,No,YES,NO,Yes, or No."
        echo ""
        echo ""
        echo ""
        echo ""
        read -p "Press [Enter] to try again"
        ;;
    esac
  done
}

#╔════════╗#
#║┌──────┐║#
#║│ Main │║#
#║└──────┘║#
#╚════════╝#

cls

# --Verify root is logged in-- # 
if [ $(whoami) != "root" ]
then
  errorMsg "User must be root to execute $(basename ${0})."
fi

# --Check for Debug-- #
if [ ! -z $1 ]
then
  if [ ${1} == 'debug' ]
  then
    echo "┌────────────┐"
    echo "│ Debug Mode │"
    echo "└────────────┘"
    set -x
    debug=1
  fi
fi

# --Get SW domain if not specified on command line-- #
if [ -z $1 ] || [ $1 = 'debug' ]
then
  collect "Enter SW Domain"
  swDomain=$retVal
else
  swDomain=$1
fi

# --Set domain's classification level--#

setClass 

# --Determine if AppGeoProxy is being installed-- #
yesNo "Deploy AppGeo Proxy"
if [ $? = 1 ]
then
  agProxy=1
  collect "Enter appgeo domain"
  agDomain=$retVal
  collect "Enter appgeo server IP"
  agIP=$retVal
  loopExit=0
  while [ $loopExit = 0 ]
  do
    if [[ $agIP =~ ^(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.)(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$ ]]
    then
      loopExit=1
    else
      errorMsg "$agIP is not a valid IPv4 IP address" "try again."
      cls
      collect "Enter appgeo server IP"
      agIP=$retVal
    fi
  done
fi

# --Determine if SSL will be enabled-- #

yesNo "Enable SSL"
if [ $? = 1 ]
then
  transport=ssl
  sslEnabled=true
  hubHost=$FQDN
  # Determine Certificate Directory
  yesNo "The current certificate directory is ${certDir}/.  Use this directory"
  if [ $? = 0 ]
  then
    exit=0
    while [ $exit = 0 ]
    do
      clear
      read -p "Enter new directory path => " certDir
      clear
      if [ ! -e $certDir ]
      then
        errorMsg "$certDir does not exist." "enter a different certificate directory."
      elif [ -z "$( ls -A $certDir )" ]
      then
        errorMsg "$certDir is empty." "enter a different certificate directory."
      else
        exit=1
      fi
    done
  else
    if [ ! -e $certDir ]
    then
      errorMsg "$certDir does not exist." "enter a different certificate directory."
      exit=0
      while [ $exit = 0 ]
      do
        clear
        read -p "Enter new directory path => " certDir
        clear
        if [ ! -e $certDir ]
        then
          errorMsg "$certDir does not exist." "enter a different certificate directory."
        elif [ -z "$( ls -A $certDir )" ]
        then
          errorMsg "$certDir is empty." "enter a different certificate directory."
        else
          exit=1
        fi
      done
    fi
    if [ -z $( ls -A $certDir ) ]
    then
      errorMsg "$certDir is empty." "enter a different certificate directory."
      exit=0
      while [ $exit = 0 ]
      do
        clear
        read -p "Enter new directory path => " certDir
        clear
        if [ ! -e $certDir ]
        then
          errorMsg "$certDir does not exist." "enter a different certificate directory."
        elif [ -z "$( ls -A $certDir )" ]
        then
          errorMsg "$certDir is empty." "enter a different certificate directory."
        else
          exit=1
        fi
      done
    fi
  fi
  fixDirPerms $certDir
fi
 
cls

 # Select server certificate and validate type PEM
while [ $svrCertValid = 0 ] && [ $transport = "ssl" ]
do
  selectFile Select_Certificate_Authority_File_[PEM_format]
  serverCert=$certDir/$menuItem
  serverCertFile=$menuItem
  openssl x509 -inform PEM -in $serverCert -noout #2&> /dev/null
  if [ $? = 0 ]
  then
    svrCertValid=1
    cls
  else
    errorMsg "$serverCert is not a PEM formatted certificate."
    cls
  fi
done

 # Select keystore and validate with password and store type
while [ $ksValid = 0 ] && [ $transport = "ssl" ]
do
  selectFile Select_Keystore_File $serverCert
  keyStore=$certDir/$menuItem
  keyStoreFile=$menuItem
  getPasswd keystore
  ksPw=$PW
  echo "${ksPw}" | keytool -list -keystore $keyStore ${ktoolOpt} &> /dev/null
  if [ $? = 0 ]
  then
    ksValid=1
    cls
  else
    errorMsg "Either password or keystore file is invalid." "retry."
    cls
  fi
done

if [ $transport = "ssl" ]
then
  chkStoreType $keyStore $ksPw PKCS12
fi

 # Select truststore and validate with password and store type
clear
while [ $tsValid = 0 ] && [ $transport = "ssl" ] 
do
  selectFile Select_Truststore_File $serverCert $keyStore
  trustStore=$certDir/$menuItem
  trustStoreFile=$menuItem
  getPasswd truststore
  tsPw=$PW
  echo "${tsPw}" | keytool -list -keystore $trustStore ${ktoolOpt} &> /dev/null
  if [ $? = 0 ]
  then
    tsValid=1
    clear
  else
    errorMsg "Either password or truststore file is invalid." "retry."
    clear
  fi
done

if [ $transport = "ssl" ]
then
  chkStoreType $trustStore $tsPw JKS
  tmpFile=temp_out.txt
  echo "${ksPw}" | keytool -v -list -keystore ${keyStore} ${ktoolOpt} &> $tmpFile
  cls
  certAlias=$(grep "Alias name:" $tmpFile | awk '{print $3}')
  rm -f $tmpFile
fi


 # Ensure firewalls are disabled
for fwService in iptables firewalld nftables
do
  systemctl is-active --quiet $fwService
  if [ $? = 0 ]
  then
    systemctl stop --quiet $fwService
    systemctl disable --quiet $fwService
  fi
done

 # Ensure docker is installed and docker-compose is available
checkRPM docker-ce-cli
if [ $? = 0 ]
then
   docker compose version &> /dev/null
   if [ $? = 0 ]
   then
     echo "Installing Docker Compose..."
     sleep .6
     echo "  - Already Installed"
   else
     setupDockerCompose
   fi
else
  setupDockerCompose
fi

# --Install java-17-- #
loadPkg java-17

# --Start services-- #
echo "Activating Container Service..."
systemctl daemon-reload
#svcStart Silkwave silkwave
svcStart Docker docker

# --Load images-- #
echo "Loading Images..."
if [ $agProxy = 1 ]
then
  imageList="silkwave appgeo-proxy common-services cs-sensorsim lasso rffs"
else
  imageList="silkwave common-services cs-sensorsim lasso rffs"
fi

for image in $imageList
do
  loadImg $image
done

# --Load maps-- #
echo "Extracting Map Data..."
umask 0022
mkdir -p /var/staging/h/MapData/data
tar --warning=no-timestamp -xf ${workDir}/repository/CSHUB/maps/*gz -C /var/staging/h/MapData/data &> /dev/null &
PID=$!
spinner $PID
chmod -R u=rX,g=rX,o=rX /var/staging/h &
PID=$!
spinner $PID
chown -R root.root /var/staging/h &
PID=$!
spinner $PID

# --Start apps-- #
echo "Starting Application Containers..."
tmpList=""
# sort list of apps so that common-services is first #
for app in $(ls $workAppDir)
do
  if [ $app = 'common-services' ]
  then
    appList="common-services"
  else
    tmpList=$tmpList" "$app
  fi
done
appList=$appList" "$tmpList

for app in $appList

do
  echo $app
  sleep 10
  # Skip appgeo-proxy if not deploying it
  if [ $app = 'appgeo-proxy' ] && [ $agProxy = 0 ]; then
    continue
  fi
  mkdir -p \
  "${DATA_DIR}" \
  "${LOG_DIR}" \
  "${KEYSTORE_DIR}" \
  "${MAP_DATA_DIR}"

  writeCompose $app ${workAppDir}/${app} $certDir
  writeEnv $app ${workAppDir}/${app}
  
  pushd ${workAppDir}/${app} &> /dev/null
  echo "  - $app"

  # Attempt to start container, continue on failure
  #docker compose up -d &> /dev/null
  docker compose up -d 
  if [ $? = 0 ]; then
    echo "    └ Started"
  else
    echo "    └ Failed to start, skipping..."
    # Optionally log the failed app
    echo "$app failed to start at $(date)" >> ${workDir}/failed_containers.log
  fi

  popd &> /dev/null
done

echo "╔═══════════╗"
echo "║ Complete! ║"
echo "╚═══════════╝"
