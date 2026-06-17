#!/usr/bin/env bash

declare -a UNPACK_OPTIONS=(
	"cs-cli"
	"java-example-code"
	"orca"
	"mfcodegen"
	"mfsign"
	"pycs-dist"
	"local-maven-repository"
)

N_OPTIONS=${#UNPACK_OPTIONS[@]}

CS_CLI_ARTIFACT="cs.cs-cli:cs-cli:0.30.1:bz2:{linux-amd64,darwin-arm64}"
JEC_ARTIFACT="cs.example:cs-example:3.2.0:zip"
ORCA_ARTIFACT="cs.orca:caci-us-cmty:20260312T185928-744baa73:bundle"
MFCODEGEN_ARTIFACT="cs.dsl:mfcodegen-dist:2.4.2:zip"
MFSIGN_ARTIFACT="cs.dsl:mf-sign:1.1:zip:standalone"
PYCS_ARTIFACT="cs:pycs-dist:2026-03:zip"
LOCAL_MAVEN_REPO_ARTIFACT="N/A"
CS_BUILD_IMAGE="company.docker.registry/cs/csbuild:rh9min-jdk17-mvn3.9.6-cscli0.30.1"

declare -a ARTIFACTS=(
	"$CS_CLI_ARTIFACT"
	"$JEC_ARTIFACT"
	"$ORCA_ARTIFACT"
	"$MFCODEGEN_ARTIFACT"
	"$MFSIGN_ARTIFACT"
	"$PYCS_ARTIFACT"
	"$LOCAL_MAVEN_REPO_ARTIFACT"
)

N_ARTIFACTS=${#ARTIFACTS[@]}

CS_CLI_DESC="A native binary that facilitates use of the Common Services ecosystem."
JEC_DESC="A buildable Maven project exhibiting use of the Common Services Java API."
ORCA_DESC="A Git bundle file containing a repository of Common Services definition files."
MFCODEGEN_DESC="An application that will produce C++ or Java code from Common Services definition files."
MFSIGN_DESC="An application that will perform signing of Common Services messages."
PYCS_DESC="The distribution for the PyCS (Python Common Services) library."
LOCAL_MAVEN_REPO_DESC="Unpacks a valid directory central repository with all the artifacts needed to build the Java Example Code."

declare -a DESC=(
	"$CS_CLI_DESC"
	"$JEC_DESC"
	"$ORCA_DESC"
	"$MFCODEGEN_DESC"
	"$MFSIGN_DESC"
	"$PYCS_DESC"
	"$LOCAL_MAVEN_REPO_DESC"
)

N_DESC=${#DESC[@]}

CS_CLI_TARGET=.
JEC_TARGET=.
ORCA_TARGET=.
MFCODEGEN_TARGET=.
MFSIGN_TARGET=.
PYCS_TARGET=.
LOCAL_MAVEN_REPO_TARGET=DIRECTORY

declare -a TARGET=(
	"$CS_CLI_TARGET"
	"$JEC_TARGET"
	"$ORCA_TARGET"
	"$MFCODEGEN_TARGET"
	"$MFSIGN_TARGET"
	"$PYCS_TARGET"
	"$LOCAL_MAVEN_REPO_TARGET"
)

N_TARGET=${#TARGET[@]}

usage() {
	# The here-doc must be intented using tabs, not spaces. Make sure you don't retab them away accidentally.
	cat <<-END
	Usage: install_csdk_artifact.sh [--repo repo_location] identifier [classifier]

	--repo is an optional flag and indicates the location of the repository to fetch the artifact from. If it is
	omitted, the script will attempt to use an installation of Maven to get the local repository location and fetch the
	desired artifact.

	identifier must be one of: ${UNPACK_OPTIONS[@]}
	classifier is only required for certain artifacts. You will be informed which options are available when it is blank.
	END
	echo
	echo Artifact Descriptions:
	for i in ${!UNPACK_OPTIONS[@]}; do
		echo ${UNPACK_OPTIONS[$i]}: ${DESC[$i]}
	done
}

if (( $# < 1 )); then
	usage
	exit 1
fi
local_maven_repo=
unset ARGS
declare -a ARGS=()
while (( $# > 0 )); do
	if [[ $1 == '--repo' ]]; then
		if (( $# < 2 )); then
			echo "--repo must have an argument indicating the location of the local Maven repository"
			exit 1
		fi
		local_maven_repo=$2
		shift 2
	else
		ARGS+=("$1")
		shift
	fi
done
if (( ${#ARGS[@]} < 1 )); then
	usage
	exit 1
fi

if [[ $N_DESC != $N_ARTIFACTS || $N_ARTIFACTS != $N_OPTIONS || $N_TARGET != $N_OPTIONS ]]; then
	echo "ERROR: Number of options, artifacts, and descriptions, and targets must be the same"
	exit
fi


get_artifact_index() {
	for (( x=0 ; x < ${#UNPACK_OPTIONS[@]} ; ++x)); do
		if [[ ${UNPACK_OPTIONS[$x]} == $1 ]]; then
			break
		fi
	done
	return $x
}

maven_found=0
find_maven() {
	which mvn &>/dev/null
	if [[ $? != 0 ]]; then
		echo "No Maven found on path. Please add it, or specify the location of a Maven repository using the --repo option."
		echo "(The repository directory distributed with the CSDK is a valid target for this option.)"
		exit 1
	fi
	maven_found=1
}

get_artifact_index "${ARGS[0]}"
artifact_index=$?

if (( $artifact_index >= $N_ARTIFACTS )); then
	usage
	exit 1
fi

if [[ ${ARGS[0]} == "local-maven-repository" ]]; then
	if [[ ${#ARGS[@]} < 2 ]]; then
		echo "You must provide a directory to install the repo, e.g. ./install_csdk_artifact local-maven-repository ~/local_maven_repo"
		exit 1
	fi
	dir=${ARGS[1]}
	if [[ -e $dir ]]; then
		echo "The target must not already exist. Remove it and try again."
		exit 1
	fi
	dir_base=$(dirname "$dir")
	if [[ ! -d $dir_base ]]; then
		echo "$dir_base must be a directory"
		exit 1
	fi
	dir=$(realpath -m "$dir")
	cd "$(dirname "${BASH_SOURCE[0]}")"
	if [[ ! -f bootstrap_metadata.tar.gz  || ! -d repository  || ! -x ./create_deployment_bootstrap.sh ]]; then
		echo "Essential files are missing, run this file from the distribution directory."
		exit 1
	fi
	if ! command -v docker >& /dev/null; then
		echo "Docker must be installed to exercise this option"
		exit 1
	fi
	bootstrap_repo=$(mktemp -d)
	if [[ $? != 0 ]]; then
		echo "Failed to make required temp directory"
		exit 1
	fi
	cleanup() {
		rm -rf "$bootstrap_repo"
	}
	trap cleanup EXIT
	if [[ ${#ARGS[@]} > 2 ]]; then
		csbuild_image="${ARGS[2]}/cs/csbuild:rh9min-jdk17-mvn3.9.6-cscli0.30.1"
		if ! [[ "$(docker image ls -q "$csbuild_image")" ]]; then
			docker pull "$csbuild_image"
		fi
	else
		csbuild_image=$CS_BUILD_IMAGE
	fi
	if ! [[ "$(docker image ls -q "$csbuild_image")" ]]; then
		cat << END
Cannot find image $csbuild_image. Ensure that all distributed Docker images have
been installed at the default coordinates (company.docker.registry) or
provide a third argument that identifies the alternate installation registry,
e.g. ./install_csdk_artifact local_maven_repository ~/local_maven_repo mycompany.docker.repo.com
(The csbuild image should be at mycompany.docker.repo.com/cs/csbuild:rh9min-jdk17-mvn3.9.6-cscli0.30.1).
END
		exit 1
	fi
	docker run --rm -u "$(id -u):$(id -g)" \
		-v "$bootstrap_repo:/bootstrap"    \
		-v "$PWD:/source"                  \
		--entrypoint=                      \
		"$csbuild_image"                   \
		/source/create_deployment_bootstrap.sh /bootstrap/repository
	if [[ $? != 0 ]]; then
		echo "Error running Docker container bootstrap"
		exit 1
	fi
	cat > "$bootstrap_repo/maven_settings.xml" << END
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                    http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository>/bootstrap/localRepo</localRepository>
  <servers>
    <server>
      <id>mvnlocal</id>
    </server>
  </servers>
  <mirrors>
    <mirror>
      <id>mvnlocal</id>
      <url>file:///bootstrap/repository</url>
      <mirrorOf>external:*</mirrorOf>
    </mirror>
  </mirrors>
</settings>
END
	mkdir "$dir"
	if [[ $? != 0 ]]; then
		echo "Could not create specified directory"
		exit 1
	fi
	docker run --rm -u "$(id -u):$(id -g)" \
		-v "$bootstrap_repo:/bootstrap"    \
		-v "$PWD:/source"                  \
		-v "$dir:/target"                  \
		--entrypoint=                      \
		--workdir /source                  \
		"$csbuild_image"                   \
		env -u MAVEN_OPTS -u MAVEN_ARGS    \
		mvn -s /bootstrap/maven_settings.xml deploy -f deploy-pom.xml -Durl=file:///target
	if [[ $? != 0 ]]; then
		echo "Deployment failed"
		exit 1
	fi
	exit 0
fi


IFS=':' read groupId artifactId version artifactType classifier <<< "${ARTIFACTS[$artifact_index]}"
if [[ $classifier =~ ^\{.*\}$ ]]; then
	my_class_choice=${ARGS[1]}
	classifier=${classifier:1:-1}
	IFS=',' read -a class_options <<< "$classifier"
	class_matches=0
	for i in ${!class_options[@]}; do
		if [[ $my_class_choice == ${class_options[$i]} ]]; then
			class_matches=1
			break
		fi
	done
	if [[ $class_matches == 0 ]]; then
		echo "This artifact requires a classifier. Valid options are: ${class_options[@]}"
		echo 'Rerun this command adding the appropriate classifier.'
		exit 1
	fi
	classifier=$my_class_choice
fi

classpart=${classifier:+-${classifier}}
artifact_path=${groupId//./\/}/${artifactId}/${version}/${artifactId}-${version}${classpart}.${artifactType}

if [[ $local_maven_repo == '' ]]; then
	find_maven
	local_maven_repo=$(mvn -q help:evaluate -Dexpression=settings.localRepository -DforceStdout=true)
	if [[ $? != 0 ]]; then
		echo "Error querying Maven for local repository. Specify repository location using --repo option."
		exit 1
	fi
fi

full_artifact_path=${local_maven_repo}/${artifact_path}
if [[ ! -f ${full_artifact_path} ]]; then
	if [[ $maven_found == 0 ]]; then
		find_maven
	fi
	artifact_class=${classifier:+:${classifier}}
	artifact_specifier=${groupId}:${artifactId}:${version}:${artifactType}$artifact_class
	mvn org.apache.maven.plugins:maven-dependency-plugin:3.3.0:get -Dartifact=${artifact_specifier} -Dtransitive=false
	if [[ $? != 0 ]]; then
		echo "Couldn't find artifact and failed to fetch it from configured repository."
		echo "You can use the --repo option to point to a respository with the required artifact."
		echo "(The repository directory shipped with the CSDK will have the artifact.)"
		exit 1
	fi
	if [[ ! -f ${full_artifact_path} ]]; then
		echo "Maven fetch was successful, but the artifact is still missing. Try removing the --repo option."
		exit 1
	fi
fi
if [[ $artifactType == 'zip' ]]; then
	target_dir=${TARGET[$artifact_index]}
	if [[ "$target_dir" != "." ]]; then
		mkdir -p "$target_dir"
		if [[ $? != 0 ]]; then
			echo "Could not create target directory for artifact."
			exit 1
		fi
	fi
	(cd "$target_dir" && unzip "${full_artifact_path}")
	if [[ $? != 0 ]]; then
		echo "Error unzipping artifact"
		exit 1
	fi
elif [[ $artifactType == 'bz2' ]]; then
	output_file=${full_artifact_path##*/}
	output_file=${output_file%.bz2}
	bunzip2 < "${full_artifact_path}" > "${output_file}"
	if [[ $? != 0 ]]; then
		echo "Error unzipping artifact"
		exit 1
	fi
	# This is set because the only bz2 file as of now is an executable. If this changes, we'll need to add a setting for
	# this in the code somewhere.
	chmod +x ${output_file}
	if [[ $? != 0 ]]; then
		echo "Successfully fetched file, but was unable to set executable permissions"
		return 0
	fi
	echo "Successfully decompressed bz2 to file ${output_file}"
else
	cp "${full_artifact_path}" .
	if [[ $? != 0 ]]; then
		echo "Error copying artifact"
		exit 1
	fi
	echo "Successfully copied file to ${full_artifact_path##*/}"
fi
# vim: set ai noet ff=unix ts=4 sw=4 sts=4:
