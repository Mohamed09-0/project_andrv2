
package com.example.voisinage_plus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter
        extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private final Context context;
    private final List<Announcement> announcements;

    public AnnouncementAdapter(
            Context context,
            List<Announcement> announcements) {

        this.context = context;
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.item_announcement,
                parent,
                false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Announcement a = announcements.get(position);

        holder.title.setText(a.getTitre());

        String transaction = a.getTypeTransaction();
        String price = a.getPrix();

        if ("PRET".equalsIgnoreCase(transaction)) {
            holder.price.setText("Gratuit");
        } else if (!price.isEmpty()) {
            try {
                holder.price.setText(String.format(
                        Locale.US, "%.2f TND",
                        Double.parseDouble(price)
                ));
            } catch (NumberFormatException e) {
                holder.price.setText(price + " TND");
            }
        } else {
            holder.price.setText("Prix non spécifié");
        }

        holder.category.setText(
                a.getCategorie() + " | " + transaction
        );

        if (a.getDescription().isEmpty()) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(a.getDescription());
        }

        holder.condition.setText(
                "État : " + a.getEtatObjet()
        );

        String location = a.getVille();

        if (!a.getQuartier().isEmpty()) {
            if (!location.isEmpty()) location += " - ";
            location += a.getQuartier();
        }

        holder.location.setText("Localisation : " + location);

        holder.owner.setText(
                "Publié par : " + a.getPrenomUtilisateur()
                        + " " + a.getNomUtilisateur()
        );

        if (a.getPhotoUrl() != null) {
            Glide.with(context)
                    .load(a.getPhotoUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.image);
        } else {
            Glide.with(context)
                    .load(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title, price, category;
        TextView description, condition, location, owner;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imgAnnouncement);
            title = itemView.findViewById(R.id.tvAnnouncementTitle);
            price = itemView.findViewById(R.id.tvAnnouncementPrice);
            category = itemView.findViewById(R.id.tvAnnouncementCategory);
            description = itemView.findViewById(R.id.tvAnnouncementDescription);
            condition = itemView.findViewById(R.id.tvAnnouncementCondition);
            location = itemView.findViewById(R.id.tvAnnouncementLocation);
            owner = itemView.findViewById(R.id.tvAnnouncementOwner);
        }
    }
}
