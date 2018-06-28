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
package utils.oauth2;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for oAuth2Client complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="oAuth2Client"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="hostPublicKey" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/&gt;
 *         &lt;element name="clientId" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="clientSecret" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *         &lt;element name="additionalHostnames" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="hostname" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *                             &lt;element name="ifServernameEquals" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "oAuth2Client", namespace = "http://schema.samply.de/config/OAuth2Client", propOrder = {
    "host",
    "hostPublicKey",
    "clientId",
    "clientSecret",
    "additionalHostnames"
})
public class OAuth2Client {

    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String host;
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String hostPublicKey;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String clientId;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String clientSecret;
    protected de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames additionalHostnames;

    /**
     * Gets the value of the host property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the hostPublicKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getHostPublicKey() {
        return hostPublicKey;
    }

    /**
     * Sets the value of the hostPublicKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setHostPublicKey(String value) {
        this.hostPublicKey = value;
    }

    /**
     * Gets the value of the clientId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the value of the clientId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClientId(String value) {
        this.clientId = value;
    }

    /**
     * Gets the value of the clientSecret property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Sets the value of the clientSecret property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setClientSecret(String value) {
        this.clientSecret = value;
    }

    /**
     * Gets the value of the additionalHostnames property.
     *
     * @return
     *     possible object is
     *     {@link de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames }
     *
     */
    public de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames getAdditionalHostnames() {
        return additionalHostnames;
    }

    /**
     * Sets the value of the additionalHostnames property.
     *
     * @param value
     *     allowed object is
     *     {@link de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames }
     *
     */
    public void setAdditionalHostnames(de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames value) {
        this.additionalHostnames = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="hostname" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
     *                   &lt;element name="ifServernameEquals" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "hostname"
    })
    public static class AdditionalHostnames {

        @XmlElement(namespace = "http://schema.samply.de/config/OAuth2Client")
        protected List<de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames.Hostname> hostname;

        /**
         * Gets the value of the hostname property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the hostname property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getHostname().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames.Hostname }
         *
         *
         */
        public List<de.samply.share.broker.utils.oauth2.OAuth2Client.AdditionalHostnames.Hostname> getHostname() {
            if (hostname == null) {
                hostname = new ArrayList<>();
            }
            return this.hostname;
        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
         *         &lt;element name="ifServernameEquals" type="{http://www.w3.org/2001/XMLSchema}token"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "host",
            "ifServernameEquals"
        })
        public static class Hostname {

            @XmlElement(namespace = "http://schema.samply.de/config/OAuth2Client", required = true)
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "token")
            protected String host;
            @XmlElement(namespace = "http://schema.samply.de/config/OAuth2Client", required = true)
            @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
            @XmlSchemaType(name = "token")
            protected String ifServernameEquals;

            /**
             * Gets the value of the host property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getHost() {
                return host;
            }

            /**
             * Sets the value of the host property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setHost(String value) {
                this.host = value;
            }

            /**
             * Gets the value of the ifServernameEquals property.
             *
             * @return
             *     possible object is
             *     {@link String }
             *
             */
            public String getIfServernameEquals() {
                return ifServernameEquals;
            }

            /**
             * Sets the value of the ifServernameEquals property.
             *
             * @param value
             *     allowed object is
             *     {@link String }
             *
             */
            public void setIfServernameEquals(String value) {
                this.ifServernameEquals = value;
            }

        }

    }

}
