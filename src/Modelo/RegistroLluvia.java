package Modelo;

import java.text.*;

/**
 * @author RJp
 */
public class RegistroLluvia implements Comparable<RegistroLluvia> {

    private int id;
    private String estacion;
    private String fechaHora;
    private double milimetros;
    private String intensidad;

    public RegistroLluvia(int id, String estacion, String fechaHora, double milimetros) {
        this.id = id;
        this.estacion = estacion;
        this.fechaHora = fechaHora;
        this.milimetros = milimetros < 0.0 ? 0.0 : milimetros;
        this.intensidad = clasificarIntensidad(this.milimetros);
    }

    private String clasificarIntensidad(double mm) {
        if (mm <= 0.0) {
            return "Sin precipitación";
        } else if (mm < 2.5) {
            return "Lluvia débil";
        } else if (mm < 7.6) {
            return "Lluvia moderada";
        } else if (mm < 30.0) {
            return "Lluvia fuerte";
        } else {
            return "Lluvia torrencial";
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        this.milimetros = milimetros < 0.0 ? 0.0 : milimetros;
        this.intensidad = clasificarIntensidad(this.milimetros);
    }

    public String getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(String intensidad) {
        this.intensidad = intensidad;
    }

    @Override
    public int compareTo(RegistroLluvia otro) {
        int compMm = Double.compare(this.milimetros, otro.milimetros);
        if (compMm != 0) {
            return compMm;
        }
        return Integer.compare(this.id, otro.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RegistroLluvia otro = (RegistroLluvia) obj;
        return this.id == otro.id;
    }

    @Override
    public String toString() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", simbolos);
        return "Registro #" + id + " [" + fechaHora + "] " + estacion + " - " + df.format(milimetros) + " mm (" + intensidad + ")";
    }
}