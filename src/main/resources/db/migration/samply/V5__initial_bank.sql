INSERT INTO samply.contact(id, firstname, lastname, email)
VALUES ('600', 'GBA', 'Searchbroker', 'no-reply@vm.vmitro.de');
INSERT INTO samply.authtoken(id, value)
VALUES ('600', 'bomH9fXlQLgfQG6NxALxPJCzwlVgyTzoT10Zf5RNczNO4JFqZQGJifATt1omOp2Q');
INSERT INTO samply.user(id, username, email, name, authid, contact_id)
VALUES ('1', 'Searchbroker', 'no-reply@vm.vmitro.de', 'GBA Searchbroker', '600', '600');

ALTER TABLE samply.site DROP CONSTRAINT site_name_key;

UPDATE samply.site SET name='Aachen' WHERE id=1;
UPDATE samply.site SET name='Frankfurt' WHERE id=2;
UPDATE samply.site SET name='Göttingen' WHERE id=3;
UPDATE samply.site SET name='Greifswald' WHERE id=4;
UPDATE samply.site SET name='Hannover' WHERE id=5;
UPDATE samply.site SET name='Heidelberg' WHERE id=6;
UPDATE samply.site SET name='Jena' WHERE id=7;
UPDATE samply.site SET name='Leipzig' WHERE id=8;
UPDATE samply.site SET name='Lübeck' WHERE id=9;
UPDATE samply.site SET name='München (HMGU)' WHERE id=10;
UPDATE samply.site SET name='München (LMU)' WHERE id=11;
UPDATE samply.site SET name='München(TUM)' WHERE id=12;
INSERT INTO samply.site (name) VALUES ('Würzburg');
INSERT INTO samply.site (name) VALUES ('Teststandort');

ALTER TABLE samply.site
 ADD CONSTRAINT site_name_key UNIQUE (name);