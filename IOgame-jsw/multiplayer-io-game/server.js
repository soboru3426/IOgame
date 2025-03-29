const express = require('express');
const http = require('http');
const socketIo = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

const PORT = process.env.PORT || 3000;

// 모든 플레이어의 상태를 추적하는 객체
let players = {};

// 클라이언트에게 정적 파일 제공
app.use(express.static('public'));

// 소켓 연결 처리
io.on('connection', (socket) => {
    console.log('A player connected: ' + socket.id);

    socket.on('chatMessage', (data) => {
        // 모든 클라이언트에게 채팅 메시지 브로드캐스트
        io.emit('chatMessage', data);
    });
    // 새로운 플레이어가 접속했을 때
    socket.on('newPlayer', (data) => {
        players[socket.id] = data;

        // 현재 플레이어에게 모든 플레이어 정보 전송
        socket.emit('currentPlayers', players);

        // 다른 플레이어들에게 새로운 플레이어 정보 전송
        socket.broadcast.emit('newPlayer', data);
    });

    // 플레이어 위치 업데이트
    socket.on('playerMove', (data) => {
        if (players[socket.id]) {
            players[socket.id].x = data.x;
            players[socket.id].y = data.y;
            players[socket.id].direction = data.direction;
            io.emit('playerMove', players[socket.id]);
        }
    });

    // 기본 공격 처리
    socket.on('basicAttack', (data) => {
        const attacker = players[socket.id];
        if (attacker) {
            // 공격 쿨타임 체크 (필요 시 구현)
            const currentTime = Date.now();
            if (attacker.lastAttackTime && currentTime - attacker.lastAttackTime < 500) return;

            attacker.lastAttackTime = currentTime;

            // 공격 범위 내의 플레이어들에게 데미지 적용
            for (let id in players) {
                if (id !== socket.id) {
                    const target = players[id];
                    const distance = getDistance(attacker.x, attacker.y, target.x, target.y);

                    // 직업별 기본 공격 범위 설정
                    let attackRange = 50;
                    if (attacker.class === 'archer') attackRange = 300; // 궁수의 화살 사정거리
                    if (distance <= attackRange) {
                        // 방향 체크 (궁수의 경우에만 적용)
                        if (attacker.class !== 'archer' || isInAttackDirection(attacker, target)) {
                            target.hp -= attacker.basicDamage;
                            io.to(id).emit('updateHealth', { id, hp: target.hp });
                            if (target.hp <= 0) {
                                attacker.score += 1;
                                updateScores();
                            }
                        }
                    }
                }
            }
        }
    });

    // 스킬 사용 처리
    socket.on('useSkill', (data) => {
        const player = players[socket.id];
        if (player) {
            const currentTime = Date.now();

            // 스킬 쿨타임 체크
            if (player.lastSkillTime && currentTime - player.lastSkillTime < player.skillCooldown) return;

            // 스킬 사용 시간 업데이트
            player.lastSkillTime = currentTime;

            // 스킬 효과 적용
            if (player.class === 'gladiator') {
                // 검투사의 빠른 무기 회전 공격
                for (let id in players) {
                    if (id !== socket.id) {
                        const target = players[id];
                        const distance = getDistance(player.x, player.y, target.x, target.y);
                        if (distance <= 100) {
                            target.hp -= 40;
                            io.to(id).emit('updateHealth', { id, hp: target.hp });
                            if (target.hp <= 0) {
                                player.score += 1;
                                updateScores();
                            }
                        }
                    }
                }
            } else if (player.class === 'general') {
                // 장군의 돌진 공격
                let dashDistance = 100;
                let originalX = player.x;
                let originalY = player.y;

                if (player.direction.includes('up')) player.y -= dashDistance;
                if (player.direction.includes('down')) player.y += dashDistance;
                if (player.direction.includes('left')) player.x -= dashDistance;
                if (player.direction.includes('right')) player.x += dashDistance;

                io.emit('playerMove', player);

                // 공격 범위 내의 플레이어들에게 데미지 적용
                for (let id in players) {
                    if (id !== socket.id) {
                        const target = players[id];
                        const distance = getDistance(player.x, player.y, target.x, target.y);
                        if (distance <= 50) {
                            target.hp -= 30 * 2; // 두 번 찌르기
                            io.to(id).emit('updateHealth', { id, hp: target.hp });
                            if (target.hp <= 0) {
                                player.score += 1;
                                updateScores();
                            }
                        }
                    }
                }
            } else if (player.class === 'archer') {
                // 궁수의 백스텝 및 화살 두 발 발사
                let backstepDistance = 50;
                if (player.direction.includes('up')) player.y += backstepDistance;
                if (player.direction.includes('down')) player.y -= backstepDistance;
                if (player.direction.includes('left')) player.x += backstepDistance;
                if (player.direction.includes('right')) player.x -= backstepDistance;

                io.emit('playerMove', player);

                // 화살 투사체는 클라이언트에서 처리
            }

            // 모든 클라이언트에게 스킬 사용 정보 전송
            io.emit('skillUsed', {
                id: socket.id,
                class: player.class,
                x: player.x,
                y: player.y,
                direction: player.direction
            });
        }
    });

    // 데미지 적용
    socket.on('inflictDamage', (data) => {
        const target = players[data.targetId];
        const attacker = players[socket.id];
        if (target && attacker) {
            target.hp -= data.damage;
    
            // 모든 사용자에게 업데이트된 HP 브로드캐스트
            io.emit('updateAllHealth', getAllPlayerHealth());
    
            // 플레이어가 죽었을 경우 처리
            if (target.hp <= 0) {
                attacker.score += 1;
                updateScores();
            }
        }
    });

    // 플레이어가 연결을 끊었을 때
    socket.on('disconnect', () => {
        console.log('A player disconnected: ' + socket.id);
        delete players[socket.id];
        io.emit('removePlayer', socket.id);
    });

    // 점수 업데이트 함수
    function updateScores() {
        const scores = {};
        for (let id in players) {
            scores[id] = {
                nickname: players[id].nickname,
                score: players[id].score
            };
        }
        io.emit('updateScores', scores);
    }

    function getAllPlayerHealth() {
        const healthData = {};
        for (let id in players) {
            healthData[id] = {
                nickname: players[id].nickname,
                hp: players[id].hp
            };
        }
        return healthData;
    }
    // 거리 계산 함수
    function getDistance(x1, y1, x2, y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    // 공격 방향 체크 함수 (궁수의 경우)
    function isInAttackDirection(attacker, target) {
        const dx = target.x - attacker.x;
        const dy = target.y - attacker.y;
        const angle = Math.atan2(dy, dx) * (180 / Math.PI);

        if (attacker.direction === 'up' && angle >= -135 && angle <= -45) return true;
        if (attacker.direction === 'down' && angle >= 45 && angle <= 135) return true;
        if (attacker.direction === 'left' && (angle >= 135 || angle <= -135)) return true;
        if (attacker.direction === 'right' && angle >= -45 && angle <= 45) return true;

        // 대각선 방향 처리
        if (attacker.direction === 'up-left' && angle >= -180 && angle <= -90) return true;
        if (attacker.direction === 'up-right' && angle >= -90 && angle <= 0) return true;
        if (attacker.direction === 'down-left' && angle >= 90 && angle <= 180) return true;
        if (attacker.direction === 'down-right' && angle >= 0 && angle <= 90) return true;

        return false;
    }
});



// 서버 시작
server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server is running on port ${PORT}`);
});
