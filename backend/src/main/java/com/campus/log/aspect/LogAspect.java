package com.campus.log.aspect;

import com.campus.log.annotation.Loggable;
import com.campus.log.entity.SystemLog;
import com.campus.log.repository.SystemLogRepository;
import com.campus.user.entity.User;
import com.campus.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 日志切面，拦截所有标注 @Loggable 的方法，自动记录操作日志到数据库。
 */
@Aspect
@Component
public class LogAspect {

    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    public LogAspect(SystemLogRepository systemLogRepository, UserRepository userRepository) {
        this.systemLogRepository = systemLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * 环绕通知，在目标方法执行后记录操作日志。
     * 包含操作用户、IP、请求参数等信息。
     *
     * @param joinPoint 连接点
     * @param loggable  日志注解
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(loggable)")
    public Object log(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                SystemLog log = new SystemLog();
                log.setUserId(userId);
                log.setAction(loggable.action());
                log.setTarget(loggable.target());

                userRepository.findById(userId).ifPresent(u -> {
                    log.setUserType(u.getUserType());
                    log.setUserName(u.getName());
                });

                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                log.setIp(request.getRemoteAddr());

                Object[] args = joinPoint.getArgs();
                StringBuilder detail = new StringBuilder();
                for (Object arg : args) {
                    if (arg != null && !(arg instanceof Authentication) && !(arg instanceof HttpServletRequest)) {
                        detail.append(arg.toString()).append(" ");
                    }
                }
                log.setDetail(detail.length() > 500 ? detail.substring(0, 500) : detail.toString());

                systemLogRepository.save(log);
            }
        } catch (Exception ignored) {
        }

        return result;
    }
}
