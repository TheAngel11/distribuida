import random
import subprocess

# number of servers (change this to test with different number of servers)
NUM_SERVERS = 3
# list of server IDs and its ports [(id, port), ...]
SERVERS = []
# get list of all server ports (used to send the token to the next server)
PORTS = []
for i in range(NUM_SERVERS):
    actual_port = 5500 + i
    SERVERS.append((i, actual_port))
    PORTS.append(actual_port)

# create and initialize servers (one new process per server). Each server will be listening on its own port
for id, port in SERVERS:
    # format that server.py expects:
    # python3 server.py <id> <port> <prev_port> <next_port> <read_not_write>
    prev_port = PORTS[id - 1]
    next_port = PORTS[(id + 1) % NUM_SERVERS]
    read_not_write = random.choices([0, 1], weights=[0.7, 0.3])

    print(f"Initializing the server...\n"
          f"Server number {id} in port {port} with previous port {prev_port} and "
          f"next port {next_port} is running.")
    args = ["python3", "server.py", str(id), str(port), str(prev_port), str(next_port), str(random.choice([0, 1]))]
    subprocess.Popen(args)
