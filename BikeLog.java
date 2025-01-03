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
                Trip[] salidas = trips();
                System.out.println("Salidas");
                for (int i=0;i<salidas.length;i++)
                    System.out.println(salidas[i].toString());
                break;
            case 2:    
                Maintenance[] mantenimientos = maintenances();
                System.out.println("Mantenimientos");
                for (int i=0;i<mantenimientos.length;i++)
                    System.out.println(mantenimientos[i].toString());
                break;
            case 3:
                /*Service[] servicios = services();
                System.out.println("Servicios");
                for (int i=0;i<servicios.length;i++)
                    System.out.println(servicios[i].toString());
                */
                serviceMenu();
                break;
            case 4:
            /*Bike[] bicicletas = bikes();
            System.out.println("Bicicletas");
            for (int i=0;i<bicicletas.length;i++)
                System.out.println(bicicletas[i].toString());
            */
            bikeMenu();
            break;  
            case 5:
                System.out.println("goodbye");
                break;
        }                  
    }
    public static void serviceMenu(){
        String name = "";
        System.out.println("\nSERVICES. ENTER AN OPTION");
        System.out.println("1.Add new Service\n2.Edit Service\n3.Search\n4.Main menu\n5.Exit");
        int option = Integer.parseInt(scanner.nextLine());
        switch (option) {
            case 1:
                System.out.print("Enter new Service name: ");
                name = scanner.nextLine();
                Service newService = new Service(0,name);
                insertValues("service",newService.getColumnList(),newService.toInsertValues());
                break;
            case 2:
                System.out.print("Enter Service ID or <search>: ");
                String field = scanner.nextLine();
                int id_service = 0;
                if (field == "")
                    id_service = searchService();
                else
                    id_service = Integer.parseInt(field);
                Service[] servicio = services("WHERE id = "+Integer.toString(id_service));//must be only one
                System.out.print("Enter new Service name or <"+servicio[0].getName()+">: ");
                field = scanner.nextLine();
                servicio[0].setName(field);
                if (field != "")
                    updateValues("service", servicio[0].toUpdateValues(),"id = "+Integer.toString(id_service));
                serviceMenu();
                break;
            case 3:
                searchService();
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

    public static int searchService (){
        System.out.print("Enter keyword for Service name: ");
        String field = scanner.nextLine();
        Service[] servicios = services("WHERE name LIKE '%"+field+"%'");//can be various
        System.out.println("Services matching keyword: \nID,name");
        for (int i=0;i<servicios.length;i++)
            System.out.println(servicios[i].toString());
        System.out.print("Enter Servce ID: ");
        return Integer.parseInt(scanner.nextLine());
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
                newBike.setBrand(scanner.nextLine().toUpperCase());
                System.out.print("Enter new Bike model: ");
                newBike.setModel(scanner.nextLine().toUpperCase());
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
                
                insertValues("bike",newBike.getColumnList(),newBike.toInsertValues(odo,ride_time));
                break;
            case 2:
                /*
                System.out.print("Enter Service ID or <search>: ");
                String field = scanner.nextLine();
                int id_service = 0;
                if (field == "")
                    id_service = searchService();
                else
                    id_service = Integer.parseInt(field);
                Service[] servicio = services("WHERE id = "+Integer.toString(id_service));//must be only one
                System.out.print("Enter new Service name or <"+servicio[0].getName()+">: ");
                field = scanner.nextLine();
                servicio[0].setName(field);
                if (field != "")
                    updateValues("service", servicio[0].toUpdateValues(),"id = "+Integer.toString(id_service));
                serviceMenu();
                */
                break;
            case 3:
                searchService();
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
    
    public static Bike[] bikes () {
        int counter = 0;
        Bike[] bicicletas = new Bike[getRowCount("bike",false)];
        // create a database connection
        try (Connection sqliteConnection = DriverManager.getConnection("jdbc:sqlite:bikelog.db");){
            Statement statement = sqliteConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM bike");
            while(rs.next()) {
                int id = rs.getInt("id"); 
                String name = rs.getString("name");
                String brand  = rs.getString("brand");;
                String model = rs.getString("model");;
                String type = rs.getString("bike_type");;
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
    public static void updateValues(String table, String set,String where){
        // create a database connection
        try(Connection connection = DriverManager.getConnection(url);){
            Statement statement = connection.createStatement();
            String updateString = "UPDATE "+table+ " SET "+set+" WHERE "+where;
            System.out.println(updateString);
            statement.executeUpdate(updateString);
        }
        catch(SQLException e)
            {e.printStackTrace(System.err); }
    }
    
    public static void insertValues(String table, String columns, String values){
        // create a database connection
        try(Connection connection = DriverManager.getConnection(url);){
            Statement statement = connection.createStatement();
            //statement.setQueryTimeout(30);  // set timeout to 30 sec.
            String updateString = "INSERT INTO "+table+ "("+columns+") VALUES ("+values+")";
            //System.out.println(updateString);
            statement.executeUpdate(updateString);
        }
        catch(SQLException e)
            {e.printStackTrace(System.err); }
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
}
