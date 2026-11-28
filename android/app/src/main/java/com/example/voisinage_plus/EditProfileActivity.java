package com.example.voisinage_plus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    // Change this IP if your computer's local IP changes.
    private static final String URL_UPDATE =
            "http://192.168.0.88:8000/api/profile/update";

    private static final String URL_LOCATION =
            "http://192.168.0.88:8000/api/profile/location";

    private static final String URL_PHOTO =
            "http://192.168.0.88:8000/api/profile/photo";

    private EditText edtEditPrenom, edtEditNom, edtEditEmail;
    private EditText edtEditLogin, edtEditTelephone;
    private EditText edtEditVille, edtEditQuartier;

    private ImageView imgEditProfile;
    private Button btnChoosePhoto, btnSaveProfile;

    private RequestQueue requestQueue;

    private Uri selectedImageUri = null;

    private int userId;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        requestQueue = Volley.newRequestQueue(this);

        // Get user ID
        userId = getIntent().getIntExtra("user_id", -1);

        if (userId == -1) {
            String idString = getIntent().getStringExtra("user_id");
            if (idString != null) {
                try {
                    userId = Integer.parseInt(idString);
                } catch (NumberFormatException e) {
                    userId = -1;
                }
            }
        }

        // Connect XML fields
        edtEditPrenom = findViewById(R.id.edtEditPrenom);
        edtEditNom = findViewById(R.id.edtEditNom);
        edtEditEmail = findViewById(R.id.edtEditEmail);
        edtEditLogin = findViewById(R.id.edtEditLogin);
        edtEditTelephone = findViewById(R.id.edtEditTelephone);
        edtEditVille = findViewById(R.id.edtEditVille);
        edtEditQuartier = findViewById(R.id.edtEditQuartier);

        imgEditProfile = findViewById(R.id.imgEditProfile);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Fill fields with current information
        edtEditPrenom.setText(getIntent().getStringExtra("prenom"));
        edtEditNom.setText(getIntent().getStringExtra("nom"));
        edtEditEmail.setText(getIntent().getStringExtra("email"));
        edtEditLogin.setText(getIntent().getStringExtra("login"));
        edtEditTelephone.setText(getIntent().getStringExtra("telephone"));
        edtEditVille.setText(getIntent().getStringExtra("ville"));
        edtEditQuartier.setText(getIntent().getStringExtra("quartier"));

        // Configure gallery picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imgEditProfile.setImageURI(uri);
                    }
                }
        );

        btnChoosePhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnSaveProfile.setOnClickListener(v -> updateProfile());
    }

    // STEP 1: Update personal information
    private void updateProfile() {

        if (userId == -1) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String prenom = edtEditPrenom.getText().toString().trim();
        String nom = edtEditNom.getText().toString().trim();
        String email = edtEditEmail.getText().toString().trim();
        String login = edtEditLogin.getText().toString().trim();
        String telephone = edtEditTelephone.getText().toString().trim();

        if (prenom.isEmpty() || nom.isEmpty()
                || email.isEmpty() || login.isEmpty()) {
            Toast.makeText(this,
                    "Please complete all required fields",
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
            e.printStackTrace();
            return;
        }

        btnSaveProfile.setEnabled(false);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_UPDATE,
                data,
                response -> updateLocation(),
                error -> {
                    btnSaveProfile.setEnabled(true);
                    showError("Profile update failed", error);
                }
        );

        requestQueue.add(request);
    }

    // STEP 2: Update city and neighborhood
    private void updateLocation() {

        String ville = edtEditVille.getText().toString().trim();
        String quartier = edtEditQuartier.getText().toString().trim();

        if (ville.isEmpty()) {
            btnSaveProfile.setEnabled(true);
            Toast.makeText(this,
                    "Please enter your city",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("user_id", userId);
            data.put("ville", ville);
            data.put("quartier", quartier);

        } catch (JSONException e) {
            e.printStackTrace();
            btnSaveProfile.setEnabled(true);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_LOCATION,
                data,
                response -> {
                    if (selectedImageUri != null) {
                        uploadPhoto();
                    } else {
                        finishSuccessfully();
                    }
                },
                error -> {
                    btnSaveProfile.setEnabled(true);
                    showError("Location update failed", error);
                }
        );

        requestQueue.add(request);
    }

    // STEP 3: Convert selected photo to Base64 and upload it
    private void uploadPhoto() {

        String imageBase64 = convertImageToBase64(selectedImageUri);

        if (imageBase64 == null) {
            btnSaveProfile.setEnabled(true);
            Toast.makeText(this,
                    "Could not read selected image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("user_id", userId);
            data.put("image_base64", imageBase64);

        } catch (JSONException e) {
            e.printStackTrace();
            btnSaveProfile.setEnabled(true);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_PHOTO,
                data,
                response -> finishSuccessfully(),
                error -> {
                    btnSaveProfile.setEnabled(true);
                    showError("Photo upload failed", error);
                }
        );

        requestQueue.add(request);
    }

    // Convert image to a compressed JPEG Base64 string
    private String convertImageToBase64(Uri imageUri) {

        try {
            InputStream inputStream =
                    getContentResolver().openInputStream(imageUri);

            if (inputStream == null) {
                return null;
            }

            Bitmap original = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (original == null) {
                return null;
            }

            // Resize large images to reduce upload size
            int maxDimension = 1000;
            int width = original.getWidth();
            int height = original.getHeight();

            float scale = Math.min(
                    1f,
                    (float) maxDimension / Math.max(width, height)
            );

            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);

            Bitmap resized = Bitmap.createScaledBitmap(
                    original, newWidth, newHeight, true
            );

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            resized.compress(
                    Bitmap.CompressFormat.JPEG,
                    75,
                    outputStream
            );

            byte[] imageBytes = outputStream.toByteArray();

            if (resized != original) {
                resized.recycle();
            }
            original.recycle();

            return Base64.encodeToString(
                    imageBytes,
                    Base64.NO_WRAP
            );

        } catch (Exception e) {
            Log.e("PROFILE_PHOTO", "Image conversion failed", e);
            return null;
        }
    }

    // Return updated information to HomeActivity
    private void finishSuccessfully() {

        getIntent().putExtra("user_id", userId);
        getIntent().putExtra("prenom",
                edtEditPrenom.getText().toString().trim());
        getIntent().putExtra("nom",
                edtEditNom.getText().toString().trim());
        getIntent().putExtra("email",
                edtEditEmail.getText().toString().trim());
        getIntent().putExtra("login",
                edtEditLogin.getText().toString().trim());
        getIntent().putExtra("telephone",
                edtEditTelephone.getText().toString().trim());
        getIntent().putExtra("ville",
                edtEditVille.getText().toString().trim());
        getIntent().putExtra("quartier",
                edtEditQuartier.getText().toString().trim());

        setResult(RESULT_OK, getIntent());

        Toast.makeText(this,
                "Profile updated successfully",
                Toast.LENGTH_SHORT).show();

        finish();
    }

    private void showError(String message,
                           com.android.volley.VolleyError error) {

        String details = message;

        if (error.networkResponse != null) {
            details += " (HTTP " + error.networkResponse.statusCode + ")";
            if (error.networkResponse.data != null) {
                details += ": " +
                        new String(error.networkResponse.data);
            }
        } else if (error.getMessage() != null) {
            details += ": " + error.getMessage();
        }

        Log.e("PROFILE_API", details, error);

        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestQueue != null) {
            requestQueue.cancelAll(request ->
                    request.getUrl().equals(URL_UPDATE)
                            || request.getUrl().equals(URL_LOCATION)
                            || request.getUrl().equals(URL_PHOTO)
            );
        }
    }
}