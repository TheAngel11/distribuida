package client

import (
	"encoding/gob"
	"exercici4/comm"
	"exercici4/transaction"
	"fmt"
	"log"
	"math/rand"
	"net"
	"time"
)

func Start(filePath string, timeToSleep time.Duration, nodes [][]string) {
	// Read transactions from file
	transactions, err := transaction.ReadTransactionsFromFile(filePath)
	if err != nil {
		log.Fatal("Error reading transactions:", err)
	}

	for _, t := range transactions {
		err := sendTransactionToNode(t, nodes)
		if err != nil {
			fmt.Println("Error sending transaction:", err)
			continue
		}
		time.Sleep(timeToSleep)
	}
}

func sendTransactionToNode(t *transaction.Transaction, nodes [][]string) error {
	// Calculate random node to send the operations
	randomNode := rand.Intn(len(nodes[t.Layer]))

	// Send each operation to the node
	for _, operation := range t.Operations {
		err := sendOperationToNode(operation, nodes[t.Layer][randomNode])
		if err != nil {
			return err
		}

		// Sleep for 2 seconds
		time.Sleep(2 * time.Second)
	}

	return nil
}

func sendOperationToNode(operation transaction.Operation, nodeAddress string) error {
	fmt.Println("Sending operation to node:", operation.String(), nodeAddress)

	conn, err := net.Dial("tcp", nodeAddress)
	if err != nil {
		return err
	}
	defer conn.Close()

	// Implicit conversion from Operation to SendMessage
	var msgToSend comm.SendMessage = operation

	encoder := gob.NewEncoder(conn)
	err = encoder.Encode(&msgToSend)
	if err != nil {
		return err
	}

	// Wait for response
	decoder := gob.NewDecoder(conn)
	var response comm.ReceiveMessage
	err = decoder.Decode(&response)

	if err != nil {
		return err
	}
	fmt.Println("Response:", response.String())

	return nil
}
