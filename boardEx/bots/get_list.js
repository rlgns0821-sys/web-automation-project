const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
puppeteer.use(StealthPlugin());

const TARGET_URL = 'https://nol.yanolja.com/entertainment/list?filter=entertainmentCategoryCodes%3DENTERTAINMENT_CATEGORY_CONCERT&sort=SOLD_COUNT_DESC';

(async () => {
    try {
        console.log("🚀 [ListBot] 브라우저 실행 중...");
        const browser = await puppeteer.launch({ 
            headless: false, // 테스트용 (잘 되면 'new'로 변경)
            args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1280,900', '--disable-blink-features=AutomationControlled']
        });
        
        const page = await browser.newPage();
        await page.setUserAgent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');
        await page.setViewport({ width: 1280, height: 900 });
        
        console.log("🌐 야놀자 접속 중...");
        await page.goto(TARGET_URL, { waitUntil: 'networkidle2', timeout: 60000 });
        
        console.log("⏳ 상품 리스트 대기 중...");
        try {
            // [수정] 새로운 URL 패턴인 '/products/'가 포함된 링크를 기다림
            await page.waitForSelector('a[href*="/products/"]', { timeout: 10000 });
        } catch (e) {
            console.log("⚠️ 타임아웃: 스크롤을 시도합니다.");
        }

        // 스크롤 다운
        await page.evaluate(async () => {
            await new Promise((resolve) => {
                let totalHeight = 0;
                const distance = 300;
                const timer = setInterval(() => {
                    const scrollHeight = document.body.scrollHeight;
                    window.scrollBy(0, distance);
                    totalHeight += distance;
                    if (totalHeight >= 3000 || totalHeight >= scrollHeight) {
                        clearInterval(timer);
                        resolve();
                    }
                }, 100);
            });
        });
        await new Promise(r => setTimeout(r, 2000));

        console.log("🕵️ 데이터 추출 시작 (새로운 URL 패턴 적용)...");

        const concertList = await page.evaluate(() => {
            const results = [];
            // [수정] '/products/' 가 포함된 모든 a 태그 검색
            const links = document.querySelectorAll('a[href*="/products/"]');
            
            links.forEach(a => {
                const href = a.href; 
                // 예: https://nol.yanolja.com/ticket/places/25001695/products/25018201
                
                if (href && href.includes('/ticket/places/')) {
                    
                    // [핵심] ID를 '장소ID/products/상품ID' 형태로 추출 (예: 25001695/products/25018201)
                    // 이렇게 해야 main.js에서 URL을 다시 조립할 수 있음
                    const urlParts = href.split('/ticket/places/');
                    if (urlParts.length > 1) {
                        const extractedId = urlParts[1]; // "25001695/products/25018201"

                        // 제목 및 날짜 추출 (구조가 바뀔 수 있어 안전하게 텍스트 전체 탐색)
                        const textLines = a.innerText.split('\n').filter(t => t.trim().length > 0);
                        let title = textLines[0] || "제목 없음"; 
                        let date = "날짜 미정";
                        
                        // 날짜 찾기 시도 (202X.XX.XX 형식)
                        const dateLine = textLines.find(t => /\d{4}\.\d{2}\.\d{2}/.test(t));
                        if(dateLine) date = dateLine;

                        // 제목 보정 (날짜가 제목으로 들어가는 것 방지)
                        if (title === date && textLines.length > 1) title = textLines[1];

                        const imgEl = a.querySelector('img');
                        const image = imgEl ? imgEl.src : "https://via.placeholder.com/150";

                        // 중복 방지
                        if (!results.some(r => r.id === extractedId)) {
                            results.push({
                                id: extractedId, // 이제 ID에 슬래시(/)가 포함됨
                                title: title,
                                date: date,
                                image: image,
                                link: href
                            });
                        }
                    }
                }
            });
            return results;
        });

        console.log(`🎉 총 ${concertList.length}개의 공연 발견!`);
        console.log(JSON.stringify(concertList));

        await browser.close();
        
    } catch (error) {
        console.error("🔥 에러: " + error.message);
        console.log("[]");
    }
})();