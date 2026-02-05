package com.korea.boardEx.bean.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Alias("userVO")
@Data
public class UserVO {
	    private String loginId;
	    private String loginName;
	    private String phone;   // 🔥 문자 받을 번호
}
