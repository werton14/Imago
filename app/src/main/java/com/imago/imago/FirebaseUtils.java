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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by werton on 10.12.17.
 */

public class FirebaseUtils {
    private FirebaseFirestore firestore = null;
    private CollectionReference imagesFr = null;
    private CollectionReference imageViewsFr = null;
    private CollectionReference usersFr = null;
    private DocumentReference taskFr = null;

    private FirebaseStorage storage = null;
    private StorageReference imagesSr = null;

    private FirebaseAuth auth = null;
    private FirebaseUser user = null;

    private com.imago.imago.Task task = null;
    private UserData userData = null;

    private SignedInListener signedInListener = null;
    private ImagesDataEventListener imagesDataEventListener = null;
    private LeadersEventListener leadersEventListener = null;
    private TaskChangedEventListener taskChangedEventListener = null;
    private UserCompleteTaskListener userCompleteTaskListener = null;
    private LikeEventListener likeEventListener = null;

    private ImageDataDownloader imageDataDownloader = null;

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
        taskFr = firestore.collection("task").document("task");
        usersFr = firestore.collection("users");


        storage = FirebaseStorage.getInstance();
        imagesSr = storage.getReference().child("images");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        imageDataDownloader = new ImageDataDownloader();
    }


    // Check and if it need update auth status

    public void setSignedInListener(SignedInListener signedInListener){
        this.signedInListener = signedInListener;

        if(user == null){
            auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        user = auth.getCurrentUser();
                        uploadUserData();
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                    }
                }
            });
        } else {
            signedInListener.onSignedIn();
        }
    }

    private void uploadUserData(){
        UserData data = new UserData();
        usersFr.document(user.getUid()).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                signedInListener.onSignedIn();
            }
        });
    }


    // Upload image

    public void uploadImage(byte [] imageByteArray){
        String fileName = user.getUid() + ".webp";
        imagesSr.child(fileName).putBytes(imageByteArray)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                uploadImageData(downloadUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "onFailure: failure", e);
            }
        });
    }

    private void uploadImageData(Uri downloadUri){
        ImageData imageData = new ImageData(downloadUri);
        imagesFr.document(user.getUid()).set(imageData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                ImageViews imageViews = new ImageViews();
                imageViewsFr.document(user.getUid()).set(imageViews);
            }
        });

        userData.setTaskNumber(task.getNumber());
        usersFr.document(user.getUid()).set(userData);
    }


    // Task

    public void setTaskEventListener(TaskChangedEventListener taskChangedEventListener,
                                            UserCompleteTaskListener userCompleteTaskListener) {
        this.taskChangedEventListener = taskChangedEventListener;
        this.userCompleteTaskListener = userCompleteTaskListener;

        setTaskChangedEventListener();
        setUserTaskCompleteListener();

    }

    private void setUserTaskCompleteListener(){
        usersFr.document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()) {
                    userData = documentSnapshot.toObject(UserData.class);
                    Log.w(TAG, "onEvent: userData" + String.valueOf(userData.getTaskNumber()));
                    if (task != null) {
                        if (getTaskStatus() &&
                                userCompleteTaskListener != null)
                            userCompleteTaskListener.onComplete();
                    } else {
                        Log.w(TAG, "onEvent: task is null!");
                    }
                }
            }
        });
    }

    private void setTaskChangedEventListener(){
        taskFr.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()) {
                    task = documentSnapshot.toObject(com.imago.imago.Task.class);
                    if (taskChangedEventListener != null) taskChangedEventListener.onEvent(task);
                }
            }
        });
    }

    private boolean getTaskStatus(){
        return task.getNumber() == userData.getTaskNumber();
    }


    // Listener for update like on TaskFragment

    public void setLikeEventListener(final LikeEventListener likeEventListener){
        this.likeEventListener = likeEventListener;

        imagesFr.document(user.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()) {
                    ImageData imageData = documentSnapshot.toObject(ImageData.class);
                    if(getTaskStatus()) likeEventListener.onEvent(imageData.getLikeCount());
                    else likeEventListener.onEvent(-1);
                } else {
                    likeEventListener.onEvent(-1);
                }
            }
        });
    }


    // Downloading image data for LeadersFragment

    public void setLeadersEventListener(LeadersEventListener leadersEventListener){
        this.leadersEventListener = leadersEventListener;
        downloadLeadersImageData();
    }

    private void downloadLeadersImageData(){
        imagesFr.orderBy("likeCount", Query.Direction.DESCENDING).limit(LEADERS_NUMBER).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<ImageData> imageData = task.getResult().toObjects(ImageData.class);
                        leadersEventListener.onEvent(imageData);
                    }
                });
    }


    // Downloading image data for ImagesFragment

    public void setImagesDataEventListener(ImagesDataEventListener imagesDataEventListener){
        this.imagesDataEventListener = imagesDataEventListener;
        imageDataDownloader.downloadImageData();
    }

    public void downloadImagesData(){
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
            imageViewsFr
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .limit(IMAGE_DATA_DOCUMENT_FOR_TASK)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                    // Sort image data from lower to upper by views
                    sortDocumentSnapshots(documents);

                    List<ImageDataExtended> imageDataList = new ArrayList<>();

                    for(int i = 0; i < documents.size() / 2; i++){
                        unUpdatedImageViews++;

                        DocumentReference documentRef = documents.get(i).getReference();
                        updateImageViews(documentRef);
                        String id = documentRef.getId();
                        if(!imageDataIds.contains(id)){
                            unDownloadedData++;
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

        private void downloadImagesData(final List<ImageDataExtended> imageDataList, final String imageId){
            imagesFr.document(imageId).get().addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    ImageData imageData = task.getResult().toObject(ImageData.class);
                    imageDataList.add(new ImageDataExtended(imageData, imageId));
                    unDownloadedData--;
                    if(unDownloadedData == 0){
                        imagesDataEventListener.onEvent(imageDataList);
                    }
                }
            });

        }


        private void updateImageViews(DocumentReference documentReference){
            Map<String,Object> updates = new HashMap<>();
            updates.put("timestamp", FieldValue.serverTimestamp());

            documentReference.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void makeLike(String imageId, boolean likeStatus){

        firestore.runTransaction(new Transaction.Function<ImageData>() {
            @Nullable
            @Override
            public ImageData apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {


                return null;
            }
        });


    }


    // Callback interfaces

    interface SignedInListener{
        public void onSignedIn();
    }

    interface ImagesDataEventListener{
        public void onEvent(List<ImageDataExtended> imagesData);
    }

    interface LeadersEventListener{
        public void onEvent(List<ImageData> imageDataList);
    }

    interface TaskChangedEventListener{
        public void onEvent(com.imago.imago.Task task);
    }

    interface UserCompleteTaskListener{
        public void onComplete();
    }

    interface LikeEventListener{
        public void onEvent(int likeCount);
    }
}
