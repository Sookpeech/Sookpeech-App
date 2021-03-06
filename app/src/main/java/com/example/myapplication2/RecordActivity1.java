package com.example.myapplication2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static com.example.myapplication2.MainActivity.EXTERNAL_STORAGE_PATH;
//import static com.example.myapplication2.MainActivity.tabWidget;

public class RecordActivity1 extends AppCompatActivity {

    public static final String TAG = "RecordActivity";
    public static String RECORDED_DIR = "myRec";
    static String filename = "";

    private int RESULT_PERMISSIONS = 100;

    MediaRecorder recorder;

    private static Camera camera = null;
    SurfaceHolder holder;

    private static CameraPreview surfaceView;
    ImageView recentRecImg;

    RelativeLayout guide;
    RelativeLayout relativeLayout_btns;

    File f;

    TabHost myTabHost = MainActivity.myTabHost;

    Intent it;
    Integer practice_index = 0;

    DBHelper dbHelper;
    SQLiteDatabase db = null;

    public static Context context;

    public static RecordActivity1 getInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        it = getIntent();
        practice_index = it.getIntExtra("PRACTICE_INDEX", 0);

        dbHelper = new DBHelper(this, 4);
        db = dbHelper.getWritableDatabase();    // ??????/?????? ????????? ????????????????????? ??????

//        // ????????? ????????????  ?????????????????? ???????????? ?????? ????????????.
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        context = getApplicationContext();

        // ??????????????? 6.0 ?????? ??????????????? CAMERA ?????? ????????? ????????????.
        requestPermissionCamera();


        File directory = new File(Environment.getExternalStorageDirectory() + File.separator+RECORDED_DIR);
        if (!directory.exists()) { // ????????? ????????? ????????? ????????? ??????
            directory.mkdirs();
            Log.d(TAG, "Directory Created");
        }


//        holder = surfaceView.getHolder();
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        guide = (RelativeLayout) findViewById(R.id.guide);
        guide.setVisibility(View.VISIBLE);
//        relativeLayout_btns = (RelativeLayout) findViewById(R.id.relativelayout_btns);
//        relativeLayout_btns.setVisibility(View.GONE);

        final ImageView recStartBtn = (ImageView) findViewById(R.id.recStartBtn);
        final ImageView recStopBtn = (ImageView) findViewById(R.id.recStopBtn);
        final ImageView recFileListBtn = (ImageView) findViewById(R.id.recFileList);
        recentRecImg = (ImageView) findViewById(R.id.recentRecFile);
        GradientDrawable drawable = (GradientDrawable) this.getDrawable(R.drawable.background_rounding);
        recentRecImg.setBackground(drawable);
        recentRecImg.setClipToOutline(true);

        f = new File(EXTERNAL_STORAGE_PATH + "/" + RECORDED_DIR);
        setThumbnail();


        camera = getCameraInstance();
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();


        recStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                guide.setVisibility(View.GONE);
                //relativeLayout_btns.setVisibility(View.VISIBLE);

                try {
                    if (recorder == null) {
                        recorder = new MediaRecorder();
                    }

                    camera.unlock();
                    recorder.setCamera(camera);
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                    recorder.setMaxDuration(1800000);  // 1800sec = 30min
                    recorder.setMaxFileSize(500000000);  // 500Mb
                    recorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
                    recorder.setOrientationHint(270);

                    filename = createFilename();
                    Log.d(TAG, "current filename : " + filename);
                    recorder.setOutputFile(filename);
                    recorder.setPreviewDisplay(holder.getSurface());
                    recorder.prepare();
                    recorder.start();
                    recStartBtn.setVisibility(View.INVISIBLE);
                    recStopBtn.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    Log.e(TAG, "Exception : ", ex);
                    recorder.release();
                    recorder = null;
                }
            }
        });

        recStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recorder == null) return;
                try {
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                } catch (Exception e) {
                    Log.w(TAG, "recStopBtn", e);
                } finally {
                    recorder = null;
                }

                recStopBtn.setVisibility(View.INVISIBLE);
                recStartBtn.setVisibility(View.VISIBLE);

                /*
                * ?????? ????????? ?????? ????????? ??? ????????? ????????? ??????, ????????? ????????? ???????????? ??????
                * dialog??? ????????????...
                * */

                ContentValues values = new ContentValues(10);

                values.put(MediaStore.MediaColumns.TITLE, "RecordedVideo");
                values.put(MediaStore.Audio.Media.ALBUM, "Video Album");
                values.put(MediaStore.Audio.Media.ARTIST, "Mike");
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "RecordedVideo");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis()/1000);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                values.put(MediaStore.Audio.Media.DATA, filename);

                Uri videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                if (videoUri == null) {
                    Log.d("SampleVideoRecorder", "Video insert failed.");
                    return;
                }
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));

                setThumbnail();


                // DB??? ????????? ??????
                db.execSQL("UPDATE tableName SET filename = '" + filename + "' WHERE mid = " + practice_index);
            }
        });

//        recentRecImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(RecordActivity.this, PlayActivity.class);
//                intent.putExtra("fileUrl", getRecentFilePath());
//                startActivity(intent);
//            }
//        });
//
        recFileListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //myTabHost.setCurrentTab(2); // Record ????????? ??????
//                Intent intent = new Intent(RecordActivity.this, HomeActivity.class);
//                startActivity(intent);
                finish();
            }
        });

    }  // end of OnCreate()

    @Override
    protected void onResume() {
        super.onResume();

        guide.setVisibility(View.VISIBLE);
        //relativeLayout_btns.setVisibility(View.GONE);
    }

    public static Camera getCamera(){
        return camera;
    }
    private void setInit(){
        getInstance = this;

        // ????????? ????????? R.layout.activity_main??? ??????????????? ????????? SurfaceView?????? ?????? ???????????? ????????? setContentView ?????? ?????? ????????????.
        camera = Camera.open(findFrontSideCamera());

        setContentView(R.layout.activity_record);

        // SurfaceView??? ???????????? ??????????????? ????????????.
        surfaceView = (CameraPreview) findViewById(R.id.preview);


        // SurfaceView ?????? - holder??? Callback??? ????????????.
        holder = surfaceView.getHolder();
        holder.addCallback(surfaceView);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }


    private int findFrontSideCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cmInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cmInfo);
            if (cmInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }


    private void setThumbnail() {
        String recentFilePath = getRecentFilePath();
        if (!recentFilePath.equals("NONE")) {
            try {
                // ????????? ????????? ?????????????????? ?????? ????????? ??????
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(recentFilePath, MediaStore.Video.Thumbnails.MICRO_KIND);
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
                recentRecImg.setImageBitmap(thumbnail);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getRecentFilePath() {
        File[] files = f.listFiles();
        if (files.length == 0) {
            return "NONE";
        } else {
            String path = files[0].getPath();
            Date lastModDate = new Date(files[0].lastModified());
            for (File file : files) {
                Date date = new Date(file.lastModified());
                if (date.compareTo(lastModDate) > 0) {
                    lastModDate = date;
                    path = file.getPath();
                }
            }
            return path;
        }
    }

    private String createFilename() {
        String newFilename = "";
        if (EXTERNAL_STORAGE_PATH == null || EXTERNAL_STORAGE_PATH.equals("")) {
            newFilename = RECORDED_DIR + "/" + getNowTime() + ".mp4";
        } else {
            newFilename = EXTERNAL_STORAGE_PATH + "/" + RECORDED_DIR + "/" + getNowTime() + ".mp4";
        }

        return newFilename;
    }

    private String getNowTime() {
        String nowTime = "";
        Date date = new Date();
        nowTime += (date.getYear() + 1900) + "" + (date.getMonth() + 1) + "" + date.getDate() + "_" + date.getHours() + "" + date.getMinutes() + "" + date.getSeconds();

        return nowTime;
    }

    protected void onPause() {
        if (camera != null) {
            //camera.release();
            camera = null;
        }

        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        overridePendingTransition(R.anim.slide_down, R.anim.slide_up);
        super.onPause();
    }

    protected Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(findFrontSideCamera());
        } catch (Exception e){
        }
        return c;
    }

    public void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();

        Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        this.getResources().getConfiguration().orientation = result;
        camera.setDisplayOrientation(result);
    }

    public boolean requestPermissionCamera(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(RecordActivity1.this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);

            }else {
                setInit();
            }
        }else{  // version 6 ????????????
            setInit();
            return true;
        }

        return true;
    }

}
