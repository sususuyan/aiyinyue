//
// Created by 11469 on 2020/11/30.
//
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_zeusee_main_hyperlandmark_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
