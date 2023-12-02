package comm

import "fmt"

// SendMessage is an interface for sending messages.
type SendMessage interface {
}

// ReceiveMessage is a struct for receiving messages.
type ReceiveMessage struct {
	Success bool // ACK
	Value   int
}

func (rm *ReceiveMessage) String() string {
	return fmt.Sprintf("ReceiveMessage -> Success: %v, Value: %d", rm.Success, rm.Value)
}
