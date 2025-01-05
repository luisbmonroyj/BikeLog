INSERT INTO "service" ("name") VALUES ('bicycle');
INSERT INTO "service" ("name") VALUES ('pedals');
INSERT INTO "service" ("name") VALUES ('rear brake pad');
INSERT INTO "service" ("name") VALUES ('front brake pad');
INSERT INTO "service" ("name") VALUES ('rear tire');
INSERT INTO "service" ("name") VALUES ('front tire');

INSERT INTO "bike" ("name","brand","model","type","odo","ride_time","starting_odo","starting_time") 
VALUES ('STAR','TREK','1500','ROAD',0.0,0.0,0.0,0.0);
INSERT INTO "bike" ("name","brand","model","type","odo","ride_time","starting_odo","starting_time") 
VALUES ('STEF','GARY FISHER','ROSCOE ONE','MTB',0.0,0.0,0.0,0.0);

INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") VALUES
('2019-10-01',1,0.70333333,21.36,41);
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") VALUES
('2019-10-02',1,0.8194444445,26.12,43.3);
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") 
VALUES ('2019-10-03',1,0.323333333,5.58,38);
INSERT INTO "trip" ("date","id_bike","trip_time","distance","max_speed") 
VALUES ('2019-10-05',1,0.5577777777,17.18,42.4);

DELETE FROM trip WHERE date = '2019-10-03';
SELECT * FROM trip ORDER BY date;
SELECT * FROM bike;

DELETE FROM trip;
UPDATE bike SET odo = 0, ride_time = 0, starting_odo = 1000 WHERE id = 1;
SELECT * FROM trip;
SELECT * FROM bike;