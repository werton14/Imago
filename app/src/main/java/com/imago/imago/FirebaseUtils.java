package com.imago.imago;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by werton on 10.12.17.
 */

public class FirebaseUtils {
    private FirebaseFirestore firestore;
    private CollectionReference imagesFr;
    private CollectionReference imageViewsFr;
    private CollectionReference usersFr;
    private DocumentReference taskFr;

    private FirebaseStorage storage;
    private StorageReference imagesSr;

    private FirebaseAuth auth;
    private FirebaseUser user;

    private com.imago.imago.Task task;
    private UserData userData;

    private ImagesDataEventListener imagesDataEventListener;
    private LeadersEventListener leadersEventListener;
    private TaskChangedEventListener taskChangedEventListener;

    private ImageDataDownloader imageDataDownloader;

    private static final int LEADERS_NUMBER = 3;

    private static volatile FirebaseUtils instance;

    public static FirebaseUtils getInstance() {
        FirebaseUtils localInstance = instance;
        if (localInstance == null) {
            synchronized (FirebaseUtils.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new FirebaseUtils();
                }
            }
        }
        return localInstance;
    }

    public FirebaseUtils(){
        firestore = FirebaseFirestore.getInstance();
        imagesFr = firestore.collection("images");
        imageViewsFr = firestore.collection("imageViews");
        taskFr = firestore.document("task");
        usersFr = firestore.collection("users");


        storage = FirebaseStorage.getInstance();
        imagesSr = storage.getReference().child("images");

        //TODO: fix auth later
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(userData == null){
            auth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    user = authResult.getUser();
                }
            });
        }

        imageDataDownloader = new ImageDataDownloader();
    }

    public void uploadImage(byte [] imageByteArray){
        String fileName = UUID.randomUUID().toString() + ".webp";
        imagesSr.child(fileName).putBytes(imageByteArray)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                uploadImageData(downloadUrl);
            }
        });
    }

    private void uploadImageData(Uri downloadUri){
        ImageData imageData = new ImageData(downloadUri);
        imagesFr.add(imageData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String imageId = documentReference.getId();

                ImageViews imageViews = new ImageViews();
                imageViewsFr.document(imageId).set(imageViews);
            }
        });



    }

    public void setImagesDataEventListener(ImagesDataEventListener imagesDataEventListener){
        this.imagesDataEventListener = imagesDataEventListener;
        imageDataDownloader.downloadImageData();
    }

    public void setLeadersEventListener(LeadersEventListener leadersEventListener){
        this.leadersEventListener = leadersEventListener;
    }

    public void setTaskChangedEventListener(final TaskChangedEventListener taskChangedEventListener) {
        this.taskChangedEventListener = taskChangedEventListener;

        taskFr.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                task = documentSnapshot.toObject(com.imago.imago.Task.class);
                if(userData != null) taskChangedEventListener.onEvent(getTaskStatus());
            }
        });

        usersFr.document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                userData = documentSnapshot.toObject(UserData.class);
                if(task != null) taskChangedEventListener.onEvent(getTaskStatus());
            }
        });
    }

    private boolean getTaskStatus(){
        return task.getNumber() == userData.getTaskNumber();
    }

    private void downloadLeadersImageData(){
        imagesFr.orderBy("like", Query.Direction.ASCENDING).limit(LEADERS_NUMBER).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<ImageData> imageData = task.getResult().toObjects(ImageData.class);
                        leadersEventListener.onEvent(imageData);
                    }
                });
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
            imageViewsFr.orderBy("time", Query.Direction.ASCENDING)
                    .limit(IMAGE_DATA_DOCUMENT_FOR_TASK)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    sortDocumentSnapshots(documents);

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

        private void sortDocumentSnapshots(List<DocumentSnapshot> snapshots){
            Collections.sort(snapshots, new Comparator<DocumentSnapshot>() {
                @Override
                public int compare(DocumentSnapshot o1, DocumentSnapshot o2) {
                    return o1.toObject(ImageViews.class).getViews()
                            < o2.toObject(ImageViews.class).getViews() ? -1 :
                            (o1.toObject(ImageViews.class).getViews()
                                    > o2.toObject(ImageViews.class).getViews()) ? 1 : 0;
                }
            });
        }

        private void downloadImagesData(final List<ImageData> imageDataList, String imageId){
            imagesFr.document(imageId).get().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    imageDataList.add(task.getResult().toObject(ImageData.class));
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

    interface LeadersEventListener{
        public void onEvent(List<ImageData> imageDataList);
    }

    interface TaskChangedEventListener{
        public void onEvent(boolean taskCompleted);
    }
}
