package com.example.jorge.mytestphotozig;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import common.FunctionCommon;
import downloads.Download;
import downloads.DownloadService;
import models.Objects;


import static common.Utility.BASE_STORAGE;
import static common.Utility.EXTRA_DATA;
import static common.Utility.EXTRA_DOWNLOAD;
import static common.Utility.EXTRA_POSITION;
import static common.Utility.EXTRA_POSITION_NUMBER;
import static common.Utility.FILE_DOWNLOAD_COMPLETE;
import static common.Utility.KEY_EXTRA_DATA;
import static common.Utility.TAG_INFORMATION;



public class DetailActivity extends AppCompatActivity implements ExoPlayer.EventListener {

    public static final String MESSAGE_PROGRESS = "message_progress";
    private static final int PERMISSION_REQUEST_CODE = 1;


    private double mTimeElapsed = 0, mFinalTime = 0,  mTimeLast = 0;


    private SimpleExoPlayer mExoPlayerAudio;
    private Handler durationHandler = new Handler();
    private SeekBar seekbar;

    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;

    private int mPosition = 0;
    private Bundle mBundle;
    private List<Objects> mData;

    private static  VideoView mVideo;

    private ImageButton mNext;
    private ImageButton mPrior;

    @BindView(R.id.pb_progress)
    ProgressBar mProgressBar;
    @BindView(R.id.tv_progress_text)
    TextView mProgressText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        try {
            registerReceiver();
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }


        /**
         * Get parameters the other activity.
         */
        Bundle extras = getIntent().getExtras();
        mPosition = Integer.parseInt(extras.getString(EXTRA_POSITION));
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

        Toast.makeText(seekbar.getContext(), R.string.Information_look_control, Toast.LENGTH_LONG)
                .show();


        ImageButton mNext = (ImageButton) findViewById(R.id.ib_next);
        mNext.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mPosition < mData.size()) {
                resetSession();
                mPosition++;
                initializeMediaSession();
                if (verifyExistFiles(mPosition)) {
                    mProgressText.setText("");
                    initializePlayer(Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getSg()), Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getBg()));
                }else{
                    allStarDownload(Integer.toString(mPosition));
                }
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
                if (verifyExistFiles(mPosition)) {
                    mProgressText.setText("");
                    initializePlayer(Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getSg()), Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getBg()));
                }else{
                    allStarDownload(Integer.toString(mPosition));
                }
            }else{
                    Toast.makeText(view.getContext(), R.string.Information_Data_first, Toast.LENGTH_LONG)
                        .show();
                 }
            }
        });


        if (!verifyExistFiles(mPosition)){
            allStarDownload(Integer.toString(mPosition));
        }else{
            initializeMediaSession();
            initializePlayer(Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getSg()), Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(mPosition).getBg()));
            /**
             * Put Name Repositorie in  title.
             */
            this.setTitle(mData.get(mPosition).getBg());
        }

    }

    /**
     * Verify if this files BG and SG they're gone
     */
    private boolean verifyExistFiles(int position) {
        if (position < mData.size()){
            String fileNameWithPathBg = Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(position).getBg();
            String fileNameWithPathSg = Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(position).getSg();

            File fileBg = new File(fileNameWithPathBg);
            File fileSg = new File(fileNameWithPathSg);

            if (!fileBg.exists() || !fileSg.exists()) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }


    }

    /**
     * Call intent the Download with put extra
     */
    private void startDownload(String fileName, String position){

        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(EXTRA_POSITION,fileName);
        intent.putExtra(EXTRA_POSITION_NUMBER, position);
        this.startService(intent);



    }

    /**
     * Reset Session the Audio and the Video
     */
    private void resetSession() {
        if (mVideo != null) {
            mVideo.stopPlayback();
        }
        if (mExoPlayerAudio != null) {
            mExoPlayerAudio.stop();
        }

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

    /**
     * Control the time for Put TXTs in TextView with this information.
     */
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            if (mExoPlayerAudio != null) {
                //get current position
                mTimeElapsed = mExoPlayerAudio.getCurrentPosition();
                mTimeLast = mExoPlayerAudio.getDuration();

                double timeRemaining = mFinalTime - mTimeElapsed;
                String second = Float.toString(1000 * (-1 * TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining)));

                Log.i(TAG_INFORMATION, second);

                String textMessageJson = verifyExistMessage(second);

                if (!textMessageJson.equals("0")) {
                    mProgressText.setText(textMessageJson);
                    Toast.makeText(seekbar.getContext(), textMessageJson, Toast.LENGTH_LONG)
                            .show();
                }


                Log.i(TAG_INFORMATION, "Last:" + mTimeLast + " Progress:" + mTimeElapsed);
                if ((mTimeLast < mTimeElapsed) && (mTimeElapsed != 0.0) && (mTimeLast > 0)) {
                    if (mVideo != null) {
                        mVideo.pause();
                    }
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
        mVideo.stopPlayback();
        mVideo = null;
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



    /**
     * Play video for play Video.
     */
    public void PlayLocalVideo(String nameFileVideo)
    {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideo);
        mediaController.setVisibility(View.INVISIBLE);

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


    /**
     * Manager the receiver for support return when finished.
     */
    private void registerReceiver() throws IntentFilter.MalformedMimeTypeException {

        Bundle extras = getIntent().getExtras();
        mPosition =  Integer.parseInt(extras.getString(EXTRA_POSITION));

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);




    }

    /**
     * BroadcastReceiver for wait when download finished.
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if(intent.getAction().equals(MESSAGE_PROGRESS)){


                int position = Integer.parseInt(intent.getStringExtra(EXTRA_POSITION_NUMBER).toString());

                Download download = intent.getParcelableExtra(EXTRA_DOWNLOAD);
                mProgressBar.setProgress(download.getProgress());
                if(download.getProgress() == 100){



                    // Initialize the player.
                    if (verifyExistFiles(position) ){
                        mProgressText.setText(FILE_DOWNLOAD_COMPLETE);
                        mProgressBar.setVisibility(View.INVISIBLE);

                        resetSession();
                        initializeMediaSession();
                        initializePlayer(Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(position).getSg()), Uri.parse(Environment.getExternalStoragePublicDirectory(BASE_STORAGE).toString() + "/" + mData.get(position).getBg()));
                    }

                } else {

                    mProgressText.setText(String.format("Downloaded (%d/%d) MB",download.getCurrentFileSize(),download.getTotalFileSize()));

                }
            }
        }
    };

    /**
     * Request Permission download for the user .
     */
    private void requestPermission(){

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }

    /**
     * Request Permission .
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    verifyExistFiles(mPosition);
                } else {

                    Toast.makeText(this.getApplicationContext(), TAG_INFORMATION, Toast.LENGTH_LONG);
                }
                break;
        }
    }


    /**
     * Start download all file SG and BG.
     */
    private void allStarDownload(String position) {


        if (FunctionCommon.checkPermission(this)) {
            if (!position.equals("")){
                if (Integer.parseInt(position) < mData.size()) {
                    mProgressText.setText("");
                    mProgressBar.setVisibility(View.VISIBLE);
                    startDownload(mData.get((mPosition)).getSg(), position);
                    startDownload(mData.get((mPosition)).getBg(), position);
                }else{
                    Toast.makeText(this, R.string.Information_Data_last, Toast.LENGTH_LONG)
                            .show();
                }
            }
        } else {
            requestPermission();

        }

   }


}
