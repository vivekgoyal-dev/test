#!/bin/bash
# Starts everything. The SERVER_PORT overrides exist only because ports 8081 and 8082 are taken
# on this machine; application.yml keeps the ports the plan specifies.
cd "$(dirname "$0")" || exit 1
mkdir -p logs
start() {
  (cd "$1" && SERVER_PORT="$2" nohup java -jar "target/$1-1.0.0.jar" > "../logs/$1.log" 2>&1 &)
  echo "started $1 on $2"
}
start discovery-server 8761
curl -s -o /dev/null --retry 30 --retry-delay 2 --retry-connrefused http://localhost:8761/
start auth-service    9091
start profile-service 9092
start product-service 9093
start cart-service    9094
start order-service   9095
start api-gateway     8080
