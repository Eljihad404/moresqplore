package com.example.moresqplore.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moresqplore.data.model.Budget;
import com.example.moresqplore.data.model.Expense;
import com.example.moresqplore.data.service.BudgetAnalysisService;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing expense and budget data
 */
public class ExpenseRepository {

    private static volatile ExpenseRepository instance;

    private final FirebaseFirestore firestore;
    private final CollectionReference expensesRef;
    private final CollectionReference budgetsRef;
    private final BudgetAnalysisService analysisService;

    private final MutableLiveData<List<Expense>> expenses = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Budget> currentBudget = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private ExpenseRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.expensesRef = firestore.collection("expenses");
        this.budgetsRef = firestore.collection("budgets");
        this.analysisService = new BudgetAnalysisService();
    }

    public static ExpenseRepository getInstance() {
        if (instance == null) {
            synchronized (ExpenseRepository.class) {
                if (instance == null) {
                    instance = new ExpenseRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Expense>> getExpenses() {
        return expenses;
    }

    public LiveData<Budget> getCurrentBudget() {
        return currentBudget;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    /**
     * Add expense
     */
    public void addExpense(Expense expense, OnExpenseListener listener) {
        // Convert to MAD
        expense.setAmountInMAD(analysisService.convertToMAD(expense.getAmount(), expense.getCurrency()));

        expensesRef.add(expense)
                .addOnSuccessListener(documentReference -> {
                    expense.setId(documentReference.getId());

                    // Update budget
                    updateBudgetSpending(expense.getTripId());

                    if (listener != null) {
                        listener.onSuccess(expense);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Delete expense
     */
    public void deleteExpense(String expenseId, String tripId, OnDeleteListener listener) {
        expensesRef.document(expenseId).delete()
                .addOnSuccessListener(aVoid -> {
                    updateBudgetSpending(tripId);
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Fetch expenses for a trip
     */
    public void fetchExpenses(String tripId, OnExpensesLoadedListener listener) {
        isLoading.postValue(true);

        expensesRef.whereEqualTo("tripId", tripId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Expense> expenseList = queryDocumentSnapshots.toObjects(Expense.class);
                    expenses.postValue(expenseList);
                    isLoading.postValue(false);

                    if (listener != null) {
                        listener.onSuccess(expenseList);
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.postValue(false);
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Create or update budget
     */
    public void saveBudget(Budget budget, OnBudgetListener listener) {
        if (budget.getId() == null) {
            budgetsRef.add(budget)
                    .addOnSuccessListener(documentReference -> {
                        budget.setId(documentReference.getId());
                        currentBudget.postValue(budget);
                        if (listener != null) {
                            listener.onSuccess(budget);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) {
                            listener.onFailure(e);
                        }
                    });
        } else {
            budgetsRef.document(budget.getId()).set(budget)
                    .addOnSuccessListener(aVoid -> {
                        currentBudget.postValue(budget);
                        if (listener != null) {
                            listener.onSuccess(budget);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) {
                            listener.onFailure(e);
                        }
                    });
        }
    }

    /**
     * Fetch budget for a trip
     */
    public void fetchBudget(String tripId, OnBudgetListener listener) {
        budgetsRef.whereEqualTo("tripId", tripId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Budget budget = queryDocumentSnapshots.getDocuments().get(0).toObject(Budget.class);
                        currentBudget.postValue(budget);
                        if (listener != null) {
                            listener.onSuccess(budget);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure(new Exception("No budget found"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Update budget spending totals
     */
    private void updateBudgetSpending(String tripId) {
        fetchExpenses(tripId, new OnExpensesLoadedListener() {
            @Override
            public void onSuccess(List<Expense> expenseList) {
                double total = 0;
                for (Expense expense : expenseList) {
                    total += expense.getAmountInMAD();
                }

                final double totalSpent = total;
                fetchBudget(tripId, new OnBudgetListener() {
                    @Override
                    public void onSuccess(Budget budget) {
                        budget.setTotalSpent(totalSpent);
                        saveBudget(budget, null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Budget not found, ignore
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Error fetching expenses
            }
        });
    }

    // Callback interfaces
    public interface OnExpenseListener {
        void onSuccess(Expense expense);

        void onFailure(Exception e);
    }

    public interface OnExpensesLoadedListener {
        void onSuccess(List<Expense> expenses);

        void onFailure(Exception e);
    }

    public interface OnBudgetListener {
        void onSuccess(Budget budget);

        void onFailure(Exception e);
    }

    public interface OnDeleteListener {
        void onSuccess();

        void onFailure(Exception e);
    }
}
