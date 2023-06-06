package com.cooksys.social_media_api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cooksys.social_media_api.dtos.HashtagDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;
import com.cooksys.social_media_api.services.HashtagService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tags")
public class HashtagController {
	private final HashtagService hashtagService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<HashtagDto> getHashtags() {
		return hashtagService.getHashtags();
	}

	@GetMapping("/{label}")
	public List<TweetResponseDto> getTaggedTweets(@PathVariable String label) {
		return hashtagService.getTaggedTweets(label);
	}
}
