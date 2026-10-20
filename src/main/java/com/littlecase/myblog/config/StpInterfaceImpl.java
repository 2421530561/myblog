package com.littlecase.myblog.config;

import cn.dev33.satoken.stp.StpInterface;
import com.littlecase.myblog.entity.User;
import com.littlecase.myblog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 角色数据源
 * <p>
 * 坑：Sa-Token 不会自动读取业务库里的角色，必须实现 {@link StpInterface}，
 * 否则 {@code @SaCheckRole("ADMIN")} 与 {@code StpUtil.hasRole()} 永远判为无角色（全部 403）。
 * 这里从 user.role 读取（ADMIN / USER，PRD 1.3）。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    /** 本博客只做两级角色，不涉及细粒度权限点 */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return new ArrayList<>();
        }
        User user = userMapper.selectById(Long.valueOf(loginId.toString()));
        if (user == null || user.getRole() == null || user.getStatus() == null || user.getStatus() == 0) {
            return new ArrayList<>();
        }
        return List.of(user.getRole());
    }
}
