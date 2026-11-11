package com.travel.service.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.entity.Order;
import com.travel.entity.User;
import com.travel.exception.BusinessException;
import com.travel.mapper.OrderMapper;
import com.travel.mapper.UserMapper;

@Service
public class AdminUserService {
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserMapper userMapper, OrderMapper orderMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<User> getUserList(String keyword, Integer status, int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getPhone, keyword)
                    .or().like(User::getNickname, keyword));
        }
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        
        // 隐藏密码
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public Map<String, Object> getUserDetail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        
        // 查询用户订单
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, id)
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 10"));
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("orders", orders);
        result.put("orderCount", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, id)));
        
        return result;
    }

    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public void updateUserRole(Long id, String role) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new BusinessException("无效的角色");
        }
        user.setRole(role);
        userMapper.updateById(user);
    }

    public void updateUserStudentStatus(Long id, Integer isStudent) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setIsStudent(isStudent);
        userMapper.updateById(user);
    }

    public void updateUserPassword(Long id, String password) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码长度至少6位");
        }
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }
}
