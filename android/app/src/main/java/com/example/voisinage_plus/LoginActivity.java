
package com.example.voisinage_plus;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private static final String URL_LOGIN =
            "http://192.168.0.88:8000/api/login";

    private EditText edtLogin;
    private EditText edtPassword;

    private Button btnLogin;
    private TextView tvGoRegister;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect XML components
        edtLogin = findViewById(R.id.edtLogin);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        requestQueue = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> loginUser());

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
            Toast.makeText(
                    this,
                    "Please enter your login and password",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("login", login);
            data.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        btnLogin.setEnabled(false);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_LOGIN,
                data,

                response -> {

                    btnLogin.setEnabled(true);

                    try {
                        boolean success =
                                response.optBoolean("success", false);

                        if (!success) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    response.optString(
                                            "message",
                                            "Login failed"
                                    ),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        // Get the nested user object
                        JSONObject user =
                                response.getJSONObject("user");

                        // Get user information
                        int userId = user.optInt("id", -1);

                        String nom = user.optString("nom", "");
                        String prenom = user.optString("prenom", "");
                        String email = user.optString("email", "");
                        String userLogin = user.optString("login", "");
                        String telephone =
                                user.optString("telephone", "");

                        String photo = user.optString("photo", "");

                        // Get saved location
                        String ville = user.optString("ville", "");
                        String quartier =
                                user.optString("quartier", "");

                        // Open HomeActivity
                        Intent intent = new Intent(
                                LoginActivity.this,
                                HomeActivity.class
                        );

                        intent.putExtra("user_id", userId);
                        intent.putExtra("nom", nom);
                        intent.putExtra("prenom", prenom);
                        intent.putExtra("email", email);
                        intent.putExtra("login", userLogin);
                        intent.putExtra("telephone", telephone);
                        intent.putExtra("photo", photo);

                        intent.putExtra("ville", ville);
                        intent.putExtra("quartier", quartier);

                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(
                                LoginActivity.this,
                                "Invalid server response",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                },

                error -> {

                    btnLogin.setEnabled(true);

                    String message = "Connection error";

                    if (error.networkResponse != null) {
                        message += " (HTTP "
                                + error.networkResponse.statusCode
                                + ")";
                    }

                    Toast.makeText(
                            LoginActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestQueue != null) {
            requestQueue.cancelAll(request ->
                    request.getUrl().equals(URL_LOGIN)
            );
        }
    }
}
