package com.example.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 사용자 등록 로직
    @Transactional
    public String registerUser(String userId, String userName, String password, String confirmPassword, String email) {

        // 이미 존재하는 사용자 아이디나 이메일이 있는지 체크
        if (userRepository.findByUserId(userId) != null) {
            return "이미 존재하는 아이디입니다.";
        }
        if (userRepository.findByEmail(email) != null) {
            return "이미 존재하는 이메일입니다.";
        }

        // 비밀번호 확인
        if (!password.equals(confirmPassword)) {
            return "비밀번호가 일치하지 않습니다.";
        }

        // User 객체 생성
        User user = new User(userId, userName, password, email);
        
        try {
            // DB에 저장
            userRepository.save(user);
        } catch (Exception e) {
            return "회원가입 실패: " + e.getMessage();  // 예외 메시지를 반환
        }

        // 회원가입 성공 메시지
        return "회원가입이 완료되었습니다.";
    }
    
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId);  // userId로 사용자 정보 조회
    }
    
    // 상위 4명 랭킹 데이터를 가져오기
    public List<User> getTopRankedUsers() {
        return userRepository.findTop4ByOrderByUserLevelDesc();
    }
    
    // 전체 랭킹 데이터를 페이징 처리로 가져오기 (20명씩)
    public Page<User> getPagedRankedUsers(int page) {
        PageRequest pageRequest = PageRequest.of(page, 20); // 20명씩 페이징
        return userRepository.findAllByOrderByUserLevelDesc(pageRequest);
    }

    public User login(String userid, String password) {
        User user = userRepository.findByUserId(userid);  // userid로 사용자 찾기
        if (user != null && user.getPassword().equals(password)) {
            return user;  // 비밀번호가 일치하면 로그인 성공
        }
        return null;  // 실패 시 null 반환
    }
    
    public User getUserById(String userId) {
        return userRepository.findByUserId(userId);  // userId를 사용해 사용자 조회
    }
}
