package me.myds.g2u.bookscanapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ScanImgViewHolder extends BaseRecyclerViewHolder<ScanImgItem> {

    public ImageView imgThum;
    public TextView txtTitle;

    public ScanImgViewHolder(@NonNull View itemView) {
        super(itemView);
        imgThum = itemView.findViewById(R.id.imgThum);
        txtTitle = itemView.findViewById(R.id.txtTitle);
    }

    @Override
    public void bindData(ScanImgItem data) {

    }
}
