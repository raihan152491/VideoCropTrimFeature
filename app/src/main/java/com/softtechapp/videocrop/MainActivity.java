package com.softtechapp.videocrop;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.softtechapp.videocrop.databinding.ActivityMainBinding;
import com.softtechapp.videocrop.videocut.activity.VideoCropGalleryActivity;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    Activity activity;
    Context context;
    Pair[] pairs = new Pair[1];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = MainActivity.this;
        context = MainActivity.this;
        //Status bar colour and fullscreen and input auto hidden
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.splashBk, typedValue, true);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView()).setAppearanceLightStatusBars(false);


        initialize();


    }

    private void initialize() {
        int SPLASH_SCREEN = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                goNextActivity();



            }
        }, SPLASH_SCREEN);

        clickHandle();
    }

    private void clickHandle() {
        binding.imageView.setOnClickListener(v -> {
            goNextActivity();
        });
    }

    private void goNextActivity() {
        Intent intent=new Intent(getApplicationContext(), VideoCropGalleryActivity.class);

        pairs[0] = new Pair<View, String>(binding.imageView, "toolBar");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, pairs);

        startActivity(intent, options.toBundle());

    }


    @Override
    public void onBackPressed() {


        //Help.showBottomDialog(this,this);
        finish();
        activity.overridePendingTransition(R.anim.frag_fade_in, R.anim.frag_fade_out);


    }

}