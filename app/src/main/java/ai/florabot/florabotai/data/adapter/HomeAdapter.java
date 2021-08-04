package ai.florabot.florabotai.data.adapter;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import ai.florabot.florabotai.R;
import ai.florabot.florabotai.data.model.Photograph;
import ai.florabot.florabotai.data.model.vision.VisionInfo;

public class HomeAdapter extends FirestoreRecyclerAdapter<Photograph, HomeAdapter.PhotographViewHolder> {

    private final String TAG = "FloraBOT: " + HomeAdapter.class.getName();

    private ArrayList<String> subscribedAlerts;
    private OnItemClickListener listener;

    public HomeAdapter(@NonNull @NotNull FirestoreRecyclerOptions<Photograph> options, ArrayList<String> subscribedAlerts) {
        super(options);
        this.subscribedAlerts = subscribedAlerts;
    }

    @Override
    protected void onBindViewHolder(@NonNull @NotNull PhotographViewHolder holder, int position, @NonNull @NotNull Photograph model) {

        Picasso.get()
                .load(model.getUrl())
                .resize(100, 100)
                .centerCrop()
                .placeholder(R.drawable.logo_florabotai)
                .rotate(model.getRotation())
                .into(holder.imageViewHomeThumbnail);

        String[] abc = new String[4];
        VisionInfo visionInfo = new VisionInfo(model.getVisionData(), subscribedAlerts);

        String title = visionInfo.getName();

        holder.textViewHomeName.setText(title);
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(model.getCreatedAt().getSeconds() * 1000);
        holder.textViewHomeTimestamp.setText(timeAgo);

        holder.textViewHomeAlert.setText("");
        holder.imageViewHomeAlert.setVisibility(View.INVISIBLE);

        if (visionInfo.isAlert()) {
            String alerts = visionInfo.getAlerts().toString();
            alerts = alerts.replace("[", "");
            alerts = alerts.replace("]", "");
            holder.textViewHomeAlert.setText(alerts);
            holder.imageViewHomeAlert.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @NotNull
    @Override
    public PhotographViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_home, parent, false);

        return new PhotographViewHolder(view);
    }

    public class PhotographViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewHomeThumbnail;
        TextView textViewHomeName;
        TextView textViewHomeAlert;
        ImageView imageViewHomeAlert;
        TextView textViewHomeTimestamp;

        public PhotographViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            imageViewHomeThumbnail = itemView.findViewById(R.id.imageViewHomeThumbnail);
            textViewHomeName = itemView.findViewById(R.id.textViewHomeName);
            textViewHomeAlert = itemView.findViewById(R.id.textViewHomeAlert);
            imageViewHomeAlert = itemView.findViewById(R.id.imageViewHomeAlert);
            textViewHomeTimestamp = itemView.findViewById(R.id.textViewHomeTimestamp);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public DocumentSnapshot getDocumentSnapshot(int position) {

        return getSnapshots().getSnapshot(position);
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}