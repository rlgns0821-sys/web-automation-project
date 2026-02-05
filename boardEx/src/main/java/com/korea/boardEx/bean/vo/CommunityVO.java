package com.korea.boardEx.bean.vo;

import java.time.LocalDate;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Alias("communityVO")
@Data
public class CommunityVO {
	private Long postId; // 커뮤니티 ID PK
	private String title; // 커뮤니티 제목
	private String content; // 커뮤니티 내용
	private String category; // 검토중 / 승인 / 반려
	private String author; // 글쓴이
	private int likes; // 좋아요 숫자 
	private LocalDate createdAt; // 등록 날짜
	private LocalDate updatedAt; // 수정 날짜 
	private String ticket; //  교통 / 티케팅  
	private int comments; // 댓글 수
	private String commentId; // 댓글 ID FK
}
