package com.korea.boardEx.bean.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.korea.boardEx.bean.vo.UserVO;
import com.korea.boardEx.mapper.UserMapper;

@Repository
public class UserDAO {

    @Autowired
    private UserMapper mapper;

    public UserVO findByLoginId(String loginId) {
        return mapper.findByLoginId(loginId);
    }
}
