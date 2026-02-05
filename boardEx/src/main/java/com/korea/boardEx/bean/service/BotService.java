package com.korea.boardEx.bean.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.korea.boardEx.bean.vo.ConcertDTO;

@Service
public class BotService {

    // 1. 목록 수집 실행 (로그 통합 버전)
    public List<ConcertDTO> getConcertList() {
        List<ConcertDTO> list = new ArrayList<>();
        
        try {
            String projectPath = System.getProperty("user.dir");
            File scriptDir = new File(projectPath, "bots"); 
            
            System.out.println("📂 봇 실행 경로: " + scriptDir.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder("node", "get_list.js");
            pb.directory(scriptDir);
            
            // [핵심] 에러 로그와 일반 로그를 하나로 합침 (끊김 방지)
            pb.redirectErrorStream(true);
            
            Process process = pb.start();

            // 통합된 로그 읽기
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );
            
            StringBuilder jsonOutput = new StringBuilder();
            String line;
            boolean isJsonStarted = false;

            while ((line = reader.readLine()) != null) {
                // JSON 데이터가 시작되면(대괄호 [ 로 시작) 저장 시작
                if (line.trim().startsWith("[")) {
                    jsonOutput.append(line);
                    isJsonStarted = true;
                } else if (isJsonStarted) {
                    // JSON이 여러 줄일 경우 계속 이어 붙임
                    jsonOutput.append(line);
                } else {
                    // JSON이 아니면 그냥 로그로 출력
                    System.out.println("🤖 NodeLog: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("⚠️ 프로세스 비정상 종료 (코드: " + exitCode + ")");
            }

            // JSON 파싱
            String finalJson = jsonOutput.toString();
            if (finalJson.length() > 0) {
                System.out.println("✅ 수신된 JSON 데이터: " + finalJson);
                ObjectMapper mapper = new ObjectMapper();
                list = mapper.readValue(finalJson, new TypeReference<List<ConcertDTO>>(){});
            } else {
                System.out.println("⚠️ 빈 데이터 수신 (화면 로딩 실패 또는 상품 없음)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    // 2. 예매 봇 실행
    public void startBooking(String goodsId, int dayIndex, int roundIndex) {
        new Thread(() -> { 
            try {
                String projectPath = System.getProperty("user.dir");
                File scriptDir = new File(projectPath, "bots");

                System.out.println("🚀 예매 봇 시작: ID " + goodsId);

                ProcessBuilder pb = new ProcessBuilder(
                    "node", "main.js", goodsId, String.valueOf(dayIndex), String.valueOf(roundIndex)
                );
                pb.directory(scriptDir);
                pb.redirectErrorStream(true); // 로그 통합
                
                Process process = pb.start();
                
                // 봇의 로그를 이클립스 콘솔에 실시간 출력
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("🎫 [예매봇] " + line);
                }
                
                process.waitFor(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}