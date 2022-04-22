package com.tencent.myglsurfaceview;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.app.Presentation;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.ViewGroup;

import java.io.IOException;
import java.text.Format;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "xxxxxx";

    private GLSurfaceView glSurface;
    private MyRender render;
    SurfaceTexture mSurfaceTexture;
    Surface surface;
    private MyFrameAvailableListener mFrameAvailableListener;
    static {
        System.loadLibrary("opengles-lesson-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glSurface = findViewById(R.id.glSurface);
        glSurface.setEGLContextClientVersion(3);
        ViewGroup.LayoutParams vp = glSurface.getLayoutParams();
        vp.height = height;
        vp.width = width;
        glSurface.setLayoutParams(vp);
        render = new MyRender();
        glSurface.setRenderer(render);
        createSurfaceTexture();

    }

    private void createSurfaceTexture() {

        mFrameAvailableListener = new MyFrameAvailableListener();
        mSurfaceTexture = new SurfaceTexture(false);
        mSurfaceTexture.setDefaultBufferSize(width, height);
        mSurfaceTexture.setOnFrameAvailableListener(mFrameAvailableListener);

        surface = new Surface(mSurfaceTexture);

        Log.d(TAG, "createSurfaceTexture: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        surface.release();
    }
    int height = 1920;
    int width = 1080;
    public class MyRender implements GLSurfaceView.Renderer {
        int textureId = 0;
        Square square;
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(TAG, "onSurfaceCreated: "+ Thread.currentThread());
            // create SurfaceTexture
            glSurface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            int[] textures = new int[1];
            GLES30.glGenTextures(1, textures, 0);
            textureId = textures[0];
            mSurfaceTexture.attachToGLContext(textureId);
            mSurfaceTexture.setDefaultBufferSize(width, height);

            nativeSurfaceCreate();
            nativeBindTexture(textureId);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
//                    getResources().getDisplayMetrics().densityDpi
                    VirtualDisplay vd = dm.createVirtualDisplay("123", width, height, getResources().getDisplayMetrics().densityDpi, surface, 0);

                    MyPresentation presentation = new MyPresentation(getApplicationContext(), vd.getDisplay());
                    presentation.show();
                }
            });

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged: "+ Thread.currentThread());
            GLES30.glViewport(0,0,width,height);
            nativeSurfaceChange(width, height);

        }

        @Override
        public void onDrawFrame(GL10 gl) {

            if (atomicBoolean.get()) {
                mSurfaceTexture.updateTexImage();
                float[] matrix= new float[16];
                mSurfaceTexture.getTransformMatrix(matrix);
                // 绘制
                atomicBoolean.set(false);
            }
            nativeDrwaFrame();
        }
    }

    public native void nativeSurfaceCreate();
    public native void nativeBindTexture(int id);
    public native void nativeSurfaceChange(int width, int height);
    public native void nativeDrwaFrame();

    @Override
    protected void onResume() {
        super.onResume();
        glSurface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurface.onPause();
    }

    public class MyFrameAvailableListener implements SurfaceTexture.OnFrameAvailableListener {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onFrameAvailable: "+ Thread.currentThread());
            atomicBoolean.set(true);
        }
    }

    AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    public class MyPresentation extends Presentation {

        public MyPresentation(Context outerContext, Display display) {
            super(outerContext, display);
        }

        public MyPresentation(Context outerContext, Display display, int theme) {
            super(outerContext, display, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.virtual_display);



        }

        @Override
        protected void onStart() {
            super.onStart();
            anim1();
//            anim2();
        }


        private void anim2() {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.5f, 1);
            valueAnimator.setDuration(1000);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    findViewById(R.id.view).setScaleX(v);
                    findViewById(R.id.view).setScaleY(v);

                }
            });
        }

        private void anim1() {
            ValueAnimator valueAnimator = ValueAnimator.ofArgb(Color.BLUE, Color.YELLOW);
            valueAnimator.setDuration(1000);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.start();
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int v = (int) animation.getAnimatedValue();
                    findViewById(R.id.view).setBackgroundColor(v);

                }
            });
        }
    }
}
