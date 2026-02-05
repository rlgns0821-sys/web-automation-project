package com.korea.boardEx.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.korea.boardEx.bean.vo.CommunityVO;
import com.korea.boardEx.bean.vo.Criteria;

@Mapper
public interface CommunityMapper {
	// 좋아요 관련
	public void increaseLikes(Long postId);
	
	public int getLikes(Long postId);
	
	// 커뮤니티 리스트 
	public List<CommunityVO> getList();
	
	// 커뮤니티 등록
	public int insertSelectKey(CommunityVO vo);
	
	// 커뮤니티 삭제
	public int delete(Long postId);
	
	// 커뮤니티 상세
	public CommunityVO select(Long postId);
	
	// 커뮤니티 수정
	public int update(CommunityVO vo);
	
	// 카테고리 수정
	public void updateCategory(Long postId, String category);
	
	// 댓글 개수
	public List<CommunityVO> getCommentCount();
	
	// 페이징
	// 게시글 목록
	public List<CommunityVO> getListWithPaging(Criteria cri);
	
	// 게시글 검색
	public List<CommunityVO> getListWithSearch(String type, String keyword);
	
	// 전체 게시글 수
	public int getTotalCount(Criteria cri);
}
