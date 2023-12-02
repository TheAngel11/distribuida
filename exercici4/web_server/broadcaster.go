package web_server

import (
	"fmt"
	"sort"
	"strings"
	"sync"
)

// Broadcaster struct to hold the subscribers
type Broadcaster struct {
	subscribers []chan string
	mu          sync.Mutex
}

// broadcaster is a singleton
var broadcaster Broadcaster

// Subscribe adds a new subscriber
func (b *Broadcaster) Subscribe() chan string {
	b.mu.Lock()
	defer b.mu.Unlock()

	ch := make(chan string)
	b.subscribers = append(b.subscribers, ch)
	return ch
}

// Broadcast sends a message to all subscribers
func (b *Broadcaster) Broadcast(msg string) {
	b.mu.Lock()
	defer b.mu.Unlock()

	for _, subscriber := range b.subscribers {
		subscriber <- msg
	}
}

// TriggerNodeUpdate triggers the update of a node in the websocket connection.
// If a node is updated, it must call this function to notify the web server.
func TriggerNodeUpdate(nodeId, nodeLayer int, currentData map[int]int) {
	// Build message
	msg := fmt.Sprintf("%d%d;%s", nodeLayer, nodeId, mapToString(currentData))

	// Broadcast message to all subscribers
	broadcaster.Broadcast(msg)
}

func mapToString(m map[int]int) string {
	var keys []int
	for k := range m {
		keys = append(keys, k)
	}

	// Sorting the keys for consistent output
	sort.Ints(keys)

	// Building the string
	var sb strings.Builder
	for _, k := range keys {
		sb.WriteString(fmt.Sprintf("[Key: %d, Value: %d]", k, m[k]))
		sb.WriteString("  ")
	}

	return sb.String()
}
