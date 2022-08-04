package com.softtechapp.videocrop.videocut.activity;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.google.android.material.snackbar.Snackbar;
import com.softtechapp.videocrop.R;
import com.softtechapp.videocrop.databinding.ActivityVideoCropEditBinding;
import com.softtechapp.videocrop.util.Help;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModel;
import com.softtechapp.videocrop.videocut.mvvm.video.VideoModelAdapter;
import com.softtechapp.videocrop.videocut.mvvm.video.edit.EditOptionModel;
import com.softtechapp.videocrop.videocut.mvvm.video.edit.EditOptionModelAdapter;
import com.softtechapp.videocrop.videocut.util.ListData;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class VideoCropEditActivity extends AppCompatActivity {

    ActivityVideoCropEditBinding binding;
    static final float END_SCALE = 0.7f;
    Activity activity;
    Context context;
    Animation topAnim, bottomAnim, leftAnim, rightAnim;
    Pair[] pairs = new Pair[1];
    Handler mHandler;
    Runnable mTicker;
    boolean goBackground = false;
    long lastProgress = 0;
    Thread thread;
    VideoModel currentVideo;
    Uri uri;

    boolean initComplete=false;
    public static int startPoint = 0;
    public static int endPoint = 5000;

    String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoCropEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = VideoCropEditActivity.this;
        context = VideoCropEditActivity.this;
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

        VideoCropTrimCutActivity.trim.postValue(false);
        play = new MutableLiveData<>(true);
        // loadFFMpegBinary();
        optionRecycleView();
        observer();

        clickHandle();

    }

    private void optionRecycleView() {


        RecyclerView recyclerView = binding.optionRecycleView;
        EditOptionModelAdapter recycleViewAdapter = new EditOptionModelAdapter(activity, context, "");
        recyclerView.setLayoutManager(new GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(recycleViewAdapter);


        recycleViewAdapter.setList(ListData.getEditOptionItem());


    }

    private void clickHandle() {


        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                Intent intent = new Intent(context, VideoCropTrimProgressActivity.class);
                intent.putExtra("startPoint",startPoint);
                intent.putExtra("endPoint",endPoint);
                intent.putExtra("duration",currentVideo.getDuration());
                intent.putExtra("path",currentVideo.getPath());

                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.frag_fade_in, R.anim.frag_fade_out);

            }
        });

        binding.btnVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);

            }
        });
        binding.btnAddMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);

            }
        });
        binding.btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);

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

                backHandle();
            }
        });

        binding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(binding.getRoot(), "All progress will be lost, Do you want to reset ?", Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.bottomLayout)
                        .setAction("Yes", vSnack -> {

                            VideoModelAdapter.clickedVideoCopy.postValue(VideoModelAdapter.clickedVideoCopy.getValue());


                        }).show();


            }
        });

    }


    private void observer() {

        binding.videoSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {


                binding.textDuration.post(new Runnable() {
                    @Override
                    public void run() {

                        binding.textDuration.setText(Help.durationCalculate(seekBar.getProgress()));

                        binding.textDuration.setX(Help.getSeekBarThumbPosX(seekBar));
                    }
                });

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
                binding.videoSeekbar.setProgress(0);
                binding.play.setVisibility(View.VISIBLE);
                play.postValue(false);

            }
        });
        binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {


                if(mediaPlayer!=null)
                {
                    if (mediaPlayer.getVideoWidth() > mediaPlayer.getVideoHeight()) {
                        binding.videoView.getLayoutParams().width = MATCH_PARENT;
                    }
                    if (mediaPlayer.getVideoWidth() == mediaPlayer.getVideoHeight()) {
                        binding.videoView.getLayoutParams().width = MATCH_PARENT;
                        binding.videoView.getLayoutParams().height = MATCH_PARENT;
                    }
                }

                initComplete=true;
                mHandler = new Handler();

                mTicker = new Runnable() {
                    @Override
                    public void run() {



                        if(binding.videoView.getCurrentPosition()>=binding.videoSeekbar.getMax())
                        {
                            binding.videoView.pause();
                            binding.videoView.seekTo(0);
                            play.postValue(false);
                            binding.videoSeekbar.setProgress(0);

                        }
                        else {
                            binding.videoSeekbar.setProgress(binding.videoView.getCurrentPosition());

                        }


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
                    binding.videoSeekbar.setProgress(binding.videoView.getCurrentPosition());
                    binding.videoView.start();
                    binding.play.setVisibility(View.GONE);
                    if (mTicker != null) {
                        mTicker.run();
                    }


                } else {
                    binding.videoView.pause();
                    binding.videoSeekbar.setProgress(binding.videoView.getCurrentPosition());
                    binding.play.setVisibility(View.VISIBLE);
                    if (mHandler != null) {
                        mHandler.removeCallbacks(mTicker);
                    }


                }

            }
        });
        VideoModelAdapter.clickedVideoCopy.observe((LifecycleOwner) activity, new Observer<VideoModel>() {
            @Override
            public void onChanged(VideoModel videoModel) {

                initComplete=false;
                currentVideo = videoModel;

                startPoint = 0;
                endPoint = Integer.parseInt(currentVideo.getDuration());

                binding.textDuration.setVisibility(View.INVISIBLE);
                binding.seekBarLayout.setVisibility(View.INVISIBLE);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        long duration = endPoint - startPoint;
                        long intervalInSecond = (duration / 1000) / 6;

                        Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage1, intervalInSecond);
                        Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage2, intervalInSecond * 2);
                        Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage3, intervalInSecond * 3);
                        Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage4, intervalInSecond * 4);
                        Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage5, intervalInSecond * 5);
                        Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage6, intervalInSecond * 6);

                        binding.textDuration.setVisibility(View.VISIBLE);
                        binding.seekBarLayout.setVisibility(View.VISIBLE);
                        binding.videoView.post(new Runnable() {
                            @Override
                            public void run() {
                                binding.videoSeekbar.setMax(endPoint);
                                uri = Uri.parse(currentVideo.getPath());
                                binding.videoView.setVideoURI(uri);
                                binding.play.setVisibility(View.GONE);



                            }
                        });

                    }
                }, 800);


            }
        });


        EditOptionModelAdapter.clickedEditOption.observe((LifecycleOwner) activity, new Observer<EditOptionModel>() {
            @Override
            public void onChanged(EditOptionModel model) {


                if (mHandler != null) {
                    mHandler.removeCallbacks(mTicker);
                }

                if (model.getId() == 1) {


                    if(initComplete)
                    {
                        Intent intent = new Intent(context, VideoCropTrimCutActivity.class);

                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.still_view);

                        binding.videoView.pause();
                        binding.videoView.seekTo(0);
                        binding.videoSeekbar.setProgress(0);

                    }


                } else {
                    Help.comingSoonSnack(binding.getRoot(), binding.bottomLayout);

                }


            }
        });

        VideoCropTrimCutActivity.trim.observe((LifecycleOwner) activity, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean)
                {
                    playTrimVideo();
                }
            }
        });


    }


    public void playTrimVideo() {



        binding.videoView.stopPlayback();
        long duration = endPoint - startPoint;
        long intervalInSecond = (duration / 1000) / 6;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage1, intervalInSecond);
                Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage2, intervalInSecond * 2);
                Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage3, intervalInSecond * 3);
                Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage4, intervalInSecond * 4);
                Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage5, intervalInSecond * 5);
                Help.setVideoThumbnailFormPath(currentVideo.getPath(), binding.thumbImage6, intervalInSecond * 6);



            }
        }, 500);

        binding.videoSeekbar.setMax((endPoint-startPoint));
        uri = Uri.parse(currentVideo.getPath());
        binding.videoView.setVideoURI(uri);
        binding.play.setVisibility(View.GONE);
        binding.videoSeekbar.setProgress(0);
        binding.videoView.start();





    }

    @Override
    protected void onPause() {
        lastProgress = binding.videoSeekbar.getProgress();

        if (lastProgress == 0) {
            lastProgress = 10;
        }
        goBackground = true;
        play.postValue(false);
        super.onPause();
    }

    @Override
    protected void onResume() {

        if (goBackground) {

            binding.videoView.seekTo((int) lastProgress);
            binding.videoSeekbar.setProgress((int) lastProgress);
            play.postValue(false);

        }
        super.onResume();

    }

    private void backHandle() {


        Snackbar.make(binding.getRoot(), "All progress will be lost, Do you want to go back ?", Snackbar.LENGTH_LONG)
                .setAnchorView(binding.bottomLayout)
                .setAction("Ok", vSnack -> {


                    super.onBackPressed();


                }).show();

    }

    @Override
    public void onBackPressed() {

        backHandle();

    }

    public String addQuotes(String s) {
        return "\"" + s + "\"";
    }






    private static final String LOGTAG = "VideoUtils";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    @SuppressLint("WrongConstant")
    private void genVideoUsingMuxer(String srcPath, String fileName, int startMs, int endMs, boolean useAudio, boolean useVideo) throws IOException {

        File folder;
        String folderName = "/TrimVideos";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            folder = new File (context.getExternalFilesDir(null) + folderName);
//        } else {
//            folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderName);
//        }
        folder = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )) + folderName);
        // folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderName);

        if (!folder.exists()) {
            boolean create = folder.mkdir();
        }

        if (!folder.exists()) {
            Toast.makeText(activity, "Can not create folder", Toast.LENGTH_SHORT).show();
            return;
        }

        File destination = new File(folder, fileName);
        String dstPath = destination.getAbsolutePath();

        // Set up MediaExtractor to read from the source.
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcPath);
        int trackCount = extractor.getTrackCount();
        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<>(trackCount);
        int bufferSize = -1;
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            boolean selectCurrentTrack = false;
            if (mime.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true;
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true;
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i);
                int dstIndex = muxer.addTrack(format);
                indexMap.put(i, dstIndex);
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    bufferSize = newSize > bufferSize ? newSize : bufferSize;
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        // Set up the orientation and starting time for extractor.
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(srcPath);
        String degreesString = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (degreesString != null) {
            int degrees = Integer.parseInt(degreesString);
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees);
            }
        }
        if (startMs > 0) {
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        int offset = 0;
        int trackIndex = -1;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        try {
            muxer.start();
            while (true) {
                bufferInfo.offset = offset;
                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
                if (bufferInfo.size < 0) {

                    bufferInfo.size = 0;
                    break;
                } else {
                    bufferInfo.presentationTimeUs = extractor.getSampleTime();
                    if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {

                        break;
                    } else {
                        bufferInfo.flags = extractor.getSampleFlags();
                        trackIndex = extractor.getSampleTrackIndex();
                        muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
                                bufferInfo);
                        extractor.advance();
                    }
                }
            }
            muxer.stop();

            //deleting the old file
            File file = new File(srcPath);
            file.delete();
        }
        catch (IllegalStateException e) {
            // Swallow the exception due to malformed source.

        }
        finally {
            muxer.release();
        }
    }



}