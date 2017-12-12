package com.imago.imago;

import android.net.Uri;

/**
 * Created by werton on 10.12.17.
 */

class ImageData {

    private String imageUrl;
    private int likeCount;

    public ImageData(){
        likeCount = 0;
        imageUrl = null;
    }

    public ImageData(Uri imageUrl){
        likeCount = 0;
        this.imageUrl = imageUrl.toString();
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
