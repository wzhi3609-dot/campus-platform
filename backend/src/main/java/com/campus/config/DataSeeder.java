package com.campus.config;

import com.campus.announcement.entity.Announcement;
import com.campus.announcement.repository.AnnouncementRepository;
import com.campus.user.entity.User;
import com.campus.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器，应用启动时自动创建默认管理员账号和初始公告。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnnouncementRepository announcementRepository;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      AnnouncementRepository announcementRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.announcementRepository = announcementRepository;
    }

    /**
     * 应用启动时执行：若数据库中无管理员则创建默认管理员，
     * 若无公告则插入初始公告。
     */
    @Override
    public void run(String... args) {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("管理员");
            admin.setUserType("TEACHER");
            admin.setTeacherId("ADMIN001");
            admin.setEmail("admin@campus.com");
            admin.setPhone("13800000000");
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            admin = userRepository.save(admin);
        }

        if (announcementRepository.count() == 0) {
            Announcement a1 = new Announcement();
            a1.setTitle("校园互助平台正式上线");
            a1.setContent("欢迎来到校园互助平台！在这里你可以：\n- 提出学习问题，互相解答\n- 买卖二手物品\n- 在论坛自由交流\n- 发布失物招领信息\n\n祝大家使用愉快！");
            a1.setPinned(true);
            a1.setAdminId(admin.getId());
            announcementRepository.save(a1);

            Announcement a2 = new Announcement();
            a2.setTitle("使用须知");
            a2.setContent("1. 请使用真实学号/工号注册\n2. 注册后需管理员审核通过方可登录\n3. 论坛帖子需审核通过后可见\n4. 请遵守校园网络文明公约，友善交流");
            a2.setPinned(false);
            a2.setAdminId(admin.getId());
            announcementRepository.save(a2);
        }
    }
}
