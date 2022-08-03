package com.softtechapp.videocrop.videocut.util;

import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.videocut.mvvm.video.edit.EditOptionModel;

import java.util.ArrayList;
import java.util.List;

public class ListData {

    public static List<EditOptionModel> getEditOptionItem(){

        List<EditOptionModel> list=new ArrayList<>();
        list.add(new EditOptionModel(0, R.drawable.a_crop,"Crop",""));
        list.add(new EditOptionModel(1, R.drawable.a_trim,"Trim & Cut",""));
        list.add(new EditOptionModel(2, R.drawable.a_flip,"Flip/Rotate",""));
        list.add(new EditOptionModel(3, R.drawable.a_filter,"Filter",""));
        list.add(new EditOptionModel(4, R.drawable.a_music,"Add Music",""));
        list.add(new EditOptionModel(5, R.drawable.a_volume,"Volume",""));
        list.add(new EditOptionModel(6, R.drawable.a_tutorial,"Tutorial",""));

        return list;
    }
}
