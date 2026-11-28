
package com.example.voisinage_plus;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class AnnouncementsActivity extends AppCompatActivity {

    private static final String API_URL =
            "http://192.168.0.88:8000/api/annonces";

    private RecyclerView recyclerAnnouncements;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private ArrayList<Announcement> announcements;
    private AnnouncementAdapter adapter;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        recyclerAnnouncements =
                findViewById(R.id.recyclerAnnouncements);

        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        announcements = new ArrayList<>();

        recyclerAnnouncements.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new AnnouncementAdapter(this, announcements);
        recyclerAnnouncements.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        loadAnnouncements();
    }

    private void loadAnnouncements() {

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,

                response -> {
                    progressBar.setVisibility(View.GONE);

                    if (!response.optBoolean("success")) {
                        showMessage("Impossible de charger les annonces");
                        return;
                    }

                    try {
                        JSONArray array =
                                response.getJSONArray("annonces");

                        announcements.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);

                            announcements.add(
                                    Announcement.fromJson(object)
                            );
                        }

                        adapter.notifyDataSetChanged();

                        if (announcements.isEmpty()) {
                            showMessage("Aucune annonce disponible");
                        }

                    } catch (Exception e) {
                        showMessage("Erreur JSON : " + e.getMessage());
                    }
                },

                error -> {
                    progressBar.setVisibility(View.GONE);

                    String message = "Erreur de connexion au serveur";

                    if (error.networkResponse != null) {
                        message += " (HTTP "
                                + error.networkResponse.statusCode + ")";
                    }

                    showMessage(message);
                }
        );

        requestQueue.add(request);
    }

    private void showMessage(String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }
}
