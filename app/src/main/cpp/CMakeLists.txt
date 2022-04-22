cmake_minimum_required(VERSION 3.10.2)

project("openglesdemo")

add_library(
        opengles-lesson-lib
        SHARED

        main.cpp
)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        opengles-lesson-lib
        EGL
        GLESv3
        android

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})