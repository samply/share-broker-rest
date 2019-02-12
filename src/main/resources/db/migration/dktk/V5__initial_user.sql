DELETE FROM samply.contact WHERE id='1';
DELETE FROM samply.authtoken WHERE id='1';
DELETE FROM samply.user WHERE id='1';

INSERT INTO samply.contact(id, firstname, lastname, email) VALUES ('1', 'DKTK', 'Searchbroker', 'no-reply@vm.vmitro.de');
INSERT INTO samply.authtoken(id, value) VALUES ('1', 'bomH9fXlQLgfQG6NxALxPJCzwlVgyTzoT10Zf5RNczNO4JFqZQGJifATt1omOp2Q');
INSERT INTO samply.user(id, username, email, name, authid, contact_id) VALUES ('1', 'Searchbroker', 'no-reply@vm.vmitro.de', 'DKTK Searchbroker', '1', '1');
