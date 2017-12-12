package com.imago.imago;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyBottomNavigationView bottombar;

    private FirebaseUtils firebaseUtils;

    private static final int START_FRAGMENT_POSITION = 1;
    private static final int NUMBER_FOR_NO_REFRESHING = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        viewPager = findViewById(R.id.view_pager);
        bottombar = (MyBottomNavigationView) findViewById(R.id.bottomBar);

        firebaseUtils = FirebaseUtils.getInstance();

        /*
        *  Init ViewPager with CustomFragmentAdapter and set starting fragment,
        *  and OffscreenPageLimit for no refreshing fragments.
        */

        firebaseUtils.setSignedInListener(new FirebaseUtils.SignedInListener() {
            @Override
            public void onSignedIn() {
                CustomFragmentPageAdapter fragmentPageAdapter =
                        new CustomFragmentPageAdapter(getSupportFragmentManager());
                viewPager.setAdapter(fragmentPageAdapter);
                viewPager.setCurrentItem(START_FRAGMENT_POSITION);
                viewPager.setOffscreenPageLimit(NUMBER_FOR_NO_REFRESHING);
            }
        });

        // Set toolbar size for different android version.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.frame_toolbar);
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            72);
            linearLayout.setLayoutParams(params);
        }

        bottombar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case 0:
                        viewPager.setCurrentItem(0);
                        break;
                    case 1:
                        viewPager.setCurrentItem(1);
                        break;
                    case 2:
                        viewPager.setCurrentItem(2);
                        break;
                }
                item.setChecked(true);
                return false;
            }
        });
    }
}
