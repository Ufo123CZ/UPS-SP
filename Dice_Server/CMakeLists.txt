cmake_minimum_required(VERSION 3.10)

# Project name
project(Dice_Server C)

# Set the C standard
set(CMAKE_C_STANDARD 23)
set(CMAKE_C_STANDARD_REQUIRED True)

# Specify the location of gcc
set(CMAKE_C_COMPILER "/usr/bin/gcc")

# Include directories
include_directories(${PROJECT_SOURCE_DIR}/src/Setup)
include_directories(${PROJECT_SOURCE_DIR}/src/Worker)

# Source files
set(SOURCES
    src/main.c
    src/Setup/server.c
    src/Worker/procces.c
    src/Struct/dice.c
    src/Struct/lobby.c
    src/Struct/player.c
    src/Struct/game.c
    src/Struct/state.c
)