package com.cooksys.social_media_api.services.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cooksys.social_media_api.dtos.ContextDto;
import com.cooksys.social_media_api.dtos.CredentialsDto;
import com.cooksys.social_media_api.dtos.HashtagDto;
import com.cooksys.social_media_api.dtos.TweetRequestDto;
import com.cooksys.social_media_api.dtos.TweetResponseDto;
import com.cooksys.social_media_api.dtos.UserResponseDto;
import com.cooksys.social_media_api.entities.Hashtag;
import com.cooksys.social_media_api.entities.Tweet;
import com.cooksys.social_media_api.entities.User;
import com.cooksys.social_media_api.exceptions.BadRequestException;
import com.cooksys.social_media_api.exceptions.NotAuthorizedException;
import com.cooksys.social_media_api.exceptions.NotFoundException;
import com.cooksys.social_media_api.mappers.HashtagMapper;
import com.cooksys.social_media_api.mappers.TweetMapper;
import com.cooksys.social_media_api.mappers.UserMapper;
import com.cooksys.social_media_api.repositories.HashtagRepository;
import com.cooksys.social_media_api.repositories.TweetRepository;
import com.cooksys.social_media_api.repositories.UserRepository;
import com.cooksys.social_media_api.services.TweetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TweetServiceImpl implements TweetService {
	private final TweetRepository tweetRepository;
	private final TweetMapper tweetMapper;
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final HashtagMapper hashtagMapper;
	private final HashtagRepository hashtagRepository;

	private User getUserByUsername(String username) {
		Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
		if (optionalUser.isEmpty()) {
			throw new NotFoundException("No user found.");
		}
		return optionalUser.get();
	}

	private User getUserByCredentials(CredentialsDto credentialsDto) {
		if (credentialsDto == null || credentialsDto.getUsername() == null || credentialsDto.getPassword() == null) {
			throw new NotAuthorizedException("Credentials are required");
		}
		User userToCheck = getUserByUsername(credentialsDto.getUsername());
		if (!userToCheck.getCredentials().getPassword().equals(credentialsDto.getPassword())) {
			throw new NotAuthorizedException("Password is invalid");
		}
		return userToCheck;
	}

	private Tweet validateAndGetTweetById(Long id) {
		Optional<Tweet> optionalTweet = tweetRepository.findByIdAndDeletedFalse(id);
		if (optionalTweet.isEmpty()) {
			throw new NotFoundException("Tweet could not be found");
		}
		return optionalTweet.get();
	}

	private void parseHashtagsAndMentions(Tweet tweet) {
		String[] wordsInContent = tweet.getContent().split("\\s+");
		for (String word : wordsInContent) {
			if (word.startsWith("#")) {
				String tag = word.substring(1);
				Optional<Hashtag> optionalHashtag = hashtagRepository.findByLabel(tag);
				if (optionalHashtag.isEmpty()) {
					Hashtag hashtag = new Hashtag();
					hashtag.setLabel(tag);
					hashtag.setFirstUsed(Timestamp.valueOf(LocalDateTime.now()));
					hashtagRepository.saveAndFlush(hashtag);
					tweet.getHashtags().add(hashtag);
					tweetRepository.saveAndFlush(tweet);
				} else {
					optionalHashtag.get().setLastUsed(Timestamp.valueOf(LocalDateTime.now()));
					hashtagRepository.saveAndFlush(optionalHashtag.get());
				}
			}
			if (word.startsWith("@")) {
				String username = word.substring(1);
				Optional<User> optionalUser = userRepository.findByCredentialsUsernameAndDeletedFalse(username);
				if (optionalUser.isPresent()) {
					tweet.getMentionedUsers().add(optionalUser.get());
					tweetRepository.saveAndFlush(tweet);
				}
			}
		}
	}

	@Override
	public List<TweetResponseDto> getTweets() {
		List<Tweet> tweets = tweetRepository.findAllByDeletedIsFalse();
		Collections.reverse(tweets);
		return tweetMapper.entitiesToDtos(tweets);
	}

	@Override
	public TweetResponseDto postTweet(TweetRequestDto tweetRequestDto) {
		if (tweetRequestDto == null || tweetRequestDto.getContent() == null
				|| tweetRequestDto.getCredentials() == null) {
			throw new BadRequestException("The content and credentials are required fields");
		}
		User author = getUserByCredentials(tweetRequestDto.getCredentials());
		Tweet tweetToSave = new Tweet();
		tweetToSave.setAuthor(author);
		tweetToSave.setContent(tweetRequestDto.getContent());
		parseHashtagsAndMentions(tweetToSave);
		return tweetMapper.entityToDto(tweetRepository.saveAndFlush(tweetToSave));
	}

	@Override
	public TweetResponseDto replyToTweet(Long id, TweetRequestDto tweetRequestDto) {
		if (tweetRequestDto == null || tweetRequestDto.getContent() == null
				|| tweetRequestDto.getCredentials() == null) {
			throw new BadRequestException("The content and credentials are required fields");
		}
		User author = getUserByCredentials(tweetRequestDto.getCredentials());
		Tweet tweetToSave = new Tweet();
		tweetToSave.setInReplyTo(validateAndGetTweetById(id));
		tweetToSave.setAuthor(author);
		tweetToSave.setContent(tweetRequestDto.getContent());
		parseHashtagsAndMentions(tweetToSave);
		return tweetMapper.entityToDto(tweetRepository.saveAndFlush(tweetToSave));
	}

	@Override
	public TweetResponseDto repostTweet(Long id, CredentialsDto credentialsDto) {
		Tweet tweetToRepost = validateAndGetTweetById(id);
		User author = getUserByCredentials(credentialsDto);
		Tweet repostedTweet = new Tweet();

		repostedTweet.setAuthor(author);
		repostedTweet.setRepostOf(tweetToRepost);

		return tweetMapper.entityToDto(tweetRepository.saveAndFlush(repostedTweet));
	}

	@Override
	public TweetResponseDto getTweet(Long id) {
		Optional<Tweet> optionalTweet = tweetRepository.findByIdAndDeletedFalse(id);
		if (optionalTweet.isEmpty()) {
			throw new NotFoundException("That tweet is deleted or doesn't exist.");
		}
		return tweetMapper.entityToDto(optionalTweet.get());
	}

	@Override
	public TweetResponseDto deleteTweet(Long id, CredentialsDto credentialsDto) {
		getUserByCredentials(credentialsDto);
		Tweet queriedTweet = validateAndGetTweetById(id);
		queriedTweet.setDeleted(true);
		tweetRepository.saveAndFlush(queriedTweet);
		return tweetMapper.entityToDto(queriedTweet);
	}

	@Override
	public void likeTweet(Long id, CredentialsDto credentialsDto) {
		Optional<Tweet> optionalTweet = tweetRepository.findById(id);
		if (optionalTweet.isEmpty()) {
			throw new NotFoundException("The specified tweet does not exist.");
		}
		Tweet tweetToLike = optionalTweet.get();
		if (tweetToLike.isDeleted()) {
			throw new BadRequestException("The specified tweet appears to have been deleted");
		}
		User currentUser = getUserByUsername(credentialsDto.getUsername());
		if (tweetToLike.getLikedByUsers().contains(currentUser)) {
			return;
		}
		tweetToLike.getLikedByUsers().add(currentUser);
		currentUser.getLikedTweets().add(tweetToLike);
		tweetRepository.saveAndFlush(tweetToLike);
		userRepository.saveAndFlush(currentUser);
	}

	@Override
	public ContextDto getContext(Long id) {
		Tweet targetTweet = validateAndGetTweetById(id);
		List<Tweet> tweetsBefore = new ArrayList<>();
		List<Tweet> tweetsAfter = new ArrayList<>();

		List<Tweet> replies = targetTweet.getReplies();
		for (Tweet t : replies) {
			if (!t.isDeleted()) {
				tweetsAfter.add(t);
			}
			replies.addAll(t.getReplies());
		}

		for (Tweet inReplyTo = targetTweet.getInReplyTo(); inReplyTo != null; inReplyTo = inReplyTo.getInReplyTo()) {
			if (!inReplyTo.isDeleted()) {
				tweetsBefore.add(inReplyTo);
			}
		}

		ContextDto contextDto = new ContextDto();
		contextDto.setTarget(tweetMapper.entityToDto(targetTweet));
		contextDto.setBefore(tweetMapper.entitiesToDtos(tweetsBefore));
		contextDto.setAfter(tweetMapper.entitiesToDtos(tweetsAfter));

		return contextDto;
	}

	@Override
	public List<HashtagDto> getHashtags(Long id) {
		Tweet tweet = validateAndGetTweetById(id);
		List<Hashtag> hashtags = tweet.getHashtags();
		return hashtagMapper.entitiesToDtos(hashtags);
	}

	@Override
	public List<TweetResponseDto> getReposts(Long id) {
		Tweet tweet = getTweetById(id);
		return tweetMapper.entitiesToDtos(activeTweets(tweet.getReposts()));
	}

	private List<Tweet> activeTweets(Collection<Tweet> tweets) {
		return tweets.stream().filter(t -> !t.isDeleted()).collect(Collectors.toList());
	}

	@Override
	public List<UserResponseDto> getLikes(Long id) {
		Tweet tweet = validateAndGetTweetById(id);
		List<User> likers = tweet.getLikedByUsers();
		return userMapper.entitiesToDtos(likers.stream().filter(u -> !u.isDeleted()).collect(Collectors.toList()));
	}

//	Retrieves the users mentioned in the tweet with the given id. If that tweet is deleted or otherwise 
//	doesn't exist, an error should be sent in lieu of a response.
//	Deleted users should be excluded from the response.
//	IMPORTANT Remember that tags and mentions must be parsed by the server!

	@Override
	public List<UserResponseDto> getUsersMentioned(Long id) {
//		Tweet tweet = getTweetById(id);
//		return userMapper.entitiesToDtos(
//				tweet.getMentionedUsers().stream().filter(u -> !u.isDeleted()).collect(Collectors.toList()));
		Tweet tweet = validateAndGetTweetById(id);
		List<User> mentionedUsers = tweet.getMentionedUsers().stream().filter(user -> !user.isDeleted())
				.collect(Collectors.toList());

		return userMapper.entitiesToDtos(mentionedUsers);
	}

	private Tweet getTweetById(Long id) {
		Optional<Tweet> tweetOptional = tweetRepository.findById(id);
		if (tweetOptional.isEmpty() || tweetOptional.get().isDeleted())
			throw new BadRequestException("Tweet not found");
		return tweetOptional.get();
	}

	@Override
	public List<TweetResponseDto> getUserFeed(String username) {
		User user = getUserByUsername(username);
		if (user == null || user.isDeleted())
			throw new NotFoundException("User not found");

		Queue<User> userQueue = new LinkedList<>();

		userQueue.add(user);
		userQueue.addAll(user.getFollowing());
		Set<Tweet> tweetSet = new HashSet<>();

		while (!userQueue.isEmpty()) {
			User author = userQueue.poll();
			if (!author.isDeleted()) {
				tweetSet.addAll(author.getTweets().stream().filter(t -> !t.isDeleted()).collect(Collectors.toList()));
			}
		}
		List<Tweet> response = new ArrayList<>(tweetSet);
		response.sort(Comparator.comparing(Tweet::getPosted));
		Collections.reverse(response);
		return tweetMapper.entitiesToDtos(response);
	}

	@Override
	public List<TweetResponseDto> getRepliesById(Long id) {
		Tweet tweet = getTweetById(id);
		return tweetMapper.entitiesToDtos(activeTweets(tweet.getReplies()));
	}
}
