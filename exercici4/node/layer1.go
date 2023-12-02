package node

import (
	"encoding/gob"
	"exercici4/comm"
	"exercici4/transaction"
	"log"
	"net"
	"sync"
)

type L1Node struct {
	data         map[int]int  // Data to be replicated
	mutex        sync.RWMutex // Read-write mutex
	otherL1Nodes []string     // IP @ of Other L1 nodes in the network
	L2Nodes      []string     // IP @ of Layer 2 nodes in the network
}

func L1NodeProvider(otherL1Nodes, l2Nodes []string) *L1Node {
	return &L1Node{
		data:         make(map[int]int),
		otherL1Nodes: otherL1Nodes,
		L2Nodes:      l2Nodes,
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

		go n.handleL1Connection(conn)
	}
}

func (n *L1Node) ReadData(key int) (int, bool) {
	n.mutex.RLock() // Lock for reading
	value, exists := n.data[key]
	n.mutex.RUnlock() // Unlock after reading
	return value, exists
}

func (n *L1Node) WriteData(key int, value int) {
	n.mutex.Lock() // Lock for writing
	n.data[key] = value
	n.mutex.Unlock() // Unlock after writing
}

// handleConnection handles a petition from a client.
func (n *L1Node) handleL1Connection(conn net.Conn) {
	defer conn.Close()

	// Read what the other node wants to do
	var msg comm.SendMessage // Message to be received
	decoder := gob.NewDecoder(conn)
	err := decoder.Decode(&msg)
	if err != nil {
		log.Println(err)
		return
	}

	// Handle message
	encoder := gob.NewEncoder(conn) // Encoder to send messages

	switch msg.(type) {
	case transaction.Operation:
		// Handle operation
		operation := msg.(transaction.Operation)
		if operation.WriteKeyValue != nil {
			n.WriteData(operation.WriteKeyValue[0], operation.WriteKeyValue[1])
			_ = encoder.Encode(comm.ReceiveMessage{Success: true})
		} else {
			_ = encoder.Encode(comm.ReceiveMessage{Success: false})
		}
	}

}
