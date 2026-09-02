package com.littlecase.myblog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.littlecase.myblog.common.Result;
import com.littlecase.myblog.dto.LoginDTO;
import com.littlecase.myblog.dto.RegisterDTO;
import com.littlecase.myblog.service.UserService;
import com.littlecase.myblog.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<Object> login(@Valid @RequestBody LoginDTO dto) {
        UserVO userVO = userService.login(dto);
        String tokens = StpUtil.getTokenValue();
        return Result.ok(new Object() {
            public final String token = tokens;
            public final UserVO userInfo = userVO;
        });
    }

    @PostMapping("/logout")
    @SaCheckLogin
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @GetMapping("/me")
    @SaCheckLogin
    public Result<UserVO> me() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userService.getCurrentUser(userId));
    }
}