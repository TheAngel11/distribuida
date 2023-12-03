package node

import (
	"encoding/gob"
	"exercici4/comm"
	"exercici4/replication"
	"exercici4/transaction"
	"exercici4/utils"
	"exercici4/web_server"
	"log"
	"net"
	"strconv"
	"sync"
)

type L2Node struct {
	id           int          //1, 2
	data         map[int]int  // Data
	mutex        sync.RWMutex // Read-write mutex
	otherL2Nodes []string     // IP @ of Other L2 nodes in the network
}

func L2NodeProvider(id int, otherL2Nodes []string) *L2Node {
	return &L2Node{
		id:           id,
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

func (n *L2Node) WriteData(newMap map[int]int) {
	n.mutex.Lock() // Lock for writing
	n.data = newMap
	web_server.TriggerNodeUpdate(n.id, 2, n.data)
	utils.WriteDataToFile("node/node_persistence/L2"+strconv.Itoa(n.id)+".txt", n.data)
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
			// L2 nodes only handle read operations
			_ = encoder.Encode(comm.ReceiveMessage{Success: false})
		} else {
			result, success := n.ReadData(operation.ReadKey)
			_ = encoder.Encode(comm.ReceiveMessage{Success: success, Value: result})
		}
	case replication.ReplicationMessage:
		// Handle replication message
		replicationMessage := msg.(replication.ReplicationMessage)
		n.WriteData(replicationMessage.Data)

		if replicationMessage.IsFirstReplication {
			// If this is the first replication, send the data to the other L2 nodes
			for _, nodeAddress := range n.otherL2Nodes {
				go replication.SendHashmapToNode(n.data, nodeAddress, false)
			}
		}
	}
}
