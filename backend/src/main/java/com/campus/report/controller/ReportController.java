package com.campus.report.controller;

import com.campus.common.Result;
import com.campus.report.entity.Report;
import com.campus.report.repository.ReportRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reportRepository;

    public ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @PostMapping
    public Result<Void> submitReport(@RequestBody Map<String, Object> body,
                                     Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType((String) body.get("targetType"));
        report.setTargetId(Long.valueOf(body.get("targetId").toString()));
        report.setReason((String) body.get("reason"));
        report.setStatus("PENDING");
        reportRepository.save(report);
        return Result.success();
    }
}
