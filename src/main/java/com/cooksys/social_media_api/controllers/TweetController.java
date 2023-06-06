package com.cooksys.social_media_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksys.social_media_api.dtos.ContextDto;
import com.cooksys.social_media_api.dtos.CredentialsDto;
import com.cooksys.social_media_api.dtos.HashtagDto;
import com.cooksys.social_media_api.dtos.TweetRequestDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;
import com.cooksys.social_media_api.dtos.UserResponseDto;
import com.cooksys.social_media_api.services.TweetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tweets")
public class TweetController {

	private final TweetService tweetService;

	// Retrieves all non-deleted tweets. They should appear in reverse-chronological
	// order
	@GetMapping
	public List<TweetResponseDto> getTweets() {
		return tweetService.getTweets();
	}

	@PostMapping
	public TweetResponseDto postTweet(@RequestBody TweetRequestDto tweetRequestDto) {
		return tweetService.postTweet(tweetRequestDto);
	}

	// Retrieves a tweet with given id. If no such tweet exists, or is deleted,
	// throw error
	@GetMapping("/{id}")
	public TweetResponseDto getTweet(@PathVariable Long id) {
		return tweetService.getTweet(id);
	}

	// "Deletes" tweet with given id.
	@DeleteMapping("/{id}")
	public TweetResponseDto deleteTweet(@PathVariable Long id, @RequestBody CredentialsDto credentialsDto) {
		return tweetService.deleteTweet(id, credentialsDto);
	}

	// Creates a 'like' relationship between the tweet with the given id and the
	// user whose credentials are provided by the request body. If given tweet is
	// deleted or doesn't exist, or if credentials do not
	// match an active user in the DB, throw error. On success, no response body is
	// sent.
	@PostMapping("/{id}/like")
	public void likeTweet(@PathVariable Long id, @RequestBody CredentialsDto credentialsDto) {
		tweetService.likeTweet(id, credentialsDto);
		return;
	}

	@PostMapping("/{id}/reply")
	public TweetResponseDto replyToTweet(@PathVariable Long id, @RequestBody TweetRequestDto tweetRequestDto) {
		return tweetService.replyToTweet(id, tweetRequestDto);
	}

	@GetMapping("/{id}/replies")
	public List<TweetResponseDto> getRepliesById(@PathVariable Long id) {
		return tweetService.getRepliesById(id);
	}

	@PostMapping("/{id}/repost")
	public TweetResponseDto repostTweet(@PathVariable Long id, @RequestBody CredentialsDto credentialsDto) {
		return tweetService.repostTweet(id, credentialsDto);
	}

	@GetMapping("/{id}/context")
	public ContextDto getContext(@PathVariable Long id) {
		return tweetService.getContext(id);
	}

	@GetMapping("/{id}/tags")
	public List<HashtagDto> getHashtags(@PathVariable Long id) {
		return tweetService.getHashtags(id);
	}

	@GetMapping("/{id}/reposts")
	public List<TweetResponseDto> getReposts(@PathVariable Long id) {
		return tweetService.getReposts(id);
	}

	@GetMapping("/{id}/likes")
	public List<UserResponseDto> getLikes(@PathVariable Long id) {
		return tweetService.getLikes(id);
	}

	@GetMapping("/{id}/mentions")
	public List<UserResponseDto> getUsersMentioned(@PathVariable Long id) {
		return tweetService.getUsersMentioned(id);
	}
}
