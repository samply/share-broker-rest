/*
 * This file is generated by jOOQ.
*/
package de.samply.share.broker.model.db.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;

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
public class Authtoken implements Serializable {

    private static final long serialVersionUID = -209171335;

    private Integer   id;
    private String    value;
    private Timestamp lastused;

    public Authtoken() {}

    public Authtoken(Authtoken value) {
        this.id = value.id;
        this.value = value.value;
        this.lastused = value.lastused;
    }

    public Authtoken(
        Integer   id,
        String    value,
        Timestamp lastused
    ) {
        this.id = id;
        this.value = value;
        this.lastused = lastused;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Timestamp getLastused() {
        return this.lastused;
    }

    public void setLastused(Timestamp lastused) {
        this.lastused = lastused;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Authtoken (");

        sb.append(id);
        sb.append(", ").append(value);
        sb.append(", ").append(lastused);

        sb.append(")");
        return sb.toString();
    }
}
