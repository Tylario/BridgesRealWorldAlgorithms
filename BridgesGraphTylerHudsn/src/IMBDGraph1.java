import bridges.connect.Bridges;
import bridges.connect.DataSource;
import bridges.data_src_dependent.ActorMovieIMDB;
import bridges.base.GraphAdjListSimple;
import java.util.List;
import java.util.HashSet;

public class IMBDGraph1 {
    public static void main(String[] args) throws Exception {

        // Initialize Bridges
        Bridges bridges = new Bridges(11, "Thudso27", "740001788848");

        // Set title and description
        bridges.setTitle("IMDB Actor-Movie Graph");
        bridges.setDescription("A Graph showing actors connected to the movies they've appeared in.");

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

        bridges.setDataStructure(graph);
        bridges.visualize();
    }
}
