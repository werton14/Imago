package com.imago.imago;

/**
 * Created by werton on 16.12.17.
 */

public class ImageDataExtended extends ImageData {

    String imageId;

    public ImageDataExtended(ImageData imageData, String imageId){
        super(imageData);

        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
