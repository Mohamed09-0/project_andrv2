
package com.example.voisinage_plus;

import org.json.JSONArray;
import org.json.JSONObject;

public class Announcement {

    private int id;
    private String titre;
    private String description;
    private String typeTransaction;
    private String prix;
    private String etatObjet;
    private String disponibilite;
    private String ville;
    private String quartier;
    private String datePublication;

    private String categorie;
    private String nomUtilisateur;
    private String prenomUtilisateur;

    private String photoUrl;

    public Announcement(
            int id,
            String titre,
            String description,
            String typeTransaction,
            String prix,
            String etatObjet,
            String disponibilite,
            String ville,
            String quartier,
            String datePublication,
            String categorie,
            String nomUtilisateur,
            String prenomUtilisateur,
            String photoUrl
    ) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.typeTransaction = typeTransaction;
        this.prix = prix;
        this.etatObjet = etatObjet;
        this.disponibilite = disponibilite;
        this.ville = ville;
        this.quartier = quartier;
        this.datePublication = datePublication;
        this.categorie = categorie;
        this.nomUtilisateur = nomUtilisateur;
        this.prenomUtilisateur = prenomUtilisateur;
        this.photoUrl = photoUrl;
    }

    // Getters

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public String getTypeTransaction() {
        return typeTransaction;
    }

    public String getPrix() {
        return prix;
    }

    public String getEtatObjet() {
        return etatObjet;
    }

    public String getDisponibilite() {
        return disponibilite;
    }

    public String getVille() {
        return ville;
    }

    public String getQuartier() {
        return quartier;
    }

    public String getDatePublication() {
        return datePublication;
    }

    public String getCategorie() {
        return categorie;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public String getPrenomUtilisateur() {
        return prenomUtilisateur;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    // Convert JSON object into Announcement

    public static Announcement fromJson(JSONObject json) {

        int id = json.optInt("id");

        String titre = json.optString("titre");
        String description = json.optString("description");

        String typeTransaction =
                json.optString("type_transaction");

        String prix = json.optString("prix");
        String etatObjet = json.optString("etat_objet");

        String disponibilite =
                json.optString("disponibilite");

        String ville = json.optString("ville");
        String quartier = json.optString("quartier");

        String datePublication =
                json.optString("date_publication");

        JSONObject categoryObject =
                json.optJSONObject("categorie");

        String categorie = "";

        if (categoryObject != null) {
            categorie = categoryObject.optString("libelle");
        }

        JSONObject userObject =
                json.optJSONObject("utilisateur");

        String nom = "";
        String prenom = "";

        if (userObject != null) {
            nom = userObject.optString("nom");
            prenom = userObject.optString("prenom");
        }

        // Get first announcement photo
        String photoUrl = null;

        JSONArray photos = json.optJSONArray("photos");

        if (photos != null && photos.length() > 0) {

            JSONObject photo = photos.optJSONObject(0);

            if (photo != null) {

                String path = photo.optString("url_photo");

                if (!path.isEmpty() && !path.equals("null")) {

                    if (path.startsWith("http")) {
                        photoUrl = path;
                    } else {
                        photoUrl =
                                "http://192.168.0.88:8000" + path;
                    }
                }
            }
        }

        return new Announcement(
                id,
                titre,
                description,
                typeTransaction,
                prix,
                etatObjet,
                disponibilite,
                ville,
                quartier,
                datePublication,
                categorie,
                nom,
                prenom,
                photoUrl
        );
    }
}
