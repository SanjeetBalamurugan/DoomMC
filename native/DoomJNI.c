#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "../DOOM/linuxdoom-1.10/doom_lib.h"

static uint32_t framebuffer[SCREENWIDTH * SCREENHEIGHT];

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomInit
  (JNIEnv *env, jclass clazz, jobjectArray jargs) {
    int argc = (*env)->GetArrayLength(env, jargs);
    char **argv = malloc(sizeof(char*) * argc);
    for (int i = 0; i < argc; i++) {
        jstring str = (jstring)(*env)->GetObjectArrayElement(env, jargs, i);
        const char *utf = (*env)->GetStringUTFChars(env, str, 0);
        argv[i] = strdup(utf);
        (*env)->ReleaseStringUTFChars(env, str, utf);
    }
    DOOM_Init(argc, argv);
    for (int i = 0; i < argc; i++) free(argv[i]);
    free(argv);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomStep
  (JNIEnv *env, jclass clazz) {
    DOOM_RunTic();
    memcpy(framebuffer, DOOM_GetScreenBuffer(), SCREENWIDTH * SCREENHEIGHT * sizeof(uint32_t));
}

JNIEXPORT jintArray JNICALL Java_com_netherairtune_doommc_DoomJNI_getFramebuffer
  (JNIEnv *env, jclass clazz) {
    jintArray out = (*env)->NewIntArray(env, SCREENWIDTH * SCREENHEIGHT);
    (*env)->SetIntArrayRegion(env, out, 0, SCREENWIDTH * SCREENHEIGHT, (const jint*) framebuffer);
    return out;
}

JNIEXPORT jint JNICALL Java_com_netherairtune_doommc_DoomJNI_getWidth
  (JNIEnv *env, jclass clazz) {
    return SCREENWIDTH;
}

JNIEXPORT jint JNICALL Java_com_netherairtune_doommc_DoomJNI_getHeight
  (JNIEnv *env, jclass clazz) {
    return SCREENHEIGHT;
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomKeyDown
  (JNIEnv *env, jclass clazz, jint key) {
    DOOM_KeyDown(key);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomKeyUp
  (JNIEnv *env, jclass clazz, jint key) {
    DOOM_KeyUp(key);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomMouseMove
  (JNIEnv *env, jclass clazz, jint x, jint y) {
    DOOM_MouseMove(x, y);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomMouseButton
  (JNIEnv *env, jclass clazz, jint button, jboolean pressed) {
    DOOM_MouseButton(button, pressed);
}
