package com.korea.boardEx.bean.vo;

import java.time.LocalDate;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Alias("commentsVO")
@Data
public class CommentsVO {
	private Long commentId; // 댓글 ID PK
	private Long postId; // 커뮤니티 ID FK
	private String content; // 댓글 내용
	private String author; // 댓글 제목
	private LocalDate createdAt; // 등록 날짜
	private LocalDate updatedAt; // 수정 날짜
}
