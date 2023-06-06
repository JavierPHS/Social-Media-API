package com.cooksys.social_media_api.mappers;

import org.mapstruct.Mapper;

import com.cooksys.social_media_api.dtos.ProfileDto;
import com.cooksys.social_media_api.entities.Profile;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
	Profile dtoToEntity(ProfileDto dto);
}
