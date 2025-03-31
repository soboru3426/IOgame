package com.example.project.notice;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 생성자 주입
    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    // 공지사항 목록 조회 (이미지 URL 포함)
    public List<Notice> getNoticeList() {
        List<Notice> notices = noticeRepository.findAll();

        // 각 공지사항에 이미지 URL을 설정
        for (Notice notice : notices) {
            // 이미지 경로를 "/images/notice/" + 공지사항 ID로 설정
            notice.setImageUrl("/images/notice/" + notice.getId() + ".jpg"); // 예시: 1.jpg, 2.jpg 등
        }

        return notices;
    }

    // 공지사항 상세 조회
    public Notice getNoticeDetail(Long id) {
        Optional<Notice> notice = noticeRepository.findById(id);
        if (notice.isPresent()) {
            return notice.get();
        } else {
            throw new IllegalArgumentException("Invalid notice Id");
        }
    }
    
    public List<Notice> getTop3Notices() {
        return noticeRepository.findAll(
            PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "date"))
        ).getContent();
    }
}
