
package com.example.voisinage_plus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "UPDATE_PROFILE";

    private static final String URL_UPDATE =
            "http://192.168.0.88:8000/api/profile/update";

    private EditText edtPrenom, edtNom, edtEmail;
    private EditText edtLogin, edtTelephone;
    private EditText edtVille, edtQuartier;

    private Button btnSaveProfile;

    private RequestQueue requestQueue;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        requestQueue = Volley.newRequestQueue(this);

        // Connect XML fields
        edtPrenom = findViewById(R.id.edtEditPrenom);
        edtNom = findViewById(R.id.edtEditNom);
        edtEmail = findViewById(R.id.edtEditEmail);
        edtLogin = findViewById(R.id.edtEditLogin);
        edtTelephone = findViewById(R.id.edtEditTelephone);
        edtVille = findViewById(R.id.edtEditVille);
        edtQuartier = findViewById(R.id.edtEditQuartier);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Receive existing information
        userId = getIntent().getIntExtra("user_id", -1);

        edtPrenom.setText(getIntent().getStringExtra("prenom"));
        edtNom.setText(getIntent().getStringExtra("nom"));
        edtEmail.setText(getIntent().getStringExtra("email"));
        edtLogin.setText(getIntent().getStringExtra("login"));
        edtTelephone.setText(getIntent().getStringExtra("telephone"));
        edtVille.setText(getIntent().getStringExtra("ville"));
        edtQuartier.setText(getIntent().getStringExtra("quartier"));

        btnSaveProfile.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {

        String prenom = edtPrenom.getText().toString().trim();
        String nom = edtNom.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String login = edtLogin.getText().toString().trim();
        String telephone = edtTelephone.getText().toString().trim();

        if (prenom.isEmpty() || nom.isEmpty()
                || email.isEmpty() || login.isEmpty()) {

            Toast.makeText(this,
                    "Please complete all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == -1) {
            Toast.makeText(this,
                    "User ID is missing",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("user_id", userId);
            data.put("prenom", prenom);
            data.put("nom", nom);
            data.put("email", email);
            data.put("login", login);
            data.put("telephone", telephone);

        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
            return;
        }

        btnSaveProfile.setEnabled(false);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_UPDATE,
                data,

                response -> {

                    btnSaveProfile.setEnabled(true);

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

                            // Return updated information
                            Intent result = new Intent();

                            result.putExtra("user_id", user.getInt("id"));
                            result.putExtra("prenom", user.getString("prenom"));
                            result.putExtra("nom", user.getString("nom"));
                            result.putExtra("email", user.getString("email"));
                            result.putExtra("login", user.getString("login"));
                            result.putExtra("telephone",
                                    user.optString("telephone", ""));

                            // Location is not saved by this API yet
                            result.putExtra("ville",
                                    edtVille.getText().toString().trim());
                            result.putExtra("quartier",
                                    edtQuartier.getText().toString().trim());

                            setResult(RESULT_OK, result);
                            finish();
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Response error", e);

                        Toast.makeText(this,
                                "Invalid server response",
                                Toast.LENGTH_SHORT).show();
                    }
                },

                error -> {

                    btnSaveProfile.setEnabled(true);

                    Log.e(TAG, "Update error", error);

                    if (error.networkResponse != null) {

                        String body = new String(
                                error.networkResponse.data,
                                StandardCharsets.UTF_8
                        );

                        Toast.makeText(this,
                                "HTTP " +
                                        error.networkResponse.statusCode
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
