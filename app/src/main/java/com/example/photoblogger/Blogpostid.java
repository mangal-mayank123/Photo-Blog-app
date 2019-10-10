package com.example.photoblogger;

public class Blogpostid {
    public String postid;
    public <T extends Blogpostid> T withid(final String id){
             this.postid=id;
             return (T)this;
    }
}
