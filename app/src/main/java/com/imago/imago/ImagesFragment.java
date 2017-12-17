package com.imago.imago;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class ImagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<ImageDataExtended> imageDataExtendedList;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseUtils firebaseUtils;

    public ImagesFragment() {
        // Required empty public constructor
    }

    public static ImagesFragment newInstance() {
        ImagesFragment fragment = new ImagesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_images, container, false);

        recyclerView = rootView.findViewById(R.id.recycler_view);

        imageDataExtendedList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageDataExtendedList);
        recyclerView.setAdapter(imageAdapter);

        recyclerView.setItemViewCacheSize(1000);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseUtils = FirebaseUtils.getInstance();
        firebaseUtils.setImagesDataEventListener(new FirebaseUtils.ImagesDataEventListener() {
            @Override
            public void onEvent(List<ImageDataExtended> imagesData) {
                imageDataExtendedList.addAll(imagesData);
                imageAdapter.notifyDataSetChanged();
            }
        });

        recyclerView
                .addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                firebaseUtils.downloadImagesData();
            }
        });

        return rootView;
    }

}
