package domaindrivers.smartschedule.planning.parallelization;

public interface DependencyRule {
    boolean isSatisfiedBy(Stage stage);
}
