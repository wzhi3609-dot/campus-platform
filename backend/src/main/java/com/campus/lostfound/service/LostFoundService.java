package com.campus.lostfound.service;

import com.campus.common.exception.BusinessException;
import com.campus.common.helper.UserNameHelper;
import com.campus.lostfound.dto.LostFoundRequest;
import com.campus.lostfound.entity.LostFoundItem;
import com.campus.lostfound.repository.LostFoundItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 失物招领业务逻辑服务，处理失物招领信息的发布、编辑、查询和解决等业务
 */
@Service
public class LostFoundService {

    private final LostFoundItemRepository repository;
    private final UserNameHelper userNameHelper;

    public LostFoundService(LostFoundItemRepository repository, UserNameHelper userNameHelper) {
        this.repository = repository;
        this.userNameHelper = userNameHelper;
    }

    /**
     * 创建失物招领记录
     * @param request 失物招领请求
     * @param userId  发布用户ID
     * @return 创建的记录
     */
    public LostFoundItem createItem(LostFoundRequest request, Long userId) {
        LostFoundItem item = new LostFoundItem();
        item.setType(request.getType());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setImage(request.getImage());
        item.setContactPhone(request.getContactPhone());
        item.setContactPerson(request.getContactPerson());
        item.setUserId(userId);
        item.setStatus("ACTIVE");
        item = repository.save(item);
        userNameHelper.setUserName(userId, item::setUserName);
        return item;
    }

    /**
     * 更新失物招领记录（仅发布者可操作）
     * @param id      记录ID
     * @param userId  操作用户ID
     * @param request 失物招领请求
     * @return 更新后的记录
     */
    public LostFoundItem updateItem(Long id, Long userId, LostFoundRequest request) {
        LostFoundItem item = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的记录");
        }
        item.setType(request.getType());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setImage(request.getImage());
        item.setContactPhone(request.getContactPhone());
        item.setContactPerson(request.getContactPerson());
        item = repository.save(item);
        userNameHelper.setUserName(userId, item::setUserName);
        return item;
    }

    /**
     * 根据ID获取失物招领记录详情
     * @param id 记录ID
     * @return 记录详情
     */
    public LostFoundItem getItem(Long id) {
        LostFoundItem item = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        userNameHelper.setUserName(item.getUserId(), item::setUserName);
        return item;
    }

    /**
     * 分页查询失物招领列表，支持按类型（失物/招领）筛选
     * @param page 页码
     * @param size 每页条数
     * @param type 类型（LOST/ FOUND）
     * @return 分页列表
     */
    public Page<LostFoundItem> getItems(int page, int size, String type) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<LostFoundItem> result;
        if (type != null && !type.isBlank()) {
            result = repository.findByTypeAndStatusOrderByPinnedDescCreatedAtDesc(type, "ACTIVE", pageRequest);
        } else {
            result = repository.findByStatusOrderByPinnedDescCreatedAtDesc("ACTIVE", pageRequest);
        }
        result.forEach(i -> userNameHelper.setUserName(i.getUserId(), i::setUserName));
        return result;
    }

    /**
     * 按关键字搜索失物招领记录
     */
    public Page<LostFoundItem> searchItems(int page, int size, String type, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<LostFoundItem> result;
        if (type != null && !type.isBlank()) {
            result = repository.findByTypeAndTitleContainingOrderByPinnedDescCreatedAtDesc(type, keyword, pageRequest);
        } else {
            result = repository.findByTitleContainingOrderByPinnedDescCreatedAtDesc(keyword, pageRequest);
        }
        result.forEach(i -> userNameHelper.setUserName(i.getUserId(), i::setUserName));
        return result;
    }

    /**
     * 将失物招领记录标记为已解决
     * @param id     记录ID
     * @param userId 操作用户ID
     */
    public void resolveItem(Long id, Long userId) {
        LostFoundItem item = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("只能操作自己的记录");
        }
        item.setStatus("RESOLVED");
        repository.save(item);
    }

    /**
     * 删除失物招领记录（仅发布者可操作）
     * @param id     记录ID
     * @param userId 操作用户ID
     */
    public void deleteItem(Long id, Long userId) {
        LostFoundItem item = repository.findById(id)
                .orElseThrow(() -> new BusinessException("记录不存在"));
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的记录");
        }
        repository.delete(item);
    }
}
