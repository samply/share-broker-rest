package de.samply.share.broker.validator;

import com.itextpdf.text.pdf.PdfReader;
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

/**
 * Check if a document is not bigger than the defined threshold of 10MB and has is a pdf file.
 * This implements a FacesValidator.
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
      System.out.println(
          "Content-Type not allowed!: " + file.getContentType() + ". Only PDF is allowed.");
      msgs.add(new FacesMessage("Dateityp nicht erlaubt. Nur PDF Dateien sind erlaubt."));
    }
    if (!msgs.isEmpty()) {
      throw new ValidatorException(msgs);
    }
  }

  /**
   * Check if the file is a pdf file.
   * Try to open it with PdfReader. If it fails, it is not a valid pdf file.
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
