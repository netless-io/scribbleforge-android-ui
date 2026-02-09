#!/bin/bash

declare -a libs=("forge-ui")

usage() {
    echo "Usage: $0 <lib-name|all> <local|remote>"
    echo "lib-name: ${libs[*]}"
    exit 1
}

if [ $# -ne 2 ]; then
    usage
fi

lib_name=$1
target=$2

if [[ ! " ${libs[*]} " =~ " ${lib_name} " ]] && [ "${lib_name}" != "all" ]; then
    echo "Invalid library name: ${lib_name}"
    usage
fi

if [ "${target}" != "local" ] && [ "${target}" != "remote" ]; then
    echo "Invalid publish target: ${target}"
    usage
fi

publish_lib() {
    local lib=$1
    local target=$2
    local gradle_task=""

    if [ "${target}" == "remote" ]; then
        gradle_task="publishToMavenCentral"
    elif [ "${target}" == "local" ]; then
        gradle_task="publishMavenPublicationToLocalRepository"
    fi

    ./gradlew :${lib}:clean
    ./gradlew :${lib}:${gradle_task} --no-configuration-cache

    if [ $? -eq 0 ]; then
        echo "Publish ${lib} to ${target} succeeded."
    else
        echo "Publish ${lib} to ${target} failed."
        exit 1
    fi
}

publish_all() {
    local target=$1
    local gradle_task=""

    if [ "${target}" == "remote" ]; then
        gradle_task="publishAllPublicationsToMavenCentralRepository"
    elif [ "${target}" == "local" ]; then
        gradle_task="publishAllPublicationsToLocalRepository"
    fi

    ./gradlew clean
    ./gradlew ${gradle_task} --no-configuration-cache

    if [ $? -eq 0 ]; then
        echo "Publish all libraries to ${target} succeeded."
    else
        echo "Publish all libraries to ${target} failed."
        exit 1
    fi
}

if [ "${lib_name}" == "all" ]; then
    publish_all $target
else
    publish_lib $lib_name $target
fi
