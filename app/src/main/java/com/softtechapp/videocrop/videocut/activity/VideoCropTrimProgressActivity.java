package com.softtechapp.videocrop.videocut.activity;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.material.snackbar.Snackbar;
import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.ActivityVideoCropTrimProgressBinding;
import com.softtechapp.videocrop.util.Help;

import java.io.File;
import java.util.Arrays;


public class VideoCropTrimProgressActivity extends AppCompatActivity {

    ActivityVideoCropTrimProgressBinding binding;
    static final float END_SCALE = 0.7f;
    Activity activity;
    Context context;

    Pair[] pairs = new Pair[1];

    CircleProgressBar circleProgressBar;
    int duration;
    int startPoint, endPoint;
    String path;

    long executionId;
    ServiceConnection serviceConnection;
    // FFMpegService ffMpegService;
    Integer res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoCropTrimProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = VideoCropTrimProgressActivity.this;
        context = VideoCropTrimProgressActivity.this;
        //Status bar colour and fullscreen and input auto hidden
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.topBarBk, typedValue, true);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView()).setAppearanceLightStatusBars(false);


        Intent intent;
        intent = getIntent();

        if (intent != null) {
            duration = intent.getIntExtra("duration", 0);
            startPoint = intent.getIntExtra("startPoint", 0);
            endPoint = intent.getIntExtra("endPoint", 0);
            path = intent.getStringExtra("path");


            initialize();
        } else {

        }


    }

    String trimmingMsg="Trimming";


    private void initialize() {

        String fileName = "VideCrop_"+ Help.getDateToStringDate("dd_MMM_YYYY_hh_mm_ss");
        trimVideo(startPoint, endPoint, path, fileName);
        clickHandel();
    }

    private void clickHandel() {


        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(binding.msg.getText().equals(trimmingMsg))
              {
                  new Handler(Looper.getMainLooper()).post(new Runnable() {
                      @Override
                      public void run() {
                          Snackbar.make(binding.getRoot(), "Dou you really want to cancel ?", Snackbar.LENGTH_LONG)
                                  .setAnchorView(binding.btnCancel)
                                  .setAction("Yes", vSnack -> {
                                      mHandler.removeCallbacks(mTicker);
                                      binding.circleProgressBar.setProgress(0);
                                      FFmpeg.cancel(executionId);
                                      binding.msg.setText("Canceled");

                                  }).show();
                      }
                  });
              }
              else if(binding.msg.getText().equals("Done")){

                  Intent intent = new Intent(context, VideoCropGalleryActivity.class);

                  activity.startActivity(intent);
                  activity.overridePendingTransition(R.anim.frag_fade_in, R.anim.frag_fade_out);
              }else {
                  VideoCropTrimProgressActivity.super.onBackPressed();
              }
            }
        });
    }
    File dest;

    private void trimVideo(int startMs, int endMs, String src, String fileName) {
        File folder;
        String folderName = "/VideoCrop";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            folder = new File (context.getExternalFilesDir(null) + folderName);
//        } else {
//            folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderName);
//        }
        folder = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)) + folderName);
        // folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderName);

        if (!folder.exists()) {
            boolean create = folder.mkdir();
        }

        if (!folder.exists()) {
            Toast.makeText(activity, "Can not create folder", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExt = ".mp4";


        dest = new File(folder, fileName + fileExt);
        int fileNo = 0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(folder, fileName + fileNo + fileExt);
        }


        int duration = (endMs - startMs) ;

        String[] command = new String[]{"-ss", "" + startMs / 1000, "-y", "-i", src, "-t", "" + (duration) / 1000, "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", dest.getAbsolutePath()};

        execFFMpegBinary(command);


    }

    Handler mHandler;
    Runnable mTicker;
    final int[] step = {1};

    private void execFFMpegBinary(final String[] command) {


        binding.msg.setText(trimmingMsg);


        Config.enableStatisticsCallback(new StatisticsCallback() {
            public void apply(Statistics newStatistics) {
                int progress = newStatistics.getTime() / (endPoint - startPoint);
                int progressFinal = progress * 100;

                if (progressFinal == 100) {

                    trimSuccess();

                }

            }
        });


        mHandler = new Handler();

        mTicker = new Runnable() {
            @Override
            public void run() {


                if (binding.circleProgressBar.getProgress() < 100) {
                    binding.circleProgressBar.setProgress(step[0]);
                    step[0]++;


                } else {

                }

                mHandler.postDelayed(mTicker, (long) (((endPoint - startPoint)) / 100));

            }
        };

        mHandler.postDelayed(mTicker, (long) (((endPoint - startPoint) * 1.1) / 100));

        Log.d("TAG", "Async command Started = " + Arrays.toString(command));


        executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {


                if (returnCode == RETURN_CODE_SUCCESS) {
                    //  progressDialog.dismiss();
                    mHandler.removeCallbacks(mTicker);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            trimSuccess();
                        }
                    });


                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.d("TAG", "Async command execution canceled by user");
                } else {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {


                            Toast.makeText(context, "Trimming Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.d("TAG", "Async command execution failed with returncode = " + returnCode + Arrays.toString(command));
                }
            }
        });

    }

    private void trimSuccess() {
        mHandler.removeCallbacks(mTicker);
        binding.circleProgressBar.setProgress(100);
        binding.msg.setText("Done");
        Toast.makeText(context, "Trimmed Successfully", Toast.LENGTH_SHORT).show();
        binding.btnCancelText.setText("Done Exporting");


        MediaScannerConnection.scanFile(context,
                new String[] { dest.getAbsolutePath().toString() },
                new String[]{"video/mp4"},
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        Log.d("TAG", "onScanCompleted: ");
                    }
                });
        Log.d("TAG", "trimSuccess: ");

    }


    @Override
    public void onBackPressed() {


        //Help.showBottomDialog(this,this);

        if (!binding.msg.getText().equals(trimmingMsg)) {


            finish();
            activity.overridePendingTransition(R.anim.frag_fade_in, R.anim.frag_fade_out);
        }

    }


}