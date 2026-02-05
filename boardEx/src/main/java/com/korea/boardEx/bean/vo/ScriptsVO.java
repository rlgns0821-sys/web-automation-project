package com.korea.boardEx.bean.vo;

import java.time.LocalDate;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Alias("scriptsVO")
@Data
public class ScriptsVO {
	// 기본 스크립트 정보
	private String siteId; // srt, ktx, interpark
	private String name; // 사이트명 / 스크립트 명
	private String description; // 설명
	private String category; // 티케팅 / 교통
	private String difficulty; // 난이도 : 쉬움 / 보통 / 어려움
	private String url; // 사이트 주소
	
	// 설명용 필드
	private String feature; // 주요기능
	private String feature2; // 주요기능2
	private String feature3; // 주요기능3
	private String feature4; // 주요기능4

	// 상태 / 운영 관련(관리자용)
	private String popular; // 인기 여부 
	private String openYn; // 공개 여부
	private int useCount; // 스크립트 사용 수
	private String manager; // 관리자
	
	// 시간 관리(관리자 + 통계용)
	private LocalDate createdAt; // 스크립트 생성일
	private LocalDate updatedAt; // 스크립트 수정일
	private LocalDate lastUsedAt; // 스크립트 최근 사용일
}
