package com.example.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 사용자 아이디로 사용자 찾기
    User findByUserId(String userId);

    // 이메일로 사용자 찾기
    User findByEmail(String email);
}