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

The SDK is started by calling the static `start` method of the `NinchatSession` class with the activity as the argument:

    import com.ninchat.sdk.NinchatSession;
    ...
    NinchatSession.start(activity);

At the moment the SDK opens up an activity that tests the connection to the Ninchat server.

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
