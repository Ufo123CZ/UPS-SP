cmake_minimum_required(VERSION 3.29)
project(Dice_Server C)

set(CMAKE_C_STANDARD 23)

add_executable(Dice_Server
        src/main.c
        src/Setup/server.c
        src/Setup/server.h
        src/Worker/procces.c
        src/Worker/procces.h
        src/Struct/player.c
        src/Struct/player.h
        src/Struct/lobby.c
        src/Struct/lobby.h
        src/Struct/game.c
        src/Struct/game.h
        src/Struct/dice.c
        src/Struct/dice.h
        src/Struct/dice.h
        src/Setup/server.c)
