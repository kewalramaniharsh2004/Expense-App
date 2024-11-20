package com.example.try2;

import android.os.Bundle;
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
import android.widget.ImageView;
import android.content.Intent;
import android.view.View;

public class MainActivity2 extends AppCompatActivity {

    private TextView totalIncomeTextView, totalExpenseTextView;
    private PieChartView pieChartView;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        totalIncomeTextView = findViewById(R.id.totalIncomeTextView);
        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        pieChartView = findViewById(R.id.pieChart);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
                finish(); // Optional, to close MainActivity2
            }
        });

        // Retrieve data from Firebase Realtime Database
        fetchDataFromFirebase();

    }

    private void fetchDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userEntriesRef = FirebaseDatabase.getInstance().getReference()
                    .child("entries").child(userId);
            userEntriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    double totalIncome = 0;
                    double totalExpense = 0;

                    for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
                        String entryType = entrySnapshot.child("type").getValue(String.class);
                        double amount = entrySnapshot.child("amount").getValue(Double.class);

                        if (entryType != null) {
                            if (entryType.equals("income")) {
                                totalIncome += amount;
                            } else if (entryType.equals("expense")) {
                                totalExpense += amount;
                            }
                        }
                    }

                    // Display total income and total expense
                    totalIncomeTextView.setText("Total Income: Rs" + totalIncome);
                    totalExpenseTextView.setText("Total Expense: Rs" + totalExpense);

                    // Update pie chart with data
                    pieChartView.setData((float) totalIncome, (float) totalExpense);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
}
