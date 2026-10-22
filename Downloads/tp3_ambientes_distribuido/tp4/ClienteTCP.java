import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClienteTCP {
    public static void main(String[] args) {
        String host = "127.0.0.1"; // Localhost
        int puerto = 5000;
        int totalTransacciones = 1000;

        // Generamos un arreglo de 1000 transacciones simuladas previas
        Transaccion[] transacciones = new Transaccion[totalTransacciones];
        for (int i = 0; i < totalTransacciones; i++) {
            transacciones[i] = new Transaccion(
                100 + i, 
                "Nodo-" + (i % 10), 
                1500.50 + i, 
                System.currentTimeMillis()
            );
        }

        System.out.println("[CLIENTE] Iniciando envío de ráfaga de " + totalTransacciones + " transacciones...");

        // ==========================================
        // PRUEBA 1: FORMATO JSON (TEXTO)
        // ==========================================
        long bytesTotalJson = 0;
        long inicioJson = System.nanoTime();

        try (Socket socket = new Socket(host, puerto);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            
            for (int i = 0; i < totalTransacciones; i++) {
                String jsonStr = ParserMensajes.serializarAJson(transacciones[i]);
                byte[] payload = jsonStr.getBytes(StandardCharsets.UTF_8);
                
                bytesTotalJson += payload.length;

                // Enviamos: Tipo (1 = JSON) + Tamaño del payload + Payload en bytes
                dos.write(1);
                dos.writeInt(payload.length);
                dos.write(payload);
            }
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long finJson = System.nanoTime();
        long tiempoTotalJsonMs = (finJson - inicioJson) / 1_000_000;


        // ==========================================
        // PRUEBA 2: FORMATO BINARIO DIRECTO
        // ==========================================
        long bytesTotalBinario = 0;
        long inicioBinario = System.nanoTime();

        try (Socket socket = new Socket(host, puerto);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            
            for (int i = 0; i < totalTransacciones; i++) {
                byte[] payload = ParserMensajes.serializarABinario(transacciones[i]);
                
                bytesTotalBinario += payload.length;

                // Enviamos: Tipo (2 = Binario) + Tamaño del payload + Payload en bytes
                dos.write(2);
                dos.writeInt(payload.length);
                dos.write(payload);
            }
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long finBinario = System.nanoTime();
        long tiempoTotalBinarioMs = (finBinario - inicioBinario) / 1_000_000;


        // ==========================================
        // IMPRESIÓN DE RESULTADOS SOLICITADOS
        // ==========================================
        System.out.println("\n==================================================");
        System.out.println("          RESULTADOS DE DESEMPEÑO (OVERHEAD)      ");
        System.out.println("==================================================");
        System.out.println("1. FORMATO JSON (Texto):");
        System.out.println("   - Tamaño total de carga útil: " + bytesTotalJson + " bytes");
        System.out.println("   - Tiempo total (serialización + envío): " + tiempoTotalJsonMs + " ms");
        
        System.out.println("\n2. FORMATO BINARIO DIRECTO:");
        System.out.println("   - Tamaño total de carga útil: " + bytesTotalBinario + " bytes");
        System.out.println("   - Tiempo total (serialización + envío): " + tiempoTotalBinarioMs + " ms");
        
        double reduccion = ((double)(bytesTotalJson - bytesTotalBinario) / bytesTotalJson) * 100;
        System.out.printf("\n-> Conclusión: El formato binario ocupó un %.2f%% menos de espacio en red que el JSON.\n", reduccion);
        System.out.println("==================================================");
    }
}