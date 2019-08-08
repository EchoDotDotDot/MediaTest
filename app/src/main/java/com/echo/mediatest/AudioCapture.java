package com.echo.mediatest;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * 使用AudioRecord采集音频
 * */
public class AudioCapture {
    private String TAG="AUDIOCAPTUURE";
    private boolean mIsStarted;

    private AudioRecord mAudioRecord;
    private int DEFAULT_SROUCE= MediaRecorder.AudioSource.MIC;
    private int DEFAULT_SAMPLE_RATE=44100;
    private int DEFAULT_CHANNEL_CONFIG= AudioFormat.CHANNEL_IN_STEREO;
    private int DEFAULT_AUDIO_FORMAT=AudioFormat.ENCODING_PCM_16BIT;

    private int mMinBufferSize=0;

    private boolean mIsLoopExit=false;
    private Thread mThread;

    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener onAudioFrameCapturedListener) {
        mOnAudioFrameCapturedListener = onAudioFrameCapturedListener;
    }

    private OnAudioFrameCapturedListener mOnAudioFrameCapturedListener;

    public interface OnAudioFrameCapturedListener{
        public void onAudioFrameCaptured(byte[] audioData);
    }

    public boolean isCaptrueStarted(){
        return mIsStarted;
    }


    public boolean startCapture(){
        return startCapture(DEFAULT_SROUCE,DEFAULT_SAMPLE_RATE,DEFAULT_CHANNEL_CONFIG,DEFAULT_AUDIO_FORMAT);
    }

    public boolean startCapture(int audrioSource,int sampleRate,int channelConfig,int audioFormat){
        if(isCaptrueStarted()==true){
            return false;
        }else {
            mMinBufferSize=AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
            Log.e(TAG,"mMinBufferSize:"+mMinBufferSize);
            mAudioRecord=new AudioRecord(audrioSource,sampleRate,channelConfig,audioFormat,mMinBufferSize);
            if( mAudioRecord.getState()==AudioRecord.STATE_UNINITIALIZED){
                Log.e(TAG,"mAudioRecord Init Failed!");
                return false;
            }
            mAudioRecord.startRecording();
            Log.e(TAG,"mAudioRecord startRecording");
            mIsLoopExit=false;
            mThread=new Thread(new AudioCaptureRunnable());
            mThread.start();
            mIsStarted=true;
            return true;
        }
    }

    private class AudioCaptureRunnable implements Runnable{

        @Override
        public void run() {
            while (!mIsLoopExit){
                byte[] buffer=new byte[mMinBufferSize];

                int ret=mAudioRecord.read(buffer,0,mMinBufferSize);
                if(ret==AudioRecord.ERROR_INVALID_OPERATION){
                    Log.e(TAG,"ERROR_INVALID_OPERATION");
                }else if(ret==AudioRecord.ERROR_BAD_VALUE){
                    Log.e(TAG,"ERROR_BAD_VALUE");
                }else {
                    if(mOnAudioFrameCapturedListener!=null){
                        mOnAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                        Log.e(TAG,"onAudioFrameCaptured");
                    }
                }
            }
        }
    }

    public void stopCapture(){
        if(isCaptrueStarted()==false){
            return;
        }

        mIsLoopExit=true;
        try{
            mThread.interrupt();
            mThread.join(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        if(mAudioRecord.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING){
            mAudioRecord.stop();
        }
        mAudioRecord.release();
        mIsStarted=false;
        mOnAudioFrameCapturedListener=null;
        Log.e(TAG,"stop sucess");
    }
}
