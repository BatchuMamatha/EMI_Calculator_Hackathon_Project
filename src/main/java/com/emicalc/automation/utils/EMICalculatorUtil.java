package com.emicalc.automation.utils;

/**
 * Pure-Java EMI math used by tests to independently compute expected values
 * before comparing against the values shown on emicalculator.net.
 *
 * Formula:
 *   E = P * r * (1+r)^n / ((1+r)^n - 1)
 * where
 *   r = annualRate / 12 / 100
 *   n = tenure in months
 */
public final class EMICalculatorUtil {

    private EMICalculatorUtil() {}

    public static double monthlyRate(double annualRatePercent) {
        return annualRatePercent / 12.0 / 100.0;
    }

    public static double emi(double principal, double annualRatePercent, int months) {
        double r = monthlyRate(annualRatePercent);
        double pow = Math.pow(1 + r, months);
        return principal * r * pow / (pow - 1);
    }

    /** Interest portion of the FIRST month's EMI. */
    public static double firstMonthInterest(double principal, double annualRatePercent) {
        return principal * monthlyRate(annualRatePercent);
    }

    /** Principal portion of the FIRST month's EMI. */
    public static double firstMonthPrincipal(double principal, double annualRatePercent, int months) {
        return emi(principal, annualRatePercent, months) - firstMonthInterest(principal, annualRatePercent);
    }

    /** Total interest across the entire tenure. */
    public static double totalInterest(double principal, double annualRatePercent, int months) {
        return emi(principal, annualRatePercent, months) * months - principal;
    }

    /**
     * Strip Indian currency formatting and parse to a numeric value.
     * Examples handled: "₹ 1,31,524", "1,31,524", "₹1,500,000", "44,986", "₹ 0".
     */
    public static long parseIndianCurrency(String s) {
        if (s == null) throw new IllegalArgumentException("Currency string is null");
        String cleaned = s.replaceAll("[^0-9.\\-]", "").trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("No numeric content in: " + s);
        }
        return (long) Math.round(Double.parseDouble(cleaned));
    }
}
