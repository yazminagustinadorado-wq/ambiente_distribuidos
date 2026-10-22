import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorTCP {
    public static void main(String[] args) {
        int puerto = 5000;
        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("[SERVIDOR] Escuchando conexiones en el puerto " + puerto + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                // Atendemos a cada cliente en un hilo independiente
                new Thread(() -> manejarCliente(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void manejarCliente(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            while (true) {
                // Leemos el indicador de formato (1 = JSON, 2 = Binario)
                int tipoFormato = dis.read();
                if (tipoFormato == -1) break; 

                // Leemos el tamaño del payload (carga útil)
                int tamanoPayload = dis.readInt();
                byte[] payload = new byte[tamanoPayload];
                dis.readFully(payload);

                // Deserializamos según corresponda para verificar que el unmarshalling funcione
                if (tipoFormato == 1) {
                    String jsonStr = new String(payload);
                    Transaccion t = ParserMensajes.deserializarDeJson(jsonStr);
                } else if (tipoFormato == 2) {
                    Transaccion t = ParserMensajes.deserializarDeBinario(payload);
                }
            }
        } catch (IOException e) {
            // Conexión finalizada por el cliente
        }
    }
}