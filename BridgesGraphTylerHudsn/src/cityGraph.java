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
    static String minCityPopulation = "100000";
    static String startingCity = "Charlotte, NC";
    static String endingCity = "Houston, TX";
    static boolean renderEntireGraph = false; // if true will only render the path, if false will render the entire graph


    // to reconstruct the path. This is static so that it can be accessed from the main method
    static Map<String, String> parent = new HashMap<>();

    public static void main(String[] args) throws IOException, RateLimitException 
    {
        Bridges bridges = new Bridges(15, "Thudso27", "740001788848");
        bridges.setTitle("Minimum Spanning Tree on US Cities Data");
        bridges.setDescription("Change the variables at the top of the script to visualize from different starting and ending cities, or to visualize the entire graph or just the path, or to change the minimum city population. Default is to visualize the path from Charlotte, NC to Houston, TX with a minimum city population of 100,000.");
        DataSource ds = bridges.getDataSource();
    
        HashMap<String, String> params = new HashMap<>();
        params.put("min_pop", minCityPopulation);
        Vector<City> cities = ds.getUSCitiesData(params);
    
        System.out.println("Number of cities: " + cities.size());
    
        GraphAdjList<String, String, Double> graph = new GraphAdjList<>();
        createCityGraph(graph, cities);
    
        // prims minimum spanning tree algorithm on the graph
        primsMSTAndColorPath(graph, cities);
        
        if (renderEntireGraph) 
        {
            // renders the entire graph
            bridges.setDataStructure(graph);
            bridges.visualize();
        } 
        else 
        {
            // renders only the path
            GraphAdjList<String, String, Double> pathGraph = buildPathGraph(graph, parent, cities, startingCity, endingCity);
            bridges.setDataStructure(pathGraph);
            bridges.visualize();
        }
    }
    
    
    static void createCityGraph(GraphAdjList<String, String, Double> graph, Vector<City> cities) 
    {
        // add each city as a vertex
        for (City city : cities) 
        {
            String cityId = city.getCity() + ", " + city.getState();
            graph.addVertex(cityId, cityId);
            graph.getVertex(cityId).setLocation(city.getLongitude(), city.getLatitude());

        }
    
        // edges for each pair of cities
        for (int i = 0; i < cities.size(); i++) 
        {
            for (int j = i + 1; j < cities.size(); j++) 
            {
                City city1 = cities.get(i);
                City city2 = cities.get(j);
                double distance = getDist(city1.getLatitude(), city1.getLongitude(), city2.getLatitude(), city2.getLongitude());
                String cityId1 = city1.getCity() + ", " + city1.getState();
                String cityId2 = city2.getCity() + ", " + city2.getState();
                
                graph.addEdge(cityId1, cityId2, distance);
                graph.addEdge(cityId2, cityId1, distance); // since the graph is undirected
            }
        }
    }

    static void primsMSTAndColorPath(GraphAdjList<String, String, Double> graph, Vector<City> cities) 
    {
        // to track if a vertex is in the MST
        Map<String, Boolean> inMST = new HashMap<>();

        // to keep track of edge weights to a vertex
        Map<String, Double> edgeWeights = new HashMap<>();
    
        // initialize all vertices
        for (City city : cities) 
        {
            String cityId = city.getCity() + ", " + city.getState();
            inMST.put(cityId, false);
            edgeWeights.put(cityId, Double.MAX_VALUE);
        }
    
        // starting vertex
        String startVertex = startingCity;
        edgeWeights.put(startVertex, 0.0); // weight to itself is 0
        parent.put(startVertex, null); 
    
        for (int i = 0; i < cities.size() - 1; i++) 
        {
            String currentVertex = null;
            double minWeight = Double.MAX_VALUE;
            for (String vertexId : edgeWeights.keySet()) 
            {
                if (!inMST.get(vertexId) && edgeWeights.get(vertexId) < minWeight) 
                {
                    minWeight = edgeWeights.get(vertexId);
                    currentVertex = vertexId;
                }
            }
    
            inMST.put(currentVertex, true);
    
            // update the weights of edges
            for (City city : cities) {
                String adjacentVertex = city.getCity() + ", " + city.getState();
                City city1 = findCityById(cities, currentVertex);
                City city2 = findCityById(cities, adjacentVertex);

                if (city1 != null && city2 != null) 
                {
                    double weight = getDist(city1.getLatitude(), city1.getLongitude(), city2.getLatitude(), city2.getLongitude());

                    if (!inMST.get(adjacentVertex) && weight < edgeWeights.get(adjacentVertex)) 
                    {
                        parent.put(adjacentVertex, currentVertex);
                        edgeWeights.put(adjacentVertex, weight);
                    }
                }
            }
        }
        
        colorPath(graph, parent, startingCity, endingCity);
    }

    static void colorPath(GraphAdjList<String, String, Double> graph, Map<String, String> parent, String start, String end) 
    {
        String current = end;
        while (current != null && !current.equals(start)) 
        {
            String parentNode = parent.get(current);
            if (parentNode != null) {
                // color the node
                graph.getVertex(current).getVisualizer().setColor("red");
                
                // color the edge between the current node and its parent
                if (graph.getEdgeData(parentNode, current) != null) 
                { 
                    graph.getLinkVisualizer(parentNode, current).setColor("red");
                    graph.getLinkVisualizer(current, parentNode).setColor("red");
                }
            }
            current = parentNode;
        }
        if (start != null) 
        {
            graph.getVertex(start).getVisualizer().setColor("red");
        }
    }

    static City findCityById(Vector<City> cities, String cityId) 
    {
        for (City city : cities) 
        {
            String currentCityId = city.getCity() + ", " + city.getState();
            if (currentCityId.equals(cityId)) 
            {
                return city;
            }
        }
        return null;
    }

    static GraphAdjList<String, String, Double> buildPathGraph(GraphAdjList<String, String, Double> originalGraph, Map<String, String> parent, Vector<City> cities, String start, String end) 
    {
        GraphAdjList<String, String, Double> pathGraph = new GraphAdjList<>();
        
        // start from the end city and follor the parent to the start city
        String current = end;
        while (current != null && !current.equals(start)) 
        {
            String parentNode = parent.get(current);
            if (parentNode != null) 
            {
                double scaleFactor = 50; // to scale up the cities (they are too close to eachother)

                // find the center, so it will appear on the bridges visualization 
                double sumLat = 0, sumLong = 0;
                for (City city : cities) {
                    sumLat += city.getLatitude() * scaleFactor;
                    sumLong += city.getLongitude() * scaleFactor;
                }
                
                double avgLat = sumLat / cities.size();
                double avgLong = sumLong / cities.size();
                double translateLat = avgLat;
                double translateLong = avgLong;
                
                if (!pathGraph.getVertices().containsKey(current)) 
                {
                    pathGraph.addVertex(current, current);
                    City currentCity = findCityById(cities, current);
                    if (currentCity != null) {
                        double scaledTranslatedLat = currentCity.getLatitude() * scaleFactor + translateLat;
                        double scaledTranslatedLong = currentCity.getLongitude() * scaleFactor + translateLong;
                        pathGraph.getVertex(current).setLocation(scaledTranslatedLong, scaledTranslatedLat);
                    }
                }
                
                if (!pathGraph.getVertices().containsKey(parentNode)) {
                    pathGraph.addVertex(parentNode, parentNode);
                    City parentNodeCity = findCityById(cities, parentNode);
                    if (parentNodeCity != null) {
                        double scaledTranslatedLat = parentNodeCity.getLatitude() * scaleFactor + translateLat;
                        double scaledTranslatedLong = parentNodeCity.getLongitude() * scaleFactor + translateLong;
                        pathGraph.getVertex(parentNode).setLocation(scaledTranslatedLong, scaledTranslatedLat);
                    }
                }
                
                // add edge between the current node and its parent
                City city1 = findCityById(cities, parentNode);
                City city2 = findCityById(cities, current);
                if (city1 != null && city2 != null) 
                {
                    double weight = getDist(city1.getLatitude(), city1.getLongitude(), city2.getLatitude(), city2.getLongitude());
                    pathGraph.addEdge(parentNode, current, weight);
                    pathGraph.addEdge(current, parentNode, weight); // since the graph is undirected
                }
            }
            current = parentNode;
        }
    
        return pathGraph;
    }
    

    static double getDist(double lat1, double long1, double lat2, double long2) 
    {
        // provided by the instructor
        final int R = 6371000; 
        final double phi1 = Math.toRadians(lat1);
        final double phi2 = Math.toRadians(lat2);
        final double delPhi = Math.toRadians((lat2 - lat1));
        final double delLambda = Math.toRadians((long2 - long1));
        final double a = Math.sin(delPhi / 2) * Math.sin(delPhi / 2)
            + Math.cos(phi1) * Math.cos(phi2) * Math.sin(delLambda / 2)
                * Math.sin(delLambda / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; 
    }
}