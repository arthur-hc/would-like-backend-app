package com.wouldlikeapp.backend.modules.user.infrastructure.mapper;

import com.wouldlikeapp.backend.modules.user.domain.entity.User;
import com.wouldlikeapp.backend.modules.user.infrastructure.entity.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface IUserJpaMapper {
  User jpaToDomain(UserJpaEntity entity);
  UserJpaEntity domainToJpa(User domain);
}
