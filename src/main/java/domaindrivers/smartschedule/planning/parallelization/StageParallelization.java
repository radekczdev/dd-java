package domaindrivers.smartschedule.planning.parallelization;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.Set;


public class StageParallelization {

  public ParallelStagesList of(Set<Stage> stages) {
    return new ParallelStagesList(stages.stream()
        .collect(groupingBy(Stage::dependencies, toSet()))
        .values()
        .stream()
        .map(ParallelStages::new)
        .filter(ps -> ps.stages()
            .stream()
            .allMatch(stage -> stage.dependencyRules()
                .stream()
                .allMatch(rule -> rule.isSatisfiedBy(stage)))
        )
        .toList());
  }

}
