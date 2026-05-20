package com.campus.home;

import com.campus.common.Result;
import com.campus.lostfound.entity.LostFoundItem;
import com.campus.lostfound.repository.LostFoundItemRepository;
import com.campus.post.entity.Post;
import com.campus.post.repository.PostRepository;
import com.campus.question.entity.Question;
import com.campus.question.repository.QuestionRepository;
import com.campus.trade.entity.TradeItem;
import com.campus.trade.repository.TradeItemRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 首页控制器，提供平台数据统计和全局搜索功能。
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final QuestionRepository questionRepository;
    private final TradeItemRepository tradeItemRepository;
    private final PostRepository postRepository;
    private final LostFoundItemRepository lostFoundRepository;

    public HomeController(QuestionRepository questionRepository,
                          TradeItemRepository tradeItemRepository,
                          PostRepository postRepository,
                          LostFoundItemRepository lostFoundRepository) {
        this.questionRepository = questionRepository;
        this.tradeItemRepository = tradeItemRepository;
        this.postRepository = postRepository;
        this.lostFoundRepository = lostFoundRepository;
    }

    /**
     * 获取平台统计数据（问题数、二手交易数、帖子数、失物招领数）。
     *
     * @return 统计数据 Map
     */
    @GetMapping("/stats")
    @Cacheable("homeStats")
    public Result<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("questions", questionRepository.count());
        stats.put("trades", tradeItemRepository.count());
        stats.put("posts", postRepository.count());
        stats.put("lostFound", lostFoundRepository.count());
        return Result.success(stats);
    }

    /**
     * 全局搜索，根据关键词搜索问题、二手物品、帖子。
     *
     * @param keyword 搜索关键词
     * @return 各模块的搜索结果
     */
    @GetMapping("/search")
    public Result<Map<String, Object>> globalSearch(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();

        PageRequest top5 = PageRequest.of(0, 5);
        result.put("questions", questionRepository.findByTitleContaining(keyword, top5).getContent());
        result.put("trades", tradeItemRepository.findByTitleContaining(keyword, top5).getContent());
        result.put("posts", postRepository.findByTitleContainingAndStatus(keyword, "APPROVED", top5).getContent());

        return Result.success(result);
    }
}
