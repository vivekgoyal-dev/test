#!/bin/bash
for s in discovery-server auth-service profile-service product-service cart-service order-service api-gateway; do
  pkill -f "target/$s-1.0.0.jar"
done
echo stopped
