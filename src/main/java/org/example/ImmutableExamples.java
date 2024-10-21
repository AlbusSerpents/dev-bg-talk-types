package org.example;

import java.util.Collection;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class ImmutableExamples {

  static ScoreStatistics mapToScore(final Map<String, Double> scores) {
    final var stats =
        scores
            .values()
            .stream()
            .filter(Objects::nonNull)
            .mapToDouble(x -> x)
            .summaryStatistics();

    return new ScoreStatistics(stats);
  }

  public record ScoreStatistics(double max, double avg, double min) {

    public ScoreStatistics(final DoubleSummaryStatistics statistics) {
      this(statistics.getMax(), statistics.getAverage(), statistics.getMin());
    }
  }

  interface IStudent {

    String name();

    int age();

    Set<String> enrolledIn();

    ScoreStatistics calculateScore();

    double scoreFor(final String subject);
  }

  @Setter
  @ToString
  @NoArgsConstructor
  @EqualsAndHashCode
  public static class Student implements IStudent {

    private int age;
    private String name;
    private Map<String, Double> score;

    @Override
    public String name() {
      return name;
    }

    @Override
    public int age() {
      return age;
    }

    @Override
    public Set<String> enrolledIn() {
      return score.keySet();
    }

    @Override
    public ScoreStatistics calculateScore() {
      return mapToScore(score);
    }

    @Override
    public double scoreFor(final String subject) {
      return score.getOrDefault(subject, 0d);
    }
  }

  public record Mutable(String name, int age, Map<String, Double> scores) implements IStudent {

    @Override
    public Set<String> enrolledIn() {
      return scores.keySet();
    }

    @Override
    public ScoreStatistics calculateScore() {
      return mapToScore(scores);
    }

    @Override
    public double scoreFor(final String subject) {
      return scores.getOrDefault(subject, 0d);
    }
  }

  public record PoorTypeInfoImmutable(String name, int age, Map<String, Double> scores) implements
      IStudent {

    @Override
    public Set<String> enrolledIn() {
      return scores.keySet();
    }

    @Override
    public Map<String, Double> scores() {
      return Collections.unmodifiableMap(scores);
    }

    @Override
    public ScoreStatistics calculateScore() {
      return mapToScore(scores);
    }

    @Override
    public double scoreFor(final String subject) {
      return scores.getOrDefault(subject, 0d);
    }
  }

  @ToString
  @EqualsAndHashCode
  @AllArgsConstructor
  public static final class EncapsulationImmutable implements IStudent {

    private final int age;
    private final String name;
    private final Map<String, Double> scores;

    @Override
    public String name() {
      return name;
    }

    @Override
    public int age() {
      return age;
    }

    @Override
    public Set<String> enrolledIn() {
      return scores.keySet();
    }

    @Override
    public ScoreStatistics calculateScore() {
      return mapToScore(scores);
    }

    @Override
    public double scoreFor(final String subject) {
      return scores.getOrDefault(subject, 0d);
    }
  }

  public record DefensiveCopyImmutable(String name, int age, Map<String, Double> scores) implements
      IStudent {

    public Map<String, Double> scores() {
      return Map.copyOf(scores);
    }

    @Override
    public Set<String> enrolledIn() {
      return scores.keySet();
    }

    @Override
    public ScoreStatistics calculateScore() {
      return mapToScore(scores);
    }

    @Override
    public double scoreFor(final String subject) {
      return scores.getOrDefault(subject, 0d);
    }
  }

  @ToString
  @EqualsAndHashCode
  public static final class TypeRichImmutable implements IStudent {

    private final String name;
    private final int age;
    private final ReadOnlyMap<String, Double> scores;

    public TypeRichImmutable(
        final String name,
        final int age,
        final Map<String, Double> scores) {
      this.name = name;
      this.age = age;
      this.scores = new ReadOnlyMap<>(scores);
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public int age() {
      return age;
    }

    public ReadOnlyMap<String, Double> scores() {
      return scores;
    }

    @Override
    public Set<String> enrolledIn() {
      return scores.keySet();
    }

    @Override
    public ScoreStatistics calculateScore() {
      final var stats = scores
          .values()
          .stream()
          .mapToDouble(x -> x)
          .summaryStatistics();

      return new ScoreStatistics(stats);
    }

    @Override
    public double scoreFor(final String subject) {
      return scores.getOrDefault(subject, 0d);
    }
  }

  @EqualsAndHashCode
  public static class ReadOnlyMap<K, V> {

    private final Map<K, V> base;

    public ReadOnlyMap(final Map<K, V> base) {
      this.base = Map.copyOf(base);
    }

    public int size() {
      return base.size();
    }

    public boolean isEmpty() {
      return base.isEmpty();
    }

    public <T extends K> boolean contains(final T key) {
      return base.containsKey(key);
    }

    public <T extends K> Optional<V> get(final T key) {
      return Optional.ofNullable(base.get(key));
    }

    public <T extends K> V getOrDefault(final T key, final V defaultValue) {
      return Optional.ofNullable(base.get(key)).orElse(defaultValue);
    }

    public Set<K> keySet() {
      return base.keySet();
    }

    public Collection<V> values() {
      return base.values();
    }

    public Set<Map.Entry<K, V>> entrySet() {
      return base.entrySet();
    }

    @Override
    public String toString() {
      return base.toString();
    }
  }
}
