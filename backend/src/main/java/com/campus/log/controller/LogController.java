package com.campus.log.controller;

import com.campus.common.Result;
import com.campus.log.entity.SystemLog;
import com.campus.log.repository.SystemLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 日志控制器（管理员接口），提供系统操作日志的分页查询。
 */
@RestController
@RequestMapping("/api/admin/logs")
public class LogController {

    private final SystemLogRepository systemLogRepository;

    public LogController(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * 分页获取系统操作日志，按创建时间降序排列。
     *
     * @param page 页码
     * @param size 每页条数
     * @return 日志分页结果
     */
    @GetMapping
    public Result<Page<SystemLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return Result.success(systemLogRepository.findByOrderByCreatedAtDesc(pr));
    }
}
