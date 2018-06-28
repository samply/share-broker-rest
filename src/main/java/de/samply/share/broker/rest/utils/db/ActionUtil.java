/**
 * Copyright (C) 2015 Working Group on Joint Research, University Medical Center Mainz
 * Contact: info@osse-register.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with Jersey (https://jersey.java.net) (or a modified version of that
 * library), containing parts covered by the terms of the General Public
 * License, version 2.0, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */

package de.samply.share.broker.rest.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.enums.ActionType;
import de.samply.share.broker.model.db.tables.daos.ActionDao;
import de.samply.share.broker.model.db.tables.pojos.Action;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.common.utils.SamplyShareUtils;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class provides static methods for CRUD operations for Action Objects
 * 
 * @see de.samply.share.broker.model.db.tables.pojos.Action
 */
public final class ActionUtil {
    
    // Prevent instantiation
    private ActionUtil() {
    }

    /**
     * Get all reminders for a given project id, ordered by date (ascending)
     *
     * @param projectId the id of the project
     * @return the list of reminders for the project
     */
    public static List<Action> getRemindersForProject(int projectId) {
        List<Action> actions = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .where(Tables.ACTION.PROJECT_ID.equal(projectId))
                        .and(Tables.ACTION.TYPE.equal(ActionType.AT_REMINDER))
                    .orderBy(Tables.ACTION.DATE.asc())
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Get all external reminders for a given project id, ordered by date (ascending)
     *
     * @param projectId the id of the project
     * @return the list of external reminders for the project
     */
    public static List<Action> getRemindersExternalForProject(int projectId) {
        List<Action> actions = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .where(Tables.ACTION.PROJECT_ID.equal(projectId))
                        .and(Tables.ACTION.TYPE.equal(ActionType.AT_REMINDER_EXTERNAL))
                    .orderBy(Tables.ACTION.DATE.asc())
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Get all callbacks for a given project id, ordered by date (descending)
     *
     * @param projectId the id of the project
     * @return the list of callbacks for the project
     */
    public static List<Action> getCallbacksForProject(int projectId) {
        List<Action> actions = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .where(Tables.ACTION.PROJECT_ID.equal(projectId))
                        .and(Tables.ACTION.TYPE.in(ActionType.AT_PROJECT_CALLBACK_RECEIVED, ActionType.AT_PROJECT_CALLBACK_SENT))
                    .orderBy(Tables.ACTION.TIME.desc())
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Get all reminders that are due today
     *
     * This is used to send mails for the reminders
     *
     * @return the list of all reminders that are due today
     */
    public static List<Action> getRemindersForToday() {
        List<Action> actions = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .where(Tables.ACTION.DATE.equal(DSL.currentDate()))
                        .and(Tables.ACTION.TYPE.equal(ActionType.AT_REMINDER))
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Get all reminders that are due in the given amount of weeks
     *
     * @param weeks the amount of weeks
     * @return the list of all reminders that are due in the given amount of weeks
     */
    public static List<Action> getRemindersExternalDueInWeeks(int weeks) {
        List<Action> actions = null;
        java.sql.Date threshold = new java.sql.Date(SamplyShareUtils.getCurrentDate().getTime() + TimeUnit.DAYS.toMillis(weeks * 7) );

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .where(Tables.ACTION.DATE.equal(threshold))
                        .and(Tables.ACTION.TYPE.equal(ActionType.AT_REMINDER_EXTERNAL))
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Insert an action to the database
     *
     * @param action the action to insert
     */
    public static void insertAction(Action action) {
        ActionDao actionDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            actionDao = new ActionDao(configuration);
            actionDao.insert(action);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a reminder
     *
     * @param reminderId the id of the reminder to delete
     */
    public static void deleteReminder(int reminderId) {
        ActionDao actionDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            actionDao = new ActionDao(configuration);
            actionDao.deleteById(reminderId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a list of all recent actions in descending order
     *
     * @return list of all recent actions in descending order
     */
    public static List<Action> getRecentActions() {
        List<Action> actions = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .where(
                        ((Tables.ACTION.DATE).lessThan(DSL.currentDate()))
                        .or((Tables.ACTION.DATE).equal(DSL.currentDate())
                            .and(Tables.ACTION.TIME.isNotNull())
                            .and(Tables.ACTION.TIME.lessThan(DSL.currentTime()))
                        )
                    )
                    .orderBy(Tables.ACTION.DATE.desc(), Tables.ACTION.TIME.desc().nullsFirst())
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Get a list of all upcoming actions in ascending order
     *
     * @return list of all upcoming actions in ascending order
     */
    public static List<Action> getUpcomingActions() {
        List<Action> actions = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .where(
                        ((Tables.ACTION.DATE).greaterThan(DSL.currentDate()))
                        .or((Tables.ACTION.DATE).equal(DSL.currentDate())
                            .and((Tables.ACTION.TIME.isNull())
                            .or(Tables.ACTION.TIME.greaterThan(DSL.currentTime())))
                        )
                    )
                    .orderBy(Tables.ACTION.DATE.asc(), Tables.ACTION.TIME.asc().nullsLast())
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Get a list of all actions in descending order
     *
     * @return list of all actions in descending order
     */
    public static List<Action> getAllActions() {
        List<Action> actions = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            actions = create.select()
                    .from(Tables.ACTION)
                    .orderBy(Tables.ACTION.DATE.desc(), Tables.ACTION.TIME.desc().nullsFirst())
                    .fetchInto(Action.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return actions;
    }

    /**
     * Add an action that indicates that an inquiry belonging to a project has been edited
     *
     * @param user the user that edited the inquiry
     * @param projectId the id of the project in which the inquiry was edited
     */
    public static void addInquiryEditActionToProject(User user, int projectId) {
        Action action;
        ActionDao actionDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            actionDao = new ActionDao(configuration);
            action = new Action();
            action.setProjectId(projectId);
            action.setUserId(user.getId());
            action.setType(ActionType.AT_INQUIRY_EDITED);
            action.setMessage(ActionType.AT_INQUIRY_EDITED.toString());
            action.setTime(SamplyShareUtils.getCurrentTime());
            actionDao.insert(action);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
