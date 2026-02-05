package com.korea.boardEx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.korea.boardEx.BoardExApplication;
import com.korea.boardEx.bean.dao.CommentsDAO;
import com.korea.boardEx.bean.vo.CommentsVO;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/boardEx/comments")
public class CommentsController {
	
	private final BoardExApplication boardExApplication;
	
	CommentsController(BoardExApplication boardExApplication) {
        this.boardExApplication = boardExApplication;
    }
	
	@Autowired
	private CommentsDAO comDao;

    // 댓글 등록
    @PostMapping("/insert")
    @ResponseBody
    public String insert(
            @RequestParam Long postId,
            @RequestParam String content,
            HttpSession session) {

        String author = (String) session.getAttribute("loginName");
        if (author == null) {
            return "LOGIN_REQUIRED";
        }

        CommentsVO vo = new CommentsVO();
        vo.setPostId(postId);
        vo.setContent(content);
        vo.setAuthor(author);

        int result = comDao.register(vo);
        return result > 0 ? "등록 완료" : "등록 실패";
    }

    // 댓글 삭제
    @PostMapping("/delete")
    @ResponseBody
    public void delete(@RequestParam Long commentId) {
        comDao.remove(commentId);
    }

    // 댓글 수정
    @PostMapping("/update")
    @ResponseBody
    public void update(
            @RequestParam Long commentId,
            @RequestParam String content) {

        comDao.modify(commentId, content);
    }
}
