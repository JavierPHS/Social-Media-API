package com.cooksys.social_media_api.services.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cooksys.social_media_api.dtos.CredentialsDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;
import com.cooksys.social_media_api.dtos.UserRequestDto;
import com.cooksys.social_media_api.dtos.UserResponseDto;
import com.cooksys.social_media_api.entities.Profile;
import com.cooksys.social_media_api.entities.Tweet;
import com.cooksys.social_media_api.entities.User;
import com.cooksys.social_media_api.exceptions.BadRequestException;
import com.cooksys.social_media_api.exceptions.NotAuthorizedException;
import com.cooksys.social_media_api.exceptions.NotFoundException;
import com.cooksys.social_media_api.mappers.ProfileMapper;
import com.cooksys.social_media_api.mappers.TweetMapper;
import com.cooksys.social_media_api.mappers.UserMapper;
import com.cooksys.social_media_api.repositories.TweetRepository;
import com.cooksys.social_media_api.repositories.UserRepository;
import com.cooksys.social_media_api.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserMapper userMapper;
	private final UserRepository userRepository;
	private final TweetRepository tweetRepository;
	private final TweetMapper tweetMapper;
	private final ProfileMapper profileMapper;

	private User getUserByUsername(String username) {
		Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (optionalUser.isEmpty() || optionalUser.get().isDeleted()) {
			throw new NotFoundException("The specified user does not exist");
		}
		return optionalUser.get();
	}

	private User getByCredentials(CredentialsDto credentialsDto) {
		if (credentialsDto == null || credentialsDto.getUsername() == null || credentialsDto.getPassword() == null) {
			throw new NotAuthorizedException("Credentials are required");
		}
		User userToCheck = getUserByUsername(credentialsDto.getUsername());
		if (!userToCheck.getCredentials().getPassword().equals(credentialsDto.getPassword())) {
			throw new NotAuthorizedException("Password is invalid");
		}
		return userToCheck;
	}

	@Override
	public UserResponseDto createUser(UserRequestDto userRequestDto) {
		if (userRequestDto == null || userRequestDto.getCredentials() == null || userRequestDto.getProfile() == null) {
			throw new BadRequestException("You must include a username, password, and at least an email");
		}
		User userToSave = userMapper.requestDtoToEntity(userRequestDto);

		if (userToSave.getCredentials().getPassword() == null || userToSave.getCredentials().getUsername() == null
				|| userToSave.getProfile().getEmail() == null) {
			throw new BadRequestException("Credentials must include username and password and at least an email");
		}
		Optional<User> optionalUser = userRepository
				.findByCredentialsUsername(userToSave.getCredentials().getUsername());
		if (optionalUser.isPresent()) {
			User existingUser = optionalUser.get();
			if (!existingUser.isDeleted()) {
				throw new BadRequestException("That username already exists, Please choose another username.");
			}
			existingUser.setDeleted(false);
			existingUser.setProfile(userToSave.getProfile());
			for (Tweet tweet : existingUser.getTweets()) {
				tweet.setDeleted(false);
			}
			userRepository.saveAndFlush(existingUser);
			tweetRepository.saveAllAndFlush(existingUser.getTweets());
			return userMapper.entityToDto(existingUser);
		} else {
			userToSave.setJoined(Timestamp.valueOf(LocalDateTime.now()));
			userRepository.saveAndFlush(userToSave);
			return userMapper.entityToDto(userToSave);
		}
	}

	@Override
	public UserResponseDto getUser(String username) {
		User user = getUserByUsername(username);
		return userMapper.entityToDto(user);
	}

	@Override
	public UserResponseDto updateUser(UserRequestDto userRequestDto, String username) {
		if (userRequestDto == null || userRequestDto.getCredentials() == null || userRequestDto.getProfile() == null) {
			throw new BadRequestException("Must provide user credentials and profile to update");
		}
		User userToUpdate = getByCredentials(userRequestDto.getCredentials());

		Profile profileToUpdate = profileMapper.dtoToEntity(userRequestDto.getProfile());
		if (profileToUpdate.getEmail() != null) {
			userToUpdate.getProfile().setEmail(profileToUpdate.getEmail());
		}
		if (profileToUpdate.getFirstName() != null) {
			userToUpdate.getProfile().setFirstName(profileToUpdate.getFirstName());
		}
		if (profileToUpdate.getLastName() != null) {
			userToUpdate.getProfile().setLastName(profileToUpdate.getLastName());
		}
		if (profileToUpdate.getPhone() != null) {
			userToUpdate.getProfile().setPhone(profileToUpdate.getPhone());
		}
		userRepository.saveAndFlush(userToUpdate);

		return userMapper.entityToDto(userToUpdate);
	}

	@Override
	public UserResponseDto deleteUser(CredentialsDto credentialsDto, String username) {
		User userToDelete = getByCredentials(credentialsDto);

		if (userToDelete.getCredentials().getUsername().equals(username)) {
			for (Tweet tweet : userToDelete.getTweets()) {
				tweet.setDeleted(true);
				tweetRepository.saveAndFlush(tweet);
			}
			userToDelete.setDeleted(true);
			userRepository.saveAndFlush(userToDelete);
		} else {
			throw new NotAuthorizedException("The provided credentials do not match the user to be deleted");
		}
		return userMapper.entityToDto(userToDelete);
	}

	@Override
	public List<UserResponseDto> getFollowers(String username) {
		try {
			User user = userRepository.findByCredentialsUsername(username).get();
			List<User> followers = user.getFollowers();
			for (User u : followers) {
				if (u.isDeleted())
					followers.remove(u);
			}
			return userMapper.entitiesToDtos(followers);
		} catch (Exception e) {
			throw new NotFoundException(username + " is either deleted or does not exist.");
		}
	}

	@Override
	public List<UserResponseDto> getFollowing(String username) {
		try {
			User user = userRepository.findByCredentialsUsername(username).get();
			List<User> following = user.getFollowing();
			for (User u : following) {
				if (u.isDeleted())
					following.remove(u);
			}
			return userMapper.entitiesToDtos(following);
		} catch (Exception e) {
			throw new NotFoundException(username + " is either deleted or does not exist.");
		}
	}

	@Override
	public List<TweetResponseDto> getMentions(String username) {
		User queriedUser = getUserByUsername(username);
		List<Tweet> userMentions = queriedUser.getMentionedTweets();
		return tweetMapper.entitiesToDtos(userMentions);
	}

	@Override
	public List<UserResponseDto> getUsers() {
		return userMapper.entitiesToDtos(userRepository.findAllByDeletedIsFalse());
	}

	@Override
	public void followUser(String username, CredentialsDto credentialsDto) {
		if (credentialsDto.getPassword() == null) {
			throw new BadRequestException("The password is required");
		}
		User userToFollow = getUserByUsername(username);
		User lemming = getUserByUsername(credentialsDto.getUsername());
		if (userToFollow.getFollowers().contains(lemming)) {
			throw new BadRequestException("You are already following this user");
		}
		userToFollow.getFollowers().add(lemming);
		lemming.getFollowing().add(userToFollow);
		userRepository.saveAndFlush(userToFollow);
		userRepository.saveAndFlush(lemming);
	}

	@Override
	public void unfollowUser(String username, CredentialsDto credentialsDto) {
		if (credentialsDto.getPassword() == null) {
			throw new BadRequestException("You must include a username and password");
		}
		User userFollowing = getUserByUsername(username);
		User noLongerALemming = getUserByUsername(credentialsDto.getUsername());
		if (!userFollowing.getFollowers().contains(noLongerALemming)) {
			throw new BadRequestException("You are not currently following this user");
		}
		userFollowing.getFollowers().remove(noLongerALemming);
		noLongerALemming.getFollowing().remove(userFollowing);
		userRepository.saveAndFlush(userFollowing);
		userRepository.saveAndFlush(noLongerALemming);
	}

	@Override
	public List<TweetResponseDto> getTweetsByUsername(String username) {
		try {
			User user = userRepository.findByCredentialsUsernameAndDeletedFalse(username).get();
			List<Tweet> tweets = user.getTweets();
			for (Tweet t : tweets) {
				if (t.isDeleted()) {
					tweets.remove(t);
				}
			}
			Collections.reverse(tweets);
			return tweetMapper.entitiesToDtos(tweets);
		} catch (Exception e) {
			throw new NotFoundException(username + " is either deleted or does not exist.");
		}
	}
}