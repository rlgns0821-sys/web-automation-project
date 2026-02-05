package com.korea.boardEx.bean.vo;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class PageDTO {
	private int startPage; // 현재 view  첫 페이지
	private int endPage; // 현재 view 마지막 페이지
	private int realEndPage; // 진짜 마지막 페이지
	private boolean prev, next; // 페이징 시 왼쪽 오른쪽 버튼
	
	private int total;
	private Criteria criteria;
	
	public PageDTO() {}
	
	public PageDTO(Criteria cri, int total) {
		this.criteria = cri;
		this.total = total;
		
		this.endPage = (int) (Math.ceil(cri.getPageNum() / 10.0)) * 10;
		this.startPage = this.endPage - 9;
		
		// 22개의 글이 있다고 가정하면 페이지는 2페이지까지 있어야함
		realEndPage = (int) Math.ceil(total * 1.0 / cri.getAmount());
		
		if (realEndPage < this.endPage) {
			this.endPage = (realEndPage == 0) ? 1 : realEndPage;
		}
		
		this.prev = this.startPage > 1;
		this.next = this.endPage < realEndPage;
	} 
}
