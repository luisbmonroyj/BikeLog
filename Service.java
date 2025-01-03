public class Service {
    private int id;
    private String name;
    
    public Service() {
    }
    //constructor
    public Service(int id, String nombre) {
        this.id = id;
        this.name = nombre;
    }
    //getters
    public int getId() {return id;}
    public String getName() {return name;}
    //setters
    public void setName(String nombre) {this.name = nombre;}
    
    public String getColumnList(){return "name";}
    public String toInsertValues(){return "'"+name+"'";}
    public String toUpdateValues(){return "name = '"+name+"'";}
    
    public String toString(){return Integer.toString(id)+","+name;}
}
