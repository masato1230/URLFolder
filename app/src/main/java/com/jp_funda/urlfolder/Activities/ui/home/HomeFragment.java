package com.jp_funda.urlfolder.Activities.ui.home;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.transition.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.jp_funda.urlfolder.Database.FolderDatabaseHandler;
import com.jp_funda.urlfolder.Database.UrlConstants;
import com.jp_funda.urlfolder.Database.UrlDatabaseHandler;
import com.jp_funda.urlfolder.Models.Folder;
import com.jp_funda.urlfolder.Models.Url;
import com.jp_funda.urlfolder.R;
import com.jp_funda.urlfolder.Utils.DownloadOgpDeskTask;
import com.jp_funda.urlfolder.Utils.DownloadOgpImageTask;
import com.jp_funda.urlfolder.Utils.DownloadOgpTitleTask;
import com.jp_funda.urlfolder.Utils.OGP;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        // if password is set, set initial visibility as gone
        if (folder.isSecret()) {
            folderStatusImage.setRotation(0);
            folderContainer.setVisibility(View.GONE);
        }

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
                    if (folder.isSecret()) {
                        // show password set dialog
                        AlertDialog.Builder checkPassDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                        checkPassDialogBuilder.setTitle(R.string.password);
                        EditText passwordEditText = new EditText(getActivity());
                        passwordEditText.setTextColor(Color.WHITE);
                        passwordEditText.setHint(R.string.password);
                        checkPassDialogBuilder.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (passwordEditText.getText().toString().equals(folder.getPassword())) {
                                    folderStatusImage.setRotation(90);
                                    folderContainer.setVisibility(View.VISIBLE);
                                    dialog.dismiss();
                                } else {
                                    Snackbar.make(v, "Password is incorrect", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                        checkPassDialogBuilder.setView(passwordEditText);
                        checkPassDialogBuilder.setNegativeButton(R.string.cancel, null);
                        checkPassDialogBuilder.create().show();
                    } else {
                        // rotate the status image
                        folderStatusImage.setRotation(90);
                        folderContainer.setVisibility(View.VISIBLE);
                    }
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
        rowUrlView.setTag(url);
        urlTitle.setText(url.getTitle());

        // ClickListeners
        rowUrlView.setOnClickListener(this::onRowUrlClick);

        return rowUrlView;
    }

    // click listeners
    private void onRowUrlClick(View view) {
        Url handlingUrl = (Url) view.getTag();

        // show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_url_management, null);
        builder.setTitle(handlingUrl.getTitle());

        // initialize dialogView
        TextView urlText = dialogView.findViewById(R.id.dialog_url_management_url);
        TextView memo = dialogView.findViewById(R.id.dialog_url_management_memo);
        TextView addedDateText = dialogView.findViewById(R.id.dialog_url_management_added_date);
        TextView browsedDateText = dialogView.findViewById(R.id.dialog_url_management_browsed_date);
        ImageView imageView = dialogView.findViewById(R.id.dialog_url_management_image);
        TextView ogpTitle = dialogView.findViewById(R.id.dialog_url_management_title);
        TextView ogpDesk = dialogView.findViewById(R.id.dialog_url_management_desk);
        // set data to Views
        urlText.setText(handlingUrl.getUrl());
        memo.setText(handlingUrl.getMemo());
        addedDateText.setText("Add: " + UrlConstants.dateFormat.format(handlingUrl.getAddedDate()));
        browsedDateText.setText("Browse: " + UrlConstants.dateFormat.format(handlingUrl.getBrowsingDate()));
        new DownloadOgpImageTask(imageView).execute(handlingUrl.getUrl());
        new DownloadOgpTitleTask(ogpTitle).execute(handlingUrl.getUrl());
        new DownloadOgpDeskTask(ogpDesk).execute(handlingUrl.getUrl());

        builder.setView(dialogView);

        // buttons
        builder.setNegativeButton(R.string.copy_url, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // copy to clipboard
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("", handlingUrl.getUrl()));
                // notify by snackBar
                Snackbar.make(view, "Link copied", Snackbar.LENGTH_LONG)
                        .setAction("", null).show();
            }
        });
        builder.setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri =  Uri.parse(handlingUrl.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // Create and start the chooser
                Intent chooser = Intent.createChooser(intent, "Open with");
                getActivity().startActivity(chooser);

                // update browse date
                handlingUrl.setBrowsingDate(new Date());
                urlDB.update(handlingUrl);
            }
        });
        builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // show dialog => title, memo, url, delete
                AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                editDialogBuilder.setTitle("Edit / Delete the url");
                View editDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_url, null);
                // initialize dialogView
                EditText titleEditText = editDialogView.findViewById(R.id.dialog_url_edit_title_edit_text);
                EditText memoEditText = editDialogView.findViewById(R.id.dialog_url_edit_memo_edit_text);
                EditText urlEditText = editDialogView.findViewById(R.id.dialog_url_edit_url_edit_text);
                ImageView imageView = editDialogView.findViewById(R.id.dialog_url_edit_image);
                // set data to views
                titleEditText.setText(handlingUrl.getTitle());
                memoEditText.setText(handlingUrl.getMemo());
                urlEditText.setText(handlingUrl.getUrl());
                new DownloadOgpImageTask(imageView).execute(handlingUrl.getUrl());
                urlEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        new DownloadOgpImageTask(imageView).execute(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                editDialogBuilder.setView(editDialogView);
                editDialogBuilder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // delete handling url data
                        // update folder data
                        Folder folder = folderDB.getOneFolder(handlingUrl.getFolderId());
                        for (Url url: folder.getUrls()) {
                            if (url.getId() == handlingUrl.getId()) {
                                List<Url> updatedUrls = folder.getUrls();
                                updatedUrls.remove(url);
                                folder.setUrls(updatedUrls);
                                break;
                            }
                        }
                        folderDB.updateFolder(folder);
                        // delete url data
                        urlDB.deleteUrl(handlingUrl.getId());
                        // redraw scrollView
                        updateScrollView();
                    }
                });
                editDialogBuilder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handlingUrl.setTitle(titleEditText.getText().toString());
                        handlingUrl.setMemo(memoEditText.getText().toString());
                        handlingUrl.setUrl(urlEditText.getText().toString());
                        // update database
                        urlDB.update(handlingUrl);
                        updateScrollView();
                    }
                });
                editDialogBuilder.create().show();
            }
        });

        builder.create().show();

    }

    private void onRowFolderClick(View view) {
        Folder handlingFolder = (Folder) view.getTag();

        // show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("Select what to do with " + handlingFolder.getTitle() +  " folder");
        builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                EditText newFolderTitle = new EditText(getActivity());
                newFolderTitle.setTextColor(getActivity().getResources().getColor(R.color.white));
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
                AlertDialog.Builder createFolderDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                // show create folder dialog
                // create dialog Views
                View createFolderDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_folder, null);

                // initialize Views
                EditText titleEditText = createFolderDialogView.findViewById(R.id.dialog_create_folder_title);
                CheckBox setPassCheckbox = createFolderDialogView.findViewById(R.id.dialog_create_folder_checkbox);
                LinearLayout setPassContainer = createFolderDialogView.findViewById(R.id.dialog_create_folder_pass_set_container);
                EditText passwordEditText = createFolderDialogView.findViewById(R.id.dialog_create_folder_password);
                setPassContainer.setVisibility(View.GONE);
                setPassCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            setPassContainer.setVisibility(View.VISIBLE);
                        } else {
                            setPassContainer.setVisibility(View.GONE);
                        }

                    }
                });

                // set views to dialog builder
                createFolderDialogBuilder.setTitle(R.string.new_folder);
                createFolderDialogBuilder.setMessage("Please enter a title for the new folder");
                createFolderDialogBuilder.setView(createFolderDialogView);
                createFolderDialogBuilder.setNegativeButton(R.string.cancel, null);
                createFolderDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // save new folder to database
                        Folder newFolder = new Folder();
                        newFolder.setTitle(titleEditText.getText().toString());
                        newFolder.setCreatedDate(new Date());
                        newFolder.setParentId(handlingFolder.getId());
                        if (setPassCheckbox.isChecked()) {
                            newFolder.setSecret(true);
                            newFolder.setPassword(passwordEditText.getText().toString());
                        }
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
                AlertDialog.Builder addUrlDialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                addUrlDialogBuilder.setTitle(R.string.add_url);
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
                        new DownloadOgpImageTask(imageView).execute(s.toString());
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