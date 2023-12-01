package main

import (
	"exercici4/client"
	"exercici4/node"
	"exercici4/utils"
	"exercici4/web_server"
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
		coreNodes[i] = node.CoreNodeProvider(addresses, l1NodeAddresses)

		// Start node
		i := i // New variable to avoid the closure problem
		go func(n *node.CoreNode) {
			n.Start(coreNodeAddresses[i])
		}(coreNodes[i])
	}

	// Initialize and start layer 1 nodes
	for i := range layer1Nodes {
		layer1Nodes[i] = node.L1NodeProvider(l2NodeAddresses)

		// Start node
		i := i // New variable to avoid the closure problem
		go func(n *node.L1Node) {
			n.Start(l1NodeAddresses[i])
		}(layer1Nodes[i])
	}

	// Initialize and start layer 2 nodes
	for i := range layer2Nodes {
		layer2Nodes[i] = node.L2NodeProvider()

		// Start node
		i := i // New variable to avoid the closure problem
		go func(n *node.L2Node) {
			n.Start(l2NodeAddresses[i])
		}(layer2Nodes[i])
	}

	// Start client to issue transactions
	go func() {
		client.Start("client/transactions.txt")
	}()

	// Start web server
	web_server.Start()
}
