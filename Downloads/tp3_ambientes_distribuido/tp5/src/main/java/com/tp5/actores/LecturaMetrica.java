package com.tp5.actores;

// Mensaje inmutable: una vez creado, sus valores no cambian (cumple con las restricciones)
public final class LecturaMetrica {
    private final String idSensor;
    private final double valor;

    public LecturaMetrica(String idSensor, double valor) {
        this.idSensor = idSensor;
        this.valor = valor;
    }

    public String getIdSensor() {
        return idSensor;
    }

    public double getValor() {
        return valor;
    }
}