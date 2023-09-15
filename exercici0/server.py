import socket
import time
from sys import argv

# simulation variables
OPERATION_WAIT_TIME = 2

# server variables
SERVER_ID = 0
SERVER_PORT = 0
NEXT_SERVER_PORT = 0
PREV_SERVER_PORT = 0
READ_OR_WRITE = 0  # 0 for a read only server , 1 for read and write server

LOCAL_VARIABLE = 0

# shared variable (what we will be sending around the ring)
TOKEN = None  # token will be: "TOKEN <int>"


# handle client connection (previous server)
def handle_client(client_socket):
    global LOCAL_VARIABLE, TOKEN
    while True:
        data = client_socket.recv(1024).decode('utf-8')
        if data.startswith('TOKEN'):
            _, token_value = data.split()
            TOKEN = int(token_value)  # update token value
            perform_operations()  # perform operations (read or write, depending on the server)
        client_socket.close()


def perform_operations():
    global READ_OR_WRITE
    if READ_OR_WRITE == 0:
        read_variable()
    else:
        read_variable()
        update_variable()


def update_variable():
    global LOCAL_VARIABLE, TOKEN, OPERATION_WAIT_TIME
    time.sleep(OPERATION_WAIT_TIME)
    LOCAL_VARIABLE += 1
    TOKEN = LOCAL_VARIABLE
    print(f"Server {SERVER_ID} updated the variable to {LOCAL_VARIABLE}")


def read_variable():
    global LOCAL_VARIABLE, TOKEN, OPERATION_WAIT_TIME
    time.sleep(OPERATION_WAIT_TIME)
    LOCAL_VARIABLE = TOKEN
    print(f"Server {SERVER_ID} read the variable {LOCAL_VARIABLE}")


def send_token_to_next_server():
    global NEXT_SERVER_PORT
    message = f"TOKEN {TOKEN}"
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect(('127.0.0.1', NEXT_SERVER_PORT))
        s.sendall(message.encode('utf-8'))


def main():
    global SERVER_ID, SERVER_PORT, TOKEN

    # create our socket (TCP). (We could also consider using UDP, check the document for the discussion)
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind(('127.0.0.1', SERVER_PORT))
    server.listen(1)  # 1 in theory, as we're on a ring

    # if we are the first server, we initialize the token and start the ring
    if SERVER_ID == 0:
        TOKEN = LOCAL_VARIABLE
        perform_operations()
        send_token_to_next_server()

    # listen for the next connection from the previous server, and pass the token to the next server
    while True:
        client_socket, addr = server.accept()
        handle_client(client_socket)
        send_token_to_next_server()


if __name__ == "__main__":
    # format that server.py expects:
    # python3 server.py <server_id> <server_port> <server_port_previous> <server_port_next> <read_or_write>
    SERVER_ID = int(argv[1])
    SERVER_PORT = int(argv[2])
    PREV_SERVER_PORT = int(argv[3])
    NEXT_SERVER_PORT = int(argv[4])
    READ_OR_WRITE = int(argv[5])

    # start server
    main()
