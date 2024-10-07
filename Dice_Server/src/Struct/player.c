#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#include "player.h"

void add_player(Player **head, const char *name) {
    Player *new_player = (Player *)malloc(sizeof(Player));
    // If memory allocation failed, return
    if (new_player == NULL) {
        return;
    }
    new_player->name = strncpy(malloc(strlen(name) + 1), name, strlen(name) + 1);
    new_player->next = *head;
    *head = new_player;
}

void remove_player(Player **head, const char *name) {
    Player *temp = *head, *prev = NULL;

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

void free_players(Player **head) {
    Player *current = *head;
    Player *next;

    while (current != NULL) {
        next = current->next;
        free(current->name);
        free(current);
        current = next;
    }

    *head = NULL;
}

bool player_exists(const Player *head, const char *name) {
    const Player *current = head;
    while (current != NULL) {
        if (strcmp(current->name, name) == 0) {
            return true;
        }
        current = current->next;
    }
    return false;
}

void print_players(const Player *head) {
    const Player *current = head;
    printf("Players:\n");
    while (current != NULL) {
        printf("%s ", current->name);
        current = current->next;
    }
    printf("\n");
}