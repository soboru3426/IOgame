const socket = io();
const gameContainer = document.getElementById('game-container');
const loginContainer = document.getElementById('login-container');
const nicknameInput = document.getElementById('nickname-input');
const classSelect = document.getElementById('class-select');
const startButton = document.getElementById('start-button');
const chatContainer = document.getElementById('chat-container');
const chatMessages = document.getElementById('chat-messages');
const chatInput = document.getElementById('chat-input');

let players = {}; // 모든 플레이어 정보를 저장
let localPlayer; // 현재 플레이어의 DOM 요소

// 플레이어 데이터를 객체로 설정
let playerData = {
    id: null,
    x: Math.random() * window.innerWidth,
    y: Math.random() * window.innerHeight,
    speed: 3,
    nickname: '',
    class: '',
    score: 0,
    hp: 100,
    direction: 'right', // 방향 정보
    isAttacking: false, // 공격 중인지 여부
    canUseSkill: true,  // 스킬 사용 가능 여부
    basicDamage: 10,    // 기본 공격 데미지
    skillCooldown: 5000 // 스킬 쿨타임 (밀리초)
};

// 스킬 쿨타임 표시를 위한 변수
let skillCooldownTime = 0; // 스킬 쿨타임 시간 (초 단위)
let skillCooldownRemaining = 0; // 남은 쿨타임 시간
let skillCooldownInterval; // 쿨타임 타이머 Interval

// 게임 시작 버튼 클릭 이벤트
startButton.addEventListener('click', () => {
    playerData.nickname = nicknameInput.value || '플레이어';
    playerData.class = classSelect.value;
    playerData.id = socket.id;

    // 직업별 기본 데미지 및 스킬 쿨타임 설정
    const skillIcon = document.getElementById('skill-icon');
    if (playerData.class === 'gladiator') {
        playerData.basicDamage = 15;
        playerData.skillCooldown = 5000;
        skillCooldownTime = 5; // 5초
        skillIcon.style.backgroundImage = "url('gladiator_skill.webp')"; // 스킬 아이콘 이미지 설정
    } else if (playerData.class === 'general') {
        playerData.basicDamage = 20;
        playerData.skillCooldown = 7000;
        skillCooldownTime = 7; // 7초
        skillIcon.style.backgroundImage = "url('general_skill.webp')";
    } else if (playerData.class === 'archer') {
        playerData.basicDamage = 10;
        playerData.skillCooldown = 150;
        skillCooldownTime = 0.15; // 5초
        skillIcon.style.backgroundImage = "url('archer_skill.webp')";
    }
    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && chatInput.value.trim() !== '') {
            const message = chatInput.value.trim();
            socket.emit('chatMessage', { id: socket.id, nickname: playerData.nickname, message });
            chatInput.value = '';
            chatInput.blur(); // 입력 후 포커스 아웃
            isChatFocused = false;
            chatInput.style.display = 'none'; // 입력 필드 숨기기
        } else if (e.key === 'Enter') {
            // 빈 메시지일 경우 입력 필드 숨기기
            chatInput.blur();
            isChatFocused = false;
            chatInput.style.display = 'none';
        }
    });

    chatContainer.addEventListener('click', () => {
        chatInput.style.display = 'block';
        chatInput.focus();
        isChatFocused = true;
    });

    // 입력 필드 포커스 여부 체크
    chatInput.addEventListener('focus', () => {
        isChatFocused = true;
    });

    chatInput.addEventListener('blur', () => {
        isChatFocused = false;
        chatInput.style.display = 'none'; // 입력 필드 숨기기
    });

    // 스킬 쿨타임 초기화
    skillCooldownRemaining = 0;
    updateSkillCooldownUI();

    loginContainer.style.display = 'none';
    gameContainer.style.display = 'block';

    initializePlayer();
    movePlayer();
});

socket.on('chatMessage', (data) => {
    const messageElement = document.createElement('p');
    messageElement.innerHTML = `<strong>${data.nickname}:</strong> ${data.message}`;
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight; // 스크롤을 맨 아래로
});

// 플레이어 초기화 함수
function initializePlayer() {
    // 플레이어 요소 생성
    localPlayer = document.createElement('div');
    localPlayer.id = playerData.id;
    localPlayer.classList.add('player');
    gameContainer.appendChild(localPlayer);

    // 닉네임 표시
    const nicknameTag = document.createElement('div');
    nicknameTag.classList.add('nickname');
    nicknameTag.innerText = playerData.nickname;
    localPlayer.appendChild(nicknameTag);

    // 체력 바 표시
    const healthBar = document.createElement('div');
    healthBar.classList.add('health-bar');
    healthBar.style.width = '100%';
    localPlayer.appendChild(healthBar);

    // 무기 생성 및 설정
    const weapon = document.createElement('div');
    weapon.classList.add('weapon');
    localPlayer.appendChild(weapon);

    // 직업에 따른 무기 설정
    if (playerData.class === 'gladiator') {
        // 검투사의 도끼 설정
        weapon.style.width = '20px';
        weapon.style.height = '20px';
        weapon.style.backgroundColor = 'gray';
        weapon.style.borderRadius = '50%';
        weapon.style.position = 'absolute';
        weapon.style.left = '-30px';
        weapon.style.top = '10px';

    } else if (playerData.class === 'general') {
        // 장군의 창 설정
        weapon.style.width = '10px';
        weapon.style.height = '50px';
        weapon.style.backgroundColor = 'silver';
        weapon.style.position = 'absolute';
        weapon.style.left = '15px';
        weapon.style.top = '-30px';
    } else if (playerData.class === 'archer') {
        // 궁수의 활 설정
        weapon.style.width = '40px';
        weapon.style.height = '10px';
        weapon.style.backgroundColor = 'brown';
        weapon.style.position = 'absolute';
        weapon.style.left = '0px';
        weapon.style.top = '15px';
    }

    // 서버에 새로운 플레이어 정보를 전송
    socket.emit('newPlayer', playerData);
}

// 키보드 입력 처리
let keys = {};
document.addEventListener('keydown', (e) => {
    keys[e.key] = true;

    // 스킬 사용 키 (이동 중에도 스킬 사용 가능)
    if ((e.key === 'x' || e.key === 'X') && playerData.canUseSkill) {
        useSkill();
    }

    if (e.key === 'Enter') {
        chatInput.style.display = 'block';
        chatInput.focus();
        isChatFocused = true;
    }
});

document.addEventListener('keyup', (e) => {
    keys[e.key] = false;
});

function updateMovement() {
    let moved = false;
    if (keys['ArrowUp']) {
        playerData.y -= playerData.speed;
        playerData.direction = 'up';
        moved = true;
    }
    if (keys['ArrowDown']) {
        playerData.y += playerData.speed;
        playerData.direction = 'down';
        moved = true;
    }
    if (keys['ArrowLeft']) {
        playerData.x -= playerData.speed;
        playerData.direction = 'left';
        moved = true;
    }
    if (keys['ArrowRight']) {
        playerData.x += playerData.speed;
        playerData.direction = 'right';
        moved = true;
    }

    // 대각선 이동 처리
    if (keys['ArrowUp'] && keys['ArrowLeft']) playerData.direction = 'up-left';
    if (keys['ArrowUp'] && keys['ArrowRight']) playerData.direction = 'up-right';
    if (keys['ArrowDown'] && keys['ArrowLeft']) playerData.direction = 'down-left';
    if (keys['ArrowDown'] && keys['ArrowRight']) playerData.direction = 'down-right';

    if (moved) movePlayer();
}

setInterval(updateMovement, 20);

socket.on('chatMessage', (data) => {
    const messageElement = document.createElement('p');
    const timestamp = new Date().toLocaleTimeString(); // 현재 시간
    messageElement.innerHTML = `<strong>[${timestamp}] ${escapeHtml(data.nickname)}:</strong> ${escapeHtml(data.message)}`;
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight; // 스크롤을 맨 아래로
});

// XSS 방지를 위한 이스케이프 처리 함수
function escapeHtml(text) {
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}
// 플레이어 이동 함수
function movePlayer() {
    if (localPlayer) {
        localPlayer.style.left = playerData.x + 'px';
        localPlayer.style.top = playerData.y + 'px';

        // 서버로 위치 정보 전송
        socket.emit('playerMove', playerData);
    }
}

// 스킬 사용 함수
function useSkill() {
    playerData.canUseSkill = false;
    skillCooldownRemaining = skillCooldownTime;
    updateSkillCooldownUI();
    startSkillCooldownTimer();

    if (playerData.class === 'archer') {
        fireArrow();
    }

    // 스킬 사용 정보를 서버로 전송 (궁수 포함)
    socket.emit('useSkill', playerData);
}

// 스킬 쿨타임 UI 업데이트 함수
function updateSkillCooldownUI() {
    const cooldownOverlay = document.getElementById('cooldown-overlay');
    const cooldownText = document.getElementById('cooldown-text');

    if (playerData.canUseSkill) {
        cooldownOverlay.style.height = '0%';
        cooldownText.innerText = '';
    } else {
        const percentage = (skillCooldownRemaining / skillCooldownTime) * 100;
        cooldownOverlay.style.height = `${percentage}%`;
        cooldownText.innerText = Math.ceil(skillCooldownRemaining);
    }
}
function fireArrow() {
    const arrow = document.createElement('div');
    arrow.classList.add('arrow');
    arrow.style.width = '10px'; // 화살 크기
    arrow.style.height = '5px';
    arrow.style.backgroundColor = 'black';
    arrow.style.position = 'absolute';
    arrow.style.left = playerData.x + 'px';
    arrow.style.top = playerData.y + 'px';
    arrow.direction = playerData.direction; // 화살 방향 저장

    gameContainer.appendChild(arrow);

    // 화살 이동 및 충돌 감지 로직 (interval 사용)
    const arrowMoveInterval = setInterval(() => {
        moveProjectile(arrow, arrow.direction); // 화살 이동

        // 다른 플레이어 충돌 감지
        for (let id in players) {
            if (id !== socket.id) {
                const otherPlayer = document.getElementById(id);
                if (otherPlayer) {
                    const distance = getDistance(
                        parseInt(arrow.style.left),
                        parseInt(arrow.style.top),
                        players[id].x,
                        players[id].y
                    );

                    // 히트 범위 증가 (원래 10px에서 20px로 증가)
                    if (distance < 20) {
                        // 서버에 데미지 이벤트 전송
                        socket.emit('arrowHit', { targetId: id, damage: 20 });

                        // 화살 제거 및 이동 중단
                        arrow.remove();
                        clearInterval(arrowMoveInterval);
                        return; // 데미지 중복 방지
                    }
                }
            }
        }

        // 화면 밖으로 나가면 제거
        if (parseInt(arrow.style.left) < 0 || parseInt(arrow.style.left) > window.innerWidth ||
            parseInt(arrow.style.top) < 0 || parseInt(arrow.style.top) > window.innerHeight) {
            arrow.remove();
            clearInterval(arrowMoveInterval);
        }
    }, 20);
}

// 스킬 쿨타임 타이머 시작 함수
function startSkillCooldownTimer() {
    skillCooldownInterval = setInterval(() => {
        skillCooldownRemaining -= 0.1;
        if (skillCooldownRemaining <= 0) {
            skillCooldownRemaining = 0;
            playerData.canUseSkill = true;
            clearInterval(skillCooldownInterval);
        }
        updateSkillCooldownUI();
    }, 100); // 0.1초마다 업데이트
}

// 충돌 감지를 위한 거리 계산 함수
function getDistance(x1, y1, x2, y2) {
    return Math.hypot(x2 - x1, y2 - y1);
}

// 투사체 이동 함수
function moveProjectile(projectile, direction) {
    let speed = 10;
    if (direction === 'up') projectile.style.top = parseInt(projectile.style.top) - speed + 'px';
    if (direction === 'down') projectile.style.top = parseInt(projectile.style.top) + speed + 'px';
    if (direction === 'left') projectile.style.left = parseInt(projectile.style.left) - speed + 'px';
    if (direction === 'right') projectile.style.left = parseInt(projectile.style.left) + speed + 'px';

    // 대각선 처리
    if (direction === 'up-left') {
        projectile.style.top = parseInt(projectile.style.top) - speed + 'px';
        projectile.style.left = parseInt(projectile.style.left) - speed + 'px';
    }
    if (direction === 'up-right') {
        projectile.style.top = parseInt(projectile.style.top) - speed + 'px';
        projectile.style.left = parseInt(projectile.style.left) + speed + 'px';
    }
    if (direction === 'down-left') {
        projectile.style.top = parseInt(projectile.style.top) + speed + 'px';
        projectile.style.left = parseInt(projectile.style.left) - speed + 'px';
    }
    if (direction === 'down-right') {
        projectile.style.top = parseInt(projectile.style.top) + speed + 'px';
        projectile.style.left = parseInt(projectile.style.left) + speed + 'px';
    }
}

// 서버로부터 현재 플레이어들 정보를 받았을 때
socket.on('currentPlayers', (serverPlayers) => {
    Object.keys(serverPlayers).forEach((id) => {
        if (id !== socket.id) {
            createOtherPlayer(serverPlayers[id]);
        }
        players[id] = serverPlayers[id];
    });
    updateRanking();
});

// 새로운 플레이어가 접속했을 때
socket.on('newPlayer', (data) => {
    if (data.id !== socket.id) {
        createOtherPlayer(data);
    }
    players[data.id] = data;
    updateRanking();
});

// 다른 플레이어 생성 함수
function createOtherPlayer(data) {
    const otherPlayer = document.createElement('div');
    otherPlayer.id = data.id;
    otherPlayer.classList.add('player');
    otherPlayer.style.left = data.x + 'px';
    otherPlayer.style.top = data.y + 'px';

    // 닉네임 표시
    const nicknameTag = document.createElement('div');
    nicknameTag.classList.add('nickname');
    nicknameTag.innerText = data.nickname;
    otherPlayer.appendChild(nicknameTag);

    // 체력 바 표시
    const healthBar = document.createElement('div');
    healthBar.classList.add('health-bar');
    healthBar.style.width = `${data.hp}%`;
    otherPlayer.appendChild(healthBar);

    // 무기 생성 및 설정
    const weapon = document.createElement('div');
    weapon.classList.add('weapon');
    otherPlayer.appendChild(weapon);

    // 직업에 따른 무기 설정
    if (data.class === 'gladiator') {
        // 검투사의 도끼 설정
        weapon.style.width = '20px';
        weapon.style.height = '20px';
        weapon.style.backgroundColor = 'gray';
        weapon.style.borderRadius = '50%';
        weapon.style.position = 'absolute';
        weapon.style.left = '-30px';
        weapon.style.top = '10px';
    } else if (data.class === 'general') {
        // 장군의 창 설정
        weapon.style.width = '10px';
        weapon.style.height = '50px';
        weapon.style.backgroundColor = 'silver';
        weapon.style.position = 'absolute';
        weapon.style.left = '15px';
        weapon.style.top = '-30px';
    } else if (data.class === 'archer') {
        // 궁수의 활 설정
        weapon.style.width = '40px';
        weapon.style.height = '10px';
        weapon.style.backgroundColor = 'brown';
        weapon.style.position = 'absolute';
        weapon.style.left = '0px';
        weapon.style.top = '15px';
    }

    gameContainer.appendChild(otherPlayer);
}

// 다른 플레이어의 이동 정보를 받았을 때
socket.on('skillUsed', (data) => {
    const playerElement = document.getElementById(data.id);
    if (playerElement) {
        // 스킬 애니메이션 또는 효과를 표시
        if (data.class === 'gladiator') {  // ✅ if문 추가
            const weapon = playerElement.querySelector('.weapon');
            let angle = 0;
            const attackRadius = 40; // 회전 반경

            // 캐릭터의 중심 좌표 가져오기
            const characterRect = playerElement.getBoundingClientRect();
            const centerX = characterRect.width / 2;
            const centerY = characterRect.height / 2;

            const attackInterval = setInterval(() => {
                angle += 20; // 회전 속도
                if (angle >= 360) {
                    clearInterval(attackInterval);
                    weapon.style.transform = ''; // 원래 상태로 복귀
                    return;
                }

                // 무기 위치를 캐릭터 중심을 기준으로 회전시키기
                const x = centerX + attackRadius * Math.cos(angle * (Math.PI / 180));
                const y = centerY + attackRadius * Math.sin(angle * (Math.PI / 180));

                // 캐릭터 중심에서의 상대 위치 적용
                weapon.style.position = 'absolute';
                weapon.style.left = `${x}px`;
                weapon.style.top = `${y}px`;
                weapon.style.transform = `rotate(${angle}deg)`;
            }, 50);
        } else if (data.class === 'general') {
            // 장군의 돌진 애니메이션
            playerElement.style.transition = 'left 0.2s, top 0.2s';
            playerElement.style.left = data.x + 'px';
            playerElement.style.top = data.y + 'px';
            setTimeout(() => {
                playerElement.style.transition = '';
            }, 200);
        } else if (data.class === 'archer') {
            // 궁수의 백스텝 및 화살 발사 애니메이션
            playerElement.style.transition = 'left 0.2s, top 0.2s';
            playerElement.style.left = data.x + 'px';
            playerElement.style.top = data.y + 'px';
            setTimeout(() => {
                playerElement.style.transition = '';
            }, 200);

            // 화살 두 발 발사 (시각적 효과만)
            for (let i = 0; i < 2; i++) {
                const arrow = document.createElement('div');
                arrow.classList.add('arrow');
                arrow.style.left = data.x + 'px';
                arrow.style.top = data.y + 'px';
                gameContainer.appendChild(arrow);

                let arrowInterval = setInterval(() => {
                    moveProjectile(arrow, data.direction);
                }, 20);

                setTimeout(() => {
                    arrow.remove();
                    clearInterval(arrowInterval);
                }, 2000);
            }
        }
    }
});

// 데미지 및 체력 업데이트
socket.on('updateHealth', (data) => {
    if (data.id === socket.id) {
        playerData.hp = data.hp;
        const healthBar = localPlayer.querySelector('.health-bar');
        healthBar.style.width = `${playerData.hp}%`;
        if (playerData.hp <= 0) {
            alert('사망했습니다!');
            // 게임 오버 처리 또는 리스폰 로직 추가
        }
    } else {
        const otherPlayer = document.getElementById(data.id);
        if (otherPlayer) {
            const healthBar = otherPlayer.querySelector('.health-bar');
            healthBar.style.width = `${data.hp}%`;
            if (data.hp <= 0) {
                otherPlayer.remove();
                delete players[data.id];
                updateRanking();
            }
        }
    }
});

// 다른 플레이어가 나갔을 때
socket.on('removePlayer', (id) => {
    delete players[id];
    const otherPlayer = document.getElementById(id);
    if (otherPlayer) {
        otherPlayer.remove();
    }
    updateRanking();
});

// 점수 랭킹 업데이트 함수 (상위 5명만 표시)
function updateRanking() {
    const rankingList = document.getElementById('ranking-list');
    rankingList.innerHTML = '';

    // 점수 순으로 플레이어 정렬 (내림차순)
    const sortedPlayers = Object.values(players).sort((a, b) => b.score - a.score);

    // 상위 5명만 리스트에 추가
    sortedPlayers.slice(0, 5).forEach((player, index) => {
        const listItem = document.createElement('li');
        listItem.textContent = `${index + 1}위 ${player.nickname}: ${player.score}점`;
        rankingList.appendChild(listItem);
    });
}


// 점수 업데이트 정보를 받았을 때
socket.on('updateScores', (scores) => {
    Object.keys(scores).forEach((id) => {
        if (players[id]) {
            players[id].score = scores[id].score;
        }
    });
    updateRanking();
});

// 왼쪽 광고 X 버튼 클릭 시 숨기기
document.getElementById('close-left-ad').addEventListener('click', function(event) {
    event.stopPropagation();  // 링크 클릭 이벤트 방지
    document.getElementById('left-ad').style.display = 'none';
});

// 오른쪽 광고 X 버튼 클릭 시 숨기기
document.getElementById('close-right-ad').addEventListener('click', function(event) {
    event.stopPropagation();  // 링크 클릭 이벤트 방지
    document.getElementById('right-ad').style.display = 'none';
});