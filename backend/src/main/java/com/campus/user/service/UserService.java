package com.campus.user.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.security.JwtTokenProvider;
import com.campus.log.entity.SystemLog;
import com.campus.log.repository.SystemLogRepository;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.dto.RegisterRequest;
import com.campus.user.entity.User;
import com.campus.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务逻辑服务，处理注册、登录、密码修改、资料更新等核心业务
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SystemLogRepository systemLogRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, SystemLogRepository systemLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * 用户注册：校验用户类型和学号/工号，创建新用户并保存
     * @param request 注册请求
     * @return 登录响应（包含用户信息）
     */
    public LoginResponse register(RegisterRequest request) {
        if (!"STUDENT".equals(request.getUserType()) && !"TEACHER".equals(request.getUserType())) {
            throw new BusinessException("用户类型错误");
        }

        User user = new User();
        user.setUserType(request.getUserType());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setEnabled(false);

        if ("STUDENT".equals(request.getUserType())) {
            if (request.getStudentId() == null || request.getStudentId().isBlank()) {
                throw new BusinessException("学号不能为空");
            }
            if (userRepository.existsByStudentId(request.getStudentId())) {
                throw new BusinessException("该学号已被注册");
            }
            user.setStudentId(request.getStudentId());
            user.setUsername(request.getStudentId());
        } else {
            if (request.getTeacherId() == null || request.getTeacherId().isBlank()) {
                throw new BusinessException("工号不能为空");
            }
            if (userRepository.existsByTeacherId(request.getTeacherId())) {
                throw new BusinessException("该工号已被注册");
            }
            user.setTeacherId(request.getTeacherId());
            user.setUsername(request.getTeacherId());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        user = userRepository.save(user);
        saveLog(user.getId(), user.getUserType(), user.getName(), "注册", user.getUserType(), "新用户注册");
        return new LoginResponse(null, toUserInfo(user));
    }

    /**
     * 用户登录：根据用户类型查找账号，校验密码和账号状态，生成 JWT token
     * @param request 登录请求
     * @return 登录响应（包含 token 和用户信息）
     */
    public LoginResponse login(LoginRequest request) {
        User user;

        if ("STUDENT".equals(request.getUserType())) {
            user = userRepository.findByStudentId(request.getAccount())
                    .orElseThrow(() -> new BusinessException("学号或密码错误"));
        } else if ("TEACHER".equals(request.getUserType())) {
            user = userRepository.findByTeacherId(request.getAccount())
                    .orElseThrow(() -> new BusinessException("工号或密码错误"));
        } else {
            throw new BusinessException("用户类型错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String msg = "STUDENT".equals(request.getUserType()) ? "学号或密码错误" : "工号或密码错误";
            throw new BusinessException(msg);
        }

        if (!user.getEnabled()) {
            throw new BusinessException("账号待管理员审核");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        saveLog(user.getId(), user.getUserType(), user.getName(), "登录", user.getUserType(), user.getUsername());
        return new LoginResponse(token, toUserInfo(user));
    }

    /**
     * 根据用户ID获取用户基本信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public LoginResponse.UserInfo getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toUserInfo(user);
    }

    /**
     * 修改密码：校验原密码正确性后更新为新密码
     * @param userId      用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 管理员登录：校验管理员身份并生成 token
     * @param username 管理员账号
     * @param password 管理员密码
     * @return 登录响应（包含 token 和用户信息）
     */
    public LoginResponse adminLogin(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByTeacherId(username).orElse(null));
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new BusinessException("无管理员权限");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("管理员账号或密码错误");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        saveLog(user.getId(), user.getUserType(), user.getName(), "登录", "管理后台", user.getUsername());
        return new LoginResponse(token, toUserInfo(user));
    }

    /**
     * 更新个人资料：修改姓名、手机号、邮箱和头像
     * @param userId 用户ID
     * @param name   新姓名
     * @param phone  新手机号
     * @param email  新邮箱
     * @param avatar 新头像URL
     * @return 更新后的用户信息
     */
    public LoginResponse.UserInfo updateProfile(Long userId, String name, String phone, String email, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (name != null && !name.isBlank()) {
            user.setName(name.trim());
        }
        if (phone != null && !phone.isBlank()) {
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException("手机号格式不正确");
            }
            user.setPhone(phone.trim());
        }
        if (email != null && !email.isBlank()) {
            if (userRepository.existsByEmailAndIdNot(email.trim(), userId)) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(email.trim());
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        user = userRepository.save(user);
        return toUserInfo(user);
    }

    /**
     * 重置密码：根据姓名、手机号和学号/工号验证身份，生成新密码
     * @param name      姓名
     * @param phone     注册手机号
     * @param studentId 学号（学生）
     * @param teacherId 工号（教师）
     * @return 新密码
     */
    public String resetPassword(String name, String phone, String studentId, String teacherId) {
        User user = null;
        if (studentId != null && !studentId.isBlank()) {
            user = userRepository.findByStudentId(studentId)
                    .orElseThrow(() -> new BusinessException("学号不存在"));
        } else if (teacherId != null && !teacherId.isBlank()) {
            user = userRepository.findByTeacherId(teacherId)
                    .orElseThrow(() -> new BusinessException("工号不存在"));
        } else {
            throw new BusinessException("请提供学号或工号");
        }

        if (user.getName() == null || !user.getName().equals(name)) {
            throw new BusinessException("姓名与注册姓名不匹配");
        }

        if (user.getPhone() == null || !user.getPhone().equals(phone)) {
            throw new BusinessException("手机号与注册手机号不匹配");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        saveLog(user.getId(), user.getUserType(), user.getName(), "重置密码", "用户", "密码重置");
        return newPassword;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }

    /**
     * 记录系统操作日志
     * @param userId   操作用户ID
     * @param userType 用户类型
     * @param userName 用户姓名
     * @param action   操作动作
     * @param target   操作目标
     * @param detail   操作详情
     */
    private void saveLog(Long userId, String userType, String userName, String action, String target, String detail) {
        try {
            SystemLog log = new SystemLog();
            log.setUserId(userId);
            log.setUserType(userType);
            log.setUserName(userName);
            log.setAction(action);
            log.setTarget(target);
            log.setDetail(detail);
            systemLogRepository.save(log);
        } catch (Exception ignored) {}
    }

    /**
     * 将 User 实体转换为 UserInfo DTO
     * @param user 用户实体
     * @return 用户信息 DTO
     */
    private LoginResponse.UserInfo toUserInfo(User user) {
        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setUserType(user.getUserType());
        info.setStudentId(user.getStudentId());
        info.setTeacherId(user.getTeacherId());
        info.setName(user.getName());
        info.setEmail(user.getEmail());
        info.setPhone(user.getPhone());
        info.setAvatar(user.getAvatar());
        info.setRole(user.getRole());
        return info;
    }
}
