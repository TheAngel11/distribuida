package node

import (
	"log"
	"net"
)

type CoreNode struct {
	data           map[int]int // Data to be replicated
	otherNodeCores []string    // IP @ of Other core nodes in the network
	L1Nodes        []string    // IP @ of Layer 1 nodes in the network
}

func CoreNodeProvider(otherNodeCores, l1Nodes []string) *CoreNode {
	return &CoreNode{
		otherNodeCores: otherNodeCores,
		L1Nodes:        l1Nodes,
	}
}

func (n *CoreNode) Start(address string) {
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

		go handleCoreConnection(conn)
	}
}

// handleConnection handles a petition from a client.
func handleCoreConnection(conn net.Conn) {
	defer conn.Close()
}
