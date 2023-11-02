#!/bin/bash

# Get the directory of the current script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Change to the script's directory
cd "$DIR"

# Check if out directory exists, if not create it
if [ ! -d "./out" ]; then
    mkdir out
fi

# Compile the LamportMutex.java file into the ./out directory
javac -d ./out LamportMutex.java

# Compile the RicartAgrawalaMutex.java file into the ./out directory
javac -d ./out RicartAgrawalaMutex.java

# Copy the Main.java, HeavyWeightProcess and LightweightProcess file into the ./out directory
cp Main.java ./out
cp HeavyweightProcess.java ./out
cp LightweightProcess.java ./out

# Check if compilation was successful
if [ $? -eq 0 ]; then
    # Change directory
    cd ./out
    # Run the Main class
    java Main.java
else
    echo "Compilation failed!"
fi
