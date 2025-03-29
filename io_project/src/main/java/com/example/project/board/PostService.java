package com.example.project.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
	
	@Autowired
    PostRepository postRepository;
    
	public Post getPostById(Long id) {
		return postRepository.findById(id).orElse(null); // ID로 게시물 조회
	}
	
    public List<Post> getPostsByBoardType(BoardType boardType) {
        return postRepository.findByBoardType(boardType);
    }
    
    public Post save(Post post) {
        return postRepository.save(post);  // JPA의 save() 메소드를 사용하여 DB에 저장
    }
    
    public void incrementViews(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setViews(post.getViews() + 1);  // 현재 views에 1을 더해줌
        postRepository.save(post);  // 업데이트된 Post를 저장
    }
}
