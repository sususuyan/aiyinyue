package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.zeusee.main.hyperlandmark.jni.Face;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

public class StickerObj {
    private Obj3D obj;
    private List<Face> faces;

    private String vertexShaderCode;
    private String fragmentShaderCode;
    private int programId;
    private int mHPosition;
    private int mHNormal;
    private int mHMatrix;
    private int mHCoord;
    private int mHTexture;
    private Context context;
    private float[] matrix;
    private int width;
    private int height;
    private int fboTextureId;
    private int frameBufferId;
    private int renderBufferId;
    private FloatBuffer mVerBuffer;
    private FloatBuffer mTexBuffer;
    protected ShortBuffer mindexBuffer;
    private int textureType=0;
    private int textureId=0;
    private static final float PROJECTION_SCALE = 2.0f;
    private float[] mVertices = new float[87264];
    private float Hat_Width = 8.5f;
    private float Hat_Height = 4.1f;
    private float Hat_Depth = 9.4f;
    private float Hat_Scale = 0.211538f;

    private float Hat_Offsetx = 0.0f;
    private float Hat_Offsety = -0.412507f;
    private float Hat_Offsetz = 0.0f;
    private int centerIndex = 21;
    //calculate the width of face
    private int startIndex = 7;
    private int endIndex = 17;

    //顶点坐标
    private float pos[] = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f,  -1.0f,
    };

    //纹理坐标
    private float[] coord={
            0.0f, 0.0f,
            0.0f,  1.0f,
            1.0f,  0.0f,
            1.0f, 1.0f,
    };

    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mMVPMatrix=new float[16];
    private float[] mModelMatrix = new float[16];
    private boolean rotate270;
    public StickerObj(Context context){
        this.context = context;
        initBuffer();
        initMatrix();
    }

    public void initBuffer(){
        ByteBuffer a=ByteBuffer.allocateDirect(32);
        a.order(ByteOrder.nativeOrder());
        mVerBuffer=a.asFloatBuffer();
        mVerBuffer.put(pos);
        mVerBuffer.position(0);
        ByteBuffer b=ByteBuffer.allocateDirect(32);
        b.order(ByteOrder.nativeOrder());
        mTexBuffer=b.asFloatBuffer();
        mTexBuffer.put(coord);
        mTexBuffer.position(0);

    }

    private void initMatrix(){
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    public void onCreate(){
        vertexShaderCode = ShaderUtils.getShaderFromAssets(context, "shader/vertex_obj.glsl");
        fragmentShaderCode = ShaderUtils.getShaderFromAssets(context, "shader/fragment_obj.glsl");

        programId = ShaderUtils.createProgram(vertexShaderCode, fragmentShaderCode);
        mHPosition = GLES20.glGetAttribLocation(programId, "vPosition");
        mHNormal = GLES20.glGetAttribLocation(programId, "vNormal");
        mHMatrix=GLES20.glGetUniformLocation(programId,"vMatrix");
        mHCoord=GLES20.glGetAttribLocation(programId,"vCoord");
        mHTexture=GLES20.glGetUniformLocation(programId,"vTexture");
        if(obj.vertTexture!=null){
            try {
                textureId=createTexture(BitmapFactory.decodeStream(context.getAssets().open("3DModel/"+obj.mtl.map_Kd)));
                setTextureId(textureId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initFrame(){
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        fboTextureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        int[] renderBuffers = new int[1];
        GLES20.glGenRenderbuffers(1, renderBuffers, 0);
        renderBufferId = renderBuffers[0];
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBufferId);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,width, height);

        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);
        frameBufferId = frameBuffers[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureId, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBufferId);
        if(GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.i("fbo", "Framebuffer error");
        }
    }

    public int  drawFrame(){
        if(faces == null){
            return 0;
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        float[] points;
        for(int i=0;i<faces.size();i++){
            Face face = faces.get(i);
            points = new float[106*2];
            for(int j =0;j<106;j++){
                int x;
                if(rotate270){
                    x = face.landmarks[j*2]*CameraProxy.SCALLE_FACTOR;
                }
                else {
                    x = CameraProxy.mPreviewHeight - face.landmarks[j*2];
                }
                int y = face.landmarks[j*2+1] * CameraProxy.SCALLE_FACTOR;
                points[j * 2] = view2openglX(x, CameraProxy.mPreviewHeight);
                points[j * 2 + 1] = view2openglY(y, CameraProxy.mPreviewWidth);
            }
            calculateVertex(face, points);
            draw();
        }
        return fboTextureId;
    }

    public void draw(){

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glUseProgram(programId);
        GLES20.glEnableVertexAttribArray(mHPosition);
        GLES20.glVertexAttribPointer(mHPosition,3, GLES20.GL_FLOAT, false, 3*4,obj.vert);
        GLES20.glEnableVertexAttribArray(mHNormal);
        GLES20.glVertexAttribPointer(mHNormal,3, GLES20.GL_FLOAT, false, 3*4,obj.vertNorl);
        GLES20.glUniformMatrix4fv(mHMatrix,1,false, mMVPMatrix,0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,getTextureId());
        GLES20.glUniform1i(mHTexture,textureType);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,obj.vertCount);
        GLES20.glDisableVertexAttribArray(mHPosition);
        GLES20.glDisableVertexAttribArray(mHNormal);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }

    public void calculateVertex(Face face, float[] points){
        float centerX = points[centerIndex*2];
        float centerY = points[centerIndex*2+1];
        float centerZ = 0.0f;
//        Log.e("debug", "centerX: "+centerX+"\tcenterY: "+centerY+"\tcenterZ: "+centerZ);
        float stickerWidth = (float) getDistance(
                points[startIndex*2]*0.5f,
                points[startIndex*2+1]*0.5f,
                points[endIndex*2]*0.5f,
                points[endIndex*2+1]*0.5f
        );
        float ndcStickerWidth = stickerWidth;
        float stickerHeight = stickerWidth;
        for(int i=0;i<obj.vertCount;i++){
            mVertices[i*3]=obj.tempVert.get(i*3)* ndcStickerWidth/Hat_Width/Hat_Scale;
            mVertices[i*3+1]=obj.tempVert.get(i*3+1)* ndcStickerWidth/Hat_Width/Hat_Scale;
            mVertices[i*3+2]=obj.tempVert.get(i*3+2)* ndcStickerWidth/Hat_Width/Hat_Scale;
        }
        setPoints(mVertices);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, centerX, centerY, 0);

        float pitchAngle = face.pitch;
        float yawAngle = -face.yaw;
        float rollAngle = -face.roll;
        if(Math.abs(yawAngle)>90){
            yawAngle = (yawAngle/Math.abs(yawAngle))*90;
        }
        if(Math.abs(pitchAngle)>90){
            pitchAngle = (pitchAngle / Math.abs(pitchAngle)) * 90;
        }

        Log.e("debug:", "rollAngle: "+rollAngle+"\tyawAngle: "+yawAngle+"\tpitchAngle: "+pitchAngle);
        Matrix.rotateM(mModelMatrix, 0, rollAngle, 0, 0, 1);//进行旋转
        Matrix.rotateM(mModelMatrix, 0, yawAngle, 0, 1, 0);
        Matrix.rotateM(mModelMatrix, 0, pitchAngle, 1, 0, 0);

        Matrix.translateM(mModelMatrix, 0, -centerX, -centerY, 0);

        Matrix.translateM(mModelMatrix, 0, centerX, centerY-Hat_Offsety, -Hat_Depth*ndcStickerWidth/Hat_Width/Hat_Scale/2);

        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mModelMatrix, 0);
//
//        Log.e("debug", "obj.tempVert.size: "+obj.tempVert.size());
//
//        Matrix.setIdentityM(mModelMatrix, 0);
//        Matrix.translateM(mModelMatrix, 0, centerX, centerY, 0);
//
//        float pitchAngle = face.pitch;
//        float yawAngle = -face.yaw;
//        float rollAngle = -face.roll;
//        if(Math.abs(yawAngle)>90){
//            yawAngle = (yawAngle/Math.abs(yawAngle))*90;
//        }
//        if(Math.abs(pitchAngle)>90){
//            pitchAngle = (pitchAngle / Math.abs(pitchAngle)) * 90;
//        }
//
////        Log.e("debug:", "rollAngle: "+rollAngle+"\tyawAngle: "+yawAngle+"\tpitchAngle: "+pitchAngle);
//        Matrix.rotateM(mModelMatrix, 0, rollAngle, 0, 0, 1);//进行旋转
//        Matrix.rotateM(mModelMatrix, 0, yawAngle, 0, 1, 0);
//        Matrix.rotateM(mModelMatrix, 0, pitchAngle, 1, 0, 0);
//
//        Matrix.translateM(mModelMatrix, 0, -centerX, -centerY, 0);
//        float scale = (stickerWidth*7)/8.5355192f;
//        Matrix.scaleM(mModelMatrix, 0, scale,scale*width/height, scale);// 进行缩放
//
//        float offsetX = 0f;
//        float offsetY = -0.412507f;
//        offsetX = -(offsetX - centerX)*scale;
//        offsetY = -(offsetY - centerY)*scale;
////        Log.e("debug", "offsetX: "+offsetX+"\toffsetY: "+offsetY);
//        Matrix.translateM(mModelMatrix, 0, 10*offsetX, 10*offsetY, 0);
//
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
//        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mModelMatrix, 0);
    }

    public void setPoints(float[] points){
        obj.vert.rewind();
        obj.vert.put(points);
        obj.vert.position(0);
    }

    private int createTexture(Bitmap bitmap){
        int[] texture=new int[1];
        if(bitmap!=null&&!bitmap.isRecycled()){
            //生成纹理
            GLES20.glGenTextures(1,texture,0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            return texture[0];
        }
        return 0;
    }

    public void setObj(Obj3D obj){
        this.obj = obj;
        Log.e("debug", "顶点数: "+obj.vertCount);
    }

    public void sizeChange(int width, int height, boolean rotate270){
        GLES20.glViewport(0,0,width,height);
        this.width = width;
        this.height = height;
        this.rotate270 = rotate270;
        float Ratio = (float) width / height;
        Matrix.frustumM(mProjectMatrix, 0, -Ratio, Ratio, -1, 1, 3, 9.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 4.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    public void setMatrix(float[] matrix){
        this.matrix = matrix;
    }

    public float[] getMatrix(){
        return matrix;
    }

    public void setTextureId(int textureId){
        this.textureId = textureId;
    }

    public int getTextureId(){
        return textureId;
    }

    public int getFboTextureId(){
        return fboTextureId;
    }

    public void setFaces(List<Face> faces) {
        this.faces = faces;
    }

    private double getDistance(float x1, float y1, float x2, float y2){
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }

    public void release(){
        GLES20.glDeleteProgram(programId);

    }

    private float view2openglX(int x, int width) {
        float centerX = width / 2.0f;
        float t = x - centerX;
        return t / centerX;
    }

    private float view2openglY(int y, int height) {
        float centerY = height / 2.0f;
        float s = centerY - y;
        return s / centerY;
    }

    public void loadModel(String path){
        obj = new Obj3D();
        try{
            ObjReader.read(context.getAssets().open(path), obj);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
