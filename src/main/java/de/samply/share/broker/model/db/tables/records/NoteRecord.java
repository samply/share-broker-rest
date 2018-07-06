/*
 * This file is generated by jOOQ.
*/
package de.samply.share.broker.model.db.tables.records;


import de.samply.share.broker.model.db.tables.Note;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


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
public class NoteRecord extends UpdatableRecordImpl<NoteRecord> implements Record5<Integer, String, Timestamp, Integer, Integer> {

    private static final long serialVersionUID = -1025393641;

    /**
     * Setter for <code>samply.note.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>samply.note.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>samply.note.content</code>.
     */
    public void setContent(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>samply.note.content</code>.
     */
    public String getContent() {
        return (String) get(1);
    }

    /**
     * Setter for <code>samply.note.created</code>.
     */
    public void setCreated(Timestamp value) {
        set(2, value);
    }

    /**
     * Getter for <code>samply.note.created</code>.
     */
    public Timestamp getCreated() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>samply.note.author_id</code>.
     */
    public void setAuthorId(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>samply.note.author_id</code>.
     */
    public Integer getAuthorId() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>samply.note.project_id</code>.
     */
    public void setProjectId(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>samply.note.project_id</code>.
     */
    public Integer getProjectId() {
        return (Integer) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Integer, String, Timestamp, Integer, Integer> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<Integer, String, Timestamp, Integer, Integer> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Note.NOTE.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Note.NOTE.CONTENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return Note.NOTE.CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field4() {
        return Note.NOTE.AUTHOR_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field5() {
        return Note.NOTE.PROJECT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getContent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value3() {
        return getCreated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value4() {
        return getAuthorId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value5() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteRecord value2(String value) {
        setContent(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteRecord value3(Timestamp value) {
        setCreated(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteRecord value4(Integer value) {
        setAuthorId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteRecord value5(Integer value) {
        setProjectId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NoteRecord values(Integer value1, String value2, Timestamp value3, Integer value4, Integer value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached NoteRecord
     */
    public NoteRecord() {
        super(Note.NOTE);
    }

    /**
     * Create a detached, initialised NoteRecord
     */
    public NoteRecord(Integer id, String content, Timestamp created, Integer authorId, Integer projectId) {
        super(Note.NOTE);

        set(0, id);
        set(1, content);
        set(2, created);
        set(3, authorId);
        set(4, projectId);
    }
}
