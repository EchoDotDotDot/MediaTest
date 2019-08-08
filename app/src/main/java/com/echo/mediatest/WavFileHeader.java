package com.echo.mediatest;

public class WavFileHeader {

    public static final int WAV_CHUNKSIZE_OFFSET = 4;
    public static final int WAV_CHUNKSIZE_EXCLUDE_DATA=36;
    public static final int WAV_CHUNKSIZE_DATA=44;

    public static final int MAV_SUB_CHUNKSIZE2_OFFSET = 40;
    public static final int MAV_AUDIO_FORMAT_OFFSET_SHORT=20;
    public static final int MAV_NUM_CHANNELS_OFFSET_SHORT=22;
    public static final int MAV_SAMPLE_RATE_OFFSET_INT=24;
    public static final int MAV_BYTE_RATE_OFFSET_INT=28;
    public static final int MAV_BLOCK_ALIGN_OFFSET_SHORT=32;
    public static final int MAV_BITS_PER_SAMPLE_OFFSET_SHORT=34;


    public String mChunkID="RIFF";
    public int mChunkSize=0;
    public String mFormat="WAVE";

    public String mSubChunk1ID="fmt ";
    public int mSubChunk1Size=16;
    public short mAudioFormat=1;
    public short mNumchannel=1;
    public int mSampleRate=8000;
    public int mByteRate=0;
    public short mBlockAlign=0;
    public short mBitsPerSample=8;

    public String mSubChunk2ID="data";
    public int mSubChunk2Size=0;

    public WavFileHeader(){}

    public WavFileHeader(int sampleRateInHz,int bitsPerSample,int channels){
        mSampleRate=sampleRateInHz;
        mBitsPerSample=(short) bitsPerSample;
        mNumchannel=(short)channels;
        mByteRate=mSampleRate*mNumchannel*mBitsPerSample/8;
        mBlockAlign=(short)(mNumchannel*mBitsPerSample/8);
    }


}
