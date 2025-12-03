package com.example.quickcash.utilities;

import android.util.Log;

public class PaymentProcessor {

    public boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    public boolean initiatePayment(String email, double amount) {
        if (!isValidEmail(email) || amount <= 0) {
            Log.e("PayPal", "Invalid email or amount");
            return false;
        }

        Log.d("PayPal", "Simulated payment of $" + amount + " sent to: " + email);
        return true; // Simulate success
    }
}
