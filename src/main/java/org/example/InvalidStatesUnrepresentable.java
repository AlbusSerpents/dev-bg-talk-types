package org.example;

import static lombok.AccessLevel.PRIVATE;
import static org.example.InvalidStatesUnrepresentable.UserType.CUSTOMER_ADMIN;
import static org.example.InvalidStatesUnrepresentable.UserType.SYSTEM_ADMIN;
import static org.example.InvalidStatesUnrepresentable.UserType.USER;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

public class InvalidStatesUnrepresentable {

//  @TODO multiple user types: system admin, customer admin, normal user

  public enum UserType {
    SYSTEM_ADMIN, CUSTOMER_ADMIN, USER
  }

  public record UserV1(
      UUID id,
      String email,
      String username,
      UserType type,
      List<String> adminPrivileges, // null if normal user
      String nuclearCode, // can only exist in system admin
      UUID customerId // customer admin & normal user have it
  ) {

  }

  @Getter
  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor(access = PRIVATE)
  public static final class UserV2 {

    private final UUID id;
    private final String email;
    private final String username;
    private final UserType type;
    private final List<String> adminPrivileges;
    private final String nuclearCode;
    private final UUID customerId;

    public static UserV2 normalUser(
        final UUID id,
        final String email,
        final String username,
        final UUID customerId) {
      return new UserV2(id, email, username, USER, null, null, customerId);
    }

    public static UserV2 customerAdinUser(
        final UUID id,
        final String email,
        final String username,
        final List<String> adminPrivileges,
        final UUID customerId) {
      return new UserV2(id, email, username, CUSTOMER_ADMIN, adminPrivileges, null, customerId);
    }

    public static UserV2 systemAdmin(
        final UUID id,
        final String email,
        final String username,
        final List<String> adminPrivileges,
        final String nuclearCode) {
      return new UserV2(id, email, username, SYSTEM_ADMIN, adminPrivileges, nuclearCode, null);
    }
  }

  //  ==================================================================================

  public void printUsername(final UserV2 user) {
    System.out.println(user.getUsername());
  }

  public void doSomethingAsSuperAdmin(final UserV2 user) {
    if (user.getType() == SYSTEM_ADMIN) {
      final String code = user.getNuclearCode();

      nextMethod(user);
//      do the thing
    }
  }

  //  This should be called only with SYSTEM_ADMIN
  private void nextMethod(final UserV2 user) {
    if (user.getType() != SYSTEM_ADMIN) {
      throw new RuntimeException("This shouldn't happen");
    }

//    do something
  }

  public sealed interface UserV3 permits BasicUser, CustomerAdmin, SystemAdmin {

    UUID id();

    String email();

    String username();
  }

  public record BasicUser(UUID id, String email, String username, UUID customerId) implements
      UserV3 {

  }

  public record CustomerAdmin(
      UUID id,
      String email,
      String username,
      List<String> adminPrivileges,
      UUID customerId) implements
      UserV3 {

  }

  public record SystemAdmin(
      UUID id,
      String email,
      String username,
      List<String> adminPrivileges,
      String nuclearCode) implements
      UserV3 {

  }

  public void printUsernameV3(final UserV3 user) {
    System.out.println(user.username());
  }

  public void doSomethingAsSuperAdmin(final UserV3 user) {
    final Optional<String> result = switch (user) {
      case SystemAdmin admin -> admin.adminPrivileges().contains("Special") ?
          Optional.of(nextMethod(admin))
          : Optional.empty();
      default -> Optional.empty();
    };

    result.ifPresent(System.out::println);
  }

  private String nextMethod(final SystemAdmin admin) {
//    do something
    return admin.nuclearCode();
  }


}
