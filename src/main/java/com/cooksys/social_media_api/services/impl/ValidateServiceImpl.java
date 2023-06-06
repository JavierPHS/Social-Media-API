package com.cooksys.social_media_api.services.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cooksys.social_media_api.entities.User;
import com.cooksys.social_media_api.repositories.HashtagRepository;
import com.cooksys.social_media_api.repositories.UserRepository;
import com.cooksys.social_media_api.services.ValidateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateServiceImpl implements ValidateService {

	private final UserRepository userRepository;
	private final HashtagRepository hashtagRepository;

	@Override
	public Boolean validateUsername(String username) {
		Optional<User> user = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		return user.isPresent();
	}

	@Override
	public Boolean validateHashtag(String label) {
		return hashtagRepository.findByLabel(label).isPresent();
	}
}
