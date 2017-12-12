package com.imago.imago;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class LeadersFragment extends Fragment {

    private ImageButton firstPlace;
//    private ImageButton secondPlace;
//    private ImageButton thirdPlace;
//    private LinearLayout ratingFirstPlace;
//    private TextView textFirstPlace;
//    private int heightScreen;
//    private int widthScreen;
    private int heightSizeFirstPlace;

    public LeadersFragment() {
        // Required empty public constructor
    }

    public static LeadersFragment newInstance() {
        LeadersFragment fragment = new LeadersFragment();
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
        View view = inflater.inflate(R.layout.fragment_leaders, container, false);

//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        widthScreen = display.getWidth();
//        heightScreen = display.getHeight();

//        ratingFirstPlace = (LinearLayout) view.findViewById(R.id.ratingFirstPlace);
//        textFirstPlace = (TextView) view.findViewById(R.id.textFirstPlace);
        firstPlace = (ImageButton) view.findViewById(R.id.firstPlace);
//        secondPlace = (ImageButton) view.findViewById(R.id.secondPlace);
//        thirdPlace = (ImageButton) view.findViewById(R.id.thirdPlace);
        heightSizeFirstPlace = firstPlace.getHeight();
//        heightSizeFirstPlace = (heightScreen/2) - 72  - 16 - 4 - textFirstPlace.getHeight() - ratingFirstPlace.getHeight();
//        widthScreen = heightSizeFirstPlace;
//        firstPlace.setLayoutParams(new RelativeLayout.LayoutParams(heightSizeFirstPlace,heightSizeFirstPlace));
        Log.w("loh", String.valueOf(heightSizeFirstPlace));
        return view;
    }

}
