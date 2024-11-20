package com.example.try2;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.MessageFormat;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private TextView totalIncomeTextView;
    private TextView totalExpenseTextView;
    private LinearLayout historyLinearLayout;
    private Button addIncomeButton;
    private Button addExpenseButton;

    private double totalIncome = 0;
    private double totalExpense = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        totalIncomeTextView = findViewById(R.id.incomeAmountTextView);
        totalExpenseTextView = findViewById(R.id.expenseAmountTextView);
        historyLinearLayout = findViewById(R.id.historyLinearLayout);
        addIncomeButton = findViewById(R.id.addIncomeButton);
        addExpenseButton = findViewById(R.id.addExpenseButton);

        addIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(true);
            }
        });

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(false);
            }
        });

        ImageButton logoutButton = findViewById(R.id.arrowButton);
        ImageButton backlogButton;
        backlogButton = findViewById(R.id.backlogButton);
        fetchDataFromFirebase();
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout button click
                goToLoginActivity();
            }
        });
        backlogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout button click
                goToLogoutActivity();
            }
        });

        // Fetch data from Firebase and update UI

    }

    // Method to navigate to another activity (e.g., LoginActivity)
    private void goToLoginActivity() {
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
        finish(); // Optional: Close current activity if you don't want to come back to it
    }
    private void goToLogoutActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: Close current activity if you don't want to come back to it
    }

    private void showInputDialog(final boolean isIncome) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isIncome ? "Add Income" : "Add Expense");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String amountString = input.getText().toString();
                if (!amountString.isEmpty()) {
                    double amount = Double.parseDouble(amountString);
                    promptForReason(isIncome, amount);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void promptForReason(final boolean isIncome, final double amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter " + (isIncome ? "Income" : "Expense") + " Reason");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reason = input.getText().toString().trim();
                if (!reason.isEmpty()) {
                    // Get the current user ID
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        updateTotal(isIncome, amount, reason, userId);
                    } else {
                        // User is not authenticated, handle accordingly
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void updateTotal(boolean isIncome, double amount, String reason, String userId) {
        // Write data to Firebase with user ID
        String entryType = isIncome ? "income" : "expense";
        DatabaseReference newEntryRef = mDatabase.child("entries").child(userId).push();
        newEntryRef.child("type").setValue(entryType);
        newEntryRef.child("amount").setValue(amount);
        newEntryRef.child("reason").setValue(reason);
    }

    private void fetchDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userEntriesRef = mDatabase.child("entries").child(userId);
            userEntriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Clear history view before adding fetched data
                    historyLinearLayout.removeAllViews();

                    totalIncome = 0;
                    totalExpense = 0;

                    for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                        String entryType = entrySnapshot.child("type").getValue(String.class);
                        double amount = entrySnapshot.child("amount").getValue(Double.class);
                        String reason = entrySnapshot.child("reason").getValue(String.class);

                        if (entryType != null && reason != null) {
                            if (entryType.equals("income")) {
                                totalIncome += amount;
                            } else if (entryType.equals("expense")) {
                                totalExpense += amount;
                            }
                            // Update history view with fetched data
                            updateHistory(entryType + ": Rs " + amount + " from " + reason);
                        }
                    }

                    // Update total income and total expense text views
                    totalIncomeTextView.setText(MessageFormat.format("Total Income: Rs {0}", totalIncome));
                    totalExpenseTextView.setText(MessageFormat.format("Total Expense: Rs {0}", totalExpense));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }

            });

        }
    }

    private void updateHistory(String item) {
        TextView textView = new TextView(this);
        textView.setText(item);
        historyLinearLayout.addView(textView);
    }
}
