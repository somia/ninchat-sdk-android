# Mobile Ninchat SDK for Android

This document describes integrating the Ninchat Android SDK into an Android application.

## Installation

Add the following maven repository to the project dependency repositories:

    maven {
        url "https://raw.github.com/somia/ninchat-sdk-android/master"
    }

Then you need to add the following dependency to the project dependencies:

    implementation 'com.ninchat:sdk:0.1.9'

## Usage

### Creating the API client

In order to use the SDK you need to create an instance of the `NinchatSession` class. Its constructor takes the application context and configuration key as parameters:

    import com.ninchat.sdk.NinchatSession;
    ...
    NinchatSession session = new NinchatSession(applicationContext, configurationKey);

### Starting the API client

The SDK is then started by calling the `start` method of the class. The method takes the calling activity as a parameter:

    session.start(activity);

The `start` method opens the SDK UI automatically.

The SDK does some things asynchronously before the SDK UI opens. Therefore it is recommended that the host application displays a spinner or some other visual cue after the SDK has been started. The spinner/whatever can be dismissed when
1. The SDK returns and the `onActivityResult` method of the calling Activity is called with the default or the given `requestCode` (see the [Optional parameters](#optionalparameters) section) or
2. The host application subscribes to LocalBroadcastManager's broadcasts with the Intent action `NinchatSession.Broadcast.QUEUES_UPDATED` that is sent when the SDK opens its UI. Alternatively, should there be any issues with the SDK launch that prevent the SDK from starting, the host application can catch that by subscribing to LocalBroadcastManager's broadcasts with the Intent action `NinchatSession.Broadcast.START_FAILED`.

### Setting metadata

The `NinchatSession` class has a setter for audience metadata (i.e. user information).  It's specified as a `com.ninchat.client.Props` object:

    Props metadata = new Props();
    metadata.setString("Significant Information", someValue);
    metadata.setString("secure", secureMetadata);
    session.setAudienceMetadata(metadata);

### <a name="optionalparameters"></a>Optional parameters

The start method can take the request code (`int`) as optional parameters:

    session.start(activity, requestCode);

By default, the request code parameter is `NinchatSession.NINCHAT_SESSION_REQUEST_CODE`. The calling activity can observe the result of the SDK opening by checking the result code in its `onActivityResult` method for the selected request code. If the SDK has joined a queue, the result code will be `Activity.RESULT_OK` while in the other cases it will be `Activity.RESULT_CANCELLED`.

Alternatively, if the client knows the queue ID it wants to join, the SDK can be started by calling the `start` method with the queue ID (`String`):

    session.start(activity, queueId);
    session.start(activity, requestCode, queueId);

These methods open the SDK directly to the queueing view for that given queue.

### Low-level API access

The SDK exposes the low-level communication interface with the method `getSession()` in the `NinchatSession` class. The host app may use this object to communicate to the server, bypassing the SDK logic.

Furthermore, the host application can register itself (or its property) as a listener to the low-level API events and/or logs by creating the `NinchatSession` instance with the listeners as constructor arguments:

    session = new NinchatSession(applicationContext, configurationKey, eventListener, logListener);

The argument `eventListener`, when non-null, must be an instance of the `NinchatSDKEventListener` interface and the `logListener` an instance of the `NinchatSDKLogListener`interface.

See [Ninchat API Reference](https://github.com/ninchat/ninchat-api/blob/v2/api.md) for information about the API's outbound Actions and inbound Events.

## Overriding SDK assets

### Images and animations

For now, the application can override certain images/animations of the SDK. The image/animation override is done by having (one, many, or even all of) them as drawable resources in the application bundle with following names:

| Asset name       | Related UI control(s)           | Notes  |
|:------------- |:-------------|:-----|
| ninchat_icon_loader   | Progress indicator icon in queue view. |  |
| ninchat_icon_chat_writing_indicator      | User is typing.. Indicator icon in chat bubble | Should be a [frame animation](https://developer.android.com/guide/topics/resources/animation-resource#Frame). |
| ninchat_chat_background    | Chat view's background image. Note: The drawable **must** be a bitmap image. |   |
| ninchat_chat_primary_button    | Background for the primary buttons. |   |
| ninchat_chat_secondary_button    | Background for the secondary buttons. |   |
| ninchat_chat_close_button              | Background for 'close chat' button. |  |
| ninchat_icon_chat_close_button              | Close icon in the 'close chat' button |  |
| ninchat_icon_download              | Download icon for images and videos. |  |
| ninchat_chat_bubble_left              | Background for left side chat bubble (first message) | Must be a scalable image, like a [9-patch image](https://developer.android.com/studio/write/draw9patch), as it needs to stretch. |
| ninchat_chat_bubble_left_repeated              | Background for left side chat bubble (serial message) | Must be a scalable image, like a [9-patch image](https://developer.android.com/studio/write/draw9patch), as it needs to stretch. |
| ninchat_chat_bubble_right              | Background for right side chat bubble (first message) | Must be a scalable image, like a [9-patch image](https://developer.android.com/studio/write/draw9patch), as it needs to stretch. |
| .ninchat_chat_bubble_right_repeated              | Background for right side chat bubble (serial message) | Must be a scalable image, like a [9-patch image](https://developer.android.com/studio/write/draw9patch), as it needs to stretch. |
| ninchat_chat_avatar_right   | Placeholder avatar icon for my messages. |  |
| ninchat_chat_avatar_left   | Placeholder avatar icon for others' messages. |  |
| ninchat_icon_chat_play_video   | Play icon for videos |  |
| ninchat_icon_textarea_camera   | Icon for initiating a video call |  |
| ninchat_icon_textarea_attachment   | Icon for selecting an attachment to be sent |  |
| ninchat_textarea_submit_button   | Background for the 'send message' button |  |
| ninchat_icon_textarea_submit_button   | Icon for the 'send message' button |  |
| ninchat_icon_video_toggle_full   | Icon for the making the video call enter the fullscreen view |  |
| ninchat_icon_video_toggle_normal   | Icon for the making the video call exit the fullscreen view |  |
| ninchat_icon_video_sound_on   | Icon for the unmuting the audio (remote audio) in video call |  |
| ninchat_icon_video_sound_off   | Icon for the muting the audio (remote audio) in video call |  |
| ninchat_icon_video_microphone_on   | Icon for the unmuting the microphone (own audio) in video call |  |
| ninchat_icon_video_microphone_off   | Icon for the muting the microphone (own audio) in video call |  |
| ninchat_icon_video_camera_on   | Icon for the sending own video in video call |  |
| ninchat_icon_video_camera_off   | Icon for the stop sending own video in video call |  |
| ninchat_icon_video_hangup   | Icon for the hanging up the video call |  |
| ninchat_icon_rating_positive   | Ratings view positive icon |  |
| ninchat_icon_rating_neutral   | Ratings view neutral icon |  |
| ninchat_icon_rating_negative   | Ratings view negative icon |  |

### Colors

In addition, the application can override colors used in the SDK. The colors need to be defined in the application's values using (one, many, or all of) the following names:

| Asset name       | Related UI control(s)
|:------------- |:-------------|
| ninchat_color_button_primary_text | Text on 'primary' buttons
| ninchat_color_button_secondary_text | Text on 'secondary' buttons
| ninchat_color_info_text | Chat view's meta information (eg. 'Chat started')
| ninchat_color_chat_name | User name above chat bubbles
| ninchat_color_chat_timestamp | Timestamp above chat bubbles
| ninchat_color_chat_bubble_left_text | Text in others' chat messages
| ninchat_color_chat_bubble_right_text | Text in my chat messages
| ninchat_color_textarea_text | Chat input text
| ninchat_color_textarea_submit_text | Message submit button title
| ninchat_color_chat_bubble_left_link | Link color in others' messages
| ninchat_color_chat_bubble_right_link | Link color in my messages
| ninchat_color_modal_background | Background in 'modal dialogs'
| ninchat_color_modal_title_text | Text in 'modal dialogs'
| ninchat_color_background_top | Background of the top part in some views
| ninchat_color_text_top | Text in top parts of some views
| ninchat_color_link | Link color (except in chat bubbles)
| ninchat_color_background_bottom | Background of the bottom part in some views
| ninchat_color_text_bottom | Text in bottom parts of some views
| ninchat_color_rating_positive_text | Text of the positive rating button
| ninchat_color_rating_neutral_text | Text of the neutral rating button
| ninchat_color_rating_negative_text | Text of the negative rating button

## Building the Go library

### Prerequisites

You need to have Go installed to build the library. Furthermore, you need to install gomobile and websocket too.

### Updating client libraries

Go client libraries can be updated with the following shell scripti in the project root:

    $ ./update-go-framework.sh

## Building SDK .aar

SDK resides in ninchatsdk module. All the aar artifacts can be built with the gradle task assemble as follows:

    $ ./gradlew assemble

After building artifacts can be then found in (project root)/ninchatsdk/build/outputs/aar folder.

## Test application

The test application can be found from the ninchat-sdk-android-testclient project on Github.

## Contact

If you have any questions, contact:

Jussi Pekonen / Qvik jussi.pekonen@qvik.fi
