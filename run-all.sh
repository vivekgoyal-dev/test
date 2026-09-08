#!/bin/bash
# Starts Eureka first, then the five services. Logs land in logs/.
cd "$(dirname "$0")" || exit 1
mkdir -p logs

# build first if any jar is missing (first run on a new machine)
for s in discovery-server profile-service product-service cart-service order-service wallet-service; do
  if [ ! -f "$s/target/$s-1.0.0.jar" ]; then
    echo "building (first run, this downloads dependencies)..."
    ./mvnw -q -B -DskipTests package || exit 1
    break
  fi
done
start() {
  (cd "$1" && nohup java -jar "target/$1-1.0.0.jar" > "../logs/$1.log" 2>&1 &)
  echo "started $1"
}
start discovery-server
# give Eureka a head start so the services register on their first attempt
curl -s -o /dev/null --retry 30 --retry-delay 2 --retry-connrefused http://localhost:8761/
for s in profile-service product-service cart-service order-service wallet-service; do start "$s"; done
for p in 9081 9082 9083 9084 9085; do
  curl -s -o /dev/null --retry 40 --retry-delay 2 --retry-connrefused "http://localhost:$p/v3/api-docs" \
    && echo "up: $p"
done
echo "Eureka dashboard: http://localhost:8761"
