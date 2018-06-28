package rest;


import de.samply.share.broker.utils.Utils;
import de.samply.share.common.utils.Constants;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.model.common.*;
import de.samply.share.utils.QueryConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

/**
 * The Class Test provides a REST resources for samply.share.client (and other similar products) to get and put
 * example messages for testing purposes.
 */
@Path("/test")
public class Test {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    final String SERVER_HEADER_VALUE = Constants.SERVER_HEADER_VALUE_PREFIX + ProjectInfo.INSTANCE.getVersionString();

    @Context
    UriInfo uriInfo;

    /**
     * Gets the example inquiry, no matter which id is provided.
     *
     * @return <CODE>200</CODE> and the serialized example inquiry on success
     *         <CODE>500</CODE> on any error
     */
    @Path("/inquiries/{inquiryid}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getInquiry(@HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader) {
        try {
            Inquiry inquiry = new Inquiry();
            inquiry.setAuthor(getDummyContact());
            inquiry.setDescription(getDummyInfo().getDescription());

            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
            URI uri = uriBuilder.build();
            inquiry.setExposeURL(uri + "test/exposes/0");

            inquiry.setId("0");
            inquiry.setLabel(getDummyInfo().getLabel());
            inquiry.setQuery(getExampleQuery());
            inquiry.setRevision("1");
            inquiry.getSearchFor().add("patienten");

            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            ObjectFactory objectFactory = new ObjectFactory();

            marshaller.marshal(objectFactory.createInquiry(inquiry), stringWriter);

            String ret = stringWriter.toString();
            ret = Utils.fixNamespaces(ret, xmlNamespaceHeader);
            return Response.status(Response.Status.OK).entity(ret).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } catch (JAXBException e) {
            logger.error("Caught JAXB Exception while trying to provide example inquiry", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets the query part of the inquiry with the given id.
     *
     * @return <CODE>200</CODE> and the serialized query part of the example inquiry on success
     *         <CODE>500</CODE> on any error
     */
    @Path("/inquiries/{inquiryid}/query")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getQuery(@HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader) {
        try {
            Query query = getExampleQuery();
            String ret = QueryConverter.queryToXml(query);
            ret = Utils.fixNamespaces(ret, xmlNamespaceHeader);
            return Response.status(Response.Status.OK).entity(ret).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } catch (JAXBException e) {
            logger.error("Caught JAXB Exception while trying to provide example query", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets a dummy contact for the example inquiry
     *
     * @return <CODE>200</CODE> and the serialized dummy contact on success
     *         <CODE>500</CODE> on any error
     */
    @Path("/inquiries/{inquiryid}/contact")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getContact(@HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader) {
        Contact contact = getDummyContact();
        try {
            String ret;
            JAXBContext jaxbContext = JAXBContext.newInstance(Contact.class);
            ObjectFactory objectFactory = new ObjectFactory();
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(objectFactory.createContact(contact), stringWriter);

            ret = stringWriter.toString();
            ret = Utils.fixNamespaces(ret, xmlNamespaceHeader);

            return Response.status(Response.Status.OK).entity(ret).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } catch (JAXBException e) {
            logger.error("Caught JAXB Exception while trying to provide example contact", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets a dummy description for the example inquiry
     *
     * @return <CODE>200</CODE> and the serialized dummy info on success
     *         <CODE>500</CODE> on any error
     */
    @Path("/inquiries/{inquiryid}/info")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getInfo(@HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader) {
        Info info = getDummyInfo();

        try {
            String ret;
            JAXBContext jaxbContext = JAXBContext.newInstance(Info.class);
            ObjectFactory objectFactory = new ObjectFactory();
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(objectFactory.createInfo(info), stringWriter);

            ret = stringWriter.toString();
            ret = Utils.fixNamespaces(ret, xmlNamespaceHeader);

            return Response.status(Response.Status.OK).entity(ret).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } catch (JAXBException e) {
            logger.error("Caught JAXB Exception while trying to provide example info", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Always return that no expose is available here
     *
     * @param inquiryId an irrelevant id that is accepted but ignored in order to fit the requests
     *
     * @return <CODE>404</CODE> in any case
     */
    @Path("/inquiries/{inquiryid}/hasexpose")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response isSynopsisAvailable(@PathParam("inquiryid") String inquiryId) throws IOException {
        return Response.status(Response.Status.NOT_FOUND).entity("unavailable").header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    /**
     * Accept any kind of reply here
     *
     * @param reply the reply
     * @return <CODE>200</CODE> in any case
     */
    @Path("/inquiries/{inquiryid}/replies/{bankemail}")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response putReply(String reply) {
        return Response.status(Response.Status.OK).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    /**
     * Construct an example query with a single criteria (gender = male)
     *
     * @return string representation of the example query
     */
    private static Query getExampleQuery() {
        ObjectFactory objectFactory = new ObjectFactory();

        Query query = new Query();
        Where where = new Where();
        And and = new And();
        Eq eq = new Eq();
        Attribute attribute = new Attribute();

        attribute.setMdrKey("urn:dktk:dataelement:1:*");
        attribute.setValue(objectFactory.createValue("M"));

        eq.setAttribute(attribute);
        and.getAndOrEqOrLike().add(eq);
        where.getAndOrEqOrLike().add(and);
        query.setWhere(where);

        return query;
    }

    /**
     * Create a dummy contact
     *
     * @return the created dummy contact
     */
    private static Contact getDummyContact() {
        Contact contact = new Contact();
        contact.setEmail("no-reply@ccp-it.dktk.dkfz.de");
        contact.setFirstname("n/a");
        contact.setLastname("n/a");
        contact.setOrganization("DKTK CCP-IT");
        contact.setPhone("-");
        contact.setTitle("-");
        return contact;
    }

    /**
     * Create a dummy info
     *
     * @return the created dummy info
     */
    private static Info getDummyInfo() {
        Info info = new Info();
        info.setDescription("This is just a testquery.");
        info.setLabel("Testquery");
        info.setRevision("1");
        return info;
    }
}
