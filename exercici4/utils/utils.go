package utils

import (
	"fmt"
	"os"
	"sort"
	"strings"
)

// ExcludeElement returns a new slice with the element at index removed.
func ExcludeElement(slice []string, index int) []string {
	// Create a new slice with a length one less than the original
	newSlice := make([]string, 0, len(slice)-1)

	// Add elements before the index
	newSlice = append(newSlice, slice[:index]...)

	// Add elements after the index
	newSlice = append(newSlice, slice[index+1:]...)

	return newSlice
}

// MapToString returns a string representation of a map.
func MapToString(m map[int]int, newLineEachKVPair bool) string {
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
		if newLineEachKVPair {
			sb.WriteString("\n")
		} else {
			sb.WriteString("  ")
		}
	}

	return sb.String()
}

// WriteDataToFile writes a map to a file.
func WriteDataToFile(fileName string, data map[int]int) {
	// Create file
	f, err := os.Create(fileName)
	if err != nil {
		fmt.Println(err)
	}
	defer f.Close()

	// Convert the map to string and write it to the file
	_, err = f.WriteString(MapToString(data, true))
	if err != nil {
		fmt.Println(err)
	}
}
