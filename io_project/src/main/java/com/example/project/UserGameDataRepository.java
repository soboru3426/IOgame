package com.example.project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGameDataRepository extends JpaRepository<UserGameData, Long> {

    // 상위 4명의 데이터를 userLevel 기준으로 내림차순 정렬
    List<UserGameData> findTop4ByOrderByUserLevelDesc();

    // 페이지네이션
    Page<UserGameData> findAll(Pageable pageable);
}