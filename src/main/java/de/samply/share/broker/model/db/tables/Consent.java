/*
 * This file is generated by jOOQ.
*/
package de.samply.share.broker.model.db.tables;


import de.samply.share.broker.model.db.Keys;
import de.samply.share.broker.model.db.Samply;
import de.samply.share.broker.model.db.tables.records.ConsentRecord;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


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
public class Consent extends TableImpl<ConsentRecord> {

    private static final long serialVersionUID = -577237774;

    /**
     * The reference instance of <code>samply.consent</code>
     */
    public static final Consent CONSENT = new Consent();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ConsentRecord> getRecordType() {
        return ConsentRecord.class;
    }

    /**
     * The column <code>samply.consent.id</code>.
     */
    public final TableField<ConsentRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('samply.consent_id_seq'::regclass)", org.jooq.impl.SQLDataType.INTEGER)), this, "");

    /**
     * The column <code>samply.consent.version</code>.
     */
    public final TableField<ConsentRecord, String> VERSION = createField("version", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>samply.consent.content</code>.
     */
    public final TableField<ConsentRecord, String> CONTENT = createField("content", org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>samply.consent.created</code>.
     */
    public final TableField<ConsentRecord, Date> CREATED = createField("created", org.jooq.impl.SQLDataType.DATE.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.DATE)), this, "");

    /**
     * Create a <code>samply.consent</code> table reference
     */
    public Consent() {
        this("consent", null);
    }

    /**
     * Create an aliased <code>samply.consent</code> table reference
     */
    public Consent(String alias) {
        this(alias, CONSENT);
    }

    private Consent(String alias, Table<ConsentRecord> aliased) {
        this(alias, aliased, null);
    }

    private Consent(String alias, Table<ConsentRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Samply.SAMPLY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<ConsentRecord, Integer> getIdentity() {
        return Keys.IDENTITY_CONSENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ConsentRecord> getPrimaryKey() {
        return Keys.CONSENT_PKEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ConsentRecord>> getKeys() {
        return Arrays.<UniqueKey<ConsentRecord>>asList(Keys.CONSENT_PKEY, Keys.CONSENT_VERSION_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Consent as(String alias) {
        return new Consent(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Consent rename(String name) {
        return new Consent(name, null);
    }
}
