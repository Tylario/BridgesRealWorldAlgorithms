import bridges.connect.Bridges;
import bridges.connect.DataSource;
import bridges.data_src_dependent.City;
import bridges.base.SymbolCollection;
import bridges.base.Circle;
import bridges.base.Polyline;
import bridges.validation.RateLimitException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class quadTreeSearchCentralia
{
    static String minCityPopulation = "10000";
    static SymbolCollection sc = new SymbolCollection();
    static float scaler = 15f;

    static class CityLocation 
    {
        String name;
        double latitude;
        double longitude;

        public CityLocation(String name, double latitude, double longitude) 
        {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    static HashMap<String, CityLocation> cityLocations = new HashMap<>();

    public static void main(String[] args) throws IOException, RateLimitException 
    {
        Bridges bridges = new Bridges(18, "Thudso27", "740001788848");
        bridges.setTitle("Quadtree on City Data");
        bridges.setDescription("A visualization of cities on a quadtree structure, with Centralia being the searched city (You will have to zoom out to see everything)");

        DataSource ds = bridges.getDataSource();
        HashMap<String, String> params = new HashMap<>();
        params.put("min_pop", minCityPopulation);
        Vector<City> cities = ds.getUSCitiesData(params);

        System.out.println("Number of cities: " + cities.size());

        //cityLocations.put("Charlotte_NC", new CityLocation("Charlotte_NC", 35.2271, -80.8431));
        cityLocations.put("Centralia_WA", new CityLocation("Centralia_WA", 46.7162, -122.9543));
        //cityLocations.put("Los Angeles_CA", new CityLocation("Los Angeles_CA", 34.0522, -118.2437));

        QuadTree qt = new QuadTree(0, 0, 100 * scaler, 100 * scaler);

        for (City city : cities) 
        {
            //the bounds for the quadtree are all legal coordinates
            double x = (city.getLongitude() + 180) * (100.0 / 360) * scaler;
            double y = (city.getLatitude() + 90) * (100.0 / 180) * scaler;
            qt.insert(new Point(x, y, city.getCity() + ", " + city.getState()));
        }

        for (CityLocation cityLocation : cityLocations.values()) 
        {
            qt.search(cityLocation.latitude, cityLocation.longitude, cityLocation.name);
        }

        // draw search paths
        for (Polyline polyline : QuadTree.searchPath) 
        {
            sc.addSymbol(polyline);
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
        static String[] colors = {"red", "green", "blue", "yellow", "magenta", "cyan", "orange", "purple"};
        static int colorIndex = 0;
        static List<Polyline> searchPath = new ArrayList<>();

        public QuadTree(double x, double y, double width, double height) 
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.children = new QuadTree[4];
            this.city = null;
        }

        public void insert(Point city) 
        {
            if (this.city != null) 
            {
                subdivide();
                insertIntoChild(this.city);
                this.city = null;
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
                Circle cityDot = new Circle((float)city.x, (float)city.y, 0.5f);
                cityDot.setFillColor("red");
                sc.addSymbol(cityDot);
                this.city = city;
            }
        }

        public boolean search(double latitude, double longitude, String label) 
        {
            double x = (longitude + 180) * (100.0 / 360) * scaler;
            double y = (latitude + 90) * (100.0 / 180) * scaler;

            if (!contains(x, y)) return false; // if the point is not in the bounds of this quadrant, return false

            Polyline border = createRectangle(this.x, this.y, this.width, this.height, colors[colorIndex]);
            searchPath.add(border);
            colorIndex = (colorIndex + 1) % colors.length;

            if (this.city != null && this.city.label.equals(label)) return true; // if the city is found, return true
            int index = getIndex(x, y);
            if (children[index] != null) return children[index].search(latitude, longitude, label);
            return false;
        }

        private boolean contains(double x, double y) 
        {
            return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
        }

        private void subdivide() 
        {
            double halfWidth = this.width / 2;
            double halfHeight = this.height / 2;

            children[0] = new QuadTree(this.x, this.y, halfWidth, halfHeight);
            children[1] = new QuadTree(this.x + halfWidth, this.y, halfWidth, halfHeight);
            children[2] = new QuadTree(this.x, this.y + halfHeight, halfWidth, halfHeight);
            children[3] = new QuadTree(this.x + halfWidth, this.y + halfHeight, halfWidth, halfHeight);
        }

        private int getIndex(double x, double y) 
        {
            boolean bottom = y > (this.y + height / 2);
            if (x < (this.x + width / 2)) return bottom ? 2 : 0;
            else return bottom ? 3 : 1;
        }

        private Polyline createRectangle(double x, double y, double width, double height, String color) 
        {
            Polyline border = new Polyline();
            border.addPoint((float)x, (float)y);
            border.addPoint((float)(x + width), (float)y);
            border.addPoint((float)(x + width), (float)(y + height));
            border.addPoint((float)x, (float)(y + height));
            border.addPoint((float)x, (float)y);
            border.setFillColor(color);
            return border;
        }

        public void draw() 
        {
            for (QuadTree child : children) 
            {
                if (child != null) child.draw();
            }

            if (city != null) 
            {
                Circle cityDot = new Circle((float)city.x, (float)city.y, 1f);
                cityDot.setFillColor("red");
                sc.addSymbol(cityDot);
            }
        }
    }
}
