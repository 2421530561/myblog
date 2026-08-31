package com.littlecase.myblog.service.impl;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.littlecase.myblog.common.BusinessException;
import com.littlecase.myblog.dto.LoginDTO;
import com.littlecase.myblog.dto.RegisterDTO;
import com.littlecase.myblog.entity.User;
import com.littlecase.myblog.mapper.UserMapper;
import com.littlecase.myblog.service.UserService;
import com.littlecase.myblog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public void register(RegisterDTO registerdto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0)
               .and(w -> w.eq(User::getUsername, registerdto.getUsername())
                         .or()
                         .eq(User::getEmail, registerdto.getEmail()));
        if (userMapper.exists(wrapper)) {
            throw new BusinessException("用户名或邮箱已被注册");
        }

        User user = new User();
        user.setUsername(registerdto.getUsername());
        user.setEmail(registerdto.getEmail());
        user.setPassword(DigestUtils.md5DigestAsHex(registerdto.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setNickname(registerdto.getUsername());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public UserVO login(LoginDTO logindto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0)
               .eq(User::getUsername, logindto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        String inputPwd = DigestUtils.md5DigestAsHex(logindto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!inputPwd.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        StpUtil.login(user.getId());
        return toVO(user);
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}