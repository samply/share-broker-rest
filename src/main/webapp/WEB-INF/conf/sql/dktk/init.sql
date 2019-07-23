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

INSERT INTO samply.contact(firstname, lastname, email) VALUES ('DKTK', 'Searchbroker', 'no-reply@vm.vmitro.de');
INSERT INTO samply.user(username, email, name, contact_id) VALUES ('Searchbroker', 'no-reply@vm.vmitro.de', 'DKTK Searchbroker',
                                                                   (SELECT MAX(id) FROM samply.contact t WHERE t.firstname = 'GBA' AND t.lastname = 'Searchbroker'));