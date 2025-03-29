package com.example.project.board;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.project.comment.CommentService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BoardController {

    @Autowired
    PostService postService;

    @Autowired
    CommentService commentService;

    @GetMapping("/board/{boardType}")
    public String showBoard(
        @PathVariable String boardType,
        @RequestParam(required = false, defaultValue = "views") String sort, // 정렬 조건
        @RequestParam(required = false) String filter, // 검색 필터
        @RequestParam(required = false) String keyword, // 검색 키워드
        Model model,
        HttpSession session) {

        System.out.println("Filter: " + filter);
        System.out.println("Keyword: " + keyword);

        try {
            // Enum 매핑을 위한 boardType 대문자 변환
            BoardType type = BoardType.valueOf(boardType.toUpperCase());

            // 해당 게시판 유형에 맞는 게시글 가져오기
            List<Post> posts = postService.getPostsByBoardType(type);

            // 검색 로직: 검색 필터와 키워드가 있을 경우 게시글 필터링
            if (filter != null && keyword != null && !keyword.isEmpty()) {
                posts = posts.stream()
                        .filter(post -> applyFilter(post, filter, keyword))
                        .collect(Collectors.toCollection(ArrayList::new)); // ArrayList로 결과를 생성
            }

            // 정렬 로직
            if ("views".equalsIgnoreCase(sort)) {
                posts.sort(Comparator.comparingInt(Post::getViews).reversed()); // 조회순 정렬
            } else {
                posts.sort(Comparator.comparing(Post::getCreatedAt).reversed()); // 최신순 정렬
            }

            // 모델에 데이터 추가
            model.addAttribute("posts", posts);
            model.addAttribute("boardType", type.toString().toLowerCase()); // 소문자로 변환
            model.addAttribute("sort", sort); // 정렬 조건 추가
            model.addAttribute("filter", filter); // 검색 필터 추가
            model.addAttribute("keyword", keyword); // 검색 키워드 추가

            // 세션에서 user_id 및 user_name 가져오기
            String userIdStr = (String) session.getAttribute("user_id");
            String userName = (String) session.getAttribute("user_name");

            if (userIdStr == null) userIdStr = "guest";
            if (userName == null || userName.isEmpty()) userName = "guest";

            model.addAttribute("user_id", userIdStr);
            model.addAttribute("user_name", userName);

            // 게시판 템플릿 반환
            return "board/" + type.toString().toLowerCase(); // 예: board/free
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 boardType 예외 처리
            model.addAttribute("error", "잘못된 게시판 유형입니다.");
            return "error/404";
        }
    }

    // 필터 적용 메서드
    private boolean applyFilter(Post post, String filter, String keyword) {
        switch (filter) {
            case "titleContent":
                return (post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                        || (post.getContent() != null && post.getContent().toLowerCase().contains(keyword.toLowerCase()));
            case "title":
                return post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword.toLowerCase());
            case "content":
                return post.getContent() != null && post.getContent().toLowerCase().contains(keyword.toLowerCase());
            case "author":
                return post.getAuthor() != null && post.getAuthor().toLowerCase().contains(keyword.toLowerCase());
            default:
                return true;
        }
    }

    @GetMapping("/board/write")
    public String showWriteForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("user_id"); // 세션에서 로그인된 사용자 ID 가져오기

        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 글쓰기가 가능합니다.");
            return "redirect:/index"; // 로그인하지 않았으면 로그인 페이지로 리디렉션
        }

        model.addAttribute("username", session.getAttribute("user_name")); // 작성자 정보 전달
        return "board/write"; // write.html 반환
    }

    @PostMapping("/board/write")
    public String writePost(@RequestParam String title,
                            @RequestParam String content,
                            @RequestParam BoardType boardType, // 게시판 종류 추가
                            HttpSession session) {

        String userId = (String) session.getAttribute("user_id");
        String userName = (String) session.getAttribute("user_name");

        Post newPost = new Post();
        newPost.setTitle(title);
        newPost.setContent(content);
        newPost.setAuthor(userName);
        newPost.setUserId(userId); // userId를 Post 객체에 설정
        newPost.setBoardType(boardType); // 게시판 종류 저장

        postService.save(newPost); // DB 저장

        // 글 작성 후 해당 게시판으로 리다이렉트
        return "redirect:/board/" + boardType.toString().toLowerCase(); // 예: board/free, board/question 등
    }

    @GetMapping("/board/{boardType}/{id}")
    public String showPost(@PathVariable String boardType, @PathVariable Long id, Model model, HttpSession session) {
        boardType = boardType.toLowerCase();

        // 조회수 증가 기능 추가
        postService.incrementViews(id); // 게시글 조회수 증가

        Post post = postService.getPostById(id);

        if (post == null) {
            return "error/404";
        }

        model.addAttribute("post", post);
        String username = (String) session.getAttribute("user_name");
        model.addAttribute("user_name", username != null ? username : "guest");

        return "board/post";
    }
}
