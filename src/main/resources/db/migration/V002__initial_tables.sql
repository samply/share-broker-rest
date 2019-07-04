CREATE TABLE "user" (
  id         SERIAL PRIMARY KEY,
  username   TEXT NOT NULL,
  email      TEXT,
  name       TEXT,
  authid     TEXT UNIQUE,
  contact_id INTEGER
);

CREATE TABLE bank (
  id           SERIAL PRIMARY KEY,
  email        TEXT UNIQUE NOT NULL,
  authtoken_id INTEGER     NOT NULL,
  clientinfo   TEXT
);

CREATE TABLE authtoken (
  id       SERIAL PRIMARY KEY,
  value    TEXT UNIQUE NOT NULL,
  lastUsed TIMESTAMP
);

CREATE TABLE inquiry (
  id          SERIAL PRIMARY KEY,
  project_id  INTEGER,
  label       TEXT,
  description TEXT,
  created     TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  expires     DATE                        DEFAULT (now() + '28 days' :: INTERVAL),
  author_id   INTEGER        NOT NULL,
  archived    BOOLEAN,
  viewfields  TEXT,
  status      INQUIRY_STATUS NOT NULL,
  revision    INTEGER,
  result_type TEXT
);

CREATE TABLE inquiry_details (
  id          SERIAL PRIMARY KEY,
  inquiry_id  INTEGER,
  criteria    TEXT           NOT NULL,
  type        INQUIRY_DETAILS_TYPE NOT NULL,
  entity_type TEXT
);

CREATE TABLE project (
  id                  SERIAL PRIMARY KEY,
  name                TEXT           NOT NULL,
  received            TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
  approved            TIMESTAMP WITHOUT TIME ZONE,
  started             DATE,
  end_estimated       DATE,
  end_actual          DATE,
  seen                BOOLEAN                     DEFAULT FALSE,
  projectleader_id    INTEGER        NOT NULL,
  archived            BOOLEAN                     DEFAULT FALSE,
  status              PROJECT_STATUS NOT NULL,
  application_number  INTEGER,
  external_assessment BOOLEAN                     DEFAULT FALSE
);

CREATE TABLE document (
  id            SERIAL PRIMARY KEY,
  project_id    INTEGER,
  inquiry_id    INTEGER,
  user_id       INTEGER NOT NULL,
  uploaded_at   DATE    NOT NULL DEFAULT CURRENT_DATE,
  filetype      TEXT,
  filename      TEXT,
  data          BYTEA,
  document_type DOCUMENT_TYPE
);

CREATE TABLE reply (
  id         SERIAL PRIMARY KEY,
  content    TEXT    NOT NULL,
  bank_id    INTEGER NOT NULL,
  inquiry_id INTEGER NOT NULL
);

CREATE TABLE tokenrequest (
  id       SERIAL PRIMARY KEY,
  issued   TIMESTAMP NOT NULL DEFAULT now(),
  email    TEXT      NOT NULL,
  authcode TEXT
);

CREATE TABLE site (
  id            SERIAL PRIMARY KEY,
  name          TEXT UNIQUE NOT NULL,
  name_extended TEXT,
  description   TEXT,
  contact       TEXT,
  biobankId     TEXT,
  collectionId  TEXT,
  active        BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE user_site (
  user_id  INTEGER NOT NULL REFERENCES "user" (id),
  site_id  INTEGER NOT NULL REFERENCES site (id),
  approved BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (user_id, site_id)
);

CREATE TABLE bank_site (
  bank_id  INTEGER NOT NULL REFERENCES bank (id),
  site_id  INTEGER NOT NULL REFERENCES site (id),
  approved BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (bank_id, site_id)
);

CREATE TABLE contact (
  id               SERIAL PRIMARY KEY,
  portrait         BYTEA,
  title            TEXT,
  firstname        TEXT,
  lastname         TEXT NOT NULL,
  phone            TEXT,
  email            TEXT,
  organizationname TEXT
);

CREATE TABLE inquiry_site (
  inquiry_id   INTEGER NOT NULL REFERENCES inquiry (id),
  site_id      INTEGER NOT NULL REFERENCES site (id),
  retrieved_at TIMESTAMP WITHOUT TIME ZONE,
  PRIMARY KEY (inquiry_id, site_id)
);

CREATE TABLE project_site (
  project_id INTEGER NOT NULL REFERENCES project (id),
  site_id    INTEGER NOT NULL REFERENCES site (id),
  PRIMARY KEY (project_id, site_id)
);

CREATE TABLE action (
  id         SERIAL PRIMARY KEY,
  type       ACTION_TYPE NOT NULL,
  date       DATE        NOT NULL DEFAULT now(),
  time       TIME WITHOUT TIME ZONE,
  project_id INTEGER REFERENCES project (id),
  user_id    INTEGER REFERENCES "user" (id),
  message    TEXT        NOT NULL,
  icon       TEXT
);

-- preemptive assignment of email address to site
CREATE TABLE email_site (
  id    SERIAL PRIMARY KEY,
  email TEXT UNIQUE NOT NULL,
  site  INTEGER     NOT NULL REFERENCES site (id)
);

ALTER TABLE bank
  ADD FOREIGN KEY (authtoken_id) REFERENCES authtoken (id) ON DELETE CASCADE;
ALTER TABLE inquiry
  ADD FOREIGN KEY (author_id) REFERENCES "user" (id) ON DELETE SET NULL;
ALTER TABLE inquiry
  ADD FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE SET NULL;
ALTER TABLE document
  ADD FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE SET NULL;
ALTER TABLE document
  ADD FOREIGN KEY (inquiry_id) REFERENCES inquiry (id) ON DELETE SET NULL;
ALTER TABLE document
  ADD FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE SET NULL;
ALTER TABLE reply
  ADD FOREIGN KEY (bank_id) REFERENCES bank (id) ON DELETE CASCADE;
ALTER TABLE reply
  ADD FOREIGN KEY (inquiry_id) REFERENCES inquiry (id) ON DELETE CASCADE;
ALTER TABLE "user"
  ADD FOREIGN KEY (contact_id) REFERENCES contact (id) ON DELETE SET NULL;
ALTER TABLE project
  ADD FOREIGN KEY (projectleader_id) REFERENCES "user" (id) ON DELETE SET NULL;
