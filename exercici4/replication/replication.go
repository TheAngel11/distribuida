package replication

import (
	"encoding/gob"
	"exercici4/comm"
	"fmt"
	"net"
)

type ReplicationMessage struct {
	Data               map[int]int
	IsFirstReplication bool
}

// SendHashmapToNode sends a hashmap to a node
func SendHashmapToNode(hashmap map[int]int, nodeAddress string, isFirstReplication bool) {
	fmt.Println("Sending hashmap to node", nodeAddress)

	conn, err := net.Dial("tcp", nodeAddress)
	if err != nil {
		fmt.Println(err)
		return
	}
	defer conn.Close()

	// Implicit conversion from Operation to SendMessage
	var msgToSend comm.SendMessage = ReplicationMessage{Data: hashmap, IsFirstReplication: isFirstReplication}

	encoder := gob.NewEncoder(conn)
	err = encoder.Encode(&msgToSend)
	if err != nil {
		fmt.Println(err)
		return
	}

	// Wait for response
	decoder := gob.NewDecoder(conn)
	var response comm.ReceiveMessage
	err = decoder.Decode(&response)

	if err != nil {
		fmt.Println(err)
		return
	}
}
