#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include "../DOOM/linuxdoom-1.10/doom_lib.h"

static jbyte framebuffer[SCREENWIDTH * SCREENHEIGHT * 4];
static int initialized = 0;

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomInit
  (JNIEnv *env, jclass clazz, jobjectArray jargs) {
    if (initialized) return;
    
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
    initialized = 1;
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomStep
  (JNIEnv *env, jclass clazz) {
    if (!initialized) return;
    
    DOOM_RunTic();
    DOOM_RenderFrame();
    
    byte* screen = DOOM_GetScreenBuffer();
    byte* palette = DOOM_GetPalette();
    
    if (!screen || !palette) return;
    
    for (int i = 0; i < SCREENWIDTH * SCREENHEIGHT; i++) {
        byte index = screen[i];
        framebuffer[i * 4 + 0] = palette[index * 3 + 0];
        framebuffer[i * 4 + 1] = palette[index * 3 + 1];
        framebuffer[i * 4 + 2] = palette[index * 3 + 2];
        framebuffer[i * 4 + 3] = (jbyte)255;
    }
}

JNIEXPORT jbyteArray JNICALL Java_com_netherairtune_doommc_DoomJNI_getFramebuffer
  (JNIEnv *env, jclass clazz) {
    jbyteArray out = (*env)->NewByteArray(env, SCREENWIDTH * SCREENHEIGHT * 4);
    (*env)->SetByteArrayRegion(env, out, 0, SCREENWIDTH * SCREENHEIGHT * 4, framebuffer);
    return out;
}

JNIEXPORT jint JNICALL Java_com_netherairtune_doommc_DoomJNI_getWidth
  (JNIEnv *env, jclass clazz) { return SCREENWIDTH; }

JNIEXPORT jint JNICALL Java_com_netherairtune_doommc_DoomJNI_getHeight
  (JNIEnv *env, jclass clazz) { return SCREENHEIGHT; }

JNIEXPORT jboolean JNICALL Java_com_netherairtune_doommc_DoomJNI_isPlayerReady
  (JNIEnv *env, jclass clazz) { 
    return initialized && DOOM_IsPlayerReady();
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_keyDown
  (JNIEnv *env, jclass clazz, jint key) { 
    if (initialized) DOOM_KeyDown(key);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_keyUp
  (JNIEnv *env, jclass clazz, jint key) { 
    if (initialized) DOOM_KeyUp(key);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_mouseMove
  (JNIEnv *env, jclass clazz, jint x, jint y) { 
    if (initialized) DOOM_MouseMove(x, y);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_mouseButton
  (JNIEnv *env, jclass clazz, jint button, jboolean pressed) { 
    if (initialized) DOOM_MouseButton(button, pressed);
}