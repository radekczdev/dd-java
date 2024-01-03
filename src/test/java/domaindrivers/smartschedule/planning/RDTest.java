package domaindrivers.smartschedule.planning;

import domaindrivers.smartschedule.allocation.ResourceId;
import domaindrivers.smartschedule.availability.AvailabilityFacade;
import domaindrivers.smartschedule.availability.Calendars;
import domaindrivers.smartschedule.planning.parallelization.ParallelStages;
import domaindrivers.smartschedule.planning.parallelization.ParallelStagesList;
import domaindrivers.smartschedule.planning.parallelization.Stage;
import domaindrivers.smartschedule.planning.schedule.Schedule;
import domaindrivers.smartschedule.shared.ResourceName;
import domaindrivers.smartschedule.shared.capability.Capability;
import domaindrivers.smartschedule.shared.timeslot.TimeSlot;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.Set;

import static domaindrivers.smartschedule.planning.parallelization.ParallelStagesList.*;
import static domaindrivers.smartschedule.planning.schedule.assertions.ScheduleAssert.assertThat;
import static java.time.Duration.ofDays;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Import({TestDbConfiguration.class})
@Sql(scripts = {"classpath:schema-planning.sql"})
class RDTest {

    static final TimeSlot JANUARY = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-31T00:00:00.00Z"));
    static final TimeSlot FEBRUARY = new TimeSlot(Instant.parse("2020-02-01T00:00:00.00Z"), Instant.parse("2020-02-28T00:00:00.00Z"));
    static final TimeSlot MARCH = new TimeSlot(Instant.parse("2020-03-01T00:00:00.00Z"), Instant.parse("2020-03-31T00:00:00.00Z"));
    static final TimeSlot Q1 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-03-31T00:00:00.00Z"));
    static final TimeSlot JAN_1_4 = new TimeSlot(Instant.parse("2020-01-01T00:00:00.00Z"), Instant.parse("2020-01-04T00:00:00.00Z"));
    static final TimeSlot FEB_2_16 = new TimeSlot(Instant.parse("2020-02-01T00:00:00.00Z"), Instant.parse("2020-02-16T00:00:00.00Z"));
    static final TimeSlot MAR_1_6 = new TimeSlot(Instant.parse("2020-03-01T00:00:00.00Z"), Instant.parse("2020-03-06T00:00:00.00Z"));

    @Autowired
    PlanningFacade projectFacade;

    @MockBean
    AvailabilityFacade availabilityFacade;

    @Disabled("not implemented yet")
    void researchAndDevelopmentProjectProcess() {
        //given
        ProjectId projectId =
                projectFacade.addNewProject("waterfall");
        //and

        ResourceName r1 = new ResourceName("r1");
        ResourceId javaAvailableInJanuary = resourceAvailableForCapabilityInPeriod(r1, Capability.skill("JAVA"), JANUARY);
        ResourceName r2 = new ResourceName("r2");
        ResourceId phpAvailableInFebruary = resourceAvailableForCapabilityInPeriod(r2, Capability.skill("PHP"), FEBRUARY);
        ResourceName r3 = new ResourceName("r3");
        ResourceId csharpAvailableInMarch = resourceAvailableForCapabilityInPeriod(r3, Capability.skill("CSHARP"), MARCH);
        Set<ResourceName> allResources = Set.of(r1, r2, r3);

        //when
        projectFacade.defineResourcesWithinDates(projectId, allResources, JANUARY);

        //then
        verifyThatResourcesAreMissing(projectId, Set.of(phpAvailableInFebruary, csharpAvailableInMarch));

        //when
        projectFacade.defineResourcesWithinDates(projectId, allResources, FEBRUARY);

        //then
        verifyThatResourcesAreMissing(projectId, Set.of(javaAvailableInJanuary, csharpAvailableInMarch));

        //when
        projectFacade.defineResourcesWithinDates(projectId, allResources, Q1);

        //then
        verifyThatNoResourcesAreMissing(projectId);

        //when
        projectFacade.adjustStagesToResourceAvailability(projectId,
                Q1,
                new Stage("Stage1")
                        .ofDuration(ofDays(3))
                        .withChosenResourceCapabilities(r1),
                new Stage("Stage2")
                        .ofDuration(ofDays(15))
                        .withChosenResourceCapabilities(r2),
                new Stage("Stage3")
                        .ofDuration(ofDays(5))
                        .withChosenResourceCapabilities(r3));

        //then
        ProjectCard loaded = projectFacade.load(projectId);
        Schedule schedule = projectFacade.load(projectId).schedule();
        assertThat(schedule)
                .hasStage("Stage1").withSlot(JAN_1_4)
                .and()
                .hasStage("Stage2").withSlot(FEB_2_16)
                .and()
                .hasStage("Stage3").withSlot(MAR_1_6);
        projectIsNotParallelized(loaded);
    }

    ResourceId resourceAvailableForCapabilityInPeriod(ResourceName resource, Capability capability, TimeSlot slot) {
        //todo
        return ResourceId.newOne();
    }

    void projectIsNotParallelized(ProjectCard loaded) {
        assertThat(loaded.parallelizedStages()).isEqualTo(empty());
    }

    void verifyThatNoResourcesAreMissing(ProjectId projectId) {
    }

    void verifyThatResourcesAreMissing(ProjectId projectId, Set<ResourceId> missingResources) {

    }
}