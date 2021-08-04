package ai.florabot.florabotai.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ai.florabot.florabotai.R;
import ai.florabot.florabotai.data.adapter.HomeAdapter;
import ai.florabot.florabotai.data.model.Alert;
import ai.florabot.florabotai.data.model.Photograph;
import ai.florabot.florabotai.databinding.FragmentHomeBinding;
import ai.florabot.florabotai.ui.details.DetailsActivity;
import ai.florabot.florabotai.utility.SharedPreferencesKey;

public class HomeFragment extends Fragment implements HomeAdapter.OnItemClickListener {

    private final String TAG = "FloraBOT: " + HomeFragment.class.getName();

    SharedPreferences sharedPreferences;

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private final StorageReference storageReference = firebaseStorage.getReference();

    private HomeAdapter homeAdapter;

    private ArrayList<String> subscribedAlerts = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        sharedPreferences = requireContext().getSharedPreferences(SharedPreferencesKey.NAME.KEY, Context.MODE_PRIVATE);
        initAlerts();

        View root = binding.getRoot();

        authStateListener = this::authStateChanged;

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        firebaseAuth.removeAuthStateListener(authStateListener);
        if (homeAdapter != null) {
            homeAdapter.stopListening();
        }
    }

    private void initAlerts() {
        String alertsString = sharedPreferences.getString(SharedPreferencesKey.ALERTS.KEY, "").trim();

        subscribedAlerts = new ArrayList<>();

        if (alertsString.length() > 1) {
            String[] alertsArray = alertsString.split(",");
            Collections.addAll(subscribedAlerts, alertsArray);
        }
    }

    private void authStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            if (homeAdapter != null) {
                homeAdapter.stopListening();
            }
        } else {
            setupRecyclerView();
            homeAdapter.startListening();
            setupAlertListener();
        }
    }

    private void setupAlertListener() {

        CollectionReference collectionReferenceAlerts = firebaseFirestore
                .collection("users")
                .document(firebaseUser.getUid())
                .collection("alerts");

        collectionReferenceAlerts.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                subscribedAlerts = new ArrayList<>();

                ArrayList<Alert> alertsArray = new ArrayList<>();

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null) {
                        Alert alert = doc.toObject(Alert.class);
                        if (alert.isActive()) {
                            subscribedAlerts.add(alert.getName());
                            alertsArray.add(alert);
                        }
                    }
                }

                String alertsString = alertsArray.toString();
                alertsString = alertsString.replace("[", "");
                alertsString = alertsString.replace("]", "");
                alertsString = alertsString.replace(", ", ",");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SharedPreferencesKey.ALERTS.KEY, alertsString);

                editor.apply();
            }
        });
    }

    private void setupRecyclerView() {

        Log.d(TAG, "setupRecyclerView: ");
        CollectionReference collectionReferencePhotographs = firebaseFirestore
                .collection("users")
                .document(firebaseUser.getUid())
                .collection("photographs");

        Query query = collectionReferencePhotographs.orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Photograph> options = new FirestoreRecyclerOptions.Builder<Photograph>()
                .setQuery(query, Photograph.class)
                .build();

        homeAdapter = new HomeAdapter(options, subscribedAlerts);
        homeAdapter.setOnItemClickListener(this);

        binding.recyclerViewHome.setHasFixedSize(true);
        binding.recyclerViewHome.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewHome.setAdapter(homeAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == ItemTouchHelper.LEFT){
                    Log.d(TAG,"Left");
                } else if (direction == ItemTouchHelper.RIGHT) {
                    Log.d(TAG,"Right");
                }

                Log.wtf(TAG, "Swipe at: " + viewHolder.getAdapterPosition() + " Direction: " + direction);

                deletePhotograph(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(binding.recyclerViewHome);
    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

        Photograph photograph = documentSnapshot.toObject(Photograph.class);

        Log.d(TAG, "onItemClick: " + documentSnapshot.getId() + " "  + position + " " + photograph.getUrl());

        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra("UserId", firebaseUser.getUid());
        intent.putExtra("PhotographId", documentSnapshot.getId());

        startActivity(intent);
    }

    private void deletePhotograph(int position) {
        Photograph photograph = homeAdapter.getItem(position);
        DocumentSnapshot documentSnapshot = homeAdapter.getDocumentSnapshot(position);
        Log.d(TAG, "deletePhotograph: " + documentSnapshot.getReference().getId());

        StorageReference storageReferencePhotographs = storageReference.child("users").child(firebaseUser.getUid()).child("photographs").child(documentSnapshot.getReference().getId());
        storageReferencePhotographs.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                documentSnapshot.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d(TAG, "onFailure: ");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

}