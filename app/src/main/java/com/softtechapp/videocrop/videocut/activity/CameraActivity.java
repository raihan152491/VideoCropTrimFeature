package com.softtechapp.videocrop.videocut.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.ActivityCameraBinding;
import com.softtechapp.videocrop.util.Help;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {

    ActivityCameraBinding binding;
    static final float END_SCALE = 0.7f;
    Activity activity;
    Context context;
    Animation topAnim, bottomAnim, leftAnim, rightAnim;
    Pair[] pairs = new Pair[1];


    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;

    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    boolean rotate = false;
    boolean record = true;
    boolean enableRecord = false;
    boolean previewFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = CameraActivity.this;
        context = CameraActivity.this;
        //Status bar colour and fullscreen and input auto hidden
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.topBarBk, typedValue, true);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView()).setAppearanceLightStatusBars(true);


        initialize();


    }

    private void initialize() {


        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {

            try {

                cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider, "back");
            }
            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());

        clickHandler();
        observer();

    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(context);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider, String action) {

        cameraProvider.unbindAll();
        CameraSelector cameraSelector;
        if (action.equals("back")) {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
        } else {
            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
        }
        Preview preview = new Preview.Builder().build();


        preview.setSurfaceProvider(binding.preview.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) activity, cameraSelector, preview, imageCapture, videoCapture);


    }

    private void observer() {


    }


    private void clickHandler() {


        binding.enableVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                enableRecord = !enableRecord;

                if (enableRecord) {
                    binding.enableVideo.setColorFilter(ResourcesCompat.getColor(getResources(), R.color.red, null));
                    binding.captureText.setText("Record");
                    binding.btnCapture.setCardBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
                } else {
                    binding.enableVideo.setColorFilter(ResourcesCompat.getColor(getResources(), R.color.white, null));

                    binding.captureText.setText("Capture");
                    binding.btnCapture.setCardBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.white, null));

                }
            }
        });

        binding.btnCapture.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {

                if (!previewFile) {
                    if (enableRecord) {
                        if (record) {
                            binding.captureText.setText("Stop");
                            Toast.makeText(activity, "Start Recording", Toast.LENGTH_SHORT).show();
                            captureVideo();
                        } else {
                            binding.captureText.setText("Record");
                            Toast.makeText(activity, "Stop Recording", Toast.LENGTH_SHORT).show();
                            videoCapture.stopRecording();
                        }
                        record = !record;
                    } else {
                        capturePhoto();
                    }

                } else {
                    backHandle();
                }


            }
        });
        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"MissingPermission", "RestrictedApi"})
            @Override
            public void onClick(View v) {


            }
        });
        binding.btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObjectAnimator animator = ObjectAnimator.ofFloat(binding.btnRotate, "scaleX", 0.0f).setDuration(100);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {

                        ObjectAnimator.ofFloat(binding.btnRotate, "scaleX", 1.0f).setDuration(100).start();
                    }
                });
                animator.start();

                if (cameraProvider != null) {
                    if (rotate) {
                        startCameraX(cameraProvider, "back");

                    } else {
                        startCameraX(cameraProvider, "front");
                    }
                    rotate = !rotate;

                }

            }
        });

    }

    @SuppressLint("RestrictedApi")
    private void captureVideo() {


        if (videoCapture == null) {
            return;
        }

        File photoFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/VideoCropVideo");
        if (!photoFolder.exists()) {
            boolean create = photoFolder.mkdir();
        }

        long time = System.currentTimeMillis();
        File filePath = new File(photoFolder, time + ".mp4");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, time);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + "/VideoCropVideo/");
        } else {
            String path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES) + "/VideoCropVideo/" + time;

            contentValues.put(MediaStore.Images.Media.DATA, path);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(activity, "Please give permission to access camera and audio", Toast.LENGTH_SHORT).show();
            return;
        }
        videoCapture.startRecording(
                new VideoCapture.OutputFileOptions.Builder(

                        getContentResolver(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {

                        Toast.makeText(activity, "Video Recorded", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        Toast.makeText(activity, "Error Saving video" + message, Toast.LENGTH_SHORT).show();

                    }
                }


        );


    }

    private void capturePhoto() {

        if (imageCapture == null) {
            return;
        }

        File photoFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/VideoCropImage/");
        if (!photoFolder.exists()) {
            boolean create = photoFolder.mkdir();
        }

        long time = System.currentTimeMillis();
        File filePath = new File(photoFolder, time + ".jpg");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, time);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/VideoCropImage/");
        } else {
            String path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/VideoCropImage/" + time;

            contentValues.put(MediaStore.Images.Media.DATA, path);
        }

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {


                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {


                        binding.imagePreview.setVisibility(View.VISIBLE);
                        previewFile = true;
                        Glide.with(binding.getRoot()).load(
                                new File(filePath.getPath())
                        ).into(binding.imagePreview);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(activity, "Error Saving Photo" + exception, Toast.LENGTH_SHORT).show();

                    }
                }
        );
    }


    @Override
    public void onBackPressed() {


        backHandle();


    }

    private void backHandle() {

        binding.preview.setVisibility(View.VISIBLE);
        //Help.showBottomDialog(this,this);
        if (previewFile) {
            previewFile = false;
            binding.imagePreview.setVisibility(View.GONE);

        } else {
            finish();
            activity.overridePendingTransition(R.anim.frag_fade_in, R.anim.frag_fade_out);
        }
    }

}