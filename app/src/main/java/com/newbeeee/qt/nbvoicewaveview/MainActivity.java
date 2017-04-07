package com.newbeeee.qt.nbvoicewaveview;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.czt.mp3recorder.MP3Recorder;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private VoiceWaveView mVoiceWaveView;
    private Timer timer;
    private MP3Recorder mp3Recorder;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVoiceWaveView = (VoiceWaveView) findViewById(R.id.voice_wave_view);

        file = new File(initFilePath());
        mp3Recorder = new MP3Recorder(file);
        setClick();
    }

    private void setClick() {
        mVoiceWaveView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mp3Recorder != null) {
                    try {
                        mp3Recorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = 1;
                        handler.sendMessage(message);
                    }
                }, 0, 100);
                return false;
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (mp3Recorder != null){
                    double ratio = (double)mp3Recorder.getVolume()/60;
                    double db = 0;
                    if (ratio > 1)
                        db = 20 * Math.log10(ratio);
                    mVoiceWaveView.setVolume((int) (db));
                }
            }
        }
    };

    private String initFilePath() {
        String path = Environment.getExternalStorageDirectory() + "/" + "VoiceWaveView";
        File filePath = new File(path);
        if(!filePath.exists()){
            filePath.mkdirs();
        }
        return filePath + "/voiceTest.3gp";
    }
}
