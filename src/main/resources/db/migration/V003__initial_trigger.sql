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

