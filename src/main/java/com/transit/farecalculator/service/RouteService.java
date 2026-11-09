package com.transit.farecalculator.service;

import com.transit.farecalculator.model.RouteInfo;
import com.transit.farecalculator.model.RoutePathResponse;
import com.transit.farecalculator.model.Station;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Service managing transit network topology, station zones, and 
 * calculating shortest transit paths using Dijkstra's Algorithm.
 */
@Service
public class RouteService {

    // Station registry: Station Name -> Station Object
    private final Map<String, Station> stations = new LinkedHashMap<>();

    // Transit Lines
    private final List<RouteInfo> routes = new ArrayList<>();

    // Graph Adjacency representation for Dijkstra
    private static class Edge {
        String target;
        double distanceKm;
        String mode; // METRO, BUS, or BOTH

        Edge(String target, double distanceKm, String mode) {
            this.target = target;
            this.distanceKm = distanceKm;
            this.mode = mode;
        }
    }

    private final Map<String, List<Edge>> graph = new HashMap<>();

    @PostConstruct
    public void initializeNetwork() {
        // 1. Initialize Stations
        registerStation(new Station("ST01", "Central Station", 1, "Red Line / Blue Line", "BOTH"));
        registerStation(new Station("ST02", "City Center", 1, "Red Line / Blue Line", "BOTH"));
        registerStation(new Station("ST03", "Downtown", 1, "Red Line / Green Line", "BOTH"));
        registerStation(new Station("ST04", "University", 2, "Red Line", "METRO"));
        registerStation(new Station("ST05", "Tech Hub", 2, "Red Line", "BOTH"));
        registerStation(new Station("ST06", "Suburb North", 3, "Red Line", "METRO"));

        registerStation(new Station("ST07", "Airport Terminal", 3, "Blue Line", "BOTH"));
        registerStation(new Station("ST08", "West End", 2, "Blue Line", "BOTH"));
        registerStation(new Station("ST09", "Harbor Point", 2, "Blue Line", "BOTH"));
        registerStation(new Station("ST10", "East Gate", 3, "Blue Line", "BOTH"));

        registerStation(new Station("ST11", "Medical Center", 2, "Green Line", "METRO"));
        registerStation(new Station("ST12", "Sports Arena", 2, "Green Line", "BOTH"));
        registerStation(new Station("ST13", "South Park", 3, "Green Line", "BOTH"));

        // 2. Initialize Line Routes
        routes.add(new RouteInfo("M-RED", "Metro Red Line (North-Central)", "METRO", "#dc2626", 26.5,
                Arrays.asList("Central Station", "City Center", "Downtown", "University", "Tech Hub", "Suburb North")));

        routes.add(new RouteInfo("M-BLUE", "Metro Blue Line (Airport-East)", "METRO", "#2563eb", 31.0,
                Arrays.asList("Airport Terminal", "West End", "City Center", "Central Station", "Harbor Point", "East Gate")));

        routes.add(new RouteInfo("M-GREEN", "Metro Green Line (Downtown-South)", "METRO", "#16a34a", 21.0,
                Arrays.asList("Downtown", "Medical Center", "Sports Arena", "South Park")));

        routes.add(new RouteInfo("B-101", "Bus Route 101 (Airport Express)", "BUS", "#d97706", 22.0,
                Arrays.asList("Central Station", "West End", "Airport Terminal")));

        routes.add(new RouteInfo("B-202", "Bus Route 202 (Crosstown Link)", "BUS", "#9333ea", 24.5,
                Arrays.asList("Tech Hub", "University", "Downtown", "Harbor Point")));

        routes.add(new RouteInfo("B-303", "Bus Route 303 (East-South Connector)", "BUS", "#0891b2", 18.0,
                Arrays.asList("East Gate", "Sports Arena", "South Park")));

        // 3. Build Adjacency Graph with distances (in KM)
        // Red Line
        addEdge("Central Station", "City Center", 3.2, "BOTH");
        addEdge("City Center", "Downtown", 2.8, "BOTH");
        addEdge("Downtown", "University", 5.5, "METRO");
        addEdge("University", "Tech Hub", 7.0, "BOTH");
        addEdge("Tech Hub", "Suburb North", 8.0, "METRO");

        // Blue Line
        addEdge("Airport Terminal", "West End", 12.0, "BOTH");
        addEdge("West End", "City Center", 6.5, "BOTH");
        addEdge("Central Station", "Harbor Point", 6.0, "BOTH");
        addEdge("Harbor Point", "East Gate", 9.5, "BOTH");

        // Green Line
        addEdge("Downtown", "Medical Center", 4.2, "METRO");
        addEdge("Medical Center", "Sports Arena", 5.8, "METRO");
        addEdge("Sports Arena", "South Park", 7.0, "BOTH");

        // Bus Shortcuts / Connectors
        addEdge("West End", "Central Station", 5.0, "BUS");
        addEdge("Downtown", "Harbor Point", 4.5, "BUS");
        addEdge("Sports Arena", "East Gate", 8.0, "BUS");
    }

    private void registerStation(Station station) {
        stations.put(station.getName(), station);
        graph.putIfAbsent(station.getName(), new ArrayList<>());
    }

    private void addEdge(String u, String v, double distance, String mode) {
        graph.putIfAbsent(u, new ArrayList<>());
        graph.putIfAbsent(v, new ArrayList<>());

        graph.get(u).add(new Edge(v, distance, mode));
        graph.get(v).add(new Edge(u, distance, mode));
    }

    public List<Station> getAllStations() {
        return new ArrayList<>(stations.values());
    }

    public List<RouteInfo> getAllRoutes() {
        return routes;
    }

    public int getStationZone(String stationName) {
        Station st = stations.get(stationName);
        return (st != null) ? st.getZone() : 1;
    }

    /**
     * Dijkstra's Algorithm to find the shortest route between two stations.
     */
    public RoutePathResponse findShortestPath(String source, String destination, String transportType) {
        if (!stations.containsKey(source)) {
            throw new IllegalArgumentException("Unknown source station: " + source);
        }
        if (!stations.containsKey(destination)) {
            throw new IllegalArgumentException("Unknown destination station: " + destination);
        }

        if (source.equalsIgnoreCase(destination)) {
            return new RoutePathResponse(source, destination, transportType, 
                    Collections.singletonList(source), 0.0, 0.0, 0, 1);
        }

        String filterMode = (transportType == null || transportType.isEmpty()) ? "METRO" : transportType.toUpperCase();

        // Distance map and predecessor map
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        for (String stationName : stations.keySet()) {
            distances.put(stationName, Double.MAX_VALUE);
        }
        distances.put(source, 0.0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        pq.add(new NodeDistance(source, 0.0));

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.node;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u.equalsIgnoreCase(destination)) break;

            List<Edge> neighbors = graph.getOrDefault(u, Collections.emptyList());
            for (Edge edge : neighbors) {
                // Check if edge is compatible with selected mode
                boolean isCompatible = edge.mode.equalsIgnoreCase("BOTH") || edge.mode.equalsIgnoreCase(filterMode);
                if (!isCompatible) continue;

                double newDist = distances.get(u) + edge.distanceKm;
                if (newDist < distances.get(edge.target)) {
                    distances.put(edge.target, newDist);
                    previous.put(edge.target, u);
                    pq.add(new NodeDistance(edge.target, newDist));
                }
            }
        }

        double totalDist = distances.get(destination);
        if (totalDist == Double.MAX_VALUE) {
            // If strictly filtered mode has no path, fall back to ANY transit connection
            return findShortestPathFallback(source, destination, filterMode);
        }

        // Reconstruct path
        List<String> path = new ArrayList<>();
        String curr = destination;
        while (curr != null) {
            path.add(0, curr);
            curr = previous.get(curr);
        }

        double roundedDistance = Math.round(totalDist * 10.0) / 10.0;
        int estimatedMinutes = (int) Math.round((roundedDistance / (filterMode.equals("METRO") ? 40.0 : 25.0)) * 60) + (path.size() * 2);
        double estimatedFare = Math.round((20.0 + (roundedDistance * (filterMode.equals("METRO") ? 2.0 : 1.2))) * 100.0) / 100.0;

        return new RoutePathResponse(source, destination, filterMode, path, roundedDistance, estimatedFare, estimatedMinutes, path.size());
    }

    private RoutePathResponse findShortestPathFallback(String source, String destination, String mode) {
        // Fallback without mode restriction to ensure user always receives a valid route
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        for (String stationName : stations.keySet()) {
            distances.put(stationName, Double.MAX_VALUE);
        }
        distances.put(source, 0.0);

        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.distance));
        pq.add(new NodeDistance(source, 0.0));

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String u = current.node;
            if (visited.contains(u)) continue;
            visited.add(u);
            if (u.equalsIgnoreCase(destination)) break;

            for (Edge edge : graph.getOrDefault(u, Collections.emptyList())) {
                double newDist = distances.get(u) + edge.distanceKm;
                if (newDist < distances.get(edge.target)) {
                    distances.put(edge.target, newDist);
                    previous.put(edge.target, u);
                    pq.add(new NodeDistance(edge.target, newDist));
                }
            }
        }

        List<String> path = new ArrayList<>();
        String curr = destination;
        while (curr != null) {
            path.add(0, curr);
            curr = previous.get(curr);
        }

        double totalDist = distances.get(destination);
        double roundedDistance = (totalDist == Double.MAX_VALUE) ? 10.0 : Math.round(totalDist * 10.0) / 10.0;
        int estimatedMinutes = (int) Math.round((roundedDistance / 35.0) * 60) + 5;
        double estimatedFare = Math.round((20.0 + (roundedDistance * 1.8)) * 100.0) / 100.0;

        return new RoutePathResponse(source, destination, mode, path, roundedDistance, estimatedFare, estimatedMinutes, path.size());
    }

    private static class NodeDistance {
        String node;
        double distance;

        NodeDistance(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}
