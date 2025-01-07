import java.text.DecimalFormat;
import java.time.LocalDate;

public class Maintenance {
    private LocalDate  date;
    private int id_bike;
    private double odo;
    private int id_service;
    private String brand;
    private String reference;
    private double price;
    private String description;
    private String duration;
    
    public DecimalFormat df = new DecimalFormat("#.###");

    public Maintenance() {
    }
    //constructor
    public Maintenance(LocalDate fecha, int id_bike, double odometro, int id_service, String marca, String referencia, double precio, String descripcion, String duracion) {
        this.date = fecha;
        this.id_bike = id_bike; 
        this.odo = odometro;
        this.id_service = id_service;
        this.brand = marca;
        this.reference = referencia;
        this.price = precio;
        this.description = descripcion;
        this.duration = duracion;
    }

    //getters
    public LocalDate  getDate () {return date; }
    public int getId_bike () {return id_bike; }
    public double getOdo () {return odo; }
    public int getId_service () {return id_service; }
    public String getBrand () {return brand; }
    public String getReference () {return reference; }
    public double getPrice () {return price; }
    public String getDescription () {return description; }
    public String getDuration () {return duration; }
     
    //setters
    public void setDate (LocalDate date) {this.date = date;}
    public void setId_bike (int id_bike) {this.id_bike = id_bike; }
    public void setOdo (double odo) {this.odo = odo;}
    public void setId_service (int id_service) {this.id_service = id_service;}
    public void setBrand (String brand) {this.brand = brand;}
    public void setReference (String reference) {this.reference = reference;}
    public void setPrice (double price) {this.price =price;}
    public void setDescription (String description) {this.description = description;}
    public void setDuration (String duration) {this.duration = duration;}
    
    public String getColumnList(){
        return "date,id_bike,odo,id_service,brand,reference,price,description,duration";
    }
    public String toUpdateValues(double startingOdo, double startingTime){
        return "date = '"+date+"', id_bike = "+Integer.toString(id_bike)+", odo = "+df.format(odo)+", id_service = "+Integer.toString(id_service)+
            ", brand = '"+brand+"', reference = '"+reference+"', price = "+df.format(price)+", description = '"+description+"', duration = '"+duration+"'";    
    }
    //works for insert
    public String toString(){
        return "'"+date.toString()+"',"+Integer.toString(id_bike)+","+df.format(odo)+","+Integer.toString(id_service)+",'"+
        brand+"','"+reference+"',"+df.format(price)+",'"+description+"','"+duration+"'";
    }
}
