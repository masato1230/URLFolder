package com.jp_funda.urlfolder.Models;

import java.util.Date;

public class URL {
    private int id;
    private String url;
    private String memo;
    private Date AddedDate;
    private Date BrowsingDate;
    private int folderId;
    private int browserId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Date getAddedDate() {
        return AddedDate;
    }

    public void setAddedDate(Date addedDate) {
        AddedDate = addedDate;
    }

    public Date getBrowsingDate() {
        return BrowsingDate;
    }

    public void setBrowsingDate(Date browsingDate) {
        BrowsingDate = browsingDate;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getBrowserId() {
        return browserId;
    }

    public void setBrowserId(int browserId) {
        this.browserId = browserId;
    }
}
