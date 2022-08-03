package com.softtechapp.videocrop.videocut.mvvm.video;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.RecycleVideoCropVideoModelBinding;
import com.softtechapp.videocrop.util.Help;
import com.softtechapp.videocrop.videocut.activity.VideoCropEditActivity;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class VideoModelAdapter extends ListAdapter<VideoModel, RecyclerView.ViewHolder> {


    BottomSheetDialog dialog;
    Runnable mTicker;
    RecyclerView recyclerView;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    static int deviceWidth = MATCH_PARENT;
    static int deviceHeight = 500;
    boolean flag = false;
    public Activity activity;
    Context context;
    String action;
    List<VideoModel> list=new ArrayList<>();


    public static MutableLiveData<VideoModel> clickedVideo=new MutableLiveData<>();
    public static MutableLiveData<VideoModel> clickedVideoCopy=new MutableLiveData<>();



    public VideoModelAdapter(Activity activity, Context context, String action) {
        super(itemCallback);

        clickedVideo=new MutableLiveData<>();
        clickedVideoCopy=new MutableLiveData<>();
        this.activity = activity;
        this.context = context;
        this.action = action;
        initialize();

    }

    int row;
    int column;
    public void setRowColumn(RecyclerView recyclerView,int row,int column,String orientation)
    {
        this.row=row;
        this.column=column;
        this.recyclerView=recyclerView;
        GridLayoutManager gridLayoutManager;
        if(orientation.equals("horizontal"))
        {
            gridLayoutManager=new GridLayoutManager(context, row, GridLayoutManager.HORIZONTAL, false);
            gridLayoutManager.setAutoMeasureEnabled(false);

            recyclerView.setLayoutManager(gridLayoutManager);

        }
        else {
            gridLayoutManager=new GridLayoutManager(context, column, GridLayoutManager.VERTICAL, false);
            gridLayoutManager.setAutoMeasureEnabled(false);
            recyclerView.setLayoutManager(gridLayoutManager);


        }




    }


    private void initialize() {
        //productViewModel =new ViewModelProvider((ViewModelStoreOwner) activity).get(ProductViewModel.class);
        list=new ArrayList<>();

    }
    public void filter(String searchText) {


        searchText = searchText.toLowerCase().trim();
        List<VideoModel> listFilter;

        if (searchText.isEmpty()) {
            listFilter = list;


        } else {
            List<VideoModel> temp = new ArrayList<>();
            for (VideoModel obj : list) {

               // String orderID="#"+obj.getClientOrderId();
                if (
                        Objects.requireNonNull(String.valueOf(obj.getId())).contains(searchText)


                ) {
                    temp.add(obj);
                }
            }
            listFilter = temp;


        }

        submitList(listFilter);
    }

    public static DiffUtil.ItemCallback<VideoModel> itemCallback = new DiffUtil.ItemCallback<VideoModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull VideoModel oldItem, @NonNull VideoModel newItem) {

            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull VideoModel oldItem, @NonNull VideoModel newItem) {
            return oldItem.equals(newItem);
        }
    };


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View viewTypeOne = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_cardview_admin_client, parent, false);

        // return new MyViewTypeOne(viewTypeOne);
        return new MyViewTypeOne(RecycleVideoCropVideoModelBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @Override
    public int getItemCount() {

            return super.getItemCount();


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyViewTypeOne myViewTypeOne = ((MyViewTypeOne) holder);



            myViewTypeOne.setData(getItem(position));






    }


    public void setList(List<VideoModel> setList) {
        list =new ArrayList<>(setList) ;
        submitList(setList);


    }


    public  class MyViewTypeOne extends RecyclerView.ViewHolder {

        RecycleVideoCropVideoModelBinding binding;


        public MyViewTypeOne(@NonNull RecycleVideoCropVideoModelBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;



        }


        public void setData(VideoModel model) {





            if (binding != null ) {

                Glide.with(binding.getRoot()).load(
                        new File(model.getPath())
                ).placeholder(R.drawable.logo).error(R.drawable.logo).into(binding.thumbNail);



                try {
                    binding.duration.setText(Help.durationCalculate( Integer.parseInt(model.getDuration())));

                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                binding.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        clickedVideo.postValue(model);
                        clickedVideoCopy.postValue(model);
                        Intent intent = new Intent(context, VideoCropEditActivity.class);
                        Pair<View, String>[] pair = new Pair[1];
                        pair[0] = new Pair<View, String>(binding.card, "videoView");
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, pair);
                        activity.startActivity(intent, options.toBundle());

                    }
                });

            }
        }




    }






}
