package com.korea.boardEx.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.korea.boardEx.BoardExApplication;
import com.korea.boardEx.bean.dao.CommentsDAO;
import com.korea.boardEx.bean.dao.CommunityDAO;
import com.korea.boardEx.bean.dao.FavoritesDAO;
import com.korea.boardEx.bean.dao.ScriptsDAO;
import com.korea.boardEx.bean.vo.CommentsVO;
import com.korea.boardEx.bean.vo.CommunityVO;
import com.korea.boardEx.bean.vo.Criteria;
import com.korea.boardEx.bean.vo.PageDTO;
import com.korea.boardEx.bean.vo.ScriptsVO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/boardEx/*")
public class BoardExController {

    private final BoardExApplication boardExApplication;
    
	@Autowired
	CommunityDAO dao;
	
	@Autowired
	CommentsDAO comDao;

	@Autowired
	FavoritesDAO faDao;
	
	@Autowired
	ScriptsDAO scDao;
	
    BoardExController(BoardExApplication boardExApplication) {
        this.boardExApplication = boardExApplication;
    }
	
	// 메인 홈
	@GetMapping("/")
	public String getHome(Model model) {
		log.info("home으로 가나요~~");
		model.addAttribute("activeMenu", "home");
		return "index";
	}
	
	// 스크립트 메뉴
	@GetMapping("scripts")
	public void getScripts(Model model, String siteId) {
		log.info("scripts로 가나요~~");
		model.addAttribute("activeMenu", "scripts");
		model.addAttribute("site", faDao.getList(siteId));
	}
	
	@GetMapping("api/scripts")
	// 없으면 Spring은 View인줄 알고 Mapping에 주소의 html을 찾음
	// @ResponseBody을 쓰면 Spring은 View가 아닌 데이터로 인식하여 HTTP body로 보냄
	@ResponseBody 
	public List<Map<String, Object>> getScriptList(){
		List<ScriptsVO> list = scDao.getOpenScripts();
		
		return list.stream().map(vo -> Map.of(
				  "id", vo.getSiteId()
				, "name", vo.getName()
				, "description", vo.getDescription()
				, "category", vo.getCategory()
				, "difficulty", vo.getDifficulty()
				, "url", vo.getUrl()
				, "isPopular", "Y".equals(vo.getPopular())
				, "features", List.of(
						  vo.getFeature()
						, vo.getFeature2()
						, vo.getFeature3()
						, vo.getFeature4()				
				).stream().filter(f -> f != null && !f.isBlank()).toList()
		)).toList();
	}
	
	// 사용 가이드 메뉴
	@GetMapping("guide")
	public void getGuide(Model model) {
		log.info("guide로 가나요~~");
		model.addAttribute("activeMenu", "guide");
	}
	
	// 내 스크립트 메뉴
	@GetMapping("favorites")
	public void getFavorites(Model model, HttpSession session) {
		log.info("favorites로 가나요~~");
		String userId = (String) session.getAttribute("loginId");
		model.addAttribute("activeMenu", "favorites");
		model.addAttribute("favorites", faDao.getList(userId));
	}
	
	@PostMapping("favoriteAdd")
	@ResponseBody
	public ResponseEntity<?> addFavorite(
	        @RequestParam String siteId,
	        HttpSession session) {

	    String userId = (String) session.getAttribute("loginId");

	    if (userId == null) {
	        return ResponseEntity.status(401).body("LOGIN_REQUIRED");
	    }

	    try {
	        faDao.addFavorite(userId, siteId);
	        return ResponseEntity.ok("OK");
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(409).body("ALREADY");
	    }
	}
	
	@PostMapping("favoriteRemove")
	@ResponseBody
	public ResponseEntity<?> removeFavorite(
	        @RequestParam String siteId,
	        HttpSession session) {

	    String userId = (String) session.getAttribute("loginId");

	    if (userId == null) {
	        return ResponseEntity.status(401).body("LOGIN_REQUIRED");
	    }

	    int result = faDao.removeFavorite(userId, siteId);

	    if (result == 0) {
	        return ResponseEntity.status(404).body("NOT_FOUND");
	    }

	    return ResponseEntity.ok("REMOVED");
	}

	
	// 로그인 기능
	@GetMapping("login")
	public void getLogin(Model model) {
		log.info("login으로 가나요~");
	}
	
	@PostMapping("login")
	@ResponseBody
	public void postLogin(@RequestBody Map<String, String> param
							, HttpSession session) {
		
		session.setAttribute("loginId", param.get("loginId"));
		session.setAttribute("loginName", param.get("loginName"));
	}
	
	@PostMapping("logout")
	@ResponseBody
	public void logout(HttpServletRequest request,
	                   HttpServletResponse response,
	                   HttpSession session) {

	    log.info("logout sessionId={}", session.getId());

	    // 1. 세션 종료
	    session.invalidate();

	    // 2. JSESSIONID 쿠키 제거 (중요)
	    Cookie cookie = new Cookie("JSESSIONID", null);
	    cookie.setPath("/");
	    cookie.setMaxAge(0);
	    response.addCookie(cookie);
	}
	
	// 네이버 로그인
	// login에 callbackUrl이 naverCallback로 보내고 controller에서 받는 느낌 
	@GetMapping("naverCallback")
    public void naverCallback() {
		log.info("naverCallback으로 가나요~");
    }
	
	// 커뮤니티 CRUD
	// 좋아요 관련
	@PostMapping("community/like")
	@ResponseBody
	public int likeCommunity(@RequestParam Long postId) {
	    log.info("좋아요 클릭: postId={}", postId);
	    return dao.likePost(postId);
	}
	
	// list
	@GetMapping("community")
	public void getCommunity(Model model, Criteria cri) {
		log.info("community로 가나요~~");
		// model.addAttribute("list", dao.getList());
		// 페이징 처리
		model.addAttribute("list", dao.getListWithPaging(cri));
		model.addAttribute("activeMenu", "community");
		
		int total = dao.getTotalCount(cri);
		model.addAttribute("paging", new PageDTO(cri, total));
		model.addAttribute("criteria", cri);
	}
	
	// insert
	@GetMapping("comRegister")
	public String getComRegister(HttpSession session) {
		log.info("getComRegister로 가나요~~");
		 String loginId = (String) session.getAttribute("loginId");

	    if (loginId == null) {
	        return "redirect:/boardEx/login";
	    }

	    return "boardEx/comRegister";
	}
	
	@PostMapping("comRegister")
	public String postComRegister(CommunityVO vo
								, HttpSession session
								, RedirectAttributes ra) {
		log.info("postComRegister로 가나요~~");
		log.info("comRegister sessionId={}", session.getId());
		log.info("loginId={}", session.getAttribute("loginId"));
		
		String loginId = (String) session.getAttribute("loginId");
		String loginName = (String) session.getAttribute("loginName");
		
		if (loginId == null) {
			return "redirect:/boardEx/login";
		}
		
		vo.setAuthor(loginName);
		
		int result = dao.register(vo);
		String msg;
		
		if(result > 0) {
			msg = vo.getPostId() + "번 글 등록완료 하였습니다.";
		}else {
			msg = "글 등록 실패 하였습니다.";
		}
		
		ra.addFlashAttribute("msg", msg);
		return "redirect:/boardEx/community";
	}
	
	@GetMapping("comRemove")
	public String getRemove(Long postId, RedirectAttributes ra) {
		log.info("getRemove로 가나요~~");
		int msg = dao.remove(postId);
		
		if (msg > 0) {
			ra.addFlashAttribute("msg", postId + "번 글 삭제 했습니다.");
		}else {
			ra.addFlashAttribute("msg", "글 삭제 실패 했습니다.");
		}
		return "redirect:/boardEx/community";
	}
	
	@PostMapping("comRemove") 
	public String postRemove( Long postId, RedirectAttributes ra) {
		log.info("postRemove~~");
		int msg = dao.remove(postId);
		if (msg > 0) {
			ra.addFlashAttribute("msg", postId + "번 글 삭제완료");
		}
		else {
			ra.addFlashAttribute("msg", "글 삭제 대실패");
		}
		return "redirect:/boardEx/community";
	}
	
	
	@GetMapping("comRead")
	public void getRead(Long postId, Model model, Criteria cri) {
		log.info("getRead가 되나요~~");
		model.addAttribute("vo", dao.read(postId));
		// 댓글기능
		List<CommentsVO> commentList = comDao.getList(postId);
		if (commentList == null) {
		    commentList = new ArrayList<>();
		}
		model.addAttribute("commentList", commentList);
		model.addAttribute("postId", postId);
		model.addAttribute("criteria", cri);
	}
	
	@GetMapping("comModify")
	public void getModify(Long postId, Model model) {
		log.info("getModify가 되나요~~");
		model.addAttribute("vo", dao.read(postId));
	}
	
	@PostMapping("comModify")
	public String postModify(CommunityVO vo, RedirectAttributes ra) {
		log.info("postModify로 가나요~~");
		int result = dao.modify(vo);
		String msg;
		
		if (result > 0) {
			msg = vo.getPostId() + "번 글이 수정 되었습니다.";
		} else {
			msg = "글 수정 실패 하였습니다.";
			return "redirect:/boardEx/comRead";
		}
		
		ra.addFlashAttribute("msg", msg);
		return "redirect:/boardEx/community";
	}
	
	@PostMapping("updateCategory")
	@ResponseBody
	public String updateCategory(
	        @RequestParam Long postId,
	        @RequestParam String category,
	        HttpSession session) {

	    String loginName = (String) session.getAttribute("loginName");

	    if (!"표기훈".equals(loginName) && !"이장욱".equals(loginName)) {
	        return "권한이 없습니다.";
	    }

	    dao.updateCategory(postId, category);
	    return "카테고리가 변경되었습니다.";
	}
	
	@GetMapping("manage/manage")
	public String getManage(Model model
						  , HttpSession session
						  , RedirectAttributes ra
						  , Criteria cri) {
		log.info("getManage로 가나요~~");
		
		cri.setAmount(3);
		// amount나 pageNum이 바뀌면 offset을 재정의 해줘야 한다. 
		cri.setOffset();
		
		String msg = null;
		
		String loginName = (String) session.getAttribute("loginName");
		
		// 관리자 체크
		if (!"표기훈".equals(loginName) && !"이장욱".equals(loginName)) {
			msg = "관리자 로그인이 필요 합니다.";
			log.info("msg?? " + msg);
			ra.addFlashAttribute("msg", msg);
			return "redirect:/boardEx/login";
		}
		
		model.addAttribute("activeMenu", "manage");
//		model.addAttribute("scriptList", scDao.getAllScripts());
		model.addAttribute("scriptList", scDao.getAllScriptsWithPaging(cri));
		
		int total = scDao.getTotalCount(cri);
		
		// 페이지 번호 계산용(1,2,3,4,5)
		model.addAttribute("paging", new PageDTO(cri, total));
		// 현재 상태 값 보관용
		model.addAttribute("criteria", cri);
		
		log.info("pageNum = {}", cri.getPageNum());
		log.info("amount = {}", cri.getAmount());
		
		return "boardEx/manage/manage";
	}
	
	// insert
	@GetMapping("manage/register")
	public String getRegister(HttpSession session) {
		log.info("getRegister로 가나요~~");
		String loginId =  (String) session.getAttribute("loginId");
		String loginName =  (String) session.getAttribute("loginName");
		
		if(loginId == null || !loginName.equals("표기훈") && !loginName.equals("이장욱")) {
			return "redirect:/boardEx/login";
		}
		return "boardEx/manage/register";
	}
	
	@PostMapping("manage/register")
	public String postRegister(ScriptsVO vo
							, RedirectAttributes ra
							, HttpSession session) {
		
		log.info("postRegister로 가나요~~");
		String loginId = (String) session.getAttribute("loginId");
		String loginName = (String) session.getAttribute("loginName");
		
		if (loginId == null || !loginName.equals("표기훈") && !loginName.equals("이장욱")) {
			return "redirect:/boardEx/login";
		}
		
		vo.setManager(loginName);
		
		int result = scDao.register(vo);
		String msg;
		
		if(result > 0) {
			msg = "스크립트 등록완료 하였습니다.";
		}else {
			msg = "글 등록 실패 하였습니다.";
		}
		
		ra.addFlashAttribute("msg", msg);
		
		return "redirect:/boardEx/manage/manage";
	}
	
	@GetMapping("manage/modify")
	public void getModify(String siteId, Model model) {
		log.info("getModify로 가나요~");
		model.addAttribute("vo", scDao.read(siteId));
	}
	
	@PostMapping("manage/modify")
	public String postManageModify(ScriptsVO vo, RedirectAttributes ra) {
		log.info("postManageModify로 가나요~~");
		int result = scDao.modify(vo);
		String msg;
		
		if (result > 0) {
			msg = vo.getSiteId() + "의 스크립트가 수정 되었습니다.";
		} else {
			msg = "글 수정 실패 하였습니다.";
			return "redirect:/boardEx/manage/modify";
		}
		
		ra.addFlashAttribute("msg", msg);
		return "redirect:/boardEx/manage/manage";
	}
	
	@GetMapping("manage/remove")
	public String getManageRemove(String siteId, RedirectAttributes ra) {
		log.info("getManageRemove로 가나요~~");
		int result = scDao.remove(siteId);
		String msg;
		
		if (result > 0) {
			ra.addFlashAttribute("msg", siteId + "의 스크립트를 삭제 했습니다.");
		}else {
			ra.addFlashAttribute("msg", "글 삭제 실패 했습니다.");
		}
		return "redirect:/boardEx/manage/manage";
	}
	
	@PostMapping("manage/remove") 
	public String postManageRemove( String siteId, RedirectAttributes ra) {
		log.info("postManageRemove~~");
		int result = scDao.remove(siteId);
		String msg;
		
		if (result > 0) {
			msg = "해당 스크립트가 삭제 되었습니다.";
		} else {
			msg = "글 삭제 실패 하였습니다.";
			return "redirect:/boardEx/manage/manage";
		}
		
		ra.addFlashAttribute("msg", msg);
		return "redirect:/boardEx/manage/manage";
	}
	
	@GetMapping("prepare")
	public String prepare(@RequestParam String siteId, Model model) {
		model.addAttribute("siteId", siteId);
		return "boardEx/prepare";
	}
	
	@PostMapping("scripts/use")
	@ResponseBody
	public void increaseUseCount(@RequestParam String siteId) {
		
		ScriptsVO script = scDao.read(siteId);
		// 나중에 사이트 개발되면 삭제
		if (!"srt".equals(siteId)) {
			return;
		}
		
		scDao.increaseUseCount(siteId);
	}
}
