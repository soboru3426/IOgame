package com.example.project.notice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
	List<Notice> findByTitleContainingIgnoreCase(String keyword);
}
