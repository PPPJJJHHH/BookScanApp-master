#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_me_myds_g2u_bookscanapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

