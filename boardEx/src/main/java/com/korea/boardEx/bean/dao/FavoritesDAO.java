package com.korea.boardEx.bean.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.korea.boardEx.bean.vo.FavoriteVO;
import com.korea.boardEx.bean.vo.ScriptsVO;
import com.korea.boardEx.mapper.FavoritesMapper;

@Repository
public class FavoritesDAO {
	
	@Autowired
	FavoritesMapper mapper;
	
	public List<ScriptsVO> getList(String userId){
		return mapper.getList(userId);
	}
	
	public void addFavorite(String userId, String siteId) {
		if (mapper.exists(userId, siteId) > 0) {
			throw new IllegalStateException("이미 추가됨");
		}
		
		FavoriteVO vo = new FavoriteVO();
		vo.setUserId(userId);
		vo.setSiteId(siteId);
		
		mapper.insert(vo);
	}
	
	public int removeFavorite(String userId, String siteId) {
		return mapper.removeFavorite(userId, siteId);
    }
}
