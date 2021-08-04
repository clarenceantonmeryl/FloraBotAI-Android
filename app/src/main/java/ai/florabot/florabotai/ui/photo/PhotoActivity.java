package ai.florabot.florabotai.ui.photo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import ai.florabot.florabotai.data.model.Photograph;
import ai.florabot.florabotai.databinding.ActivityPhotoBinding;

/**
 * The PhotoActivity to capture the image.
 *
 * @author  Clarence ANTONMERYL
 * @version 1.0
 * @since   2021-05-03
 */

public class PhotoActivity extends AppCompatActivity {

    private final String TAG = "FloraBOT: " + PhotoActivity.class.getName();

    private ActivityPhotoBinding binding;

    //region Firebase
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private final CollectionReference collectionReferencePhotographs = firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("photographs");

    private final StorageReference storageReference = firebaseStorage.getReference();
    private final StorageReference storageReferencePhotographs = storageReference.child("users").child(firebaseUser.getUid()).child("photographs");
    //endregion

    //region CameraX
    private ImageCapture imageCapture;
    OrientationEventListener orientationEventListener;
    private boolean isLensFrontFacing = false;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        binding.imageButtonCamera.setOnClickListener(this::snapPhoto);
        binding.imageButtonGallery.setOnClickListener(this::galleryPhoto);
        binding.imageButtonToggle.setOnClickListener(this::toggleCamera);

        //region CameraX
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = Surface.ROTATION_0;
                //Log.d(TAG, "onOrientationChanged: " + orientation);
                // Monitors orientation values to determine the target rotation value
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                }

                if (imageCapture != null) imageCapture.setTargetRotation(rotation);
            }
        };
        setupCamera(isLensFrontFacing);
        //endregion
    }

    @Override
    protected void onStart() {
        super.onStart();

        orientationEventListener.enable();
    }

    @Override
    protected void onStop() {
        super.onStop();

        orientationEventListener.disable();
    }

    //region CameraX

    private void setupCamera(boolean isFrontFacingLens) {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                cameraProvider.unbindAll();

                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();

                // Set up the capture use case to allow users to take photos
                imageCapture = new ImageCapture.Builder()
                        //.setFlashMode(ImageCapture.FLASH_MODE_ON)
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        //.setTargetRotation(Surface.ROTATION_0)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector;

                if (isFrontFacingLens) {
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();
                } else {
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();
                }

                // Attach use cases to the camera with the same lifecycle owner
                Camera camera = cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview,
                        imageCapture);

                // Connect the preview use case to the previewView
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get()
                // shouldn't block since the listener is being called, so no need to
                // handle InterruptedException.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent intent = result.getData();
                assert intent != null;
                Log.d(TAG, "onActivityResult: " + intent.getData().toString());
                binding.imageViewSnap.setImageURI(intent.getData());
                addPhotograph(intent.getData(), 0);
            }
        }
    });

    private void toggleCamera(View view) {
        isLensFrontFacing = !isLensFrontFacing;
        setupCamera(isLensFrontFacing);
    }

    private void galleryPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intentActivityResultLauncher.launch(intent);
    }

    private void snapPhoto(View view) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "FloraBotAI" + new Date().hashCode());
        contentValues.put(MediaStore.Images.Media.ALBUM, "FloraBotAI");
        contentValues.put(MediaStore.Images.Media.AUTHOR, "FloraBotAI");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "FloraBotAI");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NotNull ImageCapture.OutputFileResults outputFileResults) {
                        // insert your code here.
                        Log.d(TAG, "onImageSaved: URI IS " + outputFileResults.getSavedUri());
                        // binding.imageViewSnap.setImageURI(outputFileResults.getSavedUri()); //.setImageBitmap(bitmap1);

                        binding.imageViewSnap.setImageURI(outputFileResults.getSavedUri());
                        addPhotograph(outputFileResults.getSavedUri(), getRotation());
                        /*
                        try {
                            Matrix matrix = new Matrix();
                            //matrix.setRotate(90);
                            if (isLensFrontFacing) {
                                // matrix.preScale(-1.0f, 1.0f);
                            }

                            final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(outputFileResults.getSavedUri()), null, null);
                            Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            //binding.imageViewSnap.setImageBitmap(bitmap1);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        */
                    }
                    @Override
                    public void onError(@NotNull ImageCaptureException error) {
                        // insert your code here.
                        Log.d(TAG, "onError: " + error.getMessage());
                    }
                }
        );
    }
    //endregion

    //region Firebase

    private void addPhotograph(Uri uri, int rotation) {

        DocumentReference documentReferencePhotograph = collectionReferencePhotographs.document();

        StorageReference storageReferencePhotograph = storageReferencePhotographs.child(documentReferencePhotograph.getId());

        storageReferencePhotograph
                .putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: ");
                        getDownloadUrl(storageReferencePhotograph, documentReferencePhotograph, rotation);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
    }

    private void getDownloadUrl(StorageReference storageReferencePhotograph, DocumentReference documentReferencePhotograph, int rotation) {

        storageReferencePhotograph.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                savePhotograph(documentReferencePhotograph, uri.toString(), rotation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d(TAG, "onFailure: ");
            }
        });
    }

    private void savePhotograph(DocumentReference documentReferencePhotograph, String url, int rotation) {
        Photograph photograph = new Photograph(url, rotation);
        documentReferencePhotograph.set(photograph).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private int getRotation() {

        int rotation = 0;
        if(isLensFrontFacing) {
            if (imageCapture.getTargetRotation() == Surface.ROTATION_0) {
                rotation = 270;
            } else if (imageCapture.getTargetRotation() == Surface.ROTATION_90) {
                rotation = 0;
            } else if (imageCapture.getTargetRotation() == Surface.ROTATION_180) {
                rotation = 90;
            } else if (imageCapture.getTargetRotation() == Surface.ROTATION_270) {
                rotation = 180;
            }
        } else {
            if (imageCapture.getTargetRotation() == Surface.ROTATION_0) {
                rotation = 90;
            } else if (imageCapture.getTargetRotation() == Surface.ROTATION_90) {
                rotation = 0;
            } else if (imageCapture.getTargetRotation() == Surface.ROTATION_180) {
                rotation = 270;
            } else if (imageCapture.getTargetRotation() == Surface.ROTATION_270) {
                rotation = 180;
            }
        }

        return rotation;
    }

    //endregion

}