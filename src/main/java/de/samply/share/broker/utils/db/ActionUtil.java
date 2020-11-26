package de.samply.share.broker.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.enums.ActionType;
import de.samply.share.broker.model.db.tables.daos.ActionDao;
import de.samply.share.broker.model.db.tables.pojos.Action;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.common.utils.SamplyShareUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

/**
 * This class provides static methods for CRUD operations for Action Objects.
 *
 * @see Action
 */
public final class ActionUtil {

  // Prevent instantiation
  private ActionUtil() {
  }

  /**
   * Get all reminders that are due today.
   * This is used to send mails for the reminders.
   *
   * @return the list of all reminders that are due today
   */
  public static List<Action> getRemindersForToday() {
    List<Action> actions = null;

    try (Connection conn = ResourceManager.getConnection()) {
      DSLContext create = ResourceManager.getDslContext(conn);

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
   * Get all reminders that are due in the given amount of weeks.
   *
   * @param weeks the amount of weeks
   * @return the list of all reminders that are due in the given amount of weeks
   */
  public static List<Action> getRemindersExternalDueInWeeks(int weeks) {
    List<Action> actions = null;
    java.sql.Date threshold = new java.sql.Date(
        SamplyShareUtils.getCurrentDate().getTime() + TimeUnit.DAYS.toMillis(weeks * 7));

    try (Connection conn = ResourceManager.getConnection()) {
      DSLContext create = ResourceManager.getDslContext(conn);

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
   * Insert an action to the database.
   *
   * @param action the action to insert
   */
  public static void insertAction(Action action) {
    ActionDao actionDao;

    try (Connection conn = ResourceManager.getConnection()) {
      Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
      actionDao = new ActionDao(configuration);
      actionDao.insert(action);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Add an action that indicates that an inquiry belonging to a project has been edited.
   *
   * @param user      the user that edited the inquiry
   * @param projectId the id of the project in which the inquiry was edited
   */
  public static void addInquiryEditActionToProject(User user, int projectId) {
    Action action;
    ActionDao actionDao;

    try (Connection conn = ResourceManager.getConnection()) {
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
