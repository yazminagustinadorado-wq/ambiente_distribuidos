package com.tp5.actores;

import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import java.util.concurrent.CountDownLatch;

public class ActorProcesador extends AbstractBehavior<LecturaMetrica> {

    private double sumaTotal = 0.0;
    private int cantidadMensajes = 0;
    private final CountDownLatch latch;

    public static Behavior<LecturaMetrica> create(CountDownLatch latch) {
        return Behaviors.setup(context -> new ActorProcesador(context, latch));
    }

    private ActorProcesador(ActorContext<LecturaMetrica> context, CountDownLatch latch) {
        super(context);
        this.latch = latch;
    }

    @Override
    public Receive<LecturaMetrica> createReceive() {
        return newReceiveBuilder()
                .onMessage(LecturaMetrica.class, this::onLectura)
                .build();
    }

    private Behavior<LecturaMetrica> onLectura(LecturaMetrica mensaje) {
        this.cantidadMensajes++;
        this.sumaTotal += mensaje.getValor();
        double promedio = this.sumaTotal / this.cantidadMensajes;

        System.out.println(String.format("[Mailbox FIFO] Mensaje #%d | Sensor: %s | Valor: %.2f | Promedio Histórico: %.2f",
                cantidadMensajes, mensaje.getIdSensor(), mensaje.getValor(), promedio));

        // Notificamos al latch que este mensaje ya fue procesado
        latch.countDown();

        return this;
    }
}