package domaindrivers.smartschedule.planning.parallelization;

public class CyclicDependencyRule implements DependencyRule {
  public boolean isSatisfiedBy(Stage stage) {
    return stage.dependencies()
        .stream()
        .anyMatch(d -> !d.dependencies().contains(stage));
  }
}
