package com.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园互助平台主启动类。
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CampusApplication {
    /**
     * 应用入口方法。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }
}
