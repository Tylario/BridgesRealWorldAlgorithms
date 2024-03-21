import bridges.base.Edge;
import bridges.base.GraphAdjList;
import bridges.connect.Bridges;
import bridges.connect.DataSource;
import bridges.data_src_dependent.City;
import bridges.validation.RateLimitException;
import java.io.IOException;
import java.lang.String;
import java.util.*;

public class cityGraph 
{
    static String minCityPopulation = "1000000";
    static String startingCity = "Charlotte, NC";
    static String endingCity = "Los Angeles, CA";

    public static void main(String[] args) throws IOException, RateLimitException {
        Bridges bridges = new Bridges(15, "Thudso27", "740001788848");
        bridges.setTitle("Minimum Spanning Tree on US Cities Data");
        bridges.setDescription("Lab 5. Tyler Hudson. 3/21/2024");
        DataSource ds = bridges.getDataSource();
    
        HashMap<String, String> params = new HashMap<>();
        params.put("min_pop", minCityPopulation);
        Vector<City> cities = ds.getUSCitiesData(params);
    
        System.out.println("Number of cities: " + cities.size());
    
        GraphAdjList<String, String, Double> graph = new GraphAdjList<>();
        createCityGraph(graph, cities);
    
        // Now, call primsMSTAndColorPath to generate MST and color the path
        primsMSTAndColorPath(graph, cities);
    
        // Assuming you've already adjusted the method to color the nodes between Phoenix and Houston
        // and considering 'cities' is needed for looking up latitude and longitude,
        // ensure the method signature of primsMSTAndColorPath is correctly adjusted to accept the graph and the cities
    
        bridges.setDataStructure(graph);
        bridges.visualize();
    }
    
    

    static void createCityGraph(GraphAdjList<String, String, Double> graph, Vector<City> cities) {
        // Add each city as a vertex
        for (City city : cities) {
            String cityId = city.getCity() + ", " + city.getState();
            graph.addVertex(cityId, cityId);
        }
    
        // Add edges for each pair of cities
        for (int i = 0; i < cities.size(); i++) {
            for (int j = i + 1; j < cities.size(); j++) {
                City city1 = cities.get(i);
                City city2 = cities.get(j);
                double distance = getDist(city1.getLatitude(), city1.getLongitude(), city2.getLatitude(), city2.getLongitude());
                String cityId1 = city1.getCity() + ", " + city1.getState();
                String cityId2 = city2.getCity() + ", " + city2.getState();
                
                graph.addEdge(cityId1, cityId2, distance);
                graph.addEdge(cityId2, cityId1, distance); // Since the graph is undirected
            }
        }
    }

    static void primsMSTAndColorPath(GraphAdjList<String, String, Double> graph, Vector<City> cities) {
        // Map to track if a vertex is in the MST
        Map<String, Boolean> inMST = new HashMap<>();
        // Parent map to reconstruct the path
        Map<String, String> parent = new HashMap<>();
        // Map to keep track of edge weights to a vertex
        Map<String, Double> edgeWeights = new HashMap<>();
    
        // Initialize all vertices as not in MST, and infinite edge weights
        for (City city : cities) {
            String cityId = city.getCity() + ", " + city.getState();
            inMST.put(cityId, false);
            edgeWeights.put(cityId, Double.MAX_VALUE);
        }
    
        // Starting vertex
        String startVertex = "Phoenix, AZ";
        edgeWeights.put(startVertex, 0.0); // Weight to itself is 0
        parent.put(startVertex, null); // Start vertex has no parent
    
        for (int i = 0; i < cities.size() - 1; i++) { // For all vertices in the graph
            // Find the vertex with the minimum edge weight that's not yet in MST
            String currentVertex = null;
            double minWeight = Double.MAX_VALUE;
            for (String vertexId : edgeWeights.keySet()) {
                if (!inMST.get(vertexId) && edgeWeights.get(vertexId) < minWeight) {
                    minWeight = edgeWeights.get(vertexId);
                    currentVertex = vertexId;
                }
            }
    
            // Add this vertex to the MST
            inMST.put(currentVertex, true);
    
            // Update the weights of edges leading to adjacent vertices
// Inside your loop where you update the edge weights
for (City city : cities) {
    String adjacentVertex = city.getCity() + ", " + city.getState();
    City city1 = findCityById(cities, currentVertex);
    City city2 = findCityById(cities, adjacentVertex);

    if (city1 != null && city2 != null) {
        double weight = getDist(city1.getLatitude(), city1.getLongitude(), city2.getLatitude(), city2.getLongitude());

        if (!inMST.get(adjacentVertex) && weight < edgeWeights.get(adjacentVertex)) {
            parent.put(adjacentVertex, currentVertex);
            edgeWeights.put(adjacentVertex, weight);
        }
    }
}

        }
    
        // At this point, the parent map forms the MST. Now, color the path.
        colorPath(graph, parent, "Phoenix, AZ", "Houston, TX");
    }
    static void colorPath(GraphAdjList<String, String, Double> graph, Map<String, String> parent, String start, String end) {
        String current = end;
        while (current != null && !current.equals(start)) {
            String parentNode = parent.get(current);
            if (parentNode != null) {
                // Color the node
                graph.getVertex(current).getVisualizer().setColor("red");
                
                // Additionally, color the edge between the current node and its parent
                if (graph.getEdgeData(parentNode, current) != null) { // Checks if the edge exists
                    graph.getLinkVisualizer(parentNode, current).setColor("red");
                    graph.getLinkVisualizer(current, parentNode).setColor("red"); // For undirected graph
                }
            }
            current = parentNode;
        }
        // Also color the starting node
        if (start != null) {
            graph.getVertex(start).getVisualizer().setColor("red");
        }
    }
    

    static City findCityById(Vector<City> cities, String cityId) {
        for (City city : cities) {
            String currentCityId = city.getCity() + ", " + city.getState();
            if (currentCityId.equals(cityId)) {
                return city;
            }
        }
        return null; // or handle this case as per your logic
    }
    
    

    static double getDist(double lat1, double long1, double lat2, double long2) 
    {
        // uses the haversine formula
        final int R = 6371000; // meters
        final double phi1 = Math.toRadians(lat1);
        final double phi2 = Math.toRadians(lat2);
        final double delPhi = Math.toRadians((lat2 - lat1));
        final double delLambda = Math.toRadians((long2 - long1));
        final double a = Math.sin(delPhi / 2) * Math.sin(delPhi / 2)
            + Math.cos(phi1) * Math.cos(phi2) * Math.sin(delLambda / 2)
                * Math.sin(delLambda / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // meters
    }
}