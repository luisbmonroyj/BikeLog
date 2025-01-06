DROP TABLE IF EXISTS "trip";
DROP TABLE IF EXISTS "maintenance";
DROP TABLE IF EXISTS "bike";
DROP TABLE IF EXISTS "service";

CREATE TABLE "service" (
    "id" INTEGER,
    "name" TEXT NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

CREATE TABLE "bike" (
    "id" INTEGER NOT NULL,
    "name" TEXT NOT NULL UNIQUE,
    "brand" TEXT NOT NULL,
    "model" TEXT NOT NULL,
    "type" TEXT NOT NULL CHECK ("type" IN ('MTB','ROAD','GRAVEL')),
    "starting_odo"  NUMERIC DEFAULT 0.0,
    "starting_time"  NUMERIC DEFAULT 0.0,
    "odo" NUMERIC NOT NULL,
    "ride_time" NUMERIC NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "trip" (
    "date" NUMERIC NOT NULL,
    "id_bike" INTEGER NOT NULL,
    "trip_time" NUMERIC NOT NULL,
    "distance" NUMERIC NOT NULL,
    "max_speed" NUMERIC NOT NULL,
    "velocity" NUMERIC,
    "odo" NUMERIC,
    "ride_time" NUMERIC,
    FOREIGN KEY ("id_bike") REFERENCES "bike"("id")
);

--bike must exists before entering trips
DROP TRIGGER IF EXISTS "bike_exists_trip";
CREATE TRIGGER IF NOT EXISTS "bike_exists"
BEFORE INSERT ON "trip"
FOR EACH ROW
BEGIN
    UPDATE trip SET id_bike = (SELECT id FROM bike WHERE id = NEW.id_bike);
END;

--Trigger for bike updating
DROP TRIGGER IF EXISTS "update_velocity_odo";
CREATE TRIGGER "update_velocity_odo"
AFTER INSERT ON "trip"
FOR EACH ROW
BEGIN
    --updating bike.odo
    UPDATE "bike" SET "odo" = "starting_odo" + (SELECT SUM("distance") FROM "trip" WHERE "id_bike" = NEW."id_bike"),
    "ride_time" = "starting_time" + (SELECT SUM("trip_time") FROM "trip" WHERE "id_bike" = NEW."id_bike") 
    WHERE "id" = NEW."id_bike";
    --update trip.velocity, trip.odo and trip.ride_time
    UPDATE "trip" SET "velocity" = NEW."distance" / NEW."trip_time" 
        ,"odo" = (SELECT "starting_odo" FROM "bike" WHERE id = NEW."id_bike") + 
        (SELECT SUM("distance") FROM "trip" WHERE "id_bike" = NEW."id_bike" AND date <= NEW."date") 
        ,"ride_time" = (SELECT "starting_time" FROM "bike" WHERE id = NEW."id_bike") + 
        (SELECT SUM("trip_time") FROM "trip" WHERE "id_bike" = NEW."id_bike" AND date <= NEW."date") 
        WHERE "date" = NEW."date";
    UPDATE "trip" SET "odo" = "odo" + NEW."distance" WHERE "id_bike" = NEW."id_bike" AND date > NEW."date";
END;

DROP TABLE IF EXISTS "maintenance";
CREATE TABLE "maintenance" (
    "date" NUMERIC NOT NULL,
    "id_bike" INTEGER NOT NULL,
    "odo" NUMERIC DEFAULT 0.0,
    "id_service" INTEGER NOT NULL,
    "brand" TEXT,
    "reference" TEXT,
    "price" NUMERIC,
    "description" TEXT,
    "duration" TEXT,
    FOREIGN KEY ("id_bike") REFERENCES "bike"("id"),
    FOREIGN KEY ("id_service") REFERENCES "service"("id")
);

--bike must exists before entering maintenances
DROP TRIGGER IF EXISTS "bike_exists_maintenance";
CREATE TRIGGER IF NOT EXISTS "bike_exists_maintenance"
BEFORE INSERT ON "maintenance"
FOR EACH ROW
BEGIN
    UPDATE maintenance SET id_service = (SELECT id FROM service WHERE id = NEW.id_service);
END;

-- DROP TRIGGER IF EXISTS "update_duration";
-- CREATE TRIGGER IF NOT EXISTS "update_duration"
-- AFTER INSERT ON "maintenance"
-- FOR EACH ROW
-- BEGIN
--     --duration = JSON_REPLACE(KEY1, VALUE1, KEY2, VALUE2)
--     UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.km', NEW."odo" - 
--     (SELECT odo FROM maintenance WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike" AND date < NEW."date"),
--     '$.hours', (SELECT SUM(trip_time) FROM trip WHERE id_bike = NEW."id_bike" AND date BETWEEN 
--     (SELECT date FROM maintenance WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike") AND NEW."date")    ) 
    
--     WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike" AND date = NEW."date";

--     UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.km', NEW."odo")
--         WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike" AND date = NEW."date" AND (SELECT JSON_EXTRACT(duration, '$.km') AS km) IS NULL;
-- END;

--version mejorada en proceso
DROP TRIGGER IF EXISTS "update_duration";
CREATE TRIGGER IF NOT EXISTS "update_duration"
AFTER INSERT ON "maintenance"
FOR EACH ROW
BEGIN
    --duration = JSON_REPLACE(KEY1, VALUE1, KEY2, VALUE2)
    UPDATE maintenance SET duration = JSON_REPLACE(duration, 
    '$.km', (SELECT SUM(distance) FROM trip WHERE id_bike = NEW."id_bike" AND date BETWEEN 
    (SELECT date FROM maintenance WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike") AND NEW."date"),
    '$.hours', (SELECT SUM(trip_time) FROM trip WHERE id_bike = NEW."id_bike" AND date BETWEEN 
    (SELECT date FROM maintenance WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike") AND NEW."date")    ) 
        WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike" AND date = NEW."date";
    --no esta calculando la diferencia de odo ni horas del mismo mantenimiento anterior
    UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.km', NEW."odo", 
    '$.hours', (SELECT SUM (trip_time) FROM trip WHERE date <= (SELECT MAX(date) FROM trip WHERE odo <= (NEW."odo" + 
    (SELECT "starting_odo" FROM "bike" WHERE id = NEW."id_bike")) )
                )
    )
        WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike" AND date = NEW."date" AND (SELECT JSON_EXTRACT(duration, '$.km') AS km) IS NULL;
END;

--version nueva
DROP TRIGGER IF EXISTS "update_duration";
CREATE TRIGGER IF NOT EXISTS "update_duration"
AFTER INSERT ON "maintenance"
FOR EACH ROW
BEGIN
    --UPDATE odo column
    UPDATE maintenance SET odo = (SELECT MAX("odo") FROM "trip" WHERE date <= NEW."date"),
        WHERE id_service = NEW."id_service" AND  id_bike = NEW."id_bike" AND date = NEW."date";
    --SELECT duration FROM maintenance WHERE id_bike = 1 AND id_service = 2 AND date < '2020-02-07';

END;

--Trigger for bike updating
DROP TRIGGER IF EXISTS "update_velocity_odo";
CREATE TRIGGER "update_velocity_odo"
AFTER INSERT ON "trip"
FOR EACH ROW
BEGIN
    --updating bike.odo
    UPDATE "bike" SET "odo" = "starting_odo" + (SELECT SUM("distance") FROM "trip" WHERE "id_bike" = NEW."id_bike"),
    "ride_time" = "starting_time" + (SELECT SUM("trip_time") FROM "trip" WHERE "id_bike" = NEW."id_bike") 
    WHERE "id" = NEW."id_bike";
    --update trip.velocity, trip.odo and trip.ride_time
    UPDATE "trip" SET "velocity" = NEW."distance" / NEW."trip_time" 
        ,"odo" = (SELECT "starting_odo" FROM "bike" WHERE id = NEW."id_bike") + 
        (SELECT SUM("distance") FROM "trip" WHERE "id_bike" = NEW."id_bike" AND date <= NEW."date") 
        ,"ride_time" = (SELECT "starting_time" FROM "bike" WHERE id = NEW."id_bike") + 
        (SELECT SUM("trip_time") FROM "trip" WHERE "id_bike" = NEW."id_bike" AND date <= NEW."date") 
        WHERE "date" = NEW."date";
    UPDATE "trip" SET "odo" = "odo" + NEW."distance" WHERE "id_bike" = NEW."id_bike" AND date > NEW."date";
END;



DROP TRIGGER IF EXISTS "insert_odo_maintenance";
CREATE TRIGGER IF NOT EXISTS "insert_odo_maintenance"
AFTER INSERT ON "maintenance"
FOR EACH ROW
BEGIN
    UPDATE 
        maintenance SET 
            odo = 0.0,
            duration = '{"km": 0, "hours": 0}'
    WHERE date = NEW.date AND 
            (SELECT date FROM maintenance WHERE 
            id_bike = NEW.id_bike AND 
            id_service = NEW.id_service AND
            "date" < NEW.date) IS NULL;
END;

--insert odo and duration by default
DROP TRIGGER IF EXISTS "insert_odo_maintenance";
CREATE TRIGGER IF NOT EXISTS "insert_odo_maintenance"
AFTER INSERT ON "maintenance"
FOR EACH ROW
BEGIN
    UPDATE 
        maintenance SET 
            odo = 0.0,
            duration = '{"km": 0, "hours": 0}'
    WHERE date = NEW.date AND id_service = NEW.id_service AND id_bike = NEW.id_bike;
END;

UPDATE 
        maintenance SET 
            odo = 0.0,
            duration = '{"km": 0, "hours": 0}'
    WHERE date = NEW.date AND 
            (SELECT date FROM maintenance WHERE 
            id_bike = NEW.id_bike AND 
            id_service = NEW.id_service AND
            "date" < NEW.date) IS NULL;
    
    
DROP TRIGGER "delete_odo";
CREATE TRIGGER "delete_odo"
AFTER DELETE ON "trip"
FOR EACH ROW
BEGIN
    --updating bike.odo
    UPDATE "bike" SET "odo" = "starting_odo" + (SELECT SUM("distance") FROM "trip" WHERE "id_bike" = OLD."id_bike"),
    "ride_time" = "starting_time" + (SELECT SUM("trip_time") FROM "trip" WHERE "id_bike" = OLD."id_bike") 
    WHERE "id" = OLD."id_bike";
    --update trip odo
    UPDATE "trip" SET "odo" = "odo" - OLD."distance" WHERE "id_bike" = OLD."id_bike" AND date >= OLD."date" ;
END;

    --SELECT MAX(JSON_EXTRACT(duration, '$.km')) as last_odo FROM maintenance WHERE id_bike = 1 AND id_service = 3 AND date < '2023-03-05';
    -- UPDATE 
    --     maintenance SET 
    --         odo = 0.0,
    --         duration = JSON_REPLACE(duration, '$.km', 0.0, '$.hours',0.0)
    -- WHERE odo IS NULL AND date = NEW.date;



UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.km', 720) WHERE id_service = 2 AND  id_bike = 1 AND date < '2020-02-07';
UPDATE maintenance SET duration = JSON_REPLACE(duration, '$.hours', 100) WHERE id_service = 2 AND  id_bike = 1 AND date < '2020-02-07';

--to import a csv with column names
--.import --csv data-no-ids.csv tempTable


--to import a csv without column names
--.import --csv --skip 1 data.csv destinyTable
--.import --csv --skip 1 trip-raw-min-data.csv trip

-- .read /home/luisbmonroyj/dev-projects/BikeLog/schema.sql

--to check time of queries
-- .timer on