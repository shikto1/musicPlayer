package com.example.shishir.mymusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by SHishir on 4/15/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private String songTitle = "", artist = " ";
    private boolean isPlaying = false;
    private IBinder binder = new MusicBinder();
    private ArrayList<Song> songList;
    private int songListSize;
    private int songPosition;
    String LOG_TAG = "kdjf";

    //Variable For Updating SeekBar in MainActivity..............................

    int intSeekPos;
    int mediaPosition;
    private static int songEnded = 0;
    int mediaMax;
    private final Handler handler = new Handler();
    private Intent seekIntent;


    //For PhoneStateListener.......................................
    private boolean isPausedInCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telePhonyManager;

    private Intent headsetIntent;
    private int headSetSwitch = 0;
    private boolean headSetReceiverIsRegistered = false;


    @Override
    public void onCreate() {
        //    Toast.makeText(MusicService.this, "onCreate in Service", Toast.LENGTH_SHORT).show();
        if (songList == null) {
            songList = new AllSong().getSongList(this);
            songListSize = songList.size();
        }
        player = new MediaPlayer();
        initMusicPlayer();
        setUpPhoneStateListener();
        headsetIntent = new Intent(Constants.HEADSET_REMOVED_ACTION);
        seekIntent = new Intent(Constants.UPDATE_SEEKBAR_ACTION);
        registerReceiver(headSetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        headSetReceiverIsRegistered = true;
        super.onCreate();
    }

    /////Set Up PhoneStateListener for Incoming Call ............................................................................
    private void setUpPhoneStateListener() {
        telePhonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (player != null) {
                            if (player.isPlaying()) {
                                pausePlayer();
                                isPausedInCall = true;
                            }
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (player != null) {
                            if (isPausedInCall) {
                                go();
                                isPausedInCall = false;
                            }
                        }
                        break;
                }
            }
        };
        telePhonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    BroadcastReceiver headSetReceiver = new BroadcastReceiver() {

        private boolean headSetConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (headSetConnected && intent.getIntExtra("state", 0) == 0) {
                    headSetConnected = false;
                    headsetIntent.putExtra("hd", true);
                    sendBroadcast(headsetIntent);
                    headSetSwitch = 0;
                } else if (!headSetConnected && intent.getIntExtra("state", 0) == 1) {
                    headSetConnected = true;
                    headSetSwitch = 1;
                }
            }
            switch (headSetSwitch) {
                case 0:
                    pausePlayer();
                    break;
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (intent.getAction().equals(Constants.START_FOREGROUND_SERVICE)) {
//            showNotification();
//            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
//
//        }
        return START_STICKY;
    }

//    private void showNotification() {
//        Notification status;
//        RemoteViews smallViews = new RemoteViews(getPackageName(),
//                R.layout.small_views);
//        RemoteViews bigViews = new RemoteViews(getPackageName(),
//                R.layout.status_bar);
//        status = new Notification.Builder(this).build();
//        status.contentView = smallViews;
//        status.flags = Notification.FLAG_ONGOING_EVENT;
//        status.icon = R.drawable.icon_play_color;
//        //status.contentIntent = ;
//        startForeground(Constants.NOTIFICATION_ID, status);
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //     Toast.makeText(MusicService.this, "OnBind", Toast.LENGTH_SHORT).show();
        return binder;
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
    }


    public void startForeground() {
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);



//
//        Intent prevIntent = new Intent();
//        PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.mipmap.icon_play_for_notification)
                .setTicker(songList.get(songPosition).getTitle())
                .setOngoing(true)
                .setContentTitle("Now playing")
                .setContentText(songTitle);

       // builder.addAction(R.drawable.icon_previous_color,"Prev",pendInt);

        Notification not = builder.build();
        startForeground(Constants.NOTIFICATION_ID, not);
    }


    public void playSong() {
        //play
        player.reset();
        isPlaying = true;
        //get song
        Song selectedSong = songList.get(songPosition);
        //get title
        songTitle = selectedSong.getTitle();
        artist = selectedSong.getArtist();
        //get id
        long currSong = selectedSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        //set the data source
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
        setUpHandler();
    }


    private void setUpHandler() {
        handler.removeCallbacks(sendUpdateToUi);
        handler.postDelayed(sendUpdateToUi, 1000);
    }

    private Runnable sendUpdateToUi = new Runnable() {
        @Override
        public void run() {
            LogMediaPosition();
            handler.postDelayed(this, 1000);
        }
    };

    private void LogMediaPosition() {
        if (player.isPlaying()) {
            mediaPosition = player.getCurrentPosition();
            mediaMax = player.getDuration();
            seekIntent.putExtra("cp", String.valueOf(mediaPosition));
            seekIntent.putExtra("td", String.valueOf(mediaMax));
            sendBroadcast(seekIntent);
        }
    }


    public void pausePlayer() {
        if (isPlaying) {
            player.pause();
            isPlaying = false;
        }
    }

    //playback methods
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getTotalDuration() {
        return player.getDuration();
    }

    /////Go to Previous Track................................................................................
    public void playPrev() {
        songPosition--;
        if (songPosition < 0) songPosition = songListSize - 1;
        playSong();
    }

    //Go To Next Track.........................................................................................
    public void playNext() {
        songPosition++;
        if (songPosition >= songListSize) songPosition = 0;
        playSong();
    }


    public boolean isPlaying() {
        if (player.isPlaying()) {
            return true;
        } else
            return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void seekTo(int position) {
        player.seekTo(position);
    }

    public void go() {
        if (!isPlaying) {
            player.start();
            isPlaying = true;
        }
    }

    public void setSongPosition(int position) {
        songPosition = position;
    }


    public int getSongPosition() {
        return songPosition;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
            headsetIntent.putExtra("songChanged", true);
            sendBroadcast(headsetIntent);
        }

    }


    class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        telePhonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        handler.removeCallbacks(sendUpdateToUi);
        if (headSetReceiverIsRegistered) {
            unregisterReceiver(headSetReceiver);
            headSetReceiverIsRegistered = false;
        }
        //      Toast.makeText(MusicService.this, "Service Destroyed !!!", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
