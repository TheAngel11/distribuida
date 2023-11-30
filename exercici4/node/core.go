package node

import "fmt"

type CoreNode struct {
	// Define necessary fields
}

func CoreNodeProvider() *CoreNode {
	return &CoreNode{}
}

func (n *CoreNode) Start() {
	fmt.Println("Core node started")
}
