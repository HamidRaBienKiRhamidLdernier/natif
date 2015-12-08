package com.hrbkrld.natifvsphonegap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class Movefile extends AppCompatActivity{

    private ImageView mImageResult;
    private TextView mElapsedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movefile);

        mImageResult = (ImageView) findViewById(R.id.imageViewResult);
        mElapsedTime = (TextView) findViewById(R.id.textView);
    }

    /**
     * Recupere l image a partir de l URI
     */
    public void getSelectedPicture(View v) {
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        InputStream in = getResources().openRawResource(R.raw.testimage);
        if (in != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(in);

                changeBitmapColor(bitmap, mImageResult, Color.RED);

            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        endTime = System.currentTimeMillis();
        mElapsedTime.setText("Temps ecoule " + (endTime - startTime) + " ms.");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private void changeBitmapColor(Bitmap sourceBitmap, ImageView image, int color) {

        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);
        image.setImageBitmap(resultBitmap);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
    }
}

