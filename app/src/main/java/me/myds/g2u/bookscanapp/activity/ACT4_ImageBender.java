package me.myds.g2u.bookscanapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import me.myds.g2u.bookscanapp.R;

public class ACT4_ImageBender extends AppCompatActivity {

    private ImageView imgContent;
    private FloatingActionButton btnConfirm;

    private Point[] points;

    Bitmap bitmap;
    Bitmap output;
    Mat source;
    Mat aM;
    Bitmap bmp;
    List<Integer> tops = new ArrayList<>();
    List<Integer> bots = new ArrayList<>();

    List<procses> proc = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_bender);

        imgContent = findViewById(R.id.imgContent);
        btnConfirm = findViewById(R.id.btnConfirm);

        Uri ImageUri = getIntent().getData();
        points = Arrays.stream((int[][])getIntent().getSerializableExtra("points"))
                .map(p -> new Point(p[0], p[1])).toArray(Point[]::new);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
            output = MediaStore.Images.Media.getBitmap(getContentResolver(), ImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }


        source = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, source);
        for (Point point : points) {
            Imgproc.circle(source, point, 5, new Scalar(0, 0, 255), 3);
        }
        Utils.matToBitmap(source, output);
        imgContent.setImageBitmap(output);

        proc.add(() -> {
            source = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
            Utils.bitmapToMat(bitmap, source);
            Imgproc.GaussianBlur(source, source, new Size(3, 3), 1);
            Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2GRAY);
            Imgproc.Canny(source, source, 30, 230);
            Utils.matToBitmap(source, output);
            imgContent.setImageBitmap(output);
        });

        proc.add(() -> {
            aM = Imgproc.getPerspectiveTransform(new MatOfPoint2f(points), new MatOfPoint2f(
                    new Point(50, 100),
                    new Point(source.width() - 50, 100),
                    new Point(source.width() - 50, source.height() - 100),
                    new Point(50, source.height() - 100)
            ));
            Imgproc.warpPerspective(source, source, aM, source.size());
            Utils.matToBitmap(source, output);
            imgContent.setImageBitmap(output);
        });

        proc.add(() -> {

            for (int x = 50; source.width() - 50 > x; x += 5) {
                int y = 0;
                for (y = 0; 100 > y; y++) {
                    if (source.get(y, x)[0] != 0) {
                        break;
                    }
                }
                tops.add(y);
            }

            int ix = 50;
            for (int top : tops) {
                Imgproc.circle(source, new Point(ix, top), 5, new Scalar(255, 255, 255), 3);
                ix += 5;
            }

            for (int x = 50; source.width() - 50 > x; x += 5) {
                int y;
                for (y = source.height() - 1; source.height() -100 <= y; y--) {
                    if (source.get(y, x)[0] != 0) {
                        break;
                    }
                }
                bots.add(y);
            }

            ix = 50;
            for (int bot : bots) {
                Imgproc.circle(source, new Point(ix, bot), 5, new Scalar(255, 255, 255), 3);
                ix += 5;
            }

            Utils.matToBitmap(source, output);
            imgContent.setImageBitmap(output);
        });

        proc.add(() -> {
            Utils.bitmapToMat(bitmap, source);
            Imgproc.warpPerspective(source, source, aM, source.size());
            List<Mat> mats = new ArrayList<>();
            int ix = 50;
            for (int i = 0; tops.size()-1 > i; i++) {
               Mat mat = new Mat(source.rows(), 5, source.type(), new Scalar(3));
               Mat M = Imgproc.getPerspectiveTransform(new MatOfPoint2f(
                       new Point(ix, tops.get(i)),
                       new Point(ix +5, tops.get(i+1)),
                       new Point(ix +5, bots.get(i+1)),
                       new Point(ix, bots.get(i))
               ), new MatOfPoint2f(
                       new Point(0, 0),
                       new Point(5, 0),
                       new Point(5, mat.height()),
                       new Point(0, mat.height())
               ));
               Imgproc.warpPerspective(source,mat,M,mat.size());
               mats.add(mat);
               ix += 5;
            }

            ix = 0;
            Mat out = new Mat(mats.get(0).height(), 5 * mats.size(), source.type(), new Scalar(3));
            for (Mat mat : mats) {
                mat.copyTo(out.submat(new Rect(ix, 0, 5, mat.height())));
                ix += 5;
            }
            bmp = Bitmap.createBitmap(out.cols(), out.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(out, bmp);
            imgContent.setImageBitmap(bmp);
        });

        btnConfirm.setOnClickListener(v -> {
            if (proc.size() != 0) {
                proc.get(0).run();
                proc.remove(0);
            } else {
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

                Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
                if (ACT2_ImageCapture.inst != null) ACT2_ImageCapture.inst.finish();
                if (ACT3_PointPicker.inst != null) ACT3_PointPicker.inst.finish();
                finish();
            }
        });
    }

    private File createImageFile() throws IOException {
        // 이미지 파일 이름 생성
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String imageFileName = "문서-" + timeStamp + ".png";
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

    interface procses {
        void run ();
    }
}
