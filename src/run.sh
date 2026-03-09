#!/bin/bash
cd /Users/ismaelahamada/Documents/Cours/Cours_L3/Semestre_6/Reseaux/TP4/src
rm -f latence.csv
rm -f server.log

java Codeur > server.log 2>&1 &
SERVER_PID=$!
echo "Server started with PID: $SERVER_PID"
sleep 2

for n in 1 2 10 100 1000 5000; do
    echo "Testing latency with n=$n clients (immediate close)..."
    java Stress1 $n true true latence.csv
    sleep 2
done

echo "Stopping server..."
kill $SERVER_PID
echo "Tests completed. Content of latence.csv:"
cat latence.csv
