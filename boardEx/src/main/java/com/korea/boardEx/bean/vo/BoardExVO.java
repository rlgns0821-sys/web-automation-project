package com.korea.boardEx.bean.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Alias("boardExVO")
@Data
public class BoardExVO {
	private Long bno;
	private String title;
	private String content;
	private String writer;
	private String regdate;
	private String updatedate;
	
}
