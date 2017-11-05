package com.example.shishir.mymusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton previous, backward, playPause, forward, next;
    private TextView songNumberTv, songTitle,artistTv, initTimeInSeekBar, endingTimeInSeekBar;
    private ListView songView;
    private SeekBar seekBar;
    private int seekMax;

    private ArrayList<Song> songList = null;
    private int songListSize;
    private ListView songListView;

    boolean isPaused = false;

    private MusicService musicService;
    private boolean isBound = false;

    private boolean myCustomReceiverIsRegistered = false;
    private boolean mySeekBarReceiverIsRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

        startService(new Intent(this, MusicService.class));
        findViewById();
        //       Toast.makeText(MainActivity.this, "onCreate in activity", Toast.LENGTH_SHORT).show();


        songList = new AllSong().getSongList(this);
        songListSize = songList.size();
        songNumberTv.setText("" + 0 + "/" + songListSize);
        SongAdapter songAdapter = new SongAdapter(this, songList);
        songListView.setAdapter(songAdapter);
        songListView.setFastScrollEnabled(true);
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (musicService != null) {
                    musicService.setSongPosition(position);
                    musicService.playSong();
                    songTitle.setText(musicService.getSongTitle());
                    artistTv.setText(musicService.getArtist());
                    songNumberTv.setText((musicService.getSongPosition() + 1) + "/" + songListSize);
                    playPause.setImageResource(R.drawable.icon_pause);



                    Cursor cursor = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                            MediaStore.Audio.Albums._ID+ "=?",
                            new String[] {String.valueOf(songList.get(position).getImagePath())},
                            null);

                    if (cursor.moveToFirst()) {
                        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                        Toast.makeText(MainActivity.this,path+"blank",Toast.LENGTH_SHORT).show();
                        // do whatever you need to do
                    }
                }
            }
        });

    }

    ///Initialize EveryThing Here.............................................................
    private void findViewById() {

        //all Button.............
        previous = (ImageButton) findViewById(R.id.previousButton);
        backward = (ImageButton) findViewById(R.id.backwardButton);
        playPause = (ImageButton) findViewById(R.id.playPauseButton);
        forward = (ImageButton) findViewById(R.id.forwardButton);
        next = (ImageButton) findViewById(R.id.nextButton);

        //OnCLick Listener For All Buttons...................
        previous.setOnClickListener(this);
        backward.setOnClickListener(this);
        playPause.setOnClickListener(this);
        forward.setOnClickListener(this);
        next.setOnClickListener(this);

        //All TextView..................
        songNumberTv = (TextView) findViewById(R.id.songNumberTv);
        initTimeInSeekBar = (TextView) findViewById(R.id.itialTimeInSeekBar);
        endingTimeInSeekBar = (TextView) findViewById(R.id.endiTimeInSeekBar);
        songTitle = (TextView) findViewById(R.id.songTitleTv);
        artistTv=(TextView)findViewById(R.id.artistTvAtMainPage);


        //ListView And SeekBar.......................................
        songListView = (ListView) findViewById(R.id.songListView);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                initTimeInSeekBar.setText(AllSong.formatToRealTime(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(seekBar.getProgress());

            }
        });

    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            musicService = musicBinder.getService();
            isBound = true;
            if (musicService.isPlaying()) {
                playPause.setImageResource(R.drawable.icon_pause);
                songNumberTv.setText((musicService.getSongPosition() + 1) + "/" + songListSize);
                songTitle.setText(musicService.getSongTitle());
            }
            //    Toast.makeText(MainActivity.this, "Service connection", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
            // Toast.makeText(MainActivity.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onResume() {
        if (!isBound) {
            bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            isBound = true;
        }
        registerReceiver(myCustomReceiver, new IntentFilter(Constants.HEADSET_REMOVED_ACTION));
        registerReceiver(mySeekReceiver, new IntentFilter(Constants.UPDATE_SEEKBAR_ACTION));
        myCustomReceiverIsRegistered = true;
        mySeekBarReceiverIsRegistered = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isBound) {
            if (musicService.isPlaying()) {
                musicService.startForeground();
            }
            unbindService(serviceConnection);
            isBound = false;
        }
        if (myCustomReceiverIsRegistered) {
            unregisterReceiver(myCustomReceiver);
            myCustomReceiverIsRegistered = false;
        }
        if (mySeekBarReceiverIsRegistered) {
            unregisterReceiver(mySeekReceiver);
            mySeekBarReceiverIsRegistered = false;
        }

        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (!musicService.isPlaying()) {
            stopService(new Intent(this, MusicService.class));
        }
        songList.clear();
        //  Toast.makeText(MainActivity.this, "onDestroy in activity", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


    BroadcastReceiver myCustomReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("songChanged", false)) {
                if (musicService != null && isBound) {
                    songTitle.setText(musicService.getSongTitle());
                    songNumberTv.setText((musicService.getSongPosition() + 1) + "/" + songListSize);
                }
            }
            if (intent.getBooleanExtra("hd", false)) {
                playPause.setImageResource(R.drawable.icon_play);
            }
        }
    };

    BroadcastReceiver mySeekReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateToUi(intent);

        }
    };

    private void updateToUi(Intent intent) {
        String currentDuration = intent.getStringExtra("cp");
        int currentD = Integer.parseInt(currentDuration);
        String totalDuration = intent.getStringExtra("td");
        int totalD = Integer.parseInt(totalDuration);

        seekBar.setMax(totalD);
        endingTimeInSeekBar.setText(AllSong.formatToRealTime(Long.parseLong(totalDuration)));
        seekBar.setProgress(currentD);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.previousButton: {
                if (musicService != null && isBound) {
                    musicService.playPrev();
                    playPause.setImageResource(R.drawable.icon_pause);
                    songTitle.setText(musicService.getSongTitle());
                    songNumberTv.setText((musicService.getSongPosition() + 1) + "/" + songListSize);
                }
                break;
            }
            case R.id.backwardButton: {
                if (musicService != null && isBound) {
                    int position = musicService.getCurrentPosition() - 5000;
                    if (position > 0) {
                        musicService.seekTo(position);
                    }
                }
                break;
            }
            case R.id.playPauseButton: {
                if (musicService != null && isBound) {
                    if (musicService.getSongPosition() == 0) {
                        if (!musicService.isPlaying() && !isPaused) {
                            musicService.playSong();
                            playPause.setImageResource(R.drawable.icon_pause);
                            songNumberTv.setText(1 + "/" + songListSize);
                            songTitle.setText(musicService.getSongTitle());
                        } else if (musicService.isPlaying()) {
                            musicService.pausePlayer();
                            playPause.setImageResource(R.drawable.icon_play);
                            isPaused = true;
                        } else if (isPaused) {
                            musicService.go();
                            playPause.setImageResource(R.drawable.icon_pause);
                        }

                    }
                   else if (musicService.isPlaying()) {
                        playPause.setImageResource(R.drawable.icon_play);
                        musicService.pausePlayer();
                    } else {
                        playPause.setImageResource(R.drawable.icon_pause);
                        musicService.go();
                    }
                }
                break;
            }
            case R.id.forwardButton: {
                if (musicService != null && isBound) {
                    int position = musicService.getCurrentPosition() + 5000;
                    if (position < musicService.getTotalDuration()) {
                        musicService.seekTo(musicService.getCurrentPosition() + 5000);
                    }
                }
                break;
            }
            case R.id.nextButton: {
                if (musicService != null && isBound) {
                    musicService.playNext();
                    playPause.setImageResource(R.drawable.icon_pause);
                    songTitle.setText(musicService.getSongTitle());
                    songNumberTv.setText((musicService.getSongPosition() + 1) + "/" + songListSize);
                }
                break;
            }
        }

    }
}
