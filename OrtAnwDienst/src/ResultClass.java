import com.vividsolutions.jts.geom.Coordinate;

/**
 * Created by samue on 23.06.2017.
 */
public class ResultClass {
    public Coordinate Coordinate;
    public String Name;

    public ResultClass(Coordinate coordinate, String name)
    {
        this.Coordinate = coordinate;
        this.Name = name;
    }
}
