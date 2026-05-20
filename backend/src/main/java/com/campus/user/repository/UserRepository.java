package com.campus.user.repository;

import com.campus.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层接口，提供用户相关的数据库操作
 * 包括根据学号、工号、用户名、邮箱等条件查询用户
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentId(String studentId);
    Optional<User> findByTeacherId(String teacherId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByStudentId(String studentId);
    boolean existsByTeacherId(String teacherId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<User> findByEnabledFalse();
    org.springframework.data.domain.Page<User> findByEnabledFalse(org.springframework.data.domain.Pageable pageable);
    long countByUserType(String userType);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
