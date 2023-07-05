package com.example.mymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymusic.adapter.MysongListAdapter;
import com.example.mymusic.data.GlobalConstants;
import com.example.mymusic.data.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRCVSongList;
    private MysongListAdapter mSongListAdapter;
    private ArrayList<Song> mSongArrayList;
    private LinearLayout linearLayout;
    private String[] songnames;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intiView();
        initData();
        initSongList();
    }
    private void initData() {
        mSongArrayList = new ArrayList<>();
        AssetManager assetManager = getAssets();
        try {
            songnames = assetManager.list("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < songnames.length; i++){
            if (songnames[i].endsWith(".mp3")){
                mSongArrayList.add(new Song(songnames[i]));
            }
        }
    }

    private void initSongList() {
        mSongListAdapter = new MysongListAdapter(mSongArrayList,this);
        mSongListAdapter.setmItemClickListener(new MysongListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this,"点击了"+position,Toast.LENGTH_SHORT);

                Intent intent = new Intent(MainActivity.this,MusicPlayActivity.class);
                intent.putExtra(GlobalConstants.KEY_SONG_LIST,mSongArrayList);
                intent.putExtra(GlobalConstants.KEY_SONG_INDEX,position);
                intent.putParcelableArrayListExtra(GlobalConstants.KEY_SONG_LIST,mSongArrayList);
                startActivity(intent);
                name.setText(mSongArrayList.get(position).getSongName());
            }
        });
        mRCVSongList.setAdapter(mSongListAdapter);
        mRCVSongList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void intiView() {
        mRCVSongList = findViewById(R.id.rcv_song_list);
        linearLayout = findViewById(R.id.lv_bottom_ui);
        name = findViewById(R.id.tv_bottom_song_name);
    }
}