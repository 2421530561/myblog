package com.littlecase.myblog.service;
import com.littlecase.myblog.dto.LoginDTO;
import com.littlecase.myblog.dto.RegisterDTO;
import com.littlecase.myblog.vo.UserVO;

public interface UserService {
    void register(RegisterDTO registerDTO);

    UserVO login(LoginDTO loginDTO);

    UserVO getCurrentUser(Long userId);
    
}