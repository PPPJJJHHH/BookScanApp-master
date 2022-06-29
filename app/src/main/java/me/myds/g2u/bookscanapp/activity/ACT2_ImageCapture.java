package me.myds.g2u.bookscanapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.myds.g2u.bookscanapp.R;

public class ACT2_ImageCapture extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    private CameraBridgeViewBase cvCamView;
    private Mat capture;
    private Mat sample;
    private Mat out;

    public static Activity inst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inst = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_img_caputure2);

        findViewById(R.id.btnCapture).setOnClickListener(v -> {
            Bitmap bmp = null;
            Mat output = new Mat(capture.cols(), capture.rows(), CvType.CV_8U, new Scalar(3));
            Core.rotate(capture, output, Core.ROTATE_90_CLOCKWISE);
            Imgproc.cvtColor(capture, capture, Imgproc.COLOR_BGR2RGB);
            bmp = Bitmap.createBitmap(output.cols(), output.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(output, bmp);

            Uri imageUri = null;
            try {
                File imageFile = createImageFile();
                FileOutputStream out = new FileOutputStream(imageFile);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                imageUri = Uri.fromFile(imageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(this, ACT3_PointPicker.class);
            intent.setDataAndType(imageUri, "image/*");
            startActivity(intent);
        });

        checkPermission();

        cvCamView = findViewById(R.id.cvcam);
        cvCamView.setVisibility(SurfaceView.VISIBLE);
        cvCamView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {

            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                capture = inputFrame.rgba();

                if ( out == null) {
                    out = new Mat(capture.rows(), capture.cols(), capture.type(), new Scalar(3));
                }

                Imgproc.cvtColor(capture, out, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(out, out, new Size(3,3), 1);
                Imgproc.Canny(out, out, 30, 230);
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(out, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                capture.copyTo(out);
                Imgproc.drawContours(out, contours, -1, new Scalar(0, 255, 255), 5);
                return out;
            }
        });
        cvCamView.setCameraIndex(0);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    cvCamView.enableView();
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onPause()
    {
        super.onPause();
        if (cvCamView != null)
            cvCamView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (cvCamView != null)
            cvCamView.disableView();
    }


    private static final int PERMISSION_REQ_CAMERA = 189;
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA))) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, PERMISSION_REQ_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] < 0) {
                        Toast.makeText(ACT2_ImageCapture.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }
    }

    private File createImageFile() throws IOException {
        // 이미지 파일 이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "org-" + timeStamp + ".png";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures","g2uscan");
        //해당 디렉토리가 없으면 생성
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        //파일 생성
        imageFile = new File(storageDir,imageFileName);

        return imageFile;
    }
}
