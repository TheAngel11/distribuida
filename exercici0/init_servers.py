import random
import subprocess

# number of servers (change this to test with different number of servers)
NUM_SERVERS = 3

# list of server IDs and its ports [(server_id, server_port), ...]
SERVERS = []
for i in range(NUM_SERVERS):
    SERVERS.append((i, 5500 + i))

# get list of all server ports (used to send the token to the next server)
SERVER_PORTS = [port for _, port in SERVERS]

# create and initialize servers (one new process per server). Each server will be listening on its own port
for server_id, server_port in SERVERS:
    # format that server.py expects:
    # python3 server.py <server_id> <server_port> <server_port_previous> <prev_server_port> <next_server_port>
    prev_server_port = SERVER_PORTS[server_id - 1] if server_id > 0 else SERVER_PORTS[-1]
    next_server_port = SERVER_PORTS[(server_id + 1) % NUM_SERVERS]
    print(f"Starting server {server_id} on port {server_port} with prev port {prev_server_port} and "
          f"next port {next_server_port}")

    args = ["python3", "server.py", str(server_id), str(server_port), str(prev_server_port),
            str(next_server_port), str(random.choice([0, 1]))]
    subprocess.Popen(args)
