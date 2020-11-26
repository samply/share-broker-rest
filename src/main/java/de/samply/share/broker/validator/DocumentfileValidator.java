package de.samply.share.broker.validator;

import de.samply.share.common.utils.SamplyShareUtils;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;
import org.jooq.tools.StringUtils;

/**
 * Check if a document is not bigger than the defined threshold of 10MB and has one of the allowed
 * suffixes.
 * This implements a FacesValidator.
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

    String suffix = SamplyShareUtils
        .getFilesuffixFromContentDisposition(file.getHeader("Content-Disposition"));

    if (!allowedSuffixes.contains(suffix)) {
      System.out.println("Filetype not allowed: " + suffix);
      msgs.add(new FacesMessage("Dateityp nicht erlaubt. Erlaubt sind: " + StringUtils
          .join(allowedSuffixes.toArray(), ", ")));
    }
    if (!msgs.isEmpty()) {
      throw new ValidatorException(msgs);
    }
  }

}
