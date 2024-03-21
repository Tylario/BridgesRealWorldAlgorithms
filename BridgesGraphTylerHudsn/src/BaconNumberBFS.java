import bridges.connect.Bridges;
import bridges.connect.DataSource;
import bridges.data_src_dependent.ActorMovieIMDB;
import bridges.base.GraphAdjListSimple;
import bridges.base.Edge;

import java.util.*;

public class BaconNumberBFS {
    public static void main(String[] args) throws Exception {
        // Initialize Bridges, set credentials
        Bridges bridges = new Bridges(14, "Thudso27", "740001788848");
        bridges.setTitle("Bacon Number - Tyler Hudson");
        bridges.setDescription("Compute and Visualize Paths from Kevin Bacon to Arnold Schwarzenegger using BFS. Purple line connects 2 actors. Orange lines represent actors or movies with a Bacon number of 5 or less. The starting and finishing node (Kevin Bacon and Arnold Schwarzenegger) are squares.");

        DataSource ds = bridges.getDataSource();
        List<ActorMovieIMDB> actorList = ds.getActorMovieIMDBData(1813);
        GraphAdjListSimple<String> graph = new GraphAdjListSimple<>();

         // Create graph from IMDB data
         for (ActorMovieIMDB pair : actorList) {
            String actor = pair.getActor();
            String movie = pair.getMovie();

            if (!graph.getVertices().containsKey(actor)) {
                graph.addVertex(actor, actor);
            }
            if (!graph.getVertices().containsKey(movie)) {
                graph.addVertex(movie, movie);
            }
            graph.addEdge(actor, movie);
            graph.addEdge(movie, actor);
        }

        String startActor = "Kevin_Bacon_(I)";
        Map<String, String> parents = computeBaconNumberAndVisualize(graph, startActor);

        String targetActor = "Arnold_Schwarzenegger"; 
        visualizePath(graph, parents, startActor, targetActor, "magenta");

        graph.getVertex(startActor).setShape("square");
        graph.getVertex(targetActor).setShape("square");

        bridges.setDataStructure(graph);
        bridges.visualize();
    }

    private static Map<String, String> computeBaconNumberAndVisualize(GraphAdjListSimple<String> graph, String startActor) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> parents = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        distances.put(startActor, 0);
        parents.put(startActor, null); // Start actor has no parent
        queue.add(startActor);

        // Breadth-first search
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Edge<String, String> edge : graph.outgoingEdgeSetOf(current)) {
                String neighbor = edge.getTo();
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, distances.get(current) + 1);
                    parents.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Set labels and color
        for (String actor : graph.getVertices().keySet()) {
            if (distances.containsKey(actor)) {
                int distance = distances.get(actor);
                graph.getVertex(actor).setLabel(actor + " (" + distance + ")");
                if (distance <= 5) {
                    for (Edge<String, String> edge : graph.outgoingEdgeSetOf(actor)) {
                        graph.getLinkVisualizer(actor, edge.getTo()).setColor("orange");
                    }
                }
            }
        }

        return parents;
    }

    private static void visualizePath(GraphAdjListSimple<String> graph, Map<String, String> parents, String startActor, String targetActor, String color) {
        // Follow the parent from the target actor to the start actor
        String current = targetActor;
        while (current != null && parents.containsKey(current) && !current.equals(startActor)) {
            String parent = parents.get(current);
            if (parent != null) {
                // Set color and thickness for the specific path, overwriting any existing styling
                graph.getLinkVisualizer(parent, current).setColor(color);
                graph.getLinkVisualizer(parent, current).setThickness(2.0f);
            }
            current = parent;
        }
    }
}