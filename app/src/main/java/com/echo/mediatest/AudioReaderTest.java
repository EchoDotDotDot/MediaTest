package com.echo.mediatest;

import android.util.Log;

public class AudioReaderTest implements ITest{

    private final String TAG =this.getClass().getSimpleName() ;
    private WavFileReader mWavFileReader;
    private AudioPlayer mAudioPlayer;
    boolean isLooperExit=false;

    public AudioReaderTest(){
        mWavFileReader=new WavFileReader();
        mAudioPlayer=new AudioPlayer();
    }

    @Override
    public void startTest(String filePath) {
        if(mWavFileReader.openFile(filePath)){
            final WavFileHeader mWavFileHeader=mWavFileReader.getWavFileHeader();
            if(mWavFileHeader==null){
                Log.e(TAG,"mWavFileHeader==null");
                return;
            }
            if(true==mAudioPlayer.startPlayer()){
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (!isLooperExit){
                            byte[] buffer=new byte[7088];
                            if(-1!=mWavFileReader.readData(buffer,0,buffer.length)){
                                mAudioPlayer.play(buffer,0,buffer.length);
                            }
                        }
                        mAudioPlayer.stopPlayer();
                        mWavFileReader.closeFile();
                    }
                }).start();
            }
        }
    }

    @Override
    public void stopTest() {
        isLooperExit=true;
    }
}
