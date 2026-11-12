import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClienteRMI {

    public static void main(String[] args) {
        try {
            ServicioDistribuido servicio = (ServicioDistribuido) Naming.lookup("rmi://localhost/TP6/ServicioDistribuido");
            menu(servicio);
        } catch (Exception e) {
            System.err.println("Error en el cliente RMI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void menu(ServicioDistribuido servicio) throws RemoteException {
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            System.out.println("1 - Procesar estadisticas");
            System.out.println("2 - Validar integridad de documento");
            System.out.println("3 - Filtrar contenido");
            System.out.println("4 - Salir");
            System.out.print("Ingrese opcion: ");
            String opcion = scanner.nextLine();
            if (opcion.equals("1")) {
                ejecutarEstadisticas(servicio, scanner);
            } else if (opcion.equals("2")) {
                ejecutarValidacion(servicio, scanner);
            } else if (opcion.equals("3")) {
                ejecutarFiltro(servicio, scanner);
            } else if (opcion.equals("4")) {
                salir = true;
            } else {
                System.out.println("Opcion no valida");
            }
        }
        scanner.close();
    }

    private static void ejecutarEstadisticas(ServicioDistribuido servicio, Scanner scanner) throws RemoteException {
        System.out.print("Ingrese los numeros separados por coma: ");
        String entrada = scanner.nextLine();
        List<Double> numeros = new ArrayList<>();
        if (!entrada.trim().isEmpty()) {
            for (String parte : entrada.split(",")) {
                if (!parte.trim().isEmpty()) {
                    numeros.add(Double.parseDouble(parte.trim()));
                }
            }
        }
        Estadisticas resultado = servicio.procesarEstadisticas(numeros);
        System.out.println("Estadisticas - Promedio: " + resultado.getPromedio() + " Maximo: " + resultado.getMaximo() + " Minimo: " + resultado.getMinimo() + " Desviacion Estandar: " + resultado.getDesviacionEstandar());
    }

    private static void ejecutarValidacion(ServicioDistribuido servicio, Scanner scanner) throws RemoteException {
        System.out.print("Ingrese tipo de documento (DNI/CUIT): ");
        String tipo = scanner.nextLine();
        System.out.print("Ingrese numero de documento: ");
        String numero = scanner.nextLine();
        boolean valido = servicio.validarIntegridad(tipo, numero);
        System.out.println("Documento valido: " + valido);
    }

    private static void ejecutarFiltro(ServicioDistribuido servicio, Scanner scanner) throws RemoteException {
        System.out.print("Ingrese los mensajes separados por coma: ");
        String entrada = scanner.nextLine();
        System.out.print("Ingrese patron de busqueda: ");
        String patron = scanner.nextLine();
        List<String> filtrados = servicio.filtrarContenido(parseLista(entrada), patron);
        for (String f : filtrados) {
            System.out.println("- " + f);
        }
    }

    private static List<String> parseLista(String entrada) {
        List<String> lista = new ArrayList<>();
        if (entrada == null) { return lista; }
        for (String parte : entrada.split(",")) {
            if (!parte.trim().isEmpty()) { lista.add(parte.trim()); }
        }
        return lista;
    }
}
