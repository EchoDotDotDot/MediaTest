package com.echo.mediatest;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioPlayer {
    private String TAG="AUDIOPLAYER";
    private AudioTrack mAudioTrack;
    private int DEFAULT_STREAM_TYPE= AudioManager.STREAM_MUSIC;
    private int DEFAULT_SAMPLE_RATE=44100;
    private int DEFAULT_CHANNEL_CONFIG= AudioFormat.CHANNEL_IN_STEREO;
    private int DEFAULT_AUDIO_FORMATE=AudioFormat.ENCODING_PCM_16BIT;
    private int DEFAULT_PLAY_MODE=AudioTrack.MODE_STREAM;
    private int mMinBufferSize;
    private boolean mIstartPlayed;

    public boolean startPlayer(){
        return startPlayer(DEFAULT_STREAM_TYPE,DEFAULT_SAMPLE_RATE,DEFAULT_CHANNEL_CONFIG,DEFAULT_AUDIO_FORMATE);
    }

    public boolean startPlayer(int streamType,int sampleRate,int channelConfig,int audioFormat){
        if(mIstartPlayed){
            return false;
        }

        mMinBufferSize=AudioTrack.getMinBufferSize(sampleRate,channelConfig,audioFormat);
        Log.e(TAG,"mMinBufferSize:"+mMinBufferSize);
        if(mMinBufferSize==AudioTrack.ERROR_BAD_VALUE){
            Log.e(TAG,"mMinBufferSize ERROR_BAD_VALUE");
            return false;
        }

        mAudioTrack=new AudioTrack(streamType,sampleRate,channelConfig,audioFormat,mMinBufferSize,DEFAULT_PLAY_MODE);

        if(mAudioTrack.getState()==AudioTrack.STATE_UNINITIALIZED){
            Log.e(TAG,"mAudioTrack STATE_UNINITIALIZED");
            return false;
        }

        mIstartPlayed=true;
        Log.e(TAG,"mAudioTrack STATE_INITIALIZED");
        return true;
    }

    public void stopPlayer(){
        if(!mIstartPlayed){
            return;
        }

        if(mAudioTrack.getState()==AudioTrack.PLAYSTATE_PLAYING){
            mAudioTrack.stop();
        }

        mAudioTrack.release();
        mIstartPlayed=false;
        Log.e(TAG,"STOP SUCCESS");
    }

    public boolean play(byte[] audioData,int offset,int bufferSize){
        if(mIstartPlayed!=true){
            return false;
        }

        if(bufferSize<mMinBufferSize){
            Log.e(TAG,"INPUT DATA IS NOT ENOUGH");
            return false;
        }

        if(mAudioTrack.write(audioData,offset,bufferSize)!=bufferSize){
            Log.e(TAG,"CANNOT WRITE ALL DATA");
            return false;
        }

        mAudioTrack.play();
        Log.d(TAG , "OK, Played "+bufferSize+" bytes !");
        return true;
    }
}
