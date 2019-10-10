package com.example.photoblogger;

import java.util.Date;

public class Blogpost extends Blogpostid {
    private String desc,imageurl,uid,thumb;
    private Date time;
    public Blogpost(Date time) {
        this.time = time;
    }



    public Blogpost(){

    }

    public Blogpost(String desc, String imageurl, String uid, String thumb,String time) {
        this.desc = desc;
        this.imageurl = imageurl;
        this.uid = uid;
        this.thumb = thumb;
    }



    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
