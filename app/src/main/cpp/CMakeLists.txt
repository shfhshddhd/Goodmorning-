cmake_minimum_required(VERSION 3.22.1)

project("tgcalls_bridge")

add_library(
    tgcalls_bridge
    SHARED
    tgcalls_bridge.cpp
)

find_library(
    log-lib
    log
)

find_library(
    android-lib
    android
)

target_link_libraries(
    tgcalls_bridge
    ${log-lib}
    ${android-lib}
)
