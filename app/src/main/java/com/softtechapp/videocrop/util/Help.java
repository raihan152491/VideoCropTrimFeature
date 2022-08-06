package com.softtechapp.videocrop.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.VideoBitmapDecoder;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.DialogAskPermissionBinding;
import com.softtechapp.videocrop.databinding.DialogExitBinding;
import com.softtechapp.videocrop.databinding.DialogPermissionBinding;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoFolderModel;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModel;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Help extends AppCompatActivity {


    Activity activity;
    Context context;


    public Help(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;


    }

    public static void setVideoThumbnailFromPathGlide(Context context,String path, ImageView imageView,long intervalInSecond){




        RequestOptions options = new RequestOptions().frame((intervalInSecond*1000));
        Glide.with(context).asBitmap()
                .load(path)
                .apply(options)
                .into(imageView);
    }
    public static  void setVideoThumbnailFormPath(String path, ImageView imageView,long intervalInSecond) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);

            Bitmap thumb = retriever.getFrameAtTime(intervalInSecond*1000000,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

            imageView.setImageBitmap(thumb);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int getSeekBarThumbPosX(SeekBar seekBar) {
        int posX;
        posX = seekBar.getThumb().getBounds().centerX();
        return posX;
    }
    public static String durationCalculate(int time) {

        int hr= time/(60*60*1000);

        int min=((time%(60*60*1000))/(60*1000));
        int second=((time%(60*60*1000))%(60*1000))/(1000);




        if(hr<=0)
        {
            return  String.format(Locale.getDefault()," %02d:%02d",min,second);
        }
        else {
            return  String.format(Locale.getDefault()," %02d:%02d:%02d",hr,min,second);

        }




    }
    public static void callIntent(Activity activity,String number)
    {
        String url="tel:"+number;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.frag_fade_in,R.anim.frag_fade_out);
    }
    public static String getStringDateFromDateString(String dateFormat, String dateString, String getFormat)
    {

        SimpleDateFormat format = new SimpleDateFormat(dateFormat,Locale.getDefault());
        try {

            Date date=format.parse(dateString);
            SimpleDateFormat dateFormatGet = new SimpleDateFormat(getFormat,Locale.getDefault());

            assert date != null;
            return dateFormatGet.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date().toString();
    }
    public static String getStringDateFromDate(Date date,String dateFormat)
    {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat,Locale.getDefault());

        return format.format(date);


    }
    public static Date getDateFromStringDate(String dateFormat, String dateString)
    {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat,Locale.getDefault());
        try {

            return format.parse(dateString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }
    public static String capitalize(String capString){
        StringBuffer capBuffer = new StringBuffer();
        Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
        while (capMatcher.find()){
            capMatcher.appendReplacement(capBuffer, Objects.requireNonNull(capMatcher.group(1)).toUpperCase() + capMatcher.group(2).toLowerCase());
        }

        return capMatcher.appendTail(capBuffer).toString();
    }

    public  static  Dialog checkAppPermissionDialog;
    public static MutableLiveData<Boolean> checkAppPermission(Activity activity, Context context) {
        MutableLiveData<Boolean> permission=new MutableLiveData<>();
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DialogAskPermissionBinding dialogPermissionBinding;
        dialogPermissionBinding = DialogAskPermissionBinding.inflate(layoutInflater);

        checkAppPermissionDialog = new Dialog(context,R.style.AlertDialogTheme);
        checkAppPermissionDialog.setCanceledOnTouchOutside(false);
        checkAppPermissionDialog.setCancelable(false);
        checkAppPermissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkAppPermissionDialog.getWindow().getAttributes().windowAnimations = R.style.addClientDialogAnimation;
        checkAppPermissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        checkAppPermissionDialog.setContentView(dialogPermissionBinding.getRoot());

        String askMsg=context.getResources().getString(R.string.askMsg);
        dialogPermissionBinding.exitMsg.setText(askMsg);

        dialogPermissionBinding.btnAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (false) {
                    Dexter.withContext(context)
                            .withPermission(

                                    Manifest.permission.MANAGE_EXTERNAL_STORAGE


                            ).withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                                    permission.postValue(true);
                                    checkAppPermissionDialog.dismiss();

                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                    showPermissionMsgDialog(activity,context,permission);

                                    checkAppPermissionDialog.dismiss();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                    permissionToken.continuePermissionRequest();
                                }
                            }).check();
                }
                else {
                    Dexter.withContext(context)
                            .withPermissions(


                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE

                            ).withListener(new MultiplePermissionsListener() {
                                @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    //Toast.makeText(activity, "onPermissionsChecked", Toast.LENGTH_SHORT).show();
                                    if(report.areAllPermissionsGranted())
                                    {
                                        // Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show();

                                        permission.postValue(true);
                                        checkAppPermissionDialog.dismiss();
                                    }

                                    if(report.isAnyPermissionPermanentlyDenied())
                                    {

                                        showPermissionMsgDialog(activity,context,permission);

                                        checkAppPermissionDialog.dismiss();
                                    }


                                }


                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                    permissionToken.continuePermissionRequest();
                                }

                            }).check();
                }
            }
        });
        checkAppPermissionDialog.show();





        return permission;
    }


    public static void saveInSharePref(Context context,String key,String jsonString)
    {

            //Gson gson = new Gson();
           // String jsonString = gson.toJson(Static.clientTable);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(key, jsonString);
            editor.apply();



    }

     public  static Dialog permissionMsgDialog;
    public static void showPermissionMsgDialog(Activity activity, Context context, MutableLiveData<Boolean> permission)
    {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DialogPermissionBinding dialogPermissionBinding;
        dialogPermissionBinding = DialogPermissionBinding.inflate(layoutInflater);

        permissionMsgDialog = new Dialog(context,R.style.AlertDialogTheme2);
        permissionMsgDialog.setCanceledOnTouchOutside(false);
        permissionMsgDialog.setCancelable(false);
        permissionMsgDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        permissionMsgDialog.getWindow().getAttributes().windowAnimations = R.style.addClientDialogAnimation;
        permissionMsgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        permissionMsgDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        permissionMsgDialog.setContentView(dialogPermissionBinding.getRoot());

        String msg=context.getResources().getString(R.string.permissionMsg);
        dialogPermissionBinding.exitMsg.setText(msg);
        dialogPermissionBinding.extDialogBtnExit.setOnClickListener((View v) -> {

          checkAppPermission(activity, context);
            permissionMsgDialog.dismiss();


        });
        dialogPermissionBinding.extDialogBtnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionMsgDialog.dismiss();

                Intent getPermission = new Intent();
                if (false) {
                    getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivity(getPermission);
                }
                else {
                    getPermission.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);


                    Uri uri=Uri.fromParts("package",activity.getPackageName(),"");
                    getPermission.setData(uri);
                    activity.startActivity(getPermission);
                    activity.overridePendingTransition(R.anim.frag_fade_in, R.anim.frag_fade_out);
                }


            }
        });
        permissionMsgDialog.show();
    }
    public static void showBottomDialog(Activity activity,Context context)
    {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DialogExitBinding dialogExitBinding;
        dialogExitBinding = DialogExitBinding.inflate(layoutInflater);
        BottomSheetDialog dialog;
        dialog = new BottomSheetDialog(context,R.style.AppBottomSheetDialogTheme);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.SlideInSlideOut;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(dialogExitBinding.getRoot());

//        String msg=context.getResources().getString(R.string.exit_msg)+"\n"+context.getResources().getString(R.string.app_name);
        String msg="Do you want to exit ?";


        dialogExitBinding.exitMsg.setText(msg);
        dialogExitBinding.extDialogBtnExit.setOnClickListener((View v) -> {

            dialog.dismiss();
            AppExit(activity);
        });
        dialogExitBinding.extDialogBtnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public static List<VideoFolderModel> fetchAllVideoFolder(Context context) {
        List<VideoFolderModel>  videoFolderModels = new ArrayList<>();
        ArrayList<String> tempFolderList=new ArrayList<String>();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        Cursor cursor = context.getContentResolver().query(collection, null, null, null, null);
        try {

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {

                    String folderName=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    String folderId=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID));




                    if(!tempFolderList.contains(folderName))
                    {
                        tempFolderList.add(folderName);
                        videoFolderModels.add(0,
                                new VideoFolderModel(folderId,folderName,new ArrayList<>())
                        );

                    }


                } while (cursor.moveToNext());



            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        videoFolderModels.add(0,new VideoFolderModel("all","All Videos",new ArrayList<>()));

        return videoFolderModels;
    }
    public static List<VideoModel> fetchAllVideosByFolderName(String folderId, Context context) {

        List<VideoModel> videoModels = new ArrayList<>();



        String[] projection = {
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.Media.DISPLAY_NAME
        };

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        Cursor cursor;
        if(folderId.equals("all"))
        {

            cursor = context.getContentResolver().query(collection, null, null, null, null);
        }
        else {
            String selection=MediaStore.Video.Media.BUCKET_ID +" like? ";

            String[] args = { folderId };
            cursor = context.getContentResolver().query(collection, null, selection, args, null);

        }
        try {

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String id=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String folderName=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    String path=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String size=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    String duration=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    String dateAdded=cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));

                    VideoModel videoModel=new VideoModel(
                            id,
                            title,
                            folderName,size,duration,path,dateAdded
                    );
                    try {
                        if((Long.parseLong(duration)/1000)>=5)
                        {
                            videoModels.add(0,videoModel);
                        }
                    }
                    catch (NumberFormatException e) {
                        e.printStackTrace();
                    }





                } while (cursor.moveToNext());



            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            videoModels.sort(Comparator.comparing(VideoModel::getDateAdded).reversed());
//        }


        return videoModels;

    }

    public static void imageShowFromDevice(Intent data, Context context, ImageView view) {


        Bitmap bitmap;
        Uri path=data.getData();

        try {
            bitmap= MediaStore.Images.Media.getBitmap(context.getContentResolver(),path);

            view.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public static String getDateToStringDate(String dateFormat)
    {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat,Locale.getDefault());

            return format.format(new Date());


    }


    public static String getRealPathFromURI( Context context, Uri uri ) {
        String result = null;
        try {

            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
            if(cursor != null){
                if ( cursor.moveToFirst( ) ) {
                    int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                    result = cursor.getString( column_index );
                }
                cursor.close( );
            }
            if(result == null) {
                result = "Not found";
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void AppExit(Activity activity) {
        activity.finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        //finish();
        // System.exit(0);
        activity.finishAffinity();

    }

    public static void setTheme(Activity activity, int theme) {

        activity.getTheme().applyStyle(theme, true);
    }

    public static void comingSoonSnack(View root, View view) {

        Snackbar.make(root, "This feature is coming soon", Snackbar.LENGTH_LONG)
                .setAnchorView(view)
                .setAction("Ok", vSnack -> {



                }).show();
    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }





}
