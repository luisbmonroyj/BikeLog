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
    "id" INTEGER,
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

--to import a csv with column names
--.import --csv data-no-ids.csv tempTable

--to import a csv without column names
--.import --csv --skip 1 data.csv destinyTable

-- .read /home/luisbmonroyj/dev-projects/BikeLog/schema.sql
