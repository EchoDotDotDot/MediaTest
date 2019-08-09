package com.echo.videopractice;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

class H264Encoder {

    private int width;
    private int height;
    private int framerate;
    private MediaCodec mMediaCodec;
    private BufferedOutputStream mBufferOutputStream;
    private boolean isRunning;
    public ArrayBlockingQueue<byte[]> yuv420Queue=new ArrayBlockingQueue<>(10);
    private long TIMEOUT_USEC=12000;
    private byte[] configbyte;

    public H264Encoder(int width, int height, int framerate) {
        this.width=width;
        this.height=height;
        this.framerate=framerate;

        MediaFormat mMediaFormat=MediaFormat.createVideoFormat("video/avc",width,height);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,width*height*5);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);

        try {
            mMediaCodec= MediaCodec.createEncoderByType("video/avc");

            mMediaCodec.configure(mMediaFormat,null,null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
            createFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createFile() {
        String path= Environment.getExternalStorageDirectory()+"/encodevideo.mp4";
        File file=new File(path);
        if(file.exists()){
            file.delete();
        }

        try {
            mBufferOutputStream=new BufferedOutputStream(new FileOutputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void startEncoder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isRunning=true;
                byte[] input=null;
                long pts=0;
                long generateindex=0;

                while (isRunning){
                    if(yuv420Queue.size()>0){
                        input=yuv420Queue.poll();
                        byte[] yuv420sp=new byte[width*height*3/2];
                        NV21ToNV12(input,yuv420sp,width,height);
                        input=yuv420sp;
                    }
                    if(input!=null){
                        ByteBuffer[] inputBuffers=mMediaCodec.getInputBuffers();
                        ByteBuffer[] outputBuffers=mMediaCodec.getOutputBuffers();
                        int inputBufferIndex=mMediaCodec.dequeueInputBuffer(-1);
                        if(inputBufferIndex>=0){
                            pts=computePresentationTime(generateindex);
                            ByteBuffer inputBuffer=inputBuffers[inputBufferIndex];
                            inputBuffer.clear();
                            inputBuffer.put(input);
                            mMediaCodec.queueInputBuffer(inputBufferIndex,0,input.length,System.currentTimeMillis(),0);
                            generateindex+=1;
                        }

                        MediaCodec.BufferInfo bufferInfo=new MediaCodec.BufferInfo();
                        int outputBufferIndex=mMediaCodec.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
                        while (outputBufferIndex>=0){
                            ByteBuffer outputBuffer=outputBuffers[outputBufferIndex];
                            byte[] outData=new byte[bufferInfo.size];
                            outputBuffer.get(outData);
                            if(bufferInfo.flags==MediaCodec.BUFFER_FLAG_CODEC_CONFIG){
                                configbyte=new byte[bufferInfo.size];
                                configbyte=outData;
                            }else if(bufferInfo.flags==MediaCodec.BUFFER_FLAG_SYNC_FRAME){
                                byte[] keyframe=new byte[bufferInfo.size+configbyte.length];
                                System.arraycopy(configbyte,0,keyframe,0,configbyte.length);
                                System.arraycopy(outData,0,keyframe,configbyte.length,outData.length);
                                try {
                                    mBufferOutputStream.write(keyframe,0,keyframe.length);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    mBufferOutputStream.write(outData,0,outData.length);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            mMediaCodec.releaseOutputBuffer(outputBufferIndex,false);
                            outputBufferIndex=mMediaCodec.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
                        }
                    }else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mMediaCodec.stop();
                mMediaCodec.release();

                try {
                    mBufferOutputStream.flush();
                    mBufferOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private long computePresentationTime(long generateindex) {
        return 132+framerate*1000000/framerate;
    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if(nv21==null||nv12==null){
            return;
        }

        int frameSize=width*height;
        int i=0,j=0;
        System.arraycopy(nv21,0,nv12,0,frameSize);
        for (i = 0; i < frameSize; i++) {
            nv12[i]=nv12[i];
        }
        for (j = 0; j < frameSize / 2; j++) {
            nv12[frameSize+j-1]=nv21[j+frameSize];
        }

        for (j = 0; j < frameSize/ 2; j++) {
            nv12[frameSize+j]=nv21[j+frameSize-1];
        }

    }

    public void putData(byte[] bytes) {
        if(yuv420Queue.size()>=10){
            yuv420Queue.poll();
        }
        yuv420Queue.add(bytes);
    }

    public void stopEncoder() {
        isRunning=false;
    }
}
