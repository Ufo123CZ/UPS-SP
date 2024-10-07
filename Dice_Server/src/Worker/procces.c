#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "procces.h"
#include "../Struct/player.h"
#include "../Struct/lobby.h"

Player *player_list = NULL;
Lobby *lobby_list = NULL;

int output = -1;

void proccesInput(char *buffer) {
	char *info = strtok(buffer, ";");
    info = strtok(NULL, ";");

    if (info) {
        char *prefix = strtok(info, ":");

        if (prefix && strcmp(prefix, "dc") == 0) {
            char *command = strtok(NULL, ":");
            if (command) {
                if (strcmp(command, "login") == 0) {
                    char *player_name = strtok(NULL, ":");
//                    printf("Login command received for user: %s\n", player_name);
                    add_player(&player_list, player_name);
                    output = 0;
                } else if (strcmp(command, "logout") == 0) {
                    char *name = strtok(NULL, ":");
//                    printf("Logout command received for user: %s\n", name);
                    remove_player(&player_list, name);
                    output = 1;
                } else if (strcmp(command, "lobby") == 0) {
                    char *action = strtok(NULL, ":");
                    if (strcmp(action, "create") == 0) {
                        char *creator = strtok(NULL, ":");
                        char *lobby_name = strtok(NULL, ":");
//                        printf("Creating lobby %s\n", lobby_name);
//                        printf("Creator: %s\n", creator);
//                        printf("Lobby name: %s\n", lobby_name);
                        add_lobby(&lobby_list, lobby_name, creator);
                        output = 10;
                    } else if (strcmp(action, "delete") == 0) {
                        char *creator = strtok(NULL, ":");
                        if (strcmp(creator, lobby_list->creator)) {
                            char *lobby_name = strtok(NULL, ":");
//                            printf("Deleting lobby %s\n", lobby_name);
                            remove_lobby(&lobby_list, lobby_name);
                            output = 11;
                        } else {
                            printf("Player %s is not the Creator of \n", creator);
                        }
                    } else if (strcmp(action, "fetch") == 0) {
                        printf("Requesting lobbies\n");
                        output = 12;
                    } else {
                        printf("Invalid command format\n");
                    }
                }
            } else {
                printf("Invalid command format\n");
            }
        } else {
            printf("Invalid command format\n");
        }
    } else {
        printf("Invalid command format\n");
    }
}

void proccesOutput(char *buffer) {
//    printf("Content before send: %s\n", buffer);
	char lobbies_info[1024];
    switch (output) {
        case 1:		// Logout
            break;
        case 0:		// Login
        case 10:	// Create lobby
        case 11:	// Delete lobby
        case 12:	// Requesting lobbies
            get_lobbies_info(lobby_list, lobbies_info, sizeof(lobbies_info));
            strncpy(buffer, lobbies_info, sizeof(lobbies_info));
            break;
        default:
            strcpy(buffer, "dc:error:invalid_command");
            break;
    }
    output = -1;
}
