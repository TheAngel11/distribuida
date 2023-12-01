package node

import (
	"log"
	"net"
)

type L2Node struct {
	data map[int]int // Data
}

func L2NodeProvider() *L2Node {
	return &L2Node{}
}

func (n *L2Node) Start(address string) {
	ln, err := net.Listen("tcp", address)
	if err != nil {
		log.Fatal(err)
	}
	defer ln.Close()

	// Accept connections
	for {
		conn, err := ln.Accept()
		if err != nil {
			log.Println(err)
			continue
		}

		go handleL2Connection(conn)
	}
}

// handleConnection handles a petition from a client.
func handleL2Connection(conn net.Conn) {
	defer conn.Close()
}
