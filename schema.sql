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

CREATE TABLE "maintenance" (
    "date" NUMERIC,
    "id_bike" INTEGER,
    "odo" NUMERIC,
    "id_service" INTEGER NOT NULL,
    "brand" TEXT,
    "reference" TEXT,
    "price" NUMERIC,
    "description" TEXT,
    "duration" TEXT,
    FOREIGN KEY ("id_bike") REFERENCES "bike"("id"),
    FOREIGN KEY ("id_service") REFERENCES "service"("id")
);

CREATE TABLE "trip" (
    "date" NUMERIC NOT NULL,
    "id_bike" INTEGER NOT NULL,
    "trip_time" NUMERIC NOT NULL,
    "distance" NUMERIC NOT NULL,
    "max_speed" NUMERIC NOT NULL,
    "velocity" NUMERIC,
    "odo" NUMERIC,
    FOREIGN KEY ("id_bike") REFERENCES "bike"("id")
);

--A TRIGGER FOR VELOCITY CALCULATION
-- CREATE TRIGGER "calculate_velocity"
-- AFTER INSERT ON "trip"
-- FOR EACH ROW
--     UPDATE "trip" SET "velocity" = NEW."distance" / NEW."trip_time" WHERE "date" = NEW."date";
-- BEGIN
-- END;

--Trigger for bike updating
CREATE TRIGGER "update_bike"
AFTER INSERT ON "trip"
FOR EACH ROW
BEGIN
    --velocity calc
    UPDATE "trip" SET "velocity" = NEW."distance" / NEW."trip_time" WHERE "date" = NEW."date";
    --updating bike.odo
    UPDATE "bike" SET "odo" = "starting_odo" + (SELECT SUM("distance") FROM "trip" WHERE "id_bike" = NEW."id_bike"),
    "ride_time" = "starting_time" + (SELECT SUM("trip_time") FROM "trip" WHERE "id_bike" = NEW."id_bike") 
    WHERE "id" = NEW."id_bike";
    --update trip odo
END;

--to import a csv with column names
--.import --csv data-no-ids.csv tempTable
