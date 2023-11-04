#include "chat.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define CHAT_FILE "chat.txt"

// Function to count the number of lines in the file
static int count_lines(FILE *file) {
    int lines = 0;
    int ch;
    while (!feof(file)) {
        ch = fgetc(file);
        if (ch == '\n') {
            lines++;
        }
    }
    return lines;
}

int * write_1_svc(char **msg, struct svc_req *req) {
    static int result;
    FILE *file = fopen(CHAT_FILE, "a");
    if (!file) {
        result = 0; // Failure
        return &result;
    }

    fprintf(file, "%s\n", *msg);
    fclose(file);

    result = 1; // Success
    return &result;
}

char ** getchat_1_svc(int *line_num, struct svc_req *req) {
    static char *result;
    FILE *file = fopen(CHAT_FILE, "r");
    if (!file) {
        result = strdup("Error: Could not read chat file.\n");
        return &result;
    }

    int total_lines = count_lines(file);
    // If the client is already up-to-date, return an empty string.
    if (*line_num >= total_lines) {
        result = strdup("");
        fclose(file);
        return &result;
    }

    // Put the file pointer to the starting point where we need to read the new lines
    fseek(file, 0, SEEK_SET);
    int lines_to_skip = *line_num;
    for(int i = 0; i < lines_to_skip; i++) {
        while (fgetc(file) != '\n') {
            if (feof(file)) break;
        }
    }

    // Read the new lines and concatenate them into a single string
    size_t result_size = 16; // Initial size, will be adjusted with realloc as needed
    result = (char *) malloc(result_size);


    char *line = NULL;
    size_t len = 0;
    ssize_t read;
    size_t current_length = 0;

    while ((read = getline(&line, &len, file)) != -1) {

        // Check if we need to resize the result buffer
        if (current_length + read >= result_size) {
            result_size *= 2; // Double the buffer size
            result = realloc(result, result_size);
        }

        // Copy the line into the result buffer
        // Note: memcpy is used instead of strcpy because the line may not be null-terminated
        memcpy(result + current_length, line, read);
        current_length += read;
    }

    free(line);
    fclose(file);

    // Null-terminate the result string
    result[current_length] = '\0';

    return &result;
}