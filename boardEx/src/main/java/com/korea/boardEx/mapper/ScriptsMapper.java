package com.korea.boardEx.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.korea.boardEx.bean.vo.Criteria;
import com.korea.boardEx.bean.vo.ScriptsVO;

@Mapper
public interface ScriptsMapper {
	// 사용자용
	public List<ScriptsVO> getOpenScripts();
	// 관리자용
	public List<ScriptsVO> getAllScripts();
	// 관리자 페이징
	public List<ScriptsVO> getAllScriptsWithPaging(Criteria cri);
	// 관리자용 스크립트 수
	public int getTotalCount(Criteria cri);
	
	// insert
	public int insert(ScriptsVO vo);
	
	// update
	public int update(ScriptsVO vo);
	
	// siteId값 가져오기
	public ScriptsVO select(String siteId);
	
	// delete
	public int delete(String siteId);

	public int toggleOpen(@Param("siteId") String siteId
						, @Param("openYn") String openYn);

	public int togglePopular(@Param("siteId") String siteId
						   , @Param("popular") String popular);
	
	public int increaseUseCount(String siteId);

}
