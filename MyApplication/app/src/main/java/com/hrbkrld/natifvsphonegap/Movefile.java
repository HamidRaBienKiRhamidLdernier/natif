package com.hrbkrld.natifvsphonegap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Movefile extends AppCompatActivity {

    private ImageView mImageFrom, mImageResult;
    private TextView mElapsedTime;
    private int REQUEST_PICK_PHOTO = 1;
    private long startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movefile);

        mImageFrom = (ImageView) findViewById(R.id.imageViewFrom);
        mImageResult = (ImageView) findViewById(R.id.imageViewResult);
        mElapsedTime = (TextView) findViewById(R.id.textView);
    }

    /**
     * Recupere l image a partir de l URI
     * @param uri URI de l image a traiter
     */
    private void getSelectedPicture(Uri uri) {
        startTime = System.currentTimeMillis();
        if (uri != null) {
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                //mImageFrom.setImageBitmap(bitmap);

                /*for(int i = 0; i<bitmap.getHeight();i++){
                    for(int j = 0;j<bitmap.getWidth();j++){
                        //bitmap.setPixel(i,j, Color.alpha(bitmap.getPixel(i,j)));
                        bitmap.setPixel(i,j, 0);
                    }
                }*/

                changeBitmapColor(bitmap, mImageResult, Color.RED);


                //mImageResult.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        endTime = System.currentTimeMillis();
        mElapsedTime.setText("Temps ecoule "+ (endTime-startTime)+" ms.");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            getSelectedPicture(uri);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                getSelectedPicture(uri);
            }
        }
    }
    /**
     * Recupere une photo dans la memoire du device
     */
    public void pickPhoto(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
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

