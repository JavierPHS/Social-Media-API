package com.cooksys.social_media_api.services;

import java.util.List;

import com.cooksys.social_media_api.dtos.HashtagDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;

public interface HashtagService {

	List<HashtagDto> getHashtags();

	List<TweetResponseDto> getTaggedTweets(String label);
}
