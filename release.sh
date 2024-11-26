#!/bin/bash
set -e
# https://github.com/gradle-nexus/publish-plugin#publishing-and-closing-in-different-gradle-invocations
./gradlew publishToSonatype --no-configuration-cache
./gradlew findSonatypeStagingRepository closeSonatypeStagingRepository --no-configuration-cache
./gradlew findSonatypeStagingRepository releaseSonatypeStagingRepository --no-configuration-cache
