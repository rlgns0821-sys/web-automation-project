console.log("🚄 srt-content.js 실행", location.href);

(() => {
  "use strict";
  
  let SRT_CONFIG = null;

  /*********************************************************
   * 역 코드
   *********************************************************/
  const STATION_CODE = {
    "수서": "0551",
    "동탄": "0552",
    "평택지제": "0553",
    "천안아산": "0502",
    "오송": "0297",
    "대전": "0010",
    "공주": "0514",
    "익산": "0030",
    "정읍": "0033",
    "광주송정": "0036",
    "나주": "0037",
    "목포": "0041",
    "김천구미": "0507",
    "동대구": "0015",
    "경주": "0508",
    "울산": "0509",
    "부산": "0020"
  };

  const STEP_KEY  = "__SRT_STEP__";
  const INIT_KEY  = "__SRT_INIT__";
  const ALERT_KEY = "__SRT_ALERT__";

  const sleep = (ms) => new Promise(r => setTimeout(r, ms));

  /*********************************************************
   * 상태 판별
   *********************************************************/
  function isBlockedPage() {
    return document.body.innerText.includes("이용에 불편을 드려 죄송합니다");
  }

  function hasSearchForm() {
    return document.querySelector("form#search-form");
  }

  function hasResultRows() {
    return document.querySelectorAll(".tbl_wrap table tbody tr").length > 0;
  }

  function isLoggedInEnough() {
    if (isBlockedPage()) return false;
    if (hasSearchForm()) return true;
    if (hasResultRows()) return true;
    return false;
  }

  /*********************************************************
   * 조회 버튼 클릭 (submit / JS 자동 대응)
   *********************************************************/
  function clickSearchButton() {
    // submit 기반
    const submitBtn = document.querySelector("input.inquery_btn");
    if (submitBtn) {
      console.log("🟢 submit 기반 조회 버튼 클릭");
      submitBtn.dispatchEvent(new MouseEvent("click", { bubbles: true }));
      return true;
    }

    // JS 함수 기반
    const jsBtn = document.querySelector(
      "button[onclick*='selectScheduleList']"
    );
    if (jsBtn) {
      console.log("🟢 JS 기반 조회 버튼 클릭");
      jsBtn.dispatchEvent(new MouseEvent("click", { bubbles: true }));
      return true;
    }

    console.warn("❌ 조회 버튼을 찾지 못함");
    return false;
  }

  /*********************************************************
   * main.do → 검색 조건 세팅
   *********************************************************/
  function handleMainPage(cfg) {
    console.log("⏳ main.do 검색폼 대기");

    const timer = setInterval(async () => {
      const form = document.querySelector("form#search-form");
      if (!form) return;

      const dpt = form.querySelector("#dptRsStnCd");
      const arv = form.querySelector("#arvRsStnCd");
      const cal = form.querySelector("#cal, #dptDt");
      const tm  = form.querySelector("#dptTm");

      if (!dpt || !arv || !cal || !tm) return;

      clearInterval(timer);
      console.log("✅ 검색폼 감지 완료");

      const dptCode = STATION_CODE[cfg.dptName];
      const arvCode = STATION_CODE[cfg.arvName];

      if (!dptCode || !arvCode) {
        alert("❌ 역 이름 매핑 실패");
        return;
      }

      dpt.value = dptCode;
      arv.value = arvCode;
      cal.value = cfg.dateYmd || cfg.dateDot;
      tm.value  = cfg.startTime;

      ["change", "input"].forEach(ev => {
        dpt.dispatchEvent(new Event(ev, { bubbles: true }));
        arv.dispatchEvent(new Event(ev, { bubbles: true }));
        cal.dispatchEvent(new Event(ev, { bubbles: true }));
        tm.dispatchEvent(new Event(ev, { bubbles: true }));
      });

      sessionStorage.setItem(STEP_KEY, "SEARCH");

      await sleep(400 + Math.random() * 400);
      clickSearchButton();
    }, 300);
  }

  /*********************************************************
   * 조회 결과 → 좌석 감시
   *********************************************************/
  function handleResultPage(cfg) {
    console.log("⏳ 결과 테이블 대기");

    const wait = setInterval(() => {
      const rows = document.querySelectorAll(".tbl_wrap table tbody tr");
      if (rows.length === 0) return;

      clearInterval(wait);
      console.log("✅ 결과 테이블 감지", rows.length);

      watchRows(cfg);
    }, 400);
  }

  async function watchRows(cfg) {
    console.log("⏰ 좌석 감시 시작");

    const timer = setInterval(async () => {
      if (isBlockedPage()) {
        console.warn("⛔ 차단 페이지 감지 → 중단");
        sessionStorage.clear();
        clearInterval(timer);
        return;
      }

      const rows = document.querySelectorAll(".tbl_wrap table tbody tr");

      for (const tr of rows) {
        if (!tr.innerText.includes("SRT")) continue;

        const m = tr.innerText.match(/(\d{2}):(\d{2})/);
        if (!m) continue;

        const hhmmss = m[1] + m[2] + "00";
        if (hhmmss < cfg.startTime) continue;

        const reserveBtn =
          // 일반실 우선
          [...tr.querySelectorAll("a")].find(a =>
            a.getAttribute("onclick")?.includes("reservationAfterMsg") &&
            a.getAttribute("aria-label")?.includes("일반실")
          )
          // 특실 차선
          || [...tr.querySelectorAll("a")].find(a =>
            a.getAttribute("onclick")?.includes("reservationAfterMsg") &&
            a.getAttribute("aria-label")?.includes("특실")
          );

        if (!reserveBtn) continue;

        console.log("🎯 좌석 발견", hhmmss);
        clearInterval(timer);
        sessionStorage.setItem(STEP_KEY, "RESERVED");

        await sleep(300 + Math.random() * 500);
        reserveBtn.dispatchEvent(
          new MouseEvent("click", { bubbles: true })
        );
        return;
      }

      console.log("🔄 좌석 없음 → 재조회");
      await sleep(700 + Math.random() * 700);
      clickSearchButton();
    }, cfg.refreshInterval || 1500);
  }

  /*********************************************************
   * 예약 완료 → 결제 전 알림
   *********************************************************/
  function handleReservedPage(cfg) {
	console.log("cfg : ", cfg);
    const timer = setInterval(() => {
      const payBtn = [...document.querySelectorAll("a")]
        .find(a => a.textContent.includes("결제"));

      if (!payBtn) return;

      clearInterval(timer);
	  
      // 🔒 중복 문자 방지
      if (sessionStorage.getItem(ALERT_KEY)) return;
      sessionStorage.setItem(ALERT_KEY, "Y");

      // ✅ 여기서만 서버 알림 http://localhost:10001/boardEx/autoScripts/api/srt/notify 
	  fetch("http://localhost:10001/boardEx/autoScripts/api/srt/notify", {
	    method: "POST",
	    headers: { "Content-Type": "application/json" },
	    body: JSON.stringify({
	      loginId: cfg.loginId, 
	      message: document.title
	    })
	  });


      alert(
        "✅ 좌석 확보 완료!\n\n" +
        "10분 이내 결제하지 않으면\n예약이 취소됩니다."
      );
    }, 500);
  }


  /*********************************************************
   * 메인 흐름
   *********************************************************/
  async function run(cfg) {
    const url  = location.href;
    const step = sessionStorage.getItem(STEP_KEY);

    console.log("🚄 RUN", { url, step });

    if (!isLoggedInEnough()) {
      console.warn("⛔ 로그인 안 됨 / 판단 불가 → 대기");
      return;
    }

    if (url.includes("/main.do") && !step) {
      handleMainPage(cfg);
      return;
    }

    if (url.includes("selectScheduleList.do") && step === "SEARCH") {
      handleResultPage(cfg);
      return;
    }

    if (url.includes("confirmReservationInfo.do") && step === "RESERVED") {
      handleReservedPage(cfg);
      return;
    }
  }

  /*********************************************************
   * 시작점
   *********************************************************/
  (async () => {
    console.log("📦 srt-content.js 로드됨");

    const { SRT_CONFIG } = await chrome.storage.local.get("SRT_CONFIG");
	console.log("📦 불러온 SRT_CONFIG =", SRT_CONFIG);
    if (!SRT_CONFIG) {
      console.warn("❌ SRT_CONFIG 없음");
      return;
    }

    if (!sessionStorage.getItem(INIT_KEY)) {
      sessionStorage.clear();
      sessionStorage.setItem(INIT_KEY, "Y");
    }

    run(SRT_CONFIG);
  })();
  
})();
