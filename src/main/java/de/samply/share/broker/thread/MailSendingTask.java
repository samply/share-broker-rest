/*
 * Copyright (C) 2015 Working Group on Joint Research,
 * Division of Medical Informatics,
 * Institute of Medical Biometrics, Epidemiology and Informatics,
 * University Medical Center of the Johannes Gutenberg University Mainz
 *
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
package de.samply.share.broker.thread;

import de.samply.share.broker.utils.MailUtils;

/**
 * A task that checks if any reminders and/or notifications have to be sent and sends them if necessary
 *
 * This is realized as a task to avoid blocking the application when trying to send a mail
 */
public class MailSendingTask extends Task {

    public MailSendingTask() {
    }

    /*
     * (non-Javadoc)
     *
     * @see de.samply.share.thread.Task#doIt()
     */
    @Override
    public TaskResult doIt() {
        MailUtils.checkAndSendNotifications();
        MailUtils.checkAndSendReminders();
        MailUtils.checkAndSendRemindersExternal();
        MailUtils.checkAndSendReportReminders();
        return new TaskResult(0, "MailSending Check done");
    }
}
