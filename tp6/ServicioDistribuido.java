import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServicioDistribuido extends Remote {

    Estadisticas procesarEstadisticas(List<Double> numeros) throws RemoteException;

    boolean validarIntegridad(String tipoDocumento, String numeroDocumento) throws RemoteException;

    List<String> filtrarContenido(List<String> mensajes, String patron) throws RemoteException;
}
