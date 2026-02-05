package com.korea.boardEx.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.korea.boardEx.bean.vo.FavoriteVO;
import com.korea.boardEx.bean.vo.ScriptsVO;

@Mapper
public interface FavoritesMapper {
	public List<ScriptsVO> getList(String userId);
	
	public int insert(FavoriteVO vo);
	
	public int exists(@Param("userId") String uerId
					, @Param("siteId") String siteId);
	
	public int removeFavorite(@Param("userId") String userId,
            					@Param("siteId") String siteId);
}
