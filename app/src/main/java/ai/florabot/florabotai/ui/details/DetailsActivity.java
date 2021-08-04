package ai.florabot.florabotai.ui.details;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import ai.florabot.florabotai.R;
import ai.florabot.florabotai.data.adapter.DetailsAdapter;
import ai.florabot.florabotai.data.model.vision.MetaTag;
import ai.florabot.florabotai.data.model.vision.VisionInfo;
import ai.florabot.florabotai.data.model.vision.imagepropertiesannotation.DominantColor;
import ai.florabot.florabotai.data.model.vision.labelannotations.LabelAnnotation;
import ai.florabot.florabotai.data.model.Photograph;
import ai.florabot.florabotai.data.model.vision.localizedobjectannotations.LocalizedObjectAnnotation;
import ai.florabot.florabotai.data.model.vision.webdetection.BestGuessLabel;
import ai.florabot.florabotai.databinding.ActivityDetailsBinding;
import ai.florabot.florabotai.utility.SharedPreferencesKey;

/**
 * The DetailsActivity displays the image, color spectrum
 * and AI identified details of the photograph.
 *
 * @author  Clarence ANTONMERYL
 * @version 1.0
 * @since   2021-05-03
 */

public class DetailsActivity extends AppCompatActivity {

    private final String TAG = "FloraBOT: " + DetailsActivity.class.getName();

    SharedPreferences sharedPreferences;

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private ActivityDetailsBinding binding;

    private ReflectiveTypeAdapterFactory.Adapter<String> arrayAdapter;

    private ArrayList<String> subscribedAlerts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences(SharedPreferencesKey.NAME.KEY, Context.MODE_PRIVATE);
        initAlerts();


        Bundle bundle = getIntent().getExtras();

        String userId = bundle.getString("UserId");
        String photographId = bundle.getString("PhotographId");

        Log.d(TAG, "onCreate: " + userId + " -> " +  photographId);
        getDetails(userId, photographId);
    }

    private void initAlerts() {
        String alertsString = sharedPreferences.getString(SharedPreferencesKey.ALERTS.KEY, "");
        String[] alertsArray = alertsString.split(",");
        for (String alert : alertsArray) {
            subscribedAlerts.add(alert);
            Log.d(TAG, "initAlerts: " + alert);
        }
    }

    private void getDetails(String userId, String photographId) {

        DocumentReference documentReference = firebaseFirestore
                .collection("users")
                .document(userId)
                .collection("photographs")
                .document(photographId);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Photograph photograph = documentSnapshot.toObject(Photograph.class);
                displayDetails(photograph);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {

            }
        });

    }

    private void displayDetails(Photograph photograph) {

        Picasso.get()
                .load(photograph.getUrl())
                .resize(binding.imageViewDetailsPhoto.getMeasuredWidth(), binding.imageViewDetailsPhoto.getMeasuredHeight())
                .centerCrop()
                .placeholder(R.drawable.logo_florabotai)
                .rotate(photograph.getRotation())
                .into(binding.imageViewDetailsPhoto);

        // String timeAgo = (String) DateUtils.getRelativeTimeSpanString(photograph.getCreatedAt().getSeconds() * 1000);
        // binding.textViewDetailsTimestamp.setText(timeAgo);

        int spectrumWidth = binding.imageViewSpectrum.getMeasuredWidth();
        int spectrumHeight = binding.imageViewSpectrum.getMeasuredHeight();

        String[] abc = new String[4];
        VisionInfo visionInfo = new VisionInfo(photograph.getVisionData(), subscribedAlerts, spectrumWidth, spectrumHeight);

        String title = visionInfo.getName();
        String bestGuess = visionInfo.getBestGuess();

        ArrayList<MetaTag> metaTags = visionInfo.getMetaTags();

        setTitle(title);
        // binding.textViewDetailsBestGuess.setText(bestGuess.toUpperCase());

        if (metaTags.size() > 0) {
            DetailsAdapter detailsAdapter = new DetailsAdapter(metaTags, getApplicationContext());
            binding.recyclerViewDetails.setHasFixedSize(true);
            binding.recyclerViewDetails.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewDetails.setAdapter(detailsAdapter);
        }

        if (photograph.getVisionData().getImagePropertiesAnnotation() != null) {
            binding.imageViewSpectrum.setImageBitmap(visionInfo.getBitmap());
        }
    }

    public static class LabelComparator implements Comparator<LabelAnnotation>
    {
        @Override
        public int compare(LabelAnnotation o1, LabelAnnotation o2) {
            return (int)(o2.getScore() * 100) - (int)(o1.getScore() * 100);
        }
    }

    public static class ColorComparator implements Comparator<DominantColor>
    {
        @Override
        public int compare(DominantColor o1, DominantColor o2) {
            return (int)(o2.getScore() * 100) - (int)(o1.getScore() * 100);
        }
    }


}