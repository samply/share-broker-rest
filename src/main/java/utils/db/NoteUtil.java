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

package utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.daos.NoteDao;
import de.samply.share.broker.model.db.tables.pojos.Note;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class provides static methods for CRUD operations for Note Objects
 * 
 * @see de.samply.share.broker.model.db.tables.pojos.Note
 */
public final class NoteUtil {

    // Prevent instantiation
    private NoteUtil() {
    }

    /**
     * Add a note to a project
     *
     * @param text the content of the note to add
     * @param userId the id of the author of the note
     * @param projectId the id of the project where the note shall be pinned to
     */
    public static void addNoteToProject(String text, int userId, int projectId) {
        Note note;
        NoteDao noteDao;

        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            note = new Note();
            note.setContent(text);
            note.setAuthorId(userId);
            note.setProjectId(projectId);

            noteDao = new NoteDao(configuration);
            noteDao.insert(note);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
