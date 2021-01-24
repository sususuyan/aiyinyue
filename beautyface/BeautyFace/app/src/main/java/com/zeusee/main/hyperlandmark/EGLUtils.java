package com.zeusee.main.hyperlandmark;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.Surface;
//EGL环境创建的帮助类
public class EGLUtils {

    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private EGLContext eglCtx = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay eglDis = EGL14.EGL_NO_DISPLAY;


    public void initEGL(Surface surface) {
        //获取Display：Display是一个连接，用于连接设备上的底层窗口系统
        // 通常情况下传入EGL_DEFAULT_DISPLAY作为eglGetDisplay的参数就可以了，EGL 会自动返回默认的Display
        eglDis = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2]; //主版本号和副版本号
        EGL14.eglInitialize(eglDis, version, 0, version, 1);


        //选择Config
        int confAttr[] = {
                EGL14.EGL_SURFACE_TYPE,EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_RED_SIZE, 8,//指定RGB中的R大小（bits）
                EGL14.EGL_GREEN_SIZE, 8,//指定G大小
                EGL14.EGL_BLUE_SIZE, 8,//指定B大小
                EGL14.EGL_ALPHA_SIZE, 8,//指定Alpha大小，以上四项实际上指定了像素格式
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,//指定渲染api版本, EGL14.EGL_OPENGL_ES2_BIT
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_SAMPLE_BUFFERS, 1,
                EGL14.EGL_SAMPLES, 4,
                EGL14.EGL_NONE//总是以EGL14.EGL_NONE结尾
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(eglDis, confAttr, 0, configs, 0, 1, numConfigs, 0);

        //Context
        //容器，存放两个东西：
        //内部状态信息(View port, depth range, clear color, textures, VBO, FBO, ...)
        //调用缓存，保存了在这个Context下发起的GL调用指令。(OpenGL 调用是异步的)
        //总的来说，Context是设计来存储渲染相关的输入数据。
        int ctxAttr[] = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,// 0x3098
                EGL14.EGL_NONE
        };
        eglCtx = EGL14.eglCreateContext(eglDis, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0);

        //创建Surface
        int[] surfaceAttr = {
                EGL14.EGL_NONE
        };
        //对应的Surface则是设计来存储渲染相关的输出数据。
        //Surface实际上是一个对底层窗口对象的拓展、或是一个有着额外辅助缓冲的像素映射(pixmap)。
        //这些辅助缓存包括颜色缓存(color buffer)、深度缓冲(depth buffer)、模板缓冲(stencil buffer)

        eglSurface = EGL14.eglCreateWindowSurface(eglDis, configs[0], surface, surfaceAttr, 0);

        //在完成EGL的初始化之后，需要通过eglMakeCurrent()函数来将当前的上下文切换，这样opengl的函数才能启动作用
        EGL14.eglMakeCurrent(eglDis, eglSurface, eglSurface, eglCtx);

    }

    public EGLContext getContext() {
        return eglCtx;
    }

    public void swap() {
        //如果surface是一个双重缓冲surface(大多数情况)，这个方法将会交换surface内部的前端缓冲(front-buffer)和后端缓冲(back-surface)。
        // 后端缓冲用于存储渲染结果，前端缓冲则用于底层窗口系统，底层窗口系统将缓冲中的颜色信息显示到设备上
        EGL14.eglSwapBuffers(eglDis, eglSurface);
    }

    public void release() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(eglDis, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(eglDis, eglSurface);
            eglSurface = EGL14.EGL_NO_SURFACE;
        }
        if (eglCtx != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(eglDis, eglCtx);
            eglCtx = EGL14.EGL_NO_CONTEXT;
        }
        if (eglDis != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(eglDis);
            eglDis = EGL14.EGL_NO_DISPLAY;
        }
    }
}
