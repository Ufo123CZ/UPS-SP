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

        # Message Types
        src/Messages/MessageTypes/Login.cpp
        src/Messages/MessageTypes/Logout.cpp
        src/Messages/MessageTypes/Queue.cpp

        # Data
        src/Data/DataVectors.cpp
        src/Data/Player.cpp
        src/Data/Game.cpp
        src/Data/Dice.cpp

        # Events
        src/Events/Events.cpp

        # Utils
        src/Utils/ServerCommands.cpp

        # Headers
        src/Server/Server.h
        src/Utils/ServerCommands.h
        src/Utils/Consts.h
        src/Messages/MessageFormat.h
        src/Data/DataVectors.h
        src/Data/Player.h
        src/Data/Game.h
        src/Data/Dice.h
        src/Events/Events.h
)
