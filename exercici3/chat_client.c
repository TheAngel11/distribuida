#include "chat.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ncurses.h>
#include <pthread.h>

#define INPUT_HEIGHT 3

char *server;
CLIENT *client;
WINDOW *messages_win, *input_win;
int getstr_interrupted = 0;

void init_ncurses(){
    // Initialize ncurses
    initscr();
    cbreak();

    // Get the size of the window
    int max_y, max_x;
    getmaxyx(stdscr, max_y, max_x);

    // Create a window for displaying messages
    messages_win = newwin(max_y - INPUT_HEIGHT, max_x, 0, 0);
    scrollok(messages_win, TRUE); // Enable scrolling

    // Create a window for the input area
    input_win = newwin(INPUT_HEIGHT, max_x, max_y - INPUT_HEIGHT, 0);
}

void draw_messages(WINDOW *messages_win, const char *messages, int row) {
    row++; // Start on the next line

    char *message = strtok(strdup(messages), "\n");
    while (message) {
        // Print each message on a new line
        mvwprintw(messages_win, row++, 1, "%s", message);
        message = strtok(NULL, "\n");
    }
    box(messages_win, 0, 0);
    wrefresh(messages_win);
}

void draw_input(WINDOW *input_win, const char *prompt) {
    werase(input_win);
    box(input_win, 0, 0);
    mvwprintw(input_win, 1, 1, "%s", prompt);
    wrefresh(input_win);
}


void *refresh_chat(void *nickname) {
    int line_num = 0; // Keeps track of the number of lines printed (the number of chat messages we have)

    while (1) {
        char **message = getchat_1(&line_num, client);

        // Handle RPC call failure
        if (message == NULL || *message == NULL) {
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

        getstr_interrupted = 1; // Set the flag to indicate that the getnstr was interrupted
        draw_messages(messages_win, *message, line_num); // Draw new received messages
        refresh(); // Refresh the screen
        getstr_interrupted = 0; // Reset the flag

        free(*message);

        // Update the total line count
        line_num += new_lines;

        // Wait a second before refreshing again
        sleep(1);
    }

    return NULL;
}


void send_message(char *nickname) {
    char msg[256];
    char *prompt;
    asprintf(&prompt, "%s --> ", nickname);
    memset(msg, 0, sizeof(msg));

    while (1) {
        // Get the message from the user
        draw_input(input_win, prompt);
        wgetnstr(input_win, msg, sizeof(msg) - 1);

        // Check if the getnstr was interrupted by the other thread
        // (the printw on the other thread disrupts the wgetnstr)
        if(getstr_interrupted == 1 || strlen(msg) == 0){
            continue;
        }

        // Build the message to include the nickname
        char full_msg[300];
        sprintf(full_msg, "%s: %s\n", nickname, msg);

        char *p_msg = &full_msg[0];

        // Send the message
        write_1(&p_msg, client);

        // Empty the msg buffer
        memset(msg, 0, sizeof(msg));
    }

    free(prompt);
}




int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <chatServerIP> <nickname>\n", argv[0]);
        exit(1);
    }

    server = argv[1];
    char *nickname = argv[2];

    // Init ncurses
    init_ncurses();

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
    delwin(messages_win);
    delwin(input_win);
    endwin();
    return 0;
}