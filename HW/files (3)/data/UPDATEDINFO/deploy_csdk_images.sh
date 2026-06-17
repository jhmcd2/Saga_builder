#!/usr/bin/env bash

# Variables populated by maven build
declare -a SOURCE_IMAGES_ARR=(
  'cs/csbuild:rh9min-jdk17-mvn3.9.6-cscli0.30.1'
  'cs/cs-cli:0.13.2'
  'cs/csta:1.5.1'
  'cs/aoco-cs:1.2.13'
  'silkwave-ir:3.2.5'
  'cs/cssim:3.2.12'
  'cs/cssim-ui-server:1.0.12'
  'cs/ubi9/java17/csbase:2.7'
  'cs/xray:1.0.10'
  'jet:0.11.0'
  'jetwave:0.3.0'
)

function usage {
  cat << EOF

usage: deploy_csdk_images.sh -l [-r <target docker registry> [-p]]

   This script loads the docker images contained in csdk-docker-app-2026-03.tgz
   and csdk-docker-dev-2026-03.tgz into the docker registry on the host.
   
  -l 
     Load all images from archives: csdk-docker-app-2026-03.tgz, csdk-docker-dev-2026-03.tgz.
     The archives are expected to be in the folder where the script is executed.
     Docker images will be stored in the host docker registry where the script
     is executed.   The -r and -p options will deploy to an
     organization's shared repository.

   -r <target docker registry>
     The package of docker images are initially tagged with a placeholder
     registry: company.docker.registry.   This option will 're-tag' 
     those images with the provided registry and delete the placeholder.
     note: A docker image standard format is: <registry>/<image name>:<tag>
       <registry> is used by docker to know where a given image is hosted.

   -p
     This option will only work with the -r option because to push
     the images to a organization's docker registry, the image must first 
     be re-tagged using the organization's registry url.
     note: this option also requires the user running this script is logged
       into the organization's docker registry and has 'push' privileges.

EOF
}

if [[ -z $1 ]]; then
  usage
  exit 0
fi

LOAD="false"
PUSH="false"
TARGET_REG=""

while getopts "lpr:" arg; do
  case $arg in 
    l) LOAD="true"
       ;;
    p) PUSH="true"
       ;;
    r) TARGET_REG=${OPTARG}
       ;;
    *) usage
       exit 0
       ;;
  esac
done

if [[ $LOAD == "true" ]]; then
  echo "Loading docker images from archives into local docker registry.."
  for ARCHIVE_FILE in 'csdk-docker-app-2026-03.tgz' 'csdk-docker-dev-2026-03.tgz'; do
    if [[ -f $ARCHIVE_FILE ]]; then
      docker load --input "$ARCHIVE_FILE"
    else
      echo >&2 "Cannot find expected archive file $ARCHIVE_FILE"
    fi
  done
fi

if [[ -n $TARGET_REG ]]; then
  # As a special case, we also need to retag the CSTA latest tag
  CSTA_IMAGE='cs/csta:1.5.1'
  SOURCE_IMAGES_ARR+=("${CSTA_IMAGE%:*}:latest")

  # ------------ Iterate over list, re-tagging and pushing  ---------------
  MISSING_IMAGES=0
  for SOURCE_IMAGE in "${SOURCE_IMAGES_ARR[@]}"
  do
    ORIG_IMAGE="company.docker.registry/$SOURCE_IMAGE"
    if ! docker image inspect "$ORIG_IMAGE" &> /dev/null; then
      echo >&2 "Could not find expected image $ORIG_IMAGE"
      (( MISSING_IMAGES++ ))
      continue
    fi
    NEW_IMAGE="$TARGET_REG/$SOURCE_IMAGE"

    # retag with appropriate company name
    docker tag "$ORIG_IMAGE" "$NEW_IMAGE"

    # push
    if [[ $PUSH == "true" ]]; then
      # push
      echo "Pushing: $NEW_IMAGE"
      docker push "$NEW_IMAGE"
    fi

    # Remove generic tag
    docker rmi "$ORIG_IMAGE"
  done
  if (( MISSING_IMAGES > 0 )); then
    echo >&2 "$MISSING_IMAGES missing images, run with -l to load from archives"
  fi
fi
