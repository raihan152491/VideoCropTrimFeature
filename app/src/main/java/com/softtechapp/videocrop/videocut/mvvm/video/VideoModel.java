package com.softtechapp.videocrop.videocut.mvvm.video;

import java.util.Objects;

public class VideoModel {


    private String id;
    private String title;
    private String folderName;
    private String size;
    private String duration;
    private String path;
    private String dateAdded;

    public VideoModel() {
    }

    public VideoModel(String id, String title, String folderName, String size, String duration, String path, String dateAdded) {
        this.id = id;
        this.title = title;
        this.folderName = folderName;
        this.size = size;
        this.duration = duration;
        this.path = path;
        this.dateAdded = dateAdded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoModel)) return false;
        VideoModel that = (VideoModel) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getFolderName(), that.getFolderName()) && Objects.equals(getSize(), that.getSize()) && Objects.equals(getDuration(), that.getDuration()) && Objects.equals(getPath(), that.getPath()) && Objects.equals(getDateAdded(), that.getDateAdded());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getFolderName(), getSize(), getDuration(), getPath(), getDateAdded());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}
