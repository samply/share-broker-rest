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

INSERT INTO samply.contact(id, firstname, lastname, email) VALUES ('1', 'DKTK', 'Searchbroker', 'no-reply@vm.vmitro.de');
INSERT INTO samply.user(id, username, email, name, authid, contact_id) VALUES ('1', 'Searchbroker', 'no-reply@vm.vmitro.de', 'DKTK Searchbroker', '1', '1');
