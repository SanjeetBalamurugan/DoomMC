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

# JNI configuration for cross-compilation
if(DEFINED ENV{JAVA_HOME})
    set(JAVA_HOME $ENV{JAVA_HOME})
    
    # Set all required JNI variables
    set(JAVA_INCLUDE_PATH "${JAVA_HOME}/include" CACHE PATH "Java include path")
    set(JAVA_INCLUDE_PATH2 "${JAVA_HOME}/include/linux" CACHE PATH "Java include path 2")
    set(JAVA_AWT_INCLUDE_PATH "${JAVA_HOME}/include" CACHE PATH "Java AWT include path")
    
    # Find the JVM library (even though we won't link it for header-only compilation)
    file(GLOB_RECURSE JVM_LIBRARY "${JAVA_HOME}/lib/*/libjvm.so")
    if(JVM_LIBRARY)
        list(GET JVM_LIBRARY 0 JVM_LIBRARY)
        set(JAVA_JVM_LIBRARY "${JVM_LIBRARY}" CACHE FILEPATH "JVM library")
    else()
        # Fallback: create a dummy path to satisfy FindJNI
        set(JAVA_JVM_LIBRARY "${JAVA_HOME}/lib/server/libjvm.so" CACHE FILEPATH "JVM library")
    endif()
    
    # AWT library (also for FindJNI satisfaction)
    file(GLOB_RECURSE AWT_LIBRARY "${JAVA_HOME}/lib/*/libawt.so")
    if(AWT_LIBRARY)
        list(GET AWT_LIBRARY 0 AWT_LIBRARY)
        set(JAVA_AWT_LIBRARY "${AWT_LIBRARY}" CACHE FILEPATH "AWT library")
    else()
        set(JAVA_AWT_LIBRARY "${JAVA_HOME}/lib/libawt.so" CACHE FILEPATH "AWT library")
    endif()
    
    message(STATUS "Toolchain: JAVA_HOME=${JAVA_HOME}")
    message(STATUS "Toolchain: JAVA_INCLUDE_PATH=${JAVA_INCLUDE_PATH}")
    message(STATUS "Toolchain: JAVA_JVM_LIBRARY=${JAVA_JVM_LIBRARY}")
endif()
