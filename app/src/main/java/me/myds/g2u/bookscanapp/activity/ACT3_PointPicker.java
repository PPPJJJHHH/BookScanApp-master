package me.myds.g2u.bookscanapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;

import me.myds.g2u.bookscanapp.R;

public class ACT3_PointPicker extends AppCompatActivity {
    public static Activity inst;

    private ImageView imgContent;
    private FloatingActionButton btnConfirm;
    private RadioGroup rbgroup;

    private int checked뀨 = -1;
    Point[] points = new Point[4];
    ImageView[] pins = new ImageView[4];
    TextView[] txts = new TextView[4];

    float scaleX;
    float scaleY;
    int orgW;
    int orgH;
    int actW;
    int actH;
    int actX;
    int actY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_point);
        inst = this;

        imgContent = findViewById(R.id.imgContent);
        btnConfirm = findViewById(R.id.btnConfirm);
        rbgroup = findViewById(R.id.rbgroup);

        points[0] =  new Point(0, 0);
        points[1] =  new Point(0, 0);
        points[2] =  new Point(0, 0);
        points[3] =  new Point(0, 0);

        pins[0] = findViewById(R.id.ptLT);
        pins[1] = findViewById(R.id.ptRT);
        pins[2] = findViewById(R.id.ptRB);
        pins[3] = findViewById(R.id.ptLB);

        txts[0] = findViewById(R.id.txtLT);
        txts[1] = findViewById(R.id.txtRT);
        txts[2] = findViewById(R.id.txtRB);
        txts[3] = findViewById(R.id.txtLB);

        Uri imageURI = getIntent().getData();
        imgContent.getViewTreeObserver()
                .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgContent.getViewTreeObserver().removeOnPreDrawListener(this);

                float[] f = new float[9];
                imgContent.getImageMatrix().getValues(f);
                Drawable d = imgContent.getDrawable();

                scaleX = f[Matrix.MSCALE_X];
                scaleY = f[Matrix.MSCALE_Y];
                orgW = d.getIntrinsicWidth();
                orgH = d.getIntrinsicHeight();
                actW = Math.round(orgW * scaleX);
                actH = Math.round(orgH * scaleY);
                actX = Math.round(f[Matrix.MTRANS_X]) + 10;
                actY = Math.round(f[Matrix.MTRANS_Y]) + 10;

                points[0].set(0, 0);
                points[1].set(actW, 0);
                points[2].set(actW, actH);
                points[3].set(0, actH);

                for (int pinid = 0; pinid < 4; pinid++) {
                    Point point = points[pinid];
                    pins[pinid].animate()
                            .x(actX + point.x).y(actY + point.y)
                            .setDuration(0).start();
                    txts[pinid].setText(
                            "x: "+(int)(point.x/scaleX) +
                            "\ny: "+(int)(point.y/scaleY)
                    );
                }

                return true;
            }
        });
        imgContent.setImageURI(imageURI);

        btnConfirm.setOnClickListener(v -> {
            int[][] spoints = Arrays.stream(points).map(p -> new int[]{
                    (int) (p.x / scaleX),
                    (int) (p.y / scaleY)}
            ).toArray(int[][]::new);
            Intent intent = new Intent(this, ACT4_ImageBender.class);
            intent.setDataAndType(imageURI, "image/*");
            intent.putExtra("points", spoints);
            startActivity(intent);
        });

        rbgroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) ->{
            switch (checkedId){
                case R.id.rbLT: checked뀨 = 0; break;
                case R.id.rbRT: checked뀨 = 1; break;
                case R.id.rbRB: checked뀨 = 2; break;
                case R.id.rbLB: checked뀨 = 3; break;
                default: checked뀨 = -1; break;
            }
        });

        imgContent.setOnTouchListener(onTouchListener);
    }

    Point touchStart = null;
    View.OnTouchListener onTouchListener = (View v, MotionEvent event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (checked뀨 == -1) break;
                touchStart = new Point((int)event.getRawX(), (int)event.getRawY());
            }; return true;
            case MotionEvent.ACTION_MOVE: {
                if (touchStart == null || checked뀨 == -1) break;
                Point diff = new Point(
                        (int)event.getRawX() - touchStart.x,
                        (int)event.getRawY() - touchStart.y);
                Point fromPoint = points[checked뀨];
                Point toPoint = new Point(fromPoint.x + diff.x, fromPoint.y + diff.y);
                pins[checked뀨].animate()
                        .x(actX + toPoint.x).y(actY + toPoint.y)
                        .setDuration(0).start();
                txts[checked뀨].setText(
                        "x: "+(int)(toPoint.x/scaleX) +
                        "\ny: "+(int)(toPoint.y/scaleY)
                );
            }; return true;
            case MotionEvent.ACTION_UP: {
                if (touchStart == null || checked뀨 == -1) break;
                Point diff = new Point(
                        (int)event.getRawX() - touchStart.x,
                        (int)event.getRawY() - touchStart.y);
                Point curPoint = points[checked뀨];
                curPoint.set(curPoint.x + diff.x, curPoint.y + diff.y);
            }; return true;
        }
        return false;
    };
}
