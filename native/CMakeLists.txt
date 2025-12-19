cmake_minimum_required(VERSION 4.2)
project(DoomJNI C)

set(CMAKE_C_STANDARD 99)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})
include_directories(../DOOM/linuxdoom-1.10)

file(GLOB SOURCES "*.c")
list(APPEND SOURCES "../DOOM/linuxdoom-1.10/doom_lib.c")

add_library(doomjni SHARED ${SOURCES})
target_link_libraries(doomjni ${JNI_LIBRARIES} m)
set_target_properties(doomjni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib
)
