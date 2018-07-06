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
public class Note implements Serializable {

    private static final long serialVersionUID = 1890023126;

    private Integer   id;
    private String    content;
    private Timestamp created;
    private Integer   authorId;
    private Integer   projectId;

    public Note() {}

    public Note(Note value) {
        this.id = value.id;
        this.content = value.content;
        this.created = value.created;
        this.authorId = value.authorId;
        this.projectId = value.projectId;
    }

    public Note(
        Integer   id,
        String    content,
        Timestamp created,
        Integer   authorId,
        Integer   projectId
    ) {
        this.id = id;
        this.content = content;
        this.created = created;
        this.authorId = authorId;
        this.projectId = projectId;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreated() {
        return this.created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Integer getAuthorId() {
        return this.authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Note (");

        sb.append(id);
        sb.append(", ").append(content);
        sb.append(", ").append(created);
        sb.append(", ").append(authorId);
        sb.append(", ").append(projectId);

        sb.append(")");
        return sb.toString();
    }
}
