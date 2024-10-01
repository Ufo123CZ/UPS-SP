cmake_minimum_required(VERSION 3.29)
project(Dice_Server C)

set(CMAKE_C_STANDARD 23)

add_executable(Dice_Server
        main.c
        Setup/server.c
        Setup/server.h
        Worker/procces.c
        Worker/procces.h)
