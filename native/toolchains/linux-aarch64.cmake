set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR aarch64)

set(CMAKE_C_COMPILER aarch64-linux-gnu-gcc)
set(CMAKE_CXX_COMPILER aarch64-linux-gnu-g++)

set(CMAKE_FIND_ROOT_PATH /usr/aarch64-linux-gnu)

set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)

# Force position independent code
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

if(DEFINED ENV{JAVA_HOME})
    set(JAVA_HOME $ENV{JAVA_HOME})
    set(JAVA_INCLUDE_PATH "${JAVA_HOME}/include")
    set(JAVA_INCLUDE_PATH2 "${JAVA_HOME}/include/linux")
    set(JAVA_AWT_INCLUDE_PATH "${JAVA_HOME}/include")
    
    # Tell CMake where to find JNI
    set(JNI_INCLUDE_DIRS 
        "${JAVA_INCLUDE_PATH}"
        "${JAVA_INCLUDE_PATH2}"
        CACHE STRING "JNI include directories" FORCE
    )
    
    message(STATUS "Cross-compile: Using JAVA_HOME=${JAVA_HOME}")
    message(STATUS "Cross-compile: JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
endif()
