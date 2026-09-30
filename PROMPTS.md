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
