package com.tp5.actores;

import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int totalMensajes = 500;
        CountDownLatch latch = new CountDownLatch(totalMensajes);

        // 1. CREAR (Spawn): Instanciar el Actor Procesador tipado correctamente con LecturaMetrica
        ActorSystem<LecturaMetrica> sistemaProcesador = ActorSystem.create(
            ActorProcesador.create(latch), 
            "SistemaProcesador"
        );

        System.out.println("=== INICIANDO RÁFAGA DE 500 MENSAJES CONCURRENTES ===");

        // 2. ENVIAR (Send): Lanzamos 5 hilos (sensores) que envían 100 mensajes cada uno
        for (int s = 1; s <= 5; s++) {
            final String idSensor = "Sensor_" + s;
            final ActorRef<LecturaMetrica> procesadorRef = sistemaProcesador;
            
            new Thread(() -> {
                Random rand = new Random();
                for (int i = 0; i < 100; i++) {
                    double valorLectura = 10.0 + (40.0 - 10.0) * rand.nextDouble();
                    LecturaMetrica mensaje = new LecturaMetrica(idSensor, valorLectura);
                    
                    // Envío asincrónico al buzón del procesador
                    procesadorRef.tell(mensaje);
                }
            }).start();
        }

        // El hilo principal espera bloqueado hasta que el latch procese los 500 mensajes exactos
        latch.await();

        System.out.println("=== PROCESAMIENTO FINALIZADO DE LOS 500 MENSAJES ===");

        // Cerrar el sistema de actores de manera segura
        sistemaProcesador.terminate();
    }
}