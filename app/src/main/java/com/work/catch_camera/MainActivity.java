package com.work.catch_camera;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.MediaActionSound;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.Permission;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2, SensorEventListener {

    private static final String TAG = "opencv2";
    private myJavaCameraView mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;
    private DBHelper dbHelper = new DBHelper(MainActivity.this,"cameraLog",null,1);
    private SensorManager mSensorManager;
    private Sensor linearSensor;
    BottomNavigationView bottomNavigationView;
    TextView textView, textView2;
    ImageView imgLoading;
    ImageButton power, capture, log, menu;

    //public myJavaCameraView mJavaCameraView;
    public MediaActionSound sound = new MediaActionSound();
    public int chk = 1;
    public int sangtae;
    public File sdRoot;
    public String position;
    public double longitude;
    public double latitude;
    public long now;
    public Date date;
    public Bitmap green, yellow, pyojeok;
    public Mat mat_green, mat_yellow, mat_pyojeok;
    public double accX, accY, accZ;
    public boolean startChk = false;
    public boolean moveChk = false;
    String getTime;

    Location location;

    public native int ConvertRGBtoGray(long matAddrInput, long matAddrResult, int check, long matGreen, long matYellow, long matPyojeok);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        linearSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        green = BitmapFactory.decodeResource(getResources(), R.drawable.guide_on);
        yellow = BitmapFactory.decodeResource(getResources(), R.drawable.guide_off);
        pyojeok = BitmapFactory.decodeResource(getResources(), R.drawable.pyojeok);

        mat_green = new Mat();
        mat_yellow = new Mat();
        mat_pyojeok = new Mat();

        Utils.bitmapToMat(green,mat_green);
        Utils.bitmapToMat(yellow,mat_yellow);
        Utils.bitmapToMat(pyojeok,mat_pyojeok);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        power = (ImageButton)findViewById(R.id.onoff);
        capture = (ImageButton)findViewById(R.id.capture);
        log = (ImageButton)findViewById(R.id.log);
        menu = (ImageButton)findViewById(R.id.menu);

        imgLoading = (ImageView) findViewById(R.id.img_loading);
        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);

        imgLoading.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        final LocationManager locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        mOpenCvCameraView = (myJavaCameraView) findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(1080,1920);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        mOpenCvCameraView.setOnTouchListener(new myJavaCameraView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(sangtae == 2)
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:01031092355"));
                    startActivity(intent);
                }
                return false;
            }
        });


        power.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (chk > 0) {
                    startChk = true;
                    textView.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.VISIBLE);
                    power.setImageResource(R.drawable.poweron);
                    initView();
                }
                else if (chk < 0)
                {
                    startChk = false;
                    initView();
                    textView.setVisibility(View.INVISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                    power.setImageResource(R.drawable.poweroff);
                }
                chk *= -1;
            }
        });

        capture.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                    ActivityCompat.requestPermissions( MainActivity.this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION},0);
                }
                else {
                    if (sangtae == 2) {
                        //Toast.makeText(getApplication(),"qq",Toast.LENGTH_SHORT).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("몰래카메라 발견!");
                        builder.setMessage("경찰서로 신고합니다.");
                        builder.setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:01031092355"));
                                        startActivity(intent);
                                    }
                                });
                        builder.setNegativeButton("no",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Toast.makeText(getApplication(),"qq",Toast.LENGTH_SHORT).show();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    sound.play(MediaActionSound.SHUTTER_CLICK); // 찰칵 소리
                    final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    String provider = location.getProvider();
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,gpsLocationListener);

                    position = "(" + longitude + "," + latitude+")";

                    // 이미지 저장
                    sdRoot = Environment.getExternalStorageDirectory();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    String currentDateandTime = sdf.format(new Date());
                    String dir = "/DCIM/Data Collection/";
                    String fileName = "Image_"+currentDateandTime + ".jpg";
                    File mkDir = new File(sdRoot,dir);
                    mkDir.mkdirs();
                    File pictureFile = new File(sdRoot, dir + fileName);
                    Imgcodecs.imwrite("/sdcard/"+dir+fileName,matResult);
                    mOpenCvCameraView.takePicture(fileName);

                    // Mat to bitmap
                    Bitmap img = Bitmap.createBitmap(matResult.cols(),matResult.rows(), Bitmap.Config.RGB_565);
                    Utils.matToBitmap(matResult,img);
                    Bitmap bitmap = (Bitmap)img;
                    bitmap = Bitmap.createScaledBitmap(bitmap,1920,1080,false);

                    // bitmap to byte[]
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();

                    now = System.currentTimeMillis();
                    date = new Date(now);
                    getTime = sdf.format(date);

                    //이미지 DB저장
                    dbHelper.insert(byteArray,"a",position,getTime);
                }
            }
        });

        log.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });

        //mOpenCvCameraView.stopfocus();
        //mJavaCameraView = (myJavaCameraView)findViewById(R.id.activity_surface_view);
        //mJavaCameraView.setCvCameraViewListener(this);

        /*
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setItemIconTintList(null);
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.power: {
                        // power on off 판단
                        if (chk > 0) {
                            startChk = true;
                            textView.setVisibility(View.VISIBLE);
                            textView2.setVisibility(View.VISIBLE);
                            item.setIcon(R.drawable.power_on);
                            item.setTitle("on");
                            initView();
                        }
                        else if (chk < 0)
                        {
                            startChk = false;
                            initView();
                            textView.setVisibility(View.INVISIBLE);
                            textView2.setVisibility(View.INVISIBLE);
                            item.setIcon(R.drawable.power);
                            item.setTitle("off");
                        }
                        chk *= -1;

                        return true;
                    }
                    case R.id.capture: {
                        // 위치 퍼미션 확인
                        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                            ActivityCompat.requestPermissions( MainActivity.this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION},0);
                        }
                        else {
                            if (sangtae == 2) {
                                //Toast.makeText(getApplication(),"qq",Toast.LENGTH_SHORT).show();

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("몰래카메라 발견!");
                                builder.setMessage("경찰서로 신고합니다.");
                                builder.setPositiveButton("yes",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:01031092355"));
                                                startActivity(intent);
                                            }
                                        });
                                builder.setNegativeButton("no",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Toast.makeText(getApplication(),"qq",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                            sound.play(MediaActionSound.SHUTTER_CLICK); // 찰칵 소리
                            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            String provider = location.getProvider();
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,gpsLocationListener);

                            position = "(" + longitude + "," + latitude+")";

                            // 이미지 저장
                            sdRoot = Environment.getExternalStorageDirectory();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                            String currentDateandTime = sdf.format(new Date());
                            String dir = "/DCIM/Data Collection/";
                            String fileName = "Image_"+currentDateandTime + ".jpg";
                            File mkDir = new File(sdRoot,dir);
                            mkDir.mkdirs();
                            File pictureFile = new File(sdRoot, dir + fileName);
                            Imgcodecs.imwrite("/sdcard/"+dir+fileName,matResult);
                            mOpenCvCameraView.takePicture(fileName);

                            // Mat to bitmap
                            Bitmap img = Bitmap.createBitmap(matResult.cols(),matResult.rows(), Bitmap.Config.RGB_565);
                            Utils.matToBitmap(matResult,img);
                            Bitmap bitmap = (Bitmap)img;
                            bitmap = Bitmap.createScaledBitmap(bitmap,1920,1080,false);

                            // bitmap to byte[]
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            byte[] byteArray = byteArrayOutputStream .toByteArray();

                            now = System.currentTimeMillis();
                            date = new Date(now);
                            getTime = sdf.format(date);

                            //이미지 DB저장
                            dbHelper.insert(byteArray,"a",position,getTime);
                        }

                        return true;
                    }
                    case R.id.log: {
                        Intent intent = new Intent(MainActivity.this, LogActivity.class);
                        startActivity(intent);

                        return true;
                    }
                    case R.id.menu: {

                        return true;
                    }
                }
                return false;
                }
            });
         */
        }

        boolean loadingChk = false;

        private void initView(){
            if(startChk && sangtae != 2 && loadingChk) {
                loadingChk = false;
                imgLoading.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.loading);
                imgLoading.setAnimation(anim);
            }
            else if((startChk && sangtae == 2) || !startChk){
                loadingChk = true;
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.loading);
                imgLoading.setAnimation(null);
                imgLoading.setVisibility(View.INVISIBLE);
            }
        }

        final LocationListener gpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }
            @Override
            public void onProviderDisabled(String provider) { }
        };

    @Override
    public void onStart() {
        super.onStart();
        if (linearSensor != null)
            mSensorManager.registerListener(this, linearSensor,SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public void onStop() {
        super.onStop();
        if(mSensorManager!=null) mSensorManager.unregisterListener(this);
    }
        @Override
        public void onPause()
        {
            super.onPause();
            if (mOpenCvCameraView != null)
                mOpenCvCameraView.disableView();
        }

        @Override
        public void onResume()
        {
            super.onResume();

            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "onResume :: Internal OpenCV library not found.");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
            } else {
                Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }

        public void onDestroy() {
            super.onDestroy();

            if (mOpenCvCameraView != null)
                mOpenCvCameraView.disableView();
        }

        @Override
        public void onCameraViewStarted(int width, int height) {
            matResult = new Mat(width,height,CvType.CV_8UC4);
        }

        @Override
        public void onCameraViewStopped() {  }
        Mat rgbaT;

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            matInput = inputFrame.gray();
            if ( matResult == null ) matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());

            rgbaT = matInput.t();
            Core.flip(matInput.t(),rgbaT,1);
            Imgproc.resize(rgbaT,rgbaT,matInput.size());
            matInput.release();

            Imgproc.resize(mat_green,mat_green,rgbaT.size());
            Imgproc.resize(mat_yellow,mat_yellow,rgbaT.size());

            sangtae = ConvertRGBtoGray(rgbaT.getNativeObjAddr(), matResult.getNativeObjAddr(), chk, mat_green.getNativeObjAddr(), mat_yellow.getNativeObjAddr(), mat_pyojeok.getNativeObjAddr());

            rgbaT.release();

            return matResult;
        }

        //여기서부턴 퍼미션 관련 메소드
        static final int PERMISSIONS_REQUEST_CODE = 1000;
        String[] PERMISSIONS  = {"android.permission.CAMERA"};

        private boolean hasPermissions(String[] permissions) {
            int result;

            //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
            for (String perms : permissions){
                result = ContextCompat.checkSelfPermission(this, perms);
                if (result == PackageManager.PERMISSION_DENIED){
                    //허가 안된 퍼미션 발견
                    return false;
                }
            }

            //모든 퍼미션이 허가되었음
            return true;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            switch(requestCode){

                case PERMISSIONS_REQUEST_CODE:
                    if (grantResults.length > 0) {
                        boolean cameraPermissionAccepted = grantResults[0]
                                == PackageManager.PERMISSION_GRANTED;

                        if (!cameraPermissionAccepted)
                            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                    }
                    break;
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        private void showDialogForPermission(String msg) {

            AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
            builder.setTitle("알림");
            builder.setMessage(msg);
            builder.setCancelable(false);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                public void onClick(DialogInterface dialog, int id){
                    requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                }
            });
            builder.create().show();
        }

        double sumX = 0, sumY = 0, sumZ = 0;
        boolean chkX, chkY, chkZ;

        private static final float NS2S = 1.0f/1000000000.0f;
        private float timestamp;
        float dT;

    @Override
    public void onSensorChanged(SensorEvent event) {

        accX = event.values[0];
        accY = event.values[1];
        accZ = event.values[2];
/*
        // Shake detection
        float x = mGravity[0];
        float y = mGravity[1];
        float z = mGravity[2];
        */
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float)Math.sqrt(accX*accX + accY*accY + accZ*accZ);
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel*0.9f+ delta;
        // Make this higher or lower according to how much
        // motion you want to detect
        Log.e("LOG",String.format("%.4f",mAccel));
        if(mAccel > 3){
         //   Log.e("LOG",String.format("%.4f",mAccel));
        }
/*
        if(timestamp != 0 ) {
            dT = (event.timestamp - timestamp) * NS2S;
        }
        timestamp = event.timestamp;

        sumX += accX * dT;
        sumY += accY * dT;
        sumZ += accZ * dT;

       Log.e("LOG",String.format("%.4f %.4f %.4f %.4f %.4f %.4f",accX,accY,accZ,sumX,sumY,sumZ));

        /*
        sumX += accX;
        sumY += accY;
        sumZ += accZ;

        if(sumX > 3 || sumX < -3) chkX = true;
        if(sumY > 3 || sumY < -3) chkY = true;
        if(sumZ > 3 || sumZ < -3) chkZ = true;

        if(sumX < 3 && sumX > -3){
            sumX = 0;
            chkX = false;
        }
        if(sumY < 3 && sumY > -3) {
            sumY = 0;
            chkY = false;
        }
        if(sumZ < 3 && sumZ > -3)
        {
            sumZ = 0;
            chkZ = false;
        }

        //if(sumX < 0.5 || sumX > -0.5) sumX = 0;
        //if(sumY < 0.5 || sumX > -0.5) sumY = 0;
        //if(sumZ < 0.5 || sumX > -0.5) sumZ = 0;
*/

        if (sangtae == 2 || chkX || chkY || chkZ) {
            initView();
            textView.setText("몰래카메라 발견!");
            textView2.setText("터치하여 경찰서로 신고합니다.");
            textView.setTextColor(Color.rgb(255, 244, 33));
            textView2.setTextColor(Color.rgb(255, 244, 33));
        } else {
            initView();
            textView.setText("탐지중입니다.");
            textView2.setText("");
            textView.setTextColor(
                    Color.rgb(96, 255, 33));
            textView2.setTextColor(Color.rgb(96, 255, 33));
        }
/*
 //       Log.e("LOG", "ACCELOMETER           [X]:" + String.format("%.4f", sumX)
   //             + "           [Y]:" + String.format("%.4f", sumY)
     //           + "           [Z]:" + String.format("%.4f", sumZ));
   //     if (accX > 1 || accX < -1 || accY > 1 || accY < -1 || accZ > 1 || accZ < -1) chk = 2;
    //    else chk = -1;
*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
