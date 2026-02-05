package com.korea.boardEx.bean.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.korea.boardEx.bean.vo.CommunityVO;
import com.korea.boardEx.bean.vo.Criteria;
import com.korea.boardEx.mapper.CommunityMapper;

@Repository
public class CommunityDAO {
	
	@Autowired
	CommunityMapper mapper;
	
	// 좋아요 관련
    public int likePost(Long postId) {
        mapper.increaseLikes(postId);
        return mapper.getLikes(postId);
    }
	
	// 커뮤니티 리스트
	public List<CommunityVO> getList(){
		return mapper.getList();
	}
	
	// 커뮤니티 등록
	public int register(CommunityVO vo) {
		return mapper.insertSelectKey(vo);
	}
	
	// 커뮤니티 삭제
	public int remove(Long postId) {
		return mapper.delete(postId);
	}
	
	// 커뮤니티 상세
	public CommunityVO read(Long postId) {
		return mapper.select(postId);
	}
	
	// 커뮤니티 수정
	public int modify(CommunityVO vo) {
		return mapper.update(vo);
	}
	
	// 카테고리 수정
	public void updateCategory(Long postId, String category) {
		 mapper.updateCategory(postId, category);
	}
	
	// 댓글 개수
	public List<CommunityVO> getCommentCount(){
		return mapper.getCommentCount();
	}
	
	// 페이징 처리
	public List<CommunityVO> getListWithPaging(Criteria cri){
		cri.setOffset();
		return mapper.getListWithPaging(cri);
	}
	
	// 페이징 처리
	public List<CommunityVO> getListWithSearch(String type, String keyword){
		return mapper.getListWithSearch(type, keyword);
	}
	
	public int getTotalCount(Criteria cri) {
		return mapper.getTotalCount(cri);
	}
}