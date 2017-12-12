package com.imago.imago;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.WrapperListAdapter;


public class LeadersFragment extends Fragment {

    private ImageButton firstPlace;
    private ImageButton secondPlace;
    private ImageButton thirdPlace;
    private LinearLayout ratingFirstPlace;
    private TextView textFirstPlace;
    private int widthScreen;
    private int heightScreen;
    private int heightSizeFirstPlace;
    private int widthSizeAnotherPlace;

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

        Display display = getActivity().getWindowManager().getDefaultDisplay();

        heightScreen = display.getHeight();
        widthScreen = display.getWidth();

        ratingFirstPlace = (LinearLayout) view.findViewById(R.id.ratingFirstPlace);
        textFirstPlace = (TextView) view.findViewById(R.id.textFirstPlace);
        firstPlace = (ImageButton) view.findViewById(R.id.firstPlace);
        textFirstPlace.measure(0,0);
        ratingFirstPlace.measure(0,0);
        secondPlace = (ImageButton) view.findViewById(R.id.secondPlace);
        thirdPlace = (ImageButton) view.findViewById(R.id.thirdPlace);
        heightSizeFirstPlace = (heightScreen/2) - 72 - 47 - 5 - textFirstPlace.getMeasuredHeight() - ratingFirstPlace.getMeasuredHeight();
        widthSizeAnotherPlace = (widthScreen/2) - 15 - 16;

        LinearLayout.LayoutParams paramsFirstPlace = new LinearLayout.LayoutParams(heightSizeFirstPlace,heightSizeFirstPlace);
        paramsFirstPlace.gravity = Gravity.CENTER;
        firstPlace.setLayoutParams(paramsFirstPlace);

        LinearLayout.LayoutParams paramsAnotherPlace = new LinearLayout.LayoutParams(widthSizeAnotherPlace,widthSizeAnotherPlace);
        paramsAnotherPlace.gravity = Gravity.CENTER;
        secondPlace.setLayoutParams(paramsAnotherPlace);
        thirdPlace.setLayoutParams(paramsAnotherPlace);

        return view;
    }

}
