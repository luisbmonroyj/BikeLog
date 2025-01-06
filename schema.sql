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

DROP TRIGGER IF EXISTS "delete_odo";
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
    "duration" TEXT DEFAULT '{"km":0.0,"hours":0.0}',
    FOREIGN KEY ("id_bike") REFERENCES "bike"("id"),
    FOREIGN KEY ("id_service") REFERENCES "service"("id")
);

--bike must exists before entering maintenances
DROP TRIGGER IF EXISTS "bike_exists_maintenance";
CREATE TRIGGER IF NOT EXISTS "bike_exists_maintenance"
BEFORE INSERT ON "maintenance"
FOR EACH ROW
BEGIN
    UPDATE 
        maintenance SET 
            id_service = (SELECT id FROM service WHERE id = NEW.id_service)
    WHERE date = NEW.date AND id_service = NEW.id_service AND id_bike = NEW.id_bike;
END;

--AFTER INSERTION, odo must be updated assuming no equal previous maintenance done
DROP TRIGGER IF EXISTS "update_odo_maintenance";
CREATE TRIGGER IF NOT EXISTS "update_odo_maintenance"
AFTER INSERT ON "maintenance"
FOR EACH ROW
BEGIN
    UPDATE 
        maintenance SET 
            odo = (SELECT MAX(odo) FROM trip WHERE id_bike = NEW.id_bike AND date <= NEW.date)
            ,duration = JSON_REPLACE(duration, '$.km', (SELECT MAX(odo) FROM trip WHERE id_bike = NEW.id_bike AND date <= NEW.date),
            '$.hours', (SELECT MAX(ride_time) FROM trip WHERE id_bike = NEW.id_bike AND date <= NEW.date))
    WHERE date = NEW."date" AND id_service = NEW."id_service" AND id_bike = NEW."id_bike";
END;

--add odo reading to maintenance
DROP TRIGGER IF EXISTS "update_duration_maintenance";
CREATE TRIGGER IF NOT EXISTS "update_duration_maintenance"
AFTER UPDATE ON "maintenance"
FOR EACH ROW
BEGIN
    -- if a previous id_service,id_bike exists, update with the difference
    UPDATE 
        maintenance SET 
            duration = 
            JSON_REPLACE(duration, '$.km', 
                (SELECT JSON_EXTRACT(duration, '$.km') as "km" FROM maintenance WHERE 
                    id_bike = NEW.id_bike AND 
                    id_service = NEW.id_service 
                    AND date = NEW.date) -
                (SELECT MAX(JSON_EXTRACT(duration, '$.km')) as "km" FROM maintenance WHERE 
                    id_bike = NEW.id_bike AND 
                    id_service = NEW.id_service AND 
                    "date" < NEW.date),
                                '$.hours', 
                (SELECT JSON_EXTRACT(duration, '$.hours') as "hours" FROM maintenance WHERE 
                    id_bike = NEW.id_bike AND 
                    id_service = NEW.id_service 
                    AND date = NEW.date) -
                (SELECT MAX(JSON_EXTRACT(duration, '$.hours')) as "hours" FROM maintenance WHERE 
                    id_bike = NEW.id_bike AND 
                    id_service = NEW.id_service AND 
                    "date" < NEW.date)
            )
    WHERE date = NEW.date AND id_bike = NEW.id_bike AND id_service = NEW.id_service AND
            (SELECT date FROM maintenance WHERE 
            id_bike = NEW.id_bike AND 
            id_service = NEW.id_service AND
            "date" < NEW.date) IS NOT NULL;
END;
