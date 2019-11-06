package com.example.bamenela.gestureexampleactivity;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;


public class TouchExample extends View {
    private static final int MAX_POINTERS = 5;
    private float mScale = 1f;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private static final String DEBUG_TAG = "Gestures";

    private ArrayList<BitmapDrawable> images = new ArrayList<BitmapDrawable>();
    List<String> listeImg;
    private Context context_app;
    private int nb_image_ligne = 0;
    private int largeur_ecran;
    private int min_taille_image;
    private int max_taille_image;

    public static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID =
            getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }


    // récupère la liste complète des images sur le répertoire DCIM
    public static List<String> getCameraImages(Context context) {
        final String[] projection = { MediaStore.Images.Media.DATA };
        final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
        final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
        final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        ArrayList<String> result = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public TouchExample(Context context) {
        super(context);

        float taille_image = 0.0f;

        context_app = context;
        listeImg = getCameraImages(getContext()).subList(0,10);
        Point point = new Point();
        WindowManager display = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display.getDefaultDisplay().getSize(point);
        nb_image_ligne = 7;
        taille_image = point.x/nb_image_ligne;
        min_taille_image = (int)taille_image;
        max_taille_image = point.x;
        largeur_ecran = point.x;

        int pos_left = 0;
        int index_ligne = 0;
        for (String pic : listeImg) {
            Bitmap b = BitmapFactory.decodeFile(pic);
            BitmapDrawable image = new BitmapDrawable(getResources(), b);

            if(pos_left > largeur_ecran){
                pos_left=0;
                index_ligne++;
            }
            image.setBounds(pos_left,(int)(index_ligne*taille_image), (int) (pos_left+ taille_image),(int) (taille_image+(index_ligne*taille_image)));
            pos_left+= taille_image;

            images.add(image);
        }
        mGestureDetector = new GestureDetector(context, new Gesture());
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(BitmapDrawable image : images){
            image.draw(canvas);
        }
        //canvas.drawBitmap(image.getBitmap(), 0, 0, mPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
        }
        return true;
    }

    public class Gesture extends GestureDetector.SimpleOnGestureListener {
        private boolean normal = true;

        @Override
        public boolean onScroll (MotionEvent e1,
                                 MotionEvent e2,
                                 float distanceX,
                                 float distanceY)
        {
            float top = 0.0f;
            float bottom = 0.0f;


            Log.d(DEBUG_TAG, "onScroll: " + "x: " + distanceX + "   y: " + distanceY);
//            if(images.get(0).getBounds().top >= 0)
//            {
                for (BitmapDrawable index : images) {
                    Rect rectedObject = index.getBounds();
                    top = rectedObject.top - distanceY;
                    bottom = rectedObject.bottom - distanceY;
//                    if(top >= 0) {
                        index.setBounds(rectedObject.left, (int) top, rectedObject.right, (int) bottom);
//                    }
//                    else{
//                        index.setBounds(rectedObject.left, 0, rectedObject.right, (int) rectedObject.bottom);
//                    }
                }
//            }
            invalidate();
            return true;
        }
    }

    public class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Rect rectedObject;
            int pos_left = 0;
            int index_ligne = 0;
            float taille_image = 0.0f;
            int pos_top = 0;


            rectedObject = images.get(0).getBounds();
            taille_image = (int)(rectedObject.right*mScale);
            pos_top = rectedObject.top;

            if(taille_image > max_taille_image){
                taille_image = max_taille_image;
                mScale = 1f;
            }
            else if(taille_image < min_taille_image)
            {
                taille_image = min_taille_image;
                mScale = 1f;
            }
            else{
                mScale *= detector.getScaleFactor();
            }
            for(BitmapDrawable image : images){

                if((pos_left+taille_image) > largeur_ecran){
                    pos_left=0;
                    index_ligne++;
                }
                image.setBounds(pos_left,(int)(index_ligne*taille_image)+rectedObject.top, (int) (pos_left+ taille_image),(int) (taille_image+(index_ligne*taille_image))+rectedObject.top);
                pos_left+= taille_image;
            }

            invalidate();
            return true;
        }
    }
}
