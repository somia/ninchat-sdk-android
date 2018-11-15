#!/bin/bash
#
# Manually updates the Go client .aar package. You can automate this process
# via Android Studio Gradle plugin:
#
# https://github.com/golang/go/wiki/Mobile#building-and-deploying-to-android-1

set -e

readonly CURRENTDIR=$(pwd)
readonly GOCODEDIR="${CURRENTDIR}/go-sdk/src/github.com/ninchat/ninchat-go/mobile"
readonly PACKAGE="com.ninchat"
readonly NAME="client"
readonly LIBDIR="${CURRENTDIR}/$(echo ${PACKAGE} | sed 's/\./\//g')/${NAME}"


function checkIsGoInstalled() {
    local go=$(which go)
    if [ $? -ne "0" ]; then
        echo "Go not installed. Aborting."
        exit 1
    fi
}

function checkIsGOPATHset() {
    if [ -z "${GOPATH}" ]; then
        echo "GOPATH not set. Aborting."
        exit 1
    fi
}

function checkIsANDROIDHOMEset() {
    if [ -z "${ANDROID_HOME}" ]; then
        echo "ANDROID_HOME not set. Aborting."
        exit 1
    fi
}

function addGOPATHToPATHIfNeeded() {
    local hasgopathinpath=$(echo "${PATH}" | grep "${GOPATH}")
    if [ -z "${hasgopathinpath}" ]; then
        export PATH="${PATH}:${GOPATH}/bin"
    fi
}

function checkBeingCalledFromTheRightDirectory() {
    if [ ! -e "${GOCODEDIR}" ]; then
        echo "Failed to find go code dir. Did you run this from the Android project dir?"
        exit 1
    fi
}

function getGoRepoDescription() {
    cd "${GOCODEDIR}"
    local gorepodescription=$(git describe --tags)
    local gorepoversion=$(echo "${gorepodescription}" | sed -e 's/^v//' -e 's/-.*//')
    local gorepobuildversion=$(echo "${gorepodescription}" | sed -e 's/[^-]*-//' -e 's/-.*//')
    if [ ! -z "${gorepobuildversion}" ]; then
        gorepoversion="${gorepoversion}-${gorepobuildversion}"
    fi
    cd "${CURRENTDIR}"
    echo "${gorepoversion}"
}

function generateMd5CheckSum() {
    local filename="$1"
    if which md5 > /dev/null; then
        md5 -q "${filename}" > "${filename}.md5"
    else
        md5sum "${filename}" | sed 's/ .*//' > "${filename}.md5"
    fi
}

function generateSha1CheckSum() {
    local filename="$1"
    shasum -a 1 "${filename}" | sed 's/ .*//' > "${filename}.sha1"
}

function buildGoLibrary() {
    local version=$(getGoRepoDescription)
    local outdir="${LIBDIR}/${version}"
    local filename="${outdir}/${NAME}-${version}.aar"

    if [ ! -e "${outdir}" ]; then
        mkdir -p "${outdir}"
    fi

    cd "${GOCODEDIR}"
    echo "Running gomobile tool.."
    GOPATH="${CURRENTDIR}/go-sdk:${GOPATH}" gomobile bind -target android -javapkg "${PACKAGE}" -o "${filename}"
    if [ $? -ne 0 ]; then
        echo "gomobile cmd failed, aborting."
        exit 1
    fi
    cd "${CURRENTDIR}"

    for file in $(find "${outdir}" -type f -name '*.aar' -o -name '*.jar'); do
        generateMd5CheckSum "${file}"
        generateSha1CheckSum "${file}"
    done

    echo "Done."
}

function generateMavenMetadata() {
    local releaseversion=$(getGoRepoDescription)
    local filename="maven-metadata.xml"
    local time=$(date +%Y%m%d%H%M%S)

    cat > "${LIBDIR}/${filename}" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>${PACKAGE}</groupId>
  <artifactId>${NAME}</artifactId>
  <versioning>
    <release>${releaseversion}</release>
    <versions>
EOF

    for dir in $(find "${LIBDIR}" -mindepth 1 -type d); do
        local base=$(basename "${dir}")
        echo "      <version>${base}</version>" >> "${LIBDIR}/${filename}"
    done

    echo "    </versions>" >> "${LIBDIR}/${filename}"

    echo "  <lastUpdated>${time}</lastUpdated>" >> "${LIBDIR}/${filename}"

    cat >> "${LIBDIR}/${filename}" << EOF
  </versioning>
</metadata>
EOF

    generateMd5CheckSum "${LIBDIR}/${filename}"
    generateSha1CheckSum "${LIBDIR}/${filename}"
}

function generatePOMFile() {
    local version="$(getGoRepoDescription)"
    local outdir="${LIBDIR}/${version}"
    local filename="${outdir}/${NAME}-${version}.pom"

    cat > "${filename}" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${PACKAGE}</groupId>
  <artifactId>${NAME}</artifactId>
  <version>${version}</version>
  <packaging>aar</packaging>
  <dependencies>
  </dependencies>
</project>
EOF

    generateMd5CheckSum "${filename}"
    generateSha1CheckSum "${filename}"
}

function main() {
    # Pre-checks
    checkIsGoInstalled
    checkIsGOPATHset
    checkIsANDROIDHOMEset
    addGOPATHToPATHIfNeeded
    checkBeingCalledFromTheRightDirectory

    # Build
    echo "Rebuilding Go SDK framework.."
    buildGoLibrary

    # Post-processing
    generateMavenMetadata
    generatePOMFile
}

main


