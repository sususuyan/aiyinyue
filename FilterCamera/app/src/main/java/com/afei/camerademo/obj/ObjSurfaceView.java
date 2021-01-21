package com.afei.camerademo.obj;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ObjSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final String TAG = "ObjGLSurfaceView";
    private ObjRender mFilter;
    private Obj3D obj;

    public ObjSurfaceView(Context context) {
        this(context, null);
    }

    public ObjSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFilter = new ObjRender(getResources());
        obj = new Obj3D();
        try {
            ObjReader.read(context.getAssets().open("3dres/hat.obj"),obj);
            mFilter.setObj3D(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFilter.create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mFilter.onSizeChanged(width, height);
        float[] matrix = {
                1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                0,0,0,1
        };
        Matrix.scaleM(matrix, 0, 0.2f,0.2f*width/height, 0.2f);
        mFilter.setMatrix(matrix);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Matrix.rotateM(mFilter.getMatrix(), 0, 0.3f, 0, 1, 0);
        mFilter.draw();
    }
}
