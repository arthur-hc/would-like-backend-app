package com.wouldlikeapp.backend.modules.user.domain.interfaces;

import com.wouldlikeapp.backend.modules.user.domain.entity.User;
import java.util.List;
import java.util.Optional;

public interface IUserRepository {
  User saveUser(User user);
  Optional<User> findUserById(Long id);
  Optional<User> findUserByEmail(String email);
  List<User> findAllUsers();
  void deleteUser(Long id);
}
