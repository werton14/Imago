package com.imago.imago;

import android.net.Uri;

/**
 * Created by werton on 10.12.17.
 */

class ImageData {

    private int likeCount;
    private String imageUrl;

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
