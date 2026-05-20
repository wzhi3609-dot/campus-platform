package com.campus.upload;

import com.campus.common.Result;
import com.campus.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件上传控制器，提供头像上传和多图上传功能。
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB

    /**
     * 上传用户头像，返回可访问的文件路径。
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }
        validateImage(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, "avatars");
        Files.createDirectories(dir);
        file.transferTo(dir.resolve(filename));
        return Result.success("/uploads/avatars/" + filename);
    }

    /**
     * 多图上传（用于二手交易、论坛等），返回所有图片的访问路径。
     */
    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }
        List<String> urls = new ArrayList<>();
        Path dir = Paths.get(uploadDir, "images");
        Files.createDirectories(dir);
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
        validateAvatar(file);
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            file.transferTo(dir.resolve(filename));
            urls.add("/uploads/images/" + filename);
        }
        return Result.success(urls);
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 5MB");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException("仅支持 jpg、png、gif、webp 格式的图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("仅支持 jpg、png、gif、webp 格式的图片");
        }
    }

    private void validateAvatar(MultipartFile file) {
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("头像文件大小不能超过 2MB");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BusinessException("仅支持 jpg、png、gif、webp 格式的图片");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("仅支持 jpg、png、gif、webp 格式的图片");
        }
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return ".jpg";
        return name.substring(name.lastIndexOf('.'));
    }
}
