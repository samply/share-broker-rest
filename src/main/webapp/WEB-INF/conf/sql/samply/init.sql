INSERT INTO samply.site (name) VALUES ('Aachen');
INSERT INTO samply.site (name) VALUES ('Frankfurt');
INSERT INTO samply.site (name) VALUES ('Göttingen');
INSERT INTO samply.site (name) VALUES ('Greifswald');
INSERT INTO samply.site (name) VALUES ('Hannover');
INSERT INTO samply.site (name) VALUES ('Heidelberg');
INSERT INTO samply.site (name) VALUES ('Jena');
INSERT INTO samply.site (name) VALUES ('Leipzig');
INSERT INTO samply.site (name) VALUES ('Lübeck');
INSERT INTO samply.site (name) VALUES ('München-HMGU');
INSERT INTO samply.site (name) VALUES ('München-LMU');
INSERT INTO samply.site (name) VALUES ('München-TUM');
INSERT INTO samply.site (name) VALUES ('Würzburg');
INSERT INTO samply.site (name) VALUES ('Teststandort');

INSERT INTO samply.contact(firstname, lastname, email) VALUES ('GBA', 'Searchbroker', 'no-reply@vm.vmitro.de');
INSERT INTO samply.user(username, email, name, contact_id) VALUES ('Searchbroker', 'no-reply@vm.vmitro.de', 'GBA Searchbroker',
                                                                   (SELECT MAX(id) FROM samply.contact t WHERE t.firstname = 'GBA' AND t.lastname = 'Searchbroker'));