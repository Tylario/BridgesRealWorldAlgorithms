import bridges.connect.Bridges;
import bridges.connect.DataSource;
import bridges.data_src_dependent.ActorMovieIMDB;
import bridges.base.GraphAdjListSimple;
import bridges.base.Edge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BFSonImdbData
{
    public static void main(String[] args) throws Exception 
    {
		// initialize Bridges, set credentials
        Bridges bridges = new Bridges(13, "Thudso27", "740001788848");

		// set a title
		bridges.setTitle("BFS on IMDB Data");

		// set  description
		bridges.setDescription("2/20/2024 Tyler Hudson");

        DataSource ds = bridges.getDataSource();
        List<ActorMovieIMDB> actorList = ds.getActorMovieIMDBData(1813);
        GraphAdjListSimple<String> graph = new GraphAdjListSimple<>();

        // Add nodes for both actors and movies, and create edges between actors and their movies
        for (ActorMovieIMDB pair : actorList) {
            String actor = pair.getActor();
            String movie = pair.getMovie();
//graoh.getVertex("label").setLabel("String label");
            // Add the actor as a vertex if not already added
            if (!graph.getVertices().containsKey(actor)) {
                graph.addVertex(actor, actor);
            }

            // Add the movie as a vertex if not already added
            if (!graph.getVertices().containsKey(movie)) {
                graph.addVertex(movie, movie);
            }

            // Add edges between actor and movie
            graph.addEdge(actor, movie);
            graph.addEdge(movie, actor);
        }

        // Color actors and their neighbors with different colors and customize edges
        colorActorAndNeighbors(graph, "Arnold_Schwarzenegger");

        bridges.setDataStructure(graph);
        bridges.visualize();
    }

    private static void colorActorAndNeighbors(GraphAdjListSimple<String> graph, String startActor) {
        if (!graph.getVertices().containsKey(startActor)) {
            return; // return if start actor not in graph
        }
    
        String[] nodeColors = {"red", "green", "blue", "cyan", "magenta", "yellow", "mistyrose", "orange", "purple", "beige"};
        
        // Queue to manage BFS traversal
        Queue<Map.Entry<String, Integer>> queue = new LinkedList<>();
    
        // Initialize with the start actor at level 0
        queue.offer(new HashMap.SimpleEntry<>(startActor, 0));
        
        // Keep track of visited nodes to avoid reprocessing
        Map<String, Integer> visitedLevels = new HashMap<>();
    
        // Mark the start actor as visited at level 0
        visitedLevels.put(startActor, 0);
        
        // Initially set the start actor's color and label with level number
        graph.getVisualizer(startActor).setColor(nodeColors[0]);
        graph.getVertex(startActor).setLabel(startActor + " (0)");
    
        while (!queue.isEmpty()) {
            Map.Entry<String, Integer> current = queue.poll();
            String vertex = current.getKey();
            int level = current.getValue();
    
            // Iterate over each neighbor
            for (Edge<String, String> edge : graph.outgoingEdgeSetOf(vertex)) {
                String neighbor = edge.getTo();
    
                // If the neighbor hasn't been visited or has been visited at a higher level (if cycles exist)
                if (!visitedLevels.containsKey(neighbor) || visitedLevels.get(neighbor) > level + 1) {
                    // Update the level of the neighbor
                    visitedLevels.put(neighbor, level + 1);
                    // Determine the color based on the level for both the node and the edge
                    String color = nodeColors[Math.min(level + 1, 9)]; // Use "beige" for levels above 8
                    // Set the neighbor's color and label with the level number
                    graph.getVisualizer(neighbor).setColor(color);
                    graph.getVertex(neighbor).setLabel(neighbor + " (" + (level + 1) + ")");
                    // Customize the edge appearance to match the node level color
                    graph.getLinkVisualizer(vertex, neighbor).setColor(color);
                    graph.getLinkVisualizer(vertex, neighbor).setThickness(2); // Set a default thickness or adjust as needed
    
                    // Add the neighbor to the queue for further exploration
                    queue.offer(new HashMap.SimpleEntry<>(neighbor, level + 1));
                }
            }
        }
    }
}