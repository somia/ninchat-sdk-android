#!/bin/bash
#
# Manually updates the Go client .aar package. You can automate this process
# via Android Studio Gradle plugin:
#
# https://github.com/golang/go/wiki/Mobile#building-and-deploying-to-android-1

go=$(which go)
if [ "$?" != "0" ]; then
    echo "Go not installed. Aborting."
    exit 1
fi
if [ -z "${GOPATH}" ]; then
    echo "GOPATH not set. Aborting."
    exit 1
fi
if [ -z "${ANDROID_HOME}" ]; then
    echo "ANDROID_HOME not set. Aborting."
    exit 1
fi

hasgopathinpath=$(echo "${PATH}" | grep "${GOPATH}")
if [ "$?" != "0" ]; then
    export PATH="${PATH}:${GOPATH}/bin"
fi

echo "Rebuilding Go SDK framework.."

libdir="`pwd`/ninchatsdk/libs"
mygopath="$GOPATH:`pwd`/go-sdk"
gocodedir="go-sdk/src/github.com/ninchat/ninchat-go/mobile"

cd $gocodedir
if [ $? -ne 0 ]; then
    echo "Failed to find go code dir. Did you run this from the Android project dir?"
    exit 1
fi

echo "Running gomobile tool.."
GOPATH=$mygopath gomobile bind -target android -javapkg com.ninchat -o $libdir/ninchat-client.aar
if [ $? -ne 0 ]; then
    echo "gomobile cmd failed, aborting."
    exit 1
fi

echo "Done."
