package com.ninchat.sdk.views;

import android.content.Context;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class NinchatWebRTCView implements PeerConnection.Observer, SdpObserver {

    private View videoContainer;
    private GLSurfaceView video;
    private VideoSource localVideoSource;
    private MediaStream localStream;
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;
    private VideoRenderer localRender;
    private VideoRenderer.Callbacks localRenderCallback;
    private VideoTrack remoteVideoTrack;
    private VideoRenderer remoteRender;
    private VideoRenderer.Callbacks remoteRenderCallback;

    private JSONObject offer;
    private JSONObject answer;

    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;

    private class InitTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            VideoRendererGui.setView(video, null);
            remoteRenderCallback = VideoRendererGui.create(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, false);
            localRenderCallback = VideoRendererGui.create(75, 75, 25, 25, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);
            PeerConnectionFactory.initializeAndroidGlobals(videoContainer.getContext(), true, true, true);
            peerConnectionFactory = new PeerConnectionFactory();
            return null;
        }
    }

    public NinchatWebRTCView(final View view) {
        videoContainer = view;
        video = view.findViewById(R.id.video);
        new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void call() {
        videoContainer.setVisibility(View.VISIBLE);
        final RotateAnimation animation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(3000);
        final ImageView spinner = videoContainer.findViewById(R.id.video_call_spinner);
        spinner.setVisibility(View.VISIBLE);
        spinner.setAnimation(animation);
        NinchatSessionManager.getInstance().sendWebRTCCall();
    }

    public boolean handleWebRTCMessage(final String messageType, final String payload) {
        if (NinchatSessionManager.MessageTypes.OFFER.equals(messageType)) {
            try {
                offer = new JSONObject(payload);
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
                return false;
            }
            NinchatSessionManager.getInstance().sendWebRTCBeginIce();
        } else if (NinchatSessionManager.MessageTypes.PICK_UP.equals(messageType)) {
            try {
                final ImageView spinner = videoContainer.findViewById(R.id.video_call_spinner);
                spinner.getAnimation().cancel();
                spinner.setVisibility(View.GONE);
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
                servers.add(new PeerConnection.IceServer(serverInfo.getUrl(), serverInfo.getUsername(),serverInfo.getCredential()));
            }
            for (NinchatWebRTCServerInfo serverInfo : NinchatSessionManager.getInstance().getTurnServers()) {
                servers.add(new PeerConnection.IceServer(serverInfo.getUrl(), serverInfo.getUsername(),serverInfo.getCredential()));
            }
            final MediaConstraints mediaConstraints = new MediaConstraints();
            final MediaConstraints.KeyValuePair optionalConstraints = new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true");
            mediaConstraints.optional.add(optionalConstraints);
            peerConnection = peerConnectionFactory.createPeerConnection(servers, mediaConstraints, this);
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
        localStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        localVideoTrack = getLocalVideoTrack();
        if (localVideoTrack != null) {
            localStream.addTrack(localVideoTrack);
        }
        final AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localStream.addTrack(peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource));
        return localStream;
    }

    private VideoTrack getLocalVideoTrack() {
        VideoTrack videoTrack = null;

        final VideoCapturer videoCapturer = getVideoCapturer();
        if (videoCapturer != null) {
            localVideoSource = peerConnectionFactory.createVideoSource(videoCapturer, new MediaConstraints());
            videoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", localVideoSource);
            localRender = new VideoRenderer(localRenderCallback);
            videoTrack.addRenderer(localRender);
        }

        return videoTrack;
    }

    // Hackity hack, copy-pasted from https://github.com/pristineio/apprtc-android/blob/master/app/src/main/java/org/appspot/apprtc/AppRTCDemoActivity.java
    // This works, while all the other solutions do not for some reason
    private VideoCapturer getVideoCapturer() {
        String[] cameraFacing = { "front"};
        int[] cameraIndex = { 0, 1 };
        int[] cameraOrientation = { 0, 90, 180, 270 };
        for (String facing : cameraFacing) {
            for (int index : cameraIndex) {
                for (int orientation : cameraOrientation) {
                    String name = "Camera " + index + ", Facing " + facing +
                            ", Orientation " + orientation;
                    final VideoCapturer capturer = VideoCapturerAndroid.create(name);
                    if (capturer != null) {
                        return capturer;
                    }
                }
            }
        }
        return null;
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
    public void onAddStream(MediaStream mediaStream) {
        final LinkedList<VideoTrack> videoTracks = mediaStream.videoTracks;
        if (videoTracks.size() == 0) {
            return;
        }
        remoteVideoTrack = videoTracks.getFirst();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                remoteRender = new VideoRenderer(remoteRenderCallback);
                remoteVideoTrack.addRenderer(remoteRender);
                VideoRendererGui.update(remoteRenderCallback, 0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, false);
                videoContainer.setVisibility(View.VISIBLE);
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
        if (localVideoTrack != null) {
            localVideoTrack.removeRenderer(localRender);
        }
        if (localRender != null) {
            localRender.dispose();
        }
        if (remoteVideoTrack != null) {
            remoteVideoTrack.removeRenderer(remoteRender);
        }
        if (remoteRender != null) {
            remoteRender.dispose();
        }
        if (peerConnection != null) {
            peerConnection.close();
            if (sendMessage) {
                NinchatSessionManager.getInstance().sendWebRTCHangUp();
            }
        }
        peerConnection = null;
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
            video.onResume();
        } catch (final Exception e) {
            // Not fully initialized yet, ignore the exception
        }
    }

    public void onPause() {
        try {
            video.onPause();
        } catch (final Exception e) {
            // Not fully initialized yet, ignore the exception
        }
    }

}