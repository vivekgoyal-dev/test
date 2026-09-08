#!/bin/bash
pkill -f "target/discovery-server-1.0.0.jar"
for s in profile-service product-service cart-service order-service wallet-service; do
  pkill -f "target/$s-1.0.0.jar"
done
echo stopped
