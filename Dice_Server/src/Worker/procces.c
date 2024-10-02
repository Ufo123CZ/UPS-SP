#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "procces.h"
#include "../Struct/player.h"
#include "../Struct/lobby.h"

Player *player_list = NULL;
Lobby *lobby_list = NULL;


void proccesInput(char *buffer) {
    printf("%s\n", buffer);

    char *info = strtok(buffer, ";");
    info = strtok(NULL, ";");

    if (info) {
        char *prefix = strtok(info, ":");

        if (prefix && strcmp(prefix, "dc") == 0) {
            char *command = strtok(NULL, ":");
            if (command) {
                if (strcmp(command, "login") == 0) {
                    char *player_name = strtok(NULL, ":");
                    printf("Login command received for user: %s\n", player_name);
                    add_player(&player_list, player_name);
                } else if (strcmp(command, "logout") == 0) {
                    char *name = strtok(NULL, ":");
                    printf("Logout command received for user: %s\n", name);
                    remove_player(&player_list, name);
                } else if (strcmp(command, "lobby") == 0) {
                    char *action = strtok(NULL, ":");
                    if (strcmp(action, "create") == 0) {
                        char *creator = strtok(NULL, ":");
                        if (player_exists(player_list, creator)) {
                            char *lobby_name = strtok(NULL, ":");
                            add_lobby(&lobby_list, lobby_name, player_list);
                        } else {
                            printf("Player %s does not exist\n", creator);
                        }
                    } else if (strcmp(action, "delete") == 0) {
                        char *creator = strtok(NULL, ":");
                        if (player_exists(player_list, creator)) {
                            char *lobby_name = strtok(NULL, ":");
                            remove_lobby(&lobby_list, lobby_name);
                        } else {
                            printf("Player %s does not exist\n", creator);
                        }
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
    printf("%s\n", buffer);
}