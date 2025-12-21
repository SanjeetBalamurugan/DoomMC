#include <jni.h>
#include <stdlib.h>
#include <string.h>

// Forward declarations
typedef unsigned char byte;
typedef int boolean;

void DOOM_Init(int argc, char** argv);
void DOOM_RunTic(void);
void DOOM_RenderFrame(void);
byte* DOOM_GetScreenBuffer(void);
int DOOM_GetScreenWidth(void);
int DOOM_GetScreenHeight(void);
byte* DOOM_GetPalette(void);
boolean DOOM_IsPlayerReady(void);
void DOOM_KeyDown(int key);
void DOOM_KeyUp(int key);
void DOOM_MouseMove(int x, int y);
void DOOM_MouseButton(int button, boolean pressed);

#define DOOM_WIDTH 320
#define DOOM_HEIGHT 200

static jbyte framebuffer[DOOM_WIDTH * DOOM_HEIGHT * 4];
static int initialized = 0;

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomInit
  (JNIEnv *env, jclass clazz, jobjectArray jargs) {
    if (initialized) return;

    int argc = (*env)->GetArrayLength(env, jargs);
    char **argv = malloc(sizeof(char*) * (argc + 1));

    for (int i = 0; i < argc; i++) {
        jstring str = (jstring)(*env)->GetObjectArrayElement(env, jargs, i);
        const char *utf = (*env)->GetStringUTFChars(env, str, 0);
        argv[i] = strdup(utf);
        (*env)->ReleaseStringUTFChars(env, str, utf);
    }

    argv[argc] = NULL;

    DOOM_Init(argc, argv);
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
    
    for (int i = 0; i < DOOM_WIDTH * DOOM_HEIGHT; i++) {
        byte index = screen[i];
        framebuffer[i * 4 + 0] = (jbyte)palette[index * 3 + 0];
        framebuffer[i * 4 + 1] = (jbyte)palette[index * 3 + 1];
        framebuffer[i * 4 + 2] = (jbyte)palette[index * 3 + 2];
        framebuffer[i * 4 + 3] = (jbyte)255;
    }
}

JNIEXPORT jbyteArray JNICALL Java_com_netherairtune_doommc_DoomJNI_getFramebuffer
  (JNIEnv *env, jclass clazz) {
    jbyteArray out = (*env)->NewByteArray(env, DOOM_WIDTH * DOOM_HEIGHT * 4);
    if (out == NULL) return NULL;
    (*env)->SetByteArrayRegion(env, out, 0, DOOM_WIDTH * DOOM_HEIGHT * 4, framebuffer);
    return out;
}

JNIEXPORT jint JNICALL Java_com_netherairtune_doommc_DoomJNI_getWidth
  (JNIEnv *env, jclass clazz) { 
    return DOOM_WIDTH; 
}

JNIEXPORT jint JNICALL Java_com_netherairtune_doommc_DoomJNI_getHeight
  (JNIEnv *env, jclass clazz) { 
    return DOOM_HEIGHT; 
}

JNIEXPORT jboolean JNICALL Java_com_netherairtune_doommc_DoomJNI_isPlayerReady
  (JNIEnv *env, jclass clazz) { 
    return (jboolean)(initialized && DOOM_IsPlayerReady());
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_keyDown
  (JNIEnv *env, jclass clazz, jint key) { 
    if (initialized) DOOM_KeyDown((int)key);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_keyUp
  (JNIEnv *env, jclass clazz, jint key) { 
    if (initialized) DOOM_KeyUp((int)key);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_mouseMove
  (JNIEnv *env, jclass clazz, jint x, jint y) { 
    if (initialized) DOOM_MouseMove((int)x, (int)y);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_mouseButton
  (JNIEnv *env, jclass clazz, jint button, jboolean pressed) { 
    if (initialized) DOOM_MouseButton((int)button, (boolean)pressed);
}