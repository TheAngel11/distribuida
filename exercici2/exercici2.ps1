# Get the directory of the current script
$DIR = Split-Path $script:MyInvocation.MyCommand.Path

# Change to the script's directory
Set-Location -Path $DIR

# Check if out directory exists, if not create it
if (-not (Test-Path -Path ".\out")) {
    New-Item -ItemType Directory -Path ".\out"
}

# Compile the LamportMutex.java file into the .\out directory
javac -d .\out LamportMutex.java

# Compile the RicartAgrawalaMutex.java file into the .\out directory
javac -d .\out RicartAgrawalaMutex.java

# Copy the Main.java, HeavyWeightProcess and LightweightProcess file into the .\out directory
Copy-Item -Path "Main.java", "HeavyWeightProcess.java", "LightweightProcess.java" -Destination ".\out"

# Check if compilation was successful
if ($?) {
    # Change directory
    Set-Location -Path ".\out"
    # Run the Main class
    java Main
} else {
    Write-Host "Compilation failed!"
}