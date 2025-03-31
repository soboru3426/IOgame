package com.example.project;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.project.notice.Notice;
import com.example.project.notice.NoticeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NoticeService noticeService;
    
    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }
    
    @GetMapping("/register")
    public String register() {
        return "register/register";
    }
    
    @RequestMapping("/ranking")
    public String ranking() {
        return "ranking/ranking";
    }
    
    @GetMapping("/main")
    public String mainPage(HttpSession session, Model model) {
        // 공지사항 데이터
        List<Notice> topNotices = noticeService.getTop3Notices();
        System.out.println("공지사항: " + topNotices); // 디버깅용 출력
        model.addAttribute("notices", topNotices);

        // 사용자 정보
        User user = (User) session.getAttribute("user");
        if (user != null) {
            System.out.println("사용자 이름: " + user.getUserId()); // 디버깅용 출력
            model.addAttribute("username", user.getUserId());

            // 랭킹 데이터
            List<User> topRankedUsers = userService.getTopRankedUsers();
            System.out.println("랭킹 데이터: " + topRankedUsers); // 디버깅용 출력
            model.addAttribute("topRankedUsers", topRankedUsers.subList(0, Math.min(3, topRankedUsers.size())));
        } else {
            System.out.println("비로그인 상태");
            model.addAttribute("topRankedUsers", new ArrayList<>()); // 빈 리스트 전달
        }

        return "main";
    }
}