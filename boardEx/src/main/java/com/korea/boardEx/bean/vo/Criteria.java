package com.korea.boardEx.bean.vo;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component // 생성자 다른 곳에서 @Autowired
@Data
public class Criteria { // 페이지 검색 기준
	private int pageNum; // 현재 페이지
	private int amount; // 한 페이지에 들어갈 게시글 수
	private int offset; // 현재 페이지의 기준이 되는 글 누적 개수
	
	// 검색어
	private String type;
	private String keyword;
	
	public Criteria() {
		this.pageNum = 1;
		this.amount = 5;
	}
	// 계산 하고 나온 결과 값의 개수를 스킵하고 그 다음 5개(amount값)를 가져오는 구조
	public void setOffset() {
		offset = (pageNum - 1) * amount;
	}
}
