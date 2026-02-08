#!/bin/bash

# Build and run script for event generator

echo "Building event generator..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful!"
    echo "Starting event generator..."
    java -jar target/event-generator-1.0.0.jar
else
    echo "Build failed!"
    exit 1
fi
