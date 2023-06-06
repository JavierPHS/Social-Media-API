package com.cooksys.social_media_api.mappers;

import org.mapstruct.Mapper;

import com.cooksys.social_media_api.dtos.CredentialsDto;
import com.cooksys.social_media_api.entities.Credentials;

@Mapper(componentModel = "spring")
public interface CredentialsMapper {
	CredentialsDto entityToDto(Credentials credentials);

	Credentials dtoToEntity(CredentialsDto dto);
}
