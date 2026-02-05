package com.korea.boardEx.bean.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.korea.boardEx.bean.vo.Criteria;
import com.korea.boardEx.bean.vo.ScriptsVO;
import com.korea.boardEx.mapper.ScriptsMapper;

@Repository
public class ScriptsDAO {
	
	@Autowired
	private ScriptsMapper mapper;
	
	// 사용자용
	public List<ScriptsVO> getOpenScripts(){
		return mapper.getOpenScripts();
	}
	// 관리자용
	public List<ScriptsVO> getAllScripts(){
		return mapper.getAllScripts();
	}
	// 관리자용 + 페이징
	public List<ScriptsVO> getAllScriptsWithPaging(Criteria cri){
		return mapper.getAllScriptsWithPaging(cri);
	}
	// 관리자 스크립트 전체 개수
	public int getTotalCount(Criteria cri) {
		return mapper.getTotalCount(cri);
	}
	
	// insert
	public int register(ScriptsVO vo) {
		return mapper.insert(vo);
	}
	
	// update
	public int modify(ScriptsVO vo) {
		return mapper.update(vo);
	}
	
	// update에서 siteId 값 가져오기 위해서
	public ScriptsVO read(String siteId) {
		return mapper.select(siteId);
	}
	
	// delete
	public int remove(String siteId) {
		return mapper.delete(siteId);
	}
	
	public int toggleOpen(String siteId, String openYn) {
		return mapper.toggleOpen(siteId, openYn);
	}
	
	public int togglePopular(String siteId, String popular) {
		return mapper.togglePopular(siteId, popular);
	}
	
	public int increaseUseCount(String siteId) {
		return mapper.increaseUseCount(siteId);
	}
}
