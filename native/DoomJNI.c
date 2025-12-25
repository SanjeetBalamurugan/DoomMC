#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <setjmp.h>

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
static jmp_buf exit_jmp;
static int doom_should_exit = 0;

// Override exit to catch DOOM's exit calls
void exit(int status) {
    doom_should_exit = 1;
    longjmp(exit_jmp, 1);
}

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

    char* iwad_path = NULL;
    for (int i = 0; i < argc; i++) {
        if (strcmp(argv[i], "-iwad") == 0 && i + 1 < argc) {
            iwad_path = argv[i + 1];
            break;
        }
    }

    if (iwad_path) {
        char* last_slash = strrchr(iwad_path, '/');
        if (last_slash) {
            size_t dir_len = last_slash - iwad_path;
            char* dir_path = malloc(dir_len + 1);
            strncpy(dir_path, iwad_path, dir_len);
            dir_path[dir_len] = '\0';
            
            chdir(dir_path);
            free(dir_path);
        }
    }

    doom_should_exit = 0;
    if (setjmp(exit_jmp) == 0) {
        DOOM_Init(argc, argv);
        initialized = 1;
    } else {
        initialized = 0;
        doom_should_exit = 1;
    }

    for (int i = 0; i < argc; i++) {
        free(argv[i]);
    }
    free(argv);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_doomStep
  (JNIEnv *env, jclass clazz) {
    if (!initialized || doom_should_exit) return;
    
    if (setjmp(exit_jmp) == 0) {
        DOOM_RunTic();
        DOOM_RenderFrame();
        
        byte* screen = DOOM_GetScreenBuffer();
        byte* palette = DOOM_GetPalette();
        
        if (!screen || !palette) return;
        
        for (int i = 0; i < DOOM_WIDTH * DOOM_HEIGHT; i++) {
            byte index = screen[i];
            byte r = palette[index * 3 + 0];
            byte g = palette[index * 3 + 1];
            byte b = palette[index * 3 + 2];
            
            framebuffer[i * 4 + 0] = (jbyte)b;
            framebuffer[i * 4 + 1] = (jbyte)g;
            framebuffer[i * 4 + 2] = (jbyte)r;
            framebuffer[i * 4 + 3] = (jbyte)255;
        }
    } else {
        initialized = 0;
        doom_should_exit = 1;
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
    return (jboolean)(initialized && DOOM_IsPlayerReady() && !doom_should_exit);
}

JNIEXPORT jboolean JNICALL Java_com_netherairtune_doommc_DoomJNI_shouldExit
  (JNIEnv *env, jclass clazz) {
    return (jboolean)doom_should_exit;
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_keyDown
  (JNIEnv *env, jclass clazz, jint key) { 
    if (initialized && !doom_should_exit) {
        if (setjmp(exit_jmp) == 0) {
            DOOM_KeyDown((int)key);
        } else {
            initialized = 0;
            doom_should_exit = 1;
        }
    }
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_keyUp
  (JNIEnv *env, jclass clazz, jint key) { 
    if (initialized && !doom_should_exit) {
        if (setjmp(exit_jmp) == 0) {
            DOOM_KeyUp((int)key);
        } else {
            initialized = 0;
            doom_should_exit = 1;
        }
    }
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_mouseMove
  (JNIEnv *env, jclass clazz, jint x, jint y) { 
    if (initialized && !doom_should_exit) DOOM_MouseMove((int)x, (int)y);
}

JNIEXPORT void JNICALL Java_com_netherairtune_doommc_DoomJNI_mouseButton
  (JNIEnv *env, jclass clazz, jint button, jboolean pressed) { 
    if (initialized && !doom_should_exit) DOOM_MouseButton((int)button, (boolean)pressed);
}