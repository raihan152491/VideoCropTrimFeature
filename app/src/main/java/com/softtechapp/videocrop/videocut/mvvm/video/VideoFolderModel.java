package com.softtechapp.videocrop.videocut.mvvm.video;

import java.util.List;
import java.util.Objects;

public class VideoFolderModel {

    private String id;

    private String folderName;
    private List<VideoModel> videoList;

    public VideoFolderModel() {
    }

    public VideoFolderModel(String id, String folderName, List<VideoModel> videoList) {
        this.id = id;
        this.folderName = folderName;
        this.videoList = videoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoFolderModel)) return false;
        VideoFolderModel that = (VideoFolderModel) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getFolderName(), that.getFolderName()) && Objects.equals(getVideoList(), that.getVideoList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFolderName(), getVideoList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public List<VideoModel> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<VideoModel> videoList) {
        this.videoList = videoList;
    }
}
