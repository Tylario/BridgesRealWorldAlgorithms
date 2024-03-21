import bridges.connect.Bridges;
import bridges.connect.DataSource;
import bridges.data_src_dependent.ActorMovieIMDB;
import bridges.base.GraphAdjListSimple;
import bridges.base.Edge;
import java.util.List;

public class IMBDGraph2 {
    public static void main(String[] args) throws Exception {

        // Initialize Bridges
        Bridges bridges = new Bridges(12, "Thudso27", "740001788848");

        // Set title and description
        bridges.setTitle("IMDB Actor-Movie Graph Enhanced");
        bridges.setDescription("A Graph highlighting Al Pacino and Arnold Schwarzenegger with their neighbors, using different colors and customizing edges.");

        DataSource ds = bridges.getDataSource();
        List<ActorMovieIMDB> actorList = ds.getActorMovieIMDBData(1813);
        GraphAdjListSimple<String> graph = new GraphAdjListSimple<>();

        // Add nodes for both actors and movies, and create edges between actors and their movies
        for (ActorMovieIMDB pair : actorList) {
            String actor = pair.getActor();
            String movie = pair.getMovie();

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
        }

        // Define colors
        String alPacinoColor = "limegreen";
        String arnoldColor = "blue";
        String alPacinoNeighborsColor = "orange";
        String arnoldNeighborsColor = "purple";
        String edgeColor = "gray";
        int edgeThickness = 2;

        // Color actors and their neighbors with different colors and customize edges
        colorActorAndNeighbors(graph, "Al_Pacino", alPacinoColor, alPacinoNeighborsColor, edgeColor, edgeThickness);
        colorActorAndNeighbors(graph, "Arnold_Schwarzenegger", arnoldColor, arnoldNeighborsColor, edgeColor, edgeThickness);

        bridges.setDataStructure(graph);
        bridges.visualize();
    }

    private static void colorActorAndNeighbors(GraphAdjListSimple<String> graph, String actor, String actorColor, String neighborColor, String edgeColor, int edgeThickness) {
        // Color the actor
        if (graph.getVertices().containsKey(actor)) {
            graph.getVisualizer(actor).setColor(actorColor);
            // Iterate over the actor's neighbors and color them, customize edges
            for (Edge<String, String> edge : graph.outgoingEdgeSetOf(actor)) {
                String neighbor = edge.getTo();
                graph.getVisualizer(neighbor).setColor(neighborColor);
                // Customize edge appearance
                graph.getLinkVisualizer(actor, neighbor).setColor(edgeColor);
                graph.getLinkVisualizer(actor, neighbor).setThickness(edgeThickness);
            }
        }
    }
}
