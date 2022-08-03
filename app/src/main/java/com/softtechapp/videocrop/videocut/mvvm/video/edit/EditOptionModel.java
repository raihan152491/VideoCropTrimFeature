package com.softtechapp.videocrop.videocut.mvvm.video.edit;

import java.util.Objects;

public class EditOptionModel {

    private int id;
    private int icon;
    private String title;
    private  String iconUrl;

    public EditOptionModel() {
    }

    public EditOptionModel(int id, int icon, String title, String iconUrl) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditOptionModel)) return false;
        EditOptionModel that = (EditOptionModel) o;
        return getId() == that.getId() && getIcon() == that.getIcon() && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getIconUrl(), that.getIconUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getIcon(), getTitle(), getIconUrl());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
