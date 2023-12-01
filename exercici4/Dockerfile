# Start from the official Golang image
FROM golang:alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the Go Modules manifests
COPY go.mod go.sum ./

# Copy the source code into the container
COPY . .

# Download module dependencies
RUN go mod download

# Expose port 8080
EXPOSE 8080

# Run the application using `go run`
CMD ["go", "run", "."]