package com.cooksys.social_media_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TweetRequestDto {

	private String content;

	private CredentialsDto credentials;
}
