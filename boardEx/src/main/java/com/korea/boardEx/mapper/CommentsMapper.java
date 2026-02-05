package com.korea.boardEx.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.korea.boardEx.bean.vo.CommentsVO;

@Mapper
public interface CommentsMapper {
	
	// 댓글 리스트
	public List<CommentsVO> getList(Long postId);
	
	// 댓글 등록
	public int insert(CommentsVO vo);
	
	// 댓글 수정
	public int update(@Param("commentId") Long commentId
					 , @Param("content") String content);
	
	// 댓글 삭제
	public int delete(@Param("commentId") Long commentId);  
}
