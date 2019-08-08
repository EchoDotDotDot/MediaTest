package com.echo.mediatest;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class WavFileWriter {

    private String mFilePath;
    private int mDataSize=0;
    private DataOutputStream mDataOutputStream;

    public boolean openFile(String path,int sampleRateInHz,int channels,int bitsPerSample) throws IOException {
        if(mDataOutputStream!=null){
            closeFile();
        }

        mFilePath=path;

        try {
            mDataOutputStream=new DataOutputStream(new FileOutputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return writeHeader(sampleRateInHz,bitsPerSample,channels);
    }

    private boolean writeHeader(int sampleRateInHz, int bitsPerSample, int channels)  {
        if(mDataOutputStream==null){
            return false;
        }

        WavFileHeader mWavFileHeader=new WavFileHeader(sampleRateInHz, bitsPerSample,channels);

        try {
            mDataOutputStream.writeBytes(mWavFileHeader.mChunkID);
            mDataOutputStream.write(Util.intToByteArray(mWavFileHeader.mChunkSize),0,4);
            mDataOutputStream.writeBytes(mWavFileHeader.mFormat);

            mDataOutputStream.writeBytes(mWavFileHeader.mSubChunk1ID);
            mDataOutputStream.write(Util.intToByteArray(mWavFileHeader.mSubChunk1Size),0,4);
            mDataOutputStream.write(Util.shortToByteArray(mWavFileHeader.mAudioFormat),0,2);
            mDataOutputStream.write(Util.shortToByteArray(mWavFileHeader.mNumchannel),0,2);
            mDataOutputStream.write(Util.intToByteArray(mWavFileHeader.mSampleRate),0,4);
            mDataOutputStream.write(Util.intToByteArray(mWavFileHeader.mByteRate),0,4);
            mDataOutputStream.write(Util.shortToByteArray(mWavFileHeader.mBlockAlign),0,2);
            mDataOutputStream.write(Util.shortToByteArray(mWavFileHeader.mBitsPerSample),0,2);

            mDataOutputStream.writeBytes(mWavFileHeader.mSubChunk2ID);
            mDataOutputStream.write(Util.intToByteArray(mWavFileHeader.mSubChunk2Size),0,4);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean closeFile() throws IOException {
        boolean ret=true;
        if(mDataOutputStream!=null){
            ret=writeDataSize();
            mDataOutputStream.close();
            mDataOutputStream=null;
        }
        return ret;
    }

    private boolean writeDataSize()  {
        RandomAccessFile wavFile= null;
        try {
            wavFile = new RandomAccessFile(mFilePath,"rw");
            wavFile.seek(WavFileHeader.WAV_CHUNKSIZE_OFFSET);
            wavFile.write(Util.intToByteArray(mDataSize+WavFileHeader.WAV_CHUNKSIZE_EXCLUDE_DATA),0,4);

            wavFile.seek(WavFileHeader.MAV_SUB_CHUNKSIZE2_OFFSET);
            wavFile.write(Util.intToByteArray(mDataSize),0,4);
            wavFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    public boolean writeData(byte[] buffer,int offset,int count){
        if(mDataOutputStream==null){
            return false;
        }

        try {
            mDataOutputStream.write(buffer,offset,count);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mDataSize+=count;
        return true;
    }
}
