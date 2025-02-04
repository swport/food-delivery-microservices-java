package com.abc.inc.pca;

import com.abc.inc.pca.config.RdbClient;
import com.abc.inc.pca.dto.RestaurantProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RestaurantRepository {
    private static final RdbClient mysqlClient = new RdbClient();
    private static final Logger log = LoggerFactory.getLogger(RestaurantRepository.class);

    public static boolean updateRestaurantProfile(RestaurantProfile restaurantProfile, Integer user_id) {
        Connection connection = null;
        boolean ok = true;

        try {
            connection = mysqlClient.getConnection();

            String query = "UPDATE restaurants SET name = ?, location = ? WHERE id = ? AND user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, restaurantProfile.name());
                statement.setString(2, restaurantProfile.location());
                statement.setInt(3, restaurantProfile.id());
                statement.setInt(4, user_id);

                statement.executeQuery();

                ProductRepository.updateRestaurantInfo(restaurantProfile);
            }
        } catch (SQLException e) {
            log.error("Database error: ", e);
            ok = false;
        } finally {
            // Return the connection to the pool
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                    log.error("Error closing connection: ", e);
                }
            }
        }
        return ok;
    }
}
