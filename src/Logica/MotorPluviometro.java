package Logica;

import Estructuras.*;
import Modelo.*;
import java.io.*;
import java.text.*;
import java.time.*;
import java.time.format.*;

/**
 * @author RJp
 */
public class MotorPluviometro {

    private Lista<RegistroLluvia> bitacoraCronologica;
    private Pila<RegistroLluvia> pilaAuditoriaDeshacer;
    private Cola<AlertaHidrica> colaAlertasPendientes;
    private ArbolBinarioBusqueda<RegistroLluvia> arbolPorIntensidad;
    private double umbralCriticoMm;
    private int contadorRegistros;
    private int contadorAlertas;

    public MotorPluviometro() {
        this.bitacoraCronologica = new Lista<>();
        this.pilaAuditoriaDeshacer = new Pila<>();
        this.colaAlertasPendientes = new Cola<>();
        this.arbolPorIntensidad = new ArbolBinarioBusqueda<>();
        this.umbralCriticoMm = 15.0;
        this.contadorRegistros = 0;
        this.contadorAlertas = 0;
        cargarDatosAutomatico();
    }

    public RegistroLluvia registrarMedicion(String estacion, String fechaHora, double milimetros) {
        double mmRedondeado = Math.round(milimetros * 100.0) / 100.0;
        if (mmRedondeado < 0.0) {
            mmRedondeado = 0.0;
        }
        contadorRegistros++;
        RegistroLluvia registro = new RegistroLluvia(contadorRegistros, estacion, fechaHora, mmRedondeado);
        bitacoraCronologica.agregarFinal(registro);
        pilaAuditoriaDeshacer.apilar(registro);
        arbolPorIntensidad.insertar(registro);
        evaluarYEncolarAlerta(registro);
        guardarDatosAutomatico();
        return registro;
    }

    public boolean editarMedicion(int idRegistro, double nuevosMm) {
        double mmRedondeado = Math.round(nuevosMm * 100.0) / 100.0;
        if (mmRedondeado < 0.0) {
            mmRedondeado = 0.0;
        }
        RegistroLluvia objetivo = null;
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && reg.getId() == idRegistro) {
                objetivo = reg;
                break;
            }
            actual = actual.getSiguiente();
        }
        if (objetivo == null) {
            return false;
        }
        objetivo.setMilimetros(mmRedondeado);
        reconstruirArbolPorIntensidad();
        evaluarYEncolarAlerta(objetivo);
        guardarDatosAutomatico();
        return true;
    }

    private void evaluarYEncolarAlerta(RegistroLluvia registro) {
        double mm = registro.getMilimetros();
        if (mm >= umbralCriticoMm) {
            contadorAlertas++;
            String nivel;
            String recomendacion;
            if (mm >= umbralCriticoMm * 2.0) {
                nivel = "Roja";
                recomendacion = "Evacuación preventiva y activación inmediata de drenajes de emergencia.";
            } else if (mm >= umbralCriticoMm * 1.5) {
                nivel = "Naranja";
                recomendacion = "Alerta operativa: restringir tránsito en zonas bajas y vigilar quebradas.";
            } else {
                nivel = "Amarilla";
                recomendacion = "Precaución: monitoreo continuo de alcantarillado y cauces cercanos.";
            }
            AlertaHidrica alerta = new AlertaHidrica(
                    contadorAlertas,
                    registro.getId(),
                    registro.getEstacion(),
                    registro.getFechaHora(),
                    mm,
                    nivel,
                    recomendacion
            );
            colaAlertasPendientes.encolar(alerta);
        }
    }

    private void reconstruirArbolPorIntensidad() {
        arbolPorIntensidad.limpiar();
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            arbolPorIntensidad.insertar(actual.getDato());
            actual = actual.getSiguiente();
        }
    }

    public void simularLecturasAleatorias(int cantidad, String[] ciudadesDisponibles) {
        if (cantidad <= 0) {
            return;
        }
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime fechaBase = LocalDateTime.of(2026, 11, 15, 15, 0);
        if (!bitacoraCronologica.estaVacia()) {
            Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            try {
                fechaBase = LocalDateTime.parse(actual.getDato().getFechaHora().trim(), formato).plusHours(1);
            } catch (Exception ex) {
                fechaBase = fechaBase.plusHours(contadorRegistros);
            }
        }
        for (int i = 0; i < cantidad; i++) {
            String ciudadElegida = "Piura";
            if (ciudadesDisponibles != null && ciudadesDisponibles.length > 0) {
                int idx = (int) (Math.random() * ciudadesDisponibles.length);
                if (idx >= ciudadesDisponibles.length) {
                    idx = ciudadesDisponibles.length - 1;
                }
                String candidata = ciudadesDisponibles[idx];
                if (candidata != null && !candidata.trim().isEmpty()) {
                    ciudadElegida = candidata;
                }
            }
            String fechaTexto = fechaBase.plusHours(i).format(formato);
            double r = Math.random();
            double mm;
            if (r < 0.15) {
                mm = 0.0;
            } else if (r < 0.50) {
                mm = 0.2 + Math.random() * 7.4;
            } else if (r < 0.80) {
                mm = 7.6 + Math.random() * 14.4;
            } else {
                mm = 22.0 + Math.random() * 23.0;
            }
            mm = Math.round(mm * 100.0) / 100.0;
            registrarMedicion(ciudadElegida, fechaTexto, mm);
        }
    }

    public RegistroLluvia deshacerUltimaMedicion() {
        if (pilaAuditoriaDeshacer.estaVacia()) {
            return null;
        }
        RegistroLluvia eliminado = pilaAuditoriaDeshacer.desapilar();
        bitacoraCronologica.eliminarPorDato(eliminado);
        reconstruirArbolPorIntensidad();
        guardarDatosAutomatico();
        return eliminado;
    }

    public AlertaHidrica atenderSiguienteAlerta() {
        AlertaHidrica atendida = colaAlertasPendientes.desencolar();
        guardarDatosAutomatico();
        return atendida;
    }

    public void guardarDatosAutomatico() {
        try (PrintWriter pw = new PrintWriter("datos_pluviometro.txt")) {
            pw.println("CFG|" + umbralCriticoMm + "|" + contadorRegistros + "|" + contadorAlertas);

            Nodo<RegistroLluvia> actualReg = bitacoraCronologica.getCabeza();
            while (actualReg != null) {
                RegistroLluvia reg = actualReg.getDato();
                if (reg != null) {
                    pw.println("REG|" + reg.getId()
                            + "|" + limpiarCampoArchivo(reg.getEstacion())
                            + "|" + limpiarCampoArchivo(reg.getFechaHora())
                            + "|" + reg.getMilimetros());
                }
                actualReg = actualReg.getSiguiente();
            }

            Nodo<AlertaHidrica> actualAlerta = colaAlertasPendientes.getFrente();
            while (actualAlerta != null) {
                AlertaHidrica a = actualAlerta.getDato();
                if (a != null) {
                    pw.println("ALE|" + a.getIdAlerta()
                            + "|" + a.getIdRegistroOrigen()
                            + "|" + limpiarCampoArchivo(a.getEstacion())
                            + "|" + limpiarCampoArchivo(a.getFechaHora())
                            + "|" + a.getMilimetros()
                            + "|" + limpiarCampoArchivo(a.getNivel())
                            + "|" + limpiarCampoArchivo(a.getRecomendacion()));
                }
                actualAlerta = actualAlerta.getSiguiente();
            }
        } catch (IOException ex) {
        }
    }

    public void cargarDatosAutomatico() {
        bitacoraCronologica.limpiar();
        pilaAuditoriaDeshacer.limpiar();
        colaAlertasPendientes.limpiar();
        arbolPorIntensidad.limpiar();
        contadorRegistros = 0;
        contadorAlertas = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("datos_pluviometro.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] partes = linea.split("\\|");
                if (partes.length == 0) {
                    continue;
                }
                try {
                    if ("CFG".equals(partes[0]) && partes.length >= 4) {
                        double u = Double.parseDouble(partes[1]);
                        if (u > 0.0) {
                            umbralCriticoMm = u;
                        }
                        contadorRegistros = Integer.parseInt(partes[2]);
                        contadorAlertas = Integer.parseInt(partes[3]);
                    } else if ("REG".equals(partes[0]) && partes.length >= 5) {
                        int id = Integer.parseInt(partes[1]);
                        String estacion = partes[2];
                        String fechaHora = partes[3];
                        double mm = Double.parseDouble(partes[4]);
                        RegistroLluvia reg = new RegistroLluvia(id, estacion, fechaHora, mm);
                        bitacoraCronologica.agregarFinal(reg);
                        pilaAuditoriaDeshacer.apilar(reg);
                        arbolPorIntensidad.insertar(reg);
                        if (id > contadorRegistros) {
                            contadorRegistros = id;
                        }
                    } else if ("ALE".equals(partes[0]) && partes.length >= 8) {
                        int idAlerta = Integer.parseInt(partes[1]);
                        int idReg = Integer.parseInt(partes[2]);
                        String estacion = partes[3];
                        String fechaHora = partes[4];
                        double mm = Double.parseDouble(partes[5]);
                        String nivel = partes[6];
                        String recomendacion = partes[7];
                        AlertaHidrica alerta = new AlertaHidrica(idAlerta, idReg, estacion, fechaHora, mm, nivel, recomendacion);
                        colaAlertasPendientes.encolar(alerta);
                        if (idAlerta > contadorAlertas) {
                            contadorAlertas = idAlerta;
                        }
                    }
                } catch (Exception exLinea) {
                }
            }
        } catch (IOException ex) {
        }
    }

    public void limpiarTodosLosRegistros() {
        bitacoraCronologica.limpiar();
        pilaAuditoriaDeshacer.limpiar();
        colaAlertasPendientes.limpiar();
        arbolPorIntensidad.limpiar();
        contadorRegistros = 0;
        contadorAlertas = 0;
        try (PrintWriter pw = new PrintWriter("datos_pluviometro.txt")) {
        } catch (IOException ex) {
        }
    }

    private String limpiarCampoArchivo(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("|", "-").replace("\r", " ").replace("\n", " ");
    }

    public Lista<RegistroLluvia> consultarPorRangoMm(double minMm, double maxMm) {
        Lista<RegistroLluvia> resultado = new Lista<>();
        if (minMm > maxMm || maxMm < 0.0) {
            return resultado;
        }
        RegistroLluvia minBase = new RegistroLluvia(Integer.MIN_VALUE, "", "", minMm);
        RegistroLluvia maxBase = new RegistroLluvia(Integer.MAX_VALUE, "", "", maxMm);
        arbolPorIntensidad.buscarPorRango(minBase, maxBase, resultado);
        return resultado;
    }

    public Lista<RegistroLluvia> filtrarPorEstacion(String estacion) {
        Lista<RegistroLluvia> resultado = new Lista<>();
        if (estacion == null) {
            return resultado;
        }
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && estacion.equalsIgnoreCase(reg.getEstacion())) {
                resultado.agregarFinal(reg);
            }
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    public Lista<RegistroLluvia> filtrarPorIntensidad(String intensidad) {
        Lista<RegistroLluvia> resultado = new Lista<>();
        if (intensidad == null) {
            return resultado;
        }
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && intensidad.equalsIgnoreCase(reg.getIntensidad())) {
                resultado.agregarFinal(reg);
            }
            actual = actual.getSiguiente();
        }
        return resultado;
    }

    public Lista<RegistroLluvia> obtenerOrderingPorIntensidad() {
        Lista<RegistroLluvia> resultado = new Lista<>();
        arbolPorIntensidad.recorridoInOrden(resultado);
        return resultado;
    }

    public double calcularTotalAcumuladoMm() {
        double suma = 0.0;
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            suma += actual.getDato().getMilimetros();
            actual = actual.getSiguiente();
        }
        return Math.round(suma * 100.0) / 100.0;
    }

    public double calcularPromedioMm() {
        if (bitacoraCronologica.estaVacia()) {
            return 0.0;
        }
        double promedio = calcularTotalAcumuladoMm() / bitacoraCronologica.getTamano();
        return Math.round(promedio * 100.0) / 100.0;
    }

    public RegistroLluvia obtenerPicoMaximo() {
        return arbolPorIntensidad.obtenerMaximo();
    }

    public RegistroLluvia obtenerPicoMinimo() {
        return arbolPorIntensidad.obtenerMinimo();
    }

    public double calcularAcumuladoPorEstacion(String estacion) {
        if (estacion == null) {
            return 0.0;
        }
        double suma = 0.0;
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && estacion.equalsIgnoreCase(reg.getEstacion())) {
                suma += reg.getMilimetros();
            }
            actual = actual.getSiguiente();
        }
        return Math.round(suma * 100.0) / 100.0;
    }

    public double calcularAcumuladoPorCiudad(String ciudad) {
        return calcularAcumuladoPorEstacion(ciudad);
    }

    public double calcularAcumuladoPorCiudadYFecha(String ciudad, String fechaPrefix) {
        if (ciudad == null || fechaPrefix == null) {
            return 0.0;
        }
        String prefijo = fechaPrefix.trim();
        if (prefijo.isEmpty()) {
            return 0.0;
        }
        if (prefijo.length() > 10) {
            prefijo = prefijo.substring(0, 10);
        }
        double suma = 0.0;
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && ciudad.equalsIgnoreCase(reg.getEstacion())) {
                String fh = reg.getFechaHora() != null ? reg.getFechaHora().trim() : "";
                String fechaReg = fh.length() >= 10 ? fh.substring(0, 10) : fh;
                if (prefijo.length() == 10 ? fechaReg.equals(prefijo) : fechaReg.startsWith(prefijo)) {
                    suma += reg.getMilimetros();
                }
            }
            actual = actual.getSiguiente();
        }
        return Math.round(suma * 100.0) / 100.0;
    }

    public RegistroLluvia obtenerUltimaMedicionPorEstacion(String estacion) {
        if (estacion == null) {
            return null;
        }
        RegistroLluvia ultimo = null;
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && estacion.equalsIgnoreCase(reg.getEstacion())) {
                ultimo = reg;
            }
            actual = actual.getSiguiente();
        }
        return ultimo;
    }

    public RegistroLluvia obtenerUltimaLecturaCiudad(String ciudad) {
        return obtenerUltimaMedicionPorEstacion(ciudad);
    }

    public RegistroLluvia obtenerPicoMaximoPorEstacion(String estacion) {
        if (estacion == null) {
            return null;
        }
        RegistroLluvia maximo = null;
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && estacion.equalsIgnoreCase(reg.getEstacion())) {
                if (maximo == null || reg.getMilimetros() > maximo.getMilimetros()) {
                    maximo = reg;
                }
            }
            actual = actual.getSiguiente();
        }
        return maximo;
    }

    public int contarMedicionesPorEstacion(String estacion) {
        if (estacion == null) {
            return 0;
        }
        int contador = 0;
        Nodo<RegistroLluvia> actual = bitacoraCronologica.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && estacion.equalsIgnoreCase(reg.getEstacion())) {
                contador++;
            }
            actual = actual.getSiguiente();
        }
        return contador;
    }

    public void exportarReporteCiudadanoTxt(File archivo) throws IOException {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", simbolos);

        String[] regiones = {"Región Costa", "Región Valle Central", "Región Alto Piura"};
        String[][] ciudadesPorRegion = {
            {"Paita", "Sechura"},
            {"Piura - Miraflores", "Castilla - Corpac", "Catacaos"},
            {"Tambogrande", "Chulucanas"}
        };

        try (PrintWriter pw = new PrintWriter(archivo, "UTF-8")) {
            pw.println("==========================================================================");
            pw.println("        REPORTE METEOROLÓGICO CIUDADANO - MONITOREO REGIONAL PIURA        ");
            pw.println("==========================================================================");
            pw.println("Umbral de alerta configurado : " + df.format(umbralCriticoMm) + " mm");
            pw.println("Total de mediciones registradas : " + bitacoraCronologica.getTamano());
            pw.println("Acumulado regional total     : " + df.format(calcularTotalAcumuladoMm()) + " mm");
            pw.println("Alertas activas pendientes   : " + colaAlertasPendientes.getTamano());
            pw.println();
            pw.println("--------------------------------------------------------------------------");
            pw.println("RESUMEN POR REGIÓN Y CIUDAD");
            pw.println("--------------------------------------------------------------------------");

            for (int r = 0; r < regiones.length; r++) {
                pw.println();
                pw.println("[" + regiones[r] + "]");
                for (int c = 0; c < ciudadesPorRegion[r].length; c++) {
                    String ciudad = ciudadesPorRegion[r][c];
                    double acumulado = calcularAcumuladoPorCiudad(ciudad);
                    RegistroLluvia ultima = obtenerUltimaLecturaCiudad(ciudad);
                    double mmActual = ultima != null ? ultima.getMilimetros() : 0.0;
                    String riesgo = clasificarRiesgoCiudadano(mmActual);
                    String recomendacion = obtenerRecomendacionCiudadana(mmActual);
                    String fechaUltima = ultima != null ? ultima.getFechaHora() : "Sin registros";
                    pw.println("  - " + ciudad
                            + " | Última lectura: " + df.format(mmActual) + " mm (" + fechaUltima + ")"
                            + " | Acumulado: " + df.format(acumulado) + " mm"
                            + " | Riesgo: " + riesgo);
                    pw.println("    Recomendación: " + recomendacion);
                }
            }

            pw.println();
            pw.println("--------------------------------------------------------------------------");
            pw.println("ALERTAS ACTIVAS PENDIENTES DE ATENCIÓN");
            pw.println("--------------------------------------------------------------------------");
            if (colaAlertasPendientes.estaVacia()) {
                pw.println("  No existen alertas pendientes.");
            } else {
                Nodo<AlertaHidrica> actualAlerta = colaAlertasPendientes.getFrente();
                while (actualAlerta != null) {
                    AlertaHidrica a = actualAlerta.getDato();
                    pw.println("  * [" + a.getNivel() + "] " + a.getEstacion()
                            + " (" + a.getFechaHora() + ") - " + df.format(a.getMilimetros()) + " mm"
                            + " -> " + a.getRecomendacion());
                    actualAlerta = actualAlerta.getSiguiente();
                }
            }

            pw.println();
            pw.println("--------------------------------------------------------------------------");
            pw.println("HISTORIAL COMPLETO DE REPORTES");
            pw.println("--------------------------------------------------------------------------");
            if (bitacoraCronologica.estaVacia()) {
                pw.println("  Sin reportes registrados.");
            } else {
                Nodo<RegistroLluvia> actualReg = bitacoraCronologica.getCabeza();
                while (actualReg != null) {
                    RegistroLluvia reg = actualReg.getDato();
                    pw.println("  " + reg.getFechaHora() + " | " + reg.getEstacion()
                            + " | " + df.format(reg.getMilimetros()) + " mm"
                            + " | " + clasificarRiesgoCiudadano(reg.getMilimetros()));
                    actualReg = actualReg.getSiguiente();
                }
            }
            pw.println("==========================================================================");
        }
    }

    private String clasificarRiesgoCiudadano(double mm) {
        if (mm <= 0.0) {
            return "Sin lluvia";
        } else if (mm < 2.5) {
            return "Llovizna";
        } else if (mm < 7.6) {
            return "Lluvia moderada";
        } else if (mm < 30.0) {
            return "Lluvia fuerte";
        } else {
            return "Alerta extrema";
        }
    }

    private String obtenerRecomendacionCiudadana(double mm) {
        if (mm <= 0.0) {
            return "Condiciones normales.";
        } else if (mm < 2.5) {
            return "Tránsito normal.";
        } else if (mm < 7.6) {
            return "Precaución al conducir; revisar drenajes.";
        } else if (mm < 30.0) {
            return "Riesgo de aniegos en zonas bajas; proteger techos.";
        } else {
            return "Riesgo alto de inundación; evitar desplazamientos.";
        }
    }

    public Lista<RegistroLluvia> getBitacoraCronologica() {
        return bitacoraCronologica;
    }

    public Pila<RegistroLluvia> getPilaAuditoriaDeshacer() {
        return pilaAuditoriaDeshacer;
    }

    public Cola<AlertaHidrica> getColaAlertasPendientes() {
        return colaAlertasPendientes;
    }

    public ArbolBinarioBusqueda<RegistroLluvia> getArbolPorIntensidad() {
        return arbolPorIntensidad;
    }

    public double getUmbralCriticoMm() {
        return umbralCriticoMm;
    }

    public void setUmbralCriticoMm(double umbralCriticoMm) {
        if (umbralCriticoMm > 0.0) {
            this.umbralCriticoMm = umbralCriticoMm;
        }
    }
}