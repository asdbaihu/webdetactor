package com.erinicv1;

/**
 * Created by Administrator on 2017/4/8 0008.
 */
public class Document {

    private String id;

    private String content;

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return String.format(" id : %s, content : %s", id, content);
    }
}
