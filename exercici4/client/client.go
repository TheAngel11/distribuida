package client

import (
	"exercici4/transaction"
	"fmt"
	"time"
)

func Start(filePath string) {
	// Read transactions from file
	transactions, err := transaction.ReadTransactionsFromFile(filePath)
	if err != nil {
		fmt.Println("Error reading transactions:", err)
		return
	}

	// For each transaction, send it to a core node
	for _, t := range transactions {
		err := SendTransactionToCoreNode(t)
		if err != nil {
			fmt.Println("Error sending transaction:", err)
			continue
		}
		time.Sleep(time.Second)
	}
}

func SendTransactionToCoreNode(t *transaction.Transaction) error {
	fmt.Println("Sending transaction:", t)
	return nil
}
