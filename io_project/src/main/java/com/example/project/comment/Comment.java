package com.example.project.comment;

import java.time.LocalDateTime;

import com.example.project.board.Post;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "comment") // 테이블 이름 명시
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // 작성자는 반드시 필요
    private String author;

    @Column(nullable = false, columnDefinition = "TEXT") // 내용은 반드시 필요하며 TEXT 타입
    private String content;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy 로딩으로 필요할 때만 게시글 데이터 로드
    @JoinColumn(name = "post_id", nullable = false) // 외래 키로 게시글 연결
    @JsonIgnore // Post 필드를 JSON 응답에 포함시키지 않음
    private Post post;

    @Column(nullable = false) // 작성일은 반드시 필요
    private LocalDateTime createdAt;

    // 기본 생성자 추가 (JPA 필요)
    public Comment() {}

    // 모든 필드를 포함하는 생성자 추가
    public Comment(String author, String content, Post post, LocalDateTime createdAt) {
        this.author = author;
        this.content = content;
        this.post = post;
        this.createdAt = createdAt;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 저장 이전 생성 시간 자동 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
