/*
 * This file is generated by jOOQ.
*/
package de.samply.share.broker.model.db.tables.pojos;


import java.io.Serializable;
import java.sql.Date;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Consent implements Serializable {

    private static final long serialVersionUID = 2082203814;

    private Integer id;
    private String  version;
    private String  content;
    private Date    created;

    public Consent() {}

    public Consent(Consent value) {
        this.id = value.id;
        this.version = value.version;
        this.content = value.content;
        this.created = value.created;
    }

    public Consent(
        Integer id,
        String  version,
        String  content,
        Date    created
    ) {
        this.id = id;
        this.version = version;
        this.content = content;
        this.created = created;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Consent (");

        sb.append(id);
        sb.append(", ").append(version);
        sb.append(", ").append(content);
        sb.append(", ").append(created);

        sb.append(")");
        return sb.toString();
    }
}
