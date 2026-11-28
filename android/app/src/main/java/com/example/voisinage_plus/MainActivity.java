
package com.example.voisinage_plus;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_ERROR";

    private static final String URL_REGISTER =
            "http://192.168.0.88:8000/api/register";

    private EditText edtNom, edtPrenom, edtEmail, edtTelephone;
    private EditText edtLogin, edtPassword, edtConfirmPassword;

    private Button btnRegister;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        edtNom = findViewById(R.id.edtNom);
        edtPrenom = findViewById(R.id.edtPrenom);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelephone = findViewById(R.id.edtTelephone);
        edtLogin = findViewById(R.id.edtLogin);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String nom = edtNom.getText().toString().trim();
        String prenom = edtPrenom.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String telephone = edtTelephone.getText().toString().trim();
        String login = edtLogin.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()
                || telephone.isEmpty() || login.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email address");
            return;
        }

        if (password.length() < 8) {
            edtPassword.setError("Password must contain at least 8 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Passwords do not match");
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("nom", nom);
            data.put("prenom", prenom);
            data.put("email", email);
            data.put("telephone", telephone);
            data.put("login", login);
            data.put("password", password);
            data.put("confirmPassword", confirmPassword);

        } catch (JSONException e) {
            Log.e(TAG, "JSON creation failed", e);
            return;
        }

        Log.d(TAG, "Sending POST request to: " + URL_REGISTER);
        Log.d(TAG, "Request data prepared");

        btnRegister.setEnabled(false);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_REGISTER,
                data,

                response -> {
                    btnRegister.setEnabled(true);

                    Log.d(TAG, "Server response: " + response);

                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        Toast.makeText(this,
                                message,
                                Toast.LENGTH_LONG).show();

                        if (success) {
                            clearFields();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Response parsing error", e);
                        Toast.makeText(this,
                                "Invalid server response",
                                Toast.LENGTH_SHORT).show();
                    }
                },

                error -> {
                    btnRegister.setEnabled(true);

                    Log.e(TAG, "Error type: "
                            + error.getClass().getName());

                    Log.e(TAG, "Error details: "
                            + error.toString(), error);

                    if (error.networkResponse != null) {

                        int statusCode =
                                error.networkResponse.statusCode;

                        String body = new String(
                                error.networkResponse.data,
                                StandardCharsets.UTF_8
                        );

                        Log.e(TAG, "HTTP status: " + statusCode);
                        Log.e(TAG, "Response body: " + body);

                        Toast.makeText(this,
                                "HTTP " + statusCode + ": " + body,
                                Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(this,
                                error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(request);
    }

    private void clearFields() {
        edtNom.setText("");
        edtPrenom.setText("");
        edtEmail.setText("");
        edtTelephone.setText("");
        edtLogin.setText("");
        edtPassword.setText("");
        edtConfirmPassword.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }
}
