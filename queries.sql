INSERT INTO "service" ("name") VALUES ('bicycle');
INSERT INTO "service" ("name") VALUES ('pedals');
INSERT INTO "service" ("name") VALUES ('rear brake pads');
INSERT INTO "service" ("name") VALUES ('front brake pads');
INSERT INTO "service" ("name") VALUES ('rear tire');
INSERT INTO "service" ("name") VALUES ('front tire');
--unique name tested

DELETE FROM trip;
DELETE FROM bike;
DELETE FROM maintenance;

INSERT INTO "bike" ("name","brand","model","type","odo","ride_time","starting_odo","starting_time") 
VALUES ('STAR','TREK','1500','ROAD',0.0,0.0,0.0,0.0);
--Assuming new id_bike = 1
INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description","duration") 
VALUES  ('2019-10-01',1,(SELECT starting_odo FROM bike WHERE id = 1),1,'TREK','1500',1000,'Bicicleta de segunda mano Grupo Sora 9V','{"km": 0, "hours": 0}');

SELECT * FROM bike;
SELECT * FROM maintenance;
.import --csv --skip 1 trip-raw-min-data.csv trip

--delete from maintenance where id_service = 2; 
INSERT INTO "maintenance" ("date","id_bike","id_service","brand","reference","price","description","duration") 
VALUES  ('2020-02-07',1,2,'SHIMANO','PD-M520',0,'Pedales de MTB','{"km":0.0,"hours":0.0}');
SELECT * FROM maintenance ORDER BY date;










--in reality, trips of first bike are input before the second bike
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") VALUES
('2019-10-01',1,0.70333333,21.36,41);
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") VALUES
('2019-10-02',1,0.8194444445,26.12,43.3);
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") VALUES 
('2019-10-03',1,0.323333333,5.58,38);
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") VALUES 
('2019-10-05',1,0.5577777777,17.18,42.4);

INSERT INTO "bike" ("name","brand","model","type","odo","ride_time","starting_odo","starting_time") 
VALUES ('STEF','GARY FISHER','ROSCOE ONE','MTB',0.0,0.0,0.0,0.0);
--Assuming new id_bike = 2
INSERT INTO "maintenance" ("date","id_bike","id_service","brand","reference","price","description","duration") 
VALUES  ('2019-10-01',2,1,'GARY FISHER','ROSCOE ONE',-5109.381,'Bicicleta nueva R26 de 2011, se paga a cuotas','{"km": 0, "hours": 0}');

--DELETE FROM trip WHERE date = '2019-10-03';
SELECT * FROM trip ORDER BY date;
SELECT * FROM bike;

-- DELETE FROM trip;
-- UPDATE bike SET odo = 0, ride_time = 0, starting_odo = 1000 WHERE id = 1;
-- SELECT * FROM trip;
-- SELECT * FROM bike;

--MEJOR CREAR duration CON EL FORMATO JSON
delete from maintenance where id_service = 2;   
SELECT * FROM maintenance ORDER BY date;
INSERT INTO "maintenance" ("date","id_bike","id_service","brand","reference","price","description","duration") 
VALUES  ('2020-02-07',1,2,'SHIMANO','PD-M520',0,'Pedales de MTB','{"km":0.0,"hours":0.0}');
SELECT * FROM maintenance ORDER BY date;

INSERT INTO "maintenance" ("date","id_bike","id_service","brand","reference","price","description","duration") 
VALUES  ('2020-05-15',1,3,'GW','R450',5,'prueba','{"km":0.0,"hours":0.0}');
INSERT INTO "maintenance" ("date","id_bike","id_service","brand","reference","price","description","duration") 
VALUES  ('2020-05-15',1,4,'BARADINE','UNKNOWN',6,'prueba','{"km":0.0,"hours":0.0}');
SELECT * FROM maintenance ORDER BY date;

--testing trigger
delete from maintenance where date  = '2023-03-05';
SELECT * FROM maintenance ORDER BY date;
INSERT INTO "maintenance" ("date","id_bike","id_service","brand","reference","price","description","duration") 
VALUES  ('2023-03-05',1,3,'GW','R450',5,'ACABADOS POR MI','{"km":0.0,	"hours":0.0}');
SELECT * FROM maintenance ORDER BY date;

INSERT INTO "maintenance" ("date","id_bike","id_service","brand","reference","price","description","duration") 
VALUES  ('2023-03-05',1,4,'GW','R450',5,'ACABADOS POR MI','{"km":0.0,	"hours":0.0}');
SELECT * FROM maintenance ORDER BY date;
