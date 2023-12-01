package node

import (
	"log"
	"net"
)

type L1Node struct {
	data    map[int]int // Data to be replicated
	L2Nodes []string    // IP @ of Layer 2 nodes in the network
}

func L1NodeProvider(l2Nodes []string) *L1Node {
	return &L1Node{
		L2Nodes: l2Nodes,
	}
}

func (n *L1Node) Start(address string) {
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

		go handleL1Connection(conn)
	}
}

// handleConnection handles a petition from a client.
func handleL1Connection(conn net.Conn) {
	defer conn.Close()
}
