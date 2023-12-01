package main

import (
	"exercici4/client"
	"exercici4/node"
	"sync"
)

func main() {
	var wg sync.WaitGroup
	coreNodes := make([]*node.CoreNode, 3)
	layer1Nodes := make([]*node.Layer1Node, 2)
	layer2Nodes := make([]*node.Layer2Node, 2)

	// Initialize and start core nodes
	for i := range coreNodes {
		wg.Add(1)
		coreNodes[i] = node.CoreNodeProvider()
		go func(n *node.CoreNode) {
			defer wg.Done()
			n.Start()
		}(coreNodes[i])
	}

	// Initialize and start layer 1 nodes
	for i := range layer1Nodes {
		wg.Add(1)
		layer1Nodes[i] = node.Layer1NodeProvider()
		go func(n *node.Layer1Node) {
			defer wg.Done()
			n.Start()
		}(layer1Nodes[i])
	}

	// Initialize and start layer 2 nodes
	for i := range layer2Nodes {
		wg.Add(1)
		layer2Nodes[i] = node.Layer2NodeProvider()
		go func(n *node.Layer2Node) {
			defer wg.Done()
			n.Start()
		}(layer2Nodes[i])
	}

	// Start client to issue transactions
	wg.Add(1)
	go func() {
		defer wg.Done()
		client.Start("data/transactions.txt")
	}()

	// Wait for all nodes to finish
	wg.Wait()
}
