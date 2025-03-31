package com.example.project.notice;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maintitle;  // 공지사항의 메인 제목
    
    private String title;

    private String imageUrl;
    
    @Lob
    private String content; // 공지사항 내용
    
    private String maincontent;  // 추가된 필드: 공지사항의 주 내용 (maincontent)
    
    private LocalDateTime date; // 공지사항 작성 일자
    
    // 기본 생성자
    public Notice() {}

    // 생성자
    public Notice(String maintitle, String title, String content, String maincontent, LocalDateTime date) {
        this.maintitle = maintitle;  // maintitle 초기화
        this.title = title;
        this.content = content;
        this.maincontent = maincontent;  // maincontent 초기화
        this.date = date;
    }

    // Getter, Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaintitle() {
        return maintitle;
    }

    public void setMaintitle(String maintitle) {
        this.maintitle = maintitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return "/images/notice/" + this.id + ".jpg";  // 예: "/images/notice/1.jpg"
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMaincontent() {
        return maincontent;
    }

    public void setMaincontent(String maincontent) {
        this.maincontent = maincontent;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
}
