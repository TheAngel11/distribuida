package node

import "fmt"

type Layer2Node struct {
	// Define necessary fields
}

func Layer2NodeProvider() *Layer2Node {
	return &Layer2Node{}
}

func (n *Layer2Node) Start() {
	fmt.Println("Layer 2 node started")
}
