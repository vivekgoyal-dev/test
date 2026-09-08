#!/bin/bash
# Walks the whole system through the gateway, exactly as a client would. Nothing here talks to a
# service directly: every call goes to :8080 and is routed, authenticated and role-checked.
G=http://localhost:8080
RUN=$RANDOM
PASS=0; FAIL=0

say()  { echo; echo "--- $1"; }
val()  { python3 -c 'import sys,json;d=json.load(sys.stdin);print(eval(sys.argv[1]))' "$1"; }

# expect <code> <description> <curl args...>
expect() {
  want=$1; shift
  desc=$1; shift
  got=$(curl -s -o /tmp/sc_body.txt -w '%{http_code}' "$@")
  if [ "$got" = "$want" ]; then
    PASS=$((PASS+1)); printf '  ok   %-52s %s\n' "$desc" "$got"
  else
    FAIL=$((FAIL+1)); printf '  FAIL %-52s got %s want %s\n' "$desc" "$got" "$want"
    head -c 200 /tmp/sc_body.txt; echo
  fi
}

say "1. register the three roles"
for r in USER SHOPOWNER ADMIN; do
  lc=$(echo "$r" | tr '[:upper:]' '[:lower:]')
  curl -s -X POST $G/auth/register -H 'Content-Type: application/json' \
    -d "{\"email\":\"$lc$RUN@example.com\",\"password\":\"secret123\",\"role\":\"$r\"}" | val "d['userId']" > /tmp/sc_$r.id
  echo "  $r is userId $(cat /tmp/sc_$r.id)"
done
curl -s -X POST $G/auth/register -H 'Content-Type: application/json' \
  -d "{\"email\":\"shop2$RUN@example.com\",\"password\":\"secret123\",\"role\":\"SHOPOWNER\"}" > /dev/null

say "2. log in"
for r in USER SHOPOWNER ADMIN; do
  lc=$(echo "$r" | tr '[:upper:]' '[:lower:]')
  curl -s -X POST $G/auth/login -H 'Content-Type: application/json' \
    -d "{\"email\":\"$lc$RUN@example.com\",\"password\":\"secret123\"}" | val "d['token']" > /tmp/sc_$r.tok
done
U=$(cat /tmp/sc_USER.tok); S=$(cat /tmp/sc_SHOPOWNER.tok); A=$(cat /tmp/sc_ADMIN.tok)
S2=$(curl -s -X POST $G/auth/login -H 'Content-Type: application/json' \
  -d "{\"email\":\"shop2$RUN@example.com\",\"password\":\"secret123\"}" | val "d['token']")
echo "  four tokens issued"

say "3. gateway: token and role enforcement"
expect 401 "no token on an admin route"        $G/auth/users
expect 401 "garbage token"                     $G/auth/users -H "Authorization: Bearer not.a.jwt"
expect 403 "USER token on an admin route"      $G/auth/users -H "Authorization: Bearer $U"
expect 200 "ADMIN token on an admin route"     $G/auth/users -H "Authorization: Bearer $A"
expect 200 "browsing the catalog with no token" $G/products

say "4. the user fills in a profile"
curl -s -X POST $G/profiles/me -H "Authorization: Bearer $U" -H 'Content-Type: application/json' \
  -d '{"fullName":"Asha Rao","phone":"9812345670","gender":"F","dateOfBirth":"1996-02-20"}' | val "d"
expect 400 "a 4-digit phone is rejected" -X PUT $G/profiles/me -H "Authorization: Bearer $U" \
  -H 'Content-Type: application/json' -d '{"fullName":"Asha Rao","phone":"1234"}'

say "5. the shopowner lists two products"
P1=$(curl -s -X POST $G/products -H "Authorization: Bearer $S" -H 'Content-Type: application/json' \
  -d "{\"productName\":\"Kindle Paperwhite $RUN\",\"category\":\"electronics\",\"price\":12999,\"description\":\"6.8 inch e-reader\"}" | val "d['productId']")
P2=$(curl -s -X POST $G/products -H "Authorization: Bearer $S" -H 'Content-Type: application/json' \
  -d "{\"productName\":\"Cotton T-Shirt $RUN\",\"category\":\"apparel\",\"price\":799}" | val "d['productId']")
echo "  productIds $P1 and $P2, owned by userId $(curl -s $G/products/$P1 | val "d['ownerId']")"
expect 403 "a USER cannot add a product" -X POST $G/products -H "Authorization: Bearer $U" \
  -H 'Content-Type: application/json' -d '{"productName":"Nope","category":"x","price":1}'
expect 400 "a price of 0 fails validation" -X POST $G/products -H "Authorization: Bearer $S" \
  -H 'Content-Type: application/json' -d '{"productName":"Free","category":"x","price":0}'

say "6. one shopowner cannot touch another one's product"
expect 403 "shopowner 2 editing shopowner 1 product" -X PUT $G/products/$P1 -H "Authorization: Bearer $S2" \
  -H 'Content-Type: application/json' -d "{\"productName\":\"Stolen $RUN\",\"category\":\"electronics\",\"price\":1}"
expect 200 "shopowner 1 editing their own product" -X PUT $G/products/$P1 -H "Authorization: Bearer $S" \
  -H 'Content-Type: application/json' -d "{\"productName\":\"Kindle Paperwhite $RUN\",\"category\":\"electronics\",\"price\":11999}"
expect 403 "shopowner 2 deleting shopowner 1 product" -X DELETE $G/products/$P1 -H "Authorization: Bearer $S2"

say "7. the user fills a cart, sending NO price"
curl -s -X POST $G/cart/items -H "Authorization: Bearer $U" -H 'Content-Type: application/json' \
  -d "{\"productId\":$P1,\"quantity\":1,\"price\":1}" > /dev/null
curl -s -X POST $G/cart/items -H "Authorization: Bearer $U" -H 'Content-Type: application/json' \
  -d "{\"productId\":$P2,\"quantity\":2}" | val "[(i['productName'], i['price'], i['quantity']) for i in d['items']]"
echo "  cart total $(curl -s $G/cart -H "Authorization: Bearer $U" | val "d['totalPrice']"), and the price of 1 sent above was ignored"
expect 400 "quantity 0 is rejected" -X POST $G/cart/items -H "Authorization: Bearer $U" \
  -H 'Content-Type: application/json' -d "{\"productId\":$P1,\"quantity\":0}"
expect 400 "an unknown product cannot enter the cart" -X POST $G/cart/items -H "Authorization: Bearer $U" \
  -H 'Content-Type: application/json' -d '{"productId":999999,"quantity":1}'

say "8. remove the t-shirt"
echo "  total after removing it: $(curl -s -X DELETE $G/cart/items/$P2 -H "Authorization: Bearer $U" | val "d['totalPrice']")"

say "9. address, then checkout"
ADDR=$(curl -s -X POST $G/orders/address -H "Authorization: Bearer $U" -H 'Content-Type: application/json' \
  -d '{"fullName":"Asha Rao","mobileNumber":"9812345670","flatNumber":"12","city":"Bengaluru","state":"Karnataka","pincode":"560038"}' | val "d['addressId']")
curl -s -X POST $G/orders/checkout -H "Authorization: Bearer $U" -H 'Content-Type: application/json' \
  -d "{\"addressId\":$ADDR}" | val "[(o['orderId'], o['productName'], o['amountPaid'], o['orderStatus'], o['address']['city']) for o in d]"
ORDER=$(curl -s $G/orders/my -H "Authorization: Bearer $U" | val "d[0]['orderId']")

say "10. the cart is empty afterwards"
curl -s $G/cart -H "Authorization: Bearer $U" | val "('items', len(d['items']), 'total', d['totalPrice'])"
expect 400 "checking out an empty cart" -X POST $G/orders/checkout -H "Authorization: Bearer $U" \
  -H 'Content-Type: application/json' -d "{\"addressId\":$ADDR}"

say "11. the shopowner sees the order and ships it"
curl -s $G/orders/received -H "Authorization: Bearer $S" | val "[(o['orderId'], o['productName'], o['orderStatus']) for o in d]"
echo "  status now: $(curl -s -X PUT "$G/orders/$ORDER/status?status=SHIPPED" -H "Authorization: Bearer $S" | val "d['orderStatus']")"
expect 403 "the other shopowner moving that order" -X PUT "$G/orders/$ORDER/status?status=DELIVERED" -H "Authorization: Bearer $S2"

say "12. a shipped order can no longer be cancelled"
expect 400 "cancelling a SHIPPED order" -X PUT $G/orders/$ORDER/cancel -H "Authorization: Bearer $U"

say "13. admin sees everything, the user only their own"
expect 200 "admin GET /orders"    $G/orders -H "Authorization: Bearer $A"
expect 403 "user GET /orders"     $G/orders -H "Authorization: Bearer $U"
expect 200 "admin GET /profiles"  $G/profiles -H "Authorization: Bearer $A"

echo; echo "==================================="
echo "passed $PASS, failed $FAIL"
[ "$FAIL" -eq 0 ] || exit 1
