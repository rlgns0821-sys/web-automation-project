let isRunning = false;

// 1. Puppeteer의 page.waitForSelector 기능 구현
function waitForSelector(selector, timeout = 5000) {
    return new Promise((resolve, reject) => {
        const element = document.querySelector(selector);
        if (element) return resolve(element);

        const observer = new MutationObserver((mutations) => {
            const target = document.querySelector(selector);
            if (target) {
                resolve(target);
                observer.disconnect();
            }
        });

        observer.observe(document.body, { childList: true, subtree: true });

        // 타임아웃 설정
        setTimeout(() => {
            observer.disconnect();
            reject(new Error(`Timeout waiting for selector: ${selector}`));
        }, timeout);
    });
}

// 2. 딜레이 함수 (page.waitForTimeout 대체)
const delay = (ms) => new Promise(res => setTimeout(res, ms));

// 3. 실제 자동화 로직 (여기에 원하는 시나리오 작성)
async function runAutomation() {
    if (!isRunning) return;
    console.log("🚀 자동화 시작");

    try {
        // [시나리오 예시]
        // 1단계: 특정 버튼 기다리기
        console.log("버튼 찾는 중...");
        const btn = await waitForSelector('.target-button-class'); // 여기에 실제 선택자 입력
        
        // 2단계: 클릭
        btn.click();
        console.log("버튼 클릭 완료!");

        // 3단계: 팝업 처리 대기
        await delay(1000); 

        // 4단계: 다음 작업...
        
    } catch (e) {
        console.error("에러 발생:", e);
        // 실패 시 새로고침 로직 등을 여기에 추가
        // location.reload(); 
    }
}

// 4. 팝업에서 보내는 메시지 수신
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === "START") {
        isRunning = true;
        runAutomation();
    } else if (request.action === "STOP") {
        isRunning = false;
        console.log("🛑 자동화 중지");
    }
});