// get_list.js
const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
puppeteer.use(StealthPlugin());

const TARGET_URL = 'https://nol.yanolja.com/entertainment/list?filter=entertainmentCategoryCodes%3DENTERTAINMENT_CATEGORY_CONCERT&sort=SOLD_COUNT_DESC';

(async () => {
    const browser = await puppeteer.launch({ 
        headless: "new", // 목록 수집은 화면 안 보여도 됨
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    const page = await browser.newPage();
    
    // 페이지 접속
    await page.goto(TARGET_URL, { waitUntil: 'networkidle2' });

    // 데이터 추출
    const concertList = await page.evaluate(() => {
        // NOL 사이트의 리스트 아이템 선택자 (HTML 구조 기반)
        const items = document.querySelectorAll('a[href*="/entertainment/goods/"]');
        const results = [];

        items.forEach(item => {
            try {
                const link = item.href; // 예: .../goods/26000664
                const id = link.split('/goods/')[1];
                
                // 제목 찾기 (클래스명이 해시값이므로 태그 구조로 탐색)
                // 보통 a 태그 안에 h3나 div로 제목이 있음
                const titleEl = item.querySelector('h3') || item.querySelector('div[class*="Title"]');
                const title = titleEl ? titleEl.innerText : "제목 없음";

                // 날짜 찾기
                const dateEl = item.innerText.match(/\d{4}\.\d{2}\.\d{2}/);
                const date = dateEl ? dateEl[0] : "";

                // 이미지
                const imgEl = item.querySelector('img');
                const image = imgEl ? imgEl.src : "";

                results.push({
                    id: id,
                    title: title,
                    date: date,
                    image: image,
                    link: link
                });
            } catch(e) {}
        });
        return results;
    });

    // JSON 형태로 출력 (Spring이 이걸 읽음)
    console.log(JSON.stringify(concertList));

    await browser.close();
})();