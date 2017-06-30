package arse;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import fu.esi.SQL;
import fu.util.DBUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


class DBConnection {
    private static String DBparams;
    private static String SQLStatement;
    private static ArrayList<ResultClass> result;

    DBConnection(String dbparams, String sqlstatement){
        DBparams =dbparams;
        SQLStatement = sqlstatement;
    }

    ArrayList<ResultClass> getDBData() {
        Connection connection = null;
        Statement statement;
        ResultSet resultSet;
        result = new ArrayList<ResultClass>();
        try {
            DBUtil.parseDBparams(DBparams);
            connection = DBUtil.getConnection();
            System.out.println("\nInitialising DB access: Success");
        } catch (Exception e) {
            System.out.println("Error initialising DB access: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        try {
            statement = connection.createStatement();
            statement.setFetchSize(1000);
            resultSet = statement.executeQuery(SQLStatement);
            if (!resultSet.next()){
                System.out.println();
            }

            while (resultSet.next()) {
                //get realname from resultSet
                String name = resultSet.getString(1);

                //get Coordinates from resultSet
                byte[] gao_geometry=resultSet.getBytes(2);
                Geometry geom= SQL.wkb2Geometry(gao_geometry);
                Coordinate coord= geom.getCoordinate();

                ResultClass re=new ResultClass(coord,name);
                result.add(re);
            }

        } catch (Exception e) {
            System.out.println("Error processing DB queries: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }
        return result;
    }
}

