package com.example.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.notice.Notice;
import com.example.project.notice.NoticeService;

@Controller
public class RankingController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private NoticeService noticeService;
    
    // 랭킹 페이지를 보여주는 메서드
    @GetMapping("/ranking")
    public String showRanking(@RequestParam(defaultValue = "0") int page, Model model) {

        // 상위 4명의 유저 데이터를 가져옵니다.
        model.addAttribute("topRankedUsers", userService.getTopRankedUsers());

        // 전체 유저 데이터 (페이지네이션 처리)
        Page<User> rankedUsers = userService.getPagedRankedUsers(page);
        model.addAttribute("rankedUsers", rankedUsers);

        // 페이지네이션 정보도 모델에 추가
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rankedUsers.getTotalPages());

        // 템플릿 경로를 templates/ranking/ranking.html 로 지정
        return "ranking/ranking"; // "ranking/ranking"으로 수정
    }
    
    @GetMapping("/index")
    public String indexRanking(Model model) {
        // 상위 4명 랭킹 유저 정보를 가져옵니다.
        List<User> topRankedUsers = userService.getTopRankedUsers();

        // 상위 3명만 선택하여 모델에 추가
        if (topRankedUsers.size() > 3) {
            topRankedUsers = topRankedUsers.subList(0, 3);  // 첫 3명만 선택
        }
        
        // 공지사항 데이터를 가져옵니다.
        List<Notice> topNotices = noticeService.getTop3Notices();

        // 공지사항 데이터를 모델에 추가
        model.addAttribute("notices", topNotices);

        model.addAttribute("topRankedUsers", topRankedUsers);
        return "index"; // index.html로 이동
    }
}
