package com.jp_funda.urlfolder.Activities.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.jp_funda.urlfolder.Utils.DownloadOgpImageTask;
import com.jp_funda.urlfolder.Utils.OGP;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    // data
    private HomeViewModel homeViewModel;
    private FolderDatabaseHandler folderDB;
    private UrlDatabaseHandler urlDB;
    private Folder rootFolder;

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
        rootFolder = null;
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

        // clickListeners
        rowFolderView.setOnClickListener(this::onRowFolderClick);
        folderStatusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (folderContainer.getVisibility() == View.VISIBLE) {
                    // rotate the status image
                    folderStatusImage.setRotation(0);
                    // set visibility
                    folderContainer.setVisibility(View.GONE);
                } else {
                    // rotate the status image
                    folderStatusImage.setRotation(90);
                    folderContainer.setVisibility(View.VISIBLE);
                }
            }
        });

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

    // click listeners
    private void onRowFolderClick(View view) {
        Folder handlingFolder = (Folder) view.getTag();

        // show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select what to do with " + handlingFolder.getTitle() +  " folder");
        builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(getActivity());
                EditText newFolderTitle = new EditText(getActivity());
                newFolderTitle.setHint("New folder title");

                editDialogBuilder.setTitle("Edit / Delete folder");
                editDialogBuilder.setView(newFolderTitle);
                editDialogBuilder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handlingFolder.setTitle(newFolderTitle.getText().toString());
                        folderDB.updateFolder(handlingFolder);
                        // redraw scrollView
                        updateScrollView();
                    }
                });
                // delete folder
                editDialogBuilder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update parent folder
                        Folder parentFolder = folderDB.getOneFolder(handlingFolder.getParentId());
                        List<Folder> updatedParentChildFolders = parentFolder.getChildFolders();
                        for (Folder childFolder: updatedParentChildFolders) {
                            if (childFolder.getId() == handlingFolder.getId()) {
                                updatedParentChildFolders.remove(childFolder);
                                break;
                            }
                        }
                        parentFolder.setChildFolders(updatedParentChildFolders);
                        folderDB.updateFolder(parentFolder);

                        // delete folder
                        folderDB.deleteFolder(handlingFolder.getId());

                        // redraw scrollView
                        updateScrollView();
                    }
                });
                editDialogBuilder.setNeutralButton(R.string.cancel, null);
                editDialogBuilder.create().show();
            }
        });
        // create folder
        builder.setPositiveButton(R.string.create_folder, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AlertDialog.Builder createFolderDialogBuilder = new AlertDialog.Builder(getActivity());
                // show create folder dialog
                // create dialog Views
                EditText titleEditText = new EditText(getActivity());
                titleEditText.setHint("New folder title");

                // set views to dialog builder
                createFolderDialogBuilder.setTitle(R.string.new_folder);
                createFolderDialogBuilder.setMessage("Please enter a title for the new folder");
                createFolderDialogBuilder.setView(titleEditText);
                createFolderDialogBuilder.setNegativeButton(R.string.cancel, null);
                createFolderDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // save new folder to database
                        Folder newFolder = new Folder();
                        newFolder.setTitle(titleEditText.getText().toString());
                        newFolder.setCreatedDate(new Date());
                        newFolder.setParentId(handlingFolder.getId());
                        int newFolderID = (int) folderDB.addFolder(newFolder);
                        // update newFolder by database
                        newFolder = folderDB.getOneFolder(newFolderID);
                        // update parentFolder(handlingFolder)
                        List<Folder> updatedChildFolders;
                        if (handlingFolder.getChildFolders() != null) {
                            updatedChildFolders = handlingFolder.getChildFolders();
                        } else {
                            updatedChildFolders = new ArrayList<>();
                        }
                        updatedChildFolders.add(newFolder);
                        handlingFolder.setChildFolders(updatedChildFolders);
                        folderDB.updateFolder(handlingFolder);
                        dialog.dismiss();
                        // redraw scrollView
                        updateScrollView();
                        // todo set password dialog
                    }
                });
                createFolderDialogBuilder.create().show();
            }
        });
        // add url
        builder.setNegativeButton(R.string.add_url, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // add url dialog
                AlertDialog.Builder addUrlDialogBuilder = new AlertDialog.Builder(getActivity());
                // initialize dialog View
                View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_url, null);
                EditText titleEditText = dialogView.findViewById(R.id.dialog_add_url_title_edit_text);
                EditText urlEditText = dialogView.findViewById(R.id.dialog_add_url_url_edit_text);
                ImageView imageView = dialogView.findViewById(R.id.dialog_add_url_image);

                // show og:image
                urlEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // todo delete

                        new DownloadOgpImageTask(imageView).execute(s.toString());

//                        new Thread() {
//                            @Override
//                            public void run() {
//                                Elements elements = null;
//                                try {
//                                    elements = OGP.getOgpImage("https://qiita.com/shikato/items/40ce3955cb7975ad4e2b");
//                                    for (Element element : elements) {
//                                        System.out.println(element.attr("content"));
//                                        String url = element.attr("content");
//                                        InputStream inputStream = new java.net.URL(url).openStream();
//                                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                                        imageView.setImageBitmap(bitmap);
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }.start();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                addUrlDialogBuilder.setView(dialogView);
                addUrlDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // add url data to database
                        Url newUrl = new Url();
                        newUrl.setTitle(titleEditText.getText().toString());
                        newUrl.setUrl(urlEditText.getText().toString());
                        newUrl.setAddedDate(new Date());
                        newUrl.setBrowsingDate(new Date());
                        newUrl.setFolderId(handlingFolder.getId());
                        int newUrlId = (int) urlDB.addUrl(newUrl);

                        // update folder data with newUrl
                        List<Url> newUrls = new ArrayList<>();
                        if (handlingFolder.getUrls() != null) {
                            newUrls = handlingFolder.getUrls();
                        }
                        newUrls.add(urlDB.getOneUrl(newUrlId));
                        handlingFolder.setUrls(newUrls);
                        folderDB.updateFolder(handlingFolder);

                        // redraw scrollView
                        updateScrollView();
                    }
                });
                addUrlDialogBuilder.setNegativeButton(R.string.cancel, null);

                addUrlDialogBuilder.create().show();
            }
        });
        builder.create().show();
    }

    public void updateScrollView() {
        scrollView.removeAllViews();
        // set folders data to views
        rootFolder = null;
        for (Folder folder: folderDB.getAllFolder()) {
            if (folder.isRoot()) {
                rootFolder = folder;
                break;
            }
        }
        scrollView.addView(inflateFolderView(rootFolder));
    }
}