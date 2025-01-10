import java.time.LocalDate;

public class Trip {
    private LocalDate date;
    private int id_bike;
    private double trip_time;
    private double distance;
    private double max_speed;
    private double velocity;
    
    public Trip() {
    }
    //constructor
    public Trip(LocalDate fecha, int id_bike, double trip_time, double distancia, double rapidez, double velocidad) {
        this.date = fecha;
        this.id_bike = id_bike; 
        this.trip_time = trip_time;
        this.distance = distancia;
        this.max_speed = rapidez;
        this.velocity = velocidad; 
    }

    //getters
    public LocalDate  getDate () {return date; }
    public int getId_bike () {return id_bike; }
    public double getTrip_time () {return trip_time; }
    public double getDistance () {return distance; }
    public double getMaxSpeed () {return max_speed; }
    public double getVelocity () {return velocity; }
     
    //setters
    public void setDate (LocalDate date) {this.date = date;}
    public void setId_bike (int id_bike) {this.id_bike = id_bike; }
    public void setTrip_time (double trip_time) {this.trip_time = trip_time;}
    public void getDistance (double distance) {this.distance =distance;}
    public void getMaxSpeed (double max_speed) {this.max_speed = max_speed;}
    //public void getVelocity (double velocity) {this.velocity =velocity;}
    
    public String getColumnList(){
        return "date,id_bike,trip_time,distance,max_speed";
    }
    public String toUpdateValues(){
        return "date = '"+date+"', id_bike = "+Integer.toString(id_bike)+", trip_time = "+
        Double.toString(trip_time)+", distance = "+Double.toString(distance)+", max_speed = "+Double.toString(max_speed);
    }
    public String toInsertValues(){
        return "'"+date.toString()+"',"+Integer.toString(id_bike)+","+Double.toString(trip_time)+","+Double.toString(distance)+","+
            Double.toString(max_speed);
    }
    
    public String toString(){
        return date.toString()+","+Integer.toString(id_bike)+","+Double.toString(trip_time)+","+Double.toString(distance)+","+
            Double.toString(max_speed)+","+Double.toString(velocity);
    }
}

