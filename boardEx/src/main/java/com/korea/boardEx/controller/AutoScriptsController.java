package com.korea.boardEx.controller;

import java.io.File;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.korea.boardEx.bean.dao.UserDAO;
import com.korea.boardEx.bean.service.SmsService;
import com.korea.boardEx.bean.vo.UserVO;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/boardEx/autoScripts/*")
public class AutoScriptsController {
    
    @Autowired
    UserDAO userDao;
    
    @Autowired
    SmsService smsService;
    
    // 봇 파일이 있는 실제 경로 (사용자분의 경로에 맞춤)
    // 경로 확인! (사용자분 경로에 맞게 유지)
    private static final String BOT_PATH = "C:\\1900_web_erik\\00_exSpring\\dev\\boardEx\\src\\main\\resources\\static\\srt-puppeteer-bot";
    
    // [수정됨] 봇 서버 주소를 3000번 포트로 변경
    private static final String NODE_URL = "http://localhost:3000/api/start";

    @GetMapping("srt")
    public String srtPage() {
        return "boardEx/autoScripts/srt";
    }
    
    @PostMapping("srt/run")
    @ResponseBody
    public Map<String, Object> runSrtBot(@RequestBody Map<String, Object> param) {
        log.info("봇 실행 요청: {}", param);
        
        try {
            // 1차 시도: 봇 서버에 요청 보내보기
            return sendRequestToNode(param);
            
        } catch (ResourceAccessException e) {
            // 연결 실패 = 봇 서버가 꺼져있음
            log.warn("봇 서버가 꺼져있습니다. 자동으로 실행을 시도합니다...");
            
            // 봇 서버 강제 실행
            boolean isStarted = startNodeServer();
            
            if (isStarted) {
                try {
                    // 서버 켜질 시간(3초) 기다렸다가 다시 요청
                    return sendRequestToNode(param);
                } catch (Exception ex) {
                    return Map.of("result", "FAIL", "msg", "서버 자동 실행 후 재연결 실패: " + ex.getMessage());
                }
            } else {
                return Map.of("result", "FAIL", "msg", "봇 서버를 자동으로 켤 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("에러 발생", e);
            return Map.of("result", "FAIL", "msg", "에러: " + e.getMessage());
        }
    }

    // Node.js로 요청 보내는 함수 (분리됨)
    private Map<String, Object> sendRequestToNode(Map<String, Object> param) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(param, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(NODE_URL, entity, String.class);
        log.info("✅ Node.js 응답: {}", response.getBody());
        return Map.of("result", "OK", "msg", "봇이 정상적으로 실행되었습니다.");
    }

    // Node.js 서버를 자동으로 켜주는 함수
    private boolean startNodeServer() {
        try {
            // cmd 창을 최소화(/min) 상태로 띄워서 node server.js 실행
            // 이렇게 해야 자바가 꺼져도 봇 서버는 계속 살아있음
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "/min", "node", "server.js");
            pb.directory(new File(BOT_PATH)); // 실행 위치 설정 (중요)
            pb.start();
            
            log.info("🚀 Node.js 서버 실행 명령을 보냈습니다. (3초 대기 중...)");
            Thread.sleep(3000); // 서버가 켜질 때까지 3초 대기
            return true;
            
        } catch (Exception e) {
            log.error("Node.js 서버 실행 실패", e);
            return false;
        }
    }
    
    @PostMapping("api/srt/notify")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Map<String, Object> notify(@RequestBody Map<String, String> param) {
        String loginId = param.get("loginId"); 
        String trainInfo = param.get("message");
        
        UserVO user = userDao.findByLoginId(loginId);
        
        if (user == null) {
            return Map.of("result", "FAIL", "msg", "User Not Found");
        }
        
        smsService.send(user.getPhone(), "[SRT 자동예매 성공]\n" + trainInfo + "\n10분 내 결제 요망");
        return Map.of("result", "OK");
    }
}