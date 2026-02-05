package com.korea.boardEx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.korea.boardEx.bean.service.BotService;
import com.korea.boardEx.bean.vo.ConcertDTO;

@Controller
@RequestMapping("/boardEx/autoScripts/*")
public class ConcertController {

    @Autowired
    private BotService botService;

    // 1. 목록 페이지 보여주기
    @GetMapping("concertList")
    public String viewConcerts(Model model) {
        List<ConcertDTO> concerts = botService.getConcertList();
        model.addAttribute("concerts", concerts);
        return "boardEx/autoScripts/concertList"; // HTML 파일명
    }

    // 2. 사용자가 '예매 시작' 버튼을 눌렀을 때
    @PostMapping("start-bot")
    @ResponseBody
    public String runBot(@RequestParam String id, @RequestParam int day, @RequestParam int round) {
        System.out.println("봇 요청 받음: " + id + ", " + day + ", " + round);
        
        botService.startBooking(id, day, round);
        
        return "봇이 백그라운드에서 실행되었습니다! 브라우저를 확인하세요.";
    }
}