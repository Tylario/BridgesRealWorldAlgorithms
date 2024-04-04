import bridges.connect.Bridges;
import bridges.connect.DataSource;
import bridges.data_src_dependent.City;
import bridges.base.SymbolCollection;
import bridges.base.Circle;
import bridges.base.Polyline;
import bridges.validation.RateLimitException;


import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

public class quadTree 
{
    static String minCityPopulation = "10000";
    static SymbolCollection sc = new SymbolCollection();
    static float scaler = 15f;

    public static void main(String[] args) throws IOException, RateLimitException 
    {
        Bridges bridges = new Bridges(16, "Thudso27", "740001788848");
        bridges.setTitle("Quadtree on City Data");
        bridges.setDescription("A visualization of cities on a quadtree structure. (You will have to zoom out to see everything)");

        DataSource ds = bridges.getDataSource();

        HashMap<String, String> params = new HashMap<>();
        params.put("min_pop", minCityPopulation);
        Vector<City> cities = ds.getUSCitiesData(params);

        System.out.println("Number of cities: " + cities.size());

        QuadTree qt = new QuadTree(0, 0, 100 * scaler, 100 * scaler);

        for (City city : cities) 
        {
            //the bounds for the quadtree are all legal coordinates
            double x = (city.getLongitude() + 180) * (100.0 / 360) * scaler;
            double y = (city.getLatitude() + 90) * (100.0 / 180) * scaler;
            qt.insert(new Point(x, y, city.getCity() + ", " + city.getState()));
        }

        qt.draw();

        bridges.setDataStructure(sc);
        bridges.visualize();
    }

    static class Point 
    {
        public double x, y;
        public String label;

        public Point(double x, double y, String label) 
        {
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }

    static class QuadTree 
    {
        double x, y, width, height;
        Point city;
        QuadTree[] children;

        public QuadTree(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.children = new QuadTree[4];
            this.city = null;
        }

        public void insert(Point city) {
            // if this quadrant already has a city, subdivide and insert the city into the appropriate quadrant
            if (this.city != null) {
                subdivide();
                insertIntoChild(this.city);
                this.city = null; // clear the city now that it's been pushed down the tree
            }
            insertIntoChild(city);
        }

        private void insertIntoChild(Point city) 
        {
            int index = getIndex(city.x, city.y);

            if (children[index] != null) 
            {
                children[index].insert(city);
            } 
            else 
            {
                // draw city as a dot
                Circle cityDot = new Circle((float)city.x, (float)city.y, (float)0.5);
                cityDot.setFillColor("red");
                sc.addSymbol(cityDot);
                this.city = city; // if there's no child yet, the city stays here
            }
        }

        private void subdivide() 
        {
            double halfWidth = this.width / 2;
            double halfHeight = this.height / 2;
    
            children[0] = new QuadTree(this.x, this.y, halfWidth, halfHeight);
            children[1] = new QuadTree(this.x + halfWidth, this.y, halfWidth, halfHeight);
            children[2] = new QuadTree(this.x, this.y + halfHeight, halfWidth, halfHeight);
            children[3] = new QuadTree(this.x + halfWidth, this.y + halfHeight, halfWidth, halfHeight);
    
            // draw border lines with scaled dimensions
            Polyline borderLines = new Polyline();
            borderLines.addPoint((float)this.x, (float)this.y);
            borderLines.addPoint((float)this.x + (float)this.width, (float)this.y);
            borderLines.addPoint((float)this.x + (float)this.width, (float)this.y + (float)this.height);
            borderLines.addPoint((float)this.x, (float)this.y + (float)this.height);
            borderLines.addPoint((float)this.x, (float)this.y);
            borderLines.setStrokeColor("blue");
            borderLines.setStrokeWidth(0.5f); // 0.5 pixel width
            sc.addSymbol(borderLines);
        }

        private int getIndex(double x, double y) 
        {
            boolean bottom = y > (this.y + height / 2);
            if (x < (this.x + width / 2)) 
            {
                return bottom ? 2 : 0;
            } 
            else 
            {
                return bottom ? 3 : 1;
            }
        }

        public void draw() 
        {
            // if this node has children, ask them to draw themselves
            for (QuadTree child : children) 
            {
                if (child != null) {
                    child.draw();
                }
            }

            // if this node has a city, draw it
            if (city != null) 
            {
                Circle cityDot = new Circle((float)city.x, (float)city.y, 1);
                cityDot.setFillColor("red");
                sc.addSymbol(cityDot);
            }
        }
    }
}
