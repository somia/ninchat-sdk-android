# Mobile Ninchat SDK for Android

## Updating client libraries

Go client libraries can be updated with the following shell scriptin the project root:

    $ ./update-go-framework.sh

## Building SDK .aar

SDK resides in ninchatsdk module. All the aar artifacts can be built with the gradle task :ninchatsdk:assemble
                                                                                            as follows:

    $ ./gradlew :ninchatsdk:assemble

After building artifacts can be then found in (project root)/ninchatsdk/build/outputs/aar folder.

## Test application

Project itself contains a test application. You can run it from Android Studio or install it with gradle:

    $ ./gradlew installDebug

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

Timo Hintsa / Qvik timo@qvik.fi