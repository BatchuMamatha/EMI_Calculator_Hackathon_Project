package com.emicalc.automation.utils;

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

    public static double firstMonthInterest(double principal, double annualRatePercent) {
        return principal * monthlyRate(annualRatePercent);
    }

    public static double firstMonthPrincipal(double principal, double annualRatePercent, int months) {
        return emi(principal, annualRatePercent, months) - firstMonthInterest(principal, annualRatePercent);
    }

    public static double totalInterest(double principal, double annualRatePercent, int months) {
        return emi(principal, annualRatePercent, months) * months - principal;
    }

    public static long parseIndianCurrency(String s) {
        if (s == null) throw new IllegalArgumentException("Currency string is null");
        String cleaned = s.replaceAll("[^0-9.\\-]", "").trim();
        if (cleaned.isEmpty()) throw new IllegalArgumentException("No numeric content in: " + s);
        return Math.round(Double.parseDouble(cleaned));
    }
}
