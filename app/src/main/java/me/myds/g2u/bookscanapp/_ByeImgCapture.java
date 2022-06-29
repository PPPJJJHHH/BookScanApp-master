package me.myds.g2u.bookscanapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.myds.g2u.bookscanapp.activity.ACT3_PointPicker;

public class _ByeImgCapture extends AppCompatActivity {

    private static final int PERMISSION_REQ_CAMERA = 189;
    private static final int REQ_TAKE_PHOTO = 498;
    private static final int REQ_TAKE_ALBUM = 914;

    private Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_capture);

        checkPermission();
        findViewById(R.id.btnGall).setOnClickListener(v -> getAlbum());
        findViewById(R.id.btnCam).setOnClickListener(v -> captureCamera());
    }

    private File createImageFile() throws IOException {
        // 이미지 파일 이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "org-" + timeStamp + ".jpg";
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

    private void captureCamera(){
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){
            //외장 메모리가 사용 가능시(사용가능)
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null){
                File photoFile = null;
                try { photoFile = createImageFile(); } catch (IOException e) { e.printStackTrace(); }
                //이미지 파일 만들기 성공했다면
                if(photoFile != null){
                    //다른 앱에 파일 공유하기 위한 프로바이더 생성
                    imageURI = FileProvider.getUriForFile(this,getPackageName(),photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                    startActivityForResult(intent, REQ_TAKE_PHOTO);
                }
            }
            else {
                Toast.makeText(this,"저장 공간에 접근이 불가능합니다.",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void getAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,REQ_TAKE_ALBUM);
    }

    private void passImage(){
        Intent intent = new Intent(this, ACT3_PointPicker.class);
        intent.setDataAndType(imageURI, "image/*");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQ_TAKE_PHOTO:{
                if(resultCode == RESULT_OK){
                    passImage();
                }
            }break;
            case REQ_TAKE_ALBUM:{
                if(resultCode == RESULT_OK){
                    if(data.getData() != null){
                        imageURI = data.getData();
                        passImage();
                    }
                }
            }break;
        }
    }

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
                        Toast.makeText(_ByeImgCapture.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }
    }
}
