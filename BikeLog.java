import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

/*
   instalar jdk-23_linux-x64_bin.deb
   descargar sqlite-jdbc-3.47.1.0.jar en la carpeta de trabajo
 * compilar
   javac BikeLog.java
 * ejecutar
   java -cp .:sqlite-jdbc-3.47.1.0.jar BikeLog

To make a simpler script
nano BikeLog.sh
type this inside it:
cd 
cd dev-projects/BikeLog
java -cp .:sqlite-jdbc-3.47.1.0.jar BikeLog
Ctrl + X 
Y
Intro

Execute it 
bash BikeLog.sh

    para exportar los datos
    sqlite> .headers on
    sqlite> .mode csv
    sqlite> .output data.csv
    sqlite> SELECT * FROM trip;
    sqlite> .quit
 */

import java.time.LocalDateTime;

public class BikeLog {

    private static String url = "jdbc:sqlite:/home/luisbmonroyj/Dropbox/bikelog.db";
    private static int default_bikeId = 1;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args){
        System.out.println("BikeLog for java.");
        mainMenu();
    }
    public static void mainMenu(){
        System.out.println("\nPlease insert an option:\n1.Trips\n2.Maintenances\n3.Services\n4.Bikes\n5.Exit");
        int option = Integer.parseInt(scanner.nextLine());
        switch (option) {
        case 1:
            tripMenu();
            break;
        case 2:    
            maintenanceMenu();
            break;
        case 3:
            serviceMenu();
            break;
        case 4:
            bikeMenu();
        break;  
        case 5:
            System.out.println("goodbye");
            break;
        }                  
    }

    public static void tripMenu(){
        int id_bike = 0;
        System.out.println("\nTRIPS. ENTER AN OPTION");
        System.out.println("1.Add new Trip\n2.Edit Trip\n3.Search\n4.Main menu\n5.Exit");
        int option = Integer.parseInt(scanner.nextLine());
        switch (option) {
        case 1:
            setTripData(setBikeId(),"new Trip", false);
            tripMenu();
            break;
        case 2:
            searchTrips();
            setTripData(setBikeId(),"from the list above, Trip", true);
            tripMenu();
            break;
        case 3:
            searchTrips();
            tripMenu();
            break;
        case 4:
            mainMenu();
            break;
        case 5:
            break;
        }
    }
    public static void setTripData(int id_bike, String event, boolean existing){
        LocalDate date = setDate (event);
        //int id_bike = setBikeId();
        String where = "id_bike = "+Integer.toString(id_bike)+" AND date = '"+date.toString()+"'";
        Trip[] oldTrips = trips(" WHERE "+where); //method trips() has no where clause by default
        Trip trip = new Trip();
        if (oldTrips.length >= 1){ //there are more than 1 trip made that day with the same bicycle, filter by distance
            if (oldTrips.length > 1){
                System.out.println("Trips made that day: ");
                for (int i=0;i<oldTrips.length;i++)
                System.out.println(oldTrips[i].toString());
            
            System.out.println("\nEnter trip distance of the Trip to be modified: ");
            double distance = Double.parseDouble(scanner.nextLine());
            where += " AND distance = "+Double.toString(distance);
            oldTrips = trips("WHERE "+where); //length must be 1
            }
            trip = oldTrips[0];
        }
        double time = trip.getTrip_time();
        double distance = trip.getDistance();
        double speed = trip.getMaxSpeed();
        if (existing){
            time = setTime("Enter ride time (HH.MM.SS) or <"+trip.getTrip_time()+">: ",trip.getTrip_time());
            System.out.print("Enter distance (km) or <"+Double.toString(trip.getDistance())+">: ");
            String field = scanner.nextLine();
            if (field != "")
                distance = Double.parseDouble(field);
            System.out.print("Enter max speed (km/h) or <"+Double.toString(trip.getMaxSpeed())+">: ");
            field = scanner.nextLine();
            if (field != "")
                speed = Double.parseDouble(field);
            Trip modifyTrip = new Trip (date,id_bike,time,distance,speed,(distance/time));
            updateValues("trip",modifyTrip.toUpdateValues(), where, true);
        }
        else{
            time = setTime("Enter ride time (HH.MM.SS): ",0);
            System.out.print("Enter distance (km): ");
            distance = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter max speed (km/h): ");
            speed = Double.parseDouble(scanner.nextLine());
            Trip newTrip = new Trip (date,id_bike,time,distance,speed,(distance/time));
            insertValues("trip", newTrip.getColumnList(), newTrip.toInsertValues(), true);
        }
        System.out.println("Average Velocity: "+Double.toString(distance/time)+" km/h");
    }

    public static double setTime(String question,double time){
        System.out.print(question);
        String field = scanner.nextLine();
        if (!field.equals("")){
            String[] dateString = {};
            dateString = field.split("[.]");
            LocalTime tripTime = LocalTime.of(Integer.parseInt(dateString[0]),Integer.parseInt(dateString[1]),Integer.parseInt(dateString[2]));
            time = tripTime.getHour()+(double)tripTime.getMinute()/60+(double)tripTime.getSecond()/3600;
        }
        return time;
    }
    public static Trip[] searchTrips(){
        int id_bike = setBikeId();
        System.out.println("If initial date is <today>, all Trips done so far and meeting criteria will be shown");
        String query = "WHERE id_bike = "+Integer.toString(id_bike);
        LocalDate date1 = setDate ("Trip initial");
        if (date1.toString().equals(LocalDate.now().toString())) 
            query += " AND date <= '"+date1.toString()+"'";
        else{
            LocalDate date2 = setDate ("Trip final");
            query+= " AND date >= '"+date1.toString()+"' AND date <= '"+date2.toString()+"' ";
        }
        Trip[] trips = trips(query);
        System.out.println("\nTrips meeting criteria: \ndate,id_bike,trip_time,distance,max_speed,velocity");
        for (int i=0;i<trips.length;i++)
            System.out.println(trips[i].toString());
        return trips;
    }

    public static void maintenanceMenu(){
        //"date,id_bike,odo,id_service,brand,reference,price,description,duration";
        String field = "";
        int id_bike = 0;
        int id_service = 0;
        System.out.println("\nMAINTENANCES. ENTER AN OPTION");
        System.out.println("1.Add new Maintenance\n2.Edit Maintenance\n3.Search\n4.Main menu\n5.Exit");
        int option = Integer.parseInt(scanner.nextLine());
        switch (option) {
        case 1:
            LocalDate date = setDate ("new Maintenance");
            id_bike = setBikeId();
            id_service = setId_service(" or <Enter for search, 0 for a new one>:");
            if (id_service == 0){
                System.out.print("Enter new Service name: ");
                String name = scanner.nextLine();
                Service newService = new Service(0,name);
                insertValues("service",newService.getColumnList(),newService.toInsertValues(),false);
                id_service = services("WHERE name = '"+newService.getName()+"'")[0].getId();
            }
            System.out.print("Enter Brand: ");
            String brand = scanner.nextLine().toUpperCase();
            
            System.out.print("Enter Reference: ");
            String reference = scanner.nextLine().toUpperCase();
            
            double price = 0.0;
            System.out.print("Enter Price (COP K$) or <0.0>: ");
            field = scanner.nextLine();
            if (field != "")
                price = Double.parseDouble(field);
            
            System.out.print("Enter Description: ");
            String description = scanner.nextLine();
            
            String duration = "{\"km\": 0.0, \"hours\":0.0}";
            
            Maintenance maintenance = new Maintenance(date,id_bike,0.0,id_service,brand,reference,price,description,duration);
            insertValues("maintenance", maintenance.getColumnList(), maintenance.toString(), true);
            maintenanceMenu();
            break;
        case 2:
            System.out.println("For simplicity, first search between done maintenances to edit one of them");
            Maintenance[] mantenimientos = searchMaintenance();
            //mantenimientos tiene el id_bike
            System.out.println("Select Maintenance event:"); 
            
            date = setDate("Maintenance");
            id_service = setId_service(" or <Enter to search by keyword>:");
            
            mantenimientos = maintenances("WHERE id_bike = "+Integer.toString(mantenimientos[0].getId_bike())+
                " AND id_service = "+Integer.toString(id_service)+" AND date = '"+date.toString()+"'");
            //must be only one

            System.out.println("\nEnter new Maintenance event changes:"); 
            mantenimientos[0].setDate(setDate("Maintenance",date.toString(),date));
            int id_service_change = setId_service(" or <"+Integer.toString(id_service)+">:");
            
            mantenimientos[0].setId_service(id_service_change);

            System.out.print("Enter Brand or <"+mantenimientos[0].getBrand()+">: ");
            field = scanner.nextLine().toUpperCase();
            if (field != "")
                mantenimientos[0].setBrand(field);

            System.out.print("Enter Reference or <"+mantenimientos[0].getReference()+">: ");
            field = scanner.nextLine().toUpperCase();
            if (field != "")
                mantenimientos[0].setReference(field);
            
            System.out.print("Enter Price (COP K$) or <"+Double.toString(mantenimientos[0].getPrice())+">: ");
            field = scanner.nextLine();
            if (field != "")
                mantenimientos[0].setPrice(Double.parseDouble(field));
            
            System.out.print("Enter Description: or <"+mantenimientos[0].getDescription()+">: ");
            field = scanner.nextLine();
            if (field != "")
                mantenimientos[0].setDescription(field);

            mantenimientos[0].setDuration("{\"km\": 0.0, \"hours\":0.0}");
            updateValues("maintenance", mantenimientos[0].toUpdateValues(), " id_bike = "+Integer.toString(mantenimientos[0].getId_bike())+
                " AND id_service = "+Integer.toString(mantenimientos[0].getId_service())+ " AND date = '"+date.toString()+"'", false);
            maintenanceMenu();
            break;
        case 3:
            searchMaintenance();
            maintenanceMenu();
            break;
        case 4:
            mainMenu();
            break;
        case 5:
            System.out.println("goodbye");
            break;
        }
    }

    public static Maintenance[] searchMaintenance(){
        System.out.println("If initial date is <today>, all maintenances done so far and meeting criteria will be shown");
        int id_bike = setBikeId();
        int id_service = setId_service(" or <0 shows all, Enter to search by keyword>: ");
        String query = "WHERE id_bike = "+Integer.toString(id_bike);
        if (id_service != 0)
            query += " AND id_service = "+Integer.toString(id_service);
        LocalDate date1 = setDate ("Maintenance initial");
        if (date1.toString().equals(LocalDate.now().toString())) 
            query += " AND date <= '"+date1.toString()+"'";
        else{
            LocalDate date2 = setDate ("Maintenance final");
            query+= " AND date >= '"+date1.toString()+"' AND date <= '"+date2.toString()+"' ";
        }
        Maintenance[] mantenimientos = maintenances(query);
        System.out.println("List of maintenances:\ndate,id_bike,id_service,brand,reference,price,description,duration");
        for (int i=0;i<mantenimientos.length;i++)
            System.out.println(mantenimientos[i].toString());
        return mantenimientos;
    }

    public static LocalDate setDate (String event){
        System.out.print("Enter "+event+" date (YYYYY-MM-DD) or <today>: ");
        String field = scanner.nextLine();
        String[] dateString = {};
        LocalDate date = LocalDate.now();
        if (field != ""){
            dateString = field.split("[-]");
            date = date.of(Integer.parseInt(dateString[0]),Integer.parseInt(dateString[1]),Integer.parseInt(dateString[2]));
        }
        //System.out.println(date.toString());
        return date;
    }

    public static LocalDate setDate (String event, String orOption, LocalDate sameDate){
        System.out.print("Enter "+event+" date (YYYYY-MM-DD) or <"+orOption+">: ");
        String field = scanner.nextLine();
        String[] dateString = {};
        LocalDate date = sameDate;
        if (field != ""){
            dateString = field.split("[-]");
            date = date.of(Integer.parseInt(dateString[0]),Integer.parseInt(dateString[1]),Integer.parseInt(dateString[2]));
        }
        //System.out.println(date.toString());
        return date;
    }

    public static int setBikeId (){
        System.out.print("Enter Bike ID or <list>: ");
        String field = scanner.nextLine();
        if (field == "") {
            Bike[] bicicletas = bikes("");
            System.out.println("\n List of bikes: \nname,brand,model,type,odo,ride_time");
            for (int i=0;i<bicicletas.length;i++)
                System.out.println(bicicletas[i].toString());
            System.out.print("Enter Bike ID: ");
            field = scanner.nextLine();
        }
        return Integer.parseInt(field);
    }

    public static void serviceMenu(){
        String name = "";
        String field = "";
        System.out.println("\nSERVICES. ENTER AN OPTION");
        System.out.println("1.Add new Service\n2.Edit Service\n3.Search\n4.Main menu\n5.Exit");
        int option = Integer.parseInt(scanner.nextLine());
        switch (option) {
        case 1:
            System.out.print("Enter new Service name: ");
            name = scanner.nextLine();
            Service newService = new Service(0,name);
            insertValues("service",newService.getColumnList(),newService.toInsertValues(),false);
            serviceMenu();
            break;
        case 2:
            int id_service = setId_service(":");
            Service[] servicio = services("WHERE id = "+Integer.toString(id_service));//must be only one
            System.out.print("Enter Service new name or <"+servicio[0].getName()+">: ");
            field = scanner.nextLine();
            servicio[0].setName(field);
            if (field != "")
                updateValues("service", servicio[0].toUpdateValues(),"id = "+Integer.toString(id_service),false);
            serviceMenu();
            break;
        case 3:
            searchService(false,"is false");
            serviceMenu();
            break;
        case 4:
            mainMenu();
            break;
        case 5:
            System.out.println("goodbye");
            break;
        }
    }

    public static int searchService (boolean input,String orOoption){
        System.out.print("Enter keyword for Service name: ");
        String field = scanner.nextLine();
        Service[] servicios = services("WHERE name LIKE '%"+field+"%'");//can be various
        System.out.println("Services matching keyword: \nID,name");
        for (int i=0;i<servicios.length;i++)
            System.out.println(servicios[i].toString());
        if (input){
            System.out.print("Enter Service ID: ");//+orOoption);
            field = scanner.nextLine();
            if (field == "")
                return 0;
            else
            return Integer.parseInt(field);
        }
        else
            return 0;
    }
    
    public static int setId_service(String orOption){
        int id_service = 0;
        System.out.print("Enter Service ID"+orOption);
        String field = scanner.nextLine();
        if (field == "")
            id_service = searchService(true,orOption);
        else
            id_service = Integer.parseInt(field);
        return id_service;        
    }

    public static void bikeMenu(){
        System.out.println("\nBIKES. ENTER AN OPTION");
        System.out.println("1.Add new Bike\n2.Edit Bike\n3.Search\n4.Main menu\n5.Exit");
        int option = Integer.parseInt(scanner.nextLine());
        Bike newBike = new Bike();
        switch (option) {
            case 1:
                System.out.print("Enter new Bike name: ");
                newBike.setName(scanner.nextLine().toUpperCase());
                System.out.print("Enter new Bike brand: ");
                String brand = scanner.nextLine().toUpperCase();
                newBike.setBrand(brand);
                System.out.print("Enter new Bike model: ");
                String model = scanner.nextLine().toUpperCase();
                newBike.setModel(model);
                System.out.print("Enter new Bike type (ROAD/MTB/GRAVEL): ");
                newBike.setType(scanner.nextLine().toUpperCase());
                newBike.setOdo(0.0);
                newBike.setRideTime(0.0);
                insertValues("bike",newBike.getColumnList(),newBike.toInsertValues(),false);
                //a Maintenance with id_service = X must be created
                LocalDate date = setDate ("Bike Acquisition");
                Bike[] bike = bikes("WHERE name = '"+newBike.getName()+"'");
                double price = 0.0;
                System.out.print("Enter Price (COP K$) or <0.0>: ");
                String field = scanner.nextLine();
                if (field != "")
                    price = Double.parseDouble(field);
                System.out.print("Enter Description: ");
                String description = scanner.nextLine();
                String duration = "{\"km\": 0.0, \"hours\":0.0}";
                Maintenance maintenance = new Maintenance(date,bike[0].getId(),0.0,1,brand,model,price,description,duration);
                insertValues("maintenance", maintenance.getColumnList(), maintenance.toString(), true);
                break;
            case 2:
                Bike[] bicicletas = bikes("");
                System.out.println("\n List of bikes: \nname,brand,model,type,odo,ride_time");
                //LIST OF BIKES
                for (int i=0;i<bicicletas.length;i++)
                    System.out.println(bicicletas[i].toString());
                //SELECT BIKE BY ID
                System.out.print("Enter Bike ID: ");
                int id_bike = Integer.parseInt(scanner.nextLine());
                bicicletas = bikes("WHERE id = "+Integer.toString(id_bike));//must be only one
                
                System.out.print("Enter bike's new name or <"+bicicletas[0].getName()+">: ");
                field = scanner.nextLine().toUpperCase();
                if (field != "")
                    bicicletas[0].setName(field);
                System.out.print("Enter bike's new Brand or <"+bicicletas[0].getBrand()+">: ");
                field = scanner.nextLine().toUpperCase();
                if (field != "")
                    bicicletas[0].setBrand(field);
                System.out.print("Enter bike's new Model or <"+bicicletas[0].getModel()+">: ");
                field = scanner.nextLine().toUpperCase();
                if (field != "")
                    bicicletas[0].setModel(field);
                System.out.print("Enter bike's new Type (MTB,ROAD,GRAVEL) or <"+bicicletas[0].getType()+">: ");
                field = scanner.nextLine().toUpperCase();
                if (field != "")
                    bicicletas[0].setType(field);
                updateValues("bike", bicicletas[0].toUpdateValues(),"id = "+Integer.toString(id_bike),false);
                bikeMenu();
                break;
            case 3:
                String[] columns = {"name", "brand", "model","type"};
                System.out.println("Enter feature to search for:\n1.name\n2.brand\n3.model\n4.type");
                int chosen = Integer.parseInt(scanner.nextLine());
                searchBike(columns[chosen-1]);
                bikeMenu();
                break;
            case 4:
                mainMenu();
                break;
            case 5:
                System.out.println("goodbye");
                break;
        }
    }
    
    public static void searchBike (String column){
        System.out.print("Enter keyword for Bike "+column+": ");
        String field = scanner.nextLine();
        Bike[] servicios = bikes("WHERE "+column+" LIKE '%"+field+"%'");//can be various
        System.out.println("Bikes matching keyword: \nID,name,brand,model,odo,ride_time");
        for (int i=0;i<servicios.length;i++)
            System.out.println(servicios[i].toString());
    }
    
    public static Trip[] trips (String where) {
        int counter = 0;
        Trip[] salidas = new Trip[getRowCount("trip "+where,true)];
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM trip "+where+" ORDER BY date");
            while(rs.next()) {
                String[] myArray = rs.getString("date").split("[-]");
                int anno = Integer.parseInt(myArray[0]);
                int mes =Integer.parseInt(myArray[1]);
                int dia = Integer.parseInt(myArray[2]);
                LocalDate date = LocalDate.of(anno,mes,dia);
                int id_bike = rs.getInt("id_bike");
                double trip_time = rs.getDouble("trip_time");
                double distance = rs.getDouble("distance");
                double max_speed = rs.getDouble("max_speed");
                double velocity = rs.getDouble("velocity");
                salidas[counter] = new Trip(date,id_bike,trip_time,distance,max_speed,velocity); 
                counter++;
            }
            sqliteConnection.close();
        }
        catch(SQLException e)
            {e.printStackTrace(System.err); }
     
    return salidas;
    }
    
    public static Maintenance[] maintenances (String where) {
        int counter = 0;
        Maintenance[] mantenimientos = new Maintenance[getRowCount("maintenance "+where,true)];
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM maintenance "+where+" ORDER BY date");
            //System.out.println("SELECT * FROM maintenance "+where);
            while(rs.next()) {
                String[] myArray = rs.getString("date").split("[-]");
                int anno = Integer.parseInt(myArray[0]);
                int mes =Integer.parseInt(myArray[1]);
                int dia = Integer.parseInt(myArray[2]);
                LocalDate date = LocalDate.of(anno,mes,dia);
                int id_bike = rs.getInt("id_bike");
                double odo = rs.getDouble("odo");
                int id_service = rs.getInt("id_service");
                String brand = rs.getString("brand");
                String reference = rs.getString("reference");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                String duration = rs.getString("duration");
                mantenimientos[counter] = new Maintenance(date,id_bike,odo,id_service,brand,reference,price,description,duration); 
                counter++;
            }
            sqliteConnection.close();
        }
        catch(SQLException e)
            {e.printStackTrace(System.err); }
     
    return mantenimientos;
    }
    
    public static Bike[] bikes (String where) {
        int counter = 0;
        Bike[] bicicletas = new Bike[getRowCount("bike "+where,false)];
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM bike "+where);
            while(rs.next()) {
                int id = rs.getInt("id"); 
                String name = rs.getString("name");
                String brand  = rs.getString("brand");;
                String model = rs.getString("model");;
                String type = rs.getString("type");;
                double odo = rs.getDouble("odo");
                double rideTime = rs.getDouble("ride_time");;
                bicicletas[counter] = new Bike(id,name,brand,model,type,odo,rideTime); 
                counter++;
            }
            sqliteConnection.close();
        }
        catch(SQLException e)
            {e.printStackTrace(System.err); }
     
    return bicicletas;
    }
    
    public static Service[] services (String where) {
        int counter = 0;
        Service[] servicios = new Service[getRowCount("service "+where,false)];
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM service "+where);
            while(rs.next()) {
                int id = rs.getInt("id"); 
                String name = rs.getString("name");
                servicios[counter] = new Service(id,name);  
                counter++;
            }
            sqliteConnection.close();
        }
        catch(SQLException e)
            {e.printStackTrace(System.err); }
     
    return servicios;
    }
    
    public static void updateValues(String table, String set,String where, boolean echo){
        String updateString = "UPDATE "+table+ " SET "+set+" WHERE "+where;
        // create a database connection
        try(Connection connection = DriverManager.getConnection(url);){
            Statement statement = connection.createStatement();
            statement.executeUpdate(updateString);
        }
        catch(SQLException e){
            e.printStackTrace(System.err); 
            if (echo)
                System.out.println(updateString);
        }
    }
    
    public static void insertValues(String table, String columns, String values, boolean echo){
        String updateString = "INSERT INTO "+table+ "("+columns+") VALUES ("+values+")";
        // create a database connection
        try(Connection connection = DriverManager.getConnection(url);){
            Statement statement = connection.createStatement();
            //statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate(updateString);
        }
        catch(SQLException e) {
            e.printStackTrace(System.err); 
            if (echo)
                System.out.println(updateString);
        }
    }

    public static int getRowCount (String table, boolean orderByDate) {
        //in "String table" can be added a WHERE condition
        int counter = 0;
        String queryString = "SELECT * FROM "+table;
        if (orderByDate)
            queryString += " ORDER BY date";
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery(queryString);
            while(rs.next())
                counter++;
            sqliteConnection.close();
        }
        catch(SQLException e)
            {e.printStackTrace(System.err); }
        
    return counter;
    }

    public static double getHours (int id_bike, int id_service, String date, boolean orderByDate,boolean echo) {
        double hours = 0.0;
        String queryString = "SELECT MAX(JSON_EXTRACT(duration, '$.hours')) as hours FROM maintenance WHERE id_bike = "+id_bike;
        queryString += " AND id_service = "+id_service+" AND date < '"+date+"'";
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery(queryString);
            while(rs.next())
                hours = rs.getDouble("hours"); 
            sqliteConnection.close();
        }
        catch(SQLException e){
            e.printStackTrace(System.err); 
            if (echo)
                System.out.println(queryString);
        }   
    return hours;
    }
}
