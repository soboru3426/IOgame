package com.example.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_game_data")
public class UserGameData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameDataId;

    @ManyToOne
    @JoinColumn(name = "user_num", nullable = false) // users 테이블의 외래 키
    private User user;

    @Column(nullable = false)
    private int exp = 0;

    @Column(nullable = false)
    private int gold = 0;

    @Column(nullable = false)
    private int killcnt = 0;

    @Column(nullable = false)
    private int userLevel = 1;

	public UserGameData(User user, int exp, int gold, int killcnt, int userLevel) {
		this.user = user;
		this.exp = exp;
		this.gold = gold;
		this.killcnt = killcnt;
		this.userLevel = userLevel;
	}
    
    
}