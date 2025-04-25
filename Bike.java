// taken from /luisbmonroyj/Documents/01 Programacion/projects/backend 

public class Bike {
    //@Id
    //private int _id;
    private int id;
    private String name;
    private String brand;
    private String model;
    private String type;
    private double odo;
    private double rideTime;
    
    public Bike() {
    }
    //constructor
    public Bike(int id, String nombre, String marca, String modelo, String tipo, double odometro, double horometro) {
    this.id = id;
    this.name = nombre;
    this.brand = marca;
    this.model = modelo;
    this.type = tipo;
    this.odo = odometro;
    this.rideTime = horometro;
    }

    //getters
    public int getId() {return id;}
    public String getName() {return name;}
    public String getBrand(){return brand;}
    public String getModel(){return model;}
    public String getType() {return type;}
    public double getOdo()  {return odo;}
    public double getRideTime(){return rideTime;}    

    //setters
    public void setName(String nombre) {this.name = nombre;}
    public void setBrand(String marca) {this.brand = marca;}
    public void setModel(String modelo) {this.model = modelo;}
    public void setType(String tipo) {this.type = tipo;}
    public void setOdo(double odometro) {this.odo = odometro;}
    public void setRideTime (double horometro) {this.rideTime = horometro;}

    public String getColumnList(){
        return "name,brand,model,type,odo,ride_time";
    }
    public String toInsertValues(){
        return "'"+name+"','"+brand+"','"+model+"','"+type+"',"+Double.toString(odo)+","+Double.toString(rideTime);    
    }
    public String toUpdateValues(){
        return "name = '"+name+"', brand = '"+brand+"', model = '"+model+"', type = '"+type+"'";    
    }
    public String toString(){
        return Integer.toString(id)+","+name+","+brand+","+model+","+type+","+Double.toString(odo)+","+Double.toString(rideTime);    
    }
}
