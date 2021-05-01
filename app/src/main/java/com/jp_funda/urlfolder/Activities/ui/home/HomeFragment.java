package com.jp_funda.urlfolder.Activities.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jp_funda.urlfolder.Database.FolderDatabaseHandler;
import com.jp_funda.urlfolder.Database.UrlDatabaseHandler;
import com.jp_funda.urlfolder.Models.Folder;
import com.jp_funda.urlfolder.Models.Url;
import com.jp_funda.urlfolder.R;

import java.util.List;

public class HomeFragment extends Fragment {
    // data
    private HomeViewModel homeViewModel;
    private FolderDatabaseHandler folderDB;
    private UrlDatabaseHandler urlDB;

    // views
    private View root;
    private ScrollView scrollView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // initialize Databases
        folderDB = new FolderDatabaseHandler(getActivity());
        urlDB = new UrlDatabaseHandler(getActivity());
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // initialize Views
        root = inflater.inflate(R.layout.fragment_home, container, false);
        scrollView = root.findViewById(R.id.scroll_view);

        // set folders data to views
        Folder rootFolder = null;
        for (Folder folder: folderDB.getAllFolder()) {
            if (folder.isRoot()) {
                rootFolder = folder;
                break;
            }
        }
        scrollView.addView(inflateFolderView(rootFolder));

        return root;
    }

    // get root data and draw view
    private View inflateFolderView(Folder folder) {
        View rowFolderView = LayoutInflater.from(getActivity()).inflate(R.layout.row_folder, null);

        // initialize Views in rowFolderView
        ImageView folderStatusImage = rowFolderView.findViewById(R.id.row_folder_status_icon);
        TextView folderTitle = rowFolderView.findViewById(R.id.row_folder_title);
        LinearLayout folderContainer = rowFolderView.findViewById(R.id.row_container);

        // set Data to views
        rowFolderView.setTag(folder);
        folderTitle.setText(folder.getTitle());

        // inflate child folders
        if (folder.getChildFolders() != null) {
            for (Folder childFolder: folder.getChildFolders()) {
                folderContainer.addView(inflateFolderView(childFolder));
            }
        }

        // inflate including urls
        for (Url url: folder.getUrls()) {
            folderContainer.addView(inflateUrlView(url));
        }
        return rowFolderView;
    }

    private View inflateUrlView(Url url) {
        View rowUrlView = LayoutInflater.from(getActivity()).inflate(R.layout.row_url, null);

        //initialize Views in rowUrlView
        TextView urlTitle = rowUrlView.findViewById(R.id.row_url_title);

        // set Data to views
        urlTitle.setText(url.getTitle());

        return rowUrlView;
    }
}