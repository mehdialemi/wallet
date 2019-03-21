#!/usr/bin/env bash

echo "Cleaning and building app ..."
./gradlew clean installDist

echo "Building docker ..."
docker build -t wallet-server:1.0 --no-cache .

echo "Running docker ..."
docker run --network="host" wallet-server:1.0
