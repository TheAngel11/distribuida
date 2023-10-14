#!/bin/bash

# Get the directory of the current script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Change to the script's directory
cd "$DIR"

# Compile the LamportMutex.java file
javac LamportMutex.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    # Run the Main class
    java Main.java
else
    echo "Compilation failed!"
fi
