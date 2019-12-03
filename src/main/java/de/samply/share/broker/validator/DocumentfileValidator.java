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

import de.samply.share.common.utils.SamplyShareUtils;
import org.jooq.tools.StringUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;
import java.util.ArrayList;
import java.util.List;

/**
 * Check if a document is not bigger than the defined threshold of 10MB and has one of the allowed suffixes
 *
 * This implements a FacesValidator
 */
@FacesValidator("documentfileValidator")
public class DocumentfileValidator implements Validator {

    private static List<String> allowedSuffixes;
    static {
        allowedSuffixes = new ArrayList<>();
        allowedSuffixes.add(".doc");
        allowedSuffixes.add(".docx");
        allowedSuffixes.add(".docm");
        allowedSuffixes.add(".xls");
        allowedSuffixes.add(".xlsx");
        allowedSuffixes.add(".xlsm");
        allowedSuffixes.add(".pdf");
        allowedSuffixes.add(".txt");
    }

    @Override
    public void validate(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
        List<FacesMessage> msgs = new ArrayList<>();
        Part file = (Part) value;
        if (file.getSize() > 10485760) {
            System.out.println("File too big. Max 10MB.");
            msgs.add(new FacesMessage("Datei zu groß. Maximal 10MB."));
        }

        String suffix = SamplyShareUtils.getFilesuffixFromContentDisposition(file.getHeader("Content-Disposition"));

        if (!allowedSuffixes.contains(suffix)) {
            System.out.println("Filetype not allowed: " + suffix);
            msgs.add(new FacesMessage("Dateityp nicht erlaubt. Erlaubt sind: " + StringUtils.join(allowedSuffixes.toArray(), ", ")));
        }
        if (!msgs.isEmpty()) {
            throw new ValidatorException(msgs);
        }
    }

}
