all:
	gcc -g -lcurses -DRPC_SVC_FG -c -o chat_clnt.o chat_clnt.c
	gcc -g -DRPC_SVC_FG -c -o chat_client.o chat_client.c
	gcc -g -lcurses -DRPC_SVC_FG -o chat_client chat_clnt.o chat_client.o
	gcc -g -DRPC_SVC_FG -c -o chat_svc.o chat_svc.c
	gcc -g -DRPC_SVC_FG -c -o chat_server.o chat_server.c
	gcc -g -lcurses -DRPC_SVC_FG -o chat_server chat_svc.o chat_server.o

clean:
	rm -f chat_client chat_client.o chat_server chat_server.o chat.h chat_clnt.* chat_svc.* chat_xdr.*