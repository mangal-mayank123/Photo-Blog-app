package com.example.photoblogger;

import java.util.Date;

public class Notification {
    String uid,pid,desc;
    Date time;

    public Notification() {

    }
    public Notification(String uid, String pid, String desc,Date time) {
        this.uid = uid;
        this.pid = pid;
        this.desc = desc;this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
