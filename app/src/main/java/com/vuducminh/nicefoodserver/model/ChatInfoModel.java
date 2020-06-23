package com.vuducminh.nicefoodserver.model;

public class ChatInfoModel {
    private String createaName,lastMessage;
    private long createDate, lastUpdate;

    public ChatInfoModel() {
    }

    public String getCreateaName() {
        return createaName;
    }

    public void setCreateaName(String createaName) {
        this.createaName = createaName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
