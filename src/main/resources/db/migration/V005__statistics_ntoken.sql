SET search_path TO samply;

CREATE TABLE "ntoken_query" (
  id            SERIAL PRIMARY KEY,
  ntoken        TEXT,
  inquiryId     INTEGER,
  query         TEXT,
  wascreated    timestamp
);

ALTER TABLE ONLY statistics_query
    ADD CONSTRAINT statistics_query_unique_id UNIQUE (id);

ALTER TABLE ONLY statistics_field
    ADD CONSTRAINT statistics_field_unique_id UNIQUE (id);

ALTER TABLE ONLY statistics_value
    ADD CONSTRAINT statistics_value_unique_id UNIQUE (id);

ALTER TABLE statistics_field
  ADD COLUMN queryId integer;

ALTER TABLE statistics_value
  ADD COLUMN fieldId integer;

ALTER TABLE statistics_field
  ADD CONSTRAINT field_to_query_fk FOREIGN KEY (queryId) REFERENCES statistics_query(id) ON DELETE CASCADE;

ALTER TABLE statistics_value
  ADD CONSTRAINT value_to_field_fk FOREIGN KEY (fieldId) REFERENCES statistics_field(id) ON DELETE CASCADE;
