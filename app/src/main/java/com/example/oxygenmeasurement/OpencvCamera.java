package com.example.oxygenmeasurement;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;

public class OpencvCamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    Mat mRGBA;
    Mat mRGBAT;
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "onManagerConnect: OpenCV Loaded");
                    cameraBridgeViewBase.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
            super.onManagerConnected(status);
        }
    };


    Button btn_take_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(OpencvCamera.this, new String[]{Manifest.permission.CAMERA}, 1);
        setContentView(R.layout.activity_opencv_camera);
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_surface);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);


        // Button
        btn_take_picture = findViewById(R.id.btn_take_picture);
        btn_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat mInter = new Mat(mRGBA.width(), mRGBA.height(), CvType.CV_8UC4);

                Core.flip(mRGBA.t(), mRGBA, 1);
                Imgproc.cvtColor(mRGBA, mInter, Imgproc.COLOR_RGBA2BGR, 3);

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String filename = "temp.jpg";
                File file = new File(path, filename);
                boolean bool;
                filename = file.toString();
                bool = Imgcodecs.imwrite(filename, mInter);
                if (bool)
                    Log.i(TAG, "SUCCESS writing image to external storage");
                else
                    Log.i(TAG, "Fail writing image to external storage");

                // Does not work
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), String.valueOf(mInter), "Title" , "Desc");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request denied, this will return an empty array
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraBridgeViewBase.setCameraPermissionGranted();
                } else {
                    // Permission denied
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            // If Load Success
            Log.d(TAG, "onResume: OpenCV initialized");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "onResume: OpenCV not initialized");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mRGBAT = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        mRGBAT = inputFrame.gray();
        return mRGBA;
    }
}