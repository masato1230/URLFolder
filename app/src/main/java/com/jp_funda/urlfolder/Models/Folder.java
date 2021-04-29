package com.jp_funda.urlfolder.Models;

import java.util.Date;
import java.util.List;

public class Folder {
    private int id;
    private String title;
    private int colorInt;
    private int parentId;
    private String memo;
    private Date createdDate;
    private boolean isSecret;
    private boolean isRoot;
    private String password;
    private List<Integer> urlIds;
    private List<Integer> ChildIds;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getColorInt() {
        return colorInt;
    }

    public void setColorInt(int colorInt) {
        this.colorInt = colorInt;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public void setSecret(boolean secret) {
        isSecret = secret;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Integer> getURLIds() {
        return urlIds;
    }

    public void setURLIds(List<Integer> urlIds) {
        this.urlIds = urlIds;
    }

    public List<Integer> getChildIds() {
        return ChildIds;
    }

    public void setChildIds(List<Integer> childIds) {
        ChildIds = childIds;
    }
}
