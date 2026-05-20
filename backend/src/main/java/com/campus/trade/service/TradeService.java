package com.campus.trade.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.helper.UserNameHelper;
import com.campus.trade.dto.TradeItemRequest;
import com.campus.trade.entity.TradeItem;
import com.campus.trade.repository.TradeItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 二手交易业务逻辑服务，处理商品发布、编辑、查询和状态更新等业务
 */
@Service
public class TradeService {

    private final TradeItemRepository tradeItemRepository;
    private final UserNameHelper userNameHelper;

    public TradeService(TradeItemRepository tradeItemRepository, UserNameHelper userNameHelper) {
        this.tradeItemRepository = tradeItemRepository;
        this.userNameHelper = userNameHelper;
    }

    /**
     * 创建二手商品
     * @param request 商品请求
     * @param userId  发布用户ID
     * @return 创建的商品
     */
    public TradeItem createItem(TradeItemRequest request, Long userId) {
        TradeItem item = new TradeItem();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImages(request.getImages());
        item.setContactPerson(request.getContactPerson());
        item.setContactPhone(request.getContactPhone());
        item.setLocation(request.getLocation());
        item.setUserId(userId);
        item.setStatus("AVAILABLE");
        item = tradeItemRepository.save(item);
        userNameHelper.setUserName(userId, item::setUserName);
        return item;
    }

    /**
     * 更新二手商品（仅发布者可操作）
     * @param id      商品ID
     * @param userId  操作用户ID
     * @param request 商品请求
     * @return 更新后的商品
     */
    public TradeItem updateItem(Long id, Long userId, TradeItemRequest request) {
        TradeItem item = tradeItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的商品");
        }
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setImages(request.getImages());
        item.setContactPerson(request.getContactPerson());
        item.setContactPhone(request.getContactPhone());
        item.setLocation(request.getLocation());
        item = tradeItemRepository.save(item);
        userNameHelper.setUserName(userId, item::setUserName);
        return item;
    }

    /**
     * 根据ID获取商品详情，并增加浏览次数
     * @param id 商品ID
     * @return 商品详情
     */
    public TradeItem getItem(Long id) {
        TradeItem item = tradeItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        item.setViewCount(item.getViewCount() + 1);
        item = tradeItemRepository.save(item);
        userNameHelper.setUserName(item.getUserId(), item::setUserName);
        return item;
    }

    /**
     * 分页查询商品列表，支持按关键词和分类筛选
     * @param page     页码
     * @param size     每页条数
     * @param keyword  搜索关键词
     * @param category 商品分类
     * @return 商品分页列表
     */
    public Page<TradeItem> getItems(int page, int size, String keyword, String category) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TradeItem> result;
        if (keyword != null && !keyword.isBlank()) {
            result = tradeItemRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword, pageRequest);
        } else if (category != null && !category.isBlank()) {
            result = tradeItemRepository.findByCategoryAndStatus(category, "AVAILABLE", pageRequest);
        } else {
            result = tradeItemRepository.findByStatusOrderByCreatedAtDesc("AVAILABLE", pageRequest);
        }
        result.forEach(i -> userNameHelper.setUserName(i.getUserId(), i::setUserName));
        return result;
    }

    /**
     * 更新商品状态（如上架/下架/已售出）
     * @param id     商品ID
     * @param status 新状态
     * @param userId 操作用户ID
     */
    public void updateStatus(Long id, String status, Long userId) {
        TradeItem item = tradeItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的商品");
        }
        item.setStatus(status);
        tradeItemRepository.save(item);
    }

    /**
     * 删除商品（仅发布者可操作）
     * @param id     商品ID
     * @param userId 操作用户ID
     */
    public void deleteItem(Long id, Long userId) {
        TradeItem item = tradeItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的商品");
        }
        tradeItemRepository.delete(item);
    }
}
