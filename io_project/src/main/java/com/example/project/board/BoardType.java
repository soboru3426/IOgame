package com.example.project.board;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum BoardType {
	FREE,
	QUESTION,
	TIP,
	IMAGE
}
