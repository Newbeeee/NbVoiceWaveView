package com.newbeeee.qt.nbvoicewaveview;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private VoiceWaveView mVoiceWaveView;
    private RelativeLayout mLongClickView;
    private AudioRecord mRecorder;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(8000,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    private Object mLock;
    private boolean isRecording = true;
    private double waveVolume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVoiceWaveView = (VoiceWaveView) findViewById(R.id.voice_wave_view);
        mLongClickView = (RelativeLayout) findViewById(R.id.mLongClickView);
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                8000, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        mLock = new Object();
        setClick();
    }

    private void setClick() {
        //长按整个屏幕开始录音
        mLongClickView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startWave();
                return true;
            }
        });

        //松开结束录音
        mLongClickView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (mRecorder != null) {
                            isRecording = false;
                            mRecorder.stop();
                            mRecorder.release();
                            mRecorder = null;
                            mVoiceWaveView.reset();
                        }
                }
                return false;
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mVoiceWaveView.setVolume(waveVolume);
                    break;
                default:
                    break;
            }
        }
    };


    private void startWave() {
        isRecording = true;
        if (mRecorder == null) {
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    8000, AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRecorder.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isRecording) {
                    //r是实际读取的数据长度，一般而言r会小于 buffer size
                    int r = mRecorder.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
                    Log.e("分贝值：", String.valueOf(volume));
                    waveVolume = volume;
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                    // 大概一秒十次
                    synchronized (mLock) {
                        try {
                            mLock.wait(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
    }
}
