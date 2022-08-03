package com.softtechapp.videocrop.videocut.dialog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.ActivityVideoCropTrimCutBinding;
import com.softtechapp.videocrop.databinding.DialogVideoCropTrimCutBinding;
import com.softtechapp.videocrop.util.Help;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModel;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModelAdapter;

public class DialogVideoCropTrimCut {

    DialogVideoCropTrimCutBinding binding;

    Activity activity;
    Context context;


    Dialog dialog;
    Handler mHandler;
    Runnable  mTicker;


    public DialogVideoCropTrimCut(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
        onCreate();
    }

    protected void onCreate() {



        binding = DialogVideoCropTrimCutBinding.inflate(activity.getLayoutInflater());

        Dialog dialog;
        dialog = new Dialog(context,R.style.DialogFullscreen);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.addClientDialogAnimation;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        dialog.setContentView(binding.getRoot());
        //Status bar colour and fullscreen and input auto hidden
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.topBarBk, typedValue, true);
        activity.getWindow().setStatusBarColor(ContextCompat.getColor(context, typedValue.resourceId));
        activity.getWindow().setNavigationBarColor(ContextCompat.getColor(context, typedValue.resourceId));
        new WindowInsetsControllerCompat(activity.getWindow(),
                activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);


        dialog.show();


        if(binding!=null)
        {
            initialize();
        }


    }

    MutableLiveData<Boolean> play = new MutableLiveData<>();

    private void initialize() {


        play = new MutableLiveData<>(true);

        observer();

        clickHandle();

    }



    private void clickHandle() {


        binding.btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Help.comingSoonSnack(binding.getRoot(),binding.bottomLayout);


            }
        });

        binding.btnAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(),binding.bottomLayout);

            }
        });
        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(),binding.bottomLayout);

            }
        });
        binding.btnSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(),binding.bottomLayout);

            }
        });


        binding.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play.postValue(true);
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


            }
        });



    }

    private void observer() {

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
                if(mHandler!=null)
                {
                    mHandler.removeCallbacks(mTicker);
                }

                binding.videoView.start();
                binding.videoSeekbar.setProgress(0);
                play.postValue(false);

            }
        });
        binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {


                if(mediaPlayer.getVideoWidth()>mediaPlayer.getVideoHeight())
                {
                    binding.videoView.getLayoutParams().width=MATCH_PARENT;
                }
                if(mediaPlayer.getVideoWidth()==mediaPlayer.getVideoHeight())
                {
                    binding.videoView.getLayoutParams().width=MATCH_PARENT;
                    binding.videoView.getLayoutParams().height=MATCH_PARENT;
                }

                mHandler = new Handler();

                mTicker = new Runnable() {
                    @Override
                    public void run() {


                        binding.videoSeekbar.setProgress(binding.videoView.getCurrentPosition());

                        mHandler.postDelayed(mTicker, 0);

                    }
                };
                mHandler.postDelayed(mTicker, 0);

            }
        });


        play.observe((LifecycleOwner) activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean) {

                    binding.videoView.start();
                    binding.play.setVisibility(View.GONE);
                    if(mTicker!=null)
                    {
                        mTicker.run();
                    }


                } else {
                    binding.videoView.pause();
                    binding.play.setVisibility(View.VISIBLE);
                    if(mHandler!=null)
                    {
                        mHandler.removeCallbacks(mTicker);
                    }


                }
            }
        });
        VideoModelAdapter.clickedVideoCopy.observe((LifecycleOwner) activity, new Observer<VideoModel>() {
            @Override
            public void onChanged(VideoModel videoModel) {

                binding.videoSeekbar.setMax(Integer.parseInt(videoModel.getDuration()));

                Uri uri = Uri.parse(videoModel.getPath());
                binding.videoView.setVideoURI(uri);


                long duration=Long.parseLong(videoModel.getDuration());
                long intervalInSecond=(duration/1000)/6;

                Help.setVideoThumbnailFormPath(videoModel.getPath(),binding.thumbImage1,intervalInSecond);
                Help.setVideoThumbnailFormPath(videoModel.getPath(),binding.thumbImage2,intervalInSecond*2);
                Help.setVideoThumbnailFormPath(videoModel.getPath(),binding.thumbImage3,intervalInSecond*3);
                Help.setVideoThumbnailFormPath(videoModel.getPath(),binding.thumbImage4,intervalInSecond*4);
                Help.setVideoThumbnailFormPath(videoModel.getPath(),binding.thumbImage5,intervalInSecond*5);
                Help.setVideoThumbnailFormPath(videoModel.getPath(),binding.thumbImage6,intervalInSecond*6);




            }
        });





    }





}
