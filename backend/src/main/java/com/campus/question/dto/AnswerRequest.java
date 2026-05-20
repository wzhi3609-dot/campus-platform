package com.campus.question.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 回答请求 DTO，封装回答的内容
 */
public class AnswerRequest {

    @NotBlank(message = "回答内容不能为空")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
