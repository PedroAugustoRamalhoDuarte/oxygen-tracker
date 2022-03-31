package com.example.oxygenmeasurement;

import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.split;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OpencvCamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    Mat mRGBA;
    Mat mROI = new Mat();
    List<Mat> videoFrames = new ArrayList<Mat>();
    List<Mat> videoFramesBlue = new ArrayList<Mat>();
    List<Mat> videoFramesRed = new ArrayList<Mat>();

    boolean isRecording = false;
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
    Button btn_show_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(OpencvCamera.this, new String[]{Manifest.permission.CAMERA}, 1);
        setContentView(R.layout.activity_opencv_camera);
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_surface);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        btn_show_video = findViewById(R.id.btn_show_video);
        btn_show_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        // Button
        btn_take_picture = findViewById(R.id.btn_take_picture);
        btn_take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecording = !isRecording;
                if (isRecording) {
                    btn_take_picture.setText("Parar gravação");
                } else {
                    btn_take_picture.setText("Gravar");
                }
//                Mat mInter = new Mat(mRGBA.width(), mRGBA.height(), CvType.CV_8UC4);
//
//                Core.flip(mRGBA.t(), mRGBA, 1);
//                Imgproc.cvtColor(mRGBA, mInter, Imgproc.COLOR_RGBA2BGR, 3);
//
//                videoFrames.put(mInter);
//                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//                String filename = "temp.jpg";
//                File file = new File(path, filename);
//                boolean bool;
//                filename = file.toString();
//                bool = Imgcodecs.imwrite(filename, mInter);
//                if (bool)
//                    Log.i(TAG, "SUCCESS writing image to external storage");
//                else
//                    Log.i(TAG, "Fail writing image to external storage");
//
//                // Does not work
//                try {
//                    MediaStore.Images.Media.insertImage(getContentResolver(), String.valueOf(mInter), "Title", "Desc");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
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
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRGBA = inputFrame.rgba();
        Mat mRGBAT = mRGBA;
//        Mat mRGBAT = mRGBA.t();
//        Core.flip(mRGBA.t(), mRGBAT, 1);
//        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());

        // ROI

        int h2 = mRGBAT.width();
        int w2 = mRGBAT.height();
        int h_rect2 = h2/2+100;
        int w_rect2 = w2 * 3 / 5;
        int xStart2 = (w2 - w_rect2 + 150) / 2;
        int yStart2 = (h2 - h_rect2 - 100) / 3;
        int xEnd2 = (w2 + w_rect2 + 150) / 2;
        int yEnd2 = (h2 + h_rect2) / 3;

        // Draw rectangle
        Imgproc.rectangle(mRGBAT, new Point
                (xStart2, yStart2), new Point(
                xEnd2, yEnd2), new Scalar(255, 255, 0), 5);

        int h = mRGBAT.width();
        int w = mRGBAT.height();
        int h_rect = h / 10;
        int w_rect = w * 3 / 12;
        int xStart = (w - w_rect + 150) / 2;
        int yStart = (h - h_rect - 200) / 3;
        int xEnd = (w + w_rect + 150) / 2;
        int yEnd = (h + h_rect - 100) / 3;

        // Draw rectangle
        Imgproc.rectangle(mRGBAT, new Point
                (xStart, yStart), new Point(
                xEnd, yEnd), new Scalar(255, 0, 0), 5);

        if (isRecording) {
            // Extract ROI
            mROI = mRGBAT.submat(yStart, yEnd, xStart, xEnd);

            int size = videoFrames.size();
            if (size > 1) {
                Mat buffer = new Mat();
                List<Mat> channels = new ArrayList<Mat>();
                absdiff(mROI, videoFrames.get(size - 1), buffer);

                split(buffer, channels);

                videoFramesBlue.add(channels.get(0).clone());
                videoFramesRed.add(channels.get(2).clone());

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String filename = "temp.jpg";
                File file = new File(path, filename);
                boolean bool;
                filename = file.toString();
                bool = Imgcodecs.imwrite(filename, mROI);
                if (bool)
                    Log.i(TAG, "SUCCESS writing image to external storage");
                else
                    Log.i(TAG, "Fail writing image to external storage");

                Log.i(TAG, String.valueOf(videoFramesRed));
            }
            videoFrames.add(mROI.clone());

        }

        Log.i(TAG, Integer.toString(videoFrames.size()));

        return mRGBAT;
    }
}

