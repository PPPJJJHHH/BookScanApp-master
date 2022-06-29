package me.myds.g2u.bookscanapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import me.myds.g2u.bookscanapp.R;

public class ACT5_ImageShow extends AppCompatActivity {

    private ImageView imgContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_show);

        imgContent = findViewById(R.id.imgContent);
        Uri imageURI = getIntent().getData();
        imgContent.setImageURI(imageURI);
    }
}
