package com.example.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService; // UserService를 주입받습니다.

    @PostMapping("/register")
    public String registerUser(@RequestParam("userId") String userId,
                               @RequestParam("userName") String userName,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model) {

        // 사용자 등록을 처리하고 결과 메시지를 반환받습니다.
        String resultMessage = userService.registerUser(userId, userName, password, confirmPassword, email);

        // 결과 메시지를 모델에 추가
        if (resultMessage.contains("완료되었습니다")) {
            return "redirect:/index";  // 성공 시 로그인 페이지로 리디렉션
        } else {
            model.addAttribute("error", resultMessage);  // 에러 메시지 모델에 추가
            return "register";  // 실패 시 회원가입 페이지로 돌아갑니다.
        }
    }
}
