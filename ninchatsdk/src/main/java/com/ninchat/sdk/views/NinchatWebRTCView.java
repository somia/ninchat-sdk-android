package com.ninchat.sdk.views;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.managers.webrtc.NinchatAudioManager;
import com.ninchat.sdk.models.NinchatWebRTCServerInfo;
import com.ninchat.sdk.networkdispatchers.NinchatBeginICE;
import com.ninchat.sdk.networkdispatchers.NinchatSendMessage;
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes;
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NinchatWebRTCView implements PeerConnection.Observer, SdpObserver {

    private static final String TAG = NinchatWebRTCView.class.getSimpleName();

    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame videoFrame) {
            if (target == null) {
                Log.d(TAG, "video sink target is null");
                return;
            }
            target.onFrame(videoFrame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    private View videoContainer;
    private EglBase eglBase;
    private SurfaceViewRenderer localVideo;
    private MediaStream localStream;
    private AudioSource audioSource;
    private AudioTrack localAudioTrack;
    private VideoSource localVideoSource;
    private VideoTrack localVideoTrack;
    private ProxyVideoSink localRender = new ProxyVideoSink();
    private VideoCapturer videoCapturer;
    private VideoTrack remoteVideoTrack;
    private List<VideoSink> remoteSinks;
    private SurfaceViewRenderer remoteVideo;
    private SurfaceTextureHelper surfaceTextureHelper;
    private ProxyVideoSink remoteRender = new ProxyVideoSink();

    private JSONObject offer;
    private JSONObject answer;

    private NinchatAudioManager ninchatAudioManager;
    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;
    private Boolean inCall = false;

    public NinchatWebRTCView(final View view) {
        videoContainer = view;
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(view.getContext().getApplicationContext()).createInitializationOptions());
    }

    public void init() {
        if (videoContainer == null) return;
        inCall = true;
        eglBase = EglBase.create();
        remoteVideo = videoContainer.findViewById(R.id.video);
        localVideo = videoContainer.findViewById(R.id.pip_video);
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), false, false))
                .createPeerConnectionFactory();

        remoteSinks = new ArrayList<>();
        localVideo.init(eglBase.getEglBaseContext(), null);
        localVideo.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRender.setTarget(localVideo);
        remoteVideo.init(eglBase.getEglBaseContext(), null);
        remoteVideo.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        remoteVideo.setEnableHardwareScaler(false);
        remoteRender.setTarget(remoteVideo);
        remoteSinks.add(remoteRender);
        localVideo.setZOrderOnTop(true);
        localVideo.setZOrderMediaOverlay(true);
    }

    private void animateSpinner() {
        if (inCall) return;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            final RotateAnimation animation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setDuration(3000);
            final ImageView spinner = videoContainer.findViewById(R.id.video_call_spinner);
            spinner.setVisibility(View.VISIBLE);
            spinner.setAnimation(animation);
        }
    }

    public void call() {
        videoContainer.setVisibility(View.VISIBLE);
        animateSpinner();
        NinchatSendMessage.executeAsync(
                NinchatScopeHandler.getIOScope(),
                NinchatSessionManager.getInstance().getSession(),
                NinchatSessionManager.getInstance().ninchatState.getChannelId(),
                NinchatMessageTypes.CALL,
                "{}",
                aLong -> null
        );
    }

    private void removeSpinner() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            final ImageView spinner = videoContainer.findViewById(R.id.video_call_spinner);
            final Animation animation = spinner.getAnimation();
            if (animation != null) {
                animation.cancel();
                animation.reset();
            }
            spinner.setVisibility(View.GONE);
        }
    }

    public boolean handleWebRTCMessage(final String messageType, final String payload) {
        if (NinchatMessageTypes.OFFER.equals(messageType)) {
            try {
                offer = new JSONObject(payload);
                videoContainer.setVisibility(View.VISIBLE);
                animateSpinner();
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
                return false;
            }
            NinchatBeginICE.executeAsync(
                    NinchatScopeHandler.getIOScope(),
                    NinchatSessionManager.getInstance().getSession(), aLong -> null);
        } else if (NinchatMessageTypes.PICK_UP.equals(messageType)) {
            try {
                removeSpinner();
                final JSONObject answer = new JSONObject(payload);
                if (answer.getBoolean("answer")) {
                    NinchatBeginICE.executeAsync(
                            NinchatScopeHandler.getIOScope(),
                            NinchatSessionManager.getInstance().getSession(), aLong -> null);
                } else {
                    videoContainer.setVisibility(View.GONE);
                    return true;
                }
            } catch (final JSONException e) {
                videoContainer.setVisibility(View.GONE);
                return true;
            }
        } else if (NinchatMessageTypes.WEBRTC_SERVERS_PARSED.equals(messageType)) {
            try {
                startWithSDP(offer != null ? offer.getJSONObject("sdp") : null);
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
            }
        } else if (NinchatMessageTypes.ANSWER.equals(messageType) && peerConnection != null) {
            try {
                answer = new JSONObject(payload);
                if (peerConnection.getRemoteDescription() == null) {
                    peerConnection.setRemoteDescription(this, new SessionDescription(SessionDescription.Type.ANSWER, answer.getJSONObject("sdp").getString("sdp")));
                }
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
            }
        } else if (NinchatMessageTypes.ICE_CANDIDATE.equals(messageType) && peerConnection != null && peerConnection.iceGatheringState() == PeerConnection.IceGatheringState.GATHERING) {
            JSONObject candidate;
            try {
                candidate = new JSONObject(payload).getJSONObject("candidate");
            } catch (final JSONException e) {
                return false;
            }
            String sdpMid;
            try {
                sdpMid = candidate.getString("sdpMid");
            } catch (final JSONException e) {
                return false;
            }
            int sdpMLineIndex;
            try {
                sdpMLineIndex = candidate.getInt("sdpMLineIndex");
            } catch (final JSONException e) {
                return false;
            }
            try {
                peerConnection.addIceCandidate(new IceCandidate(sdpMid, sdpMLineIndex, candidate.getString("candidate")));
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
            }
        } else if (NinchatMessageTypes.HANG_UP.equals(messageType)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    hangUp(false);
                }
            });
            return true;
        }
        return false;
    }

    private MediaConstraints getDefaultConstrains() {
        final MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        return mediaConstraints;
    }

    protected void startWithSDP(final JSONObject sdp) {
        try {
            init();
            initializeAudioManager();
            final List<PeerConnection.IceServer> servers = new ArrayList<>();
            for (NinchatWebRTCServerInfo serverInfo : NinchatSessionManager.getInstance().ninchatState.getStunServers()) {
                servers.add(PeerConnection.IceServer.builder(serverInfo.getUrl()).setUsername(serverInfo.getUsername()).setPassword(serverInfo.getCredential()).createIceServer());
            }
            for (NinchatWebRTCServerInfo serverInfo : NinchatSessionManager.getInstance().ninchatState.getTurnServers()) {
                servers.add(PeerConnection.IceServer.builder(serverInfo.getUrl()).setUsername(serverInfo.getUsername()).setPassword(serverInfo.getCredential()).createIceServer());
            }
            final PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(servers);
            configuration.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
            peerConnection = peerConnectionFactory.createPeerConnection(configuration, this);
            getLocalMediaStream();
            if (offer != null) {
                peerConnection.setRemoteDescription(this, new SessionDescription(SessionDescription.Type.OFFER, sdp.getString("sdp")));
            } else {
                peerConnection.createOffer(this, getDefaultConstrains());
            }
        } catch (final Exception e) {
            // TODO: Show error?
            Log.e("WebRTC", e.getMessage());
        }
    }

    private void initializeAudioManager() {
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        ninchatAudioManager = NinchatAudioManager.create(videoContainer.getContext().getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        ninchatAudioManager.start(new NinchatAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(NinchatAudioManager.AudioDevice audioDevice, Set<NinchatAudioManager.AudioDevice> availableAudioDevices) {
                Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                        + "selected: " + audioDevice);
            }
        });
    }

    private RtpTransceiver getVideoTransceiver() {
        for (final RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            if (transceiver.getMediaType() == MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO) {
                return transceiver;
            }
        }
        return null;
    }

    private MediaStream getLocalMediaStream() {
        localStream = peerConnectionFactory.createLocalMediaStream("NINAMS");
        audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack("NINAMSa0", audioSource);
        localStream.addTrack(localAudioTrack);
        final List<String> mediaIds = new ArrayList<>();
        mediaIds.add("NINAMS");
        peerConnection.addTrack(localAudioTrack, mediaIds);
        localVideoTrack = getLocalVideoTrack();
        if (localVideoTrack != null) {
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
            videoCapturer.initialize(surfaceTextureHelper, videoContainer.getContext().getApplicationContext(), localVideoSource.getCapturerObserver());
            videoCapturer.startCapture(1280, 710, 30);
            localVideoTrack.addSink(localRender);
            localStream.addTrack(localVideoTrack);
            peerConnection.addTrack(localVideoTrack, mediaIds);
            final RtpTransceiver transceiver = getVideoTransceiver();
            if (transceiver != null) {
                final MediaStreamTrack track = transceiver.getReceiver().track();
                peerConnection.addTransceiver(track);
            }
        }
        return localStream;
    }

    private VideoTrack getLocalVideoTrack() {
        VideoTrack videoTrack = null;

        videoCapturer = getVideoCapturer();
        if (videoCapturer != null) {
            localVideoSource = peerConnectionFactory.createVideoSource(false);
            videoTrack = peerConnectionFactory.createVideoTrack("NINAMSv0", localVideoSource);
        }

        return videoTrack;
    }

    private VideoCapturer getVideoCapturerForCameraType(final CameraEnumerator cameraEnumerator, final boolean isFrontCamera) {
        for (final String deviceName : cameraEnumerator.getDeviceNames()) {
            if (cameraEnumerator.isFrontFacing(deviceName) == isFrontCamera) {
                final VideoCapturer videoCapturer = cameraEnumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    private VideoCapturer getVideoCapturer() {
        final Camera2Enumerator camera2Enumerator = new Camera2Enumerator(videoContainer.getContext());
        final Camera1Enumerator camera1Enumerator = new Camera1Enumerator();

        // Try to use Camera2 front camera
        VideoCapturer videoCapturer = getVideoCapturerForCameraType(camera2Enumerator, true);

        if (videoCapturer == null) {
            // Camera2 front camera not found, try Camera1 front camera
            videoCapturer = getVideoCapturerForCameraType(camera1Enumerator, true);
        }

        if (videoCapturer == null) {
            // Use Camera2 back camera
            videoCapturer = getVideoCapturerForCameraType(camera2Enumerator, false);
        }

        if (videoCapturer == null) {
            // Last resort, try Camera1 back camera
            videoCapturer = getVideoCapturerForCameraType(camera1Enumerator, false);
        }

        return videoCapturer;
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        if (newState == PeerConnection.PeerConnectionState.FAILED || newState == PeerConnection.PeerConnectionState.CLOSED) {
            new Handler(Looper.getMainLooper()).post(() -> hangUp(true));
        } else if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // show controls button
                videoContainer.findViewById(R.id.video_call_media_controls).setVisibility(View.VISIBLE);
                // remove spinner
                removeSpinner();
            });
        }
    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {

    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        try {
            final JSONObject data = new JSONObject();
            final JSONObject candidate = new JSONObject();
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("candidate", iceCandidate.sdp);
            candidate.put("id", iceCandidate.sdpMLineIndex);
            candidate.put("label", iceCandidate.sdpMid);
            data.put("candidate", candidate);
            NinchatSendMessage.executeAsync(
                    NinchatScopeHandler.getIOScope(),
                    NinchatSessionManager.getInstance().getSession(),
                    NinchatSessionManager.getInstance().ninchatState.getChannelId(),
                    NinchatMessageTypes.ICE_CANDIDATE,
                    data.toString(),
                    aLong -> null
            );

        } catch (final JSONException e) {
            NinchatSessionManager.getInstance().sessionError(e);
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        for(MediaStream stream : mediaStreams) {
            final List<VideoTrack> videoTracks = stream.videoTracks;
            if (videoTracks.size() == 0) {
                return;
            }
            remoteVideoTrack = videoTracks.get(0);
            for (final VideoSink remoteSink : remoteSinks) {
                remoteVideoTrack.addSink(remoteSink);
            }
        }
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        if (peerConnection.getLocalDescription() == null) {
            peerConnection.setLocalDescription(this, sessionDescription);
        }
        try {
            final Map<SessionDescription.Type, String> typeMap = new HashMap<>();
            typeMap.put(SessionDescription.Type.ANSWER, NinchatMessageTypes.ANSWER);
            typeMap.put(SessionDescription.Type.OFFER, NinchatMessageTypes.OFFER);
            final String messageType = typeMap.get(sessionDescription.type);
            if (messageType == null) {
                return;
            }
            final JSONObject data = new JSONObject();
            final JSONObject sdp = new JSONObject();
            sdp.put("type", sessionDescription.type.canonicalForm());
            sdp.put("sdp", sessionDescription.description);
            data.put("sdp", sdp);
            NinchatSendMessage.executeAsync(
                    NinchatScopeHandler.getIOScope(),
                    NinchatSessionManager.getInstance().getSession(),
                    NinchatSessionManager.getInstance().ninchatState.getChannelId(),
                    messageType,
                    data.toString(),
                    aLong -> null
            );

        } catch (final JSONException e) {
            NinchatSessionManager.getInstance().sessionError(e);
        }

    }

    @Override
    public void onSetSuccess() {
        if (peerConnection.getLocalDescription() == null) {
            peerConnection.createAnswer(this, getDefaultConstrains());
        }
    }

    @Override
    public void onCreateFailure(String s) {
    }

    @Override
    public void onSetFailure(String s) {
    }

    public void hangUp() {
        hangUp(true);
    }

    private void hangUp(final boolean sendMessage) {
        inCall = false;
        if (localRender != null) {
            localRender.setTarget(null);
        }
        if (remoteRender != null) {
            remoteRender.setTarget(null);
        }
        if (localVideo != null) {
            localVideo.release();
            localVideoTrack = null;
        }
        if (remoteVideo != null) {
            remoteVideo.release();
            remoteVideo = null;
        }
        if (peerConnection != null) {
            peerConnection.dispose();
            peerConnection = null;
            if (sendMessage) {
                NinchatSendMessage.executeAsync(
                        NinchatScopeHandler.getIOScope(),
                        NinchatSessionManager.getInstance().getSession(),
                        NinchatSessionManager.getInstance().ninchatState.getChannelId(),
                        NinchatMessageTypes.HANG_UP,
                        "{}",
                        aLong -> null
                );

            }
        }
        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (final InterruptedException e) {
                // Ignore
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }
        if (localVideoSource != null) {
            localVideoSource.dispose();
            localVideoSource = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory.stopAecDump();
            peerConnectionFactory.dispose();
            peerConnectionFactory = null;
        }
        if (eglBase != null) {
            eglBase.release();
            eglBase = null;
        }
        if (ninchatAudioManager != null) {
            ninchatAudioManager.stop();
            ninchatAudioManager = null;
        }
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        videoContainer.setVisibility(View.GONE);
        resetMediaButtons();
    }

    private boolean isAudioMuted = false;

    public void toggleAudio() {
        isAudioMuted = !isAudioMuted;
        final ImageView image = videoContainer.findViewById(R.id.audio_on_off);
        image.setImageResource(isAudioMuted ? R.drawable.ninchat_icon_video_sound_off : R.drawable.ninchat_icon_video_sound_on);
        if (isAudioMuted) {
            toggleAudio(true);
        } else {
            toggleAudio(false);
        }
    }

    private void toggleAudio(final boolean mute) {
        // sanity check
        if (peerConnection == null) {
            return;
        }
        // https://w3c.github.io/webrtc-pc/#rtcrtpreceiver-interface
        for (final RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            if (transceiver.getMediaType() == MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO) {
                final MediaStreamTrack mediaStreamTrack = transceiver.getReceiver().track();
                if (mediaStreamTrack != null) {
                    mediaStreamTrack.setEnabled(!mute);
                }
            }
        }
    }

    private boolean isMicrophoneMuted = false;

    public void toggleMicrophone() {
        isMicrophoneMuted = !isMicrophoneMuted;
        final ImageView image = videoContainer.findViewById(R.id.microphone_on_off);
        image.setImageResource(isMicrophoneMuted ? R.drawable.ninchat_icon_video_microphone_off : R.drawable.ninchat_icon_video_microphone_on);
        localAudioTrack.setEnabled(!isMicrophoneMuted);
    }

    private boolean isVideoDisabled = false;

    public void toggleVideo() {
        isVideoDisabled = !isVideoDisabled;
        final ImageView image = videoContainer.findViewById(R.id.video_on_off);
        image.setImageResource(isVideoDisabled ? R.drawable.ninchat_icon_video_camera_off : R.drawable.ninchat_icon_video_camera_on);
        localVideoTrack.setEnabled(!isVideoDisabled);
    }

    /**
     * Reset media view state
     */
    public void resetMediaButtons() {
        final ImageView microphoneImage = videoContainer.findViewById(R.id.microphone_on_off);
        final ImageView audioImage = videoContainer.findViewById(R.id.audio_on_off);
        final ImageView videoImage = videoContainer.findViewById(R.id.video_on_off);
        microphoneImage.setImageResource(R.drawable.ninchat_icon_video_microphone_on);
        audioImage.setImageResource(R.drawable.ninchat_icon_video_sound_on);
        videoImage.setImageResource(R.drawable.ninchat_icon_video_camera_on);
        videoContainer.findViewById(R.id.video_call_media_controls).setVisibility(View.GONE);
    }

    public void onResume() {
        try {
            // TODO: Resume video?
        } catch (final Exception e) {
            // Not fully initialized yet, ignore the exception
        }
    }

    public void onPause() {
        try {
            // TODO: Pause video?
        } catch (final Exception e) {
            // Not fully initialized yet, ignore the exception
        }
    }

    public boolean isInCall() {
        return this.inCall;
    }

}
