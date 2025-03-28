const config = {
    type: Phaser.AUTO,
    width: 800,
    height: 600,
    backgroundColor: "#ddd",
    parent: "phaser-game", // HTML에 <div id="phaser-game"></div> 생성 필요
    scene: {
      preload: preload,
      create: create,
      update: update
    }
  };
  
  const game = new Phaser.Game(config);
  
  let character;       // 캐릭터 스프라이트
  let cursors;         // 키 입력 (Arrow 키)
  let messageText;     // "죽었습니다" 메시지 텍스트 객체
  
  // 캐릭터 초기 위치 (게임 중앙)
  let posX = config.width / 2;
  let posY = config.height / 2;
  
  function preload() {
    // 캐릭터 스프라이트 시트 로드 (프레임: 4행 4열로 구성 → 각 프레임 크기: 58x117)
    // 실제 스프라이트 시트 이미지 경로로 바꾸세요.
    this.load.spritesheet("character", "./killyou2.png", {
      frameWidth: 58,
      frameHeight: 117
    });
  }
  
  function create() {
    // 캐릭터 스프라이트 요청 생성 - 중앙에 위치
    character = this.add.sprite(posX, posY, "character", 0);
    character.setOrigin(0.5, 0.5);
  
    // 애니메이션 정의: 
    // 화면 이동 시 4 프레임 애니메이션 (프레임 0~3를 순차 재생)
    this.anims.create({
      key: "walk",
      frames: this.anims.generateFrameNumbers("character", { start: 0, end: 3 }),
      frameRate: 10,
      repeat: -1
    });
  
    // 화살표 키 입력 처리
    cursors = this.input.keyboard.createCursorKeys();
  
    // 메시지 텍스트 (초기 비활성 상태)
    messageText = this.add.text(config.width / 2, 50, "", {
      fontSize: "24px",
      fontStyle: "bold",
      fill: "#ff0000",
      backgroundColor: "rgba(0,0,0,0.7)",
      padding: { x: 10, y: 10 }
    });
    messageText.setOrigin(0.5);
    messageText.setVisible(false);
  }
  
  function update(time, delta) {
    // 캐릭터 이동 속도
    const speed = 48;
    let moved = false;
  
    // 현재 좌표
    let newX = character.x;
    let newY = character.y;
  
    if (cursors.left.isDown) {
      newX -= speed;
      moved = true;
    } else if (cursors.right.isDown) {
      newX += speed;
      moved = true;
    }
    if (cursors.up.isDown) {
      newY -= speed;
      moved = true;
    } else if (cursors.down.isDown) {
      newY += speed;
      moved = true;
    }
  
    // 경계 체크: 캐릭터 전체가 씬 안에 있어야 함
    // 캐릭터의 반 너비와 반 높이를 고려합니다.
    const halfWidth = character.width / 2;  // 29 (58/2)
    const halfHeight = character.height / 2;  // 58 (117/2)
  
    if (newX < halfWidth || newX > config.width - halfWidth ||
        newY < halfHeight || newY > config.height - halfHeight) {
      // 경계 밖이면 메시지 출력 및 일정 시간 후 리셋
      if (!messageText.visible) {
        messageText.setText("죽었습니다");
        messageText.setVisible(true);
        // 1초 후에 캐릭터 중앙으로 리셋
        this.time.delayedCall(1000, resetCharacter, [], this);
      }
    } else {
      // 경계 내이면 캐릭터 이동 업데이트
      character.x = newX;
      character.y = newY;
      if (moved) {
        if (!character.anims.isPlaying) {
          character.anims.play("walk");
        }
      } else {
        character.anims.stop();
      }
      // 메시지가 보이는 상태라면 숨김
      if (messageText.visible) {
        messageText.setVisible(false);
      }
    }
  }
  
  function resetCharacter() {
    // 캐릭터 위치를 중앙으로 리셋
    character.x = config.width / 2;
    character.y = config.height / 2;
    if (messageText.visible) {
      messageText.setVisible(false);
    }
  }
  