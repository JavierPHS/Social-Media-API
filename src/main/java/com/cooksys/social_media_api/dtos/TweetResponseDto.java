package com.cooksys.social_media_api.dtos;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TweetResponseDto {

	private Long id;

	private UserResponseDto author;

	private Timestamp posted;

	private String content;

	private TweetResponseDto inReplyTo;

	private TweetResponseDto repostOf;

	private List<UserResponseDto> mentionedUsers;

}
