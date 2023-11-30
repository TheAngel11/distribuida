package node

import "fmt"

type Layer1Node struct {
	// Define necessary fields
}

func Layer1NodeProvider() *Layer1Node {
	return &Layer1Node{}
}

func (n *Layer1Node) Start() {
	fmt.Println("Layer 1 node started")
}
