package com.cooksys.social_media_api.services;

import java.util.List;

import com.cooksys.social_media_api.dtos.CredentialsDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;
import com.cooksys.social_media_api.dtos.UserRequestDto;
import com.cooksys.social_media_api.dtos.UserResponseDto;

public interface UserService {

	UserResponseDto createUser(UserRequestDto userRequestDto);

	List<UserResponseDto> getFollowers(String username);

	List<UserResponseDto> getFollowing(String username);

	UserResponseDto getUser(String username);

	List<TweetResponseDto> getMentions(String username);

	UserResponseDto updateUser(UserRequestDto userRequestDto, String username);

	UserResponseDto deleteUser(CredentialsDto credentialsDto, String username);

	List<UserResponseDto> getUsers();

	void followUser(String username, CredentialsDto credentials);

	void unfollowUser(String username, CredentialsDto credentials);

	List<TweetResponseDto> getTweetsByUsername(String username);

}
