package domaindrivers.smartschedule.planning.parallelization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ParallelizationTest {

  static final StageParallelization stageParallelization = new StageParallelization();

  @Test
  void everythingCanBeDoneInParallelWhenThereAreNoDependencies() {
    //given
    Stage stage1 = new Stage("Stage1");
    Stage stage2 = new Stage("Stage2");

    //when
    ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2));

    //then
    assertEquals(1, sortedStages.all().size());
  }

  @Test
  void testSimpleDependencies() {
    //given
    Stage stage1 = new Stage("Stage1");
    Stage stage2 = new Stage("Stage2");
    Stage stage3 = new Stage("Stage3");
    Stage stage4 = new Stage("Stage4");
    stage2.dependsOn(stage1);
    stage3.dependsOn(stage1);
    stage4.dependsOn(stage2);

    //when
    ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2, stage3, stage4));

    //then
    assertEquals(sortedStages.print(), "Stage1 | Stage2, Stage3 | Stage4");
  }

  @Test
  void cantBeDoneWhenThereIsACycle() {
    //given
    Stage stage1 = new Stage("Stage1");
    Stage stage2 = new Stage("Stage2");
    stage2.dependsOn(stage1).addDependencyRule(new CyclicDependencyRule());
    stage1.dependsOn(stage2).addDependencyRule(new CyclicDependencyRule());


    //when
    ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2));

    //then
    assertTrue(sortedStages.all().isEmpty());
  }

}