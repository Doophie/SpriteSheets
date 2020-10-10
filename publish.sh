#!/bin/bash
./gradlew clean build install 
mkdir ./spritesheet/build/publications
mkdir ./spritesheet/build/publications/release
cp ./spritesheet/build/poms/pom-default.xml ./spritesheet/build/publications/release/pom-default.xml
./gradlew bintrayUpload -Ppublish=true
