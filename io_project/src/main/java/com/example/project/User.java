package com.example.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userNum;

    @Column(nullable = false, unique = true)
    private String userId;  // 로그인 아이디

    @Column(nullable = false, unique = true)
    private String userName;  // 사용자 닉네임

    @Column(nullable = false)
    private String password;  // 비밀번호

    @Column(nullable = false, unique = true)
    private String email;  // 이메일

    @Column(nullable = false)
    private int userLevel = 1;

    @Column(nullable = false)
    private int killcnt = 0;

    @Column(nullable = false)
    private int exp = 0;

    @Column(nullable = false)
    private int gold = 0;

    // 기본 생성자
    public User() {}

    // 생성자 (아이디, 닉네임, 비밀번호, 이메일을 받아서 객체를 생성)
    public User(String userId, String userName, String password, String email) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    // Getter와 Setter
    public Long getUserNum() {
        return userNum;
    }

    public void setUserNum(Long userNum) {
        this.userNum = userNum;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public int getKillcnt() {
        return killcnt;
    }

    public void setKillcnt(int killcnt) {
        this.killcnt = killcnt;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
