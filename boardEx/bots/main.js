const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
const Tesseract = require('tesseract.js');
const { Jimp } = require('jimp'); 
const fs = require('fs');

puppeteer.use(StealthPlugin());

const wait = (ms) => new Promise(res => setTimeout(res, ms));

const args = process.argv.slice(2);
if (args.length < 3) {
    console.error("❌ 오류: 파라미터가 부족합니다.");
    process.exit(1);
}

const PATH_ID = args[0]; 
const PRODUCT_URL = `https://nol.yanolja.com/ticket/places/${PATH_ID}`;
const TARGET_DAY_INDEX = parseInt(args[1]); 
const TARGET_ROUND_INDEX = parseInt(args[2]); 

(async () => {
  try {
    const browser = await puppeteer.launch({
      headless: false,
      userDataDir: './user_data_bot',
      executablePath: "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
      args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1280,1024', '--disable-popup-blocking']
    });

    const page = (await browser.pages())[0];
    await page.setViewport({ width: 1280, height: 1024 });

    console.log("🚀 페이지 접속 중...");
    await page.goto(PRODUCT_URL, { waitUntil: 'networkidle2' });

    // 1. 로그인 확인
    let isLoggedIn = false;
    try { isLoggedIn = await page.evaluate(() => document.body.innerText.includes('로그아웃')); } catch(e) {}
    if (!isLoggedIn) {
        console.log("🤖 로그인 필요! 로그인 대기...");
        await page.goto('https://ticket.interpark.com/Gate/TPLogin.asp', { waitUntil: 'domcontentloaded' });
        while (true) {
            await wait(1000);
            if (await page.evaluate(() => document.body.innerText.includes('로그아웃'))) break;
        }
        await page.goto(PRODUCT_URL, { waitUntil: 'networkidle2' });
    }

    // 2. 예매/날짜선택 모달 열기 (강력하게 시도)
    console.log("⚡ 예매 모달 활성화 시도...");
    await page.evaluate(() => {
        const btns = Array.from(document.querySelectorAll('button, a'));
        // '예매하기' 또는 '날짜' 글자가 들어간 버튼 찾기
        const target = btns.find(b => b.innerText.includes('예매') || b.innerText.includes('날짜'));
        if (target) target.click();
    });
    
    // 3. 달력 요소 대기 (대기 시간 연장 및 유연한 선택자)
    console.log("📅 달력 로딩 대기 중...");
    try {
        // .react-calendar__tile 외에도 달력 전체 컨테이너를 먼저 확인
        await page.waitForSelector('.react-calendar, .react-calendar__tile', { timeout: 10000 });
        console.log("✅ 달력 포착!");
    } catch (e) {
        console.log("⚠️ 달력 대기 타임아웃. 화면을 강제로 확인합니다.");
    }
    await wait(1000); // 애니메이션 안정화 대기

    // 4. 날짜 및 회차 선택
    console.log("📅 날짜 선택 실행...");
    await page.evaluate((dayIdx, roundIdx) => {
        // 날짜 클릭
        const dates = Array.from(document.querySelectorAll('.react-calendar__tile:not(:disabled)'));
        if (dates[dayIdx]) dates[dayIdx].click();
        else if (dates[0]) dates[0].click();
        
        // 시간 클릭 (시간차를 두고 실행)
        setTimeout(() => {
            const timeBtns = Array.from(document.querySelectorAll('button:has(time), button time'))
                                  .map(el => el.closest('button'))
                                  .filter(b => b && !b.disabled);
            if (timeBtns[roundIdx]) timeBtns[roundIdx].click();
            else if (timeBtns[0]) timeBtns[0].click();
        }, 500);
    }, TARGET_DAY_INDEX, TARGET_ROUND_INDEX);
    
    await wait(1000);

    // 5. 최종 '예매하기' 클릭
    console.log("🖱️ 최종 예매하기 버튼 클릭...");
    const finalBtn = '[data-testid="modal-booking-button"]';
    try {
        await page.waitForSelector(finalBtn, { timeout: 5000 });
        await page.click(finalBtn);
    } catch(e) {
        await page.evaluate(() => {
            const btn = Array.from(document.querySelectorAll('button')).find(b => b.innerText.trim() === '예매하기');
            if (btn) btn.click();
        });
    }

    // 6. 안내 사항(Gate) 돌파 및 팝업 감지
    console.log("⏳ 팝업/안내 페이지 감시 시작...");
    let popupPage = null;
    const startMonitor = Date.now();
    
    while (!popupPage && (Date.now() - startMonitor < 20000)) {
        const targets = await browser.targets();
        for (const t of targets) {
            const url = t.url();
            if (url.includes('interpark.com') && (url.includes('gate') || url.includes('onestop') || url.includes('seat'))) {
                popupPage = await t.page();
                break;
            }
        }
        if (!popupPage) await wait(500);
    }

    if (popupPage) {
        console.log("🎉 예매 창 진입!");
        await popupPage.setViewport({ width: 1000, height: 1000 });

        // 안내사항 강제 통과
        if (popupPage.url().includes('gates/ticket')) {
            console.log("⚠️ 안내 사항(Gate) 감지 -> '확인' 클릭");
            await wait(1500);
            await popupPage.evaluate(() => {
                const btn = Array.from(document.querySelectorAll('button, a, span'))
                                 .find(el => el.innerText.includes('확인') || el.innerText.includes('동의'));
                if (btn) btn.click();
            });
        }

        // 이후 캡챠 및 좌석 로직...
        // (기존 캡챠 로직 생략 - 필요시 위 답변의 캡챠 로직 유지)
        console.log("🔍 캡챠 로직 가동...");
    }

  } catch (e) {
    console.error("❌ 에러 발생:", e.message);
    // 에러 발생 시 현재 화면 캡처하여 원인 파악
    try { await page.screenshot({ path: 'error_screenshot.png' }); } catch(err) {}
  }
})();