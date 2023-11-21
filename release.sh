#!/bin/bash
set -e
# https://github.com/gradle-nexus/publish-plugin#publishing-and-closing-in-different-gradle-invocations
./gradlew publishToSonatype
./gradlew findSonatypeStagingRepository closeSonatypeStagingRepository
./gradlew findSonatypeStagingRepository releaseSonatypeStagingRepository
