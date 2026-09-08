# Shopping Cart - Spring Boot Microservices

Seven Spring Boot applications behind an API gateway: three roles, JWT login, Feign for
service-to-service calls, validation on every request body, and AOP logging in each service.
Backend only, driven from Postman or Swagger. No frontend.

**Full documentation with architecture diagrams, real request and response examples and
step-by-step setup: [`DOCUMENTATION.html`](DOCUMENTATION.html)** (open it in a browser).

## The projects

| Project | Port | Owns |
|---|---|---|
| `discovery-server` | 8761 | Eureka registry |
| `api-gateway` | 8080 | The only door in. Validates the JWT, checks the role, forwards |
| `auth-service` | 8081 | Logins, password hashes, roles. Issues the JWT |
| `profile-service` | 8082 | User profiles |
| `product-service` | 8083 | The catalog, one owner per product |
| `cart-service` | 8084 | One cart per user |
| `order-service` | 8085 | Orders, addresses, order status |

Two services call another service, both over Feign, both resolved through Eureka by name:
cart asks product for prices, order reads the cart and the product owner at checkout.

## Running it

**You need:** JDK 17+, MySQL 8, and Eclipse with Spring Tools 4 plus the Lombok agent installed
(run `java -jar lombok.jar` from your `.m2` repository, point it at Eclipse, restart).

**1. Create the five schemas**

```sql
CREATE DATABASE shopcart_auth;
CREATE DATABASE shopcart_profile;
CREATE DATABASE shopcart_product;
CREATE DATABASE shopcart_cart;
CREATE DATABASE shopcart_order;
```

**2. Tell the services your MySQL password.** They read it from an environment variable, so it is
not committed anywhere:

```bash
setx DB_PASSWORD "your-mysql-root-password"      # Windows, then restart Eclipse
export DB_PASSWORD="your-mysql-root-password"    # macOS or Linux
```

**3. Import and start.** In Eclipse: File > Import > Maven > Existing Maven Projects, select this
folder, tick all seven. Then Run As > Spring Boot App, starting `discovery-server` first, the five
services next, and `api-gateway` last.

From a terminal instead:

```bash
./run-local.sh      # starts all seven, logs in logs/
./smoketest.sh      # drives the whole system through the gateway
./stop-local.sh
```

Wait about 30 seconds after startup and check http://localhost:8761. All six applications should be
listed. Cross-service calls do not work until they are.

## Using it

Everything goes through **http://localhost:8080**.

```bash
POST /auth/register   {"email":"you@example.com","password":"secret123","role":"SHOPOWNER"}
POST /auth/login      {"email":"you@example.com","password":"secret123"}
```

Copy the `token` from the login response and send it as `Authorization: Bearer <token>` on every
later call. In Postman put it under Authorization > Bearer Token once and it applies to the folder.

Each service also serves its own Swagger page, for example
http://localhost:8083/swagger-ui.html. Swagger talks to a service directly and so bypasses the
gateway, which means no role check and no `X-User-Id` header. For anything involving a logged-in
user, use Postman against :8080.

## What is verified

`./smoketest.sh` walks registration, login, profile, listing products, filling a cart, checkout,
shipping and every rejection in between. Last run: **19 checks, 0 failures**, including:

- one shopowner cannot edit or delete another shopowner's product
- the cart ignores a client-supplied price and uses the one product-service reports
- checkout empties the cart, and a second checkout returns 400
- a shipped order can no longer be cancelled, and the wrong shopowner cannot ship it

## Notes

- Ports 8081 to 8085 are the defaults in `application.yml`. `run-local.sh` overrides them on this
  machine because 8081 and 8082 were already taken; the gateway finds services through Eureka by
  name, so a port change needs no other edit.
- The shell scripts need Git Bash on Windows. Eclipse does not need them at all.
- `ddl-auto: update` creates the tables for you. That is right for development and wrong for a real
  server.

## Not built

Wallet or payment, so orders are cash on delivery. Refresh tokens, password reset, email
verification, logout. Product images. Ratings and reviews. Stock levels. Config server and
centralised logging.
