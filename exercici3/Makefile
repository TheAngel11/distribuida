# Compiler settings
CC = gcc
CFLAGS = -Wall -g
RPCGEN = rpcgen

# Source files
SRC_CLIENT = chat_client.c chat_clnt.c chat_xdr.c
SRC_SERVER = chat_server.c chat_svc.c chat_xdr.c

# Header files
RPC_HEADER = chat.h

# Object files
OBJ_CLIENT = $(SRC_CLIENT:.c=.o)
OBJ_SERVER = $(SRC_SERVER:.c=.o)

# Executables
CLIENT = chat_client
SERVER = chat_server

# Default rule
all: $(CLIENT) $(SERVER)

# Rule for client executable
$(CLIENT): $(OBJ_CLIENT)
	$(CC) $(CFLAGS) -o $@ $^ -lncurses -lpthread

# Rule for server executable
$(SERVER): $(OBJ_SERVER)
	$(CC) $(CFLAGS) -o $@ $^

# Pattern rule for object files
%.o: %.c $(RPC_HEADER)
	$(CC) $(CFLAGS) -c $< -o $@

# Rule to generate RPC code
$(RPC_HEADER): chat.x
	$(RPCGEN) chat.x

# Clean rule
clean:
	rm -f $(CLIENT) $(SERVER) $(OBJ_CLIENT) $(OBJ_SERVER) chat_svc.* chat_clnt.* chat.h chat_xdr.*

# Phony targets
.PHONY: all clean