package com.korea.boardEx.bean.vo;

import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;

import lombok.Data;
// 2026.1.12 스크립트에서 인기 클릭 시 내 스크립트로 이동 테이블 
@Alias("favoriteVO")
@Data
public class FavoriteVO {
	private Long favoriteId;
	private String userId;
	private String siteId;
	private LocalDateTime createdAt;
}
