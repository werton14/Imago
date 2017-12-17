package com.imago.imago;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Created by werton on 10.12.17.
 */

class ImageViews {

    private @ServerTimestamp Date timestamp;
    private int views;

    public ImageViews(){
        views = 0;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
