package de.samply.share.broker.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

import javax.sql.DataSource;

/**
 * Take care of database migration
 *
 * Uses flyway to upgrade/migrate database schemas
 */
public class Migration {
    private Migration() {
    }

    public static void doUpgrade() throws FlywayException {
        upgradeSamply(ResourceManager.getDataSource());
    }

    /**
     * Upgrade the samply schema
     *
     * @param dataSource which datasource to use
     */
    private static void upgradeSamply(DataSource dataSource) throws FlywayException {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("db/migration");
        flyway.setSchemas("samply");
        flyway.migrate();
    }
}
