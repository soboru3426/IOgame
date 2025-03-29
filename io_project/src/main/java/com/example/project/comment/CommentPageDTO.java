package com.example.project.comment;

import java.util.List;

public class CommentPageDTO {
    private List<CommentDTO> comments; // 댓글 목록
    private int totalPages; // 총 페이지 수

    public CommentPageDTO(List<CommentDTO> comments, int totalPages) {
        this.comments = comments;
        this.totalPages = totalPages;
    }

    // Getter & Setter
    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
