cmake_minimum_required(VERSION 3.25)
project(Dice_Server_v2)

set(CMAKE_CXX_STANDARD 23)

add_executable(Dice_Server_v2
        # Main
        src/main.cpp

        # Server
        src/Server/Server.cpp

        # Utils
        src/Utils/ServerCommands.cpp

        # Headers
        src/Server/Server.h
        src/Utils/ServerCommands.h
        src/Utils/Consts.h
)
