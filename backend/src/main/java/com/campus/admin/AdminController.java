package com.campus.admin;

import com.campus.common.Result;
import com.campus.common.exception.BusinessException;
import com.campus.common.helper.UserNameHelper;
import com.campus.log.annotation.Loggable;
import com.campus.lostfound.entity.LostFoundItem;
import com.campus.lostfound.repository.LostFoundItemRepository;
import com.campus.post.entity.Post;
import com.campus.post.repository.PostRepository;
import com.campus.post.service.PostService;
import com.campus.question.repository.QuestionRepository;
import com.campus.report.entity.Report;
import com.campus.report.repository.ReportRepository;
import com.campus.trade.repository.TradeItemRepository;
import com.campus.user.dto.LoginResponse;
import com.campus.user.entity.User;
import com.campus.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.campus.common.helper.UserNameHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员控制器，提供用户审核、帖子审核等管理功能。
 * 所有接口要求调用者具有 ADMIN 角色。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PostService postService;
    private final UserNameHelper userNameHelper;
    private final QuestionRepository questionRepository;
    private final TradeItemRepository tradeItemRepository;
    private final LostFoundItemRepository lostFoundRepository;
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;

    public AdminController(UserRepository userRepository, PostService postService,
                           UserNameHelper userNameHelper, QuestionRepository questionRepository,
                           TradeItemRepository tradeItemRepository,
                           LostFoundItemRepository lostFoundRepository,
                           ReportRepository reportRepository,
                           PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postService = postService;
        this.userNameHelper = userNameHelper;
        this.questionRepository = questionRepository;
        this.tradeItemRepository = tradeItemRepository;
        this.lostFoundRepository = lostFoundRepository;
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
    }

    /**
     * 校验当前用户是否为管理员。
     *
     * @param auth Spring Security 认证信息
     * @throws BusinessException 若用户不存在或不是 ADMIN 角色
     */
    private void checkAdmin(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BusinessException("无管理员权限");
        }
    }

    /**
     * 获取待审核的用户列表（按创建时间降序）。
     *
     * @param page 页码（从0开始）
     * @param size 每页条数
     * @param auth 认证信息
     * @return 分页的待审核用户
     */
    @GetMapping("/users/pending")
    public Result<org.springframework.data.domain.Page<User>> getPendingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkAdmin(auth);
        var pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = userRepository.findByEnabledFalse(pr);
        result.forEach(u -> userNameHelper.setUserName(u.getId(), u::setName));
        return Result.success(result);
    }

    /**
     * 审批通过指定用户的注册申请，将其启用。
     *
     * @param id   用户ID
     * @param auth 认证信息
     * @return 审批后的用户信息
     */
    @PutMapping("/users/{id}/approve")
    @Loggable(action = "审批通过用户", target = "用户")
    public Result<LoginResponse.UserInfo> approveUser(@PathVariable Long id, Authentication auth) {
        checkAdmin(auth);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setEnabled(true);
        userRepository.save(user);
        return Result.success(toUserInfo(user));
    }

    /**
     * 驳回指定用户的注册申请，从数据库中删除该用户。
     *
     * @param id   用户ID
     * @param auth 认证信息
     * @return 操作结果
     */
    @PutMapping("/users/{id}/reject")
    @Loggable(action = "驳回用户", target = "用户")
    public Result<Void> rejectUser(@PathVariable Long id, Authentication auth) {
        checkAdmin(auth);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        userRepository.delete(user);
        return Result.success();
    }

    /**
     * 获取待审核的帖子列表。
     *
     * @param page 页码
     * @param size 每页条数
     * @param auth 认证信息
     * @return 分页的待审核帖子
     */
    @GetMapping("/posts/pending")
    public Result<Page<Post>> getPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkAdmin(auth);
        return Result.success(postService.getPendingPosts(page, size));
    }

    /**
     * 审批通过指定帖子。
     *
     * @param id   帖子ID
     * @param auth 认证信息
     * @return 操作结果
     */
    @PutMapping("/posts/{id}/approve")
    @Loggable(action = "审批通过帖子", target = "帖子")
    public Result<Void> approvePost(@PathVariable Long id, Authentication auth) {
        checkAdmin(auth);
        postService.approvePost(id);
        return Result.success();
    }

    /**
     * 驳回指定帖子。
     *
     * @param id   帖子ID
     * @param auth 认证信息
     * @return 操作结果
     */
    @PutMapping("/posts/{id}/reject")
    @Loggable(action = "驳回帖子", target = "帖子")
    public Result<Void> rejectPost(@PathVariable Long id, Authentication auth) {
        checkAdmin(auth);
        postService.rejectPost(id);
        return Result.success();
    }

    /**
     * 获取管理后台仪表盘统计数据。
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(Authentication auth) {
        checkAdmin(auth);
        Map<String, Object> data = new HashMap<>();

        data.put("totalUsers", userRepository.count());
        data.put("totalQuestions", questionRepository.count());
        data.put("totalTrades", tradeItemRepository.count());
        data.put("totalPosts", postService.getApprovedPosts(0, 1).getTotalElements());
        data.put("totalLostFound", lostFoundRepository.count());
        data.put("pendingUsers", userRepository.findByEnabledFalse(PageRequest.of(0, 1)).getTotalElements());
        data.put("pendingPosts", postService.getPendingPosts(0, 1).getTotalElements());

        // 登录用户占比（学生 vs 教师）
        Map<String, Long> userTypeDistribution = new HashMap<>();
        userTypeDistribution.put("student", userRepository.countByUserType("STUDENT"));
        userTypeDistribution.put("teacher", userRepository.countByUserType("TEACHER"));
        data.put("userTypeDistribution", userTypeDistribution);

        // 二手商品分类统计
        Map<String, Long> tradeCategories = new LinkedHashMap<>();
        String[] cats = {"教材", "电子产品", "生活用品", "服饰", "体育", "其他"};
        for (String cat : cats) {
            tradeCategories.put(cat, tradeItemRepository.countByCategory(cat));
        }
        data.put("tradeCategories", tradeCategories);

        return Result.success(data);
    }

    /**
     * 获取待处理的举报列表。
     */
    @GetMapping("/reports/pending")
    public Result<Page<Report>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkAdmin(auth);
        return Result.success(reportRepository.findByStatusOrderByCreatedAtDesc("PENDING",
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    /**
     * 处理举报（标记为已处理）。
     */
    @PutMapping("/reports/{id}/resolve")
    @Loggable(action = "处理举报", target = "举报")
    public Result<Void> resolveReport(@PathVariable Long id, Authentication auth) {
        checkAdmin(auth);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("举报不存在"));
        report.setStatus("RESOLVED");
        reportRepository.save(report);
        return Result.success();
    }

    /**
     * 获取近7天数据趋势（用户注册数 + 发帖数）。
     */
    @GetMapping("/dashboard/timeline")
    public Result<Map<String, Object>> getTimeline(Authentication auth) {
        checkAdmin(auth);
        List<String> dates = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> newPosts = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            dates.add(dayStart.toLocalDate().toString());
            newUsers.add(userRepository.countByCreatedAtBetween(dayStart, dayEnd));
            newPosts.add(postRepository.countByCreatedAtBetween(dayStart, dayEnd));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("newUsers", newUsers);
        data.put("newPosts", newPosts);
        return Result.success(data);
    }

    /**
     * 获取所有已通过的帖子列表（用于置顶管理）。
     */
    @GetMapping("/posts")
    public Result<Page<Post>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkAdmin(auth);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "pinned").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<Post> result = postRepository.findByStatusOrderByCreatedAtDesc("APPROVED", pageRequest);
        result.forEach(p -> userNameHelper.setUserName(p.getUserId(), p::setUserName));
        return Result.success(result);
    }

    /**
     * 切换帖子的置顶状态。
     */
    @PutMapping("/posts/{id}/pin")
    @Loggable(action = "置顶切换", target = "帖子")
    public Result<Post> togglePinPost(@PathVariable Long id, Authentication auth) {
        checkAdmin(auth);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.setPinned(!Boolean.TRUE.equals(post.getPinned()));
        postRepository.save(post);
        return Result.success(post);
    }

    /**
     * 获取所有失物招领记录（用于置顶管理）。
     */
    @GetMapping("/lost-found")
    public Result<Page<LostFoundItem>> getLostFoundItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        checkAdmin(auth);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "pinned").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<LostFoundItem> result = lostFoundRepository.findAll(pageRequest);
        result.forEach(i -> userNameHelper.setUserName(i.getUserId(), i::setUserName));
        return Result.success(result);
    }

    /**
     * 切换失物招领的置顶状态。
     */
    @PutMapping("/lost-found/{id}/pin")
    @Loggable(action = "置顶切换", target = "失物招领")
    public Result<LostFoundItem> togglePinLostFound(@PathVariable Long id, Authentication auth) {
        checkAdmin(auth);
        LostFoundItem item = lostFoundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        item.setPinned(!Boolean.TRUE.equals(item.getPinned()));
        lostFoundRepository.save(item);
        return Result.success(item);
    }

    /**
     * 将 User 实体转换为 LoginResponse.UserInfo DTO。
     *
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
