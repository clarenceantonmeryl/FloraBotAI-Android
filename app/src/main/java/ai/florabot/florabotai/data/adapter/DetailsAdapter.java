package ai.florabot.florabotai.data.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import ai.florabot.florabotai.R;
import ai.florabot.florabotai.data.model.vision.MetaTag;

public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder> {

    ArrayList<MetaTag> metaTags;
    Context context;

    public DetailsAdapter(ArrayList<MetaTag> metaTags, Context context) {
        this.metaTags = metaTags;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_details, parent, false);

        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull DetailsViewHolder holder, int position) {
        MetaTag metaTag = metaTags.get(position);

        holder.textViewDetailsTitle.setText(metaTag.getName());

        if (!metaTag.getType().equals("Best Guess")) {
            holder.textViewDetailsDescription.setText((int)(metaTag.getScore() * 100) + "%");
        } else {
            holder.textViewDetailsDescription.setText("");
        }

        if (metaTag.isAlert()) {

            holder.textViewDetailsTitle.setTextColor(Color.WHITE);
            holder.textViewDetailsDescription.setTextColor(Color.WHITE);
            holder.imageViewDetailsAlert.setVisibility(View.VISIBLE);

            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.living_coral));

        } else {
            holder.textViewDetailsTitle.setTextColor(Color.BLACK);
            holder.textViewDetailsDescription.setTextColor(Color.BLACK);
            holder.imageViewDetailsAlert.setVisibility(View.GONE);

            if (metaTag.getType().equals("Object")) {
                holder.itemView.setBackgroundColor(Color.LTGRAY);
            } else if (metaTag.getType().equals("Best Guess")) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.greenery));
                holder.textViewDetailsTitle.setTextColor(Color.WHITE);
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }

        }

    }

    @Override
    public int getItemCount() {
        return metaTags.size();
    }

    public class DetailsViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDetailsTitle;
        TextView textViewDetailsDescription;
        ImageView imageViewDetailsAlert;

        public DetailsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            textViewDetailsTitle = itemView.findViewById(R.id.textViewAlertName);
            textViewDetailsDescription = itemView.findViewById(R.id.textViewAlertActive);
            imageViewDetailsAlert = itemView.findViewById(R.id.imageViewDetailsAlert);

            /*
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
             */
        }
    }

}
