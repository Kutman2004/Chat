package com.example.chat.Activity;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chat.R;

public class VideoViewActivity extends AppCompatActivity {

    private VideoView videoView;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        videoView=findViewById(R.id.video_view);

        videoUrl=getIntent().getStringExtra("video");

        videoView.setVideoPath(videoUrl);
        videoView.start();

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);



    }
}

