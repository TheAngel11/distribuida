package node

import (
	"encoding/gob"
	"exercici4/comm"
	"exercici4/transaction"
	"log"
	"net"
	"sync"
)

type CoreNode struct {
	data           map[int]int  // Data to be replicated
	mutex          sync.RWMutex // Read-write mutex
	otherNodeCores []string     // IP @ of Other core nodes in the network
	L1Nodes        []string     // IP @ of Layer 1 nodes in the network
}

func CoreNodeProvider(otherNodeCores, l1Nodes []string) *CoreNode {
	return &CoreNode{
		data:           make(map[int]int),
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

		go n.handleConnection(conn)
	}
}

func (n *CoreNode) ReadData(key int) (int, bool) {
	n.mutex.RLock() // Lock for reading
	value, exists := n.data[key]
	n.mutex.RUnlock() // Unlock after reading
	return value, exists
}

func (n *CoreNode) WriteData(key int, value int) {
	n.mutex.Lock() // Lock for writing
	n.data[key] = value
	n.mutex.Unlock() // Unlock after writing
}

// handleConnection handles a petition from a client or another core.
func (n *CoreNode) handleConnection(conn net.Conn) {
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
			result, success := n.ReadData(operation.ReadKey)
			_ = encoder.Encode(comm.ReceiveMessage{Success: success, Value: result})
		}
	}

}
