import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorRMI {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("TP6/ServicioDistribuido", new ServicioDistribuidoImpl());
            System.out.println("Servidor RMI iniciado. Servicio publicado en TP6/ServicioDistribuido");
        } catch (Exception e) {
            System.err.println("Error en el servidor RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
