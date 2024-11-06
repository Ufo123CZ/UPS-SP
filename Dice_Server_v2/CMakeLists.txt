cmake_minimum_required(VERSION 3.25)
project(Dice_Server_v2)

set(CMAKE_CXX_STANDARD 23)

add_executable(Dice_Server_v2
        # Main
        src/main.cpp

        # Server
        src/Server/Server.cpp
        src/Server/MessageProcessing.cpp

        # Messages
        src/Messages/MessageFormat.cpp
        src/Messages/Login.cpp
        src/Messages/Logout.cpp

        # Data
        src/Data/DataVectors.cpp
        src/Data/Player.cpp

        # Utils
        src/Utils/ServerCommands.cpp

        # Headers
        src/Server/Server.h
        src/Utils/ServerCommands.h
        src/Utils/Consts.h
        src/Messages/MessageFormat.h
        src/Data/DataVectors.h
        src/Data/Player.h
)
