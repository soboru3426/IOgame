package com.example.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 세션에서 로그인한 사용자 정보 가져오기
        User user = (User) session.getAttribute("user");

        // 로그인되지 않은 경우 로그인 페이지로 리디렉션
        if (user == null) {
        	redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 확인하세요.");
            return "redirect:/index";  // 로그인 안 된 경우 로그인 페이지로 리디렉션
        }

        // 로그인한 사용자의 프로필 정보를 userService에서 가져오기
        User userProfile = userService.getUserById(user.getUserId());
        model.addAttribute("user", userProfile);  // 모델에 사용자 프로필 정보 추가

        return "profile/profile";  // 프로필 페이지로 이동
    }
}
