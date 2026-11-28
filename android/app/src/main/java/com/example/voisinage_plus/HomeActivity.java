
package com.example.voisinage_plus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class HomeActivity extends AppCompatActivity {

    private int userId;

    private ImageView imgProfile;

    private TextView tvProfileName;
    private TextView tvProfileFirstName;
    private TextView tvProfileLastName;
    private TextView tvProfileEmail;
    private TextView tvProfileLogin;
    private TextView tvProfilePhone;
    private TextView tvProfileLocation;

    private Button btnEditProfile;

    private String nom, prenom, email, login;
    private String telephone, photo, ville, quartier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge and handle system bars ourselves
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);

        window.setStatusBarColor(Color.parseColor("#F5F6F8"));
        window.setNavigationBarColor(Color.WHITE);

        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        setContentView(R.layout.activity_profile);

        // Reserve space for Android status and navigation bars
        View root = findViewById(R.id.profileRoot);

        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + systemBars.bottom
            );

            return insets;
        });

        ViewCompat.requestApplyInsets(root);

        // Initialize views
        imgProfile = findViewById(R.id.imgProfile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileFirstName = findViewById(R.id.tvProfileFirstName);
        tvProfileLastName = findViewById(R.id.tvProfileLastName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileLogin = findViewById(R.id.tvProfileLogin);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileLocation = findViewById(R.id.tvProfileLocation);

        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Get logged-in user information
        Intent intent = getIntent();

        userId = intent.getIntExtra("user_id", -1);
        nom = intent.getStringExtra("nom");
        prenom = intent.getStringExtra("prenom");
        email = intent.getStringExtra("email");
        login = intent.getStringExtra("login");
        telephone = intent.getStringExtra("telephone");
        photo = intent.getStringExtra("photo");
        ville = intent.getStringExtra("ville");
        quartier = intent.getStringExtra("quartier");

        // Display profile
        tvProfileName.setText(
                (prenom != null ? prenom : "") + " " +
                        (nom != null ? nom : "")
        );

        tvProfileFirstName.setText(prenom != null ? prenom : "");
        tvProfileLastName.setText(nom != null ? nom : "");
        tvProfileEmail.setText(email != null ? email : "");
        tvProfileLogin.setText(login != null ? login : "");
        tvProfilePhone.setText(telephone != null ? telephone : "");

        String location = "";

        if (ville != null) {
            location += ville;
        }

        if (quartier != null && !quartier.isEmpty()) {
            if (!location.isEmpty()) {
                location += " - ";
            }

            location += quartier;
        }

        tvProfileLocation.setText(location);

        // Load profile photo
        if (photo != null && !photo.isEmpty()) {

            String photoUrl = photo;

            if (photo.startsWith("/")) {
                photoUrl = "http://192.168.0.88:8000" + photo;
            }

            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_menu_camera)
                    .centerCrop()
                    .into(imgProfile);
        }

        // Edit profile
        btnEditProfile.setOnClickListener(v -> {

            Intent editIntent = new Intent(
                    HomeActivity.this,
                    EditProfileActivity.class
            );

            editIntent.putExtra("user_id", userId);
            editIntent.putExtra("nom", nom);
            editIntent.putExtra("prenom", prenom);
            editIntent.putExtra("email", email);
            editIntent.putExtra("login", login);
            editIntent.putExtra("telephone", telephone);
            editIntent.putExtra("photo", photo);
            editIntent.putExtra("ville", ville);
            editIntent.putExtra("quartier", quartier);

            startActivity(editIntent);
        });

        // Bottom navigation

        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already on Home/Profile
        });

        findViewById(R.id.navBrowse).setOnClickListener(v -> {

            Intent browseIntent = new Intent(
                    HomeActivity.this,
                    AnnouncementsActivity.class
            );

            startActivity(browseIntent);
        });

        findViewById(R.id.navPublish).setOnClickListener(v -> {

            Intent publishIntent = new Intent(
                    HomeActivity.this,
                    PublishActivity.class
            );

            publishIntent.putExtra("user_id", userId);

            startActivity(publishIntent);
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // Already on Profile
        });
    }
}
