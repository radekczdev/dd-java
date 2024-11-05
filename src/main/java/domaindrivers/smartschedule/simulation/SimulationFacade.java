package domaindrivers.smartschedule.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SimulationFacade {

  Map<SimulatedProject, Set<AvailableResourceCapability>> resourcesAllocatedToProjects = new HashMap<>();

  public Result whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(List<SimulatedProject> projects, SimulatedCapabilities totalCapability) {
    var totalCaps = new SimulatedCapabilities(new ArrayList<>(totalCapability.capabilities()));
    var filledProjects = projects
        .stream()
        .filter(project -> areDemandsSatisfied(project, totalCaps))
        .sorted(Comparator.comparing(SimulatedProject::earnings).reversed())
        .map(project -> satisfyDemandsAndRemoveFromTotalCapability(project, totalCaps))
        .filter(project -> project.missingDemands().all().isEmpty())
        .toList();
    return new Result(filledProjects.stream().map(SimulatedProject::earnings).mapToDouble(BigDecimal::doubleValue).sum(),
        filledProjects,
        resourcesAllocatedToProjects);
  }

  private SimulatedProject satisfyDemandsAndRemoveFromTotalCapability(SimulatedProject project, SimulatedCapabilities totalCapability) {
    var availableCaps = totalCapability
        .capabilities()
        .stream()
        .toList();

    ArrayList<Demand> projectDemands = new ArrayList<>();

    project.missingDemands().all()
        .forEach(demand -> {
          var capability = availableCaps
              .stream()
              .filter(demand::isSatisfiedBy)
              .findFirst();
          if (capability.isPresent()) {
            resourcesAllocatedToProjects
                .computeIfAbsent(project, p -> new HashSet<>())
                .add(capability.get());
            totalCapability.capabilities().remove(capability.get());
          } else {
            projectDemands.add(demand);
          }
        });
    return new SimulatedProject(project.projectId(), project.earnings(), Demands.of(projectDemands.toArray(new Demand[0])));
  }

  private boolean areDemandsSatisfied(SimulatedProject simulatedProject, SimulatedCapabilities simulatedCapabilities) {
    var availableCaps = simulatedCapabilities
        .capabilities()
        .stream()
        .toList();

    var projectDemands = simulatedProject
        .missingDemands()
        .all();

    return projectDemands
        .stream().allMatch(demand -> availableCaps
            .stream()
            .anyMatch(demand::isSatisfiedBy));
  }

}

