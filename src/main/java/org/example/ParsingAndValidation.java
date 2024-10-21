package org.example;

import static lombok.AccessLevel.PRIVATE;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

public class ParsingAndValidation {

  public record Request(String name, int age, Set<String> courses) {

  }


  public static class Validator {

    private static final Set<String> VALID_COURSE_NAMES = Set.of(
        "Maths",
        "Physics",
        "Art",
        "Music");

    public static final String NAME_REGEX = "some regex";

    public static boolean worstValidation(final Request request) {
      if (!request.name().matches(NAME_REGEX)) {
        return false;
      }

      if (request.age() < 6) {
        return false;
      }

      final Set<String> courses = request.courses();

      if (courses.isEmpty()) {
        return false;
      }

      return VALID_COURSE_NAMES.containsAll(courses);
    }

    public static void veryBadValidate(final Request request) {
      if (request.name().matches(NAME_REGEX)) {
        throw new RuntimeException("Not a valid name");
      }

      if (request.age() <= 0) {
        throw new RuntimeException("Can not be a student at that age");
      }

      final Set<String> courses = request.courses();

      if (courses.isEmpty()) {
        throw new RuntimeException("Student has to be enrolled in at least 1 course");
      }

      final boolean allCoursesValid = VALID_COURSE_NAMES.containsAll(courses);

      if (!allCoursesValid) {
        throw new RuntimeException("Not all courses are valid");
      }
    }
  }

  public record Response(String status) {

  }

  public static class Controller {

    private final Service service = new Service();

    public Response addNewStudent(final Request request) {
      final boolean valid = Validator.worstValidation(request);

      if (!valid) {
        return new Response("Error");
      }

      service.saveStudent(request);

      return new Response("Success");
    }

    public Response addNewStudentABitBetter(final Request request) {
      try {
        Validator.veryBadValidate(request);
      } catch (Exception cause) {
//        NOTE: this is sometimes implicit
        return new Response(cause.getLocalizedMessage());
      }

      service.saveStudent(request);
      return new Response("Success");
    }
  }

  public static class SecondController {

    private final Service service = new Service();

    public Response anotherWayToAddAStudent(final Request request) {
      final boolean valid = Validator.worstValidation(request);

      if (!valid) {
        return new Response("Error");
      }

      service.saveStudent(request);

      return new Response("Success");
    }
  }

  public static class SomeOtherService {

    private static final Set<String> BASE_CLASSES = Set.of("Music", "Maths");

    private final Service service = new Service();

    public void autoEnrollChildren(final String name) {
      service.saveStudent(new Request(name, 7, BASE_CLASSES));
    }
  }

  public static class Service {

    public void saveStudent(final Request request) {
//      Save to the DB
//      Send events to whoever
//      Notify the classes service
//      .....
    }
  }

//  ==================================================================================

  public record ValidStudent(StudentName name, SchoolAge age, NotEmptySet<Course> courses) {

  }

  public static class StudentParser {

    public ValidStudent parseRequest(final Request request) throws RuntimeException {
      final StudentName name = StudentName.fromString(request.name());
      final SchoolAge age = SchoolAge.fromInt(request.age());
      final NotEmptySet<Course> courses = request
          .courses()
          .stream()
          .map(Course::fromString)
          .collect(
              Collectors.collectingAndThen(
                  Collectors.toSet(),
                  NotEmptySet::fromSet));

      return new ValidStudent(name, age, courses);
    }
  }

  public static class NewService {

    public void saveStudent(final ValidStudent student) {
//      Save to the DB
//      Send events to whoever
//      Notify the classes service
//      .....
    }
  }

  public static class NewSomeOtherService {

    private static final Set<String> BASE_CLASSES = Set.of("Music", "Maths");

    private final NewService service = new NewService();
    private final StudentParser parser = new StudentParser();

    public void autoEnrollChildren(final String name) {
      final Request request = new Request(name, 7, BASE_CLASSES);
      final ValidStudent parsed = parser.parseRequest(request);
      service.saveStudent(parsed);
    }
  }

  public static class NewController {

    private final NewService service = new NewService();
    private final StudentParser parser = new StudentParser();

    public Response addNewStudent(final Request request) {
      try {
        final ValidStudent student = parser.parseRequest(request);
        service.saveStudent(student);
      } catch (Exception cause) {
        return new Response(cause.getLocalizedMessage());
      }

      return new Response("Success");
    }
  }

  @EqualsAndHashCode
  @AllArgsConstructor(access = PRIVATE)
  public static class StudentName {

    public static final String NAME_REGEX = "some regex";

    private final String name;

    static StudentName fromString(final String base) throws RuntimeException {
      if (!base.matches(NAME_REGEX)) {
        throw new RuntimeException("Name: %s is not a valid name".formatted(base));
      }

      return new StudentName(base);
    }

    public String unwrap() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @EqualsAndHashCode
  @AllArgsConstructor(access = PRIVATE)
  public static class SchoolAge {

    private final int age;

    static SchoolAge fromInt(final int base) throws RuntimeException {
      if (base < 6) {
        throw new RuntimeException("Not ready for school at age: %d".formatted(base));
      }

      return new SchoolAge(base);
    }

    public int unwrap() {
      return age;
    }

    @Override
    public String toString() {
      return String.valueOf(age);
    }

  }

  @EqualsAndHashCode
  @AllArgsConstructor(access = PRIVATE)
  public static class Course {

    public static final Set<String> VALID_COURSES = Set.of(
        "Maths",
        "Physics",
        "Art",
        "Music");

    private final String course;

    static Course fromString(final String base) throws RuntimeException {
      if (!VALID_COURSES.contains(base)) {
        throw new RuntimeException("Not a valid course: %s".formatted(base));
      }

      return new Course(base);
    }

    public String unwrap() {
      return course;
    }

    @Override
    public String toString() {
      return course;
    }
  }

  @EqualsAndHashCode
  @AllArgsConstructor(access = PRIVATE)
  public static class NotEmptySet<E> {

    private final Set<E> base;

    public static <E> NotEmptySet<E> fromSet(final Set<E> base) throws RuntimeException {
      if (base.isEmpty()) {
        throw new RuntimeException(String.format("Set is not empty: %s", base));
      }

      return new NotEmptySet<>(base);
    }

    public Stream<E> stream() {
      return base.stream();
    }

    @Override
    public String toString() {
      return base.toString();
    }
  }
}
