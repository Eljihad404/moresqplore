package com.example.moresqplore.data.service;

import com.example.moresqplore.data.model.Expense;
import com.example.moresqplore.data.model.ExpenseCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for budget analysis and currency conversion
 */
public class BudgetAnalysisService {

    private ExchangeRateService exchangeRateService;

    public BudgetAnalysisService() {
        this.exchangeRateService = new ExchangeRateService();
        // Fetch rates on initialization
        exchangeRateService.fetchRates(new ExchangeRateService.ExchangeRateCallback() {
            @Override
            public void onSuccess(Map<String, Double> rates) {
                // Rates loaded successfully
            }

            @Override
            public void onFailure(Exception e) {
                // Will use fallback rates
            }
        });
    }

    /**
     * Convert amount to MAD using real exchange rates
     */
    public double convertToMAD(double amount, String currency) {
        return exchangeRateService.convertToMAD(amount, currency);
    }

    /**
     * Convert amount from MAD to target currency using real exchange rates
     */
    public double convertFromMAD(double amountInMAD, String targetCurrency) {
        return exchangeRateService.convertFromMAD(amountInMAD, targetCurrency);
    }

    /**
     * Calculate total spending by category
     */
    public Map<String, Double> calculateCategoryTotals(List<Expense> expenses) {
        Map<String, Double> totals = new HashMap<>();

        // Initialize all categories
        for (ExpenseCategory category : ExpenseCategory.values()) {
            totals.put(category.name(), 0.0);
        }

        // Sum expenses by category
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double current = totals.getOrDefault(category, 0.0);
            totals.put(category, current + expense.getAmountInMAD());
        }

        return totals;
    }

    /**
     * Calculate daily average spending
     */
    public double calculateDailyAverage(List<Expense> expenses, int durationDays) {
        if (durationDays == 0)
            return 0;
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmountInMAD();
        }
        return total / durationDays;
    }

    /**
     * Predict remaining budget based on daily average
     */
    public double predictRemainingBudget(double currentRemaining, double dailyAverage, int daysLeft) {
        return currentRemaining - (dailyAverage * daysLeft);
    }

    /**
     * Generate spending insight
     */
    public String generateInsight(double totalBudget, double totalSpent, int daysElapsed, int daysRemaining) {
        double percentSpent = (totalSpent / totalBudget) * 100;
        double percentTimeElapsed = (double) daysElapsed / (daysElapsed + daysRemaining) * 100;

        if (percentSpent < percentTimeElapsed - 10) {
            return "Great! You're spending less than expected. Keep it up!";
        } else if (percentSpent > percentTimeElapsed + 10) {
            return "Warning: You're spending faster than planned. Consider adjusting.";
        } else {
            return "You're on track with your budget. Well done!";
        }
    }

    /**
     * Get top spending category
     */
    public String getTopSpendingCategory(Map<String, Double> categoryTotals) {
        String topCategory = null;
        double maxAmount = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        return topCategory;
    }
}
