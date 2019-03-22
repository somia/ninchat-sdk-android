package com.ninchat.sdk.views;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatWebRTCServerInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
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
import java.util.List;

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

    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;

    public NinchatWebRTCView(final View view) {
        videoContainer = view;
        eglBase = EglBase.create();
        remoteVideo = view.findViewById(R.id.video);
        localVideo = view.findViewById(R.id.pip_video);
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(view.getContext().getApplicationContext()).createInitializationOptions());
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
    }

    private void animateSpinner() {
        final RotateAnimation animation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(3000);
        final ImageView spinner = videoContainer.findViewById(R.id.video_call_spinner);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAnimation(animation);
    }

    public void call() {
        videoContainer.setVisibility(View.VISIBLE);
        animateSpinner();
        NinchatSessionManager.getInstance().sendWebRTCCall();
    }

    private void removeSpinner() {
        final ImageView spinner = videoContainer.findViewById(R.id.video_call_spinner);
        final Animation animation = spinner.getAnimation();
        animation.cancel();
        animation.reset();
        spinner.setVisibility(View.GONE);
    }

    public boolean handleWebRTCMessage(final String messageType, final String payload) {
        if (NinchatSessionManager.MessageTypes.OFFER.equals(messageType)) {
            try {
                offer = new JSONObject(payload);
                videoContainer.setVisibility(View.VISIBLE);
                animateSpinner();
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
                return false;
            }
            NinchatSessionManager.getInstance().sendWebRTCBeginIce();
        } else if (NinchatSessionManager.MessageTypes.PICK_UP.equals(messageType)) {
            try {
                removeSpinner();
                final JSONObject answer = new JSONObject(payload);
                if (answer.getBoolean("answer")) {
                    NinchatSessionManager.getInstance().sendWebRTCBeginIce();
                } else {
                    videoContainer.setVisibility(View.GONE);
                    return true;
                }
            } catch (final JSONException e) {
                videoContainer.setVisibility(View.GONE);
                return true;
            }
        } else if (NinchatSessionManager.MessageTypes.WEBRTC_SERVERS_PARSED.equals(messageType)) {
            try {
                startWithSDP(offer != null ? offer.getJSONObject("sdp") : null);
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
            }
        } else if (NinchatSessionManager.MessageTypes.ANSWER.equals(messageType) && peerConnection != null) {
            try {
                answer = new JSONObject(payload);
                if (peerConnection.getRemoteDescription() == null) {
                    peerConnection.setRemoteDescription(this, new SessionDescription(SessionDescription.Type.ANSWER, answer.getJSONObject("sdp").getString("sdp")));
                }
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
            }
        } else if (NinchatSessionManager.MessageTypes.ICE_CANDIDATE.equals(messageType) && peerConnection != null && peerConnection.iceGatheringState() == PeerConnection.IceGatheringState.GATHERING) {
            JSONObject candidate;
            Log.e("JUSSI", "payload: " + payload);
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
        } else if (NinchatSessionManager.MessageTypes.HANG_UP.equals(messageType)) {
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
            final List<PeerConnection.IceServer> servers = new ArrayList<>();
            for (NinchatWebRTCServerInfo serverInfo : NinchatSessionManager.getInstance().getStunServers()) {
                servers.add(PeerConnection.IceServer.builder(serverInfo.getUrl()).setUsername(serverInfo.getUsername()).setPassword(serverInfo.getCredential()).createIceServer());
            }
            for (NinchatWebRTCServerInfo serverInfo : NinchatSessionManager.getInstance().getTurnServers()) {
                servers.add(PeerConnection.IceServer.builder(serverInfo.getUrl()).setUsername(serverInfo.getUsername()).setPassword(serverInfo.getCredential()).createIceServer());
            }
            final PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(servers);
            configuration.sdpSemantics = PeerConnection.SdpSemantics.PLAN_B;
            peerConnection = peerConnectionFactory.createPeerConnection(configuration, this);
            peerConnection.addStream(getLocalMediaStream());
            if (offer != null) {
                peerConnection.setRemoteDescription(this, new SessionDescription(SessionDescription.Type.OFFER, sdp.getString("sdp")));
            } else {
                peerConnection.createOffer(this, getDefaultConstrains());
            }
        } catch (final Exception e) {
            // TODO: Show error?
        }
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

    // Hackity hack, copy-pasted from https://github.com/pristineio/apprtc-android/blob/master/app/src/main/java/org/appspot/apprtc/AppRTCDemoActivity.java
    // This works, while all the other solutions do not for some reason
    private VideoCapturer getVideoCapturer() {
        final Camera2Enumerator enumerator = new Camera2Enumerator(videoContainer.getContext());
        final String[] deviceNames = enumerator.getDeviceNames();
        for (final String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                final VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        // Front facing camera not found, try something else
        for (final String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {

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
        NinchatSessionManager.getInstance().sendWebRTCIceCandidate(iceCandidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        final List<VideoTrack> videoTracks = mediaStream.videoTracks;
        if (videoTracks.size() == 0) {
            return;
        }
        remoteVideoTrack = videoTracks.get(0);
        for (final VideoSink remoteSink : remoteSinks) {
            remoteVideoTrack.addSink(remoteSink);
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                removeSpinner();
            }
        });

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

    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        if (peerConnection.getLocalDescription() == null) {
            peerConnection.setLocalDescription(this, sessionDescription);
        }
        NinchatSessionManager.getInstance().sendWebRTCSDPReply(sessionDescription);
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
                NinchatSessionManager.getInstance().sendWebRTCHangUp();
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
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        videoContainer.setVisibility(View.GONE);
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
        final AudioManager audioManager = (AudioManager) videoContainer.getContext().getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, mute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
            audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, mute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
        }
    }

    private boolean isMicrophoneMuted = false;

    public void toggleMicrophone() {
        isMicrophoneMuted = !isMicrophoneMuted;
        final ImageView image = videoContainer.findViewById(R.id.microphone_on_off);
        image.setImageResource(isMicrophoneMuted ? R.drawable.ninchat_icon_video_microphone_off : R.drawable.ninchat_icon_video_microphone_on);
        if (isMicrophoneMuted) {
            localAudioTrack = localStream.audioTracks.get(0);
            localStream.removeTrack(localAudioTrack);
        } else {
            localStream.addTrack(localAudioTrack);
        }
        peerConnection.removeStream(localStream);
        peerConnection.addStream(localStream);
    }

    private boolean isVideoDisabled = false;

    public void toggleVideo() {
        isVideoDisabled = !isVideoDisabled;
        final ImageView image = videoContainer.findViewById(R.id.video_on_off);
        image.setImageResource(isVideoDisabled ? R.drawable.ninchat_icon_video_camera_off : R.drawable.ninchat_icon_video_camera_on);
        if (isVideoDisabled) {
            localVideoTrack = localStream.videoTracks.get(0);
            localStream.removeTrack(localVideoTrack);
        } else {
            localStream.addTrack(localVideoTrack);
        }
        peerConnection.removeStream(localStream);
        peerConnection.addStream(localStream);
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

}
