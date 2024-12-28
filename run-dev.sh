#!/bin/bash

# Kill background processes on script exit
trap 'kill $(jobs -p)' EXIT

# Set environment variables
export API_URL="http://localhost:8080"
export WATCH_DIR="$HOME/Music"  # Default music directory, can be overridden

# Start Backend
echo "Starting Backend..."
cd backend
./mvnw spring-boot:run &

# Wait for backend to be ready
echo "Waiting for backend to start..."
sleep 10

# Start Frontend
echo "Starting Frontend..."
cd ../frontend
npm install
npm run serve &

# Start Daemon
echo "Starting Daemon..."
cd ../daemon
npm install
node index.js --watch-dir="$WATCH_DIR" --api-url="$API_URL" &

# Keep script running
wait 