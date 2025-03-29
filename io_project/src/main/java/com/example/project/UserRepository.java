package com.example.project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 사용자 아이디로 사용자 찾기
    User findByUserId(String userId);
    
    // 이메일로 사용자 찾기
    User findByEmail(String email);
    
    // 상위 4명 유저 가져오기
    List<User> findTop4ByOrderByUserLevelDesc();

    // 전체 유저 페이징 처리 (20명씩)
    Page<User> findAllByOrderByUserLevelDesc(Pageable pageable);
}
