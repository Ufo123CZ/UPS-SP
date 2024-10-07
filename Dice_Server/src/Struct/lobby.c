#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>

#include "lobby.h"

void add_lobby(Lobby **head, const char *name, char *creator) {
    Lobby *new_lobby = (Lobby *)malloc(sizeof(Lobby));
    if (new_lobby == NULL) {
        return;
    }
    new_lobby->name = strncpy(malloc(strlen(name) + 1), name, strlen(name) + 1);
    new_lobby->maxPlayers = 2;
    new_lobby->currentPlayers = 0;
    new_lobby->creator = strncpy(malloc(strlen(creator) + 1), creator, strlen(creator) + 1);
    new_lobby->state[0] = NULL;
    new_lobby->state[1] = NULL;
    new_lobby->next = *head;
    *head = new_lobby;
}

void remove_lobby(Lobby **head, const char *name) {
    Lobby *temp = *head, *prev = NULL;

    while (temp != NULL && strcmp(temp->name, name) != 0) {
        prev = temp;
        temp = temp->next;
    }

    if (temp == NULL) return;

    if (prev == NULL) {
        *head = temp->next;
    } else {
        prev->next = temp->next;
    }

    free(temp->name);
    free(temp);
}

void free_lobbies(Lobby **head) {
    Lobby *current = *head;
    Lobby *next;

    while (current != NULL) {
        next = current->next;
        free(current->name);
        free(current->creator);
        free(current);
        current = next;
    }

    *head = NULL;
}

void get_lobbies_info(const Lobby *head, char *buffer, size_t buffer_size) {
    const Lobby *current = head;
    buffer[0] = '\0';

    while (current != NULL) {
        char lobby_info[256];
        snprintf(lobby_info, sizeof(lobby_info), "%s:%d:%s;", current->name, current->currentPlayers, current->creator);
        if (strlen(buffer) + strlen(lobby_info) < buffer_size) {
            strcat(buffer, lobby_info);
        } else {
        	// If the buffer is too small, break the loop
            break;
        }
        current = current->next;
    }
}

void print_lobbies(const Lobby *head) {
    const Lobby *current = head;
    printf("Lobbies:\n");
    while (current != NULL) {
        printf("Name: %s, Max Players: %d, Current Players: %d, Creator: %s\n", current->name, current->maxPlayers, current->currentPlayers, current->creator);
        current = current->next;
    }
}