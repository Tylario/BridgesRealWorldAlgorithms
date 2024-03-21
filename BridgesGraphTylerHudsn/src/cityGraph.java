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
    public static void main(String[] args) throws IOException, RateLimitException {
        Bridges bridges = new Bridges(15, "Thudso27", "740001788848");
        bridges.setTitle("Minimum Spanning Tree on US Cities Data");
        bridges.setDescription("Lab 5. Tyler Hudson. 3/21/2024");
        DataSource ds = bridges.getDataSource();
    
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("state", "NC");
        params.put("min_pop", "100000");
        Vector<City> cities = ds.getUSCitiesData(params);
    
        System.out.println("First 2 cities are " + cities.get(0).getCity() + "," + cities.get(0).getState() + " and " + cities.get(1).getCity() + "," + cities.get(1).getState());
        System.out.println("Dist. between the two cities: " + getDist(cities.get(0).getLatitude(), cities.get(0).getLongitude(), cities.get(1).getLatitude(), cities.get(1).getLongitude()) + " meters");
    
        GraphAdjList<String, String, Double> graph = new GraphAdjList<>();
        createCityGraph(graph, cities); // Adjusted to use the new method
        
        // Visualization (optional settings can be added here)
        
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