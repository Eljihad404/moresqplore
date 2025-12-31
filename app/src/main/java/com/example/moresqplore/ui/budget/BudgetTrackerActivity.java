package com.example.moresqplore.ui.budget;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Budget;
import com.example.moresqplore.data.model.Expense;
import com.example.moresqplore.data.repository.ExpenseRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

/**
 * Activity for tracking budget and expenses
 */
public class BudgetTrackerActivity extends AppCompatActivity {

    private TextView tvTripName, tvTotalBudget, tvSpent, tvRemaining;
    private ProgressBar progressBudget;
    private RecyclerView recyclerViewExpenses;
    private View emptyState;
    private FloatingActionButton fabAddExpense;

    private ExpenseAdapter adapter;
    private ExpenseRepository repository;

    private String tripId = "demo_trip"; // TODO: Get from intent
    private Budget currentBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_tracker);

        initializeViews();
        setupRecyclerView();
        setupListeners();

        repository = ExpenseRepository.getInstance();

        // Create demo budget if needed
        // createDemoBudget(); // REMOVED: Now using user input
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from AddExpenseActivity
        loadData();
    }

    private void initializeViews() {
        tvTripName = findViewById(R.id.tvTripName);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvSpent = findViewById(R.id.tvSpent);
        tvRemaining = findViewById(R.id.tvRemaining);
        progressBudget = findViewById(R.id.progressBudget);
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        emptyState = findViewById(R.id.emptyState);
        fabAddExpense = findViewById(R.id.fabAddExpense);
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter();
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(adapter);

        adapter.setOnExpenseClickListener(new ExpenseAdapter.OnExpenseClickListener() {
            @Override
            public void onExpenseClick(Expense expense) {
                // TODO: Open edit expense
                Toast.makeText(BudgetTrackerActivity.this,
                        "Edit: " + expense.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExpenseDelete(Expense expense) {
                showDeleteConfirmation(expense);
            }
        });
    }

    private void setupListeners() {
        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(BudgetTrackerActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });
    }

    private void loadData() {
        // Load budget
        repository.fetchBudget(tripId, new ExpenseRepository.OnBudgetListener() {
            @Override
            public void onSuccess(Budget budget) {
                currentBudget = budget;
                runOnUiThread(() -> updateBudgetDisplay());
            }

            @Override
            public void onFailure(Exception e) {
                // No budget found, prompt user to create one
                runOnUiThread(() -> showSetBudgetDialog());
            }
        });

        // Load expenses
        repository.fetchExpenses(tripId, new ExpenseRepository.OnExpensesLoadedListener() {
            @Override
            public void onSuccess(List<Expense> expenses) {
                runOnUiThread(() -> {
                    adapter.setExpenses(expenses);
                    emptyState.setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    // Show detailed error for debugging
                    Toast.makeText(BudgetTrackerActivity.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    
                    // Also log it
                    android.util.Log.e("BudgetTracker", "Error loading expenses", e);
                });
            }
        });
    }

    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Trip Budget");
        builder.setMessage("Enter the total budget for this trip (in MAD):");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Set Budget", (dialog, which) -> {
            String budgetStr = input.getText().toString();
            if (!budgetStr.isEmpty()) {
                double amount = Double.parseDouble(budgetStr);
                createNewBudget(amount);
            } else {
                Toast.makeText(BudgetTrackerActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                showSetBudgetDialog(); // Show again
            }
        });

        builder.setCancelable(false); // Force user to set budget
        builder.show();
    }

    private void createNewBudget(double amount) {
        Budget budget = new Budget(amount, 3); // Default 3 days duration for now
        budget.setTripId(tripId);
        budget.setTripName("My Trip"); // Could be dynamic
        // Set default dates or today
        budget.setStartDate("2024-01-01"); 
        budget.setEndDate("2024-01-03");

        repository.saveBudget(budget, new ExpenseRepository.OnBudgetListener() {
            @Override
            public void onSuccess(Budget savedBudget) {
                currentBudget = savedBudget;
                runOnUiThread(() -> {
                    updateBudgetDisplay();
                    Toast.makeText(BudgetTrackerActivity.this, "Budget set successfully!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(BudgetTrackerActivity.this, "Failed to save budget", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateBudgetDisplay() {
        if (currentBudget == null)
            return;

        tvTripName.setText(currentBudget.getTripName());
        tvTotalBudget.setText(String.format(Locale.US, "%.0f MAD", currentBudget.getTotalBudget()));
        tvSpent.setText(String.format(Locale.US, "%.0f MAD", currentBudget.getTotalSpent()));
        tvRemaining.setText(String.format(Locale.US, "%.0f MAD", currentBudget.getRemaining()));

        progressBudget.setProgress(currentBudget.getProgressPercentage());
    }

    private void showDeleteConfirmation(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> deleteExpense(expense))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpense(Expense expense) {
        repository.deleteExpense(expense.getId(), tripId, new ExpenseRepository.OnDeleteListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(BudgetTrackerActivity.this,
                            "Expense deleted", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(BudgetTrackerActivity.this,
                            "Error deleting expense", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
