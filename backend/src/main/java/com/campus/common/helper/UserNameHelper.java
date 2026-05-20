package com.campus.common.helper;

import com.campus.user.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 用户名称辅助类，根据用户ID设置用户显示名称。
 * 用于在实体中填充瞬态字段 adminName / userName。
 */
@Component
public class UserNameHelper {

    private final UserRepository userRepository;

    public UserNameHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户ID查询用户名称并设置到指定的 Consumer（通常为 setter 方法引用）。
     *
     * @param userId 用户ID
     * @param setter 接收用户名称的 Consumer
     */
    public void setUserName(Long userId, Consumer<String> setter) {
        userRepository.findById(userId).ifPresent(u -> setter.accept(u.getName()));
    }
}
