package com.ninchat.sdk.views;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatWebRTCServerInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public final class NinchatWebRTCView implements PeerConnection.Observer, SdpObserver {

    private GLSurfaceView remoteVideo;
    private GLSurfaceView localVideo;
    private VideoSource localVideoSource;

    private JSONObject offer;

    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;

    public NinchatWebRTCView(final View view) {
        remoteVideo = view.findViewById(R.id.remoteVideo);
        localVideo = view.findViewById(R.id.localVideo);
        /*try {
            peerConnectionFactory = new PeerConnectionFactory();
        } catch (final Exception e) {
            Log.e("JUSSI", "new PeerConnectionFactory()", e);
        }*/
    }

    public void handleWebRTCMessage(final String messageType, final String payload) {
        if (NinchatSessionManager.MessageTypes.OFFER.equals(messageType)) {
            try {
                offer = new JSONObject(payload);
                Log.e("JUSSI", offer.toString());
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
                return;
            }
            NinchatSessionManager.getInstance().sendWebRTCBeginIce();
        } else if (NinchatSessionManager.MessageTypes.WEBRTC_SERVERS_PARSED.equals(messageType)) {
            try {
                startWithSDP(offer.getJSONObject("sdp"));
            } catch (final JSONException e) {
                NinchatSessionManager.getInstance().sessionError(e);
            }
        }
    }

    protected void startWithSDP(final JSONObject offer) {
        Log.e("JUSSI", offer.toString());
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
            /*final SessionDescription remoteDescription = new SessionDescription(SessionDescription.Type.OFFER, offer.getString("sdp"));*/
            //peerConnection.setRemoteDescription(this, remoteDescription);
        } catch (final Exception e) {
            Log.e("JUSSI", "error", e);
        }
    }

    private MediaStream getLocalMediaStream() {
        final MediaStream localStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        final VideoTrack videoTrack = getLocalVideoTrack();
        if (videoTrack != null) {
            localStream.addTrack(videoTrack);
        }
        //localStream.addTrack(peerConnectionFactory.createAudioTrack("ARDAMSa0", null));
        return localStream;
    }

    private VideoTrack getLocalVideoTrack() {
        VideoTrack videoTrack = null;

        final CameraManager cameraManager = (CameraManager) NinchatSessionManager.getInstance().getContext().getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            for (String id : cameraManager.getCameraIdList()) {
                Log.e("JUSSI", "camera id " + id);
                final CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                final Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }
        } catch (final CameraAccessException e) {
            // Ignore
        }
        Log.e("JUSSI", "front camera id = " + cameraId);
        /*if (cameraId != null) {
            final VideoCapturer videoCapturer = cameraEnumerator.createCapturer(cameraId, null);
            final VideoSource videoSource = peerConnectionFactory.createVideoSource(false);
            videoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
        }*/

        return videoTrack;
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.e("JUSSI", "onSignalingChange: " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.e("JUSSI", "onIceConnectionChange: " + iceConnectionState);
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.e("JUSSI", "onIceConnectionReceivingChange: " + b);

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.e("JUSSI", "onIceGatheringChange: " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.e("JUSSI", "onIceCandidate: " + iceCandidate);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.e("JUSSI", "onAddStream: " + mediaStream);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.e("JUSSI", "onRemoveStream: " + mediaStream);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.e("JUSSI", "onDataChannel: " + dataChannel);
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.e("JUSSI", "onRenegotiationNeeded");
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.e("JUSSI", "onCreateSuccess: " + sessionDescription);
    }

    @Override
    public void onSetSuccess() {
        Log.e("JUSSI", "onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e("JUSSI", "onCreateFailure: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e("JUSSI", "onSetFailure: " + s);
    }
}
