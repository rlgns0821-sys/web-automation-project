/**
 * 
 * Template Name : Login Page
 * js파일 만드려면 static-> js -> new -> javas 입력 -> .js 파일로 만듬
 * sessionStorage.setItem("loginName", user.name)을 
 * 지정하고 getItem으로 name값 들고옴
 * 
 */

/* 로그인 안바뀌는 현상 무조건 여기부터 */
function applyLoginHeader() {
  const name = sessionStorage.getItem("loginName");
  window.loginId = sessionStorage.getItem("loginId");

  const loginArea = document.getElementById("loginArea");
  const welcomeArea = document.getElementById("welcomeArea");
  const logoutArea = document.getElementById("logoutArea");
  const welcome = document.getElementById("welcome");
  const myPage = document.getElementById("myPage");
  const welcomeText = document.getElementById("welcomeText");
  const myPageMenu = document.getElementById("myPageMenu");
  const manage = document.getElementById("manage");

  // header fragment가 아직 DOM에 없으면 그냥 리턴
  if (!loginArea || !welcomeArea || !logoutArea || !welcome || !myPage || !welcomeText || !myPageMenu || !manage) return;

  if (name) {
    loginArea.style.display = "none";
    welcomeArea.style.display = "block";
    logoutArea.style.display = "block";
	
    myPage.innerText = `${name}`;
	myPage.href = "/boardEx/favorites";
	welcomeText.innerText = " 님 환영합니다👋";
	
	// 관리자 전용 메뉴
	if(name === "표기훈" || name === "이장욱"){
		manage.style.display = "block";
		manage.href = "/boardEx/manage/manage";
	} else {
		manage.style.display = "none";
	}
	
	// ${name}을 클릭 시 '내 스크립트' 메뉴가 보이게 함
	if(location.pathname === "/boardEx/favorites"){
		myPageMenu.style.display = "block";
	} else {
		myPageMenu.style.display = "none";
	}		

  } else {
    // 로그인 안 된 상태 UI도 확실히 세팅
    loginArea.style.display = "block";
    welcomeArea.style.display = "none";
    logoutArea.style.display = "none";
    myPageMenu.style.display = "none";
    manage.style.display = "none";
	
    myPage.innerText = "";
	myPage.removeAttribute("href");
    welcomeText.innerText = "";
	
  }
}

function logout() {
	fetch("/boardEx/logout", {
	    method: "POST"
	  }).then(() => {
	    sessionStorage.removeItem("loginName");
	    sessionStorage.removeItem("loginId");
	    location.href = "/boardEx/login";
	  });
}

// header 포함된 뒤 실행되도록 DOMContentLoaded로 감싸기
window.addEventListener("DOMContentLoaded", () => {
  applyLoginHeader();
});

// (선택) 다른 페이지에서 sessionStorage가 바뀌는 경우 실시간 반영하고 싶으면:
window.addEventListener("pageshow", applyLoginHeader);
/*======= 로그인 안바뀌는 현상 무조건 여기부터========= */



