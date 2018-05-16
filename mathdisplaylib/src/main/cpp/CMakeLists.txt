


cmake_minimum_required(VERSION 3.4.1)

add_library( # Sets the name of the library.
             main

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             freetype_jni.cpp
             )

find_package(cdep-dependencies REQUIRED)
add_all_cdep_dependencies(main)

target_link_libraries(main log android)
