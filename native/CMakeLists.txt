cmake_minimum_required(VERSION 4.2)
project(DoomJNI C)

set(CMAKE_C_STANDARD 99)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Add DOOM subdirectory
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/DOOM ${CMAKE_BINARY_DIR}/doom_build)

# Include DOOM headers
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/DOOM/linuxdoom-1.10)

# Build JNI library
add_library(doomjni SHARED DoomJNI.c)

target_link_libraries(doomjni 
    doomheadless
    ${JNI_LIBRARIES} 
    m
)

set_target_properties(doomjni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib
    OUTPUT_NAME "doomjni"
)