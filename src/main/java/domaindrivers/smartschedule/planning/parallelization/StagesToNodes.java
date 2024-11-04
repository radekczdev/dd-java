package domaindrivers.smartschedule.planning.parallelization;

import domaindrivers.smartschedule.sorter.Node;
import domaindrivers.smartschedule.sorter.Nodes;

import java.util.*;
import java.util.stream.Collectors;

class StagesToNodes {

    Nodes<Stage> calculate(List<Stage> stages) {
        Map<String, Node<Stage>> result = stages.stream()
                .collect(Collectors.toMap(Stage::name, stage -> new Node<>(stage.name(), stage)));

        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            result = explicitDependencies(stage, result);
            result = sharedResources(stage, stages.stream().skip(i + 1).collect(Collectors.toList()), result);
        }

        return new Nodes<>(new HashSet<>(result.values()));
    }

    private Map<String, Node<Stage>> sharedResources(Stage stage, List<Stage> with, Map<String, Node<Stage>> result) {
        for (Stage other : with) {
            if (!stage.name().equals(other.name())) {
                if (!Collections.disjoint(stage.resources(), other.resources())) {
                    if (other.resources().size() > stage.resources().size()) {
                        Node<Stage> node = result.get(stage.name());
                        node = node.dependsOn(result.get(other.name()));
                        result.put(stage.name(), node);
                    } else {
                        Node<Stage> node = result.get(other.name());
                        node = node.dependsOn(result.get(stage.name()));
                        result.put(other.name(), node);
                    }
                }
            }
        }
        return result;
    }

    private Map<String, Node<Stage>> explicitDependencies(Stage stage, Map<String, Node<Stage>> result) {
        Node<Stage> nodeWithExplicitDeps = result.get(stage.name());
        for(Stage explicitDependency: stage.dependencies()) {
            nodeWithExplicitDeps = nodeWithExplicitDeps.dependsOn(result.get(explicitDependency.name()));
        }
        result.put(stage.name(), nodeWithExplicitDeps);
        return result;
    }
}

