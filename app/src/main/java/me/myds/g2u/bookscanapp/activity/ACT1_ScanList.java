package me.myds.g2u.bookscanapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import me.myds.g2u.bookscanapp.BaseRecyclerAdapter;
import me.myds.g2u.bookscanapp.R;
import me.myds.g2u.bookscanapp.ScanImgItem;
import me.myds.g2u.bookscanapp.ScanImgViewHolder;

public class ACT1_ScanList extends AppCompatActivity {

    private FloatingActionButton btnAdd;

    private RecyclerView lstImg;
    private LinearLayoutManager mLayoutMgr;
    private BaseRecyclerAdapter<ScanImgItem, ScanImgViewHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_img_lst);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, ACT2_ImageCapture.class);
            startActivity(intent);
        });


        lstImg = findViewById(R.id.lstImg);
        mLayoutMgr = new LinearLayoutManager(this);
        mAdapter = new BaseRecyclerAdapter<ScanImgItem, ScanImgViewHolder>(
                R.layout.item_scan_img, ScanImgViewHolder.class
        ) {
            @Override
            public void onCreateAfterViewHolder(ScanImgViewHolder holder) {
                holder.itemView.setOnClickListener(v -> {

                });
            }

            @Override
            public void dataConvertViewHolder(ScanImgViewHolder holder, ScanImgItem data) {
                holder.bindData(data);
            }
        };

        lstImg.setLayoutManager(mLayoutMgr);
        lstImg.setAdapter(mAdapter);
    }
}
