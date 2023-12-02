package node

import (
	"encoding/gob"
	"exercici4/comm"
	"exercici4/transaction"
	"log"
	"net"
	"sync"
)

type L2Node struct {
	data         map[int]int  // Data
	mutex        sync.RWMutex // Read-write mutex
	otherL2Nodes []string     // IP @ of Other L2 nodes in the network
}

func L2NodeProvider(otherL2Nodes []string) *L2Node {
	return &L2Node{
		data:         make(map[int]int),
		otherL2Nodes: otherL2Nodes,
	}
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

		go n.handleL2Connection(conn)
	}
}

func (n *L2Node) ReadData(key int) (int, bool) {
	n.mutex.RLock() // Lock for reading
	value, exists := n.data[key]
	n.mutex.RUnlock() // Unlock after reading
	return value, exists
}

func (n *L2Node) WriteData(key int, value int) {
	n.mutex.Lock() // Lock for writing
	n.data[key] = value
	n.mutex.Unlock() // Unlock after writing
}

// handleConnection handles a petition from a client.
func (n *L2Node) handleL2Connection(conn net.Conn) {
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
