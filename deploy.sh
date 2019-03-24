#!/usr/bin/env bash

echo "Cleaning and building app ..."
gradle clean installDist

echo "Building docker ..."
sudo docker build -t wallet-server:1.0 --no-cache .

echo "Running docker ..."
sudo docker run --network="host" wallet-server:1.0
