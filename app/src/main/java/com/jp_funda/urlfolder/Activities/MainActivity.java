package com.jp_funda.urlfolder.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.jp_funda.urlfolder.Database.FolderDatabaseHandler;
import com.jp_funda.urlfolder.Database.UrlDatabaseHandler;
import com.jp_funda.urlfolder.Models.Folder;
import com.jp_funda.urlfolder.Models.Url;
import com.jp_funda.urlfolder.R;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // Ad
    private AdView adView;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ad
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // initialize ViewModel
        MainActivityViewModel mainActivityViewModel =
                new ViewModelProvider(this).get(MainActivityViewModel.class);

        // 初回起動の判定を行う
        SharedPreferences prefs = getSharedPreferences("IsFirstPref", Context.MODE_PRIVATE);
        boolean isFirst = prefs.getBoolean("is_first", true);

        if (isFirst) {
            Toast.makeText(this, "First", Toast.LENGTH_LONG).show();
            // 初回起動時の処理
            FolderDatabaseHandler folderDB = new FolderDatabaseHandler(this);
            UrlDatabaseHandler urlDB = new UrlDatabaseHandler(this);
            // create folder
            Folder folder = new Folder();
            folder.setTitle("Root");
            folder.setColorInt(1);
            folder.setParentId(-1);
            folder.setMemo("Example Folder");
            folder.setCreatedDate(new Date());
            folder.setSecret(false);
            folder.setRoot(true);
            folder.setPassword(null);
            folder.setUrls(null);
            folder.setChildFolders(null);
            folderDB.addFolder(folder);

            // create url => google.com and add to folder
            Url url = new Url();
            url.setTitle("google");
            url.setUrl("https://www.google.com/");
            url.setMemo("url example");
            url.setAddedDate(new Date());
            url.setBrowsingDate(new Date());
            url.setFolderId(1);
            url.setBrowserId(1);
            urlDB.addUrl(url);
            // update folderDB
            folder.setUrls(urlDB.getForOneFolder(1));
            folderDB.updateFolder(folder);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_first", false);
        editor.commit();
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}