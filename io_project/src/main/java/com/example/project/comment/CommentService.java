package com.example.project.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.project.board.Post;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    // 댓글 저장
    public void saveComment(Comment comment) {
        commentRepository.save(comment);
    }

    // 게시물에 해당하는 댓글을 페이지 단위로 가져오기
    public Page<Comment> getCommentsByPost(Post post, Pageable pageable) {
        return commentRepository.findByPost(post, pageable);
    }
}
