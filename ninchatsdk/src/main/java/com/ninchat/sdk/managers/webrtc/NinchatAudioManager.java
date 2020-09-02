package com.ninchat.sdk.managers.webrtc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.Log;

import org.webrtc.ThreadUtils;

import java.util.HashSet;
import java.util.Set;

public class NinchatAudioManager {
    private static final String TAG = NinchatAudioManager.class.getSimpleName();

    /**
     * AudioDevice is the names of possible audio devices that we currently
     * support.
     */
    public enum AudioDevice {SPEAKER_PHONE, WIRED_HEADSET, EARPIECE, BLUETOOTH, NONE}

    public enum AudioManagerState {
        UNINITIALIZED,
        RUNNING,
    }

    /**
     * Selected audio device change event.
     */
    public interface AudioManagerEvents {
        // Callback fired once audio device is changed or list of available audio devices changed.
        void onAudioDeviceChanged(
                AudioDevice selectedAudioDevice, Set<AudioDevice> availableAudioDevices);
    }

    @Nullable
    private Context mContext;
    @Nullable
    private AudioManager mAudioManager;

    @Nullable
    private AudioManagerEvents audioManagerEvents;
    private AudioManagerState audioManagerState;
    private int savedAudioMode = AudioManager.MODE_INVALID;
    private boolean savedIsSpeakerPhoneOn;
    private boolean savedIsMicrophoneMute;
    private boolean hasWiredHeadset;

    // Default audio device; speaker phone for video calls or earpiece for audio
    // only calls.
    private AudioDevice defaultAudioDevice;

    // Contains the currently selected audio device.
    // This device is changed automatically using a certain scheme where e.g.
    // a wired headset "wins" over speaker phone. It is also possible for a
    // user to explicitly select a device (and override any predefined scheme).
    // See |userSelectedAudioDevice| for details.
    private AudioDevice selectedAudioDevice;
    // Handles all tasks related to Bluetooth headset devices.
    private final NinchatBluetoothManager bluetoothManager;

    // Contains a list of available audio devices. A Set collection is used to
    // avoid duplicate elements.
    private Set<AudioDevice> audioDevices = new HashSet<>();

    // Broadcast receiver for wired headset intent broadcasts.
    private BroadcastReceiver wiredHeadsetReceiver;

    /* Receiver which handles changes in wired headset availability. */
    private class WiredHeadsetReceiver extends BroadcastReceiver {
        private static final int STATE_UNPLUGGED = 0;
        private static final int STATE_PLUGGED = 1;
        private static final int HAS_NO_MIC = 0;
        private static final int HAS_MIC = 1;

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", STATE_UNPLUGGED);
            int microphone = intent.getIntExtra("microphone", HAS_NO_MIC);
            String name = intent.getStringExtra("name");
            Log.d(TAG, "WiredHeadsetReceiver.onReceive" + ": "
                    + "a=" + intent.getAction() + ", s="
                    + (state == STATE_UNPLUGGED ? "unplugged" : "plugged") + ", m="
                    + (microphone == HAS_MIC ? "mic" : "no mic") + ", n=" + name + ", sb="
                    + isInitialStickyBroadcast());
            hasWiredHeadset = (state == STATE_PLUGGED);
            updateAudioDeviceState();
        }
    }

    /**
     * Construction.
     */
    public static NinchatAudioManager create(Context context) {
        return new NinchatAudioManager(context);
    }

    private NinchatAudioManager(Context context) {
        Log.d(TAG, "ctor");
        ThreadUtils.checkIsOnMainThread();
        mContext = context;
        mAudioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        bluetoothManager = NinchatBluetoothManager.create(context, this);
        wiredHeadsetReceiver = new WiredHeadsetReceiver();
        audioManagerState = AudioManagerState.UNINITIALIZED;
        defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
        Log.d(TAG, "defaultAudioDevice: " + defaultAudioDevice);
    }

    @SuppressWarnings("deprecation")
    public void start(AudioManagerEvents audioManagerEvents) {
        ThreadUtils.checkIsOnMainThread();
        if (audioManagerState == AudioManagerState.RUNNING) {
            Log.e(TAG, "AudioManager is already active");
            return;
        }
        this.audioManagerEvents = audioManagerEvents;
        audioManagerState = AudioManagerState.RUNNING;

        // Store current audio state so we can restore it when stop() is called.
        savedAudioMode = mAudioManager.getMode();
        savedIsSpeakerPhoneOn = mAudioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = mAudioManager.isMicrophoneMute();
        hasWiredHeadset = hasWiredHeadset();

        // Request audio playout focus (without ducking) and install listener for changes in focus.
        int result = mAudioManager.requestAudioFocus(null,
                AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus request granted for VOICE_CALL streams");
        } else {
            Log.e(TAG, "Audio focus request failed");
        }

        // Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
        // required to be in this mode when playout and/or recording starts for
        // best possible VoIP performance.
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Always disable microphone mute during a WebRTC call.
        setMicrophoneMute(false);

        selectedAudioDevice = AudioDevice.NONE;
        audioDevices.clear();

        // Initialize and start Bluetooth if a BT device is available or initiate
        // detection of new (enabled) BT devices.
        bluetoothManager.start();

        // Do initial selection of audio device. This setting can later be changed
        // either by adding/removing a BT or wired headset or by covering/uncovering
        // the proximity sensor.
        updateAudioDeviceState();

        // Register receiver for broadcast intents related to adding/removing a
        // wired headset.
        registerReceiver(wiredHeadsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        Log.d(TAG, "AudioManager started");
    }

    @SuppressWarnings("deprecation")
    // TODO(henrika): audioManager.abandonAudioFocus() is deprecated.
    public void stop() {
        Log.d(TAG, "stop");
        ThreadUtils.checkIsOnMainThread();
        if (audioManagerState != AudioManagerState.RUNNING) {
            Log.e(TAG, "Trying to stop AudioManager in incorrect state: " + audioManagerState);
            return;
        }
        audioManagerState = AudioManagerState.UNINITIALIZED;

        unregisterReceiver(wiredHeadsetReceiver);

        bluetoothManager.stop();

        // Restore previously stored audio states.
        setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        setMicrophoneMute(savedIsMicrophoneMute);
        mAudioManager.setMode(savedAudioMode);

        // Abandon audio focus. Gives the previous focus owner, if any, focus.
        mAudioManager.abandonAudioFocus(null);
        audioManagerEvents = null;
        Log.d(TAG, "AudioManager stopped");
    }

    /**
     * Changes selection of the currently active audio device.
     */
    private void setAudioDeviceInternal(AudioDevice device) {
        Log.d(TAG, "setAudioDeviceInternal(device=" + device + ")");
        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                break;
            case EARPIECE:
            case WIRED_HEADSET:
            case BLUETOOTH:
                setSpeakerphoneOn(false);
                break;
            default:
                Log.e(TAG, "Invalid audio device selection");
                break;
        }
        selectedAudioDevice = device;
    }

    /**
     * Helper method for receiver registration.
     */
    private void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        mContext.registerReceiver(receiver, filter);
    }

    /**
     * Helper method for unregistration of an existing receiver.
     */
    private void unregisterReceiver(BroadcastReceiver receiver) {
        mContext.unregisterReceiver(receiver);
    }

    /**
     * Sets the speaker phone mode.
     */
    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = mAudioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(on);
    }

    /**
     * Sets the microphone mute state.
     */
    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = mAudioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }
        mAudioManager.setMicrophoneMute(on);
    }

    /**
     * Gets the current earpiece state.
     */
    private boolean hasEarpiece() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    @Deprecated
    private boolean hasWiredHeadset() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return mAudioManager.isWiredHeadsetOn();
        } else {
            final AudioDeviceInfo[] devices = mAudioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo device : devices) {
                final int type = device.getType();
                if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    Log.d(TAG, "hasWiredHeadset: found wired headset");
                    return true;
                } else if (type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                    Log.d(TAG, "hasWiredHeadset: found USB audio device");
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Updates list of possible audio devices and make new device selection.
     * TODO: add unit test to verify all state transitions.
     */
    public void updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread();
        Log.d(TAG, "--- updateAudioDeviceState: "
                + "wired headset=" + hasWiredHeadset + ", "
                + "BT state=" + bluetoothManager.getState());
        Log.d(TAG, "Device status: "
                + "available=" + audioDevices + ", "
                + "selected=" + selectedAudioDevice);

        // Check if any Bluetooth headset is connected. The internal BT state will
        // change accordingly.
        // TODO(henrika): perhaps wrap required state into BT manager.
        if (bluetoothManager.getState() == NinchatBluetoothManager.State.HEADSET_AVAILABLE
                || bluetoothManager.getState() == NinchatBluetoothManager.State.HEADSET_UNAVAILABLE
                || bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_DISCONNECTING) {
            bluetoothManager.updateDevice();
        }

        // Update the set of available audio devices.
        Set<AudioDevice> newAudioDevices = new HashSet<>();

        if (bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_CONNECTED
                || bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_CONNECTING
                || bluetoothManager.getState() == NinchatBluetoothManager.State.HEADSET_AVAILABLE) {
            newAudioDevices.add(AudioDevice.BLUETOOTH);
        }

        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            newAudioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            newAudioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                newAudioDevices.add(AudioDevice.EARPIECE);
            }
        }
        // Store state which is set to true if the device list has changed.
        boolean audioDeviceSetUpdated = !audioDevices.equals(newAudioDevices);
        // Update the existing audio device set.
        audioDevices = newAudioDevices;

        boolean needBluetoothAudioStart =
                bluetoothManager.getState() == NinchatBluetoothManager.State.HEADSET_AVAILABLE && !hasWiredHeadset;

        // Need to stop Bluetooth audio if user selected different device and
        // Bluetooth SCO connection is established or in the process.
        boolean needBluetoothAudioStop =
                (bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_CONNECTED
                        || bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_CONNECTING)
                        && (hasWiredHeadset);

        if (bluetoothManager.getState() == NinchatBluetoothManager.State.HEADSET_AVAILABLE
                || bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_CONNECTING
                || bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_CONNECTED) {
            Log.d(TAG, "Need BT audio: start=" + needBluetoothAudioStart + ", "
                    + "stop=" + needBluetoothAudioStop + ", "
                    + "BT state=" + bluetoothManager.getState());
        }

        // Start or stop Bluetooth SCO connection given states set earlier.
        if (needBluetoothAudioStop) {
            bluetoothManager.stopScoAudio();
            bluetoothManager.updateDevice();
        }

        if (needBluetoothAudioStart && !needBluetoothAudioStop) {
            // Attempt to start Bluetooth SCO audio (takes a few second to start).
            if (!bluetoothManager.startScoAudio()) {
                // Remove BLUETOOTH from list of available devices since SCO failed.
                audioDevices.remove(AudioDevice.BLUETOOTH);
                audioDeviceSetUpdated = true;
            }
        }

        // Update selected audio device.
        final AudioDevice newAudioDevice;

        if (bluetoothManager.getState() == NinchatBluetoothManager.State.SCO_CONNECTED) {
            // If a Bluetooth is connected, then it should be used as output audio
            // device. Note that it is not sufficient that a headset is available;
            // an active SCO channel must also be up and running.
            newAudioDevice = AudioDevice.BLUETOOTH;
        } else if (hasWiredHeadset) {
            // If a wired headset is connected, but Bluetooth is not, then wired headset is used as
            // audio device.
            newAudioDevice = AudioDevice.WIRED_HEADSET;
        } else {
            // No wired headset and no Bluetooth, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            // |defaultAudioDevice| contains either AudioDevice.SPEAKER_PHONE or AudioDevice.EARPIECE
            // depending on the user's selection.
            newAudioDevice = defaultAudioDevice;
        }
        // Switch to new device but only if there has been any changes.
        if (newAudioDevice != selectedAudioDevice || audioDeviceSetUpdated) {
            // Do the required device switch.
            setAudioDeviceInternal(newAudioDevice);
            Log.d(TAG, "New device status: "
                    + "available=" + audioDevices + ", "
                    + "selected=" + newAudioDevice);
            if (audioManagerEvents != null) {
                // Notify a listening client that audio device has been changed.
                audioManagerEvents.onAudioDeviceChanged(selectedAudioDevice, audioDevices);
            }
        }
        Log.d(TAG, "--- updateAudioDeviceState done");
    }
}
