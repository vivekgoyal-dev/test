# EShoppingZone

Backend for the e-commerce project in `Shopping Cart System.pdf`: five independent Spring Boot
services behind a Eureka registry. No frontend, no login — every endpoint is open, so it is
driven from Swagger UI or Postman.

## Stack

Java 17, Spring Boot 3.3.4, Spring Cloud 2023.0.3 (Eureka + LoadBalancer), Spring Data JPA,
H2 (file-backed, one database per service), Lombok, springdoc-openapi.

## The services

| Service | Port | Swagger UI | Owns |
|---|---|---|---|
| discovery-server | 8761 | – | Eureka registry ([dashboard](http://localhost:8761)) |
| profile-service | 9081 | http://localhost:9081/swagger-ui.html | customers, merchants, delivery agents |
| product-service | 9082 | http://localhost:9082/swagger-ui.html | catalog, categories, ratings, reviews |
| cart-service | 9083 | http://localhost:9083/swagger-ui.html | carts and their items |
| order-service | 9084 | http://localhost:9084/swagger-ui.html | orders, delivery addresses, checkout |
| wallet-service | 9085 | http://localhost:9085/swagger-ui.html | wallet balance and statements |

Ports are 9081-9085 rather than 8081-8085 because 8081 was already taken on this machine.

Only two services talk to another service, both over `RestTemplate` resolved through Eureka:

    cart-service  -> product-service   price every item at the source
    order-service -> wallet-service    debit the wallet at checkout

## Run it

    ./run-all.sh                # builds on the first run, then starts Eureka and the five services
    ./smoketest.sh              # walks the whole purchase flow and asserts the results
    ./stop-all.sh

Only a JDK 17+ is needed — `./mvnw` fetches Maven itself. See "Setting it up on a fresh machine"
below. Each service also runs on its own: `cd product-service && ../mvnw spring-boot:run`.
The H2 databases are files under `<service>/data/`; delete them for a clean slate.
Browse them at e.g. http://localhost:9082/h2-console (JDBC URL `jdbc:h2:file:./data/productdb`,
user `sa`, no password).

## Setting it up on a fresh machine

**Only a JDK is needed.** Maven comes down through the wrapper (`./mvnw`), and the database is an
embedded H2 file — nothing to install, no server to configure.

1. Install **JDK 17 or newer** and check it:

       java -version        # must say 17 or higher

   macOS: `brew install openjdk@17` · Windows/Linux: [Temurin 17](https://adoptium.net/temurin/releases/?version=17)

2. Get the project folder and open a terminal inside it (the folder with `pom.xml` and `mvnw`).

3. Start everything:

       ./run-all.sh

   The first run builds all six modules and downloads dependencies, so give it a few minutes.
   It prints `up: 9081` … `up: 9085` when the services are live.

4. Open http://localhost:9082/swagger-ui.html, or import
   `EShoppingZone.postman_collection.json` into Postman and work down it in order.

5. Check the whole flow really works:

       ./smoketest.sh       # ends with ALL CHECKS DONE

6. Stop:

       ./stop-all.sh

### On Windows

`run-all.sh`, `stop-all.sh` and `smoketest.sh` are bash scripts — run them from **Git Bash**
(ships with Git for Windows). From plain PowerShell or cmd, start each service in its own
terminal instead, Eureka first:

    mvnw.cmd -DskipTests package
    cd discovery-server && ..\mvnw.cmd spring-boot:run
    cd profile-service  && ..\mvnw.cmd spring-boot:run
    ...and so on for product-, cart-, order- and wallet-service

### If a port is already taken

The services use 8761 and 9081-9085. If something else on the machine holds one of those, the
service logs `Port XXXX was already in use` in `logs/<service>.log`. Change `server.port` in that
service's `src/main/resources/application.yml`, or start it with an override:

    java -jar profile-service/target/profile-service-1.0.0.jar --server.port=9091

Only cart-service and order-service call other services, and they resolve them through Eureka by
name, so changing a port needs no edits anywhere else.

### Anything odd?

- Services appear on the Eureka dashboard (http://localhost:8761) about 30 seconds after start.
  Cross-service calls (cart pricing, wallet payment) need that registration, so give it a moment.
- Every service logs to `logs/<service>.log` — that is the first place to look.
- Fresh start on data: `./stop-all.sh`, delete the `*/data/` folders, start again.

## The flow it implements

1. Register a customer — `POST /profiles/customer` (also `/merchant`, `/delivery`).
2. Merchant lists products — `POST /products`. Browse with `GET /products/category/{category}`,
   `/products/type/{type}`, `/products/name/{name}`.
3. Open a cart — `POST /carts/{cartId}`. The cart id is the customer's profile id.
4. Add or remove items — `PUT /carts` with the full item list the cart should hold.
   Send only `productName` and `quantity`: cart-service asks product-service for the id and
   price, so a client cannot set its own prices.
5. Store a delivery address — `POST /orders/address`.
6. Check out, either way:
   - `POST /orders/place` — cash on delivery
   - `POST /orders/onlinePayment` — pays from the wallet
   Both take the cart as the body. One order row is written per cart line.
7. Wallet — `POST /wallets/{id}` to open, `PUT /wallets/{id}/add?amount=` to top up,
   `GET /wallets/{id}` for the balance, `GET /wallets/{id}/statements` for the passbook.
8. History — `GET /orders/customer/{customerId}`, `PUT /orders/{orderId}/status?status=Shipped`.

## Things worth knowing

- **A failed wallet payment leaves no order.** `onlinePayment` writes the orders, then debits the
  wallet inside the same transaction; if the debit is refused the transaction rolls back.
  Step 12 of `smoketest.sh` asserts it.
- **Every balance change writes a statement row.** The balance is never moved on its own.
- **Prices are never taken from the caller** — cart-service overwrites whatever price is posted.
- Errors come back as normal HTTP codes: 404 unknown id, 409 duplicate mobile/product,
  400 bad quantity, empty cart, unknown product, insufficient balance.

## Not built (say the word)

- The cart is not emptied after checkout — re-posting the same cart places the order again.
- Ratings and reviews are stored on a product but no endpoint adds one review at a time.
- No authentication, so any caller can act as any customer id. The PDF's GitHub login and the
  Thymeleaf `UserController` are out of scope here.
- Order cancellation only changes `orderStatus`; nothing refunds the wallet.
