// game.js (1/5) - 기본 설정 및 로그인 처리

const socket = io();
const gameContainer = document.getElementById('game-container');
const loginContainer = document.getElementById('login-container');
const nicknameInput = document.getElementById('nickname-input');
const classSelect = document.getElementById('class-select');
const startButton = document.getElementById('start-button');
const chatContainer = document.getElementById('chat-container');
const chatMessages = document.getElementById('chat-messages');
const chatInput = document.getElementById('chat-input');

let players = {};
let localPlayer;
let isChatFocused = false;

let playerData = {
  id: null,
  x: Math.random() * window.innerWidth,
  y: Math.random() * window.innerHeight,
  speed: 2,
  nickname: '',
  class: '',
  score: 0,
  hp: 100,
  direction: 'right',
  isAttacking: false,
  canUseSkill: true,
  basicDamage: 10,
  skillCooldown: 5000
};

let skillCooldownTime = 0;
let skillCooldownRemaining = 0;
let skillCooldownInterval;

function directionToIndex(direction, job) {
    if (job === 'general') {
      // 궁수는: 0 왼쪽, 1 오른쪽, 2 위쪽, 3 아래쪽
      switch (direction) {
        case 'left': return 0;
        case 'right': return 1;
        case 'up': return 2;
        case 'down': return 3;
        default: return 0;
      }
    } else if (job === 'archer') {
      // 마법사는: 0 아래, 1 왼쪽, 2 오른쪽, 3 위쪽
      switch (direction) {
        case 'down': return 0;
        case 'left': return 1;
        case 'right': return 2;
        case 'up': return 3;
        default: return 0;
      }
    } else {
      // 검투사와 기타 직업은 기본 방향 유지
      switch (direction) {
        case 'down': return 0;
        case 'up': return 1;
        case 'left': return 2;
        case 'right': return 3;
        default: return 0;
      }
    }
  }

let frame = 0;

startButton.addEventListener('click', () => {
  playerData.nickname = nicknameInput.value || '플레이어';
  playerData.class = classSelect.value;
  playerData.id = socket.id;

  const skillIcon = document.getElementById('skill-icon');
  if (playerData.class === 'gladiator') {
    playerData.basicDamage = 15;
    playerData.skillCooldown = 5000;
    skillCooldownTime = 5;
    skillIcon.style.backgroundImage = "url('gladiator_skill.webp')";
  } else if (playerData.class === 'general') {
    playerData.basicDamage = 20;
    playerData.skillCooldown = 7000;
    skillCooldownTime = 7;
    skillIcon.style.backgroundImage = "url('general_skill.webp')";
  } else if (playerData.class === 'archer') {
    playerData.basicDamage = 10;
    playerData.skillCooldown = 150;
    skillCooldownTime = 0.15;
    skillIcon.style.backgroundImage = "url('archer_skill.webp')";
  }

  loginContainer.style.display = 'none';
  gameContainer.style.display = 'block';

  skillCooldownRemaining = 0;
  updateSkillCooldownUI();

  initializePlayer();
  movePlayer();
});

// 화면 클릭 시 채팅 포커스 해제
window.addEventListener('click', (e) => {
  if (isChatFocused && e.target !== chatInput) {
    chatInput.style.display = 'none';
    chatInput.blur();
    isChatFocused = false;
  }
});

// 채팅 전송 처리
chatInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    const message = chatInput.value.trim();
    if (message !== '') {
      socket.emit('chatMessage', { nickname: playerData.nickname, message });
      chatInput.value = '';
    }
    chatInput.style.display = 'none';
    chatInput.blur();
    isChatFocused = false;
  }
});

// 채팅 수신 처리
socket.on('chatMessage', (data) => {
  const messageElement = document.createElement('div');
  messageElement.textContent = `${data.nickname}: ${data.message}`;
  chatMessages.appendChild(messageElement);
  chatMessages.scrollTop = chatMessages.scrollHeight;
});

const jobs = {
    gladiator: {
      src: "./killyou2.png",
      weapon: "./kimremove.png",
      frameWidth: 58,
      frameHeight: 117,
      weaponScale: 0.2,
      weaponLogic: (x, y, dir) => {
        if (dir === 0) return { x: x + 20, y: y + 90, rotate: 0 };
        if (dir === 1) return { x: x + 20, y: y + 10, rotate: 0 };
        if (dir === 2) return { x: x, y: y + 50, rotate: -90 };
        return { x: x + 50, y: y + 50, rotate: 90 };
      }
    },
    general: {   // 이름만 장군이지 실제론 궁수 코드입니다.
        src: "./helpme.png",
        weapon: "./shootingremove.png",
        frameWidth: 87,
        frameHeight: 90,
        weaponScale: 0.2,
        weaponLogic: (x, y, dir) => {
          // dir 기준: 0 = down, 1 = up, 2 = left, 3 = right
          if (dir === 2) return { x: x, y: y + 40, rotate: -90 }; // 왼쪽
          if (dir === 3) return { x: x + 50, y: y + 40, rotate: 90 }; // 오른쪽
          if (dir === 1) return { x: x + 20, y: y + 5, rotate: 0 }; // 위쪽
          return { x: x + 20, y: y + 70, rotate: 0 }; // 아래쪽
        }
      },
      
    archer: { // 이름만 궁수이지 실제론 마법사 코드 입니다.
      src: "./ling.png",
      weapon: "./stickremove.png",
      frameWidth: 60,
      frameHeight: 88,
      weaponScale: 0.2,
      weaponLogic: (x, y, dir) => {
        if (dir === 2) return { x: x + 10, y: y + 35, rotate: 40 };   // left
        if (dir === 3) return { x: x + 30, y: y + 35, rotate: 35 };   // right
        if (dir === 1) return { x: x + 15, y: y + 10, rotate: -45 };  // up
        return { x: x + 15, y: y + 60, rotate: -45 };                 // down
      }
    },
    wizard: { // 그냥 gpt가 생성한 코드
      src: "./wizard.png",
      weapon: "./staff.png",
      frameWidth: 64,
      frameHeight: 96,
      weaponScale: 0.25,
      weaponLogic: (x, y, dir) => {
        if (dir === 0) return { x: x + 22, y: y + 75, rotate: 0 };   // down
        if (dir === 2) return { x: x + 5, y: y + 40, rotate: -90 };  // left
        if (dir === 3) return { x: x + 45, y: y + 40, rotate: 90 };  // right
        return { x: x + 20, y: y + 5, rotate: 0 };                   // up
      }
    }
  };
  
  // 내 플레이어 초기화
  function initializePlayer() {
    const jobData = jobs[playerData.class];
    if (!jobData) return;
  
    localPlayer = document.createElement('div');
    localPlayer.id = playerData.id;
    localPlayer.classList.add('player');
    localPlayer.style.position = 'absolute';
    localPlayer.style.width = jobData.frameWidth + 'px';
    localPlayer.style.height = jobData.frameHeight + 'px';
    localPlayer.style.overflow = 'hidden';
    gameContainer.appendChild(localPlayer);
  
    const sprite = document.createElement('img');
    sprite.classList.add('character');
    sprite.src = jobData.src;
    sprite.style.width = jobData.frameWidth * 4 + 'px';
    sprite.style.height = jobData.frameHeight * 4 + 'px';
    sprite.style.position = 'absolute';
    sprite.style.imageRendering = 'pixelated';
  
    const weapon = document.createElement('img');
    weapon.classList.add('weapon');
    weapon.src = 'images/kimremove.png';

    weapon.style.position = 'absolute';
    weapon.style.pointerEvents = 'none';
    weapon.style.zIndex = '2';
    weapon.style.imageRendering = 'pixelated';
  
    localPlayer.sprite = sprite;
    localPlayer.weaponImg = weapon;
  
    localPlayer.appendChild(sprite);
    localPlayer.appendChild(weapon);
  
    const nicknameTag = document.createElement('div');
    nicknameTag.classList.add('nickname');
    nicknameTag.innerText = playerData.nickname;
    localPlayer.appendChild(nicknameTag);
  
    const healthBar = document.createElement('div');
    healthBar.classList.add('health-bar');
    healthBar.style.width = '100%';
    localPlayer.appendChild(healthBar);
  
    socket.emit('newPlayer', playerData);
  }
  
  // 다른 플레이어 생성
function createOtherPlayer(data) {
    const jobData = jobs[data.class];
    if (!jobData) return;
  
    const otherPlayer = document.createElement('div');
    otherPlayer.id = data.id;
    otherPlayer.classList.add('player');
    otherPlayer.style.position = 'absolute';
    otherPlayer.style.left = data.x + 'px';
    otherPlayer.style.top = data.y + 'px';
    otherPlayer.style.width = jobData.frameWidth + 'px';
    otherPlayer.style.height = jobData.frameHeight + 'px';
    otherPlayer.style.overflow = 'hidden';
  
    const sprite = document.createElement('img');
    sprite.classList.add('character');
    sprite.src = jobData.src;
    sprite.style.width = jobData.frameWidth * 4 + 'px';
    sprite.style.height = jobData.frameHeight * 4 + 'px';
    sprite.style.position = 'absolute';
    sprite.style.imageRendering = 'pixelated';
    
    const weapon = document.createElement('img');
    weapon.classList.add('weapon');
    weapon.src = 'images/kimremove.png'; // 이미지 경로 확인 필요!
    
    weapon.style.position = 'absolute';
    weapon.style.pointerEvents = 'none';
    weapon.style.zIndex = '2';
    weapon.style.imageRendering = 'pixelated';
    
    otherPlayer.sprite = sprite;       // ✅ sprite도 연결해줘야 weapon update 가능
    otherPlayer.weaponImg = weapon;    // ✅ 다른 플레이어 객체에 weaponImg 저장
    
    otherPlayer.appendChild(sprite);
    otherPlayer.appendChild(weapon);   // ✅ weapon도 붙임
    

  
    const nicknameTag = document.createElement('div');
    nicknameTag.classList.add('nickname');
    nicknameTag.innerText = data.nickname;
    otherPlayer.appendChild(nicknameTag);
  
    const healthBar = document.createElement('div');
    healthBar.classList.add('health-bar');
    healthBar.style.width = `${data.hp}%`;
    otherPlayer.appendChild(healthBar);
  
    gameContainer.appendChild(otherPlayer);
  }
  
  // 이동 처리
  let keys = {};
  document.addEventListener('keydown', (e) => {
    if (isChatFocused) return;
  
    keys[e.key] = true;
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
  
  document.addEventListener('click', () => {
    // 화면 클릭 시 채팅창 포커스 해제
    if (isChatFocused) {
      chatInput.blur();
      chatInput.style.display = 'none';
      isChatFocused = false;
    }
  });
  
  function updateMovement() {
    if (isChatFocused) return;
  
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
  
    if (keys['ArrowUp'] && keys['ArrowLeft']) playerData.direction = 'up';
    if (keys['ArrowUp'] && keys['ArrowRight']) playerData.direction = 'up';
    if (keys['ArrowDown'] && keys['ArrowLeft']) playerData.direction = 'down';
    if (keys['ArrowDown'] && keys['ArrowRight']) playerData.direction = 'down';
  
    if (moved) movePlayer();
  }
  
  setInterval(updateMovement, 10);
  function movePlayer() {
    if (localPlayer) {
      localPlayer.style.left = playerData.x + 'px';
      localPlayer.style.top = playerData.y + 'px';
  
      playerData.frame = frame;
  
      socket.emit('playerMove', playerData);
  
      updateCharacterSpriteAndWeapon();
    }
  }
  
  let lastFrameTime = Date.now();

function updateCharacterSpriteAndWeapon() {
  const jobData = jobs[playerData.class];
  if (!jobData || !localPlayer.sprite || !localPlayer.weaponImg) return;

    // 👉 여기서 시간 체크로 프레임 전환 제한
    const now = Date.now();
    if (now - lastFrameTime > 150) { // 150ms마다만 프레임 전환
      frame = (frame + 1) % 4;
      lastFrameTime = now;
    }

    const sprite = localPlayer.sprite;
    const directionIndex = directionToIndex(playerData.direction, playerData.class);
    sprite.style.transform = `translate(-${frame * jobData.frameWidth}px, -${directionIndex * jobData.frameHeight}px)`;
  
    const characterRect = localPlayer.getBoundingClientRect();
    const gameRect = gameContainer.getBoundingClientRect();
    const relativeX = characterRect.left - gameRect.left;
    const relativeY = characterRect.top - gameRect.top;
  
    const weaponPos = jobData.weaponLogic(relativeX, relativeY, directionIndex);
    const weapon = localPlayer.weaponImg;
  
    // ✅ 디버깅용 로그
    console.log("🛠️ weapon.src:", weapon.src);
    console.log("🛠️ 무기 위치 계산:", weaponPos);
    console.log("🛠️ weapon 엘리먼트 유효:", weapon instanceof HTMLImageElement);
    console.log("🛠️ 적용 전 weapon 위치:", weapon.style.left, weapon.style.top);
  
    weapon.style.left = weaponPos.x + 'px';
    weapon.style.top = weaponPos.y + 'px';
    weapon.style.transform = `scale(${jobData.weaponScale}) rotate(${weaponPos.rotate}deg)`;
  
    console.log("✅ 적용 후 weapon 위치:", weapon.style.left, weapon.style.top);
  }
  
  
  function useSkill() {
    playerData.canUseSkill = false;
    skillCooldownRemaining = skillCooldownTime;
    updateSkillCooldownUI();
    startSkillCooldownTimer();
  
    if (playerData.class === 'archer') {
      fireArrow();
    }
  
    socket.emit('useSkill', playerData);
  }
  
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
  
  function startSkillCooldownTimer() {
    skillCooldownInterval = setInterval(() => {
      skillCooldownRemaining -= 0.1;
      if (skillCooldownRemaining <= 0) {
        skillCooldownRemaining = 0;
        playerData.canUseSkill = true;
        clearInterval(skillCooldownInterval);
      }
      updateSkillCooldownUI();
    }, 100);
  }
  
  function fireArrow() {
    const arrow = document.createElement('div');
    arrow.classList.add('arrow');
    arrow.style.width = '10px';
    arrow.style.height = '5px';
    arrow.style.backgroundColor = 'black';
    arrow.style.position = 'absolute';
    arrow.style.left = playerData.x + 'px';
    arrow.style.top = playerData.y + 'px';
    arrow.direction = playerData.direction;
    gameContainer.appendChild(arrow);
  
    const arrowMoveInterval = setInterval(() => {
      moveProjectile(arrow, arrow.direction);
  
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
            if (distance < 20) {
              socket.emit('arrowHit', { targetId: id, damage: 20 });
              arrow.remove();
              clearInterval(arrowMoveInterval);
              return;
            }
          }
        }
      }
  
      if (
        parseInt(arrow.style.left) < 0 ||
        parseInt(arrow.style.left) > window.innerWidth ||
        parseInt(arrow.style.top) < 0 ||
        parseInt(arrow.style.top) > window.innerHeight
      ) {
        arrow.remove();
        clearInterval(arrowMoveInterval);
      }
    }, 20);
  }
  
  function moveProjectile(projectile, direction) {
    let speed = 10;
    if (direction === 'up') projectile.style.top = parseInt(projectile.style.top) - speed + 'px';
    if (direction === 'down') projectile.style.top = parseInt(projectile.style.top) + speed + 'px';
    if (direction === 'left') projectile.style.left = parseInt(projectile.style.left) - speed + 'px';
    if (direction === 'right') projectile.style.left = parseInt(projectile.style.left) + speed + 'px';
  
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
  
  function getDistance(x1, y1, x2, y2) {
    return Math.hypot(x2 - x1, y2 - y1);
  }
  // 서버로부터 플레이어 목록 수신
socket.on('currentPlayers', (serverPlayers) => {
    Object.keys(serverPlayers).forEach((id) => {
      if (id !== socket.id) {
        createOtherPlayer(serverPlayers[id]);
      }
      players[id] = serverPlayers[id];
    });
    updateRanking();
  });
  
  // 새 플레이어 접속
  socket.on('newPlayer', (data) => {
    if (data.id !== socket.id) {
      createOtherPlayer(data);
    }
    players[data.id] = data;
    updateRanking();
  });
  
  // 플레이어 이동
socket.on('playerMove', (data) => {
  if (data.id !== socket.id) {
    const otherPlayer = document.getElementById(data.id);
    if (otherPlayer) {
      otherPlayer.style.left = data.x + 'px';
      otherPlayer.style.top = data.y + 'px';

      const jobData = jobs[data.class];
      const sprite = otherPlayer.querySelector('.character');
      if (sprite && jobData) {
        const dirIndex = directionToIndex(data.direction, data.class);
        const frameX = (data.frame || 0) * jobData.frameWidth;
        sprite.style.transform = `translate(-${frameX}px, -${dirIndex * jobData.frameHeight}px)`;
      }

      const weapon = otherPlayer.querySelector('.weapon');
      if (weapon && jobData) {
        // ✅ 상대방 위치 기준으로 무기 위치 계산
        const characterRect = otherPlayer.getBoundingClientRect();
        const gameRect = gameContainer.getBoundingClientRect();
        const relX = characterRect.left - gameRect.left;
        const relY = characterRect.top - gameRect.top;

        const dirIndex = directionToIndex(data.direction, data.class);
        const weaponPos = jobData.weaponLogic(relX, relY, dirIndex);

        weapon.style.left = weaponPos.x + "px";
        weapon.style.top = weaponPos.y + "px";
        weapon.style.transform = `scale(${jobData.weaponScale}) rotate(${weaponPos.rotate}deg)`;
      }
    }
  }
});

  
  // 체력 업데이트
  socket.on('updateHealth', (data) => {
    if (data.id === socket.id) {
      playerData.hp = data.hp;
      const healthBar = localPlayer.querySelector('.health-bar');
      healthBar.style.width = `${playerData.hp}%`;
      if (playerData.hp <= 0) {
        alert('사망했습니다!');
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
  
  // 플레이어 퇴장
  socket.on('removePlayer', (id) => {
    delete players[id];
    const otherPlayer = document.getElementById(id);
    if (otherPlayer) {
      otherPlayer.remove();
    }
    updateRanking();
  });
  
  // 점수 업데이트
  socket.on('updateScores', (scores) => {
    Object.keys(scores).forEach((id) => {
      if (players[id]) {
        players[id].score = scores[id].score;
      }
    });
    updateRanking();
  });
  
  
  // 화면 클릭 시 채팅창 벗어나기
  document.addEventListener('click', (e) => {
    if (isChatFocused && !chatInput.contains(e.target)) {
      chatInput.blur();
      chatInput.style.display = 'none';
      isChatFocused = false;
    }
  });
  
  // 랭킹 갱신
  function updateRanking() {
    const rankingList = document.getElementById('ranking-list');
    rankingList.innerHTML = '';
  
    const sortedPlayers = Object.values(players).sort((a, b) => b.score - a.score);
    sortedPlayers.slice(0, 5).forEach((player, index) => {
      const listItem = document.createElement('li');
      listItem.textContent = `${index + 1}위 ${player.nickname}: ${player.score}점`;
      rankingList.appendChild(listItem);
    });
  }
  
 // 광고 닫기 버튼 기능
document.getElementById('close-left-ad').addEventListener('click', () => {
  document.getElementById('left-ad').style.display = 'none';
});

document.getElementById('close-right-ad').addEventListener('click', () => {
  document.getElementById('right-ad').style.display = 'none';
});
