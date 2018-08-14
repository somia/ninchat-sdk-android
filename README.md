# Mobile Ninchat SDK for Android

## Prerequisites

You need to have Go installed to build the library. Furthermore, you need to install gomobile and websocket too.

## Updating client libraries

Go client libraries can be updated with the following shell scriptin the project root:

    $ ./update-go-framework.sh

## Building SDK .aar

SDK resides in ninchatsdk module. All the aar artifacts can be built with the gradle task assemble as follows:

    $ ./gradlew assemble

After building artifacts can be then found in (project root)/ninchatsdk/build/outputs/aar folder.

## Test application

The test application can be found from the ninchat-sdk-android-testclient project on Github.

## Usage

A simple test Activity can be created by extending ChatTestActivity:

    import com.ninchat.sdk.ChatTestActivity;
    public class MainActivity extends ChatTestActivity {}

Go client connection can be tested with a static method in Connection class:

    import com.ninchat.sdk.Connection;
    public class Test {
        private void testConnection() {
            Connection.test();
        }
    }

## Contact
If you have any questions, contact:

Jussi Pekonen / Qvik jussi.pekonen@qvik.fi