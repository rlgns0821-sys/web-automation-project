package com.korea.boardEx.bean.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.korea.boardEx.bean.vo.CommentsVO;
import com.korea.boardEx.mapper.CommentsMapper;

@Repository
public class CommentsDAO {
	
	@Autowired
	CommentsMapper mapper;
	
	// 댓글 리스트
	public List<CommentsVO> getList(Long postId){
		return mapper.getList(postId);
	};
	
	// 댓글 등록
	public int register(CommentsVO vo){
		return mapper.insert(vo);
	};
	
	// 댓글 수정
	public int modify(Long commentId, String content){
		return mapper.update(commentId, content);
	};
	
	// 댓글 수정
	public int remove(Long commentId){
		return mapper.delete(commentId);
	};
}
 