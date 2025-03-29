package com.example.project.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.project.board.Post;
import com.example.project.board.PostService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/board")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    // 댓글 저장
    @PostMapping("/{boardType}/{id}/comment")
    public ResponseEntity<?> saveComment(
            @PathVariable String boardType,
            @PathVariable("id") Long postId,
            @RequestBody Comment comment,
            HttpSession session) {

        try {
            String username = (String) session.getAttribute("user_name");
            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인이 필요합니다.");
            }

            Post post = postService.getPostById(postId);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 게시물 ID입니다.");
            }

            // 댓글 저장
            comment.setAuthor(username);
            comment.setPost(post);
            comment.setCreatedAt(LocalDateTime.now());
            commentService.saveComment(comment);

            // 최신 댓글 반환
            Page<Comment> commentsPage = commentService.getCommentsByPost(
                post, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
            );

            List<CommentDTO> commentDTOs = commentsPage.getContent()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(commentDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 저장 중 문제가 발생했습니다.");
        }
    }

    // 댓글 목록 가져오기
    @GetMapping("/{boardType}/{id}/comments")
    public ResponseEntity<CommentPageDTO> getComments(
            @PathVariable String boardType,
            @PathVariable("id") Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            if (page < 0 || size <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            Post post = postService.getPostById(postId);
            if (post == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            Page<Comment> commentsPage = commentService.getCommentsByPost(
                post, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            );

            CommentPageDTO commentPageDTO = new CommentPageDTO(
                commentsPage.getContent().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList()),
                commentsPage.getTotalPages()
            );

            return ResponseEntity.ok(commentPageDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private CommentDTO convertToDTO(Comment comment) {
        return new CommentDTO(
            comment.getId(), comment.getAuthor(),
            comment.getContent(), comment.getCreatedAt()
        );
    }
}


