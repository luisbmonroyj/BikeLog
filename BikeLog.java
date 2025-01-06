import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

/*
   instalar jdk-23_linux-x64_bin.deb
 * compilar
   javac BikeLog.java
 * ejecutar
   java -cp .:sqlite-jdbc-3.47.1.0.jar BikeLog
 */
import java.time.LocalDateTime;

public class BikeLog {

    private static String url = "jdbc:sqlite:bikelog.db";
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
            //comprobar que se haya creado una bicicleta en maintenance antes de ingresar el primer trip    
            Trip[] salidas = trips();
                System.out.println("Salidas");
                for (int i=0;i<salidas.length;i++)
                    System.out.println(salidas[i].toString());
                break;
            case 2:    
                maintenanceMenu();
                /*
                Maintenance[] mantenimientos = maintenances();
                System.out.println("Mantenimientos");
                for (int i=0;i<mantenimientos.length;i++)
                    System.out.println(mantenimientos[i].toString());
                */
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

    public static void maintenanceMenu(){
        //"date,id_bike,odo,id_service,brand,reference,price,description,duration";
        String field = "";
        System.out.println("\nMAINTENANCES. ENTER AN OPTION");
        System.out.println("1.Add new Maintenance\n2.Edit Maintenance\n3.Search\n4.Main menu\n5.Exit");
        int option = Integer.parseInt(scanner.nextLine());
        switch (option) {
            case 1:
                LocalDate date = setDate ("Maintenance");
                int id_bike = setBikeId();
                int id_service = setId_service();
                
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
                //field = scanner.nextLine();
                /*
                if (field == "1"){
                    odo = 0.0;
                    //SELECT MAX (odo) FROM trip WHERE date <= <fecha de maintenance>
                    System.out.println("before UNDER CONSTRUCTION");
                }
                else{
                    odo = 10.0;
                    //SELECT MAX (odo) FROM trip WHERE date = <fecha de maintenance>
                    System.out.println("after UNDER CONSTRUCTION");
                }
                */
                break;
            case 2:
                date = setDate ("Maintenance");
                double km = getHours(1, 2, date.toString(), false, true);
                System.out.print(Double.toString(km));
                
            /*
                System.out.print("Enter Service ID or <search>: ");
                field = scanner.nextLine();
                int id_service = 0;
                if (field == "")
                    id_service = searchService(false);
                else
                    id_service = Integer.parseInt(field);
                Service[] servicio = services("WHERE id = "+Integer.toString(id_service));//must be only one
                System.out.print("Enter new Service name or <"+servicio[0].getName()+">: ");
                field = scanner.nextLine();
                servicio[0].setName(field);
                if (field != "")
                    updateValues("service", servicio[0].toUpdateValues(),"id = "+Integer.toString(id_service),false);
                serviceMenu();
                */
                break;
            case 3:
                searchService(false);
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

    public static LocalDate setDate (String event){
        System.out.print("Enter new "+event+" date (YYYYY-MM-DD) or <today>: ");
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
                int id_service = setId_service();
                Service[] servicio = services("WHERE id = "+Integer.toString(id_service));//must be only one
                System.out.print("Enter new Service name or <"+servicio[0].getName()+">: ");
                field = scanner.nextLine();
                servicio[0].setName(field);
                if (field != "")
                    updateValues("service", servicio[0].toUpdateValues(),"id = "+Integer.toString(id_service),false);
                serviceMenu();
                break;
            case 3:
                searchService(false);
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

    public static int searchService (boolean input){
        System.out.print("Enter keyword for Service name: ");
        String field = scanner.nextLine();
        Service[] servicios = services("WHERE name LIKE '%"+field+"%'");//can be various
        System.out.println("Services matching keyword: \nID,name");
        for (int i=0;i<servicios.length;i++)
            System.out.println(servicios[i].toString());
        if (input){
            System.out.print("Enter Servce ID: ");
            return Integer.parseInt(scanner.nextLine());
        }
        else
            return 0;
    }
    
    public static int setId_service(){
        int id_service = 0;
        System.out.print("Enter Service ID or <search>: ");
        String field = scanner.nextLine();
        if (field == "")
            id_service = searchService(true);
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
                double odo = 0.0;
                System.out.print("Enter new Bike starting odometer reading <0>: ");
                String field = scanner.nextLine();
                if (field != "")
                    odo = Double.parseDouble(field);
                double ride_time = 0.0;
                System.out.print("Enter new Bike starting ride time reading <0>: ");
                field = scanner.nextLine();
                if (field != "")
                    ride_time= Double.parseDouble(field);
                //odo and ride_time goes to databases' (starting_odo and starting_time) columns instead of odo and ride_time
                //because these last ones store kilometers and hours registered by the user
                insertValues("bike",newBike.getColumnList(),newBike.toInsertValues(odo,ride_time),false);
                //a Maintenance with id_service = X must be created
                LocalDate date = setDate ("Bike Acquisition");
                Bike[] bike = bikes("WHERE name = '"+newBike.getName()+"'");
                double price = 0.0;
                System.out.print("Enter Price (COP K$) or <0.0>: ");
                field = scanner.nextLine();
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
        //System.out.print("Enter Servce ID: ");
        //return Integer.parseInt(scanner.nextLine());
    }
    
    public static Trip[] trips () {
        int counter = 0;
        Trip[] salidas = new Trip[getRowCount("trip",true)];
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM trips ORDER BY date");
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
    
    public static Maintenance[] maintenances () {
        int counter = 0;
        Maintenance[] mantenimientos = new Maintenance[getRowCount("maintenance",true)];
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection(url);){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM maintenance");
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
