package com.zeusee.main.hyperlandmark;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLPoints {
    private FloatBuffer vertexBuffer;//顶点坐标数据缓冲
    private int bufferLength = 109*2*4;
    private int programId = -1;//自定义渲染管线程序id
    private int aPositionHandle;//顶点位置属性引用id
    private int[] vertexBuffers;

    private String fragmentShader =
            "void main() {\n" +
            "    gl_FragColor = vec4(1.0,0.0,0.0,1.0);\n" +
            "}";
    private  String vertexShader = "attribute vec2 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(aPosition,0.0,1.0);\n" +
            "    gl_PointSize = 10.0;\n"+
            "}";

    public GLPoints(){
        vertexBuffer = ByteBuffer.allocateDirect(bufferLength)//分配一块本地内存，分配大小由外部传入
                .order(ByteOrder.nativeOrder())//诉缓冲区，按照本地字节序组织内容
                .asFloatBuffer();//我们希望操作Float，调用这个方法会返回FloatBuffer
        vertexBuffer.position(0);//把数据下标移动到指定位置
    }
    //初始化
    public void initPoints(){
        //基于顶点着色器与片元着色器创建程序
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        //引用id获取vertexShader程序中顶点位置属性aPosition
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        // 创建VBO得到vboId
        vertexBuffers = new int[1];//Vertex Buffer object
        GLES20.glGenBuffers(1,vertexBuffers,0);
        //根据id绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        //分配VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferLength, vertexBuffer,GLES20.GL_STATIC_DRAW);
        //解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    //传入坐标位置
    public void setPoints(float[] points){
        vertexBuffer.rewind();
        vertexBuffer.put(points);//填充数据
        vertexBuffer.position(0);//把数据下标移动到指定位置
    }

    //点绘制
    public void drawPoints(){
        GLES20.glUseProgram(programId);
        //根据id绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        //为VBO设置顶点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0,bufferLength,vertexBuffer);
        //启用指定属性
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        //设置顶点数据
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false,
                0, 0);
       // 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //绘制
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 109);
    }

    public void release(){
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteBuffers(1,vertexBuffers,0);
    }
}
