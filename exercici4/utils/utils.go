package utils

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
