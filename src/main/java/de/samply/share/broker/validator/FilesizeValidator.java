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
package de.samply.share.broker.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;

import com.itextpdf.text.pdf.PdfReader;

/**
 * Check if a document is not bigger than the defined threshold of 10MB and has is a pdf file
 *
 * This implements a FacesValidator
 */
@FacesValidator("filesizeValidator")
public class FilesizeValidator implements Validator {

    @Override
    public void validate(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
        List<FacesMessage> msgs = new ArrayList<>();
        Part file = (Part) value;
        if (file.getSize() > 10485760) {
            System.out.println("File too big. Max 10MB.");
            msgs.add(new FacesMessage("Datei zu groß. Maximal 10MB."));
        }
		if (!checkPdf(file)) {
			System.out.println("Content-Type not allowed!: " + file.getContentType() + ". Only PDF is allowed.");
			msgs.add(new FacesMessage("Dateityp nicht erlaubt. Nur PDF Dateien sind erlaubt."));
		}
        if (!msgs.isEmpty()) {
            throw new ValidatorException(msgs);
        }
    }

    /**
     * Check if the file is a pdf file
     *
     * Try to open it with PdfReader. If it fails, it is not a valid pdf file
     *
     * @param file the file to check
     * @return true if it is a pdf, false if it is not
     */
    private boolean checkPdf(Part file) {
    	try {
			PdfReader pdfReader = new PdfReader(file.getInputStream());
			pdfReader.close();
		} catch (IOException e) {
			return false;
		}
    	return true;
    }

}
