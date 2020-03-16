ALTER TABLE "statistics_field"
ADD COLUMN statistic_query_id integer,
ADD  CONSTRAINT statistic_query_id FOREIGN KEY (statistic_query_id) REFERENCES "statistics_query"(id);

ALTER TABLE "statistics_value"
ADD COLUMN statistics_field_id integer,
ADD  CONSTRAINT statistics_field_id  FOREIGN KEY (statistics_field_id) REFERENCES "statistics_field"(id);

