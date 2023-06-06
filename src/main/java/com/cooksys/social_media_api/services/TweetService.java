package com.cooksys.social_media_api.services;

import java.util.List;

import com.cooksys.social_media_api.dtos.ContextDto;
import com.cooksys.social_media_api.dtos.CredentialsDto;
import com.cooksys.social_media_api.dtos.HashtagDto;
import com.cooksys.social_media_api.dtos.TweetRequestDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;
import com.cooksys.social_media_api.dtos.UserResponseDto;

public interface TweetService {

	List<TweetResponseDto> getTweets();

	TweetResponseDto postTweet(TweetRequestDto tweetRequestDto);

	TweetResponseDto getTweet(Long id);

	TweetResponseDto deleteTweet(Long id, CredentialsDto credentialsDto);

	void likeTweet(Long id, CredentialsDto credentialsDto);

	TweetResponseDto replyToTweet(Long id, TweetRequestDto tweetRequestDto);

	ContextDto getContext(Long id);

	List<HashtagDto> getHashtags(Long id);

	List<TweetResponseDto> getReposts(Long id);

	List<UserResponseDto> getLikes(Long id);

	TweetResponseDto repostTweet(Long id, CredentialsDto credentialsDto);

	List<UserResponseDto> getUsersMentioned(Long id);

	List<TweetResponseDto> getUserFeed(String username);

	List<TweetResponseDto> getRepliesById(Long id);

}
