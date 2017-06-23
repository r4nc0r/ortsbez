
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fu.esi.SQL;
import fu.keys.LSIClassCentreDB;
import fu.util.DBUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class DBConnection {
    private static String DBparams;
    private static String SQLStatement;
    private static ArrayList<Coordinate> result;

    public DBConnection(String dbparams, String sqlstatement){
        this.DBparams =dbparams;
        this.SQLStatement = sqlstatement;
    }

    public ArrayList<Coordinate> getDBData() {
        Connection connection = null;
        Statement statement;
        ResultSet resultSet;
        result = new ArrayList<Coordinate>();
        try {
            DBUtil.parseDBparams(this.DBparams);
            connection = DBUtil.getConnection();
            connection.setAutoCommit(false);
            LSIClassCentreDB.initFromDB(connection);
            System.out.println("Initialising DB access: Success");
        } catch (Exception e) {
            System.out.println("Error initialising DB access: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        try {
            statement = connection.createStatement();
            statement.setFetchSize(1000);
            resultSet = statement.executeQuery(this.SQLStatement);
            if (!resultSet.next()){
                System.out.println();
            }
            int cnt = 0;
            while (resultSet.next()) {

                byte[] gao_geometry=resultSet.getBytes(2);
                Geometry geom= SQL.wkb2Geometry(gao_geometry);
                Coordinate coord=((Point)geom).getCoordinate();
                result.add(coord);
                cnt++;
            }

        } catch (Exception e) {
            System.out.println("Error processing DB queries: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        return result;
    }
}

