#!/usr/bin/env bash

echo "Building app ..."
./gradlew installDist
echo "Building docker ..."
docker build -t wallet-server:1.0 .
docker run --network="host" wallet-server:1.0
