all:
	rpcgen -C chat.x
	gcc -DRPC_SVC_FG -c -o chat_svc.o chat_svc.c
	gcc -DRPC_SVC_FG -c -o chat_server.o chat_server.c
	gcc -DRPC_SVC_FG -lcurses -o chat_server chat_svc.o chat_server.o
	gcc -DRPC_SVC_FG -lcurses -c -o chat_clnt.o chat_clnt.c
	gcc -DRPC_SVC_FG -c -o chat_client.o chat_client.c
	gcc -DRPC_SVC_FG -lcurses -o chat_client chat_clnt.o chat_client.o

clean:
	rm -f chat_client chat_client.o chat_server chat_server.o chat_client.d chat_server.d chat.h chat_clnt.* chat_svc.*