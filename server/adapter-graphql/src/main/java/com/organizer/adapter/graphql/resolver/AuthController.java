package com.organizer.adapter.graphql.resolver;

import com.organizer.adapter.graphql.context.AuthContext;
import com.organizer.adapter.graphql.dto.SessionCreateInput;
import com.organizer.adapter.graphql.dto.SessionCreatePayload;
import com.organizer.adapter.graphql.dto.SessionRefreshInput;
import com.organizer.adapter.graphql.dto.SessionRefreshPayload;
import com.organizer.adapter.graphql.dto.User;
import com.organizer.adapter.graphql.dto.UserRegisterInput;
import com.organizer.adapter.graphql.dto.UserRegisterPayload;
import com.organizer.adapter.graphql.error.UseCaseInvoker;
import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.application.user.GetCurrentUserUseCase;
import com.organizer.application.user.LoginUseCase;
import com.organizer.application.user.RefreshTokenUseCase;
import com.organizer.application.user.RegisterUserUseCase;
import com.organizer.coreconfig.error.NotFoundException;
import com.organizer.coreconfig.error.UnauthenticatedException;
import com.organizer.coreconfig.id.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final GetCurrentUserUseCase getCurrentUserUseCase;
  private final UseCaseInvoker useCaseInvoker;

  public AuthController(
      RegisterUserUseCase registerUserUseCase,
      LoginUseCase loginUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      GetCurrentUserUseCase getCurrentUserUseCase,
      UseCaseInvoker useCaseInvoker) {
    this.registerUserUseCase = registerUserUseCase;
    this.loginUseCase = loginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.getCurrentUserUseCase = getCurrentUserUseCase;
    this.useCaseInvoker = useCaseInvoker;
  }

  @MutationMapping
  public UserRegisterPayload userRegister(@Argument UserRegisterInput input) {
    return UserRegisterPayload.from(
        useCaseInvoker.invoke(() -> registerUserUseCase.execute(input.toCommand())));
  }

  @MutationMapping
  public SessionCreatePayload sessionCreate(@Argument SessionCreateInput input) {
    return SessionCreatePayload.from(
        useCaseInvoker.invoke(() -> loginUseCase.execute(input.toCommand())));
  }

  @MutationMapping
  public SessionRefreshPayload sessionRefresh(@Argument SessionRefreshInput input) {
    return SessionRefreshPayload.from(
        useCaseInvoker.invoke(() -> refreshTokenUseCase.execute(input.toCommand())));
  }

  @QueryMapping
  public User me(
      @ContextValue(name = AuthContext.PRINCIPAL_KEY, required = false)
          AuthenticatedUser principal) {
    UserId userId = AuthContext.requireUserId(principal);
    try {
      return User.from(getCurrentUserUseCase.execute(userId));
    } catch (NotFoundException deletedSinceTokenWasIssued) {
      throw new UnauthenticatedException("Authentication required");
    }
  }
}
