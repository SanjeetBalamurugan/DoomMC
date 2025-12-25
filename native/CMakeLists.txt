cmake_minimum_required(VERSION 4.2)
project(DoomJNI LANGUAGES C)

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI REQUIRED)

add_subdirectory(
    ${CMAKE_CURRENT_SOURCE_DIR}/../DOOM/linuxdoom-1.10
    ${CMAKE_BINARY_DIR}/doom_build
)

add_library(doomjni SHARED DoomJNI.c)

target_include_directories(doomjni PRIVATE
    ${JNI_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}/../DOOM/linuxdoom-1.10
)

if (MSVC)
    target_link_libraries(doomjni PRIVATE doomheadless)
    target_link_options(doomjni PRIVATE "/WHOLEARCHIVE:doomheadless.lib")
else()
    target_link_libraries(doomjni PRIVATE $<LINK_LIBRARY:WHOLE_ARCHIVE,doomheadless> m)
    target_link_options(doomjni PRIVATE -Wl,--no-undefined)
endif()

set_target_properties(doomjni PROPERTIES
    OUTPUT_NAME "doomjni"
)
