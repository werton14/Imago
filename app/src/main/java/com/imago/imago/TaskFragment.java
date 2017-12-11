package com.imago.imago;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


public class TaskFragment extends Fragment {

    private Button makePhotoButton;

    private Uri imageUri;

    private FirebaseUtils firebaseUtils;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int OUTPUT_IMAGE_HEIGHT = 720;
    private static final int OUTPUT_IMAGE_WIDTH = 720;

    public TaskFragment() {
        // Required empty public constructor
    }

    public static TaskFragment newInstance() {
        TaskFragment fragment = new TaskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task, container, false);
        makePhotoButton = rootView.findViewById(R.id.make_photo_button);

        makePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        firebaseUtils = FirebaseUtils.getInstance();

        return rootView;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File cacheDir = getActivity().getExternalCacheDir();
        String imageFileName = "imageTmp";
        if(cacheDir != null) {
            File tmpFile = new File(cacheDir.getPath() + imageFileName + ".jpg");

            imageUri = Uri.fromFile(tmpFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        } else {
            Toast.makeText(getContext(), "If you go to Settings -> Apps " +
                    "and clear data via \"Clear data\" button, you must close all opened apps" +
                    "for using camera.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            uploadImage();
        }
    }

    private void uploadImage(){
        Bitmap img = getBitmapFromFile(imageUri);
        img = getScaledBitmap(img);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.WEBP, 35, stream);
        byte [] imageByteArray = stream.toByteArray();

        firebaseUtils.uploadImage(imageByteArray);
    }

    private Bitmap getBitmapFromFile(Uri imageUri){
        Bitmap img = null;
        try {
            img = MediaStore.Images.Media
                    .getBitmap(getActivity().getContentResolver(), imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    private Bitmap getScaledBitmap(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        return Bitmap.createScaledBitmap(dstBmp, OUTPUT_IMAGE_WIDTH,
                OUTPUT_IMAGE_HEIGHT, false);
    }

}
