package transaction

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

type Type int

type Transaction struct {
	Layer      int
	Operations []Operation
}
type Operation struct {
	// If the operation is a read, then WriteKeyValue is nil
	ReadKey       int
	WriteKeyValue []int // [0] = key, [1] = value
}

func NewTransaction(line string) *Transaction {
	t := &Transaction{}
	t.Parse(line)
	return t
}

// Parse parses a transaction from a string.
func (t *Transaction) Parse(line string) {
	parts := strings.Split(line, ", ") // Split line by ", " (comma and space)

	// If the first part is "b", then it is a readWrite transaction and the layer is 0
	// If not, then it is a read-only transaction and the layer is parts[0][2]
	if parts[0] != "b" {
		t.Layer = int(parts[0][2] - '0') // Convert from ASCII rune to int
	}

	// Parse read and write keys
	for _, part := range parts[1 : len(parts)-1] {
		var key, value int

		// If the part is "r", then it is a read operation
		if _, err := fmt.Sscanf(part, "r(%d)", &key); err == nil {
			t.Operations = append(t.Operations, Operation{ReadKey: key})
			continue
		}

		// If the part is "w", then it is a write operation
		if _, err := fmt.Sscanf(part, "w(%d,%d)", &key, &value); err == nil {
			t.Operations = append(t.Operations, Operation{WriteKeyValue: []int{key, value}})
			continue
		}

		fmt.Println("Error parsing part:", part)
	}
}

// ReadTransactionsFromFile reads transactions from a file.
func ReadTransactionsFromFile(filePath string) ([]*Transaction, error) {
	// Open file
	file, err := os.Open(filePath)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	// Read all transactions from file
	var transactions []*Transaction
	scanner := bufio.NewScanner(file)

	// For each line, create a new transaction
	for scanner.Scan() {
		transactions = append(transactions, NewTransaction(scanner.Text()))
	}

	// Check for errors
	if err := scanner.Err(); err != nil {
		return nil, err
	}

	// Return transactions read from file
	return transactions, nil
}
func (o *Operation) String() string {
	if o.WriteKeyValue == nil {
		return fmt.Sprintf("Operation -> r(%d)", o.ReadKey)
	}

	return fmt.Sprintf("Operation -> w(%d,%d)", o.WriteKeyValue[0], o.WriteKeyValue[1])
}
