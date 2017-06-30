import com.vividsolutions.jts.geom.Coordinate;


//Object created from the DB return data
public class ResultClass {
    public Coordinate Coordinate;
    public String Name;

    public ResultClass(Coordinate coordinate, String name)
    {
        this.Coordinate = coordinate;
        this.Name = name;
    }
}
