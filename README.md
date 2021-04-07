# Mobile Ninchat SDK for Android

This document describes integrating the Ninchat Android SDK into an Android application.

## Installation

Add the following maven repository to the project dependency repositories:

    maven {
        url "https://raw.github.com/somia/ninchat-sdk-android/master"
    }

Then you need to add the following dependency to the project dependencies:

    implementation 'com.ninchat:sdk:0.8.8'

## Usage

### Creating the API client

In order to use the SDK you need to create an instance of the `NinchatSession` class with `NinchatSession.Builder`. Its constructor takes the application context and configuration key as parameters:

    import com.ninchat.sdk.NinchatSession;
    ...
    NinchatSession.Builder builder = new NinchatSession.Builder(applicationContext, configurationKey);
    NinchatSession session = builder.create();

### Starting the API client

The SDK is then started by calling the `start` method of the class. The method takes the calling activity as a parameter:

    session.start(activity);

The `start` method opens the SDK UI automatically.

The SDK does some things asynchronously before the SDK UI opens. Therefore it is recommended that the host application displays a spinner or some other visual cue after the SDK has been started. The spinner/whatever can be dismissed when
1. The SDK returns and the `onActivityResult` method of the calling Activity is called with the default or the given `requestCode` (see the [Optional parameters](#optionalparameters) section) or
2. The host application sets a `NinchatSDKEventListener` to the `NinchatSession` object when creating it (see the [Low-level API access](#lowlevelapi) section) and listens to the `onSessionStarted` event that gets called when the SDK will open its UI.

Should there be any issues with the SDK init, the `NinchatSDKEventListener`'s `onSessionInitFailed` method will be called. The `NinchatSDKEventListener` class has also a method `onSessionInitiated` that gets called when the SDK has successfully fetched the configuration.

As of 0.5.0 `onSessionInitiated` will return a `NinchatSessionCredentials` object which client should store in case the app crashes/process is killed. When passing this object to `NinchatSession` constructor the previous session will be opened. If `null` is passed a new session will be opened. If the session is not valid `onSessionInitFailed` will be called and the saved object should be cleared. Saved object should also be cleared every time `onActivityResult` is invoked.

### Setting metadata

The `NinchatSession.Builder` class has a setter for audience metadata (i.e. user information).  It's specified as a `com.ninchat.client.Props` object:

    Props metadata = new Props();
    metadata.setString("Significant Information", someValue);
    metadata.setString("secure", secureMetadata);
    ...
    builder.setAudienceMetadata(metadata);

### <a name="optionalparameters"></a>Optional parameters

The API client accepts a list of preferred environments when it is created as a `String` *array*:

    NinchatSession session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, preferredEnvironments);

With this array, the various parameters used by the SDK are read from the preferred environment data the SDK loads when it is started. Should a parameter be missing from the preferred environment data, the SDK tries to read it from the next preferred environment. If the parameter is not found in any preferred environments, the SDK uses the `default` environment or the parameter key (or a sensible default value) as its value.

The start method can take the request code (`int`) as optional parameters:

    session.start(activity, requestCode);

By default, the request code parameter is `NinchatSession.NINCHAT_SESSION_REQUEST_CODE`. The calling activity can observe the result of the SDK opening by checking the result code in its `onActivityResult` method for the selected request code. If the SDK has joined a queue, the result code will be `Activity.RESULT_OK` while in the other cases it will be `Activity.RESULT_CANCELLED`.

Alternatively, if the client knows the queue ID it wants to join, the SDK can be started by calling the `start` method with the queue ID (`String`):

    session.start(activity, queueId);
    session.start(activity, requestCode, queueId);

These methods open the SDK directly to the queueing view for that given queue.

### <a name="lowlevelapi"></a>Low-level API access

The SDK exposes the low-level communication interface with the method `getSession()` in the `NinchatSession` class. The host app may use this object to communicate to the server, bypassing the SDK logic.

Furthermore, the host application can register itself (or its property/properties) as a listener to the low-level API events and/or logs by creating the `NinchatSession` instance with the listeners as constructor arguments:

### Following constructors deprecated in 0.6.0
```
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, eventListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, preferredEnvironments, eventListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, logListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, preferredEnvironments, logListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, eventListener, logListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, preferredEnvironments, eventListener, logListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, eventListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, logListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, eventListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, logListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, eventListener, logListener);
session = new NinchatSession(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, eventListener, logListener);
```

The argument `eventListener`, when non-null, must be an instance of the `NinchatSDKEventListener` class, the `logListener` an instance of the `NinchatSDKLogListener`interface, and `ninchatConfiguration` is an instance of `NinchatConfiguration ` class.

*As of version 0.5.0 `sessionCredentials` can be added to open up a previous session. Passing `null` will open a new session. Passing invalid/outdated `sessionCredentials` will cause `onSessionInitFailed` to be invoked.*

*As of version 0.6.0 aforementioned `NinchatSession` constructors are deprecated and a builder pattern has been introduced. `NinchatSession` object needs to be initialized the following way:*

```
NinchatSession.Builder builder = new NinchatSession.Builder(applicationContext, configurationKey);
builder.setSessionCredentials(sessionCredentials); // optional
builder.setConfiguration(ninchatConfiguration); // optional
builder.setPreferredEnvironments(preferredEnvironments); // optional
builder.setEventListener(eventListener); // optional
builder.setLogListener(logListener); // optional
NinchatSession session = builder.create();
```

See [Ninchat API Reference](https://github.com/ninchat/ninchat-api/blob/v2/api.md) for information about the API's outbound Actions and inbound Events.

### Resuming ninchat session using saved credentials

Ninchat support session resuming. In order to support session resuming feature using existing saved credentials in your application, you need to create and pass ( *optional* )  `NinchatSessionCredentials` object instance in the builder script

```java
SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", MODE_PRIVATE);

NinchatSessionCredentials sessionCredentials = new NinchatSessionCredentials(
  pref.getString("user_id", null),
  pref.getString("user_auth", null), 
  pref.getString("session_id", null));

NinchatSession.Builder builder = new NinchatSession.Builder(
  getApplicationContext(),
  getString(R.string.ninchat_configuration_key))
                .setConfiguration(this.ninchatConfiguration)
                // If session should be established using saved credentials
                .setSessionCredentials(sessionCredentials)
                .setEventListener(eventListener);
```



One way to get and update user credentails is to listen `onSessionInitiated`, `onSessionInitFailed` callback(s) from `NinchatSDKEventListener`. When session is initialised you can catch the user id, user auth, and session id related information which you can later pass by call `setSessionCredentials` method.

```kotlin
private val eventListener: NinchatSDKEventListener = object : NinchatSDKEventListener() {
        override fun onSessionInitiated(sessionCredentials: NinchatSessionCredentials) {
            Log.e(">>", sessionCredentials.toString())
            Handler(Looper.getMainLooper()).post {
                findViewById<View>(R.id.button).visibility = View.VISIBLE
                findViewById<View>(R.id.progress).visibility = View.GONE
                val pref = applicationContext.getSharedPreferences("pref", MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString("user_id", sessionCredentials.userId)
                editor.putString("user_auth", sessionCredentials.userAuth)
                editor.putString("session_id", sessionCredentials.sessionId)
                editor.apply()
            }
        }

        override fun onSessionInitFailed() {
            Log.e(">>", "session failed")
            Handler(Looper.getMainLooper()).post {
                findViewById<View>(R.id.button).visibility = View.VISIBLE
                findViewById<View>(R.id.progress).visibility = View.GONE
                val pref = applicationContext.getSharedPreferences("pref", MODE_PRIVATE)
                val editor = pref.edit()
                editor.remove("user_id")
                editor.remove("user_auth")
                editor.remove("session_id")
                editor.apply()
            }
        }

        override fun onSessionError(error: Exception) {
            Log.e(">>", error.localizedMessage)
        }

        override fun onEvent(params: Props, payload: Payload) {
            Log.e("On Event >>", params.toString())
            if(params.getString("event") != "error" ) {
                return
            }
            // there is an error event. may be process error event based on application needs
            when(params.getString("error_type")){
                 "permission_already_spent", "permission_expired" -> {
                    // close ninchat session and try with valid secure metadata
                     ninchatSession?.close()
                }
            }
        }
    }
```



#### Initiate a session with custom/dynamic user name

If we want to set a dynamic user name to a client,  we can pass the user name using NinchatConfiguration. Here is how we can create a NinchatConfiguration with a dynamic user name

```kotlin
val ninchatConfiguration = NinchatConfiguration.Builder()
    .setUserName("Test Android client")
    .create()
```

Then, we can pass this to the given session using

```kotlin
val builder = NinchatSession.Builder(applicationContext, getString(R.string.ninchat_configuration_key))
    .setConfiguration(ninchatConfiguration)
    .setEventListener(eventListener)
val ninchatSession = builder.create()
```


#### Notes related to secure metadata 

If you are using secure metadata in your application, and for some reason the secure metadata has expired or already in used; Ninchat SDK will throw a relevant error event. *In this case, you have to reinitialize Ninchat session with new secure token*. You can leverage `onEvent` callback to catch secure metadata related error. Along with that, you will receive all Ninchat SDK related low-level events in `onEvent` callback. 

- [List of Low-level API events](https://github.com/ninchat/ninchat-api/blob/v2/api.md#events)
- [List of Low-level API Error types](https://github.com/ninchat/ninchat-api/blob/v2/api.md#error-types)

For secure metadata related error, we are only interested `permission_already_spent` or `permission_expired`. If any of the error happens, close the existing Ninchat session, and creates a new Ninchat session with a valid secure metadata. Please, check the above `onEvent`  sample code implementation for reference.



## Overriding SDK assets

### Images and animations

For now, the application can override certain images/animations of the SDK. The image/animation override is done by having (one, many, or even all of) them as drawable resources in the application bundle with following names:

| Asset name       | Related UI control(s)           | Notes  |
|:------------- |:-------------|:-----|
| ninchat_icon_loader   | Progress indicator icon in queue view. |  |
| ninchat_icon_chat_writing_indicator      | User is typing.. Indicator icon in chat bubble | Should be a [frame animation](https://developer.android.com/guide/topics/resources/animation-resource#Frame). |
| ninchat_chat_background    | Chat view's background image that gets tiled. | The drawable **must** be a bitmap image. |
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
| ninchat_ui_compose_select_button   | Multichoice select button, unselected |  |
| ninchat_ui_compose_select_button_selected   | Multichoice select button, selected |  |
| ninchat_ui_compose_select_submit   | Multichoice selection submit button |  |
| ninchat_border_with_error | Questionnaire input field styling when error occured |  |
| ninchat_border_with_focus   | Questionnaire input field styling when field is focused |  |
| ninchat_border_with_ok   | Questionnaire input field styling when field input value is a valid value |  |
| ninchat_border_with_unfocus   | Questionnaire input field styling when field input field is not focused |  |
| ninchat_chat_bubble_left_repeated_disabled | Question item background styling where they are disabled or already filled |  |
| ninchat_chat_disable_button | Questionnaire button element styling when they are disabled |  |
| ninchat_chat_questionnaire_background | Form and conversation like questionnaire background styling |  |
| ninchat_chat_secondary_onclicked_button | Question previous button styling when they are clicked |  |
| ninchat_dropdown_border_select | Questionnaire drop down item border styling when they are selected |  |
| ninchat_dropdown_border_not_selected | Questionnaire drop down item default border styling or when when they are selected | |
| ninchat_dropdown_border_with_error | Questionnaire drop down item border styling when error occured | |
| ninchat_dropdown_arrow | Questionnaire drop down option arrow styling | |
| ninchat_icon_back | Questionnaire back button styling | |
| ninchat_icon_next | Questionnaire next icon button styling | |
| ninchat_radio_select_button | Questionnaire radio button syling when it is selected | |






### Colors

In addition, the application can override colors used in the SDK. The colors need to be defined in the application's values using (one, many, or all of) the following names:

| Asset name       | Related UI control(s)
|:------------- |:-------------|
| ninchat_color_button_primary_text | Text on 'primary' buttons |
| ninchat_color_button_secondary_text | Text on 'secondary' buttons |
| ninchat_color_button_disable_text | Ninchat questionnaire Button text color when it is disabled |
| ninchat_color_info_text | Chat view's meta information (eg. 'Chat started') |
| ninchat_color_chat_name | User name above chat bubbles |
| ninchat_color_chat_timestamp | Timestamp above chat bubbles |
| ninchat_color_chat_bubble_left_text | Text in others' chat messages |
| ninchat_color_chat_bubble_right_text | Text in my chat messages |
| ninchat_color_textarea_text | Chat input text |
| ninchat_color_textarea_text_hint | Chat input text hint |
| ninchat_color_textarea_submit_text | Message submit button title |
| ninchat_color_chat_bubble_left_link | Link color in others' messages |
| ninchat_color_chat_bubble_right_link | Link color in my messages |
| ninchat_color_modal_background | Background in 'modal dialogs' |
| ninchat_color_modal_title_text | Text in 'modal dialogs' |
| ninchat_color_background_top | Background of the top part in some views |
| ninchat_color_text_top | Text in top parts of some views |
| ninchat_color_link | Link color (except in chat bubbles) |
| ninchat_color_background_bottom | Background of the bottom part in some views |
| ninchat_color_text_bottom | Text in bottom parts of some views |
| ninchat_color_rating_positive_text | Text on the positive rating button |
| ninchat_color_rating_neutral_text | Text on the neutral rating button |
| ninchat_color_rating_negative_text | Text on the negative rating button |
| ninchat_color_ui_compose_select_unselected_text | Text on the unselected multichoice button |
| ninchat_color_ui_compose_select_selected_text | Text on the selected multichoice button |
| ninchat_color_ui_compose_submit_text | Text on the multichoice selection submit button |
| ninchat_color_text_normal | Ninchat questionnaire TextView default item color |
| ninchat_color_text_disabled | Ninchat questionnaire TextView item color when they are disabled |
| ninchat_color_radio_item_selected_text | Ninchat questionnaire Radio Button item color when the item is selected |
| ninchat_color_radio_item_unselected_text | Ninchat questionnaire Radio Button item color when the item is not selected |
| ninchat_color_dropdown_selected_text | Ninchat questionnaire dropdown select item text color when the item is selected |
| ninchat_color_dropdown_unselected_text | Ninchat questionnaire dropdown select item text color when the item is not selected |
| ninchat_color_checkbox_selected | Ninchat questionnaire checkbox item  color when the item is selected |
| ninchat_color_checkbox_unselected | Ninchat questionnaire checkbox item  color when the item is not selected |



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

Pallab Gain pallab.gain@somia.fi
