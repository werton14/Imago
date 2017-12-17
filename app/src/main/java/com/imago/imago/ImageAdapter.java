package com.imago.imago;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by werton on 13.12.17.
 */

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ImageDataExtended> imageDataList;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new ImageHolder(inflater.inflate(R.layout.image_layout, parent, false));
    }

    public ImageAdapter(List<ImageDataExtended>imageDataList){

        this.imageDataList = imageDataList;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ImageHolder){
            ImageDataExtended imageData = imageDataList.get(position);

            ImageHolder imageHolder = (ImageHolder) holder;
            imageHolder.bindView(imageData);
        }
    }

    @Override
    public int getItemCount() {
        return imageDataList.size();
    }

    public class ImageHolder extends RecyclerView.ViewHolder{

        View view;

        private ImageButton alertImageButton;
        private ImageButton likeImageButton;
        private ImageView imageView;
        private TextView likeTextView;

        private ImageDataExtended imageData;

        public ImageHolder(View view){
            super(view);

            this.view = view;

            imageView = view.findViewById(R.id.image_view);
            likeImageButton = view.findViewById(R.id.like_image_button);
            alertImageButton = view.findViewById(R.id.alert_image_button);
            likeTextView = view.findViewById(R.id.like_text_view);
        }

        public void bindView(ImageDataExtended imageData){
            this.imageData = imageData;

            GlideApp.with(view)
                    .load(imageData.getImageUrl())
                    .into(imageView);

            likeTextView.setText(String.valueOf(imageData.getLikeCount()));

        }
    }
}
