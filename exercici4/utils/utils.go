package utils

// ExcludeElement returns a new slice with the element at index removed.
func ExcludeElement(slice []string, index int) []string {
	return append(slice[:index], slice[index+1:]...)
}
