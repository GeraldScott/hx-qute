package io.archton.scaffold.service;

import io.archton.scaffold.entity.Person;
import io.archton.scaffold.entity.PersonRelationship;
import io.archton.scaffold.entity.Relationship;
import io.archton.scaffold.repository.PersonRepository;
import io.archton.scaffold.repository.PersonRelationshipRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class NetworkService {

    @Inject
    PersonRepository personRepository;

    @Inject
    PersonRelationshipRepository personRelationshipRepository;

    /**
     * A single discovered connection at a specific depth in the network.
     */
    public record NetworkConnection(Person person, Relationship relationship, Person connectedThrough, int depth) {}

    /**
     * The full network result for a focal person, with connections grouped by depth.
     */
    public record NetworkResult(Person focalPerson, Map<Integer, List<NetworkConnection>> connectionsByDepth, int maxDepth, int totalConnections) {}

    /**
     * Build a person's network using BFS traversal up to the given depth.
     *
     * @param focalPersonId the starting person
     * @param maxDepth maximum degrees of separation (clamped to 1-3)
     * @return the network result, or null if person not found
     */
    public NetworkResult buildNetwork(Long focalPersonId, int maxDepth) {
        maxDepth = Math.max(1, Math.min(maxDepth, 3));

        Person focalPerson = personRepository.findById(focalPersonId);
        if (focalPerson == null) {
            return null;
        }

        Set<Long> visited = new HashSet<>();
        visited.add(focalPersonId);

        Set<Long> currentFrontier = new HashSet<>();
        currentFrontier.add(focalPersonId);

        Map<Integer, List<NetworkConnection>> connectionsByDepth = new HashMap<>();
        int totalConnections = 0;

        for (int depth = 1; depth <= maxDepth; depth++) {
            List<PersonRelationship> relationships = personRelationshipRepository.findConnectionsForPersonIds(currentFrontier);

            List<NetworkConnection> depthConnections = new ArrayList<>();
            Set<Long> nextFrontier = new HashSet<>();

            for (PersonRelationship pr : relationships) {
                // Determine the "other" person -- the one NOT in visited
                Person newPerson = null;
                Person throughPerson = null;

                if (!visited.contains(pr.relatedPerson.id)) {
                    newPerson = pr.relatedPerson;
                    throughPerson = pr.sourcePerson;
                } else if (!visited.contains(pr.sourcePerson.id)) {
                    newPerson = pr.sourcePerson;
                    throughPerson = pr.relatedPerson;
                }

                if (newPerson != null) {
                    // Only add if we haven't already discovered this person at this depth
                    if (!visited.contains(newPerson.id)) {
                        visited.add(newPerson.id);
                        nextFrontier.add(newPerson.id);
                        depthConnections.add(new NetworkConnection(newPerson, pr.relationship, throughPerson, depth));
                    }
                }
            }

            connectionsByDepth.put(depth, depthConnections);
            totalConnections += depthConnections.size();

            currentFrontier = nextFrontier;
            if (currentFrontier.isEmpty()) {
                break;
            }
        }

        return new NetworkResult(focalPerson, connectionsByDepth, maxDepth, totalConnections);
    }
}
