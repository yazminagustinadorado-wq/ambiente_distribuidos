import java.io.Serializable;

public class Transaccion implements Serializable {
    private int id;                 // Entero de 32 bits
    private String origen;          // Cadena de texto
    private double monto;           // Número flotante de doble precisión
    private long timestamp;         // Entero de 64 bits (long)

    // Constructor para inicializar los datos
    public Transaccion(int id, String origen, double monto, long timestamp) {
        this.id = id;
        this.origen = origen;
        this.monto = monto;
        this.timestamp = timestamp;
    }

    // Métodos Getters y Setters para acceder a los atributos
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}