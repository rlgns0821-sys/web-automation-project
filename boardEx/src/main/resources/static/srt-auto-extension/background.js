// background.js
console.log("🟢 background.js 로드됨");

chrome.runtime.onMessage.addListener((msg, sender) => {
  console.log("📩 background 메시지 수신:", msg);

  // 🔥 객체/문자열 전부 허용 (응급 복구)
  if (msg === "OPEN_SRT" || msg?.type === "OPEN_SRT") {
    console.log("🚀 SRT 탭 강제 오픈");

    chrome.tabs.create({
      url: "https://etk.srail.kr/main.do",
      active: true
    });
  }
});
