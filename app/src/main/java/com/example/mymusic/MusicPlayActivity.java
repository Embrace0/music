package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusic.data.GlobalConstants;
import com.example.mymusic.data.Song;
import com.example.mymusic.service.MyMusicService;
import com.example.mymusic.util.TimeUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayActivity extends AppCompatActivity {
    private ImageView ivPlayOrPause,ivPre,ivNext;
    private TextView tvTitle,tvCurTime,tvDuration;
    private SeekBar mSeekBar;
    private ArrayList<Song> mSongArrayList;
    private int curSongIndex;
    private MyMusicService.MyMusicBind mMusicBind;
    private Song mCurSong;
    private boolean isSeekbarDragging;
    private Timer timer;
    private static String name;
    private View view;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            //传递信息
            mMusicBind = ((MyMusicService.MyMusicBind) iBinder);
            mMusicBind.updateMusicList(mSongArrayList);
            mMusicBind.updateCurrentMusicIndex(curSongIndex);
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private void updateUI(){
        int curProgress = mMusicBind.getCurProgress();
        tvCurTime.setText(TimeUtil.millToTimeFormat(curProgress));

        int duration = mMusicBind.getDuration();
        tvDuration.setText(TimeUtil.millToTimeFormat(duration));

        mSeekBar.setMax(duration);
        mSeekBar.setProgress(curProgress);

        if (timer != null){
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int curProgress = mMusicBind.getCurProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isSeekbarDragging && mMusicBind.isPlaying()){
                            mSeekBar.setProgress(curProgress);

                            tvCurTime.setText(TimeUtil.millToTimeFormat(curProgress));
                            if (mSeekBar.getProgress() >= mSeekBar.getMax()){
                                nextMusic(view);
                            }
                        }
                    }
                });
            }
        },0,200);
    }
    private void updateCurTimeText(int progress){
        tvCurTime.setText(TimeUtil.millToTimeFormat(progress));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        initView();
        Intent intent = getIntent();
        curSongIndex = intent.getIntExtra(GlobalConstants.KEY_SONG_INDEX,0);
//        mSongArrayList = (ArrayList<Song>) intent.getSerializableExtra(GlobalConstants.KEY_SONG_LIST);
        mSongArrayList = intent.getParcelableArrayListExtra(GlobalConstants.KEY_SONG_LIST);
        mCurSong = mSongArrayList.get(curSongIndex);
        Log.d("tag","当前歌曲:" + curSongIndex);
        if (mSongArrayList != null){
            Log.d("tag","当前歌曲列表:" + mSongArrayList);
        }
        initTitle();
        startMusicService();
    }

    private void initTitle() {
        tvTitle.setText(mCurSong.getSongName());
        name = mCurSong.getSongName();
    }

    private void initView() {
        ivPlayOrPause = findViewById(R.id.iv_play_pause);
        ivPre = findViewById(R.id.iv_previous);
        ivNext = findViewById(R.id.iv_next);
        tvTitle = findViewById(R.id.tv_music_title);
        tvCurTime = findViewById(R.id.tv_cur_time);
        tvDuration = findViewById(R.id.tv_total_time);
        mSeekBar = findViewById(R.id.seek_bar_music);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                updateCurTimeText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarDragging = false;
                int progress = seekBar.getProgress();
                mMusicBind.seekTo(progress);
            }
        });

    }

    private void startMusicService() {
        //bind 启动service
        Intent intent = new Intent(this, MyMusicService.class);
        bindService(intent,conn,BIND_AUTO_CREATE);
    }

    public void playOrPause(View view) {
        if (mMusicBind.isPlaying()){
            //暂停
            mMusicBind.pause();
            Toast.makeText(this,"暂停",Toast.LENGTH_SHORT).show();
            //修改图标
            ivPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
        }else{
            //播放
            mMusicBind.play();
            Toast.makeText(this,"播放",Toast.LENGTH_SHORT).show();
            ivPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    public void preMusic(View view) {
        if (!mMusicBind.isPlaying()){
            //修改图标
            ivPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
        }
        Toast.makeText(this,"上一首",Toast.LENGTH_SHORT).show();
        mMusicBind.previous();
        mCurSong = mMusicBind.getCurSong();
        initTitle();
        updateUI();
    }

    public void nextMusic(View view) {
        if (!mMusicBind.isPlaying()){
            ivPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
        }
        Toast.makeText(this,"下一首",Toast.LENGTH_SHORT).show();
        mMusicBind.next();
        mCurSong = mMusicBind.getCurSong();
        initTitle();
        updateUI();
    }

    public void stopMusic(View view) {
        mMusicBind.stop();
        Toast.makeText(this,"停止播放",Toast.LENGTH_SHORT).show();
        ivPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
        updateCurTimeText(0);
        mSeekBar.setProgress(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }
}