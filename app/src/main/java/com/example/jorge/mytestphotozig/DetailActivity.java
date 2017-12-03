package com.example.jorge.mytestphotozig;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import java.util.List;
import java.util.concurrent.TimeUnit;

import models.Objects;

import static common.Utility.BASE_STORAGE;
import static common.Utility.EXTRA_DATA;
import static common.Utility.EXTRA_POSITION;
import static common.Utility.KEY_EXTRA_DATA;
import static common.Utility.TAG_INFORMATION;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, ExoPlayer.EventListener {

    private static final int CORRECT_ANSWER_DELAY_MILLIS = 1000;
    private static final String REMAINING_SONGS_KEY = "remaining_songs";


    private double timeElapsed = 0, finalTime = 0;


    private SimpleExoPlayer mExoPlayerAudio;
    private Handler durationHandler = new Handler();
    private SeekBar seekbar;

    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;

    private int mPosition;
    private Bundle mBundle;
    private List<Objects> mData;

    private static  VideoView mVideo;

    private ImageButton mNext;
    private ImageButton mPrior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        mPosition = extras.getInt(EXTRA_POSITION);
        mBundle = extras.getBundle(EXTRA_DATA);

        /**
         * The List<Objects> for get information all List OBJECTS for support change other file MP3 and MP4 .
         */
        mData = (List<Objects>) mBundle.getSerializable(KEY_EXTRA_DATA);

        // Initialize the player view.
        mPlayerView = (SimpleExoPlayerView) findViewById(R.id.sep_playerView_Audio);

        seekbar = (SeekBar) findViewById(R.id.exo_progress);


        /**
         * The component for Show video MP4 .
         */
        mVideo =(VideoView) findViewById(R.id.vv_video);


        ImageButton mNext = (ImageButton) findViewById(R.id.ib_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPosition < mData.size()) {
                    resetSession();
                    mPosition++;
                    initializeMediaSession();
                    initializePlayer(Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getSg()), Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getBg()));
                }else{
                    Toast.makeText(view.getContext(), R.string.Information_Data_last, Toast.LENGTH_LONG)
                            .show();
                }

            }
        });

        ImageButton mPrior = (ImageButton) findViewById(R.id.ib_prior);
        mPrior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPosition > 0) {
                    resetSession();
                    mPosition--;
                    initializeMediaSession();
                    initializePlayer(Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getSg()), Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getBg()));
                }else{
                    Toast.makeText(view.getContext(), R.string.Information_Data_first, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });



        // Initialize the Media Session.
        initializeMediaSession();

        // Initialize the player.
        initializePlayer(Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getSg()), Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getBg()));
    }

    private void resetSession() {
        mVideo.stopPlayback();
        mExoPlayerAudio.stop();
        mExoPlayerAudio = null;
    }

    /**
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(this, "BEBETO");

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);

    }


    /**
     * Shows Media Style notification, with actions that depend on the current MediaSession
     * PlaybackState.
     */
    private void showNotification(PlaybackStateCompat state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        int icon;
        String play_pause;
        if(state.getState() == PlaybackStateCompat.STATE_PLAYING){
            icon = R.drawable.exo_controls_pause;
            play_pause = getString(R.string.pause);
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = getString(R.string.play);
        }


        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));

        NotificationCompat.Action restartAction = new android.support.v4.app.NotificationCompat
                .Action(R.drawable.exo_controls_previous, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent
                        (this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this, 0, new Intent(this, DetailActivity.class), 0);


        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0,1));





        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());


    }


    /**
     * Initialize ExoPlayer.
     */
    private void initializePlayer(Uri mediaUriAudio, Uri mediaUriVideo ) {
        if (mExoPlayerAudio == null) {

            /**
             * Create Audio.
             */
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayerAudio = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);


            mPlayerView.setPlayer(mExoPlayerAudio);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayerAudio.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(this, "ClassicalMusicQuiz");
            MediaSource mediaSourceAudio = new ExtractorMediaSource(mediaUriAudio, new DefaultDataSourceFactory(
                    this, userAgent), new DefaultExtractorsFactory(), null,null);

            PlayLocalVideo(mediaUriVideo.toString());

            mExoPlayerAudio.prepare(mediaSourceAudio);

            durationHandler.postDelayed(updateSeekBarTime, 1);
            mExoPlayerAudio.setPlayWhenReady(true);

        }
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            if (mExoPlayerAudio != null) {
                //get current position
                timeElapsed = mExoPlayerAudio.getCurrentPosition();

                double timeRemaining = finalTime - timeElapsed;
                String second = Float.toString(1000 * (-1 * TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining)));

                Log.i(TAG_INFORMATION, second);

                String textMessageJson = verifyExistMessage(second);

                if (!textMessageJson.equals("0")) {
                    Toast.makeText(seekbar.getContext(), textMessageJson, Toast.LENGTH_LONG)
                            .show();
                }

                //repeat yourself that again in 100 milliseconds
                durationHandler.postDelayed(this, 100);
            }
        }
    };

    public String verifyExistMessage(String second){
        String message = "0";
        if (mData.get(mPosition).getTxts()  != null) {
            for (int i = 0; i < mData.get(mPosition).getTxts().size(); i++) {
                if (convertSecondForMillisecond(mData.get(mPosition).getTxts().get(i).getTime().toString()).equals(second)) {
                    return mData.get(mPosition).getTxts().get(i).getTxt();
                }
                Log.i(TAG_INFORMATION, second + " verifyExistMessage " + convertSecondForMillisecond(mData.get(mPosition).getTxts().get(i).getTime().toString()));
            }
        }
        return message;
    }

    /**
     * Release ExoPlayer.
     */

    public String convertSecondForMillisecond(String second){
        if (!second.equals("0.0")){
            return Float.toString(Float.parseFloat(second) * 10000);
        }else{
            return "0";
        }
    }


    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        mNotificationManager.cancelAll();
        mExoPlayerAudio.stop();
        mExoPlayerAudio.release();
        mExoPlayerAudio = null;
    }
    /**
     * The OnClick method for all of the answer buttons. The method uses the index of the button
     * in button array to to get the ID of the sample from the array of question IDs. It also
     * toggles the UI to show the correct answer.
     */
    @Override
    public void onClick(View v) {


    }


    /**
     * Release the player when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mMediaSession.setActive(false);
    }


    // ExoPlayer Event Listeners

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        if (timeline.getPeriodCount() == 1){
            Toast.makeText(this.getApplicationContext(), R.string.Information_Data_first, Toast.LENGTH_LONG)
                    .show();

        }
        Log.i(TAG_INFORMATION,"onTimelineChanged" +  timeline);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.i(TAG_INFORMATION,"onTracksChanged");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.i(TAG_INFORMATION,"onLoadingChanged");
    }

    /**
     * Method that is called when the ExoPlayer state changes. Used to update the MediaSession
     * PlayBackState to keep in sync, and post the media notification.
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {


        if((playbackState == ExoPlayer.STATE_READY) && playWhenReady){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayerAudio.getCurrentPosition(), 1f);
            mVideo.start();
        } else if((playbackState == ExoPlayer.STATE_READY)){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayerAudio.getCurrentPosition(), 1f);
            mVideo.pause();
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
        showNotification(mStateBuilder.build());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
    }




    public void PlayLocalVideo(String nameFileVideo)
    {


        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideo);
        mVideo.setMediaController(mediaController);
        mVideo.setKeepScreenOn(true);
        mVideo.setVideoPath(nameFileVideo);
        mVideo.start();
        mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });


        mVideo.requestFocus();
    }


    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayerAudio.setPlayWhenReady(true);

        }

        @Override
        public void onPause() {
            mExoPlayerAudio.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayerAudio.seekTo(0);
        }
    }

    /**
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        Log.i(TAG_INFORMATION,"onPositionDiscontinuity");

    }



}
