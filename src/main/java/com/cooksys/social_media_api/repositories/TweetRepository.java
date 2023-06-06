package com.cooksys.social_media_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooksys.social_media_api.entities.Tweet;

@Repository
public interface TweetRepository extends JpaRepository<Tweet, Long> {

	List<Tweet> findAllByDeletedIsFalse();

	Optional<Tweet> findByIdAndDeletedFalse(Long id);

	boolean existsByIdAndAuthor(Long id, String username);

	boolean existsByIdAndDeletedIsFalse(Long id);
}
