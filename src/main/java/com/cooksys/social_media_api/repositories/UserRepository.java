package com.cooksys.social_media_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cooksys.social_media_api.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByCredentialsUsernameAndDeletedFalse(String username);

	boolean existsByCredentialsUsernameAndDeletedTrue(String username);

	Optional<User> findByCredentialsUsername(String username);

	List<User> findAllByDeletedIsFalse();

}
