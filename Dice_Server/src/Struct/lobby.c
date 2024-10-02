#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "lobby.h"

void add_lobby(Lobby **head, const char *name, Player *creator) {
    Lobby *new_lobby = (Lobby *)malloc(sizeof(Lobby));
    if (new_lobby == NULL) {
        return;
    }
    new_lobby->name = strdup(name);
    new_lobby->maxPlayers = 2;
    new_lobby->currentPlayers = 0;
    new_lobby->creator = creator;
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
        free(current);
        current = next;
    }

    *head = NULL;
}

void print_lobbies(const Lobby *head) {
    const Lobby *current = head;
    printf("Lobbies: ");
    while (current != NULL) {
        printf("Name: %s, Max Players: %d, Current Players: %d\n", current->name, current->maxPlayers, current->currentPlayers);
        current = current->next;
    }
    printf("\n");
}