package main

import (
	"encoding/gob"
	"exercici4/client"
	"exercici4/comm"
	"exercici4/node"
	"exercici4/replication"
	"exercici4/transaction"
	"exercici4/utils"
	"exercici4/web_server"
	"time"
)

func main() {
	// Initialize nodes
	coreNodes := make([]*node.CoreNode, 3)
	layer1Nodes := make([]*node.L1Node, 2)
	layer2Nodes := make([]*node.L2Node, 2)

	// Initialize node addresses
	coreNodeAddresses := []string{
		"localhost:8081",
		"localhost:8082",
		"localhost:8083",
	}
	l1NodeAddresses := []string{
		"localhost:8084",
		"localhost:8085",
	}
	l2NodeAddresses := []string{
		"localhost:8086",
		"localhost:8087",
	}

	// Initialize and start core nodes
	for i := range coreNodes {
		// Create a new slice excluding the address at index i
		addresses := utils.ExcludeElement(coreNodeAddresses, i)

		// Primary Backup L1 node is the first L1 node in the list
		coreNodes[i] = node.CoreNodeProvider(i+1, addresses, l1NodeAddresses[0])

		// Start node
		go coreNodes[i].Start(coreNodeAddresses[i])
	}

	// Initialize and start layer 1 nodes
	for i := range layer1Nodes {
		// Create a new slice excluding the address at index i
		addresses := utils.ExcludeElement(l1NodeAddresses, i)

		// Primary Backup L2 node is the first L2 node in the list
		layer1Nodes[i] = node.L1NodeProvider(i+1, addresses, l2NodeAddresses[0])

		// Start node
		go layer1Nodes[i].Start(l1NodeAddresses[i])
	}

	// Initialize and start layer 2 nodes
	for i := range layer2Nodes {
		// Create a new slice excluding the address at index i
		addresses := utils.ExcludeElement(l2NodeAddresses, i)
		layer2Nodes[i] = node.L2NodeProvider(i+1, addresses)

		// Start node
		go layer2Nodes[i].Start(l2NodeAddresses[i])
	}

	// Register types for gob
	gob.Register(transaction.Operation{})
	gob.Register(comm.ReceiveMessage{})
	gob.Register(replication.ReplicationMessage{})

	// Start client to issue transactions
	go client.Start("client/transactions.txt", time.Second*10,
		[][]string{
			coreNodeAddresses,
			l1NodeAddresses,
			l2NodeAddresses,
		},
	)

	// Start web server and block
	web_server.Start()
}
