package com.cooksys.social_media_api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksys.social_media_api.services.ValidateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/validate")
public class ValidateController {
	private final ValidateService validateService;

	@GetMapping("/username/exists/@{username}")
	public Boolean validateUsername(@PathVariable String username) {
		return validateService.validateUsername(username);
	}

	// Check if user-name is available
	@GetMapping("/username/available/@{username}")
	public Boolean usernameAvailable(@PathVariable String username) {
		return !validateService.validateUsername(username);
	}

	// Check whether or not a given hash-tag exists
	@GetMapping("/tag/exists/{label}")
	public Boolean validateHashtag(@PathVariable String label) {
		return validateService.validateHashtag(label);
	}

}
