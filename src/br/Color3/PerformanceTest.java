package br.Color3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Random;

public class PerformanceTest {
    private static String[][] randomValues;

    private static void generateRandomValues (int imageNumber) {
        Random valueGenerator = new Random();
        DecimalFormat df = new DecimalFormat("###.#######");
        randomValues = new String[imageNumber][imageNumber];
        for (int i = 0; i < randomValues.length; i++) {
            for (int j = 0; j < randomValues.length; j++) {
                randomValues[i][j] = df.format(Math.abs(valueGenerator.nextDouble()) * 100).replace(',', '.');
            }
        }
    }

    private static double createStructuresMysql () {
        Connection conn = null;
        long initialTime = 0;
        long endTime = initialTime;
        int imageQuantity = randomValues.length;

        try {
            conn = DatabaseConnection.mysqlConnect();
            Statement deleteOperation = conn.createStatement();
            deleteOperation.execute("DROP DATABASE IF EXISTS mydb;");
            initialTime = System.currentTimeMillis();

            Statement createOperation = conn.createStatement();
            createOperation.execute("SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;");
            createOperation.execute("SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;");
            createOperation.execute("SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';");
            createOperation.execute("CREATE SCHEMA IF NOT EXISTS mydb;");
            createOperation.execute("USE mydb");
            createOperation.execute("CREATE TABLE IF NOT EXISTS mydb.image ( " +
                    "idImage INT NOT NULL AUTO_INCREMENT, " +
                    "pathImage VARCHAR(500) NOT NULL, " +
                    "PRIMARY KEY (idImage)," +
                    "INDEX fk_image_path (pathImage ASC)) " +
                    "ENGINE = InnoDB;");
            createOperation.execute("CREATE TABLE IF NOT EXISTS mydb.image_distance (" +
                    "image_originId INT NOT NULL, " +
                    "image_destinyId INT NOT NULL, " +
                    "distance VARCHAR(20) NOT NULL, " +
                    "PRIMARY KEY (image_originId, image_destinyId), " +
                    "INDEX fk_image_distance_origin (image_originId ASC), " +
                    "CONSTRAINT fk_image_distance_origin_image FOREIGN KEY (image_originId) REFERENCES mydb.image (idImage) ON DELETE NO ACTION ON UPDATE NO ACTION, " +
                    "CONSTRAINT fk_image_distance_destiny_image FOREIGN KEY (image_destinyId) REFERENCES mydb.image (idImage) ON DELETE NO ACTION ON UPDATE NO ACTION) " +
                    "ENGINE = InnoDB;");
            createOperation.execute("SET SQL_MODE=@OLD_SQL_MODE;");
            createOperation.execute("SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;");
            createOperation.execute("SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;");
            Statement insertOperation = conn.createStatement();

            if (imageQuantity != 0) {
                int index;
                String imageCreationQuery = "INSERT INTO mydb.image (idImage,pathImage) VALUES ";
                for (index = 1; index < imageQuantity; index++) {
                    imageCreationQuery = imageCreationQuery + "("+index+",'imagem/"+index+".jpg'), ";
                }
                imageCreationQuery = imageCreationQuery + "("+index+",'imagem/"+index+".jpg');";
                insertOperation.execute(imageCreationQuery);
//                System.out.println("Inseridos todas as imagens");

                for (int origin = 1; origin <= imageQuantity; origin++) {
                    for (int destiny = 1; destiny <= imageQuantity; destiny++) {
//                        System.out.println("Inserindo relacionamento entre " + origin +" e " +destiny);
                        insertOperation.execute("INSERT INTO mydb.image_distance (image_originId,image_destinyId,distance) VALUES "+"("+origin+","+destiny+","+randomValues[origin-1][destiny-1]+");");
                    }
                }
            }
            endTime = System.currentTimeMillis();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            endTime = System.currentTimeMillis();
            return (double) (endTime - initialTime)/1000;
        }
        finally {
            try {
                if (conn != null) conn.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        double interval = (double) (endTime - initialTime)/1000;
        return interval;
    }

    private static double searchForImageMysql (String path, int similarImageQuantity) {
        long initialTime = 0;
        long finalTime = initialTime;
        double interval = 0;
        int searchId = 0;
        String searchPath = null;

        Connection conn = null;
        String SearchedImageQuery = "SELECT * FROM mydb.image WHERE pathImage = '" + path +"'";

        try {
            conn = DatabaseConnection.mysqlConnect();
            initialTime = System.currentTimeMillis();
            Statement searchedImageOperation = conn.createStatement();
            ResultSet rs = searchedImageOperation.executeQuery(SearchedImageQuery);
            while (rs.next()) {
                searchId = rs.getInt("idImage");
                searchPath = rs.getString("pathImage");
            }

            String SimilarImageQuery = "SELECT B.pathImage AS result " +
                    "FROM mydb.image_distance AS A " +
                    "JOIN mydb.image AS B ON A.image_destinyId = B.idImage " +
                    "WHERE A.image_originId = " + searchId + " " +
                    "ORDER BY A.distance " +
                    "LIMIT " + similarImageQuantity;
            Statement searchSimilarImages = conn.createStatement();
            ResultSet rs2 = searchSimilarImages.executeQuery(SimilarImageQuery);
            while (rs2.next()) {
                searchPath = rs2.getString("result");
//                System.out.println("Imagem encontrada: " + searchPath);
            }
            finalTime = System.currentTimeMillis();
            interval = ((double) finalTime - initialTime)/1000;
        }
        catch (Exception error) {
            error.printStackTrace();
            interval = 0;
        }
        finally {
            try {
                if (conn != null) conn.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return interval;
    }

    private static double createStructuresNeo4j() {
        Connection conn = null;
        long initialTime = 0;
        long endTime = initialTime;
        int imageQuantity = randomValues.length;

        try {
            conn = DatabaseConnection.neo4jConnect();
            Statement deleteStatement = conn.createStatement();
            deleteStatement.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n, r");
            Statement createStatement = conn.createStatement();
            String createQuery = "CREATE ";
            initialTime = System.currentTimeMillis();
            if (imageQuantity != 0) {
                int index = 1;
                for (index = 1; index < imageQuantity; index++) {
                    createQuery = createQuery +
                            "(img" + index + ":image {idImage: " + index + ", pathImage: 'imagem/" + index + ".jpg'} ), ";
                }
                createQuery = createQuery +
                        "(img" + index + ":image {idImage: " + index + ", pathImage: 'imagem/" + index + ".jpg'} )";
                createStatement.execute(createQuery);
                createStatement.execute("CREATE INDEX ON :image (idImage)");
                createStatement.execute("CREATE INDEX ON :image (pathImage)");

                for (int origin = 1; origin <= imageQuantity; origin++) {
                    for (int destiny = 1; destiny <= imageQuantity; destiny++) {
                        createStatement.execute("MATCH (origin:image {idImage: " + origin + "}) USING INDEX origin:image(idImage) " +
                                "MATCH (destiny:image {idImage: " + destiny + "}) USING INDEX destiny:image(idImage) " +
                                "CREATE (origin)-[rel:distance { dist: " + randomValues[origin-1][destiny-1] + " }]->(destiny) " +
                                "RETURN rel");
                    }
                }
            }
            endTime = System.currentTimeMillis();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            endTime = System.currentTimeMillis();
            return ((double) endTime - initialTime) / 1000;
        }
        finally {
            try {
                if (conn != null) conn.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        double interval = ((double) endTime - initialTime)/1000;
        return interval;
    }

    private static double searchForImageNeo4j (String path, int similarQuantity) {
        long initialTime = 0;
        long endTime = initialTime;
        double interval = 0;
        String searchPath = "";
        Connection conn = null;

        try {
            conn = DatabaseConnection.neo4jConnect();
            initialTime = System.currentTimeMillis();
            String similarImageQuery = "MATCH (p1:image {pathImage: '" + path + "' })-[d:distance]->(p2:image) " +
                    "USING INDEX p1:image(pathImage) " +
                    "WITH p2, d.dist AS sim " +
                    "ORDER BY sim DESC " +
                    "LIMIT " + similarQuantity + " " +
                    "RETURN p2.pathImage AS result";
            Statement searchSimilarImages = conn.createStatement();
            ResultSet rs2 = searchSimilarImages.executeQuery(similarImageQuery);
            while (rs2.next()) {
                searchPath = rs2.getString("result");
            }
            endTime = System.currentTimeMillis();
            interval = ((double) endTime - initialTime)/1000;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            interval = 0;
        }
        finally {
            try {
                if (conn != null) conn.close();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return interval;
    }

    public static void main (String[] args) {
        try {
            generateRandomValues(10);
//            createStructuresMysql();
//            searchForImageMysql("imagem/1.jpg", 5);
            createStructuresNeo4j();
            searchForImageNeo4j("imagem/1.jpg", 5);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
