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

    // Init variable
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

    // récupère le chemin repertoire photo
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
    // et retourne une liste des chemins de chaque images
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

    // à l'initialisation de la view
    public TouchExample(Context context) {
        super(context);

        float taille_image = 0.0f;

        // set taux de compression des images à afficher
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        // récupération du contexte
        context_app = context;

        // récupération de la liste des chemins de chaque image
        listeImg = getCameraImages(getContext());

        // initialisation du windowManager pour récupérer la taille du device
        Point point = new Point();
        WindowManager display = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display.getDefaultDisplay().getSize(point);

        // définition du nombre d'image par ligne
        nb_image_ligne = 7;
        taille_image = point.x/nb_image_ligne;
        min_taille_image = (int)taille_image;
        max_taille_image = point.x;
        largeur_ecran = point.x;

        int pos_left = 0;
        int index_ligne = 0;

        // pour chaque chemin d'images
        for (String pic : listeImg) {

            // transforme le fichier en bitmap avec un certains taux de compression
            Bitmap b = BitmapFactory.decodeFile(pic,options);

            // transforme la bitmap en bitmap drawable pour pouvoir avoir plus de facilité
            // à la manipulation de celle-ci
            BitmapDrawable image = new BitmapDrawable(getResources(), b);

            // si la position de l'image à afficher est supérieur à la taille de l'écran alors on
            // replace l'image tout à gauche et on saute une ligne
            if(pos_left > largeur_ecran){
                pos_left=0;
                index_ligne++;
            }

            // placement de l'image à afficher
            image.setBounds(pos_left,(int)(index_ligne*taille_image), (int) (pos_left+ taille_image),(int) (taille_image+(index_ligne*taille_image)));

            // la position gauche de la prochaine image est incrémenté
            pos_left+= taille_image;

            // ajout de l'image à afficher dans la liste d'images
            images.add(image);
        }
        // initialisation des detecteur de mouvement
        mGestureDetector = new GestureDetector(context, new Gesture());
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());
    }

    // fonction d'affichage de l'image dans le canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // pour chaque image présente dans la liste, l'image est dessiné
        for(BitmapDrawable image : images){
            image.draw(canvas);
        }

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

    // utilisation de la classe simpleOnGestureListener pour executer du code lors du scroll
    public class Gesture extends GestureDetector.SimpleOnGestureListener {
        //private boolean normal = true;

        @Override
        public boolean onScroll (MotionEvent e1,
                                 MotionEvent e2,
                                 float distanceX,
                                 float distanceY)
        {
            int top = 0;
            int bottom = 0;
            int scroll = (int)(distanceY);
            Rect rectedObject;

            //Récupération du top de la première image
            rectedObject = images.get(0).getBounds();
            top = rectedObject.top - scroll;

            //On ne scroll pas si la première image se retrouve en une position > 0
            if(top <= 0) {
                for (BitmapDrawable index : images) {
                    rectedObject = index.getBounds();
                    top = rectedObject.top - scroll;
                    bottom = rectedObject.bottom - scroll;
                    //Redéfinir l'affichage
                    index.setBounds(rectedObject.left, (int) top, rectedObject.right, (int) bottom);
                }
            }
            invalidate();
            return true;
        }
    }

    public class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        // rédéfinition de la fonction onScale (lors du zoom)
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            // initialisation des variables
            Rect rectedObject;
            int pos_left = 0;
            int index_ligne = 0;
            float taille_image = 0.0f;


            // récupération des images affiché et redimensionnement en fonction du
            // taux de zoom de l'utilisateur
            rectedObject = images.get(0).getBounds();
            taille_image = (int)(rectedObject.right*mScale);

            // si la taille de l'image est plus grande que la limite défini alors la taille
            // ne s'incrémente plus et on reset le facteur d'agrandissement
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

            // pour chaque images
            for(BitmapDrawable image : images){

                // Si la prochaine image à afficher dépasse la largeur de l'écran alors on saute une ligne et on affiche l'image tout à droite
                if((pos_left+taille_image) > largeur_ecran){
                    pos_left=0;
                    index_ligne++;
                }
                // placement de l'image
                image.setBounds(pos_left,(int)(index_ligne*taille_image)+rectedObject.top, (int) (pos_left+ taille_image),(int) (taille_image+(index_ligne*taille_image))+rectedObject.top);
                // incrémentation de l'index gauche, pour la prochaine image
                pos_left+= taille_image;
            }

            invalidate();
            return true;
        }
    }
}
