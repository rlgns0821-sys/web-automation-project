console.log("✅ site-content.js 로드됨 (API 모드)");

const $ = (id) => document.getElementById(id);

$("autoBtn")?.addEventListener("click", async () => {
  // 1. 입력된 값 가져오기
  const dpt = $("dpt").value.trim();
  const arv = $("arv").value.trim();
  const date = $("date").value; // YYYY-MM-DD
  const time = $("start").value; // HH:MM
  
  // 로그인 정보는 세션이나 입력창에서 가져와야 함 (예시)
  const loginId = sessionStorage.getItem("loginId") || prompt("회원번호를 입력하세요");
  const loginPw = sessionStorage.getItem("loginPw") || prompt("비밀번호를 입력하세요");

  if (!loginId || !loginPw) {
      alert("로그인 정보가 필요합니다.");
      return;
  }

  // 2. 보낼 데이터 포장
  const payload = {
    loginId: loginId,
    loginPw: loginPw,
    dpt: dpt,
    arv: arv,
    date: date,
    time: time
  };

  console.log("🚀 봇 서버로 전송:", payload);

  try {
      // 3. Node.js 서버(10001포트)로 전송!
      const response = await fetch("http://localhost:10001/api/start", {
          method: "POST",
          headers: {
              "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
      });

      const result = await response.json();

      if (result.status === 'started') {
          alert("✅ 봇이 실행되었습니다!\n서버 컴퓨터의 크롬창을 확인하세요.");
      } else {
          alert("❌ 실행 실패: " + result.message);
      }

  } catch (error) {
      console.error(error);
      alert("❌ 봇 서버(Node.js)가 켜져 있는지 확인해주세요!\n(에러: " + error.message + ")");
  }
});