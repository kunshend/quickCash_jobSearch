package com.example.quickcash.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quickcash.R;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;

/**
 * CompleteAndPayActivity handles the payment process for completed jobs.
 * This activity allows employers to pay employees for completed jobs using PayPal.
 * It displays job information and provides a payment amount field for the employer.
 *
 * @author QuickCash Team
 * @version 1.0
 */
public class CompleteAndPayActivity extends Activity {

    /**
     * Request code for PayPal payment intent
     */
    private static final int PAYPAL_REQUEST_CODE = 123;

    /**
     * PayPal Client ID for payment processing
     */
    private static final String CLIENT_ID = "AU5dBpn6NcLa_h8tQizF1va1R-vC-DxWAESBxdazuYDq30wou8xUMBpy_GjDQPEkHJvXw_ZdDcSCHwBu";

    /**
     * PayPal configuration object for setting up the payment environment
     */
    private PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(CLIENT_ID);

    /**
     * UI element for entering payment amount
     */
    private EditText etPaymentAmount;

    /**
     * Button to initiate payment process
     */
    private Button payButton;

    /**
     * Initializes the activity, sets up the UI, and starts the PayPal service.
     * Retrieves job details from the intent extras and displays them in the UI.
     * Sets up the click listener for the pay button.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_and_pay);

        // Start PayPal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        // Retrieve references to UI elements
        TextView tvJobTitle = findViewById(R.id.jobTitle);
        TextView tvEmployeeEmail = findViewById(R.id.employeeEmail);
        etPaymentAmount = findViewById(R.id.etPaymentAmount);
        payButton = findViewById(R.id.payButton);

        // **Get extras** from the intent that launched this activity
        Intent extrasIntent = getIntent();
        if (extrasIntent != null) {
            String jobName = extrasIntent.getStringExtra("jobName");
            String employeeEmail = extrasIntent.getStringExtra("employeeEmail");

            // Set the text on the UI
            if (jobName != null) {
                tvJobTitle.setText(jobName);
            }
            if (employeeEmail != null) {
                tvEmployeeEmail.setText(employeeEmail);
            }
        }

        // On Pay button click
        payButton.setOnClickListener(v -> startPayment());
    }

    /**
     * Initiates the PayPal payment process.
     * Validates the payment amount entered by the user and creates a PayPal payment object.
     * Launches the PayPal payment activity to handle the transaction.
     */
    private void startPayment() {
        // Retrieve the employer-entered payment amount.
        String amountStr = etPaymentAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Please enter a payment amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid payment amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a PayPalPayment using the entered amount.
        PayPalPayment payment = new PayPalPayment(
                new BigDecimal(amount), "USD",
                "QuickCash Job Payment",
                PayPalPayment.PAYMENT_INTENT_SALE
        );

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    /**
     * Handles the result from the PayPal payment activity.
     * Processes the payment confirmation or cancellation and shows appropriate messages.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity.
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    // TODO: Update your Firebase or perform any post-payment actions here.
                    Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(this, "Invalid Payment", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * Stops the PayPal service to prevent resource leaks.
     */
    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }
}