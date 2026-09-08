# Shopping Cart - Microservices Project Plan

Backend only, tested from Swagger and Postman. No frontend.

Seven Spring Boot projects, three roles, JWT login through an API gateway, Feign for
service-to-service calls, validation on every request body, and AOP logging in every service.
Everything below is decided, so this can be built as it reads.

---

## 1. The services

| # | Project | Port | Owns |
|---|---|---|---|
| 1 | discovery-server | 8761 | Eureka registry. Everyone registers here |
| 2 | api-gateway | 8080 | The only door in. Checks the JWT and the role, then forwards |
| 3 | auth-service | 8081 | Logins, passwords, roles. Issues the JWT |
| 4 | profile-service | 8082 | User profiles |
| 5 | product-service | 8083 | The catalog |
| 6 | cart-service | 8084 | One cart per user |
| 7 | order-service | 8085 | Orders, addresses, order status |

**Only two services call another service, and both use Feign:**

```
cart-service  ->  product-service    to get the real price of an item
order-service ->  cart-service       to read the cart at checkout, then empty it
```

Everything else is self-contained. Keeping it to two calls is deliberate: every extra call is
another thing that can be down while you are demoing.

Feign turns a call into an interface. You declare what you want and Spring writes the HTTP code,
resolving the service name through Eureka:

```java
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductDto getProductById(@PathVariable int id);
}
```

Then the service class just calls `productClient.getProductById(id)`. Add
`spring-cloud-starter-openfeign` to cart-service and order-service, and `@EnableFeignClients` on
their main class. Only those two need it.

**The user record is split, with nothing duplicated:**

- **auth-service** holds userId, email, password hash, role, active
- **profile-service** holds the same userId with no password, plus name, phone, gender, date of birth

The userId comes from the JWT, so neither service ever asks the other for anything.

---

## 2. The three roles

| Role | Can do |
|---|---|
| **USER** | Register, log in, keep a profile, browse products, manage own cart, place and view own orders |
| **SHOPOWNER** | Everything a USER can, plus add, edit and delete **their own** products and update the status of orders for those products |
| **ADMIN** | See all users, products and orders. Delete any product. Cannot place orders |

Two rules that are easy to miss and expensive to add later:

- **A SHOPOWNER can only touch their own products.** Every product stores an `ownerId` taken from
  the token, and edit and delete compare it against the caller. Without this, any shopowner can
  delete a competitor's catalog.
- **A USER only ever sees their own cart, profile and orders.** The user id comes from the token,
  never from a path variable the caller can change.

---

## 3. The eight folders, in every service

Package root is `com.shoppingcart.<service>`.

```
src/main/java/com/shoppingcart/product/
├── aop/             the logging aspect
├── config/          Swagger, Feign config, and in auth-service the security config
├── controller/      endpoints only, no logic
├── dto/             what the API accepts and returns
├── exception/       custom exceptions + one @RestControllerAdvice
├── model/           the JPA entities
├── repository/      Spring Data interfaces
└── service/         the rules, interface + Impl
```

- **controller** takes the request, calls one service method, returns the response. An `if` doing
  business logic belongs in service.
- **service** holds every rule and is the only layer that throws business exceptions.
- **dto** is separate from **model** so a password can be accepted at registration and never
  appear in a response.
- **exception** holds one `@RestControllerAdvice`, so an error is clean JSON instead of a stack
  trace.

No ninth folder, and that decides two things:

- auth-service's `SecurityConfig`, `JwtUtil` and JWT filter go in **config/**, and the gateway's
  filter goes in its **config/** too.
- The **Feign client interfaces go in service/**, next to the service classes that use them. A
  Feign interface is how this service talks to another one, so it belongs with the rules, not in a
  folder of its own.

---

## 4. How login works

1. `POST /auth/register` with email, password and role. auth-service hashes the password with
   BCrypt and saves it.
2. `POST /auth/login` returns a JWT holding the user id, email and role, signed with a secret,
   valid 24 hours.
3. Every later call sends `Authorization: Bearer <token>`.
4. **The gateway checks the token once.** Bad or expired gives 401. Wrong role for that route gives
   403. Otherwise it adds two headers and forwards:

   ```
   X-User-Id: 42
   X-User-Role: SHOPOWNER
   ```
5. **Services just read those headers.** No service parses a JWT. Spring Security and jjwt exist in
   auth-service only.

This is the main simplification in the whole plan. A JWT filter in all seven projects means seven
copies of the same secret and the same bug. One filter at the gateway is one place to fix it.

**Not in v1:** refresh tokens, password reset, email verification, logout, token blacklisting. All
can be added later without changing anything above.

---

## 5. Controller, validation and errors

These three go together: the controller only receives, validation rejects bad input before any rule
runs, and the advice turns whatever is thrown into one consistent JSON shape.

**The controller does four things and nothing else.** Read the headers, call one service method,
wrap the result in a `ResponseEntity`, return it. No queries, no business `if`.

```java
@PostMapping
public ResponseEntity<ProductDto> addProduct(@Valid @RequestBody ProductDto dto,
                                             @RequestHeader("X-User-Id") int userId) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.addProduct(dto, userId));
}
```

**Validation lives on the DTO**, so the rules sit next to the fields they guard and every
controller taking that DTO gets them for free. Add `spring-boot-starter-validation`, then annotate:

```java
public class ProductDto {

    @NotBlank(message = "product name is required")
    private String productName;

    @NotBlank(message = "category is required")
    private String category;

    @Positive(message = "price must be greater than 0")
    private double price;

    @Size(max = 2000, message = "description is too long")
    private String description;
}
```

The one that actually matters is `@Valid` on the parameter. Without it the annotations above are
decoration and nothing is checked. The common ones you will need: `@NotBlank` and `@NotNull`,
`@Email` on register, `@Size(min = 6)` on a password, `@Positive` on a price, `@Min(1)` on a cart
quantity.

**One `@RestControllerAdvice` per service**, in `exception/`, catching three things:

| Thrown | Becomes |
|---|---|
| `MethodArgumentNotValidException` (a `@Valid` failure) | 400 with a field-by-field list of what is wrong |
| Your own `ResourceNotFoundException`, `UnauthorizedException` | 404 or 403 with the message |
| Anything else | 500 with a generic message, never a stack trace |

Every error, from every service, comes back in the same shape:

```json
{
  "status": 400,
  "message": "validation failed",
  "errors": { "price": "price must be greater than 0" },
  "timestamp": "2026-09-08T18:24:11"
}
```

Getting this right once per service is what stops a demo from ever showing a Whitelabel Error Page.

---

## 6. AOP logging

One class in `aop/` per service, around forty lines, and no log statement anywhere in the business
code.

```java
@Around("execution(* com.shoppingcart.product.controller..*(..)) || " +
        "execution(* com.shoppingcart.product.service..*(..))")
```

It logs the method, the arguments, the time taken and the result. On an exception it logs and
rethrows.

```
INFO  ProductController.addProduct(..) called by user 42 [SHOPOWNER]
INFO  ProductController.addProduct(..) returned in 84ms
ERROR ProductServiceImpl.deleteProduct(..) threw NotFoundException: no product 91
```

Two things to get right:

- **Never log the arguments of login and register.** They hold plain-text passwords. Exclude that
  controller from the pointcut.
- **Rethrow, never swallow.** An aspect that catches and hides makes bugs invisible.

Add `spring-boot-starter-aop`. It is not in the Initializr default set.

---

## 7. The endpoints

This is also the gateway's route table.

**auth-service** `/auth/**`

| Method | Path | Role |
|---|---|---|
| POST | `/auth/register` | public |
| POST | `/auth/login` | public |
| GET | `/auth/users` | ADMIN |

**profile-service** `/profiles/**`

| Method | Path | Role |
|---|---|---|
| GET | `/profiles/me` | any logged in |
| POST | `/profiles/me` | any logged in |
| PUT | `/profiles/me` | any logged in |
| GET | `/profiles` | ADMIN |

**product-service** `/products/**`

| Method | Path | Role |
|---|---|---|
| GET | `/products` | public |
| GET | `/products/{id}` | public |
| GET | `/products/category/{category}` | public |
| POST | `/products` | SHOPOWNER |
| PUT | `/products/{id}` | SHOPOWNER, own only |
| DELETE | `/products/{id}` | SHOPOWNER own only, ADMIN any |
| GET | `/products/my` | SHOPOWNER |

**cart-service** `/cart/**` - the cart id is the caller's user id, so it is in no path

| Method | Path | Role |
|---|---|---|
| GET | `/cart` | USER |
| POST | `/cart/items` | USER |
| PUT | `/cart/items/{productId}` | USER |
| DELETE | `/cart/items/{productId}` | USER |
| DELETE | `/cart` | USER |

**order-service** `/orders/**`

| Method | Path | Role |
|---|---|---|
| POST | `/orders/address` | USER |
| POST | `/orders/checkout` | USER |
| GET | `/orders/my` | USER |
| GET | `/orders/received` | SHOPOWNER |
| PUT | `/orders/{id}/status` | SHOPOWNER |
| GET | `/orders` | ADMIN |

Checkout sends only an address id. order-service reads the cart from cart-service, writes one order
row per item, then empties the cart. It stores its own copy of the address, because an old order
must still be correct after the customer edits their address.

---

## 8. Build order

Each phase ends with something you can click in Swagger. Nothing moves on until it passes, so you
are never debugging six services at once.

| Phase | Build | Done when |
|---|---|---|
| 0 | MySQL running, five schemas created (section 10) | `SHOW DATABASES;` lists all five |
| 1 | discovery-server | Dashboard at 8761 opens and says no instances |
| 2 | auth-service | Register, log in, and see the role inside the token on jwt.io |
| 3 | api-gateway | No token gives 401, a USER token on an ADMIN route gives 403, a good token gets through |
| 4 | profile-service | A user creates and reads their own profile, and cannot read anyone else's |
| 5 | product-service | A SHOPOWNER adds a product, a USER browsing works, a USER adding gets 403, a second shopowner cannot edit the first one's product, and posting a blank name gives a 400 naming the field |
| 6 | cart-service | A USER adds an item and the price comes back from product-service over Feign, not from the request |
| 7 | order-service | Checkout creates the orders and empties the cart, the shopowner sees it and moves it to Shipped |
| 8 | `aop/` logging in all five | Console shows entry, exit and timing for every call |
| 9 | Swagger and Postman | Every endpoint above runs from Postman in one pass |

**Validation and the exception advice are not a phase.** They are written with each service, in
phases 4 to 7, because a service without them is not finished. Only the AOP logger is deferred: it
is one file per service and takes an hour at the end, whereas fighting it while a service is
half-built wastes a day.

---

## 9. Ground rules

- Spring Boot 3.3.x, Java 17, same version everywhere.
- Per service: Spring Web, Spring Data JPA, **MySQL Driver**, Eureka Discovery Client, Lombok, **Validation**,
  **AOP**, springdoc. cart-service and order-service add **OpenFeign**. auth-service adds Spring
  Security and jjwt. api-gateway takes Spring Cloud Gateway and Eureka client only, no JPA.
- **`@Valid` on every `@RequestBody`**, and one `@RestControllerAdvice` in every service.
- **One MySQL schema per service.** No service reads another service's tables. See section 10.
- Every cross-service call uses the service name, not a port.
- **Never take a price from the client.** cart-service fetches it from product-service. This is the
  most common flaw in a cart project.
- Same error shape everywhere: status, message, timestamp.

---

## 10. Database

MySQL, with **one schema per service**. A service connects to its own schema and to nothing else,
which is what keeps them independent: product-service cannot accidentally join onto the orders
table, because it cannot see it.

Create the five schemas once, before the first run:

```sql
CREATE DATABASE shopcart_auth;
CREATE DATABASE shopcart_profile;
CREATE DATABASE shopcart_product;
CREATE DATABASE shopcart_cart;
CREATE DATABASE shopcart_order;
```

Then in each service's `application.yml`, pointing at its own schema:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shopcart_product?createDatabaseIfNotExist=true
    username: root
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

Three things worth agreeing on now:

- **`ddl-auto: update` is for development only.** Hibernate creates and alters tables to match the
  entities, which is exactly what you want while building and exactly what you do not want on a
  real server. It never drops a column, so a renamed field leaves the old one behind.
- **The password does not go in the yml.** Use an environment variable, as above. A database
  password committed to git is the single most common mistake in a project like this.
- **Same MySQL server, five schemas.** Five separate servers would be more correct and is not worth
  it here. The isolation that matters is that no service reads another's tables, and separate
  schemas give you that.

The dependency is `mysql-connector-j`, which Spring Initializr lists as **MySQL Driver**.

---

## 11. Not in v1

Wallet or payment, so orders are cash on delivery. Refresh tokens and password reset. Product
images. Ratings and reviews. Stock levels. Config server and centralised logging.

Each can be added later without changing the seven projects above, which is the point of stopping
here.
