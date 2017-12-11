package com.imago.imago;

/**
 * Created by werton on 10.12.17.
 */

class ImageViews {

    private long time;
    private int views;

    public ImageViews(){
        time = 0L;
        views = 0;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
