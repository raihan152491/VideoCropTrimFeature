package com.softtechapp.videocrop.videocut.mvvm.video.edit;


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
import com.google.android.material.snackbar.Snackbar;
import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.RecycleVideoCropEditOptionModelBinding;
import com.softtechapp.videocrop.util.Help;
import com.softtechapp.videocrop.videocut.activity.VideoCropTrimCutActivity;
import com.softtechapp.videocrop.videocut.dialog.DialogVideoCropTrimCut;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditOptionModelAdapter extends ListAdapter<EditOptionModel, RecyclerView.ViewHolder> {


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
    List<EditOptionModel> list=new ArrayList<>();


    public static MutableLiveData<EditOptionModel> clickedEditOption=new MutableLiveData<>();



    public EditOptionModelAdapter(Activity activity, Context context, String action) {
        super(itemCallback);

        clickedEditOption=new MutableLiveData<>();
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
        List<EditOptionModel> listFilter;

        if (searchText.isEmpty()) {
            listFilter = list;


        } else {
            List<EditOptionModel> temp = new ArrayList<>();
            for (EditOptionModel obj : list) {

               // String orderID="#"+obj.getClientOrderId();
                if (
                        Objects.requireNonNull(obj.getId()+"").toLowerCase().contains(searchText)


                ) {
                    temp.add(obj);
                }
            }
            listFilter = temp;


        }

        submitList(listFilter);
    }

    public static DiffUtil.ItemCallback<EditOptionModel> itemCallback = new DiffUtil.ItemCallback<EditOptionModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull EditOptionModel oldItem, @NonNull EditOptionModel newItem) {

            return oldItem.getId() == (newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull EditOptionModel oldItem, @NonNull EditOptionModel newItem) {
            return oldItem.equals(newItem);
        }
    };


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View viewTypeOne = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_cardview_admin_client, parent, false);

        // return new MyViewTypeOne(viewTypeOne);
        return new MyViewTypeOne(RecycleVideoCropEditOptionModelBinding.inflate(LayoutInflater.from(parent.getContext()),
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


    public void setList(List<EditOptionModel> setList) {
        list =new ArrayList<>(setList) ;
        submitList(setList);


    }


    public class MyViewTypeOne extends RecyclerView.ViewHolder {

        RecycleVideoCropEditOptionModelBinding binding;


        public MyViewTypeOne(@NonNull RecycleVideoCropEditOptionModelBinding itemView) {
            super(itemView.getRoot());
            this.binding = itemView;



        }


        public void setData(EditOptionModel model) {





            if (binding != null ) {

               Glide.with(binding.getRoot()).load(model.getIconUrl()).placeholder(model.getIcon()).error(model.getIcon()).dontAnimate().into(binding.icon);


               binding.text.setText(model.getTitle());

               binding.card.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                      clickedEditOption.postValue(model);

                   }
               });

            }
        }

    private void setColumn(View view, int column) {

        float density = activity.getResources().getDisplayMetrics().density;

        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) binding.card.getLayoutParams();


       int columnWidth = (int) ((recyclerView.getMeasuredWidth() - ( vlp.leftMargin+vlp.rightMargin) * density) / column);


        if(column!=0)
        {
            view.getLayoutParams().width = columnWidth;
        }
    }


}






}

