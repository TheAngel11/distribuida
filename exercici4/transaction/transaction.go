package transaction

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

type Type int

const (
	ReadOnly Type = iota
	Write
)

type Transaction struct {
	Type      Type
	Layer     int
	ReadKeys  []int
	WriteKeys map[int]int
}

func NewTransaction(line string) *Transaction {
	t := &Transaction{
		WriteKeys: make(map[int]int),
	}
	t.Parse(line)
	return t
}

// Parse parses a transaction from a string.
func (t *Transaction) Parse(line string) {
	parts := strings.Split(line, ",")

	// If the first part is "b", then it is a write transaction
	if parts[0] == "b" {
		t.Type = Write
	} else {
		t.Type = ReadOnly
		t.Layer = int(parts[0][1] - '0') // Convert from ASCII rune to int
	}

	// Parse read and write keys
	for _, part := range parts[1 : len(parts)-1] {
		if t.Type == ReadOnly {
			var key int
			if _, err := fmt.Sscanf(part, "r(%d)", &key); err == nil {
				t.ReadKeys = append(t.ReadKeys, key)
			}
		} else {
			var key, value int
			if _, err := fmt.Sscanf(part, "w(%d,%d)", &key, &value); err == nil {
				t.WriteKeys[key] = value
			}
		}
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
