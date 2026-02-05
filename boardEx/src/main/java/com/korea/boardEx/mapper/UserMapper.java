package com.korea.boardEx.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.korea.boardEx.bean.vo.UserVO;

@Mapper
public interface UserMapper {

	public UserVO findByLoginId(@Param("loginId") String loginId);

}
