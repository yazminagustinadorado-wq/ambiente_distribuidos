import java.io.*;
import java.nio.charset.StandardCharsets;

public class ParserMensajes {

    // ==========================================
    // 1. FORMATO TEXTO (JSON)
    // ==========================================
    public static String serializarAJson(Transaccion t) {
        return "{" +
                "\"id\":" + t.getId() + "," +
                "\"origen\":\"" + t.getOrigen() + "\"," +
                "\"monto\":" + t.getMonto() + "," +
                "\"timestamp\":" + t.getTimestamp() +
                "}";
    }

    public static Transaccion deserializarDeJson(String json) {
        // Limpiamos los caracteres de estructura JSON básica
        json = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] partes = json.split(",");
        int id = 0;
        String origen = "";
        double monto = 0.0;
        long timestamp = 0;

        for (String parte : partes) {
            String[] kv = parte.split(":");
            String clave = kv[0].trim();
            String valor = kv[1].trim();

            switch (clave) {
                case "id": id = Integer.parseInt(valor); break;
                case "origen": origen = valor; break;
                case "monto": monto = Double.parseDouble(valor); break;
                case "timestamp": timestamp = Long.parseLong(valor); break;
            }
        }
        return new Transaccion(id, origen, monto, timestamp);
    }

    // ==========================================
    // 2. FORMATO BINARIO DIRECTO
    // ==========================================
    public static byte[] serializarABinario(Transaccion t) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Escribimos cada tipo de dato en crudo al flujo binario
        dos.writeInt(t.getId());                              // 4 bytes
        
        byte[] origenBytes = t.getOrigen().getBytes(StandardCharsets.UTF_8);
        dos.writeInt(origenBytes.length);                     // 4 bytes para medir longitud del texto
        dos.write(origenBytes);                               // N bytes del texto

        dos.writeDouble(t.getMonto());                        // 8 bytes
        dos.writeLong(t.getTimestamp());                      // 8 bytes

        dos.flush();
        return baos.toByteArray();
    }

    public static Transaccion deserializarDeBinario(byte[] datos) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(datos);
        DataInputStream dis = new DataInputStream(bais);

        int id = dis.readInt();
        
        int lenOrigen = dis.readInt();
        byte[] origenBytes = new byte[lenOrigen];
        dis.readFully(origenBytes);
        String origen = new String(origenBytes, StandardCharsets.UTF_8);

        double monto = dis.readDouble();
        long timestamp = dis.readLong();

        return new Transaccion(id, origen, monto, timestamp);
    }
}