package com.echo.videopractice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Policy;

public class Main2Activity extends AppCompatActivity {

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final String TAG = getClass().getSimpleName();
    SurfaceView mSurfaceView;
    Camera mCamera;
    TextureView mTextureView;
    H264Encoder mEncoder;
    private MediaExtractor mMediaExtractor;
    private MediaMuxer mMediaMuxer;

    private int width=1280;
    private int height=720;
    private int framerate=30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mCamera=Camera.open();
//        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters=mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(1280,720);
        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                if(mEncoder!=null){
                    mEncoder.putData(bytes);
                }
            }
        });
        mSurfaceView= (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if(surfaceHolder==null){
                    return;
                }

//                Canvas mCanvas=surfaceHolder.lockCanvas();
//                Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.timg);
//                Paint mPaint=new Paint();
//                mPaint.setAntiAlias(true);
//                mPaint.setStyle(Paint.Style.STROKE);
//                mCanvas.drawBitmap(bitmap,0,0,mPaint);
//                surfaceHolder.unlockCanvasAndPost(mCanvas);


                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.startPreview();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(supportH264Codec()){
                    mEncoder=new H264Encoder(width,height,framerate);
                    mEncoder.startEncoder();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                if(mEncoder!=null){
                    mEncoder.stopEncoder();
                }
//                mCamera.release();
            }
        });


//        mTextureView=(TextureView)(findViewById(R.id.textureView));
//        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
//                try {
//                    mCamera.setPreviewTexture(surfaceTexture);
//                    mCamera.startPreview();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
//
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
////                mCamera.release();
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//
//            }
//        });


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                loop();
//            }
//
//
//        }).start();
    }

    @Override
    protected void onDestroy() {
//        mCamera.release();
        super.onDestroy();
    }

    private void loop() {
        mMediaExtractor=new MediaExtractor();
        int framerate=0;
        try {
            mMediaExtractor.setDataSource(SDCARD_PATH+"/test.mp4");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"cannot find"+SDCARD_PATH+"/test.mp4");
        }
        
        int mVideoTrackIndex=-1;

        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            MediaFormat format=mMediaExtractor.getTrackFormat(i);
            String mime=format.getString(MediaFormat.KEY_MIME);
            if(!mime.startsWith("video/")){
                continue;
            }
            framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            mMediaExtractor.selectTrack(i);
            try {
                mMediaMuxer=new MediaMuxer(SDCARD_PATH+"/output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"init mMediaMuxer failed");
            }
            mVideoTrackIndex=mMediaMuxer.addTrack(format);
            mMediaMuxer.start();
        }

        if(mMediaMuxer==null){
            return;
        }

        MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();
        info.presentationTimeUs=0;
        ByteBuffer buffer=ByteBuffer.allocate(500*1024);
        int sampleSize=0;
        while ((sampleSize=mMediaExtractor.readSampleData(buffer,0))>0){
            info.offset=0;
            info.size=sampleSize;
            info.flags=MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            info.presentationTimeUs+=1000*1000/framerate;
            mMediaMuxer.writeSampleData(mVideoTrackIndex,buffer,info);
            mMediaExtractor.advance();
        }

        mMediaExtractor.release();

        mMediaMuxer.stop();
        mMediaMuxer.release();
    }

    private boolean supportH264Codec(){
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo codecInfo=MediaCodecList.getCodecInfoAt(i);
            String[] supportedTypes = codecInfo.getSupportedTypes();
            for (int i1 = 0; i1 < supportedTypes.length; i1++) {
                if(supportedTypes[i1].equalsIgnoreCase("video/avc")){
                    return true;
                }
            }
        }
        return false;
    }
}
