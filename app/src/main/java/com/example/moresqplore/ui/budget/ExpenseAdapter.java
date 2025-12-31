package com.example.moresqplore.ui.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Expense;
import com.example.moresqplore.data.model.ExpenseCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying expenses
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);

        void onExpenseDelete(Expense expense);
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    public void setOnExpenseClickListener(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense, listener);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryIcon;
        private final TextView tvDescription;
        private final TextView tvCategory;
        private final TextView tvDateTime;
        private final TextView tvAmount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(Expense expense, OnExpenseClickListener listener) {
            // Category icon
            ExpenseCategory category = expense.getCategoryEnum();
            tvCategoryIcon.setText(category.getIcon());

            // Description
            tvDescription.setText(expense.getDescription());

            // Category name
            tvCategory.setText(category.getDisplayName());

            // Date and time
            String dateTime = expense.getDate() + " • " + expense.getTime();
            tvDateTime.setText(dateTime);

            // Amount
            tvAmount.setText(expense.getFormattedAmount());

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExpenseClick(expense);
                }
            });

            // Long click for delete
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onExpenseDelete(expense);
                }
                return true;
            });
        }
    }
}
