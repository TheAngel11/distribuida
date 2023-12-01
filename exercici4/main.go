package main

import (
	"exercici4/client"
	"exercici4/node"
	"exercici4/web_server"
	"sync"
)

func main() {
	var wg sync.WaitGroup
	coreNodes := make([]*node.CoreNode, 3)
	layer1Nodes := make([]*node.Layer1Node, 2)
	layer2Nodes := make([]*node.Layer2Node, 2)

	// Initialize and start core nodes
	for i := range coreNodes {
		coreNodes[i] = node.CoreNodeProvider()
		go func(n *node.CoreNode) {
			n.Start()
		}(coreNodes[i])
	}

	// Initialize and start layer 1 nodes
	for i := range layer1Nodes {
		layer1Nodes[i] = node.Layer1NodeProvider()
		go func(n *node.Layer1Node) {
			n.Start()
		}(layer1Nodes[i])
	}

	// Initialize and start layer 2 nodes
	for i := range layer2Nodes {
		layer2Nodes[i] = node.Layer2NodeProvider()
		go func(n *node.Layer2Node) {
			n.Start()
		}(layer2Nodes[i])
	}

	// Once all nodes are running, start the web server
	wg.Add(1)
	go func() {
		defer wg.Done()
		web_server.Start()
	}()

	// Start client to issue transactions
	go func() {
		client.Start("client/transactions.txt")
	}()

	// Wait for all nodes to finish
	wg.Wait()
}
