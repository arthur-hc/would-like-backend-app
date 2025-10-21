package com.wouldlikeapp.backend.modules.user.infrastructure.repository;

import com.wouldlikeapp.backend.modules.user.domain.entity.User;
import com.wouldlikeapp.backend.modules.user.domain.interfaces.IUserRepository;
import com.wouldlikeapp.backend.modules.user.infrastructure.entity.UserJpaEntity;
import com.wouldlikeapp.backend.modules.user.infrastructure.mapper.IUserJpaMapper;
import com.wouldlikeapp.backend.modules.user.infrastructure.repository.interfaces.IUserJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImplementation implements IUserRepository {

  private final IUserJpaRepository userJpaRepository;
  private final IUserJpaMapper userJpaMapper;

  public UserRepositoryImplementation(
      IUserJpaRepository userJpaRepository,
      IUserJpaMapper userJpaMapper
  ) {
    this.userJpaRepository = userJpaRepository;
    this.userJpaMapper = userJpaMapper;
  }

  @Override
  public User saveUser(User user) {
    UserJpaEntity entity = userJpaMapper.domainToJpa(user);
    return userJpaMapper.jpaToDomain(userJpaRepository.save(entity));
  }

  @Override
  public Optional<User> findUserById(Long id) {
    return userJpaRepository.findById(id).map(userJpaMapper::jpaToDomain);
  }

  @Override
  public Optional<User> findUserByEmail(String email) {
    return userJpaRepository.findByEmail(email).map(userJpaMapper::jpaToDomain);
  }

  @Override
  public List<User> findAllUsers() {
    return userJpaRepository.findAll()
        .stream()
        .map(userJpaMapper::jpaToDomain)
        .toList();
  }

  @Override
  public void deleteUser(Long id) {
    userJpaRepository.deleteById(id);
  }
}
