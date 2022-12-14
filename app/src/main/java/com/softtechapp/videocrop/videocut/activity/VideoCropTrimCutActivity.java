package com.softtechapp.videocrop.videocut.activity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.slider.RangeSlider;
import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.ActivityVideoCropTrimCutBinding;
import com.softtechapp.videocrop.util.Help;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModel;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModelAdapter;

import java.util.Arrays;
import java.util.Locale;


public class VideoCropTrimCutActivity extends AppCompatActivity {

    ActivityVideoCropTrimCutBinding binding;
    static final float END_SCALE = 0.7f;
    Activity activity;
    Context context;
    Animation topAnim, bottomAnim, leftAnim, rightAnim;
    Pair[] pairs = new Pair[1];
    Handler mHandler;
    Runnable mTicker;
    boolean goBackground = false;
    long lastProgress = 0;
    VideoModel currentVideo;

    int startPoint = 0;
    int endPoint = 100;

    int trimStartPoint = 0;
    int trimEndPoint = 100;
    Uri uri;
    boolean initSuccess;
    String TAG = "TAG";

    public static boolean trimSync = false;

    public static MutableLiveData<Boolean> trim = new MutableLiveData<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoCropTrimCutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = VideoCropTrimCutActivity.this;
        context = VideoCropTrimCutActivity.this;
        //Status bar colour and fullscreen and input auto hidden


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.topBarBk, typedValue, true);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView()).setAppearanceLightStatusBars(false);


        initialize();

    }

    MutableLiveData<Boolean> play = new MutableLiveData<>();

    private void initialize() {


        play = new MutableLiveData<>(false);

        clickHandle();

        observer();


    }


    private void clickHandle() {


        binding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (initSuccess) {
                    trimSync = true;
                    binding.videoView.pause();
                    VideoCropEditActivity.lastProgress = trimStartPoint;
                    VideoCropEditActivity.startPoint = trimStartPoint;
                    VideoCropEditActivity.endPoint = trimEndPoint;

                    trim.postValue(true);
                    backHandle();
                }


            }
        });
        binding.btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);
            }
        });

        binding.btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);

            }
        });
        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);

            }
        });
        binding.btnSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);
                ObjectAnimator animator = ObjectAnimator.ofFloat(binding.seekBarLayout, "scaleY", 0.0f).setDuration(100);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {

                        ObjectAnimator.ofFloat(binding.seekBarLayout, "scaleY", 1.0f).setDuration(100).start();
                    }
                });
                animator.start();

            }
        });
        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (play.getValue() != null) {
                    play.postValue(!play.getValue());
                } else {
                    play.postValue(false);
                }

            }
        });


        binding.videoViewFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (play.getValue() != null) {
                    play.postValue(!play.getValue());
                } else {
                    play.postValue(false);
                }


            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.videoView.pause();
                trim.postValue(false);
                backHandle();
            }
        });


    }

    private void observer() {


        RangeSlider.OnChangeListener changeListener = new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                String st = String.format(Locale.getDefault(), "%.0f", slider.getValues().get(0) + 1);
                String en = String.format(Locale.getDefault(), "%.0f", slider.getValues().get(1) + 1);

                try {
                    int tst = Integer.parseInt(st);

                    int ten = Integer.parseInt(en);


                    trimStartPoint = Integer.parseInt(st);
                    trimEndPoint = Integer.parseInt(en);


                }
                catch (NumberFormatException e) {
                    Toast.makeText(activity, "" + e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                trimTextUpdate();


            }
        };
        binding.doubleRangeSeekbar.addOnChangeListener(changeListener);


        RangeSlider.OnSliderTouchListener touchListener = new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
//                Toast.makeText(activity, ""+ Arrays.toString(slider.getValues().toArray()), Toast.LENGTH_SHORT).show();

                binding.videoView.pause();
                play.postValue(false);
                trimTextUpdate();

            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {

                String st = String.format(Locale.getDefault(), "%.0f", slider.getValues().get(0) + 1);
                String en = String.format(Locale.getDefault(), "%.0f", slider.getValues().get(1) + 1);

                try {


                    trimStartPoint = Integer.parseInt(st);
                    trimEndPoint = Integer.parseInt(en);
                }
                catch (NumberFormatException e) {
                    Toast.makeText(activity, "" + e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                trimTextUpdate();

                seekBarLogic();


            }
        };

        binding.doubleRangeSeekbar.addOnSliderTouchListener(touchListener);

        binding.videoSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if (binding.videoView.canPause()) {
                    play.postValue(false);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                binding.videoView.seekTo(seekBar.getProgress());
            }
        });

        binding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mHandler != null) {
                    mHandler.removeCallbacks(mTicker);
                }

                binding.videoView.start();
                binding.videoSeekbar.setProgress(trimStartPoint);

                play.postValue(false);

            }
        });
        binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                seekBarLogic();
                if (mediaPlayer != null) {
                    if (mediaPlayer.getVideoWidth() > mediaPlayer.getVideoHeight()) {
                        binding.videoView.getLayoutParams().width = MATCH_PARENT;
                    }
                    if (mediaPlayer.getVideoWidth() == mediaPlayer.getVideoHeight()) {
                        binding.videoView.getLayoutParams().width = MATCH_PARENT;
                        binding.videoView.getLayoutParams().height = MATCH_PARENT;
                    }
                }


                mHandler = new Handler();

                mTicker = new Runnable() {
                    @Override
                    public void run() {

                        binding.videoSeekbar.setProgress(binding.videoView.getCurrentPosition());


                        if (
                                binding.videoView.getCurrentPosition() > (trimEndPoint)

                        ) {


                            Log.d(TAG, "run: true");

                            play.postValue(false);

                        }

                        // Log.d(TAG, "video: "+binding.videoView.getCurrentPosition()+" Progress: "+ binding.videoSeekbar.getProgress());


                        mHandler.postDelayed(mTicker, 0);

                    }
                };
                mHandler.postDelayed(mTicker, 0);

            }
        });


        play.observe((LifecycleOwner) activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {


                seekBarLogic();

                if (aBoolean) {


                    binding.btnPlay.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.a_pause));
                    binding.videoSeekbar.setProgress(binding.videoView.getCurrentPosition());
                    binding.videoView.start();


                } else {
                    binding.btnPlay.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.a_play_arrow));

                    if (binding.videoView.getCurrentPosition() >= trimEndPoint) {


                        binding.videoView.pause();
                        binding.videoView.seekTo(trimStartPoint);
                        binding.videoSeekbar.setProgress(trimStartPoint);


                    } else {
                        binding.videoView.pause();
                        binding.videoSeekbar.setProgress(binding.videoView.getCurrentPosition());

                    }

                    Log.d(TAG, "video: " + binding.videoView.getCurrentPosition() + " Progress: " + binding.videoSeekbar.getProgress());
                    Log.d(TAG, "trimStart: " + trimStartPoint + " trimEnd: " + trimEndPoint);


                }


            }
        });
        VideoModelAdapter.clickedVideoCopy.observe((LifecycleOwner) activity, new Observer<VideoModel>() {
            @Override
            public void onChanged(VideoModel videoModel) {

                currentVideo = videoModel;

                startPoint = 0;
                endPoint = Integer.parseInt(currentVideo.getDuration());
                trimStartPoint = startPoint;
                trimEndPoint = endPoint;
                int duration = endPoint - startPoint;

                binding.videoSeekbar.setMax(endPoint);


                trimStartPoint = (int) (duration / 4) + 1;
                trimEndPoint = trimStartPoint * 3;


                binding.doubleRangeSeekbar.setValueFrom(startPoint);
                binding.doubleRangeSeekbar.setValueTo(endPoint);


                long intervalInSecond = (duration) / 6;

                Help.setVideoThumbnailFromPathGlide(context, currentVideo.getPath(), binding.thumbImage1, intervalInSecond);
                Help.setVideoThumbnailFromPathGlide(context, currentVideo.getPath(), binding.thumbImage2, intervalInSecond * 2);
                Help.setVideoThumbnailFromPathGlide(context, currentVideo.getPath(), binding.thumbImage3, intervalInSecond * 3);
                Help.setVideoThumbnailFromPathGlide(context, currentVideo.getPath(), binding.thumbImage4, intervalInSecond * 4);
                Help.setVideoThumbnailFromPathGlide(context, currentVideo.getPath(), binding.thumbImage5, intervalInSecond * 5);
                Help.setVideoThumbnailFromPathGlide(context, currentVideo.getPath(), binding.thumbImage6, intervalInSecond * 6);

                initSuccess = true;


                uri = Uri.parse(currentVideo.getPath());
                binding.videoView.setVideoURI(uri);
                binding.videoView.seekTo(1);
                binding.doubleRangeSeekbar.setValues((float) trimStartPoint, (float) trimEndPoint);


            }
        });


    }

    private void trimTextUpdate() {

        binding.trimStartText.post(new Runnable() {
            @Override
            public void run() {
                binding.trimStartText.setText(Help.durationCalculate(trimStartPoint));
            }
        });
        binding.trimEndText.post(new Runnable() {
            @Override
            public void run() {
                binding.trimEndText.setText(Help.durationCalculate(trimEndPoint));
            }
        });
    }

    private void seekBarLogic() {

        if (binding.videoView.getCurrentPosition() > trimEndPoint) {
            binding.videoView.seekTo(trimEndPoint);
            binding.videoSeekbar.setProgress(trimEndPoint);
        }
        if (binding.videoView.getCurrentPosition() < trimStartPoint) {
            binding.videoView.seekTo(trimStartPoint);
            binding.videoSeekbar.setProgress(trimStartPoint);
        }
    }


    @Override
    protected void onPause() {
        lastProgress = binding.videoSeekbar.getProgress();

        if (lastProgress == 0) {
            lastProgress = 10;
        }
        seekBarLogic();
        play.postValue(false);
        goBackground = true;
        super.onPause();
    }

    @Override
    protected void onResume() {

        if (goBackground) {

            binding.videoView.seekTo((int) lastProgress);
            binding.videoSeekbar.setProgress((int) lastProgress);

            seekBarLogic();
            play.postValue(false);
        }

        super.onResume();

    }

    @Override
    public void onBackPressed() {

        binding.videoView.stopPlayback();
        if (mHandler != null) {
            mHandler.removeCallbacks(mTicker);
        }

        backHandle();

    }

    private void backHandle() {

        //Help.showBottomDialog(this,this);

        finish();
        activity.overridePendingTransition(R.anim.still_view, R.anim.slide_down);


    }

}