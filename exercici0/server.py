import socket
import time
from sys import argv

# constants
HOST = "127.0.0.1"
# simulation variables
OPERATION_WAIT_TIME = 1
# server variables
ID = 0
PORT = 0
NEXT_PORT = 0
PREV_PORT = 0
READ_NOT_WRITE = 0  # 1 for a read only server , 0 for read and write server
LOCAL_VALUE = 0 # The int variable that we will read and/or update
# shared variable (what we will be sending around the ring)
TOKEN = None  # token will be: "TOKEN <int>"

# handle client connection (previous server)
def handle_client(connection):
    global TOKEN
    data = connection.recv(16).decode("utf-8")

    TOKEN = int(data)  # update token value
    make_operations()  # make operations (read or write, depending on the server)
    connection.close()

def make_operations():
    global READ_NOT_WRITE, ID
    if ID == 0:
        print("----------------------------------")

    if READ_NOT_WRITE == 1:
        get_current_value()
    else:
        get_current_value()
        update_current_value()
    time.sleep(OPERATION_WAIT_TIME)

def update_current_value():
    global LOCAL_VALUE, TOKEN, OPERATION_WAIT_TIME
    LOCAL_VALUE += 1  # UPDATING THE VARIABLE
    TOKEN = LOCAL_VALUE
    print(f"Server {ID} has updated the variable to value {LOCAL_VALUE}")

def get_current_value():
    global LOCAL_VALUE, TOKEN, OPERATION_WAIT_TIME
    LOCAL_VALUE = TOKEN
    print(f"Server {ID} has read the variable with value {LOCAL_VALUE}")

def send_token():
    global NEXT_PORT
    message = f"{TOKEN}"

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s.connect((HOST, NEXT_PORT))
        s.sendall(message.encode("utf-8"))
    finally:
        s.close()

def main():
    global TOKEN, ID, PORT, PREV_PORT, NEXT_PORT, READ_NOT_WRITE, LOCAL_VALUE

    # format that server.py expects:
    # python3 server.py <id> <port> <prev_port> <next_port> <read_not_write>
    ID = int(argv[1])
    PORT = int(argv[2])
    PREV_PORT = int(argv[3])
    NEXT_PORT = int(argv[4])
    READ_NOT_WRITE = int(argv[5])

    # create our socket (TCP). (We could also consider using UDP, check the document for the discussion)
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((HOST, PORT))
    server.listen(1)  # 1 in theory, as we're on a ring

    if READ_NOT_WRITE == 1:
        print(f"Server {ID} is a read only server")
    else:
        print(f"Server {ID} is a read and write server")

    # if we are the first server, we initialize the token and start the ring
    if ID == 0:
        TOKEN = LOCAL_VALUE
        send_token()

    # listen for the next connection from the previous server, and pass the token to the next server
    for i in range(10):
        connection, _ = server.accept()
        handle_client(connection)
        send_token()


if __name__ == "__main__":
    main()
