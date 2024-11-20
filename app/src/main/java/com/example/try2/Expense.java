package com.example.try2;
public class Expense {
    private String type;
    private float amount;

    public Expense() {
        // Default constructor required for calls to DataSnapshot.getValue(Expense.class)
    }

    public Expense(String type, float amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public float getAmount() {
        return amount;
    }
}
