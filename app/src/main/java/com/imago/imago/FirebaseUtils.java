package com.imago.imago;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by werton on 10.12.17.
 */

public class FirebaseUtils {
    private FirebaseFirestore firestore;
    private CollectionReference images;
    private CollectionReference imageViews;

    private ImagesDataEventListener imagesDataEventListener;
    private ImageDataDownloader imageDataDownloader;

    public FirebaseUtils(){
        firestore = FirebaseFirestore.getInstance();
        images = firestore.collection("images");
        imageViews = firestore.collection("imageViews");

        imageDataDownloader = new ImageDataDownloader();
    }

    public void setImagesDataEventListener(ImagesDataEventListener imagesDataEventListener){
        imagesDataEventListener = imagesDataEventListener;
        imageDataDownloader.downloadImageData();
    }


    private class ImageDataDownloader{

        private int unDownloadedData = 0;
        private int unUpdatedImageViews = 0;
        private int unExecutedTasks = 0;

        private static final int IMAGE_DATA_DOCUMENT_FOR_TASK = 10;
        private List<String> imageDataIds = new ArrayList<String>();


        public void downloadImageData(){
            unExecutedTasks++;
            if(unUpdatedImageViews == 0) {
                executeTask();
            }
        }

        public void executeTask(){
            imageViews.orderBy("time", Query.Direction.ASCENDING)
                    .limit(IMAGE_DATA_DOCUMENT_FOR_TASK)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots,
                                            FirebaseFirestoreException e) {
                            List<DocumentSnapshot> documents = documentSnapshots.getDocuments();
                            List<ImageData> imageDataList = new ArrayList<>();
                            for(int i = 0; i < documents.size(); i++){
                                unDownloadedData++;
                                unUpdatedImageViews++;

                                DocumentReference documentRef = documents.get(i).getReference();
                                String id = documentRef.getId();
                                if(!imageDataIds.contains(id)){
                                    ImageViews views = documents.get(i).toObject(ImageViews.class);
                                    updateImageViews(documentRef, views);
                                    imageDataIds.add(id);
                                    downloadImagesData(imageDataList, id);
                                }
                            }

                        }
                    });
        }

        private void downloadImagesData(final List<ImageData> imageDataList, String imageId){
            images.document(imageId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    imageDataList.add(documentSnapshot.toObject(ImageData.class));
                    unDownloadedData--;
                    if(unDownloadedData == 0){
                        imagesDataEventListener.onEvent(imageDataList);
                    }
                }
            });

        }

        @ServerTimestamp Date date;
        private void updateImageViews(DocumentReference documentReference,
                                      ImageViews views){
            views.setTime(date.getTime());
            documentReference.set(views).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    unUpdatedImageViews--;
                    if(unUpdatedImageViews == 0 && unExecutedTasks > 0){
                        executeTask();
                    }
                }
            });
            iterateImageViews(documentReference);
        }

        private void iterateImageViews(final DocumentReference documentReference){
            firestore.runTransaction(new Transaction.Function<ImageViews>() {
                @Nullable
                @Override
                public ImageViews apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(documentReference);
                    ImageViews i = snapshot.toObject(ImageViews.class);
                    i.setViews(i.getViews() + 1);
                    transaction.set(documentReference, i);
                    return i;
                }
            }).addOnSuccessListener(new OnSuccessListener<ImageViews>() {
                @Override
                public void onSuccess(ImageViews result) {
                    Log.d(TAG, "Transaction success: " + result);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Transaction failure.", e);
                }
            });
        }

    }

    /*
    * Callback interface for downloading image data for ImagesFragment
    * */
    interface ImagesDataEventListener{
        public void onEvent(List<ImageData> imagesData);
    }
}
