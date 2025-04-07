# IOgame
io게임 만들기

  document.getElementById('close-left-ad').addEventListener('click', function(event) {
    event.stopPropagation();  // 링크 클릭 이벤트 방지
    document.getElementById('left-ad').style.display = 'none';
});

// 오른쪽 광고 X 버튼 클릭 시 숨기기
document.getElementById('close-right-ad').addEventListener('click', function(event) {
    event.stopPropagation();  // 링크 클릭 이벤트 방지
    document.getElementById('right-ad').style.display = 'none';
});
