package com.example.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam("userid") String userid, 
                        @RequestParam("password") String password, 
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        User user = userService.login(userid, password);  // userid로 로그인 처리
        
        if (user != null) {
            session.setAttribute("user", user);  // 로그인한 사용자 세션에 저장
            session.setAttribute("user_id", user.getUserId());
            session.setAttribute("user_name", user.getUserName());
            
            // 세션 데이터 확인 (디버깅용 로그)
            System.out.println("Session user_id: " + session.getAttribute("user_id"));
            System.out.println("Session user_name: " + session.getAttribute("user_name"));
            
            return "redirect:/main";  // 로그인 성공 후 메인 페이지로 리디렉션
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 실패: 사용자 정보가 잘못되었습니다.");
            return "redirect:/index";  // 로그인 실패 시 다시 로그인 페이지로
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 데이터 확인 (디버깅용 로그)
        System.out.println("Logging out user: " + session.getAttribute("user_id"));
        
        session.invalidate();  // 세션 무효화 (로그아웃 처리)
        return "redirect:/index";  // 메인 페이지로 리디렉션
    }
}
