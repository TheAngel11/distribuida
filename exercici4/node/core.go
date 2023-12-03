package node

import (
	"encoding/gob"
	"exercici4/client"
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

type CoreNode struct {
	id             int          //1, 2, 3
	data           map[int]int  // Data to be replicated
	mutex          sync.RWMutex // Read-write mutex
	otherNodeCores []string     // IP @ of Other core nodes in the network
	L1PrimaryNode  string       // IP @ of Primary Backup L1 node in the network
	updateCounter  int          // Every 10 updates, send the data to L1 node
}

func CoreNodeProvider(id int, otherNodeCores []string, l1PrimaryNode string) *CoreNode {
	return &CoreNode{
		id:             id,
		data:           make(map[int]int),
		otherNodeCores: otherNodeCores,
		L1PrimaryNode:  l1PrimaryNode,
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
	web_server.TriggerNodeUpdate(n.id, 0, n.data)
	utils.WriteDataToFile("node/node_persistence/core"+strconv.Itoa(n.id)+".txt", n.data)
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

			// Check if we need to send the data to the L1 node
			n.updateCounter++
			if n.updateCounter >= 10 {
				n.updateCounter = 0
				// Only send to L1 Primary Backup if this operation is not a replication operation
				// (otherwise, we would send the same operation more than once)
				if operation.IsReplicationOperation == false {
					go replication.SendHashmapToNode(n.data, n.L1PrimaryNode, true)
				}
			}

			// If it's not a replication operation, then send it to the other nodes (eager replication)
			if operation.IsReplicationOperation == false {
				wg := sync.WaitGroup{}
				// Send operation to other core nodes
				operation.IsReplicationOperation = true
				for _, otherNodeCore := range n.otherNodeCores {
					wg.Add(1)
					go func(nodeToSendMsg string) {
						err := client.SendOperationToNode(operation, nodeToSendMsg)
						if err != nil {
							//TODO: What if one of the other nodes fails?
							// We should cancel all the other transactions already done
							// We won't handle this for now
							log.Println("Error sending operation to other core:", err)
						}
						wg.Done()
					}(otherNodeCore)
				}
				wg.Wait() // Wait for all the other nodes to finish
			}

			// Send success message to client
			_ = encoder.Encode(comm.ReceiveMessage{Success: true})
		} else {
			result, success := n.ReadData(operation.ReadKey)
			_ = encoder.Encode(comm.ReceiveMessage{Success: success, Value: result})
		}

	}

}
