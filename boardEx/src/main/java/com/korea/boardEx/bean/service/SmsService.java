package com.korea.boardEx.bean.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Slf4j
@Service
public class SmsService {

	private final DefaultMessageService messageService;

    public SmsService() {
        this.messageService =
            NurigoApp.INSTANCE.initialize(
                "NCSBB7WVOSBFSZJE",
                "VJAIOY61COWAYF4LVXDRZO6ZISGBB2J2",
                "https://api.coolsms.co.kr"
            );
    }

    public void send(String to, String text) {

        Message message = new Message();
        message.setFrom("01099130783"); // CoolSMS에 등록된 번호
        message.setTo(to.replaceAll("-", "")); 
        message.setText(text);

        try {
            messageService.sendOne(
                new SingleMessageSendingRequest(message)
            );
            log.info("실제 SMS 발송 성공 → {}", to);
        } catch (Exception e) {
            log.error("SMS 발송 실패", e);
        }
    }
}
