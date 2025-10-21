package com.wouldlikeapp.backend.modules.user.api.controller;

import com.wouldlikeapp.backend.modules.user.application.useCase.RegisterUserUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterUserController {

  RegisterUserUseCase registerUserUseCase;

  RegisterUserController(
      RegisterUserUseCase registerUserUseCase
  ) {
    this.registerUserUseCase = registerUserUseCase;
  }

  @PostMapping("/user")
  public String handle() {
    return this.registerUserUseCase.execute();
  }
}
