
package com.example.voisinage_plus;

import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PublishActivity extends AppCompatActivity {

    private static final String BASE_URL =
            "http://192.168.0.88:8000";

    private EditText edtTitre, edtDescription, edtPrix;
    private EditText edtVille, edtQuartier;

    private Spinner spCategorie, spEtat, spTransaction;

    private Button btnPublish, btnChoosePhoto;
    private ImageView imgAnnouncement;

    private RequestQueue requestQueue;

    private int userId;
    private Uri selectedImageUri;

    private final ArrayList<Integer> categoryIds =
            new ArrayList<>();

    private final ArrayList<String> categoryNames =
            new ArrayList<>();

    private ActivityResultLauncher<String> imagePickerLauncher;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        userId = getIntent().getIntExtra("user_id", -1);

        edtTitre = findViewById(R.id.edtTitre);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrix = findViewById(R.id.edtPrix);
        edtVille = findViewById(R.id.edtVille);
        edtQuartier = findViewById(R.id.edtQuartier);

        spCategorie = findViewById(R.id.spCategorie);
        spEtat = findViewById(R.id.spEtat);
        spTransaction = findViewById(R.id.spTransaction);

        btnPublish = findViewById(R.id.btnPublish);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        imgAnnouncement = findViewById(R.id.imgAnnouncement);

        requestQueue = Volley.newRequestQueue(this);

        if (userId == -1) {
            Toast.makeText(this,
                    "User not identified. Please log in again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imgAnnouncement.setImageURI(uri);
                    }
                }
        );

        btnChoosePhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnPublish.setOnClickListener(v ->
                publishAnnouncement()
        );

        loadCategories();
    }

    // =========================================
    // LOAD CATEGORIES
    // =========================================

    private void loadCategories() {

        String url = BASE_URL + "/api/categories";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    try {
                        JSONArray categories =
                                response.getJSONArray("categories");

                        categoryIds.clear();
                        categoryNames.clear();

                        for (int i = 0; i < categories.length(); i++) {

                            JSONObject category =
                                    categories.getJSONObject(i);

                            categoryIds.add(category.getInt("id"));
                            categoryNames.add(
                                    category.getString("libelle")
                            );
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        this,
                                        android.R.layout.simple_spinner_item,
                                        categoryNames
                                );

                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        spCategorie.setAdapter(adapter);

                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Category response error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                },

                error -> Toast.makeText(this,
                        "Cannot load categories: " + error.toString(),
                        Toast.LENGTH_LONG).show()
        );

        requestQueue.add(request);
    }

    // =========================================
    // CREATE ANNOUNCEMENT
    // =========================================

    private void publishAnnouncement() {

        String titre =
                edtTitre.getText().toString().trim();

        String description =
                edtDescription.getText().toString().trim();

        String prixText =
                edtPrix.getText().toString().trim();

        String ville =
                edtVille.getText().toString().trim();

        String quartier =
                edtQuartier.getText().toString().trim();

        if (titre.isEmpty()) {
            edtTitre.setError("Title is required");
            return;
        }

        if (categoryIds.isEmpty()) {
            Toast.makeText(this,
                    "No category available",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String transaction =
                spTransaction.getSelectedItem().toString();

        if (transaction.equals("VENTE") && prixText.isEmpty()) {
            edtPrix.setError("Price is required for sale");
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("user_id", userId);

            data.put("categorie_id",
                    categoryIds.get(
                            spCategorie.getSelectedItemPosition()
                    )
            );

            data.put("titre", titre);
            data.put("description", description);
            data.put("type_transaction", transaction);

            data.put("etat_objet",
                    spEtat.getSelectedItem().toString());

            data.put("disponibilite", "DISPONIBLE");

            data.put("ville", ville);
            data.put("quartier", quartier);

            if (!prixText.isEmpty()) {
                data.put("prix", prixText);
            } else if (transaction.equals("PRET")) {
                data.put("prix", 0);
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "Error preparing announcement",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnPublish.setEnabled(false);

        String url = BASE_URL + "/api/annonces/create";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                data,

                response -> {

                    if (!response.optBoolean("success")) {
                        btnPublish.setEnabled(true);

                        Toast.makeText(this,
                                response.optString(
                                        "message",
                                        "Announcement creation failed"
                                ),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    int annonceId =
                            response.optInt("annonce_id", -1);

                    if (annonceId == -1) {
                        btnPublish.setEnabled(true);

                        Toast.makeText(this,
                                "Announcement created, but ID was not received",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (selectedImageUri != null) {
                        uploadPhoto(annonceId);
                    } else {
                        Toast.makeText(this,
                                "Announcement published successfully!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                },

                error -> {
                    btnPublish.setEnabled(true);

                    Toast.makeText(this,
                            "Publication failed: " + error.toString(),
                            Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(request);
    }

    // =========================================
    // UPLOAD PHOTO
    // =========================================

    private void uploadPhoto(int annonceId) {

        btnPublish.setEnabled(false);

        Toast.makeText(this,
                "Uploading photo...",
                Toast.LENGTH_SHORT).show();

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String boundary =
                        "----VoisinageBoundary"
                                + System.currentTimeMillis();

                String mimeType =
                        getContentResolver().getType(selectedImageUri);

                if (mimeType == null ||
                        !(mimeType.equals("image/jpeg")
                                || mimeType.equals("image/png")
                                || mimeType.equals("image/webp"))) {

                    throw new Exception(
                            "Only JPEG, PNG and WEBP images are supported"
                    );
                }

                // Read image bytes
                byte[] imageBytes;

                try (
                        InputStream input =
                                getContentResolver().openInputStream(
                                        selectedImageUri
                                );

                        ByteArrayOutputStream buffer =
                                new ByteArrayOutputStream()
                ) {

                    if (input == null) {
                        throw new Exception(
                                "Cannot read selected image"
                        );
                    }

                    byte[] data = new byte[8192];
                    int count;

                    while ((count = input.read(data)) != -1) {

                        buffer.write(data, 0, count);

                        if (buffer.size() > 5 * 1024 * 1024) {
                            throw new Exception(
                                    "Image must be smaller than 5 MB"
                            );
                        }
                    }

                    imageBytes = buffer.toByteArray();
                }

                String fileName = getFileName(selectedImageUri);

                String endpoint =
                        BASE_URL
                                + "/api/annonces/"
                                + annonceId
                                + "/photos";

                URL url = new URL(endpoint);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);

                connection.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                // Send multipart request
                try (OutputStream output =
                             connection.getOutputStream()) {

                    String header =
                            "--" + boundary + "\r\n"
                                    + "Content-Disposition: form-data; "
                                    + "name=\"photo\"; filename=\""
                                    + fileName + "\"\r\n"
                                    + "Content-Type: " + mimeType
                                    + "\r\n\r\n";

                    output.write(header.getBytes("UTF-8"));
                    output.write(imageBytes);

                    output.write(
                            ("\r\n--" + boundary + "--\r\n")
                                    .getBytes("UTF-8")
                    );

                    output.flush();
                }

                int responseCode =
                        connection.getResponseCode();

                InputStream responseStream;

                if (responseCode >= 200 && responseCode < 300) {
                    responseStream = connection.getInputStream();
                } else {
                    responseStream = connection.getErrorStream();
                }

                StringBuilder responseText =
                        new StringBuilder();

                if (responseStream != null) {

                    try (BufferedReader reader =
                                 new BufferedReader(
                                         new InputStreamReader(
                                                 responseStream
                                         )
                                 )) {

                        String line;

                        while ((line = reader.readLine()) != null) {
                            responseText.append(line);
                        }
                    }
                }

                String responseBody =
                        responseText.toString().trim();

                // Detect HTML or other non-JSON responses
                if (!responseBody.startsWith("{")) {

                    String preview = responseBody.length() > 400
                            ? responseBody.substring(0, 400)
                            : responseBody;

                    throw new Exception(
                            "HTTP " + responseCode
                                    + " - Server response: "
                                    + preview
                    );
                }

                JSONObject response =
                        new JSONObject(responseBody);

                if (responseCode >= 200
                        && responseCode < 300
                        && response.optBoolean("success")) {

                    runOnUiThread(() -> {

                        Toast.makeText(this,
                                "Announcement and photo published successfully!",
                                Toast.LENGTH_LONG).show();

                        finish();
                    });

                } else {

                    String message =
                            response.optString(
                                    "message",
                                    "Photo upload failed"
                            );

                    runOnUiThread(() -> {

                        btnPublish.setEnabled(true);

                        Toast.makeText(this,
                                "Announcement created, but photo upload failed: "
                                        + message,
                                Toast.LENGTH_LONG).show();
                    });
                }

            } catch (Exception e) {

                String errorMessage = e.getMessage();

                runOnUiThread(() -> {

                    btnPublish.setEnabled(true);

                    Toast.makeText(this,
                            "Photo upload error: " + errorMessage,
                            Toast.LENGTH_LONG).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================
    // GET IMAGE FILE NAME
    // =========================================

    private String getFileName(Uri uri) {

        String result = "announcement_photo.jpg";

        try (
                Cursor cursor = getContentResolver().query(
                        uri,
                        null,
                        null,
                        null,
                        null
                )
        ) {

            if (cursor != null && cursor.moveToFirst()) {

                int index = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                );

                if (index >= 0) {

                    String name = cursor.getString(index);

                    if (name != null && !name.trim().isEmpty()) {

                        result = name.replaceAll(
                                "[^a-zA-Z0-9._-]",
                                "_"
                        );
                    }
                }
            }

        } catch (Exception ignored) {
        }

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }

        executor.shutdownNow();
    }
}
