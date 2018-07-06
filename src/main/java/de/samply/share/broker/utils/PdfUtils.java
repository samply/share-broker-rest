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
package de.samply.share.broker.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.samply.share.broker.utils.db.*;
import de.samply.share.common.utils.QueryTreeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.model.tree.TreeModel;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.db.tables.pojos.Contact;
import de.samply.share.broker.model.db.tables.pojos.Inquiry;
import de.samply.share.broker.model.db.tables.pojos.Project;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.common.model.uiquerybuilder.EnumOperator;
import de.samply.share.common.model.uiquerybuilder.QueryItem;

/**
 * Utility class to create PDF files
 */
public class PdfUtils {

    private static final Logger logger = LogManager.getLogger(PdfUtils.class);

    public static final Font titleFont = new Font( Font.FontFamily.HELVETICA, 16, Font.BOLD );
    public static final Font subtitleFont = new Font( Font.FontFamily.HELVETICA, 12, Font.BOLD );
    public static final Font contentFont = new Font( Font.FontFamily.HELVETICA, 10, Font.NORMAL );
    public static final String FILENAME_SUFFIX_PDF = ".pdf";

    /**
     * Create a pdf byte array stream from an inquiry
     *
     * @param inquiry the inquiry to transform to a pdf
     * @return byte array stream of the pdf containing the inquiry information
     */
    public static ByteArrayOutputStream createPdfOutputstream(Inquiry inquiry) throws DocumentException {
        Project project;
        if (inquiry.getProjectId() == null) {
            project = null;
        } else {
            project = ProjectUtil.fetchProjectById(inquiry.getProjectId());
        }

        User user = InquiryUtil.getUserForInquiry(inquiry);
        Contact contact = ContactUtil.getContactForUser(user);
        Site site = UserUtil.getSiteForUser(user);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, bos);
        document.open();
        Paragraph p = new Paragraph();
        p.setFont(titleFont);
        if (project != null) {
            p.add("Projekt: " + project.getName());
        } else if (inquiry.getLabel() == null) {
            p.add("Anfrage: Unbenannt");
        } else {
            p.add("Anfrage: " + inquiry.getLabel());
        }
        p.setSpacingAfter(5);
        document.add(p);
        p.clear();
        
        p.setFont(contentFont);
        if (project != null && project.getApplicationNumber() != null && project.getApplicationNumber() > 0) {
        	p.add("Antragsnummer: " + project.getApplicationNumber());
        } else {
        	p.add("Antragsnummer: Unbekannt");
        }
        p.setSpacingAfter(10);
        document.add(p);
        p.clear();

        p.setFont(subtitleFont);
        p.add("Antragssteller");
        p.setSpacingAfter(5);
        document.add(p);
        p.clear();

        p.setFont(contentFont);
        p.add(user.getName());
        p.add(Chunk.NEWLINE);
        if (site == null) {
            p.add("Standort: Unbekannt");
        } else {
            p.add("Standort: " + site.getName());
        }
        if (user.getEmail() != null) {
            p.add(Chunk.NEWLINE);
            p.add("E-Mail: " + user.getEmail());
        }
        if (contact.getPhone() != null) {
            p.add(Chunk.NEWLINE);
            p.add("Telefon: " + contact.getPhone());
        }

        p.setSpacingAfter(20);
        document.add(p);
        p.clear();

        p.setFont(subtitleFont);
        if (project != null) {
            p.add("Projektbeschreibung");
        } else {
            p.add("Beschreibung");
        }
        p.setSpacingAfter(10);
        document.add(p);
        p.clear();

        p.setFont(contentFont);
        p.add(inquiry.getDescription());
        p.setSpacingAfter(30);
        document.add(p);
        p.clear();


        p.setFont(subtitleFont);
        p.add("Suchkriterien");
        p.setSpacingAfter(10);
        document.add(p);
        p.clear();

        if (inquiry.getCriteria() == null) {
            p.add("Keine Suchkriterien ausgewählt.");
        } else {
            p = queryStringToItextParagraph(inquiry.getCriteria());
        }
        p.setFont(contentFont);
        document.add(p);
        p.clear();

        document.addCreationDate();
        if (inquiry.getLabel() == null) {
            document.addTitle("Unbenannt");
        } else {
            document.addTitle(inquiry.getLabel());
        }

        document.close();

        Integer exposeId = DocumentUtil.getExposeIdByInquiryId(inquiry.getId());
        if (exposeId != null && exposeId > 0) {
            try {
                return concatExpose(bos, exposeId);
            } catch (IOException e) {
                logger.error("Problem with concatenation");
                e.printStackTrace();
            }
        }

        return bos;
    }

    /**
     * Append an expose to a pdf
     *
     * @param pdfIn the base pdf
     * @param exposeId the id of the expose to append
     * @return the base pdf with the expose appended
     */
    private static ByteArrayOutputStream concatExpose(ByteArrayOutputStream pdfIn, int exposeId) throws DocumentException, IOException {
        PdfReader.unethicalreading = true;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfCopy copy = new PdfCopy(document, bos);

        document.open();
        document.newPage();

        PdfReader readerInquiry = new PdfReader(pdfIn.toByteArray());
        PdfReader readerExposee = new PdfReader(DocumentUtil.getDocumentOutputStreamById(exposeId).toByteArray());

        int n = readerInquiry.getNumberOfPages();

        for (int page = 1; page <= n; ++page) {
            copy.addPage(copy.getImportedPage(readerInquiry, page));
        }
        copy.freeReader(readerInquiry);
        readerInquiry.close();

        n = readerExposee.getNumberOfPages();
        for (int page = 1; page <= n; ++page) {
            copy.addPage(copy.getImportedPage(readerExposee, page));
        }
        copy.freeReader(readerExposee);
        readerExposee.close();
        copy.close();
        document.close();
        return bos;
    }

    /**
     * Transform a serialized query criteria string to a paragraph for itext
     *
     * @param queryString the query xml string
     * @return the itext paragraph
     */
    public static Paragraph queryStringToItextParagraph(String queryString) {
        TreeModel<QueryItem> queryTree = QueryTreeUtil.queryStringToTree(queryString);
        Paragraph p = new Paragraph();

        p = addNodeToParagraph(queryTree.getChildren().get(0), p);

        return p;
    }

    /**
     * Add a query node to a paragraph
     *
     * @param node the query node
     * @param p the paragraph to add to
     * @return the paragraph with the attached node
     */
    private static Paragraph addNodeToParagraph(TreeModel<QueryItem> node, Paragraph p) {
        QueryItem qi = node.getData();
        p.add(Chunk.NEWLINE);
        for (int i = 1; i < node.getLevel(); i++) {
            p.add(" ");
        }
        if (node.getChildCount() > 0) {
            p.add(Messages.getString(qi.getConjunction().toString()));
            for (TreeModel<QueryItem> n : node.getChildren()) {
                p = addNodeToParagraph(n, p);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(WebUtils.getDesignation(qi.getMdrId(), "de"));
            sb.append(" ");
            sb.append(WebUtils.getOperator(qi.getOperator().getIcon()));
            if (qi.getOperator() != EnumOperator.IS_NOT_NULL && qi.getOperator() != EnumOperator.IS_NULL) {
                sb.append(" ");
                sb.append(WebUtils.getValueDesignation(qi.getMdrId(), qi.getValue(), "de"));
            }

            p.add(sb.toString());
        }
        return p;
    }

    /**
     * Inject an application number to the application form
     *
     * @param exposeId the id of the expose to modify
     * @param appNr the application to inject
     * @param exposeOutputStream the pdf file with the application number
     */
	public static void injectApplicationNumber(int exposeId, int appNr, ByteArrayOutputStream exposeOutputStream) throws IOException, DocumentException {
		PdfReader reader = new PdfReader(new ByteArrayInputStream(exposeOutputStream.toByteArray()));
//		AcroFields acroFields = reader.getAcroFields();
//		Map<String, Item> fieldItems = acroFields.getFields();
//		acroFields.setField("Antragsnummer", Integer.toString(appNr));	
//		Iterator<?> it = fieldItems.entrySet().iterator();
//		while (it.hasNext()) {
//	        @SuppressWarnings("rawtypes")
//			Map.Entry pair = (Map.Entry)it.next();
//	        System.out.println(pair.getKey() + " = " + pair.getValue());
//	        it.remove(); // avoids a ConcurrentModificationException			
//		}
		ByteArrayOutputStream newOutputStream = new ByteArrayOutputStream();
		PdfStamper stamper = new PdfStamper(reader, newOutputStream);
		AcroFields acroFields = stamper.getAcroFields();
		acroFields.setField("Antragsnummer", Integer.toString(appNr));
		stamper.close();
		DocumentUtil.changeExposeData(exposeId, newOutputStream);
	}

}
