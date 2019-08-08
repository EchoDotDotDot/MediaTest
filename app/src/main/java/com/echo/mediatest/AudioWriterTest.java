package com.echo.mediatest;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import java.io.IOException;

public class AudioWriterTest implements ITest{
    private WavFileWriter mWavFileWriter;
    private AudioCapture mAudioCapture;

    public AudioWriterTest(){
        mWavFileWriter=new WavFileWriter();
        mAudioCapture=new AudioCapture();
    }

    @Override
    public void startTest(String filePath) {
        try {
            if(mWavFileWriter.openFile(filePath,44100,2,16)){
                mAudioCapture.setOnAudioFrameCapturedListener(new AudioCapture.OnAudioFrameCapturedListener() {
                    @Override
                    public void onAudioFrameCaptured(byte[] audioData) {
                        mWavFileWriter.writeData(audioData,0,audioData.length);
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mAudioCapture.startCapture(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
    }

    @Override
    public void stopTest() {
        mAudioCapture.stopCapture();
        try {
            mWavFileWriter.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
