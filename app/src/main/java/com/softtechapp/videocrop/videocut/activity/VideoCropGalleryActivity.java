package com.softtechapp.videocrop.videocut.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.ActivityVideoCropGalleryBinding;
import com.softtechapp.videocrop.util.Help;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoFolderModel;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoFolderModelAdapter;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModelAdapter;

import java.util.ArrayList;
import java.util.List;


public class VideoCropGalleryActivity extends AppCompatActivity {

    ActivityVideoCropGalleryBinding binding;

    Activity activity;
    Context context;
    Animation topAnim, bottomAnim, leftAnim, rightAnim;
    Pair[] pairs = new Pair[1];
    boolean goBakGround=false;

    public static MutableLiveData<VideoFolderModel> selectedFolder=new MutableLiveData<>();

    MutableLiveData<String> galleryFolderName;
    List<VideoFolderModel> videoFolderModels=new ArrayList<>();
    VideoModelAdapter videoModelAdapter;
    VideoFolderModelAdapter videoFolderModelAdapter;
    MutableLiveData<Boolean> showFolder=new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoCropGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = VideoCropGalleryActivity.this;
        context = VideoCropGalleryActivity.this;
        //Status bar colour and fullscreen and input auto hidden
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.topBarBk, typedValue, true);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), typedValue.resourceId));
        new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView()).setAppearanceLightStatusBars(false);


        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        leftAnim = AnimationUtils.loadAnimation(this, R.anim.left_animation);
        rightAnim = AnimationUtils.loadAnimation(this, R.anim.right_animation);

        galleryFolderName = new MutableLiveData<>("  All Video  ");

        binding.galleryFolderName.setText(galleryFolderName.getValue());


        checkPermission();

    }


    @SuppressLint("NewApi")
    private void checkPermission() {

        PackageManager pm = context.getPackageManager();
        int readPermission = pm.checkPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                context.getPackageName());
        int writePermission = pm.checkPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.getPackageName());
        if (false) {


            if (!Environment.isExternalStorageManager())  {


                Help.checkAppPermission(activity,context).observe((LifecycleOwner) activity, aBoolean -> {

                    if(aBoolean)
                    {
                        initialize();
                    }else {
                        checkPermission();
                    }

                });
            }
            else {
                if(!goBakGround){
                    initialize();
                }

            }
        }
        else {
            if ((readPermission != PackageManager.PERMISSION_GRANTED) && writePermission != PackageManager.PERMISSION_GRANTED ) {


                Help.checkAppPermission(activity,context).observe((LifecycleOwner) activity, aBoolean -> {

                    if(aBoolean)
                    {
                        initialize();
                    }else {
                        checkPermission();
                    }

                });
            }
            else {
                if(!goBakGround){
                    initialize();
                }

            }
        }


    }

    private void initialize() {


        selectedFolder.postValue(new VideoFolderModel("all","All Videos",new ArrayList<>()));


        goBakGround=true;

        observer();
        folderRecycleView();
        galleryRecycleView();

        clickHandle();

    }




    private void observer() {


        selectedFolder.observe((LifecycleOwner) activity, new Observer<VideoFolderModel>() {
            @Override
            public void onChanged(VideoFolderModel videoFolderModel) {


                galleryFolderName.postValue("  "+videoFolderModel.getFolderName()+"  ");


                videoModelAdapter.setList(Help.fetchAllVideosByFolderName(videoFolderModel.getId(),context));


                showFolder.postValue(false);


            }
        });

        galleryFolderName.observe((LifecycleOwner) activity, folderName -> {

            binding.galleryFolderName.setText(folderName);
        });
        showFolder.observe((LifecycleOwner) activity, aBoolean -> {
            if(aBoolean)
            {
                binding.folderRecycleView.setVisibility(View.VISIBLE);
                //binding.videoRecycleView.setVisibility(View.GONE);
                binding.setting.setText("Cancel");
                binding.setting.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                binding.galleryFolderName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.a_up_arrow, 0, R.drawable.a_up_arrow, 0);
                bottomAnim = AnimationUtils.loadAnimation(context, R.anim.folder_slide_down);
                binding.folderRecycleView.setAnimation(bottomAnim);
            }else {
                binding.folderRecycleView.setVisibility(View.GONE);
                binding.setting.setText("");
                binding.setting.setCompoundDrawablesWithIntrinsicBounds(R.drawable.a_setting, 0, 0, 0);
                binding.galleryFolderName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.a_down_arrow, 0, R.drawable.a_down_arrow, 0);
                topAnim = AnimationUtils.loadAnimation(context, R.anim.folder_slide_up);
                binding.folderRecycleView.setAnimation(topAnim);
                //binding.videoRecycleView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void clickHandle() {


        binding.setting.setOnClickListener(v -> {

            if(!binding.setting.getText().equals(""))
            {
                showFolder.postValue(false);

            }
            else {
                Help.comingSoonSnack(binding.getRoot(),binding.snackMsg);
            }

        });
        binding.cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(),binding.snackMsg);
            }
        });

        binding.galleryFolderName.setOnClickListener(v -> {

            Boolean val=showFolder.getValue();
            if(val!=null)
            {
                showFolder.postValue(!showFolder.getValue());

            }else {
                showFolder.postValue(true);
            }

        });

    }




    private void galleryRecycleView() {

        RecyclerView recyclerView = binding.videoRecycleView;

        videoModelAdapter = new VideoModelAdapter(activity, context, "");
        recyclerView.setLayoutManager(new GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(videoModelAdapter);

        videoModelAdapter.setList(Help.fetchAllVideosByFolderName("all",context));

    }
    private void folderRecycleView() {

        RecyclerView recyclerView = binding.folderRecycleView;

        videoFolderModelAdapter = new VideoFolderModelAdapter(activity, context, "");
        recyclerView.setLayoutManager(new GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(videoFolderModelAdapter);


        videoFolderModelAdapter.setList(Help.fetchAllVideoFolder(context));

    }




    @Override
    protected void onRestart() {
        super.onRestart();



        if(Help.permissionMsgDialog!=null)
        {
            if(Help.permissionMsgDialog.isShowing())
            {
                Help.permissionMsgDialog.dismiss();
            }
        }
        if(Help.checkAppPermissionDialog!=null)
        {
            if(Help.checkAppPermissionDialog.isShowing())
            {
                Help.checkAppPermissionDialog.dismiss();
            }

        }

        checkPermission();

    }

    @Override
    public void onBackPressed() {


        Help.showBottomDialog(activity,context);

        activity.overridePendingTransition(R.anim.frag_fade_in, R.anim.frag_fade_out);


    }

}