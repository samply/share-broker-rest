package de.samply.share.broker.utils.db;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.samply.share.broker.model.db.tables.pojos.Reply;

public class DonorCountExtractor {

  int extractDonorCount(Reply reply) {
    try {
      JsonResult result = new Gson().fromJson(reply.getContent(), JsonResult.class);
      JsonResultEntity donor = result.getDonor();
      return donor == null ? 0 : donor.getCount();
    } catch (JsonSyntaxException exception) {
      return extractDonorCountLegacyFormat(reply);
    }
  }

  private int extractDonorCountLegacyFormat(Reply reply) {
    try {
      JsonResultLegacy result = new Gson().fromJson(reply.getContent(), JsonResultLegacy.class);
      return result.getDonor();
    } catch (JsonSyntaxException exception) {
      return 0;
    }
  }

  private static class JsonResult {

    @SuppressWarnings("unused")
    private JsonResultEntity donor;

    JsonResultEntity getDonor() {
      return donor;
    }
  }

  private static class JsonResultEntity {

    @SuppressWarnings("unused")
    private int count;

    int getCount() {
      return count;
    }
  }

  private static class JsonResultLegacy {

    @SuppressWarnings("unused")
    private int donor;

    int getDonor() {
      return donor;
    }
  }
}
