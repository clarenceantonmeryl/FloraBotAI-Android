package ai.florabot.florabotai.data.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import ai.florabot.florabotai.R;
import ai.florabot.florabotai.data.model.Alert;

public class AlertAdapter extends FirestoreRecyclerAdapter<Alert, AlertAdapter.AlertViewHolder> {

    private final String TAG = "FloraBOT: " + AlertAdapter.class.getName();

    private AlertAdapter.OnItemClickListener listener;

    public AlertAdapter(@NonNull @NotNull FirestoreRecyclerOptions<Alert> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull @NotNull AlertAdapter.AlertViewHolder holder, int position, @NonNull @NotNull Alert model) {

        holder.switchMaterialAlert.setText(model.getName());
        holder.switchMaterialAlert.setChecked(model.isActive());
    }

    @NonNull
    @NotNull
    @Override
    public AlertAdapter.AlertViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_alert, parent, false);

        return new AlertAdapter.AlertViewHolder(view);
    }

    public class AlertViewHolder extends RecyclerView.ViewHolder {

        SwitchMaterial switchMaterialAlert;

        public AlertViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            switchMaterialAlert = itemView.findViewById(R.id.switchMaterialAlert);

            switchMaterialAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getAdapterPosition();
                    listener.onItemClick(getSnapshots().getSnapshot(position), position, isChecked);
                }
            });
        }
    }

    public DocumentSnapshot getDocumentSnapshot(int position) {

        return getSnapshots().getSnapshot(position);
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position, boolean isChecked);
    }

    public void setOnItemClickListener(AlertAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}