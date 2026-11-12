import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ServicioDistribuidoImpl extends UnicastRemoteObject implements ServicioDistribuido {

    protected ServicioDistribuidoImpl() throws RemoteException {
        super();
    }

    @Override
    public Estadisticas procesarEstadisticas(List<Double> numeros) throws RemoteException {
        System.out.println("Servidor: ejecutando operacion procesarEstadisticas");
        if (numeros == null || numeros.size() == 0) {
            throw new RemoteException("Lista vacia");
        }
        double suma = 0;
        double maximo = Double.NEGATIVE_INFINITY;
        double minimo = Double.POSITIVE_INFINITY;
        for (Double numero : numeros) {
            double valor = numero;
            suma = suma + valor;
            if (valor > maximo) { maximo = valor; }
            if (valor < minimo) { minimo = valor; }
        }
        double promedio = suma / numeros.size();
        double varianza = 0;
        for (Double numero : numeros) {
            double valor = numero;
            double diferencia = valor - promedio;
            varianza = varianza + (diferencia * diferencia);
        }
        varianza = varianza / numeros.size();
        Estadisticas resultado = new Estadisticas(promedio, maximo, minimo, Math.sqrt(varianza));
        System.out.println("Servidor: operacion procesarEstadisticas finalizada");
        return resultado;
    }

    @Override
    public boolean validarIntegridad(String tipoDocumento, String numeroDocumento) throws RemoteException {
        System.out.println("Servidor: ejecutando operacion validarIntegridad");
        if (numeroDocumento == null || numeroDocumento.trim().length() == 0) { return false; }
        String valor = numeroDocumento.trim();
        String soloNumeros = valor.replace("-", "" );
        if (soloNumeros.length() == 0) { return false; }
        int longitudEsperada = 11;
        if (tipoDocumento != null && tipoDocumento.toUpperCase().contains("DNI")) { longitudEsperada = 7; }
        if (soloNumeros.length() != longitudEsperada) { return false; }
        for (int i = 0; i < soloNumeros.length(); i++) {
            char c = soloNumeros.charAt(i);
            if (c < 48 || c > 57) { return false; }
        }
        System.out.println("Servidor: operacion validarIntegridad finalizada");
        return true;
    }

    @Override
    public List<String> filtrarContenido(List<String> mensajes, String patron) throws RemoteException {
        System.out.println("Servidor: ejecutando operacion filtrarContenido");
        List<String> filtrados = new ArrayList<>();
        if (mensajes == null) { return filtrados; }
        for (String mensaje : mensajes) {
            if (mensaje != null && mensaje.contains(patron)) { filtrados.add(mensaje); }
        }
        System.out.println("Servidor: operacion filtrarContenido finalizada");
        return filtrados;
    }
}
