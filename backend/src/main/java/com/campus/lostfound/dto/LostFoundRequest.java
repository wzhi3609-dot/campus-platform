package com.campus.lostfound.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 失物招领请求 DTO，封装发布/编辑失物招领时提交的信息
 */
public class LostFoundRequest {

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    private String location;

    private String image;

    private String contactPhone;

    private String contactPerson;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
}
