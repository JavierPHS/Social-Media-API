package com.cooksys.social_media_api.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cooksys.social_media_api.dtos.HashtagDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;
import com.cooksys.social_media_api.entities.Hashtag;
import com.cooksys.social_media_api.exceptions.BadRequestException;
import com.cooksys.social_media_api.mappers.HashtagMapper;
import com.cooksys.social_media_api.mappers.TweetMapper;
import com.cooksys.social_media_api.repositories.HashtagRepository;
import com.cooksys.social_media_api.services.HashtagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HashtagServiceImpl implements HashtagService {
	private final HashtagRepository hashtagRepository;
	private final HashtagMapper hashtagMapper;
	private final TweetMapper tweetMapper;

	@Override
	public List<HashtagDto> getHashtags() {
		return hashtagMapper.entitiesToDtos(hashtagRepository.findAll());
	}

	@Override
	public List<TweetResponseDto> getTaggedTweets(String label) {
		Optional<Hashtag> hashtagOptional = hashtagRepository.findByLabel(label);
		if (hashtagOptional.isEmpty())
			throw new BadRequestException("Invalid label");
		return tweetMapper.entitiesToDtos(
				hashtagOptional.get().getTweets().stream().filter(t -> !t.isDeleted()).collect(Collectors.toList()));
	}
}
