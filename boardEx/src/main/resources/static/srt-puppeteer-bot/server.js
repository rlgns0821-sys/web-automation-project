const express = require('express');
const puppeteer = require('puppeteer');
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();

app.use(cors());
app.use(express.static('public'));
app.use(bodyParser.json());

// SRT 역 코드
const STATION_CODE = {
    "수서": "0551", "동탄": "0552", "평택지제": "0553", "천안아산": "0502", "오송": "0297",
    "대전": "0010", "공주": "0514", "익산": "0030", "정읍": "0033", "광주송정": "0036",
    "나주": "0037", "목포": "0041", "김천구미": "0507", "동대구": "0015", "경주": "0508",
    "울산": "0509", "부산": "0020"
};

app.post('/api/start', async (req, res) => {
    // loginId: 내 사이트 아이디 (알림용)
    // srtId: SRT 회원번호 (로그인용)
    const { loginId, srtId, loginPw, dpt, arv, date, time } = req.body;
    
    console.log('--------------------------------------------------');
    console.log(`🚀 [요청 수신]`);
    console.log(`📨 알림 받을 ID : ${loginId}`);
    console.log(`🚄 SRT 로그인 ID: ${srtId}`); 
    console.log(`📅 여정 : ${dpt} -> ${arv} (${date} ${time} 이후)`);
    console.log('--------------------------------------------------');

    res.json({ status: 'started' });
    
    // 봇 실행 (변수 순서 명확히 전달)
    runMacro(srtId, loginId, loginPw, dpt, arv, date, time);
});

// 함수 인자: (SRT아이디, 알림아이디, 비밀번호, ...)
async function runMacro(srtId, notifyId, loginPw, dptName, arvName, date, time) {
    const browser = await puppeteer.launch({
        headless: false,
        defaultViewport: null,
        args: ['--start-maximized']
    });

    const page = await browser.newPage();
    page.on('dialog', async dialog => { try { await dialog.accept(); } catch(e) {} });

    try {
        // [STEP 1] 로그인
        console.log('🌎 로그인 페이지 접속...');
        await page.goto('https://etk.srail.kr/cmc/01/selectLoginForm.do?pageId=TK0701000000', { waitUntil: 'networkidle2' });
        await page.waitForSelector('#srchDvNm01');

        console.log(`🔑 로그인 시도 (ID: ${srtId})...`); // 여기에 찍히는 값이 입력됩니다.
        
        await page.evaluate((id, pw) => {
            const idInput = document.querySelector('#srchDvNm01');
            const pwInput = document.querySelector('#hmpgPwdCphd01');
            if(idInput) { 
                idInput.value = id; // 여기에 srtId가 들어감
                idInput.dispatchEvent(new Event('input')); 
            }
            if(pwInput) { 
                pwInput.value = pw; 
                pwInput.dispatchEvent(new Event('input')); 
            }
        }, srtId, loginPw); // [중요] evaluate에 srtId를 넘김

        console.log('👆 로그인 버튼 클릭...');
        await Promise.all([
            page.waitForNavigation({ waitUntil: 'domcontentloaded' }),
            page.click('.loginSubmit')
        ]);
        console.log('✅ 로그인 시도 완료');

        // [STEP 2] 예매 전용 페이지 이동
        console.log('🏗️ 예매 전용 페이지로 이동...');
        await page.goto('https://etk.srail.kr/hpg/hra/01/selectScheduleList.do?pageId=TK0101010000', { waitUntil: 'domcontentloaded' });
        await page.waitForSelector('#dptRsStnCd', { timeout: 10000 });

        const dptCode = STATION_CODE[dptName];
        const arvCode = STATION_CODE[arvName];
        const dateStr = date.replace(/-/g, '');
        const hour = parseInt(time.split(':')[0]);
        const safeHour = (Math.floor(hour / 2) * 2).toString().padStart(2, '0') + '0000';
        const targetTimeNum = parseInt(time.replace(':', '') + '00');

        console.log(`📝 조건 설정: ${dptName}->${arvName}, ${safeHour}`);

        await page.evaluate((d, a, dt, tm) => {
            const dptEl = document.querySelector("#dptRsStnCd");
            const arvEl = document.querySelector("#arvRsStnCd");
            const dtEl = document.querySelector("#dptDt");
            const tmEl = document.querySelector("#dptTm");
            if(dptEl) { dptEl.value = d; document.querySelector("#dptRsStnCdNm").value = ""; }
            if(arvEl) { arvEl.value = a; document.querySelector("#arvRsStnCdNm").value = ""; }
            if(dtEl) dtEl.value = dt;
            if(tmEl) tmEl.value = tm;
        }, dptCode, arvCode, dateStr, safeHour);

        console.log('🖱️ "조회하기" 클릭');
        await Promise.all([
            page.waitForNavigation({ waitUntil: 'domcontentloaded' }).catch(()=>{}),
            page.evaluate(() => {
                const links = Array.from(document.querySelectorAll('a, button, input'));
                const btn = links.find(el => el.textContent.includes('조회하기') || el.value?.includes('조회하기'));
                if (btn) btn.click();
            })
        ]);
        await page.waitForSelector('.tbl_wrap', { timeout: 10000 });

        // [STEP 3] 예매 루프
        console.log('🔄 좌석 스캔 시작...');
        let reserved = false;

        while (!reserved) {
            try {
                if (page.isClosed()) break;
                
                const isBlocked = await page.evaluate(() => document.body.innerText.includes("이용에 불편을 드려")).catch(()=>false);
                if (isBlocked) { console.log('⛔ 차단됨.'); break; }

                const rows = await page.$$('.tbl_wrap table tbody tr');
                
                for (const row of rows) {
                    const rowText = await page.evaluate(el => el.innerText, row).catch(() => "ERROR");
                    if (!rowText.includes('SRT')) continue;

                    const timeMatch = rowText.match(/(\d{2}):(\d{2})/);
                    if (!timeMatch) continue;
                    
                    const trainTimeNum = parseInt(timeMatch[1] + timeMatch[2] + "00");
                    if (trainTimeNum < targetTimeNum) continue; 

                    const btn = await row.$('a[aria-label*="일반실"][onclick*="reservationAfterMsg"]') 
                             || await row.$('a[aria-label*="특실"][onclick*="reservationAfterMsg"]');

                    if (btn) {
                        console.log(`🎉 [발견] ${timeMatch[0]} 기차 예매 시도!`);
                        try {
                            await Promise.all([
                                page.waitForNavigation({ timeout: 3000 }),
                                page.evaluate(el => el.click(), btn)
                            ]);
                        } catch (e) {}

                        // 결제 페이지 확인
                        if (page.url().includes('confirmReservationInfo')) {
                            reserved = true;
                            console.log('🏁 결제 페이지 진입 성공! (진짜 성공)');
                            
                            // ==========================================================
                            // [문자 발송] 여기서는 notifyId (내 사이트 아이디) 사용
                            // ==========================================================
                            try {
                                console.log(`📩 문자 요청 보냄 (수신자: ${notifyId})`);
                                await fetch('http://localhost:10001/boardEx/autoScripts/api/srt/notify', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({
                                        loginId: notifyId, // 여기가 핵심!
                                        message: `${date} ${timeMatch[0]} ${dptName}->${arvName} 예매완료!`
                                    })
                                });
                                console.log('✅ 문자 요청 성공');
                            } catch (e) { console.error('❌ 문자 실패:', e.message); }
                            // ==========================================================
                            
                            break; 
                        } else {
                            console.log('❌ 예매 실패 (매진). 다시 찾습니다.');
                            await page.reload();
                            await page.waitForSelector('.tbl_wrap');
                            break; 
                        }
                    }
                }

                if (reserved) break;

            } catch (err) {
                if (err.message.includes('Target closed') || 
                    err.message.includes('detached Frame') || 
                    err.message.includes('Execution context')) {
                    await new Promise(r => setTimeout(r, 500));
                    continue; 
                }
                console.log('⚠️ 에러(무시):', err.message);
            }

            if (!reserved) {
                await new Promise(r => setTimeout(r, 1000 + Math.random() * 1000));
                try {
                    await Promise.all([
                        page.waitForNavigation({ waitUntil: 'domcontentloaded', timeout: 5000 }).catch(() => {}),
                        page.evaluate(() => {
                            const links = Array.from(document.querySelectorAll('input, a, button'));
                            const btn = links.find(el => el.value === '조회하기' || el.textContent.includes('조회하기'));
                            if (btn) btn.click(); else location.reload();
                        })
                    ]);
                    await page.waitForSelector('.tbl_wrap', { timeout: 5000 });
                } catch(e) {
                    try { await page.reload(); await page.waitForSelector('.tbl_wrap'); } catch(z) {}
                }
            }
        }

    } catch (error) {
        console.error('⚠️ 봇 전체 에러:', error.message);
    }
}

app.listen(3000, () => {
    console.log('🚄 SRT 봇 서버 가동 (포트: 3000)');
});