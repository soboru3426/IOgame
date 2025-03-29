package com.example.project.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class NoticeController {
	
	@Autowired
    NoticeService noticeService;

    // 공지사항 목록 페이지
    @GetMapping("/notices")
    public String getNoticeList(Model model) {
        List<Notice> notices = noticeService.getNoticeList(); // Service에서 공지사항 목록을 가져옴
        model.addAttribute("notices", notices);
        return "notice/notices";
    }

    // 공지사항 상세 페이지
    @GetMapping("/notices/{id}")
    public String getNoticeDetail(@PathVariable Long id, Model model) {
        Notice notice = noticeService.getNoticeDetail(id); // Service에서 상세 정보를 가져옴
        model.addAttribute("notice", notice);
        return "notice/notice-detail"; // notice-detail.html 파일을 렌더링
    }
}
