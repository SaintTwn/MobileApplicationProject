package com.batugeray.instablog;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.batugeray.instablog.Fragments.BookmarksFragment;
import com.batugeray.instablog.Fragments.HomeFragment;
import com.batugeray.instablog.Fragments.TrendingFragment;
import com.batugeray.instablog.Helper.BottomNavigationViewHelper;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    TextView header;
    FirebaseAuth mAuth;
    BottomNavigationView bottom_nav;
    private CircleImageView profileimage;
    private Fragment homeFragment,trendingFragment,bookmarksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        header = findViewById(R.id.header);
        profileimage = findViewById(R.id.profileimage);
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                profile.putExtra("UserId", mAuth.getCurrentUser().getUid());
                startActivity(profile);
            }
        });
        loadprofileImage(mAuth.getCurrentUser().getPhotoUrl());

        bottom_nav = findViewById(R.id.bottom_nav);
        BottomNavigationViewHelper.disableShiftMode(bottom_nav);
        Menu m = bottom_nav.getMenu();
        for (int i=0;i<m.size();i++) {
            MenuItem mi = m.getItem(i);
        }
        bottom_nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        homeFragment = new HomeFragment();
        trendingFragment = new TrendingFragment();
        bookmarksFragment = new BookmarksFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment)
                .add(R.id.fragment_container,trendingFragment)
                .add(R.id.fragment_container,bookmarksFragment)
                .hide(trendingFragment)
                .hide(bookmarksFragment)
                .commit();

    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Çıkmak için bir kez daha tıkla!", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(homeFragment, trendingFragment, bookmarksFragment);
                    return true;
                case R.id.trending:
                    replaceFragment(trendingFragment, homeFragment, bookmarksFragment);
                    return true;
                case R.id.bookmarks:
                    replaceFragment(bookmarksFragment, homeFragment,trendingFragment);
                    return true;
            }
            return false;
        }
    };

  


    private void loadprofileImage(Uri uri){
        Glide.with(MainActivity.this)
                .applyDefaultRequestOptions(new RequestOptions()
                .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                .error(R.drawable.com_facebook_profile_picture_blank_square)
                .diskCacheStrategy(DiskCacheStrategy.ALL))
                .load(uri)
                .into(profileimage);
    }

    private void replaceFragment(Fragment one, Fragment two, Fragment three){
        if (!one.isVisible()){
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .show(one)
                    .hide(two)
                    .hide(three)
                    .commit();
        }

    }
}