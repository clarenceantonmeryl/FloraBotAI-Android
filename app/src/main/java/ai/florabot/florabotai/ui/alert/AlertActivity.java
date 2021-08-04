package ai.florabot.florabotai.ui.alert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import ai.florabot.florabotai.data.adapter.AlertAdapter;
import ai.florabot.florabotai.data.model.Alert;
import ai.florabot.florabotai.data.model.Photograph;
import ai.florabot.florabotai.databinding.ActivityAlertBinding;
import ai.florabot.florabotai.utility.SharedPreferencesKey;

public class AlertActivity extends AppCompatActivity implements AlertAdapter.OnItemClickListener {

    private final String TAG = "FloraBOT: " + AlertActivity.class.getName();

    SharedPreferences sharedPreferences;

    private ActivityAlertBinding binding;

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private AlertAdapter alertAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences(SharedPreferencesKey.NAME.KEY, Context.MODE_PRIVATE);

        authStateListener = this::authStateChanged;

        binding.buttonAlertAdd.setOnClickListener(this::onClickAdd);

        /*
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
         */


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
        if (alertAdapter != null) {
            alertAdapter.stopListening();
        }
    }

    private void onClickAdd(View view) {
        Log.d(TAG, "onClickAdd: " + binding.editTextAlertNew.getText().toString());

        Alert alert = new Alert(binding.editTextAlertNew.getText().toString().trim(), true);


        CollectionReference collectionReferenceAlerts = firebaseFirestore
                .collection("users")
                .document(firebaseUser.getUid())
                .collection("alerts");

        collectionReferenceAlerts.add(alert).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d(TAG, "onSuccess: Alert Added - " + alert.getName());
                binding.editTextAlertNew.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    private void authStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            if (alertAdapter != null) {
                alertAdapter.stopListening();
            }
        } else {
            setupRecyclerView();
            alertAdapter.startListening();
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

                ArrayList<Alert> alertsArray = new ArrayList<>();

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null) {
                        Alert alert = doc.toObject(Alert.class);
                        if (alert.isActive()) {
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
                .collection("alerts");

        Query query = collectionReferencePhotographs.orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Alert> options = new FirestoreRecyclerOptions.Builder<Alert>()
                .setQuery(query, Alert.class)
                .build();

        alertAdapter = new AlertAdapter(options);
        alertAdapter.setOnItemClickListener(this);

        binding.recyclerViewAlert.setHasFixedSize(true);
        binding.recyclerViewAlert.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAlert.setAdapter(alertAdapter);

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

                deleteAlert(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(binding.recyclerViewAlert);
    }

    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot, int position, boolean isChecked) {

        documentSnapshot.getReference().update("active", isChecked).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void deleteAlert(int position) {
        
        // Alert alert = alertAdapter.getItem(position);
        DocumentSnapshot documentSnapshot = alertAdapter.getDocumentSnapshot(position);
        Log.d(TAG, "deleteAlert: " + documentSnapshot.getReference().getId());

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

    /*
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Context context = getPreferenceManager().getContext();
            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

            SwitchPreferenceCompat notificationPreference = new SwitchPreferenceCompat(context);
            notificationPreference.setKey("notifications");
            notificationPreference.setTitle("Enable message notifications");

            PreferenceCategory notificationCategory = new PreferenceCategory(context);
            notificationCategory.setKey("notifications_category");
            notificationCategory.setTitle("Notifications");
            screen.addPreference(notificationCategory);
            notificationCategory.addPreference(notificationPreference);

            Preference feedbackPreference = new Preference(context);
            feedbackPreference.setKey("feedback");
            feedbackPreference.setTitle("Send feedback");
            feedbackPreference.setSummary("Report technical issues or suggest new features");

            PreferenceCategory helpCategory = new PreferenceCategory(context);
            helpCategory.setKey("help");
            helpCategory.setTitle("Help");
            screen.addPreference(helpCategory);
            helpCategory.addPreference(feedbackPreference);

            setPreferenceScreen(screen);
        }
    }
    */
}