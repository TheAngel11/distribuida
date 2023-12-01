package main

import (
	"exercici4/client"
	"exercici4/node"
	"exercici4/web_server"
)

func main() {
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

	// Start client to issue transactions
	go func() {
		client.Start("client/transactions.txt")
	}()

	// Start web server
	web_server.Start()
}
