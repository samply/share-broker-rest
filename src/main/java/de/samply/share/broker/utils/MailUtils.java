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
package de.samply.share.broker.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.faces.application.ProjectStage;

import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import de.samply.common.mailing.EmailBuilder;
import de.samply.common.mailing.MailSender;
import de.samply.common.mailing.MailSending;
import de.samply.common.mailing.OutgoingEmail;
import de.samply.share.broker.model.db.tables.pojos.Action;
import de.samply.share.broker.model.db.tables.pojos.Bank;
import de.samply.share.broker.model.db.tables.pojos.Contact;
import de.samply.share.broker.model.db.tables.pojos.Inquiry;
import de.samply.share.broker.model.db.tables.pojos.Project;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.thread.MailSenderThread;
import de.samply.share.broker.utils.db.ActionUtil;
import de.samply.share.broker.utils.db.ContactUtil;
import de.samply.share.broker.utils.db.InquiryUtil;
import de.samply.share.broker.utils.db.ProjectUtil;
import de.samply.share.broker.utils.db.SiteUtil;
import de.samply.share.common.utils.ProjectInfo;

/**
 * An utility class holding methods to send emails.
 * 
 * Templates stored in src/main/resources/mailTemplates
 */
public class MailUtils {
    private static final Logger logger = LogManager.getLogger(MailUtils.class);
    
    private static final char MAIL_DELIMITER_CHAR = ',';
    
    private static final String CCP_OFFICE_MAIL = "mail.receiver.ccpoffice";
    private static final String ADMIN_MAIL = "mail.receiver.admin";
    private static final String MAIL_SWITCH = "mail.switch";

    private static final String MAIL_SUBJECT_EXPIRY_DE = "Eine Kollaborationsanfrage läuft ab";
    private static final String MAIL_SUBJECT_EXPIRY_EN = "Inquiry about to expire";
    
    private static final String MAIL_SUBJECT_REGISTRATION_DE = "Ihre Registrierung am Samply.Share Suchbroker";
    private static final String MAIL_SUBJECT_REGISTRATION_EN = "Your registration with Samply.Share.Broker";
    
    private static final String MAIL_SUBJECT_NEWPROPOSAL_DE = "[Dezentrale Suche] Neuer Projektvorschlag";
    private static final String MAIL_SUBJECT_NEWPROPOSAL_EN = "[Decentral Search] New Project Proposal";
    
    private static final String MAIL_SUBJECT_MODIFIEDPROPOSAL_DE = "[Dezentrale Suche] Bearbeiteter Projektvorschlag";
    private static final String MAIL_SUBJECT_MODIFIEDPROPOSAL_EN = "[Decentral Search] Modified Project Proposal";
    
    private static final String MAIL_SUBJECT_REMINDER_DE = "Erinnerung für Projekt (Antragsnummer: ";
    private static final String MAIL_SUBJECT_REMINDER_EN = "Reminder for project (Number: ";

    // TODO: translations needed
    private static final String MAIL_SUBJECT_ASSESSMENT_DE = "Begutachtung Ihres Projektvorschlags";
    private static final String MAIL_SUBJECT_ASSESSMENT_EN = "Begutachtung Ihres Projektvorschlags";
    
    private static final String MAIL_SUBJECT_GRANTED_DE = "Weiterleitung Ihres Projektvorschlags";
    private static final String MAIL_SUBJECT_GRANTED_EN = "Weiterleitung Ihres Projektvorschlags";
    
    private static final String MAIL_SUBJECT_REJECTED_DE = "Ablehnung Ihres Projektvorschlags";
    private static final String MAIL_SUBJECT_REJECTED_EN = "Ablehnung Ihres Projektvorschlags";
    
    private static final String MAIL_SUBJECT_CALLBACK_DE = "Rückfragen zu Ihrem Projektvorschlag";
    private static final String MAIL_SUBJECT_CALLBACK_EN = "Rückfragen zu Ihrem Projektvorschlag";
    
    private static final String MAIL_SUBJECT_ACTIVATED_DE = "Aktivierung Ihres Projekts";
    private static final String MAIL_SUBJECT_ACTIVATED_EN = "Aktivierung Ihres Projekts";
    
    private static final String MAIL_SUBJECT_REPORT_DE = "Zwischenbericht für Ihr Projekt";
    private static final String MAIL_SUBJECT_REPORT_EN = "Zwischenbericht für Ihr Projekt";
    
    private static final String MAIL_SUBJECT_ENDING1_DE = "Ihr Projekt";
    private static final String MAIL_SUBJECT_ENDING1_EN = "Your Project";
    
    private static final String MAIL_SUBJECT_ENDING2_DE = "Update erforderlich";
    private static final String MAIL_SUBJECT_ENDING2_EN = "Update required";
    
    private static final String MAIL_SUBJECT_ARCHIVE1_DE = "Ihr Projektvorschlag";
    private static final String MAIL_SUBJECT_ARCHIVE1_EN = "Your project proposal";
    
    private static final String MAIL_SUBJECT_ARCHIVE2_DE = "wird archiviert";
    private static final String MAIL_SUBJECT_ARCHIVE2_EN = "is being archived";
    


    /**
     * Check and send user-defined reminders.
     */
    public static void checkAndSendReminders() {
        List<Action> reminders = ActionUtil.getRemindersForToday();
        if (!SamplyShareUtils.isNullOrEmpty(reminders)) {
            for (Action reminder : reminders) {
                MailUtils.sendReminder(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL), reminder);
            }
            logger.debug("Sent " + reminders.size() + " reminders.");
        }
    }

    /**
     * Check and send deadline reminders to project leaders
     */
    public static void checkAndSendRemindersExternal() {
        List<Action> reminders = ActionUtil.getRemindersExternalDueInWeeks(8);
        if (!SamplyShareUtils.isNullOrEmpty(reminders)) {
            for (Action reminder : reminders) {
                Project project = ProjectUtil.fetchProjectById(reminder.getProjectId());
                MailUtils.sendReportReminder(project, reminder);
            }
            logger.debug("Sent " + reminders.size() + " reminders.");
        }
    }

    /**
     * Check and send expiry notifications.
     */
    public static void checkAndSendNotifications() {
        // Disabled due to request by ccp office
        if (true) {
            return;
        }
        logger.debug("Checking for expiring inquiries");
        List<Inquiry> inquiries = InquiryUtil.getInquiriesThatExpireInDays(3);
        if (inquiries == null || inquiries.size() < 1) {
            logger.debug("No expiry warnings to send");
        } else {
            for (Inquiry inquiry : inquiries) {
                MailUtils.sendExpiryWarning(inquiry);
            }
            logger.debug("Sent " + inquiries.size() + " expiry warnings.");
        }
    }
    
    /**
     * Check and send time-based reminders to send reports
     *
     * TODO: Maybe this can be substituted with general external reminder
     */
    public static void checkAndSendReportReminders() {
        List<Project> projects = ProjectUtil.getProjectsThatEndInDays(14);
        if (!SamplyShareUtils.isNullOrEmpty(projects)) {
            for (Project project : projects) {
                MailUtils.sendProjectEndingInfo(project);
            }
            logger.debug("Sent " + projects.size() + " project ending reminders.");
        }
    }
    
    /**
     * Send activation mail.
     * Mail content can be changed in the soy templates
     *
     * @param receiverAddress the email address of the receiver
     * @param token the token used to verify the mail address
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
     * Send new project proposal email.
     * Mail content can be changed in the soy templates
     *
     * @param receiverAddress the email address of the receiver
     */
    public static void sendProjectProposalMail(String receiverAddress) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        email.addAddressee(receiverAddress);
        if (projectName.equalsIgnoreCase("dktk")) {
            email.setLocale("de");
            email.setSubject(MAIL_SUBJECT_NEWPROPOSAL_DE);
        } else {
            email.setLocale("en");
            email.setSubject(MAIL_SUBJECT_NEWPROPOSAL_EN);
        }
        email.putParameter("toOffice", "true");
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("NewProjectProposalContent.soy", "NewProjectProposalContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }

    /**
     * Send modified project proposal email.
     * Mail content can be changed in the soy templates
     *
     * @param receiverAddress the email address of the receiver
     */
    public static void sendModifiedProjectProposalMail(String receiverAddress) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        email.addAddressee(receiverAddress);
        if (projectName.equalsIgnoreCase("dktk")) {
            email.setLocale("de");
            email.setSubject(MAIL_SUBJECT_MODIFIEDPROPOSAL_DE);
        } else {
            email.setLocale("en");
            email.setSubject(MAIL_SUBJECT_MODIFIEDPROPOSAL_EN);
        }
        email.putParameter("toOffice", "true");
        email.putParameter("modified", "true");
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("NewProjectProposalContent.soy", "NewProjectProposalContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }

    /**
     * Send expiry warning mail.
     * Mail content can be changed in the soy templates
     *
     * @param inquiry the expiring inquiry
     */
    public static void sendExpiryWarning(Inquiry inquiry) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        java.sql.Date expiryDate = inquiry.getExpires();
        email.addAddressee(InquiryUtil.getUserForInquiry(inquiry).getEmail());
        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_EXPIRY_DE);
        } else {
            email.setSubject(MAIL_SUBJECT_EXPIRY_EN);
        }
        email.setLocale(locale);
        email.putParameter("inquiryName", inquiry.getLabel());
        email.putParameter("expiryDate", sqlDateToString(expiryDate, locale));
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("ExpiryContent.soy", "ExpiryContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }

    /**
     * Send reminder mail.
     * Mail content can be changed in the soy templates
     *
     * @param receiverAddress where to send the mail to
     * @param action which action causes the mail to be sent
     */
    public static void sendReminder(String receiverAddress, Action action) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        String applicationNumber = getAppNr(action);
        email.addAddressee(receiverAddress);
        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_REMINDER_DE + applicationNumber + ")");
        } else {
            email.setSubject(MAIL_SUBJECT_REMINDER_EN + applicationNumber + ")");
        }
        email.setLocale(locale);
        email.putParameter("appnr", applicationNumber);
        email.putParameter("reminder", action.getMessage());
        email.putParameter("toOffice", "true");
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));


        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("ReminderContent.soy", "ReminderContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }


    /**
     * Send assessment information mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: ccp office reports project proposal as sent to assessment
     * sender: ccp office
     * receiver: inquirer
     *
     * @param project the project that is afflicted
     */
    public static void sendAssessmentInfo(Project project) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String appNrString = Integer.toString(project.getApplicationNumber());
        String projectLabel = project.getName();

        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_ASSESSMENT_DE + " " + projectLabel + " - " + appNrString);
        } else {
            email.setSubject(MAIL_SUBJECT_ASSESSMENT_EN + " " + projectLabel + " - " + appNrString);
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);
        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("AssessmentContent.soy", "AssessmentContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send project granted information mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: ccp office grants project on web interface
     * sender: ccp office
     * receiver: inquirer
     *
     * @param project the project that is afflicted
     * @param message the message to send to the inquirer
     * @param siteIdList the list of ids of the sites, the project proposal will be sent to
     */
    public static void sendProjectGrantedInfo(Project project, String message, List<String> siteIdList) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String appNrString = Integer.toString(project.getApplicationNumber());
        String projectLabel = project.getName();

        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_GRANTED_DE + " " + projectLabel + " - " + appNrString);
        } else {
            email.setSubject(MAIL_SUBJECT_GRANTED_EN + " " + projectLabel + " - " + appNrString);
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);

        List<String> siteList = SiteUtil.getSiteNamesByIds(siteIdList);

        String sites = Joiner.on(", ").join(siteList);

        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        email.putParameter("assessment", project.getExternalAssessment() ? "true" : null);
        email.putParameter("message", message);
        email.putParameter("sites", sites);
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("GrantProjectContent.soy", "GrantProjectContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send project rejected information mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: ccp office rejects project on web interface
     * sender: ccp office
     * receiver: inquirer
     *
     * @param project the project that is afflicted
     * @param message the message to send to the inquirer
     */
    public static void sendProjectRejectedInfo(Project project, String message) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String appNrString = Integer.toString(project.getApplicationNumber());
        String projectLabel = project.getName();

        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_REJECTED_DE + " " + projectLabel + " - " + appNrString);
        } else {
            email.setSubject(MAIL_SUBJECT_REJECTED_EN + " " + projectLabel + " - " + appNrString);
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);

        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        email.putParameter("assessment", project.getExternalAssessment() ? "true" : null);
        email.putParameter("message", message);
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("RejectProjectContent.soy", "RejectProjectContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send project granted information mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: ccp office grants project on web interface
     * sender: ccp office
     * receiver: inquirer
     *
     * @param project the project that is afflicted
     * @param message the message to send to the inquirer
     */
    public static void sendProjectCallbackInfo(Project project, String message) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String appNrString = Integer.toString(project.getApplicationNumber());
        String projectLabel = project.getName();

        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_CALLBACK_DE + " " + projectLabel + " - " + appNrString);
        } else {
            email.setSubject(MAIL_SUBJECT_CALLBACK_EN + " " + projectLabel + " - " + appNrString);
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);

        List<Site> siteList = ProjectUtil.fetchProjectPartners(project);
        String sites = Joiner.on(", ").join(siteList);


        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        email.putParameter("message", message);
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("CallbackProjectContent.soy", "CallbackProjectContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send project activated information mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: ccp office grants project on web interface
     * sender: ccp office
     * receiver: inquirer
     *
     * @param project the project that is afflicted
     * @param partnerIdList a list of ids of the project partners
     */
    public static void sendProjectActivatedInfo(Project project, List<String> partnerIdList) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        Integer applicationNumber = project.getApplicationNumber();
        String appNrString;
        if (applicationNumber != null) {
            appNrString = Integer.toString(applicationNumber);
        } else {
            appNrString = "-";
        }
        String projectLabel = project.getName();

        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_ACTIVATED_DE + " " + projectLabel + " - " + appNrString);
        } else {
            email.setSubject(MAIL_SUBJECT_ACTIVATED_EN + " " + projectLabel + " - " + appNrString);
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);

        List<String> siteList = SiteUtil.getSiteNamesByIds(partnerIdList);

        String sites = Joiner.on(", ").join(siteList);

        int months = SamplyShareUtils.getMonthsDiff(project.getStarted(), project.getEndEstimated());

        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        email.putParameter("months", Integer.toString(months));
        email.putParameter("end_planned", WebUtils.isoToGermanDateString(project.getEndEstimated()));
        email.putParameter("partners", sites);
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));
        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("ActivateProjectContent.soy", "ActivateProjectContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send report reminder mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: 8 weeks before the report is due
     * sender: ccp office
     * receiver: inquirer
     *
     * @param project the project that is afflicted
     * @param reminder the reminder that triggered this mail
     */
    public static void sendReportReminder(Project project, Action reminder) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String appNrString = Integer.toString(project.getApplicationNumber());
        String projectLabel = project.getName();

        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_REPORT_DE + " " + projectLabel + " - " + appNrString);
        } else {
            email.setSubject(MAIL_SUBJECT_REPORT_EN + " " + projectLabel + " - " + appNrString);
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);

        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        email.putParameter("start", WebUtils.isoToGermanDateString(project.getStarted()));
        email.putParameter("dueDate", WebUtils.isoToGermanDateString(reminder.getDate()));
        if (reminder.getMessage() != null && reminder.getMessage().length() > 0) {
            email.putParameter("message", reminder.getMessage());
        }
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("ProjectReportContent.soy", "ProjectReportContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send project ending information mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: estimated end of project in 2 weeks
     * sender: ccp office
     * receiver: inquirer
     *
     * @param project the project that is afflicted
     */
    public static void sendProjectEndingInfo(Project project) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String appNrString = Integer.toString(project.getApplicationNumber());
        String projectLabel = project.getName();

        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            email.setSubject(MAIL_SUBJECT_ENDING1_DE + " " + projectLabel + " - " + appNrString + " - " + MAIL_SUBJECT_ENDING2_DE);
        } else {
            email.setSubject(MAIL_SUBJECT_ENDING1_EN + " " + projectLabel + " - " + appNrString + " - " + MAIL_SUBJECT_ENDING2_EN);
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);

        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        email.putParameter("end_planned", WebUtils.isoToGermanDateString(project.getEndEstimated()));
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("ProjectEndingContent.soy", "ProjectEndingContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send project archived information mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: ccp office closes project on web interface
     * sender: ccp office
     * receiver: project leader
     *
     * @param project the project that is afflicted
     * @param message the message to send to the project leader
     * @param hasFinalReport true if a final report is available for the project
     */
    public static void sendProjectArchivedInfo(Project project, String message, boolean hasFinalReport) {
        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
            return;
        }
        String mailSwitch = ProjectInfo.INSTANCE.getConfig().getProperty(MAIL_SWITCH);
        if (mailSwitch == null || !mailSwitch.equalsIgnoreCase("on")) {
            logger.debug("Project mail switch is off. Skipping mail sending. You can change this in the config file. The parameter is called " + MAIL_SWITCH);
            return;            
        }
        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        Integer applicationNumber = project.getApplicationNumber();
        String appNrString;
        if (applicationNumber != null) {
            appNrString = Integer.toString(applicationNumber);
        } else {
            appNrString = "-";
        }
        String projectLabel = project.getName();
        boolean wasActive = project.getEndActual() != null ;
        String locale = "en";
        if (projectName.equalsIgnoreCase("dktk")) {
            locale = "de";
            if (wasActive) {
                email.setSubject(MAIL_SUBJECT_ENDING1_DE + " " + projectLabel + " - " + appNrString + " - " + MAIL_SUBJECT_ARCHIVE2_DE);
            } else {
                email.setSubject(MAIL_SUBJECT_ARCHIVE1_DE + " " + projectLabel + " - " + appNrString + " - " + MAIL_SUBJECT_ARCHIVE2_DE);
            }
        } else {
            if (wasActive) {
                email.setSubject(MAIL_SUBJECT_ENDING1_EN + " " + projectLabel + " - " + appNrString + " - " + MAIL_SUBJECT_ARCHIVE2_EN);
            } else {
                email.setSubject(MAIL_SUBJECT_ARCHIVE1_EN + " " + projectLabel + " - " + appNrString + " - " + MAIL_SUBJECT_ARCHIVE2_EN);
            }
        }
        User projectLeader = ProjectUtil.getProjectLeader(project);
        Contact plContact = ContactUtil.getContactForUser(projectLeader);

        email.addAddressee(projectLeader.getEmail());
        email.addReplyTo(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.addCcRecipient(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        email.setLocale(locale);
        email.putParameter("projectName", projectLabel);
        email.putParameter("projectNr", appNrString);
        email.putParameter("office", "true");
        email.putParameter("message", message);
        if (wasActive) {
            email.putParameter("end_actual", WebUtils.isoToGermanDateString(project.getEndActual()));
        }
        if (hasFinalReport) {
            email.putParameter("has_final_report", "true");
        }
        if (plContact.getTitle() != null && plContact.getTitle().length() > 0) {
            email.putParameter("title", plContact.getTitle());
        }
        email.putParameter("name", projectLeader.getName());
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("ArchiveProjectContent.soy", "ArchiveProjectContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send user site assignment mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: user sets a site for himself
     * sender: user
     * receiver: admin
     *
     * @param user the user that triggered the change
     * @param site the site the user wants to be associated with
     */
    public static void sendUserSiteMail(User user, Site site) {
//        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
//            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
//            return;
//        }

        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String userMail = user.getEmail();
        String siteName = site.getName();
        String siteId;
        try {
            siteId = Integer.toString(site.getId());
        } catch (NumberFormatException nfe) {
            siteId = "error";
        }

        String locale = "de";

        email.setSubject("[broker] User assignment requested: " + userMail + " -> " + siteName);

        List<String> addressees = Splitter.on(MAIL_DELIMITER_CHAR).trimResults().omitEmptyStrings().splitToList(ProjectInfo.INSTANCE.getConfig().getProperty(ADMIN_MAIL));
        for (String addressee : addressees) {
            email.addAddressee(addressee);
        }

//            email.addAddressee(ProjectInfo.INSTANCE.getConfig().getProperty(ADMIN_MAIL));
        email.setLocale(locale);
        email.putParameter("userName", user.getName());
        email.putParameter("userAuthId", user.getAuthid());
        email.putParameter("userMail", userMail);
        email.putParameter("siteId", siteId);
        email.putParameter("siteName", siteName);
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("UserSiteContent.soy", "UserSiteContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    

    /**
     * Send bank site assignment mail.
     * Mail content can be changed in the soy templates
     * 
     * triggered by: bank sets a site for itself
     * sender: bank
     * receiver: admin
     *
     * @param bank the bank that triggered the change
     * @param site the site the user wants to be associated with
     */
    public static void sendBankSiteMail(Bank bank, Site site) {
//        if (Utils.getProjectStage().equals(ProjectStage.Development)) {
//            logger.debug("Project Stage is DEVELOPMENT. Skipping mail sending.");
//            return;
//        }

        OutgoingEmail email = new OutgoingEmail();
        String projectName = ProjectInfo.INSTANCE.getProjectName();

        String bankMail = bank.getEmail();
        String siteName = site.getName();
        String siteId;
        try {
            siteId = Integer.toString(site.getId());
        } catch (NumberFormatException nfe) {
            siteId = "error";
        }

        String locale = "de";

        email.setSubject("[broker] Bank assignment requested: " + bankMail + " -> " + siteName);

        List<String> addressees = Splitter.on(MAIL_DELIMITER_CHAR).trimResults().omitEmptyStrings().splitToList(ProjectInfo.INSTANCE.getConfig().getProperty(ADMIN_MAIL));
        for (String addressee : addressees) {
            email.addAddressee(addressee);
        }

//            email.addAddressee(ProjectInfo.INSTANCE.getConfig().getProperty(ADMIN_MAIL));
        email.setLocale(locale);
        email.putParameter("bankMail", bankMail);
        email.putParameter("bankId", Integer.toString(bank.getId()));
        email.putParameter("siteId", siteId);
        email.putParameter("siteName", siteName);
        MailSending mailSending = MailSender.loadMailSendingConfig(projectName, System.getProperty("catalina.base") + File.separator + "conf", ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF"));

        EmailBuilder builder = initializeBuilder(mailSending);
        builder.addTemplateFile("BankSiteContent.soy", "BankSiteContent");
        email.setBuilder(builder);

        Thread mailSenderThread = new Thread(new MailSenderThread(mailSending, email));
        mailSenderThread.start();
    }
    
    /**
     * Initializes an EmailBuilder with the two default Soy-Templates: main.soy and footer.soy.
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

    /**
     * Get the application number of the project an action is related to
     *
     * @param action the action that triggers an event
     * @return the application number of the project this action belongs to
     */
    private static String getAppNr(Action action) {
        String appNr = "Unbekannt";
        try {
            Project project = ProjectUtil.fetchProjectById(action.getProjectId());
            int appNrInt = project.getApplicationNumber();
            appNr = Integer.toString(appNrInt);
        } catch (Exception e) {
            logger.warn("Couldn't read or parse application number for reminder " + action.getId());
        }
        return appNr;
    }

    /**
     * Transform an SQL Date to String
     *
     * For German locale, date format will be "dd.MM.yyyy" (e.g. "15.11.2015")
     * For other locales, it will be "dd. MMMMM yyyy" (e.g. "15. November 2015")
     *
     * @param date the sql date object
     * @param lang the language code
     * @return the date string in "dd.MM.yyyy" (for "de") or "dd. MMMMM yyyy" otherwise
     */
    private static String sqlDateToString(java.sql.Date date, String lang) {
        if (lang.equalsIgnoreCase("de")) {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            return df.format(date);
        } else {
            DateFormat df = new SimpleDateFormat("dd. MMMMM yyyy");
            return df.format(date);
        }
    }
}
