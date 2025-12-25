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
#define AUDIO_BUFFER_SIZE 2048
#define SAMPLE_RATE 11025
#define NUM_CHANNELS 2

static jbyte framebuffer[DOOM_WIDTH * DOOM_HEIGHT * 4];
static short audio_buffer[AUDIO_BUFFER_SIZE * NUM_CHANNELS];
static int audio_write_pos = 0;
static int initialized = 0;
static jmp_buf exit_jmp;
static int doom_should_exit = 0;

void exit(int status) {
    doom_should_exit = 1;
    longjmp(exit_jmp, 1);
}

void I_InitSound() {
    memset(audio_buffer, 0, sizeof(audio_buffer));
    audio_write_pos = 0;
}

void I_UpdateSound(void) {}

void I_SubmitSound(void) {}

void I_ShutdownSound(void) {}

void I_SetChannels() {}

int I_GetSfxLumpNum(void* sfxinfo) { return 0; }

int I_StartSound(int id, int vol, int sep, int pitch, int priority) {
    return id;
}

void I_StopSound(int handle) {}

int I_SoundIsPlaying(int handle) { return 0; }

void I_UpdateSoundParams(int handle, int vol, int sep, int pitch) {}

void I_WriteSamples(short* samples, int num_samples) {
    for (int i = 0; i < num_samples && audio_write_pos < AUDIO_BUFFER_SIZE; i++) {
        audio_buffer[audio_write_pos * NUM_CHANNELS] = samples[i];
        audio_buffer[audio_write_pos * NUM_CHANNELS + 1] = samples[i];
        audio_write_pos++;
    }
}

void I_InitMusic(void) {}
void I_ShutdownMusic(void) {}
void I_SetMusicVolume(int volume) {}
void I_PauseSong(int handle) {}
void I_ResumeSong(int handle) {}
int I_RegisterSong(void *data) { return 0; }
void I_PlaySong(int handle, int looping) {}
void I_StopSong(int handle) {}
void I_UnRegisterSong(int handle) {}

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
        I_InitSound();
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
        audio_write_pos = 0;
        
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

JNIEXPORT jshortArray JNICALL Java_com_netherairtune_doommc_DoomJNI_getAudioBuffer
  (JNIEnv *env, jclass clazz) {
    int size = audio_write_pos * NUM_CHANNELS;
    if (size == 0) return NULL;
    
    jshortArray out = (*env)->NewShortArray(env, size);
    if (out == NULL) return NULL;
    (*env)->SetShortArrayRegion(env, out, 0, size, (jshort*)audio_buffer);
    return out;
}

JNIEXPORT jint JNICALL Java_com_netherairtune_doommc_DoomJNI_getSampleRate
  (JNIEnv *env, jclass clazz) {
    return SAMPLE_RATE;
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