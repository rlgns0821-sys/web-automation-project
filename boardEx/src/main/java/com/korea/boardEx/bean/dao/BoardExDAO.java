package com.korea.boardEx.bean.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.korea.boardEx.bean.vo.BoardExVO;
import com.korea.boardEx.mapper.BoardExMapper;

@Repository
public class BoardExDAO {
	
	@Autowired
	BoardExMapper mapper;
	
	public List<BoardExVO> getList(){
		return mapper.getList();
	} 
}
