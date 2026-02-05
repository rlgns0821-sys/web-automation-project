package com.korea.boardEx.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.korea.boardEx.bean.vo.BoardExVO;

@Mapper
public interface BoardExMapper {
	// 게시글 리스트 조회
	public List<BoardExVO> getList();
}
