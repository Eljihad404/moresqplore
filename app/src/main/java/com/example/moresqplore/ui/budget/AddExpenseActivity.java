package com.example.moresqplore.ui.budget;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Expense;
import com.example.moresqplore.data.model.ExpenseCategory;
import com.example.moresqplore.data.repository.ExpenseRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for adding/editing expenses
 */
public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etDescription, etDate, etTime;
    private ChipGroup chipGroupCurrency, chipGroupCategory, chipGroupPayment;
    private MaterialButton btnSave;

    private ExpenseRepository repository;
    private String tripId = "demo_trip"; // TODO: Get from intent
    private Calendar calendar = Calendar.getInstance();
    private com.example.moresqplore.data.model.Budget currentBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        initializeViews();
        setupListeners();

        repository = ExpenseRepository.getInstance();

        // Set current date and time
        updateDateField();
        updateTimeField();
        
        fetchCurrentBudget();
    }

    private void fetchCurrentBudget() {
        repository.fetchBudget(tripId, new ExpenseRepository.OnBudgetListener() {
            @Override
            public void onSuccess(com.example.moresqplore.data.model.Budget budget) {
                currentBudget = budget;
            }

            @Override
            public void onFailure(Exception e) {
                // Ignore, budget might not exist yet
            }
        });
    }

    private void initializeViews() {
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);

        chipGroupCurrency = findViewById(R.id.chipGroupCurrency);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipGroupPayment = findViewById(R.id.chipGroupPayment);

        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Save button
        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeField();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void updateDateField() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeField() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        etTime.setText(sdf.format(calendar.getTime()));
    }

    private void saveExpense() {
        // Validate amount
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // Get selected currency
        String currency = getSelectedCurrency();

        // Get selected category
        String category = getSelectedCategory();

        // Get description
        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            description = category; // Use category as default
        }

        // Get payment method
        String paymentMethod = getSelectedPaymentMethod();

        // Create expense
        Expense expense = new Expense(amount, category, description);
        expense.setTripId(tripId);
        expense.setCurrency(currency);
        expense.setDate(etDate.getText().toString());
        expense.setTime(etTime.getText().toString());
        expense.setPaymentMethod(paymentMethod);

        // Save to repository
        repository.addExpense(expense, new ExpenseRepository.OnExpenseListener() {
            @Override
            public void onSuccess(Expense savedExpense) {
                runOnUiThread(() -> {
                    checkAndShowAlert(savedExpense);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(AddExpenseActivity.this,
                            "Error saving expense: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void checkAndShowAlert(Expense savedExpense) {
        if (currentBudget != null) {
            double totalSpent = currentBudget.getTotalSpent() + savedExpense.getAmountInMAD();
            double totalBudget = currentBudget.getTotalBudget();

            if (totalSpent >= totalBudget) {
                // Show Alert
                new androidx.appcompat.app.AlertDialog.Builder(AddExpenseActivity.this)
                        .setTitle("⚠️ Budget Exceeded!")
                        .setMessage("You have reached or exceeded your budget limit of " + String.format(Locale.US, "%.0f MAD", totalBudget))
                        .setPositiveButton("OK", (dialog, which) -> {
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            } else {
                Toast.makeText(AddExpenseActivity.this, "Expense saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(AddExpenseActivity.this, "Expense saved!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String getSelectedCurrency() {
        int selectedId = chipGroupCurrency.getCheckedChipId();
        if (selectedId == R.id.chipUSD)
            return "USD";
        if (selectedId == R.id.chipEUR)
            return "EUR";
        if (selectedId == R.id.chipGBP)
            return "GBP";
        return "MAD";
    }

    private String getSelectedCategory() {
        int selectedId = chipGroupCategory.getCheckedChipId();
        if (selectedId == R.id.chipFood)
            return ExpenseCategory.FOOD.name();
        if (selectedId == R.id.chipTransport)
            return ExpenseCategory.TRANSPORT.name();
        if (selectedId == R.id.chipAccommodation)
            return ExpenseCategory.ACCOMMODATION.name();
        if (selectedId == R.id.chipActivities)
            return ExpenseCategory.ACTIVITIES.name();
        if (selectedId == R.id.chipShopping)
            return ExpenseCategory.SHOPPING.name();
        return ExpenseCategory.OTHER.name();
    }

    private String getSelectedPaymentMethod() {
        int selectedId = chipGroupPayment.getCheckedChipId();
        if (selectedId == R.id.chipCard)
            return "Card";
        return "Cash";
    }
}
