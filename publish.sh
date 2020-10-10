#!/bin/bash
./gradlew clean build install 
mkdir ./app/build/publications
mkdir ./app/build/publications/release
cp ./app/build/poms/pom-default.xml ./app/build/publications/release/pom-default.xml
./gradlew bintrayUpload -Ppublish=true
