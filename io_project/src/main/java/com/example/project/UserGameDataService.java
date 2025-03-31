package com.example.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class UserGameDataService {

    @Autowired
    private UserGameDataRepository userGameDataRepository;

    // 상위 4명의 유저 데이터를 가져오는 메서드
    public List<UserGameData> getTopRankedUsers() {
        return userGameDataRepository.findTop4ByOrderByUserLevelDesc();
    }

    // 페이지네이션 처리된 유저 데이터를 가져오는 메서드
    public Page<UserGameData> getPagedRankedUsers(int page) {
        return userGameDataRepository.findAll(PageRequest.of(page, 10)); // 10개씩 페이지네이션
    }
}