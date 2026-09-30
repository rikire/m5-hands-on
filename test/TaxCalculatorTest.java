import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Three seed tests. Coverage is intentionally light:
 *  - computeIncomeTax: only the middle slab branch is hit
 *  - computeVAT: only the 18% rate is hit
 *  - applyExemption: only the "under cap" branch is hit
 *  - roundToPaise: not exercised
 *  - isEligibleForReturn: not exercised at all
 */
public class TaxCalculatorTest {

    @Test
    public void incomeTax_middleSlab() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal tax = c.computeIncomeTax(new BigDecimal("700000"));
        // 12500 (5% on 250k-500k) + 40000 (20% on 500k-700k) = 52500
        assertEquals(0, tax.compareTo(new BigDecimal("52500.00")));
    }

    @Test
    public void vat_eighteenPercent() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal gst = c.computeVAT(new BigDecimal("1000"), 18);
        assertEquals(0, gst.compareTo(new BigDecimal("180.00")));
    }

    @Test
    public void exemption_underCap() {
        TaxCalculator c = new TaxCalculator();
        BigDecimal net = c.applyExemption(
                new BigDecimal("600000"), new BigDecimal("120000"));
        assertEquals(0, net.compareTo(new BigDecimal("480000")));
    }

    // ===== isEligibleForReturn (AI-generated, see PROMPTS.md 5A Part B) =====

    // ---------- (a) null / invalid input ----------

    @Test
    public void isEligibleForReturn_nullIncome_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();

        // Act
        boolean eligible = calc.isEligibleForReturn(null, 40);

        // Assert
        assertFalse(eligible, "missing income must never require a return");
    }

    @Test
    public void isEligibleForReturn_negativeAge_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal highIncome = new BigDecimal("10000000");

        // Act
        boolean eligible = calc.isEligibleForReturn(highIncome, -1);

        // Assert
        assertFalse(eligible, "a negative age is invalid input, so no filing obligation is reported even for high income");
    }

    // ---------- (b) "empty" input: zero income ----------

    @Test
    public void isEligibleForReturn_zeroIncome_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();

        // Act
        boolean eligible = calc.isEligibleForReturn(BigDecimal.ZERO, 30);

        // Assert
        assertFalse(eligible, "no income means no filing obligation");
    }

    // ---------- (c) boundary values: income limit, under 60 ----------

    @Test
    public void isEligibleForReturn_under60IncomeExactlyAtLimit_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal atLimit = new BigDecimal("250000");

        // Act
        boolean eligible = calc.isEligibleForReturn(atLimit, 30);

        // Assert
        assertFalse(eligible, "income equal to the limit is not above it");
    }

    @Test
    public void isEligibleForReturn_under60IncomeOnePaiseAboveLimit_returnsTrue() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal justAbove = new BigDecimal("250000.01");

        // Act
        boolean eligible = calc.isEligibleForReturn(justAbove, 30);

        // Assert
        assertTrue(eligible, "the smallest amount above the limit triggers filing");
    }

    @Test
    public void isEligibleForReturn_incomeAtLimitWithDifferentScale_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal atLimitTwoDecimals = new BigDecimal("250000.00");

        // Act
        boolean eligible = calc.isEligibleForReturn(atLimitTwoDecimals, 30);

        // Assert
        assertFalse(eligible, "the comparison must be by numeric value, not by BigDecimal scale");
    }

    // ---------- (c) boundary values: income limit, 60-79 ----------

    @Test
    public void isEligibleForReturn_senior60to79IncomeExactlyAtLimit_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal atLimit = new BigDecimal("300000");

        // Act
        boolean eligible = calc.isEligibleForReturn(atLimit, 70);

        // Assert
        assertFalse(eligible, "a senior at exactly 3 lakh need not file");
    }

    @Test
    public void isEligibleForReturn_senior60to79IncomeOnePaiseAboveLimit_returnsTrue() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal justAbove = new BigDecimal("300000.01");

        // Act
        boolean eligible = calc.isEligibleForReturn(justAbove, 70);

        // Assert
        assertTrue(eligible, "a senior just above 3 lakh must file");
    }

    // ---------- (c) boundary values: income limit, 80+ ----------

    @Test
    public void isEligibleForReturn_superSeniorIncomeExactlyAtLimit_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal atLimit = new BigDecimal("500000");

        // Act
        boolean eligible = calc.isEligibleForReturn(atLimit, 85);

        // Assert
        assertFalse(eligible, "a super senior at exactly 5 lakh need not file");
    }

    @Test
    public void isEligibleForReturn_superSeniorIncomeOnePaiseAboveLimit_returnsTrue() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal justAbove = new BigDecimal("500000.01");

        // Act
        boolean eligible = calc.isEligibleForReturn(justAbove, 85);

        // Assert
        assertTrue(eligible, "a super senior just above 5 lakh must file");
    }

    // ---------- (c) boundary values: age band edges ----------
    // Each income is chosen so the two sides of the age edge give different answers.

    @Test
    public void isEligibleForReturn_age59WithIncome3Lakh_returnsTrue() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal income = new BigDecimal("300000");

        // Act
        boolean eligible = calc.isEligibleForReturn(income, 59);

        // Assert
        assertTrue(eligible, "age 59 is still under the general 2.5 lakh limit");
    }

    @Test
    public void isEligibleForReturn_age60WithIncome3Lakh_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal income = new BigDecimal("300000");

        // Act
        boolean eligible = calc.isEligibleForReturn(income, 60);

        // Assert
        assertFalse(eligible, "the senior limit applies from the 60th birthday");
    }

    @Test
    public void isEligibleForReturn_age79WithIncome5Lakh_returnsTrue() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal income = new BigDecimal("500000");

        // Act
        boolean eligible = calc.isEligibleForReturn(income, 79);

        // Assert
        assertTrue(eligible, "age 79 is still under the 3 lakh senior limit");
    }

    @Test
    public void isEligibleForReturn_age80WithIncome5Lakh_returnsFalse() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal income = new BigDecimal("500000");

        // Act
        boolean eligible = calc.isEligibleForReturn(income, 80);

        // Assert
        assertFalse(eligible, "the super-senior limit applies from the 80th birthday");
    }

    // ---------- (d) happy path ----------

    @Test
    public void isEligibleForReturn_workingAgeTypicalSalary_returnsTrue() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();
        BigDecimal salary = new BigDecimal("800000");

        // Act
        boolean eligible = calc.isEligibleForReturn(salary, 35);

        // Assert
        assertTrue(eligible, "a 35-year-old earning 8 lakh must file a return");
    }

    // Kills PIT mutant L70 `ageYears < 0` -> `ageYears <= 0` (see PROMPTS.md 5A Part D)
    @Test
    public void isEligibleForReturn_ageZeroAboveThreshold_returnsTrue() {
        // Arrange
        TaxCalculator calc = new TaxCalculator();

        // Act
        boolean eligible = calc.isEligibleForReturn(new BigDecimal("300000"), 0);

        // Assert
        assertTrue(eligible);
    }
}
