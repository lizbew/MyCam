package com.viifly.mycam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewActivity extends AppCompatActivity {
    private final static String TAG = ImageViewActivity.class.getSimpleName();
    public final static String INTENT_PARAM_IMG_PATH = "img_path";
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        mImageView = (ImageView)findViewById(R.id.imageView);

        Intent intent = getIntent();
        if (intent != null) {
            showImageFromIntent(intent);
        }
    }

    private void showImageFromIntent(Intent intent) {
        String imgPath = intent.getStringExtra(INTENT_PARAM_IMG_PATH);
        if (imgPath == null) {
            return;
        }
        Log.d(TAG, "Received img_path from intent " + imgPath);
        int w = mImageView.getWidth();
        int h = mImageView.getHeight();
        Log.d(TAG, "Image View W: " + w + ", H: " + h);
        mImageView.setImageBitmap(decodeSampledBitmapFromResource(imgPath, 500, 500));
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        Log.d(TAG, "Image Width: " + width + ", Height: " + height);

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String imgPath,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        //BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(imgPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        //return BitmapFactory.decodeResource(res, resId, options);
        return  BitmapFactory.decodeFile(imgPath, options);
    }
}
