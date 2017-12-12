package com.imago.imago;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


public class TaskFragment extends Fragment {

    private Button makePhotoButton;
    private ImageView myImageView;
    private TextView taskTextView;
    private ProgressBar progressBar;
    private TextView likeCountTextView;

    private Uri imageUri;

    private FirebaseUtils firebaseUtils;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int OUTPUT_IMAGE_HEIGHT = 720;
    private static final int OUTPUT_IMAGE_WIDTH = 720;
    private static final int MILLION = 1000000;
    private static final int ONE_HUNDRED_THOUSANDS = 100000;

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
        myImageView = rootView.findViewById(R.id.my_image_view);
        taskTextView = rootView.findViewById(R.id.task_text_view);
        progressBar = rootView.findViewById(R.id.progress_bar);
        likeCountTextView = rootView.findViewById(R.id.like_count_text_view);

        makePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        firebaseUtils = FirebaseUtils.getInstance();

        firebaseUtils.setTaskEventListener(new FirebaseUtils.TaskChangedEventListener() {
            @Override
            public void onEvent(Task task) {
                myImageView.setVisibility(View.GONE);
                makePhotoButton.setVisibility(View.VISIBLE);
                taskTextView.setText(task.getTask());
                likeCountTextView.setText("-");
            }
        }, new FirebaseUtils.UserCompleteTaskListener() {
            @Override
            public void onComplete() {
                myImageView.setVisibility(View.VISIBLE);
                makePhotoButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

                if(updateImageUri()) {

                    GlideApp.with(getActivity().getApplicationContext())
                            .load(imageUri)
                            .skipMemoryCache(true)
                            .into(myImageView);

                }
            }
        });

        firebaseUtils.setLikeEventListener(new FirebaseUtils.LikeEventListener() {
            @Override
            public void onEvent(int likeCount) {

                if(likeCount != -1) {
                    String like = String.valueOf(likeCount);
                    int tmp = likeCount / MILLION;
                    if (tmp > 0) {
                        like = String.valueOf(tmp) + "M";
                    } else {
                        tmp = likeCount / ONE_HUNDRED_THOUSANDS;
                        if (tmp > 0) {
                            like = String.valueOf(tmp) + "K";
                        }
                    }

                    likeCountTextView.setText(like);
                } else {
                    likeCountTextView.setText("-");
                }
            }
        });

        return rootView;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        updateImageUri();

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

    }

    private boolean updateImageUri() {
        File cacheDir = getActivity().getExternalCacheDir();
        String imageFileName = "imageTmp";
        if(cacheDir != null) {
            File tmpFile = new File(cacheDir.getPath() + imageFileName + ".jpg");
            imageUri = Uri.fromFile(tmpFile);

            return true;
        }

        Toast.makeText(getContext(), "If you go to Settings -> Apps " +
                "and clear data via \"Clear data\" button, you must close all opened apps" +
                "for using camera.", Toast.LENGTH_LONG).show();

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            uploadImage();
            makePhotoButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
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
