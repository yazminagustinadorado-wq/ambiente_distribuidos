import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteCalculadora {
    private static final String HOST = "localhost";
    private static final int PUERTO = 5500;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Cliente Calculadora ===");
        
        // Solicitar datos por consola
        System.out.print("Ingrese el primer numero entero: ");
        int num1 = scanner.nextInt();

        System.out.print("Ingrese la operacion (+, -, *, /): ");
        String operacion = scanner.next();

        System.out.print("Ingrese el segundo numero entero: ");
        int num2 = scanner.nextInt();

        // Empaquetar datos con el delimitador ";"
        String trama = num1 + ";" + operacion + ";" + num2;

        System.out.println("\nConectando al servidor " + HOST + ":" + PUERTO + "...");

        try (Socket socket = new Socket(HOST, PUERTO);
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Enviar trama al servidor
            salida.println(trama);
            System.out.println("Trama enviada: " + trama);

            // Recibir e imprimir respuesta
            String respuesta = entrada.readLine();
            System.out.println("\n[RESPUESTA DEL SERVIDOR]: " + respuesta);

        } catch (IOException e) {
            System.err.println("Error de conexion: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}