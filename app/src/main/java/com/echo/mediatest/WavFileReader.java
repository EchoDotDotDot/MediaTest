package com.echo.mediatest;

import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class WavFileReader {
    private String TAG=this.getClass().getSimpleName();

    private WavFileHeader mWavFileHeader;
    private DataInputStream mDataInputStream;

    public boolean openFile(String filepath){
        if(mDataInputStream!=null){
            closeFile();
        }

        try {
            mDataInputStream=new DataInputStream(new FileInputStream(filepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return readHeader();
    }

    private boolean readHeader() {
        byte[] headbuffer=new byte[WavFileHeader.WAV_CHUNKSIZE_DATA];

        try {
            mDataInputStream.read(headbuffer,0,WavFileHeader.WAV_CHUNKSIZE_DATA);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(headbuffer.length!=WavFileHeader.WAV_CHUNKSIZE_DATA){
            Log.e(TAG,"headbuffer error");
            return false;
        }

        mWavFileHeader=new WavFileHeader();
        mWavFileHeader.mAudioFormat=Util.byteArrayToShort(Util.byteSplit(headbuffer,WavFileHeader.MAV_AUDIO_FORMAT_OFFSET_SHORT,2));
        Log.e(TAG,"mWavFileHeader.mAudioFormat:"+mWavFileHeader.mAudioFormat);
        mWavFileHeader.mNumchannel=Util.byteArrayToShort(Util.byteSplit(headbuffer,WavFileHeader.MAV_NUM_CHANNELS_OFFSET_SHORT,2));
        Log.e(TAG,"mWavFileHeader.mNumchannel:"+mWavFileHeader.mNumchannel);
        mWavFileHeader.mSampleRate=Util.byteArrayToInt(Util.byteSplit(headbuffer,WavFileHeader.MAV_SAMPLE_RATE_OFFSET_INT,4));
        Log.e(TAG,"mWavFileHeader.mSampleRate:"+mWavFileHeader.mSampleRate);
        mWavFileHeader.mByteRate=Util.byteArrayToInt(Util.byteSplit(headbuffer,WavFileHeader.MAV_BYTE_RATE_OFFSET_INT,4));
        Log.e(TAG," mWavFileHeader.mByteRate:"+ mWavFileHeader.mByteRate);
        mWavFileHeader.mBlockAlign=Util.byteArrayToShort(Util.byteSplit(headbuffer,WavFileHeader.MAV_BLOCK_ALIGN_OFFSET_SHORT,2));
        Log.e(TAG,"mWavFileHeader.mBlockAlign:"+mWavFileHeader.mBlockAlign);
        mWavFileHeader.mBitsPerSample=Util.byteArrayToShort(Util.byteSplit(headbuffer,WavFileHeader.MAV_BITS_PER_SAMPLE_OFFSET_SHORT,2));
        Log.e(TAG,"mWavFileHeader.mBitsPerSample:"+mWavFileHeader.mBitsPerSample);
        mWavFileHeader.mSubChunk2Size=Util.byteArrayToInt(Util.byteSplit(headbuffer,WavFileHeader.MAV_SUB_CHUNKSIZE2_OFFSET,4));
        return true;
    }

    public void closeFile(){
        if(mDataInputStream==null)
            return;
        try {
            mDataInputStream.close();
            mDataInputStream=null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public WavFileHeader getWavFileHeader(){
        return mWavFileHeader;
    }

    public int readData(byte[] buffer,int offset,int count){
        if(mDataInputStream==null||mWavFileHeader==null){
            return -1;
        }

        try {
            mDataInputStream.read(buffer,offset,count);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }


}
