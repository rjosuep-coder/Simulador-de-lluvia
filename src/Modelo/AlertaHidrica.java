package Modelo;

import java.text.*;

/**
 * @author RJp
 */
public class AlertaHidrica {

    private int idAlerta;
    private int idRegistroOrigen;
    private String estacion;
    private String fechaHora;
    private double milimetros;
    private String nivel;
    private String recomendacion;

    public AlertaHidrica(int idAlerta, int idRegistroOrigen, String estacion, String fechaHora, double milimetros, String nivel, String recomendacion) {
        this.idAlerta = idAlerta;
        this.idRegistroOrigen = idRegistroOrigen;
        this.estacion = estacion;
        this.fechaHora = fechaHora;
        this.milimetros = milimetros;
        this.nivel = nivel;
        this.recomendacion = recomendacion;
    }

    public int getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(int idAlerta) {
        this.idAlerta = idAlerta;
    }

    public int getIdRegistroOrigen() {
        return idRegistroOrigen;
    }

    public void setIdRegistroOrigen(int idRegistroOrigen) {
        this.idRegistroOrigen = idRegistroOrigen;
    }

    public String getEstacion() {
        return estacion;
    }

    public void setEstacion(String estacion) {
        this.estacion = estacion;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public double getMilimetros() {
        return milimetros;
    }

    public void setMilimetros(double milimetros) {
        this.milimetros = milimetros;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    @Override
    public String toString() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", simbolos);
        return "Alerta #" + idAlerta + " [" + nivel + "] - " + estacion + " (" + fechaHora + "): " + df.format(milimetros) + " mm - " + recomendacion;
    }
}