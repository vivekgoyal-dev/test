#!/bin/bash
# End-to-end walk through the EShoppingZone services. Run it with all five services up.
set -e
PROFILE=http://localhost:9081; PRODUCT=http://localhost:9082
CART=http://localhost:9083;    ORDER=http://localhost:9084; WALLET=http://localhost:9085
j() { python3 -c 'import sys,json;d=json.load(sys.stdin);print(eval(sys.argv[1]))' "d$1"; }
say() { echo; echo "--- $1"; }
RUN=$$                       # H2 keeps data between runs, soeach run uses its own customer + products
MOBILE=$((9990000000 + RANDOM))
P1="Kindle Paperwhite $RUN"; P2="Cotton T-Shirt $RUN"

say "1. register a customer"
CID=$(curl -s -X POST $PROFILE/profiles/customer -H 'Content-Type: application/json' -d '{
  "fullName":"Test Customer","emailId":"test@example.com","mobileNumber":'$MOBILE',
  "gender":"F","dateOfBirth":"1998-04-12","password":"secret",
  "addresses":[{"houseNumber":12,"streetName":"MG Road","colonyName":"Indiranagar","city":"Bengaluru","state":"Karnataka","pincode":560038}]}' | j "['profileId']")
echo "customerId=$CID"

say "2. merchant lists two products"
curl -s -X POST $PRODUCT/products -H 'Content-Type: application/json' -d '{
  "productName":"'"$P1"'","productType":"Electronics","category":"electronics","price":12999,
  "description":"6.8 inch e-reader","image":["kindle.jpg"],"specification":{"storage":"16GB"}}' | j "['productId']" > /dev/null
curl -s -X POST $PRODUCT/products -H 'Content-Type: application/json' -d '{
  "productName":"'"$P2"'","productType":"Apparel","category":"apparel","price":799,
  "description":"Plain crew neck"}' | j "['productId']" > /dev/null
echo "browse by category:"; curl -s $PRODUCT/products/category/electronics | j "[0]['productName']"

say "3. open a cart and add both products"
curl -s -X POST $CART/carts/$CID > /dev/null
curl -s -X PUT $CART/carts -H 'Content-Type: application/json' -d "{\"cartId\":$CID,\"items\":[
  {\"productName\":\"$P1\",\"quantity\":1},{\"productName\":\"$P2\",\"quantity\":2}]}"
echo; echo "cart total (priced by product-service, not by the caller):"; curl -s $CART/carts/$CID/total

say "4. remove the t-shirt (send the list the cart should hold)"
curl -s -X PUT $CART/carts -H 'Content-Type: application/json' -d "{\"cartId\":$CID,\"items\":[
  {\"productName\":\"$P1\",\"quantity\":1}]}" | j "['totalPrice']"

say "5. store a delivery address"
curl -s -X POST $ORDER/orders/address -H 'Content-Type: application/json' -d "{\"customerId\":$CID,
  \"fullName\":\"Test Customer\",\"mobileNumber\":\"$MOBILE\",\"flatNumber\":12,
  \"city\":\"Bengaluru\",\"pincode\":560038,\"state\":\"Karnataka\"}" | j "['addressId']"

say "6. checkout: cash on delivery"
CARTJSON=$(curl -s $CART/carts/$CID)
echo "$CARTJSON" | curl -s -X POST $ORDER/orders/place -H 'Content-Type: application/json' -d @- | j "[0]"

say "7. wallet: open, top up 20000"
curl -s -X POST $WALLET/wallets/$CID > /dev/null
curl -s -X PUT "$WALLET/wallets/$CID/add?amount=20000" | j "['currentBalance']"

say "8. checkout: wallet payment"
echo "$CARTJSON" | curl -s -X POST $ORDER/orders/onlinePayment -H 'Content-Type: application/json' -d @- | j "[0]['modeOfPayment']"
echo "balance after paying 12999:"; curl -s $WALLET/wallets/$CID

say "9. statements"
curl -s $WALLET/wallets/$CID/statements | python3 -c 'import sys,json
for s in json.load(sys.stdin): print(" ", s["transactionType"], s["amount"], "orderId=", s["orderId"])'

say "10. previous orders"
curl -s $ORDER/orders/customer/$CID | python3 -c 'import sys,json
for o in json.load(sys.stdin): print(" ", o["orderId"], o["product"]["productName"], o["ammountPaid"], o["modeOfPayment"], o["orderStatus"])'

say "11. merchant moves the order to Shipped"
LAST=$(curl -s $ORDER/orders/latest | j "['orderId']")
curl -s -X PUT "$ORDER/orders/$LAST/status?status=Shipped" | j "['orderStatus']"

say "12. NEGATIVE: wallet payment with an empty wallet must not create an order"
BEFORE=$(curl -s $ORDER/orders | python3 -c "import sys,json;print(len(json.load(sys.stdin)))")
echo "$CARTJSON" | curl -s -o /tmp/ez_fail.txt -w "http=%{http_code}\n" -X POST $ORDER/orders/onlinePayment -H 'Content-Type: application/json' -d @-
cat /tmp/ez_fail.txt | head -c 200; echo
AFTER=$(curl -s $ORDER/orders | python3 -c "import sys,json;print(len(json.load(sys.stdin)))")
echo "orders before=$BEFORE after=$AFTER"
[ "$BEFORE" = "$AFTER" ] && echo "PASS: failed payment left no order" || { echo "FAIL: order survived a failed payment"; exit 1; }

say "13. NEGATIVE: unknown product cannot enter a cart"
curl -s -o /dev/null -w "http=%{http_code} (expect 400)\n" -X PUT $CART/carts -H 'Content-Type: application/json' \
  -d "{\"cartId\":$CID,\"items\":[{\"productName\":\"No Such Thing\",\"quantity\":1}]}"

say "14. NEGATIVE: duplicate mobile number is rejected"
curl -s -o /dev/null -w "http=%{http_code} (expect 409)\n" -X POST $PROFILE/profiles/customer \
  -H 'Content-Type: application/json' -d '{"fullName":"Someone Else","mobileNumber":'$MOBILE'}'

echo; echo "ALL CHECKS DONE"
