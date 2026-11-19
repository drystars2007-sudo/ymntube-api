#!/bin/sh
set -eu

MAVEN_VERSION=3.9.9
MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_EXE="${MAVEN_HOME}/apache-maven-${MAVEN_VERSION}/bin/mvn"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip"

if [ ! -x "${MAVEN_EXE}" ]; then
    mkdir -p "${MAVEN_HOME}"
    archive="${MAVEN_HOME}/apache-maven-${MAVEN_VERSION}-bin.zip"
    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --silent --show-error "${MAVEN_URL}" --output "${archive}"
    elif command -v wget >/dev/null 2>&1; then
        wget --quiet "${MAVEN_URL}" --output-document="${archive}"
    else
        echo "Maven bootstrap requires curl or wget." >&2
        exit 1
    fi
    unzip -q -o "${archive}" -d "${MAVEN_HOME}"
    rm -f "${archive}"
fi

exec "${MAVEN_EXE}" "$@"
