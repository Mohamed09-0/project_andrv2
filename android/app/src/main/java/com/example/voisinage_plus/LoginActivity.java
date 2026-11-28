
package com.example.voisinage_plus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_ERROR";

    private static final String URL_LOGIN =
            "http://192.168.0.88:8000/api/login";

    private EditText edtLogin, edtPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestQueue = Volley.newRequestQueue(this);

        edtLogin = findViewById(R.id.edtLogin);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        // Login button
        btnLogin.setOnClickListener(v -> loginUser());

        // Open registration screen
        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    MainActivity.class
            );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String login = edtLogin.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Please enter login and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("login", login);
            data.put("password", password);

        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
            return;
        }

        btnLogin.setEnabled(false);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_LOGIN,
                data,

                // SUCCESS
                response -> {

                    btnLogin.setEnabled(true);

                    try {

                        boolean success =
                                response.getBoolean("success");

                        String message =
                                response.getString("message");

                        Toast.makeText(this,
                                message,
                                Toast.LENGTH_LONG).show();

                        if (success) {

                            JSONObject user =
                                    response.getJSONObject("user");

                            // Get user information
                            int userId = user.getInt("id");

                            String nom = user.getString("nom");
                            String prenom = user.getString("prenom");
                            String email = user.getString("email");
                            String username = user.getString("login");
                            String telephone =
                                    user.optString("telephone", "");
                            String photo =
                                    user.optString("photo", "");

                            // Open HomeActivity
                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    HomeActivity.class
                            );

                            // Pass user information
                            intent.putExtra("user_id", userId);
                            intent.putExtra("nom", nom);
                            intent.putExtra("prenom", prenom);
                            intent.putExtra("email", email);
                            intent.putExtra("login", username);
                            intent.putExtra("telephone", telephone);
                            intent.putExtra("photo", photo);

                            startActivity(intent);

                            finish();
                        }

                    } catch (JSONException e) {

                        Log.e(TAG, "Response error", e);

                        Toast.makeText(this,
                                "Invalid server response",
                                Toast.LENGTH_SHORT).show();
                    }
                },

                // ERROR
                error -> {

                    btnLogin.setEnabled(true);

                    Log.e(TAG, "Login error", error);

                    if (error.networkResponse != null) {

                        String body = new String(
                                error.networkResponse.data,
                                StandardCharsets.UTF_8
                        );

                        Toast.makeText(this,
                                "HTTP "
                                        + error.networkResponse.statusCode
                                        + ": " + body,
                                Toast.LENGTH_LONG).show();

                    } else {

                        Toast.makeText(this,
                                "Network error: " + error.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }
}
