# m5-pub

Java 17 teaching project: a simplified Indian income-tax and GST calculator (`TaxCalculator`) with JUnit 5 tests, plus an in-memory order service (`OrderService`) behind a REST-style controller (`OrderApi`) that uses stand-in Spring annotations and does not run as a web server.

## Description

- `TaxCalculator` (amounts in INR, `BigDecimal`):
  - `computeIncomeTax(income)`: simplified old-regime slabs. 0% up to 2,50,000; 5% up to 5,00,000; 20% up to 10,00,000; 30% above that. Throws `IllegalArgumentException` if `income` is null or negative.
  - `computeVAT(amount, gstRatePercent)`: GST at 0, 5, 12, 18 or 28 percent, rounded HALF_UP to 2 decimals. Any other rate throws `IllegalArgumentException`.
  - `applyExemption(taxableIncome, exemption)`: subtracts the exemption, capped at 1,50,000. The result never goes below 0.
  - `roundToPaise(amount)`: rounds HALF_UP to 2 decimals.
  - `isEligibleForReturn(grossIncome, ageYears)`: `true` if income is strictly above 2,50,000 (under 60), 3,00,000 (60 to 79) or 5,00,000 (80 and over). Returns `false` for null income or a negative age.
- `OrderService`: keeps orders in memory in a `ConcurrentHashMap`. It can list, find by id, create (UUID id, status `NEW`) and cancel (status `CANCELLED`). Cancelling an order that is missing or already cancelled returns `false`.
- `OrderApi`: a controller mapped to `/orders`:
  - `GET /orders`
  - `GET /orders/{id}`: 404 if not found
  - `POST /orders`: 201; 400 if `customerId` is blank or `amount <= 0`
  - `POST /orders/{id}/cancel`: 404 if not found, 409 if already cancelled

  The annotations and `ResponseEntity` are minimal stand-ins in `src/SpringStubs.java`. Spring is not on the classpath, so nothing serves these endpoints over HTTP.
- `test/TaxCalculatorTest.java`: JUnit 5 tests for `TaxCalculator`. There are no tests for the order classes.

## Build

Requirements: a JDK that supports `--release 17`, `make`, and network access the first time you run each target (the JUnit, JaCoCo and PIT jars are downloaded into `libs/`).

```sh
make deps        # download the JUnit console standalone jar
make build       # compile src/ and test/ into build/
make test        # run all tests with the JUnit console launcher
make coverage    # JaCoCo report: coverage/index.html and coverage/report.xml
make mutation    # PIT mutation report for TaxCalculator: build/reports/pitest/index.html
make clean       # delete build/, libs/, coverage/, jacoco.exec
```

## Quick example

There is no `main` method. All classes are in the default package, so the calling code has to be in the default package too, the way the tests are. The expected values below are taken from the existing tests.

```java
import java.math.BigDecimal;

TaxCalculator c = new TaxCalculator();
c.computeIncomeTax(new BigDecimal("700000"));        // 52500.00 (12500 + 40000)
c.computeVAT(new BigDecimal("1000"), 18);            // 180.00
c.applyExemption(new BigDecimal("600000"),
                 new BigDecimal("120000"));          // 480000
c.isEligibleForReturn(new BigDecimal("300000"), 60); // false (limit is 3,00,000 at age 60-79)
```

## Contributing

TODO

## License

MIT. TODO: add a `LICENSE` file with the copyright holder and year.
