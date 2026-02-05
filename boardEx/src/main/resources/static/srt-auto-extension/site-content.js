console.log("✅ site-content.js 로드됨 (내 사이트)");

const $ = (id) => document.getElementById(id);

$("autoBtn")?.addEventListener("click", async () => {
  const cfg = {
    dptName: $("dpt").value.trim(),
    arvName: $("arv").value.trim(),
    dateYmd: $("date").value.replaceAll("-", ""),
    startTime: $("start").value.replace(":", "") + "00",
    endTime: $("end").value.replace(":", "") + "00",
	// 2026.01.22 로그인 정보 추가(메시지용)
	loginId: sessionStorage.getItem("loginId"),
    refreshInterval: Number($("interval").value || 1500),
    autoPay: false
  };

  cfg.dateDot = cfg.dateYmd.replace(
    /(\d{4})(\d{2})(\d{2})/,
    "$1.$2.$3"
  );

  await chrome.storage.local.set({ SRT_CONFIG: cfg });

  console.log("✅ SRT_CONFIG 저장 완료", cfg);
  console.log("✅ 저장할 cfg= ", cfg);

  // 🔥 이 줄 하나로 무조건 트리거
  chrome.runtime.sendMessage("OPEN_SRT");
});
