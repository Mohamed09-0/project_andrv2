
package com.example.voisinage_plus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileFirstName;
    private TextView tvProfileLastName;
    private TextView tvProfileEmail;
    private TextView tvProfileLogin;
    private TextView tvProfilePhone;
    private TextView tvProfileLocation;

    private Button btnEditProfile;

    private int userId;
    private String nom, prenom, email, login, telephone;
    private String ville = "";
    private String quartier = "";

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileFirstName = findViewById(R.id.tvProfileFirstName);
        tvProfileLastName = findViewById(R.id.tvProfileLastName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileLogin = findViewById(R.id.tvProfileLogin);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileLocation = findViewById(R.id.tvProfileLocation);

        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Receive information from LoginActivity
        userId = getIntent().getIntExtra("user_id", -1);
        nom = getIntent().getStringExtra("nom");
        prenom = getIntent().getStringExtra("prenom");
        email = getIntent().getStringExtra("email");
        login = getIntent().getStringExtra("login");
        telephone = getIntent().getStringExtra("telephone");

        displayProfile();

        // Receive updated information when EditProfileActivity closes
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {

                        Intent data = result.getData();

                        prenom = data.getStringExtra("prenom");
                        nom = data.getStringExtra("nom");
                        email = data.getStringExtra("email");
                        login = data.getStringExtra("login");
                        telephone = data.getStringExtra("telephone");
                        ville = data.getStringExtra("ville");
                        quartier = data.getStringExtra("quartier");

                        displayProfile();
                    }
                }
        );

        btnEditProfile.setOnClickListener(v -> openEditProfile());
    }

    private void displayProfile() {

        tvProfileName.setText(prenom + " " + nom);
        tvProfileFirstName.setText("First name: " + prenom);
        tvProfileLastName.setText("Last name: " + nom);
        tvProfileEmail.setText("Email: " + email);
        tvProfileLogin.setText("Username: " + login);
        tvProfilePhone.setText("Phone: " + telephone);

        String location = ville.isEmpty() ? "Not specified" : ville;

        if (!quartier.isEmpty()) {
            location += ", " + quartier;
        }

        tvProfileLocation.setText("Location: " + location);
    }

    private void openEditProfile() {

        Intent intent = new Intent(
                HomeActivity.this,
                EditProfileActivity.class
        );

        intent.putExtra("user_id", userId);
        intent.putExtra("nom", nom);
        intent.putExtra("prenom", prenom);
        intent.putExtra("email", email);
        intent.putExtra("login", login);
        intent.putExtra("telephone", telephone);
        intent.putExtra("ville", ville);
        intent.putExtra("quartier", quartier);

        editProfileLauncher.launch(intent);
    }
}
