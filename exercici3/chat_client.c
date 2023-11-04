#include "chat.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ncurses.h>
#include <pthread.h>

char *server;
CLIENT *client;

void *refresh_chat(void *nickname) {
    initscr(); // Start curses mode
    noecho(); // Don't echo input
    cbreak(); // Line buffering disabled

    int line_num = 0; // Keeps track of the number of lines printed (the number of chat messages we have)

    while (1) {
        char **message = getchat_1(&line_num, client);

        // Handle RPC call failure
        if (message == NULL || *message == NULL) {
            endwin(); // End curses mode
            return NULL;
        }

        // Count new lines
        int new_lines = 0;
        char *line_ptr = *message;
        while (*line_ptr != '\0') {
            if (*line_ptr == '\n') {
                new_lines++;
            }
            line_ptr++;
        }

        // Update the total line count
        line_num += new_lines;

        printw("%s", *message); // Print the chat messages
        refresh(); // Refresh the screen

        free(*message);

        // Wait a second before refreshing again
        sleep(1);
    }

    return NULL;
}


void send_message(char *nickname) {
    char msg[256];
    echo();  // Echo input to screen

    while (1) {
        mvprintw(LINES-1, 0, "%s --> ", nickname); // Print the prompt
        getnstr(msg, sizeof(msg) - 1); // Get the message from the user

        // Build the message to include the nickname
        char full_msg[300];
        sprintf(full_msg, "%s: %s\n", nickname, msg);

        char *p_msg = &full_msg[0];

        // Send the message
        write_1(&p_msg, client);
    }
}


int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <chatServerIP> <nickname>\n", argv[0]);
        exit(1);
    }

    server = argv[1];
    char *nickname = argv[2];

    // Create the connection to the server
    client = clnt_create(server, CHATPROG, CHATVERS, "udp");
    if (client == NULL) {
        clnt_pcreateerror(server);
        exit(1);
    }

    // Create a thread to refresh the chat display
    pthread_t refresh_thread;
    if (pthread_create(&refresh_thread, NULL, refresh_chat, (void *)nickname)) {
        fprintf(stderr, "Error creating thread\n");
        return 1;
    }

    // The main thread will send messages
    send_message(nickname);

    // TODO: We should do this in the SIGINT/SIGTERM handler
    pthread_join(refresh_thread, NULL);
    clnt_destroy(client);
    return 0;
}