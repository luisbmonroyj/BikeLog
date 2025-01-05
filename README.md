# BikeLog
Java backend and terminal frontend for bicycle's trip and maintenance log, using sqlite3

## SERVICES
### Consulting the list of services

SELECT * FROM "service";

### Creating services

INSERT INTO "service" ("name") VALUES ('bicycle');
INSERT INTO "service" ("name") VALUES ('pedals');
INSERT INTO "service" ("name") VALUES ('rear brake pad');
INSERT INTO "service" ("name") VALUES ('front brake pad');
INSERT INTO "service" ("name") VALUES ('rear tire');
INSERT INTO "service" ("name") VALUES ('front tire');

### updating a service registry
UPDATE "service" SET "name" = 'RR brake pads' WHERE "name" = 'rear brake pad';
UPDATE "service" SET "name" = 'FF brake pads' WHERE "name" = 'front brake pad';

### deleting from service
DELETE FROM "service" WHERE "name" = 'algo';

## BIKES

### Consulting the list of bicycles
SELECT * FROM "bikes";
SELECT * FROM "bikes" WHERE "byke_type" IS "MTB";
SELECT * FROM "bikes" WHERE "brand" IS "TREK";

### Creating bikes
INSERT INTO "bike" ("name","brand","model","type","odo","ride_time","starting_odo","starting_time") 
VALUES ('STAR','TREK','1500','ROAD',0.0,0.0,0.0,0.0);
INSERT INTO "bike" ("name","brand","model","type","odo","ride_time","starting_odo","starting_time") 
VALUES ('STEF','GARY FISHER','ROSCOE ONE','MTB',0.0,0.0,0.0,0.0);

--TRIGGER idea
UPDATE "bikes" SET "odo" = (SELECT SUM("distance") FROM "trip" WHERE "id_bike" = 1),
    "ride_time" = (SELECT SUM("trip_time") FROM "trip" WHERE "id_bike" = 1) 
    WHERE "id" = 1;

### deleting bikes
DELETE FROM "bikes" WHERE "id" = 4;

## TRIPS
### Consulting 
* trips made between two dates in one bicycle
SELECT * FROM "trip" WHERE "date" > "2024-02-01" AND "date" < "2024-03-01" and "id_bike" = 1;

* trips made in one bicycle that were ridden at least at 17 km/h
SELECT * FROM "trip" WHERE velocity >= 17 AND "id_bike" = 1;

### Creating trips
* not recommended time FORMAT as trip_time
--INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed","velocity",odometer) VALUES
--('2019-10-01',1,'0:42:12',21.36,41,30.37,0.0);

* USING DECIMAL TIME
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed","velocity",odometer) VALUES
('2019-10-01',1,0.70333333,21.36,41,30.37,0.0);
--A new trip modifies bikes' data
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed","velocity") VALUES
('2019-10-02',1,0.8194444445,26.12,43.3,31.88);
--trigger calculate_velocity
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") 
VALUES ('2019-10-03',1,0.323333333,5.58,38);
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") 
VALUES ('2019-10-05',1,0.5577777777,17.18,42.4);
--both triggers proven

### Deleting a trip
DELETE FROM "trip" WHERE "id_bike" = 1 AND "date" = '2024-12-01';

## MAINTENANCE
### READ
* Consulting the list of maintenance service made to a bicycle
SELECT * FROM "maintenance" WHERE "id_bike" = 1;
SELECT * FROM "maintenance" WHERE "id_bike" = (SELECT "id" FROM "bikes" WHERE "name" IS "Star");

* Consulting the list of a specific service made to a bicycle
SELECT * FROM "maintenance" WHERE "id_bike" = 1 AND "id_service" =
    (SELECT "id" FROM "service" WHERE "name" = "rueda trasera");

* Reading json from duration
SELECT JSON_EXTRACT(duration, '$.km') as odo FROM maintenance WHERE id_bike = 1 AND date = '2020-02-07';
SELECT JSON_EXTRACT(duration, '$.hours') as hours FROM maintenance WHERE id_bike = 1  AND date = '2020-02-07';

SELECT * FROM maintenance WHERE data LIKE '%"km":1500%'; --or something like that...

### Creating maintenance
INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description","duration") 
VALUES  ('2019-10-01',1,0,1,'TREK','1500',1000,'Bicicleta de segunda mano Grupo Sora 9','{"km": 0, "hours": 0}');
INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description","duration") 
VALUES  ('2020-02-07',1,720,2,'SHIMANO','PD-M520',0,'Pedales de MTB','{"km":0.0,"hours":0.0}');
INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description","duration") 
VALUES  ('2020-05-15',1,1203,3,'GW','R450',5,'prueba','{"km":0.0,"hours":0.0}');
INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description","duration") 
VALUES  ('2020-05-15',1,1203,4,'BARADINE','UNKNOWN',6,'prueba' 2,'{"km":0.0,"hours":0.0}');

--testing trigger
INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description","duration") 
VALUES  ('2023-03-05',1,6586,3,'GW','R450',5,'ACABADOS POR MI','{"km":0.0,	"hours":0.0}');

INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description","duration") 
VALUES  ('2023-03-05',1,6586,4,'GW','R450',5,'ACABADOS POR MI','{"km":0.0,	"hours":0.0}');

* testing unexistent id_service
INSERT INTO "maintenance" ("date","id_bike","odo","id_service","brand","reference","price","description",
    "duration") VALUES ("2020-09-30",1,2644,(SELECT "id" FROM "service" WHERE "name" = 'rear tire'),"Continental","Ultra sport3 700x25",73,'example',"{'km': 0, 'hours': 0}");

### updating a maintenance
* standard columns
UPDATE "maintenance" SET "reference" = "Ultra sport3 700x25" WHERE "reference" = "Utra sport3 700x25";

* json column "duration"
UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.km', 720) WHERE id_service = 2 AND  id_bike = 1 AND date < '2020-02-07';
UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.hours', 100) WHERE id_service = 2 AND  id_bike = 1 AND date < '2020-02-07';

### Deleting a maintenance
DELETE FROM "maintenance" WHERE "id_bike" = 3;

--strftime(format, time-value)
--2020-02-07,STAR,720,2,Shimano,"unknown",0,NONE,"{'km': 0, 'hours': 0}"

UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.hours', NEW."odo" - (
    SELECT MAX(JSON_EXTRACT(duration, '$.hours')) as hours FROM maintenance WHERE id_service = 2 AND  id_bike = 1 AND date < '2020-02-07'
    ) 
    ) 
    WHERE id_service = 2 AND  id_bike = 1 AND date < '2020-02-07';


