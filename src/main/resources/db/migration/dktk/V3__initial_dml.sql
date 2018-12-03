CREATE FUNCTION samply.getnextappnr()
  RETURNS INTEGER AS $$
DECLARE
  current_year INTEGER;
  max_appnr    INTEGER;
BEGIN
  current_year := date_part('year', now());
  SELECT COALESCE(max(application_number), 19000000)
  INTO max_appnr
  FROM samply.project;

  IF current_year * 10000 > max_appnr
  THEN
    RETURN (current_year * 10000 + 0001);
  ELSE
    RETURN max_appnr + 1;
  END IF;

END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION samply.appnr_insert()
  RETURNS TRIGGER AS $appnr_insert$
BEGIN
  NEW.application_number := samply.getnextappnr();
  RETURN NEW;
END;
$appnr_insert$ LANGUAGE plpgsql;

CREATE TRIGGER appnr_insert
BEFORE INSERT ON samply.project
FOR EACH ROW EXECUTE PROCEDURE samply.appnr_insert();

INSERT INTO "site" (name) VALUES ('Berlin');
INSERT INTO "site" (name) VALUES ('Dresden');
INSERT INTO "site" (name) VALUES ('Düsseldorf');
INSERT INTO "site" (name) VALUES ('Essen');
INSERT INTO "site" (name) VALUES ('Frankfurt');
INSERT INTO "site" (name) VALUES ('Freiburg');
INSERT INTO "site" (name) VALUES ('Heidelberg');
INSERT INTO "site" (name) VALUES ('Mainz');
INSERT INTO "site" (name) VALUES ('München (LMU)');
INSERT INTO "site" (name) VALUES ('München (TUM)');
INSERT INTO "site" (name) VALUES ('Tübingen');
INSERT INTO "site" (name) VALUES ('Teststandort');

-- OSSE
-- INSERT INTO "consent" (version, content) values ('1.0', 'In order to participate in the decentral search it is necessary that your contact information is made available to anyone registered with the system. As of now, there is no instance that controls who is eligible to register. Please confirm that you read and understood this.')
