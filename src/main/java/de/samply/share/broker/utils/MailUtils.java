/*
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
package de.samply.share.broker.utils;

import com.google.common.base.Splitter;
import de.samply.common.mailing.EmailBuilder;
import de.samply.common.mailing.MailSender;
import de.samply.common.mailing.MailSending;
import de.samply.common.mailing.OutgoingEmail;
import de.samply.config.util.FileFinderUtil;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.thread.MailSenderThread;
import de.samply.share.broker.utils.db.ActionUtil;
import de.samply.share.broker.utils.db.ContactUtil;
import de.samply.share.broker.utils.db.InquiryUtil;
import de.samply.share.broker.utils.db.ProjectUtil;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.ProjectStage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * An utility class holding methods to send emails.
 * <p>
 * Templates stored in src/main/resources/mailTemplates
 */
public class MailUtils {
    private static final Logger logger = LogManager.getLogger(MailUtils.class);
    private static final String MAIL_SUBJECT_REGISTRATION_DE = "Ihre Registrierung am Samply.Share Suchbroker";
    private static final String MAIL_SUBJECT_REGISTRATION_EN = "Your registration with Samply.Share.Broker";


    /**
     * Send activation mail.
     * Mail content can be changed in the soy templates
     *
     * @param receiverAddress the email address of the receiver
     * @param token           the token used to verify the mail address
     */
    public static void sendActivationmail(String receiverAddress, String token) {
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        email.addAddressee(receiverAddress);
        if (projectName.equalsIgnoreCase("dktk")) {
            email.setLocale("de");
            email.setSubject(MAIL_SUBJECT_REGISTRATION_DE);
        } else {
            email.setLocale("en");
            email.setSubject(MAIL_SUBJECT_REGISTRATION_EN);
        }
        email.putParameter("token", token);
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("NewRegistrationContent.soy", "NewRegistrationContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }

    /**
     * Initializes an EmailBuilder with the two default Soy-Templates: main.soy and footer.soy.
     *
     * @param mailSending
     * @return an email builder instance
     */
    private static EmailBuilder initializeBuilder(MailSending mailSending) {
        String templateFolder = Utils.getRealPath(mailSending.getTemplateFolder());

        EmailBuilder builder = new EmailBuilder(templateFolder, false);
        builder.addTemplateFile("MainMailTemplate.soy", null);
        builder.addTemplateFile("Footer.soy", "Footer");
        return builder;
    }

    public static void sendStatistics() throws IOException {
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        email.setSubject("Sample Locator Statistic");
        File addressFile = FileFinderUtil.findFile("statistic_notification.txt", projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));
        Stream<String> streams = Files.lines(addressFile.toPath(), StandardCharsets.UTF_8);
        streams.forEach(email::addAddressee);
        DateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");
        String statistics = System.getProperty("catalina.base") + File.separator + "logs" + File.separator + "statistics" + File.separator + "statistic_" + parseFormat.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) + ".xlsx";
        email.getAttachmentPaths().add(statistics);
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));
        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
}
