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
	"time"
)

type L1Node struct {
	id            int          //1, 2
	data          map[int]int  // Data to be replicated
	mutex         sync.RWMutex // Read-write mutex
	otherL1Nodes  []string     // IP @ of Other L1 nodes in the network
	L2PrimaryNode string       // IP @ of Primary Backup L2 node in the network
}

func L1NodeProvider(id int, otherL1Nodes []string, l2PrimaryNode string) *L1Node {
	return &L1Node{
		id:            id,
		data:          make(map[int]int),
		otherL1Nodes:  otherL1Nodes,
		L2PrimaryNode: l2PrimaryNode,
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

func (n *L1Node) WriteData(newMap map[int]int) {
	n.mutex.Lock() // Lock for writing
	n.data = newMap
	web_server.TriggerNodeUpdate(n.id, 1, n.data)
	utils.WriteDataToFile("node/node_persistence/L1"+strconv.Itoa(n.id)+".txt", n.data)
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
			// L1 nodes only handle read operations
			_ = encoder.Encode(comm.ReceiveMessage{Success: false})
		} else {
			result, success := n.ReadData(operation.ReadKey)
			_ = encoder.Encode(comm.ReceiveMessage{Success: success, Value: result})
		}
	case replication.ReplicationMessage:
		// Handle replication message
		replicationMessage := msg.(replication.ReplicationMessage)
		n.WriteData(replicationMessage.Data)
		_ = encoder.Encode(comm.ReceiveMessage{Success: true})

		if replicationMessage.IsFirstReplication {
			// Send replication message to other L1 nodes
			for _, nodeAddress := range n.otherL1Nodes {
				go replication.SendHashmapToNode(n.data, nodeAddress, false)
			}

			// Send replication message to L2 primary node in 10 seconds
			go func() {
				<-time.After(10 * time.Second)
				go replication.SendHashmapToNode(n.data, n.L2PrimaryNode, true)
			}()
		}
	}
}
