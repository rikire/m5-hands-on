# PROMPTS.md — Module 5 hands-on

Prompts, AI replies and notes for Sessions 5A and 5B.

---

## Session 5A — AI-generated tests

Coverage report path is `coverage/index.html` in this starter (the handout says
`build/reports/jacoco/...`, but the Makefile writes to `coverage/`).

### Part A — Baseline coverage

From `make coverage`, 3 seed tests:

| Scope | Line | Branch |
|---|---|---|
| `TaxCalculator` | 20/41 = **48.8%** | 15/44 = **34.1%** |
| whole bundle (incl. `Order*`, never tested) | 33/83 = 39.8% | 15/48 = 31.2% |

Methods under 50% branch coverage:

- `computeVAT` — 6/14 (42.9%)
- `roundToPaise` — 0/2 (0%)
- `isEligibleForReturn` — 0/10 (0%)

`computeIncomeTax` and `applyExemption` are at exactly 50%, so not below.

Two methods tie at 0%. I took `isEligibleForReturn` because it has 10 branches
against 2 in `roundToPaise`, so there's more to actually test.

Baseline PIT (`make mutation`): 15/42 killed (36%). `isEligibleForReturn`: 0/11,
all NO_COVERAGE.

### Part B — Generate tests

Prompt (handout text, then the method source):

```
Generate JUnit 5 tests for the following method.
Rules:
- Use AAA style with explicit comments.
- One assertion per test.
- Test names of the form
  <method>_<condition>_<expected>.
- Cover: (a) null input, (b) empty input if applicable,
  (c) boundary values, (d) one representative happy path.
- Do NOT use mocks.
- Do NOT include implementation-copying assertions; each
  assertion must state a contract, not restate the code.
```

```java
    public boolean isEligibleForReturn(BigDecimal grossIncome, int ageYears) {
        if (grossIncome == null || ageYears < 0) {
            return false;
        }
        BigDecimal threshold;
        if (ageYears >= 80) {
            threshold = new BigDecimal("500000");
        } else if (ageYears >= 60) {
            threshold = new BigDecimal("300000");
        } else {
            threshold = new BigDecimal("250000");
        }
        return grossIncome.compareTo(threshold) > 0;
    }
```

AI reply (verbatim):

> `TaxCalculator` is in the default package in `src/TaxCalculator.java`, and the seed tests are in `test/TaxCalculatorTest.java`. You can put these tests in that file or in a new class next to it. I haven't compiled or run them.
>
> ```java
> import org.junit.jupiter.api.Test;
> import static org.junit.jupiter.api.Assertions.*;
>
> import java.math.BigDecimal;
>
> /**
>  * Contract of isEligibleForReturn:
>  *  - A return is required only when gross income is strictly above the
>  *    basic exemption limit for the person's age band:
>  *      under 60 -> 2.5 lakh, 60-79 -> 3 lakh, 80 and over -> 5 lakh.
>  *  - Invalid input (null income, negative age) is never eligible.
>  */
> public class TaxCalculatorEligibilityTest {
>
>     // ---------- (a) null / invalid input ----------
>
>     @Test
>     public void isEligibleForReturn_nullIncome_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(null, 40);
>
>         // Assert
>         assertFalse(eligible, "missing income must never require a return");
>     }
>
>     @Test
>     public void isEligibleForReturn_negativeAge_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal highIncome = new BigDecimal("10000000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(highIncome, -1);
>
>         // Assert
>         assertFalse(eligible, "an invalid age must be rejected even for high income");
>     }
>
>     // ---------- (b) "empty" input: zero income ----------
>
>     @Test
>     public void isEligibleForReturn_zeroIncome_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(BigDecimal.ZERO, 30);
>
>         // Assert
>         assertFalse(eligible, "no income means no filing obligation");
>     }
>
>     // ---------- (c) boundary values: income limit, under 60 ----------
>
>     @Test
>     public void isEligibleForReturn_under60IncomeExactlyAtLimit_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal atLimit = new BigDecimal("250000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(atLimit, 30);
>
>         // Assert
>         assertFalse(eligible, "income equal to the limit is not above it");
>     }
>
>     @Test
>     public void isEligibleForReturn_under60IncomeOnePaiseAboveLimit_returnsTrue() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal justAbove = new BigDecimal("250000.01");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(justAbove, 30);
>
>         // Assert
>         assertTrue(eligible, "the smallest amount above the limit triggers filing");
>     }
>
>     @Test
>     public void isEligibleForReturn_incomeAtLimitWithDifferentScale_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal atLimitTwoDecimals = new BigDecimal("250000.00");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(atLimitTwoDecimals, 30);
>
>         // Assert
>         assertFalse(eligible, "the comparison must be by numeric value, not by BigDecimal scale");
>     }
>
>     // ---------- (c) boundary values: income limit, 60-79 ----------
>
>     @Test
>     public void isEligibleForReturn_senior60to79IncomeExactlyAtLimit_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal atLimit = new BigDecimal("300000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(atLimit, 70);
>
>         // Assert
>         assertFalse(eligible, "a senior at exactly 3 lakh need not file");
>     }
>
>     @Test
>     public void isEligibleForReturn_senior60to79IncomeOnePaiseAboveLimit_returnsTrue() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal justAbove = new BigDecimal("300000.01");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(justAbove, 70);
>
>         // Assert
>         assertTrue(eligible, "a senior just above 3 lakh must file");
>     }
>
>     // ---------- (c) boundary values: income limit, 80+ ----------
>
>     @Test
>     public void isEligibleForReturn_superSeniorIncomeExactlyAtLimit_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal atLimit = new BigDecimal("500000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(atLimit, 85);
>
>         // Assert
>         assertFalse(eligible, "a super senior at exactly 5 lakh need not file");
>     }
>
>     @Test
>     public void isEligibleForReturn_superSeniorIncomeOnePaiseAboveLimit_returnsTrue() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal justAbove = new BigDecimal("500000.01");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(justAbove, 85);
>
>         // Assert
>         assertTrue(eligible, "a super senior just above 5 lakh must file");
>     }
>
>     // ---------- (c) boundary values: age band edges ----------
>     // Each income is chosen so the two sides of the age edge give different answers.
>
>     @Test
>     public void isEligibleForReturn_age59WithIncome3Lakh_returnsTrue() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal income = new BigDecimal("300000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(income, 59);
>
>         // Assert
>         assertTrue(eligible, "age 59 is still under the general 2.5 lakh limit");
>     }
>
>     @Test
>     public void isEligibleForReturn_age60WithIncome3Lakh_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal income = new BigDecimal("300000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(income, 60);
>
>         // Assert
>         assertFalse(eligible, "the senior limit applies from the 60th birthday");
>     }
>
>     @Test
>     public void isEligibleForReturn_age79WithIncome5Lakh_returnsTrue() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal income = new BigDecimal("500000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(income, 79);
>
>         // Assert
>         assertTrue(eligible, "age 79 is still under the 3 lakh senior limit");
>     }
>
>     @Test
>     public void isEligibleForReturn_age80WithIncome5Lakh_returnsFalse() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal income = new BigDecimal("500000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(income, 80);
>
>         // Assert
>         assertFalse(eligible, "the super-senior limit applies from the 80th birthday");
>     }
>
>     @Test
>     public void isEligibleForReturn_ageZeroAboveGeneralLimit_returnsTrue() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal income = new BigDecimal("250001");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(income, 0);
>
>         // Assert
>         assertTrue(eligible, "age 0 is a valid age (a minor with income) and uses the general limit");
>     }
>
>     // ---------- (d) happy path ----------
>
>     @Test
>     public void isEligibleForReturn_workingAgeTypicalSalary_returnsTrue() {
>         // Arrange
>         TaxCalculator calc = new TaxCalculator();
>         BigDecimal salary = new BigDecimal("800000");
>
>         // Act
>         boolean eligible = calc.isEligibleForReturn(salary, 35);
>
>         // Assert
>         assertTrue(eligible, "a 35-year-old earning 8 lakh must file a return");
>     }
> }
> ```
>
> - **Empty input (b):** a `BigDecimal` or an `int` can't be empty. The closest thing is zero income, so that is what I tested.
> - **Picking values:**
>   - **Income limits:** for each age band, one test sits exactly at the limit and another 0.01 above it, so a `>=` in place of `>` would fail.
>   - **Age edges:** the tests at 59/60 and 79/80 each use an income that falls between the two limits. That way each age edge changes the result. With an income far above both limits, those tests would pass even if the age cutoffs were wrong.
> - **Contract, not code:** every assertion checks a filing rule (limit per age band, "strictly above", invalid input is rejected). No test works out the limit the way the method does.
> - **Gap to ask your instructor about:** negative income with a valid age (e.g. `-1`, age 30) returns `false` today, but the rule isn't stated anywhere, so I didn't test it.
> - **Not included:** the age 0 test and the `250000.00` scale test weren't asked for. The scale test catches a switch from `compareTo` to `equals`, a common mistake with `BigDecimal`. Delete either one if you want the smallest set.

Pasted the 16 test methods into `TaxCalculatorTest.java` (not as a separate
class). `make test`: 19/19 pass.

### Part C — Read every assertion

Check for each one: is the expected value worked out from the input the way the
method does it? None are. Every assertion is a literal `true`/`false` at a fixed
amount, and the amounts (2.5 / 3 / 5 lakh by age band) are the old-regime basic
exemption limits, i.e. they come from the tax rules, not from the code. So
nothing here copies the implementation in the handout's sense.

Two things I still changed:

| Test | Problem | What I did |
|---|---|---|
| `ageZeroAboveGeneralLimit_returnsTrue` | Message says a minor with income "uses the general limit". That's a tax rule the AI made up (a minor's income is normally clubbed with the parent's). Also looked redundant next to the under-60 boundary tests. | Deleted |
| `negativeAge_returnsFalse` | Message says the age "must be rejected", but the method doesn't reject anything, it just returns `false`. | Reworded message, assertion unchanged |

15 AI tests kept. `make test`: 18/18 pass.

### Part D — Mutation testing

`make mutation` after Part C: `isEligibleForReturn` **10/11** killed.
Whole class 25/42 (60%).

Surviving mutant in my method:

- line 70, changed conditional boundary: `ageYears < 0` → `ageYears <= 0`

(Other survivors are in methods I didn't touch: boundary mutants on lines
13/20/23/26 in `computeIncomeTax`, 38/41 in `computeVAT`, 55/57 in
`applyExemption`.)

Prompt:

```
The mutant at line 70 (`ageYears < 0` -> `ageYears <= 0`) survived
the test suite. Write ONE new JUnit test that would kill this
mutant without changing any other test. Explain in one
sentence why this test kills the mutant.
```

AI reply (verbatim):

> ```java
> @Test
> public void isEligibleForReturn_ageZeroAboveThreshold_returnsTrue() {
>     TaxCalculator calc = new TaxCalculator();
>
>     boolean eligible = calc.isEligibleForReturn(new BigDecimal("300000"), 0);
>
>     assertTrue(eligible);
> }
> ```
>
> Why it kills the mutant: age 0 is the only input where `ageYears < 0` and `ageYears <= 0` give different results. The original code lets age 0 through to the 250,000 threshold and returns `true` because 300,000 is above it, but the mutant returns `false` early, so the assertion fails.
>
> This fits the existing JUnit 5 style in `/home/remi/study/PAI/hand5/m5-pub/test/TaxCalculatorTest.java`. The income has to be above 250,000. With an income at or below it, both versions return `false` and the mutant would survive again. I haven't run this test, because I was asked not to run anything.

Added it (only added AAA comments). Re-ran PIT: line 70 mutant **KILLED**,
`isEligibleForReturn` 11/11.

Funny part: this is basically the age-0 test I deleted in Part C. The reason I
gave for deleting it (the made-up "minor" rule) was fair, but "redundant" was
wrong. Age 0 is the only value that tells `< 0` from `<= 0`. This time
the test checks what the guard lets through, without claiming anything about
tax law.

### Part E — Reflect

| | Before | After |
|---|---|---|
| `TaxCalculator` line coverage | 48.8% | 68.3% |
| `TaxCalculator` branch coverage | 34.1% | 56.8% |
| `isEligibleForReturn` branch | 0/10 | 10/10 |
| Mutation score, whole class | 15/42 (36%) | 26/42 (62%) |
| Mutation score, `isEligibleForReturn` | 0/11 before any tests; 10/11 after Part C | 11/11 after the targeted test |

Mutation testing was more useful. Coverage was already 10/10 branches after
Part B and still missed the one input (age 0) that actually tells the guard
apart, and PIT pointed straight at it.


---

## Session 5B — Auto-documentation

**Note on the starter:** `m5-pub.tgz` (as downloaded on 30 Sep, Last-Modified 28 Sep)
has no `OrderApi.java`, even though the handout and the starter README both mention it.
The handout only says it's a `@RestController` with four endpoints and no JavaDoc, and the
README says it uses fake Spring annotations. So I wrote it myself on top of `OrderService`
(commit "Add OrderApi (missing from starter archive)..."):
`GET /orders`, `GET /orders/{id}`, `POST /orders`, `POST /orders/{id}/cancel`, with the
annotations and a minimal `ResponseEntity` in `src/SpringStubs.java`. No JavaDoc on it before
Part A. If the real file shows up I'll redo 5B on it.

### Part A — JavaDoc for `POST /orders`

Prompt (handout text, then the `create` method):

```
Generate JavaDoc for the following method. Rules:
- One-line summary in imperative mood.
- Describe the contract, not the implementation.
- @param for each parameter with type and constraints.
- @return with what is returned.
- @throws for each declared or unchecked exception the
  caller should be aware of.
- If any behaviour is not evident from the code, write
  "TODO" and skip.
```

AI reply (verbatim):

> I checked `src/SpringStubs.java`, `OrderService.java` and `Order.java` before writing this. The `ResponseEntity` here is a local stub, not Spring's, so `badRequest()` and `status(int, body)` do build complete responses. They wouldn't compile against real Spring.
>
> ```java
>     /**
>      * Create a new order for a customer and return it.
>      *
>      * <p>Only {@code customerId} and {@code amount} are read from the request.
>      * Any {@code id} or {@code status} the client sends is ignored: the server
>      * always assigns a new unique id and the initial status {@code "NEW"}.
>      * The call is not idempotent, so each successful call creates a separate
>      * order, even when the request is identical to an earlier one.
>      *
>      * <p>{@code customerId} is not checked against any customer registry and
>      * is stored exactly as sent, without trimming.
>      *
>      * @param req the order to create ({@link OrderDto}); must not be
>      *            {@code null}. {@code req.customerId()} must be non-null and
>      *            not blank. {@code req.amount()} must be non-null and strictly
>      *            greater than zero. There is no upper limit or scale limit
>      *            on the amount.
>      * @return a response with status {@code 201 Created} whose body is the
>      *         created order as an {@link OrderDto}, including its generated
>      *         {@code id} and status {@code "NEW"}; or {@code 400 Bad Request}
>      *         with a {@code null} body if any of the conditions on
>      *         {@code req} is not met. No {@code Location} header is set.
>      * @throws TODO exceptions raised by request-body deserialization (for
>      *         example malformed JSON or a non-numeric amount) happen outside
>      *         this method, and their mapping to HTTP responses is not visible
>      *         in this code.
>      */
> ```
>
> - **Exceptions:** the method itself declares and throws none. Every invalid input gets a 400 response instead of an exception. `OrderService.create` only writes a non-null UUID key into a `ConcurrentHashMap`, so it throws nothing either. That's why the only `@throws` is a TODO for the framework layer.
> - **The TODO line:** `@throws TODO ...` isn't a real exception type, so the javadoc tool will flag it. If that matters, turn it into a plain `TODO:` sentence in the description.
> - **Amount check:** `compareTo` ignores scale, so `0.00` is rejected and `0.001` is accepted. This is covered by "strictly greater than zero".

Pasted the JavaDoc as is.

**Verification, pass 1.** New chat, prompt from the handout + JavaDoc + method body:

```
Given the JavaDoc above and the method body below,
identify any statement in the JavaDoc that is inconsistent
with the code. Do not fix; just list.
```

AI reply (verbatim):

> None of the JavaDoc's statements contradict the code. I checked each one against `OrderApi.create` and the code it calls: `OrderService.create`, the `Order` constructor, `OrderDto.from` and the `ResponseEntity` stub in `src/SpringStubs.java`.
>
> - **Only `customerId` and `amount` are read; client `id`/`status` are ignored.** Matches: only `req.customerId()` and `req.amount()` are passed to `service.create`.
> - **New unique id and status `"NEW"`.** Matches: the id comes from `UUID.randomUUID()` and the `Order` constructor sets `"NEW"`. Strictly, a random UUID is unique only with overwhelming probability, not guaranteed.
> - **Not idempotent.** Matches: every call makes a new UUID and a new `store.put`; there is no duplicate check.
> - **`customerId` not checked against a registry and stored as sent, without trimming.** Matches: there is no lookup and no `trim()`. `isBlank()` is only a check; it doesn't change the value.
> - **`req` must be non-null; `customerId` non-null and not blank; `amount` non-null and > 0.** Matches the guard exactly. `compareTo(ZERO) <= 0` rejects zero and negatives.
> - **No upper limit or scale limit on the amount.** Matches: nothing checks either one.
> - **201 with the created order as `OrderDto`, including id and `"NEW"`.** Matches `ResponseEntity.status(201, OrderDto.from(o))`.
> - **400 with a `null` body on any failed condition.** Matches: the stub's `badRequest()` returns `new ResponseEntity<>(400, null)`.
> - **No `Location` header.** Matches: the stub has no header support, and none is set.
> - **Deserialization errors happen outside this method.** Matches: the method doesn't parse the body itself.
>
> One defect that isn't about the code's behaviour: `@throws TODO` is a malformed tag. `@throws` expects an exception type, and `TODO` isn't one, so the javadoc tool will report an error. The text under it describes something outside the method rather than an exception this method throws.
>
> One caveat: the JavaDoc promises a 400 for a `null` `req`. That holds for this code and this stub. In real Spring, `@RequestBody` is required by default, so a missing body would be rejected before the method runs and might not come back as this method's 400. This doesn't affect the answer for the code as written.

So: 0 statements that contradict the code, 1 formal problem (`@throws TODO` isn't an
exception type). The first reply had already warned about it. Fixed it by moving the
TODO into the description as a plain `TODO:` sentence and saying the method throws
nothing. `javadoc -Xdoclint:all` is clean for this method after the fix.

**Verification, pass 2.** New chat, same prompt, updated JavaDoc:

> I found no statement in the JavaDoc that contradicts the code. I checked it against `OrderApi.java`, `OrderService.java`, `Order.java`, `OrderDto.java` and `SpringStubs.java` in `/home/remi/study/PAI/hand5/m5-pub/src/`.
>
> Each claim matches the code:
> - **Only `customerId` and `amount` are read; client `id`/`status` are ignored:** `service.create(req.customerId(), req.amount())` is the only use of `req`. `OrderService.create` generates the id with `UUID.randomUUID()`, and the `Order` constructor hard-codes `status = "NEW"`.
> - **Not idempotent:** every call makes a new UUID and a new entry in the store. Nothing is deduplicated.
> - **No registry check, no trimming:** `customerId` goes unchanged into `Order`. `isBlank()` only validates it; it never changes the value.
> - **Validation and the 400 response:** a null `req`, a null or blank `customerId`, or a null amount or one `<= 0` returns `ResponseEntity.badRequest()`. In `SpringStubs` that is `new ResponseEntity<>(400, null)`, so the body is null as documented. There is no upper limit or scale check.
> - **Success response:** `ResponseEntity.status(201, OrderDto.from(o))`, where `OrderDto.from` copies the generated id and `"NEW"`.
> - **No `Location` header:** the stub `ResponseEntity` has no header support at all.
> - **Throws no exceptions:** nothing on this path throws in normal operation.
>
> These aren't contradictions, but the wording is loose in four places:
> 1. **"must not be null" on `req`:** this reads like a caller precondition. The code actually accepts null and returns 400. The `@return` text already says this, so it's a matter of emphasis.
> 2. **"unique id":** it's a random UUID. The code never checks for a collision, and `store.put` would silently overwrite an existing order. In practice a collision won't happen.
> 3. **"Throws no exceptions":** true of the code shown, but no try/catch protects it. It depends on `OrderService` and `OrderDto.from` never throwing.
> 4. **The TODO about deserialization:** it's accurate. The stubs define no error mapping, so the behaviour really isn't visible in this code.
>
> This is based on reading the code only. I didn't compile or run anything.

"No statement contradicts the code", so the loop stops here. I left the four "loose
wording" points: they're not contradictions, and point 1 is already covered by `@return`.

### Part B — README

Prompt: file tree + `Makefile` (PIT download rules truncated) + current `README.md` +
`.gitignore`, then:

```
Draft a README.md for this repository with sections:
description, build, quick example, contributing, license.
Use MIT license placeholder.
Rules:
- Do NOT invent features not present in the code.
- If a section has no evidence in the code, write "TODO" and
  skip.
- The one-line description must be a factual summary of what
  the code does, not marketing copy.
```

The draft is saved unchanged in `README.raw.md`. Notes the AI added after the draft (verbatim):

> Things you should know before using it:
>
> - **Contributing is TODO:** the repo has no CONTRIBUTING file and no contribution rules, so there was nothing to base that section on.
> - **License:** there is no `LICENSE` file either, so MIT is only a placeholder, as you asked.
> - **Title:** "m5-pub" is just the directory name. The code doesn't define a project name.
> - **No HTTP example on purpose:** a `curl` example would be made up. `OrderApi` is never started, and the `ResponseEntity` stub's getters are package-private.
> - **Unchecked details:**
>   - The requirements line says "network access" because the jar download rules in the Makefile were cut off in what you sent. I didn't check whether they use `curl` or `wget`.
>   - I didn't compile or run the example snippet. Two of its values (the 700000 income tax and the 60-year-old eligibility) are my own reading of the code, not taken from the tests.
> - **Old numbers in the current README:** it says the seed tests give about 36% mutation score and ~45%/~30% coverage. Those numbers are out of date: according to `PROMPTS.md`, the suite now has 19 tests and 26 of 42 mutants are killed. I left the numbers out of the draft so they can't go stale again.
> - **Lost content:** the draft replaces the Session 5A/5B assignment notes in the current README. If you still need them, keep them in a separate section or in `PROMPTS.md`.

How I checked it: every claim against the code with grep (`ConcurrentHashMap`,
`randomUUID`, `"NEW"`/`"CANCELLED"`, `HALF_UP`, `150000`, `409`, `@RequestMapping("/orders")`,
no `main`, no `package`, `curl` in the Makefile), and I ran the quick example in a scratch
class: 52500.00 / 180.00 / 480000 / false, all as the README says.

**Invented features deleted: none.** Every capability in the draft exists in the code.
Two small things were off:
- `POST /orders` 400 rule was incomplete (only "blank customerId or amount <= 0"; the code
  also rejects a missing body, null `customerId` and null `amount`). Rewrote the line.
- In its notes the AI says two example values are "my own reading, not taken from the
  tests". That's wrong: both 700000 and age 60 / 300000 are in the tests. The README
  sentence itself was fine.

Other edits for `README.md`: title `m5-hands-on`, one-line description in my own words,
filled the Contributing TODO (branch + PR against `main`, `make test` must pass, report
coverage/mutation changes when touching `TaxCalculator`), said where the example lines
can go. License stays an MIT placeholder, there's no copyright holder to put in.

### Part C — OpenAPI spec

Prompt: full `OrderApi.java` (with the JavaDoc from Part A), then:

```
Read the following Spring @RestController and generate an
OpenAPI 3.0 YAML spec covering:
- Every endpoint (path, method, summary from JavaDoc).
- Request body schemas for POST/PUT.
- Response schemas for 2xx and 4xx.
- Referenced DTO schemas in the components section.
If any endpoint's behaviour is unclear, add a TODO comment
in the spec at that location.
```

AI reply (verbatim, YAML omitted here):

> I checked the code in `/home/remi/study/PAI/hand5/m5-pub/src` before writing the spec. Three things there change it compared with reading the controller alone:
>
> - **`OrderDto` has four fields:** `record OrderDto(String id, String customerId, BigDecimal amount, String status)`.
> - **Cancel returns the updated order.** `OrderService` keeps orders in a map and changes them in place. So when `cancel` returns `o.get()`, that order already has status `"CANCELLED"`. The 409 happens only when the order is already `"CANCELLED"`.
> - **This is not real Spring.** `SpringStubs.java` supplies its own annotations and `ResponseEntity`, and nothing turns them into HTTP. The spec describes how the API is meant to behave; no running server was checked against it. With real Spring, `notFound()` and `badRequest()` would also need `.build()`.
>
> Only `create` has JavaDoc. The summaries for the other three endpoints come from the code, and each has a TODO saying so.
>
> [... YAML spec, saved unchanged as `openapi.yaml` in commit "Add AI-generated openapi.yaml" ...]
>
> Choices you may want to change:
> - **No separate request schema.** POST uses `OrderDto` directly, with `id` and `status` marked `readOnly`. That matches the code. A dedicated `CreateOrderRequest` with only `customerId` and `amount` would be clearer for API clients; it's a one-line switch if you prefer it.
> - **No error schema.** Every 4xx returns an empty body in this code, so the spec defines no error schema.
>
> I made no file changes.

Checked it:

- Paths and methods vs the annotations: all 4 match (`GET/POST /orders`,
  `GET /orders/{id}`, `POST /orders/{id}/cancel`).
- `$ref`s: `schemas/OrderDto`, `parameters/OrderId`, both defined in `components`.
- 2xx + 4xx per method: `GET /orders` had only `200`. The controller never returns a 4xx
  there, so I added a `4XX` response by hand and said in its description that it's
  framework-level (e.g. unsupported `Accept`), not from `list()`.
- Also fixed: the AI put `# TODO` lines inside the `info.description: |` block. Inside a
  block scalar that's text, not a comment, so Swagger UI would show "# TODO ..." in the
  API description. Moved them out as a real YAML comment.
- `redocly lint --extends=spec`: valid. With the default "recommended" rules: 4×
  `security-defined` and `no-empty-servers`. I didn't add either, because the code has
  no auth and no server URL, so it would be made up. Added `license: MIT` to `info`.
- Rendered in Swagger UI (`swaggerapi/swagger-ui` in docker, spec mounted): title
  "Orders API", OAS 3.0, all 4 operations with the right summaries, `OrderDto` under
  Schemas, no error panel.

### Part D — Reflect

- **JavaDoc self-check, first pass:** 0 inconsistencies with the code. It flagged 1
  problem, but a formal one (`@throws TODO` isn't a valid tag), not a wrong claim.
- **README invented features:** 0. The AI could open the repo and clearly did, which
  probably explains it. The only factual slip was an incomplete 400 rule.
- **Most hand editing: README.** Counting edits: README 5 (title, description, 400 rule,
  Contributing, example note), OpenAPI 3 (`4XX`, misplaced TODO, license), JavaDoc 1.
  The README edits were mostly about writing what the AI correctly refused to guess
  (description in my words, Contributing). The OpenAPI fixes were fewer but more
  important. The misplaced TODO looked fine in the YAML and only showed up as garbage
  in the rendered page.
