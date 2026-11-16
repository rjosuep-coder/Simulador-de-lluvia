package Vista;

import Estructuras.*;
import Logica.*;
import Modelo.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

/**
 * @author RJp
 */
public class FrmPluviometro extends JFrame {

    private static final String[] DISTRITOS_PIURA = {
        "Piura", "Castilla", "Veintiséis de Octubre", "Catacaos", "Tambogrande",
        "Las Lomas", "La Unión", "La Arena", "Cura Mori", "El Tallán",
        "Paita", "Colán", "La Huaca", "Vichayal", "Amotape", "Tamarindo", "Arenal",
        "Pariñas (Talara)", "Máncora", "Los Órganos", "El Alto", "Lobitos", "La Brea",
        "Sullana", "Bellavista", "Marcavelica", "Querecotillo", "Lancones", "Ignacio Escudero", "Miguel Checa", "Salitral",
        "Chulucanas", "Morropón", "La Matanza", "Buenos Aires", "Salitral (Morropón)", "Santo Domingo", "Chalaco", "Yamango", "San Juan de Bigote", "Santa Catalina de Mossa",
        "Huancabamba", "Canchaque", "Huarmaca", "El Carmen de la Frontera", "Sondorillo", "Sóndor", "San Miguel de El Faique", "Lalaquiz",
        "Sechura", "Vice", "Bernal", "Cristo Nos Valga", "Bellavista de la Unión", "Rinconada Llicuar",
        "Ayabaca", "Frías", "Sapillica", "Paimas", "Suyo", "Montero", "Pacaipampa", "Lagunas", "Jililí", "Sicchez"
    };

    private MotorPluviometro motor;
    private DecimalFormat formatoDecimal;

    private JMenuItem itemCargarEjemplo;
    private JMenuItem itemSimularRafaga;
    private JMenuItem itemExportarTxt;
    private JMenuItem itemLimpiarRegistros;

    private JComboBox<String> cmbEstacion;
    private JTextField txtFechaHora;
    private JTextField txtMilimetros;
    private JSpinner spnUmbralAlerta;
    private JButton btnRegistrarMedicion;
    private JButton btnEliminarUltimo;

    private JTabbedPane tabPrincipal;

    private JPanel panelBannerRecomendacion;
    private JLabel lblTituloRecomendacion;
    private JLabel lblTextoRecomendacion;
    private DefaultTableModel modeloReportes;
    private JTable tblReportes;
    private JButton btnModificarMedicion;
    private Lista<RegistroLluvia> registrosVisiblesReportes;

    private CardLayout cardNavegacion;
    private JPanel contenedorNavegacion;
    private PanelMapasPiura panelMapasPiura;
    private PanelCiudad3D panelCiudad3D;
    private JButton btnVolverAlMapa;
    private JLabel lblTituloCiudadNivel3;
    private JSlider sldProyeccionHoras;
    private JLabel lblValorProyeccion;

    private JLabel lblResumenAlertas;
    private DefaultTableModel modeloAlertas;
    private JTable tblAlertas;
    private JButton btnMarcarAtendida;

    private JTextField txtMinMm;
    private JTextField txtMaxMm;
    private JButton btnBuscarIntensidad;
    private JButton btnOrdenarIntensidad;
    private JComboBox<String> cmbFiltroDistrito;
    private JButton btnFiltrarDistrito;
    private JButton btnVerTodoHistorial;
    private JLabel lblEstadoHistorial;
    private DefaultTableModel modeloHistorial;
    private JTable tblHistorial;
    private Lista<RegistroLluvia> registrosVisiblesHistorial;
    private JLabel lblResumenCriticos;

    private JLabel lblKpiCiudadActiva;
    private JLabel lblKpiAcumuladoCiudad;
    private JLabel lblKpiAlturaSuelo;
    private JPanel panelTarjetaRiesgo;
    private JLabel lblKpiNivelRiesgo;
    private JLabel lblKpiAlertasActivas;

    private String ciudadActiva;
    private boolean mostrandoNivel3;
    private boolean modoLecturaSeleccionada;
    private RegistroLluvia lecturaVisualizada;
    private String modoHistorialActivo;
    private boolean actualizandoControles;

    public FrmPluviometro() {
        this.motor = new MotorPluviometro();
        this.motor.cargarDatosAutomatico();

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        this.formatoDecimal = new DecimalFormat("0.00", simbolos);
        this.registrosVisiblesReportes = new Lista<>();
        this.registrosVisiblesHistorial = new Lista<>();
        this.ciudadActiva = "Piura";
        this.mostrandoNivel3 = false;
        this.modoLecturaSeleccionada = false;
        this.lecturaVisualizada = null;
        this.modoHistorialActivo = "TODO";
        this.actualizandoControles = false;

        inicializarVentana();
        inicializarMenu();
        inicializarComponentes();
        spnUmbralAlerta.setValue(motor.getUmbralCriticoMm());
        registrarEventos();
        lecturaVisualizada = obtenerUltimaLecturaEnFecha(ciudadActiva, obtenerFechaFiltroActual());
        actualizarVistas();
    }

    private void inicializarVentana() {
        setTitle("Monitor Meteorológico Regional - Sistema de Alerta de Lluvias");
        setSize(1180, 740);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
    }

    private void inicializarMenu() {
        JMenuBar barraMenu = new JMenuBar();

        JMenu menuSimulacionExportacion = new JMenu("Simulación y Exportación");
        menuSimulacionExportacion.setFont(new Font("SansSerif", Font.PLAIN, 12));

        itemCargarEjemplo = new JMenuItem("Cargar datos de ejemplo");
        itemCargarEjemplo.setFont(new Font("SansSerif", Font.PLAIN, 12));

        itemSimularRafaga = new JMenuItem("Simular ráfaga de lluvia (6 lecturas)");
        itemSimularRafaga.setFont(new Font("SansSerif", Font.PLAIN, 12));

        itemExportarTxt = new JMenuItem("Exportar reporte (.txt)");
        itemExportarTxt.setFont(new Font("SansSerif", Font.PLAIN, 12));

        itemLimpiarRegistros = new JMenuItem("Limpiar todos los registros");
        itemLimpiarRegistros.setFont(new Font("SansSerif", Font.PLAIN, 12));

        menuSimulacionExportacion.add(itemCargarEjemplo);
        menuSimulacionExportacion.add(itemSimularRafaga);
        menuSimulacionExportacion.addSeparator();
        menuSimulacionExportacion.add(itemExportarTxt);
        menuSimulacionExportacion.addSeparator();
        menuSimulacionExportacion.add(itemLimpiarRegistros);

        barraMenu.add(menuSimulacionExportacion);
        setJMenuBar(barraMenu);
    }

    private void inicializarComponentes() {
        JPanel contenedorNorte = new JPanel(new BorderLayout());
        contenedorNorte.add(crearPanelCabecera(), BorderLayout.NORTH);
        contenedorNorte.add(crearPanelRegistroSuperior(), BorderLayout.CENTER);
        add(contenedorNorte, BorderLayout.NORTH);

        tabPrincipal = new JTabbedPane();
        tabPrincipal.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabPrincipal.setBorder(new EmptyBorder(6, 10, 6, 10));
        tabPrincipal.addTab("Monitor en Vivo", crearPestanaMonitorEnVivo());
        tabPrincipal.addTab("Alertas de Prevención", crearPestanaAlertasPrevencion());
        tabPrincipal.addTab("Historial y Búsqueda por Intensidad", crearPestanaHistorialBusqueda());
        add(tabPrincipal, BorderLayout.CENTER);

        add(crearPanelInferiorSemaforo(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelCabecera() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(14, 42, 71));
        panel.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel lblTitulo = new JLabel("CENTRO DE MONITOREO DE LLUVIAS Y ALERTA CIUDADANA");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Región Piura  •  Mapa Departamental de 8 Provincias, Distritos y Simulación 3D");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(186, 214, 240));

        panel.add(lblTitulo, BorderLayout.WEST);
        panel.add(lblSubtitulo, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearPanelRegistroSuperior() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(244, 247, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(5, 10, 2, 10),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Registro y Monitoreo por Distrito",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("SansSerif", Font.BOLD, 12),
                        new Color(15, 41, 66)
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        cmbEstacion = new JComboBox<>(DISTRITOS_PIURA);
        cmbEstacion.setFont(new Font("SansSerif", Font.PLAIN, 12));

        txtFechaHora = new JTextField("2026-11-15 15:00", 14);
        txtFechaHora.setFont(new Font("SansSerif", Font.PLAIN, 12));

        txtMilimetros = new JTextField("12.5", 7);
        txtMilimetros.setFont(new Font("SansSerif", Font.PLAIN, 12));

        spnUmbralAlerta = new JSpinner(new SpinnerNumberModel(15.0, 1.0, 100.0, 0.5));
        spnUmbralAlerta.setFont(new Font("SansSerif", Font.BOLD, 12));
        spnUmbralAlerta.setPreferredSize(new Dimension(74, 26));

        gbc.gridy = 0;
        gbc.gridx = 0;
        panel.add(crearEtiquetaCampo("Distrito:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.30;
        panel.add(cmbEstacion, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(crearEtiquetaCampo("Fecha / Hora:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.22;
        panel.add(txtFechaHora, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        panel.add(crearEtiquetaCampo("Precipitación (mm):"), gbc);
        gbc.gridx = 5;
        gbc.weightx = 0.14;
        panel.add(txtMilimetros, gbc);

        gbc.gridx = 6;
        gbc.weightx = 0;
        panel.add(crearEtiquetaCampo("Umbral Alerta (mm):"), gbc);
        gbc.gridx = 7;
        gbc.weightx = 0.10;
        panel.add(spnUmbralAlerta, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        panelBotones.setOpaque(false);

        btnRegistrarMedicion = crearBotonEstilizado("Registrar Medición", new Color(21, 101, 192), Color.WHITE);
        btnEliminarUltimo = crearBotonDiscreto("Eliminar Último Registro");

        panelBotones.add(btnRegistrarMedicion);
        panelBotones.add(btnEliminarUltimo);

        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 8;
        gbc.weightx = 1.0;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JPanel crearPestanaMonitorEnVivo() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(6, 8, 6, 8));

        panelBannerRecomendacion = new JPanel(new BorderLayout(6, 2));
        panelBannerRecomendacion.setBackground(new Color(236, 253, 245));
        panelBannerRecomendacion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 5, 1, 1, new Color(22, 163, 74)),
                new EmptyBorder(7, 10, 7, 10)
        ));

        lblTituloRecomendacion = new JLabel("Recomendación para la población — Piura");
        lblTituloRecomendacion.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTituloRecomendacion.setForeground(new Color(21, 128, 61));

        lblTextoRecomendacion = new JLabel("Sin lluvia: Condiciones normales.");
        lblTextoRecomendacion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTextoRecomendacion.setForeground(new Color(30, 41, 59));

        panelBannerRecomendacion.add(lblTituloRecomendacion, BorderLayout.NORTH);
        panelBannerRecomendacion.add(lblTextoRecomendacion, BorderLayout.CENTER);

        String[] columnas = {"Distrito", "Fecha / Hora", "Lluvia (mm)", "Estado / Riesgo"};
        modeloReportes = crearModeloNoEditable(columnas);
        tblReportes = new JTable(modeloReportes);
        estilizarTablaRiesgo(tblReportes, 3);
        tblReportes.getColumnModel().getColumn(0).setPreferredWidth(135);
        tblReportes.getColumnModel().getColumn(1).setPreferredWidth(115);
        tblReportes.getColumnModel().getColumn(2).setPreferredWidth(75);
        tblReportes.getColumnModel().getColumn(3).setPreferredWidth(115);

        btnModificarMedicion = crearBotonEstilizado("Modificar Medición (mm)", new Color(3, 105, 161), Color.WHITE);
        JLabel lblAyudaTabla = new JLabel("Doble clic en una fila para ajustar los mm");
        lblAyudaTabla.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblAyudaTabla.setForeground(new Color(100, 116, 139));

        JPanel barraAccionesTabla = new JPanel(new BorderLayout(6, 2));
        barraAccionesTabla.setOpaque(false);
        barraAccionesTabla.setBorder(new EmptyBorder(4, 2, 2, 2));
        barraAccionesTabla.add(lblAyudaTabla, BorderLayout.WEST);
        barraAccionesTabla.add(btnModificarMedicion, BorderLayout.EAST);

        JPanel panelTablaReportes = new JPanel(new BorderLayout(0, 4));
        panelTablaReportes.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Reportes Recientes",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(15, 41, 66)
        ));
        panelTablaReportes.add(new JScrollPane(tblReportes), BorderLayout.CENTER);
        panelTablaReportes.add(barraAccionesTabla, BorderLayout.SOUTH);

        JPanel panelIzquierdo = new JPanel(new BorderLayout(0, 6));
        panelIzquierdo.add(panelBannerRecomendacion, BorderLayout.NORTH);
        panelIzquierdo.add(panelTablaReportes, BorderLayout.CENTER);

        JPanel panelDerecho = crearPanelNavegacion3Niveles();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        split.setResizeWeight(0.41);
        split.setDividerLocation(465);
        split.setContinuousLayout(true);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelNavegacion3Niveles() {
        cardNavegacion = new CardLayout();
        contenedorNavegacion = new JPanel(cardNavegacion);
        contenedorNavegacion.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        panelMapasPiura = new PanelMapasPiura(motor, obtenerFechaFiltroActual(), new Consumer<String>() {
            @Override
            public void accept(String distritoSeleccionado) {
                navegarNivel3Ciudad(distritoSeleccionado);
            }
        });

        JPanel wrapperNivel3 = new JPanel(new BorderLayout());
        JPanel barraNivel3 = new JPanel(new BorderLayout(8, 0));
        barraNivel3.setBackground(new Color(241, 245, 249));
        barraNivel3.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)),
                new EmptyBorder(5, 10, 5, 10)
        ));

        btnVolverAlMapa = crearBotonDiscreto("← Volver al Mapa");
        lblTituloCiudadNivel3 = new JLabel("Ciudad 3D: Piura", SwingConstants.LEFT);
        lblTituloCiudadNivel3.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTituloCiudadNivel3.setForeground(new Color(15, 41, 66));

        JPanel panelIzqBarra3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelIzqBarra3.setOpaque(false);
        panelIzqBarra3.add(btnVolverAlMapa);
        panelIzqBarra3.add(lblTituloCiudadNivel3);

        JPanel panelSlider = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelSlider.setOpaque(false);
        JLabel lblProy = new JLabel("Proyección (1h a 6h):");
        lblProy.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblProy.setForeground(new Color(30, 41, 59));

        sldProyeccionHoras = new JSlider(1, 6, 1);
        sldProyeccionHoras.setOpaque(false);
        sldProyeccionHoras.setMajorTickSpacing(1);
        sldProyeccionHoras.setPaintTicks(true);
        sldProyeccionHoras.setSnapToTicks(true);
        sldProyeccionHoras.setPreferredSize(new Dimension(125, 26));

        lblValorProyeccion = new JLabel("1h");
        lblValorProyeccion.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblValorProyeccion.setForeground(new Color(21, 101, 192));

        panelSlider.add(lblProy);
        panelSlider.add(sldProyeccionHoras);
        panelSlider.add(lblValorProyeccion);

        barraNivel3.add(panelIzqBarra3, BorderLayout.WEST);
        barraNivel3.add(panelSlider, BorderLayout.EAST);

        panelCiudad3D = new PanelCiudad3D();
        wrapperNivel3.add(barraNivel3, BorderLayout.NORTH);
        wrapperNivel3.add(panelCiudad3D, BorderLayout.CENTER);

        contenedorNavegacion.add(panelMapasPiura, "MAPA");
        contenedorNavegacion.add(wrapperNivel3, "CIUDAD_3D");
        cardNavegacion.show(contenedorNavegacion, "MAPA");

        return contenedorNavegacion;
    }

    private JPanel crearPestanaAlertasPrevencion() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 4));
        panelSuperior.setBackground(new Color(254, 242, 242));
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 202, 202)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        lblResumenAlertas = new JLabel("No existen alertas activas por encima del umbral configurado.");
        lblResumenAlertas.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblResumenAlertas.setForeground(new Color(153, 27, 27));

        btnMarcarAtendida = crearBotonEstilizado("Marcar Alerta como Atendida", new Color(185, 28, 28), Color.WHITE);

        panelSuperior.add(lblResumenAlertas, BorderLayout.WEST);
        panelSuperior.add(btnMarcarAtendida, BorderLayout.EAST);

        String[] columnas = {
            "Distrito Afectado",
            "Fecha / Hora",
            "Lluvia (mm)",
            "Nivel de Peligro",
            "Acción Recomendada para los Vecinos"
        };
        modeloAlertas = crearModeloNoEditable(columnas);
        tblAlertas = new JTable(modeloAlertas);
        estilizarTablaNivelAlerta(tblAlertas, 3);
        tblAlertas.getColumnModel().getColumn(0).setPreferredWidth(145);
        tblAlertas.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblAlertas.getColumnModel().getColumn(2).setPreferredWidth(85);
        tblAlertas.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblAlertas.getColumnModel().getColumn(4).setPreferredWidth(455);

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "Avisos de Prevención Activos",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(153, 27, 27)
        ));
        panelTabla.add(new JScrollPane(tblAlertas), BorderLayout.CENTER);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(panelTabla, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPestanaHistorialBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel panelFiltros = new JPanel(new GridLayout(2, 1, 0, 2));
        panelFiltros.setBackground(new Color(248, 250, 252));
        panelFiltros.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JPanel filaRango = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        filaRango.setOpaque(false);

        txtMinMm = new JTextField("5.0", 5);
        txtMaxMm = new JTextField("40.0", 5);
        btnBuscarIntensidad = crearBotonEstilizado("Buscar por Rango (mm)", new Color(21, 101, 192), Color.WHITE);
        btnOrdenarIntensidad = crearBotonEstilizado("Ordenar de Menor a Mayor", new Color(46, 125, 50), Color.WHITE);

        filaRango.add(crearEtiquetaCampo("Desde (mm):"));
        filaRango.add(txtMinMm);
        filaRango.add(crearEtiquetaCampo("Hasta (mm):"));
        filaRango.add(txtMaxMm);
        filaRango.add(btnBuscarIntensidad);
        filaRango.add(btnOrdenarIntensidad);

        JPanel filaDistrito = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        filaDistrito.setOpaque(false);

        String[] distritosFiltro = new String[DISTRITOS_PIURA.length + 1];
        distritosFiltro[0] = "Todos los distritos";
        for (int i = 0; i < DISTRITOS_PIURA.length; i++) {
            distritosFiltro[i + 1] = DISTRITOS_PIURA[i];
        }
        cmbFiltroDistrito = new JComboBox<>(distritosFiltro);
        btnFiltrarDistrito = crearBotonEstilizado("Filtrar por Distrito", new Color(0, 121, 107), Color.WHITE);
        btnVerTodoHistorial = crearBotonDiscreto("Ver Todo el Historial");

        filaDistrito.add(crearEtiquetaCampo("Distrito:"));
        filaDistrito.add(cmbFiltroDistrito);
        filaDistrito.add(btnFiltrarDistrito);
        filaDistrito.add(btnVerTodoHistorial);

        panelFiltros.add(filaRango);
        panelFiltros.add(filaDistrito);

        String[] columnas = {"Distrito", "Fecha / Hora", "Lluvia (mm)", "Estado / Riesgo"};
        modeloHistorial = crearModeloNoEditable(columnas);
        tblHistorial = new JTable(modeloHistorial);
        estilizarTablaRiesgo(tblHistorial, 3);

        lblEstadoHistorial = new JLabel("Historial completo de reportes (0 registros)");
        lblEstadoHistorial.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEstadoHistorial.setForeground(new Color(30, 58, 138));
        lblEstadoHistorial.setBorder(new EmptyBorder(4, 4, 2, 4));

        JPanel panelTabla = new JPanel(new BorderLayout(4, 4));
        panelTabla.add(lblEstadoHistorial, BorderLayout.NORTH);
        panelTabla.add(new JScrollPane(tblHistorial), BorderLayout.CENTER);

        JPanel panelResumenCritico = new JPanel(new BorderLayout(6, 4));
        panelResumenCritico.setBackground(new Color(255, 247, 237));
        panelResumenCritico.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, new Color(234, 88, 12)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblTituloCriticos = new JLabel("Resumen de Días y Horas Más Críticas");
        lblTituloCriticos.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTituloCriticos.setForeground(new Color(154, 52, 18));

        lblResumenCriticos = new JLabel("Aún no se registran episodios de lluvia para analizar.");
        lblResumenCriticos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblResumenCriticos.setForeground(new Color(51, 65, 85));

        panelResumenCritico.add(lblTituloCriticos, BorderLayout.NORTH);
        panelResumenCritico.add(lblResumenCriticos, BorderLayout.CENTER);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(panelTabla, BorderLayout.CENTER);
        panel.add(panelResumenCritico, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelInferiorSemaforo() {
        JPanel contenedor = new JPanel(new GridLayout(1, 5, 10, 0));
        contenedor.setBackground(new Color(226, 232, 240));
        contenedor.setBorder(new EmptyBorder(8, 10, 10, 10));

        lblKpiCiudadActiva = new JLabel("Piura", SwingConstants.CENTER);
        lblKpiAcumuladoCiudad = new JLabel("0.00 mm", SwingConstants.CENTER);
        lblKpiAlturaSuelo = new JLabel("0.00 cm", SwingConstants.CENTER);
        lblKpiNivelRiesgo = new JLabel("Sin lluvia", SwingConstants.CENTER);
        lblKpiAlertasActivas = new JLabel("0", SwingConstants.CENTER);

        JPanel tarjetaCiudad = crearTarjetaKPI("CIUDAD ACTIVA", lblKpiCiudadActiva, new Color(21, 101, 192), Color.WHITE);
        JPanel tarjetaAcumulado = crearTarjetaKPI("ACUMULADO DEL DÍA (mm)", lblKpiAcumuladoCiudad, new Color(0, 121, 107), Color.WHITE);
        JPanel tarjetaAlturaSuelo = crearTarjetaKPI("ALTURA ESTIMADA EN SUELO (cm)", lblKpiAlturaSuelo, new Color(3, 105, 161), Color.WHITE);
        panelTarjetaRiesgo = crearTarjetaKPI("NIVEL DE RIESGO", lblKpiNivelRiesgo, new Color(22, 163, 74), new Color(240, 253, 244));
        JPanel tarjetaAlertas = crearTarjetaKPI("ALERTAS ACTIVAS", lblKpiAlertasActivas, new Color(198, 40, 40), Color.WHITE);

        contenedor.add(tarjetaCiudad);
        contenedor.add(tarjetaAcumulado);
        contenedor.add(tarjetaAlturaSuelo);
        contenedor.add(panelTarjetaRiesgo);
        contenedor.add(tarjetaAlertas);
        return contenedor;
    }

    private JPanel crearTarjetaKPI(String titulo, JLabel lblValor, Color colorAcento, Color colorFondo) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(colorFondo);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 1, 1, 1, colorAcento),
                new EmptyBorder(6, 8, 8, 8)
        ));

        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblTitulo.setForeground(new Color(71, 85, 105));

        lblValor.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblValor.setForeground(colorAcento);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);
        return card;
    }

    private JLabel crearEtiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(30, 41, 59));
        return lbl;
    }

    private JButton crearBotonEstilizado(String texto, final Color colorFondo, Color colorTexto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(colorFondo.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(colorFondo.brighter());
                } else {
                    g2.setColor(colorFondo);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(colorTexto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 13, 7, 13));
        return btn;
    }

    private JButton crearBotonDiscreto(String texto) {
        final Color fondoNormal = new Color(226, 232, 240);
        final Color fondoHover = new Color(203, 213, 225);
        final Color bordeColor = new Color(148, 163, 184);
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(fondoHover.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(fondoHover);
                } else {
                    g2.setColor(fondoNormal);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(bordeColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(new Color(51, 65, 85));
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private DefaultTableModel crearModeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void estilizarTablaRiesgo(JTable tabla, int columnaEstado) {
        tabla.setRowHeight(24);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setGridColor(new Color(226, 232, 240));
        tabla.setShowVerticalLines(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(columnaEstado).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    String val = value.toString();
                    if ("Alerta extrema".equalsIgnoreCase(val)) {
                        c.setBackground(new Color(254, 226, 226));
                        c.setForeground(new Color(153, 27, 27));
                    } else if ("Lluvia fuerte".equalsIgnoreCase(val)) {
                        c.setBackground(new Color(255, 237, 213));
                        c.setForeground(new Color(154, 52, 18));
                    } else if ("Lluvia moderada".equalsIgnoreCase(val)) {
                        c.setBackground(new Color(254, 249, 195));
                        c.setForeground(new Color(133, 77, 14));
                    } else if ("Llovizna".equalsIgnoreCase(val)) {
                        c.setBackground(new Color(224, 242, 254));
                        c.setForeground(new Color(3, 105, 161));
                    } else {
                        c.setBackground(new Color(236, 253, 245));
                        c.setForeground(new Color(21, 128, 61));
                    }
                }
                return c;
            }
        });
    }

    private void estilizarTablaNivelAlerta(JTable tabla, int columnaNivel) {
        tabla.setRowHeight(26);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setGridColor(new Color(226, 232, 240));
        tabla.setShowVerticalLines(false);
        tabla.getColumnModel().getColumn(columnaNivel).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    String val = value.toString();
                    if ("Roja".equalsIgnoreCase(val)) {
                        c.setBackground(new Color(254, 226, 226));
                        c.setForeground(new Color(153, 27, 27));
                    } else if ("Naranja".equalsIgnoreCase(val)) {
                        c.setBackground(new Color(255, 237, 213));
                        c.setForeground(new Color(154, 52, 18));
                    } else if ("Amarilla".equalsIgnoreCase(val)) {
                        c.setBackground(new Color(254, 249, 195));
                        c.setForeground(new Color(133, 77, 14));
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(new Color(30, 41, 59));
                    }
                }
                return c;
            }
        });
    }

    private void registrarEventos() {
        itemCargarEjemplo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionCargarDatosEjemplo();
            }
        });

        itemSimularRafaga.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionSimularRafaga();
            }
        });

        itemExportarTxt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionExportarReporteTxt();
            }
        });

        itemLimpiarRegistros.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionLimpiarTodosLosRegistros();
            }
        });

        cmbEstacion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (actualizandoControles) {
                    return;
                }
                ciudadActiva = String.valueOf(cmbEstacion.getSelectedItem());
                modoLecturaSeleccionada = false;
                lecturaVisualizada = obtenerUltimaLecturaEnFecha(ciudadActiva, obtenerFechaFiltroActual());
                sldProyeccionHoras.setValue(1);
                lblValorProyeccion.setText("1h");
                lblTituloCiudadNivel3.setText("Ciudad 3D: " + ciudadActiva);
                actualizarPanelesCiudadanos();
            }
        });

        txtFechaHora.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alCambiarFechaInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                alCambiarFechaInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                alCambiarFechaInput();
            }
        });

        spnUmbralAlerta.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sincronizarUmbralDesdeSpinner();
            }
        });

        if (spnUmbralAlerta.getEditor() instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spnUmbralAlerta.getEditor();
            editor.getTextField().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    sincronizarUmbralDesdeSpinner();
                }
            });
        }

        btnRegistrarMedicion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionRegistrarMedicion();
            }
        });

        btnEliminarUltimo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionEliminarUltimoRegistro();
            }
        });

        btnModificarMedicion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int fila = tblReportes.getSelectedRow();
                RegistroLluvia objetivo = null;
                if (fila >= 0 && fila < registrosVisiblesReportes.getTamano()) {
                    objetivo = registrosVisiblesReportes.obtener(fila);
                } else if (lecturaVisualizada != null) {
                    objetivo = lecturaVisualizada;
                }
                if (objetivo == null) {
                    JOptionPane.showMessageDialog(FrmPluviometro.this, "Seleccione un reporte de la tabla para modificar sus milímetros.", "Sin selección", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                abrirDialogoModificarMedicion(objetivo);
            }
        });

        tblReportes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tblReportes.getSelectedRow();
                    if (fila >= 0 && fila < registrosVisiblesReportes.getTamano()) {
                        RegistroLluvia reg = registrosVisiblesReportes.obtener(fila);
                        if (reg != null) {
                            abrirDialogoModificarMedicion(reg);
                        }
                    }
                }
            }
        });

        tblReportes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() || actualizandoControles) {
                    return;
                }
                int fila = tblReportes.getSelectedRow();
                if (fila >= 0 && fila < registrosVisiblesReportes.getTamano()) {
                    RegistroLluvia reg = registrosVisiblesReportes.obtener(fila);
                    if (reg != null) {
                        seleccionarLectura(reg);
                    }
                }
            }
        });

        tblHistorial.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() || actualizandoControles) {
                    return;
                }
                int fila = tblHistorial.getSelectedRow();
                if (fila >= 0 && fila < registrosVisiblesHistorial.getTamano()) {
                    RegistroLluvia reg = registrosVisiblesHistorial.obtener(fila);
                    if (reg != null) {
                        seleccionarLectura(reg);
                    }
                }
            }
        });

        btnVolverAlMapa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                volverAlMapa();
            }
        });

        sldProyeccionHoras.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int horas = sldProyeccionHoras.getValue();
                lblValorProyeccion.setText(horas + "h");
                actualizarPanelesCiudadanos();
            }
        });

        btnMarcarAtendida.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accionMarcarAlertaAtendida();
            }
        });

        btnBuscarIntensidad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modoHistorialActivo = "RANGO";
                actualizarTablaHistorial(true);
            }
        });

        btnOrdenarIntensidad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modoHistorialActivo = "ORDENADO";
                actualizarTablaHistorial(true);
            }
        });

        btnFiltrarDistrito.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modoHistorialActivo = "DISTRITO";
                actualizarTablaHistorial(true);
            }
        });

        btnVerTodoHistorial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modoHistorialActivo = "TODO";
                actualizarTablaHistorial(true);
            }
        });
    }

    private void alCambiarFechaInput() {
        if (actualizandoControles) {
            return;
        }
        String texto = txtFechaHora.getText().trim();
        if (texto.isEmpty() || texto.length() >= 10) {
            String fecha = obtenerFechaFiltroActual();
            panelMapasPiura.setFechaFiltro(fecha);
            if (!modoLecturaSeleccionada) {
                lecturaVisualizada = obtenerUltimaLecturaEnFecha(ciudadActiva, fecha);
            }
            actualizarPanelesCiudadanos();
        }
    }

    private String obtenerFechaFiltroActual() {
        String texto = txtFechaHora.getText().trim();
        if (texto.length() > 10) {
            return texto.substring(0, 10);
        }
        return texto;
    }

    private RegistroLluvia obtenerUltimaLecturaEnFecha(String ciudad, String fechaPrefix) {
        if (ciudad == null) {
            return null;
        }
        String fecha = fechaPrefix != null ? fechaPrefix.trim() : "";
        if (fecha.isEmpty()) {
            return motor.obtenerUltimaLecturaCiudad(ciudad);
        }
        if (fecha.length() > 10) {
            fecha = fecha.substring(0, 10);
        }
        RegistroLluvia ultima = null;
        Nodo<RegistroLluvia> actual = motor.getBitacoraCronologica().getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg != null && ciudad.equalsIgnoreCase(reg.getEstacion())) {
                String fh = reg.getFechaHora() != null ? reg.getFechaHora().trim() : "";
                String fechaReg = fh.length() >= 10 ? fh.substring(0, 10) : fh;
                if (fechaReg.equals(fecha)) {
                    ultima = reg;
                }
            }
            actual = actual.getSiguiente();
        }
        return ultima;
    }

    private void volverAlMapa() {
        mostrandoNivel3 = false;
        sldProyeccionHoras.setValue(1);
        lblValorProyeccion.setText("1h");
        cardNavegacion.show(contenedorNavegacion, "MAPA");
        panelMapasPiura.setFechaFiltro(obtenerFechaFiltroActual());
        actualizarPanelesCiudadanos();
    }

    private void navegarNivel3Ciudad(String distrito) {
        actualizandoControles = true;
        ciudadActiva = distrito;
        cmbEstacion.setSelectedItem(ciudadActiva);
        modoLecturaSeleccionada = false;
        lecturaVisualizada = obtenerUltimaLecturaEnFecha(ciudadActiva, obtenerFechaFiltroActual());
        sldProyeccionHoras.setValue(1);
        lblValorProyeccion.setText("1h");
        actualizandoControles = false;

        mostrandoNivel3 = true;
        lblTituloCiudadNivel3.setText("Ciudad 3D: " + ciudadActiva);
        cardNavegacion.show(contenedorNavegacion, "CIUDAD_3D");
        actualizarPanelesCiudadanos();
    }

    private void seleccionarLectura(RegistroLluvia reg) {
        actualizandoControles = true;
        ciudadActiva = reg.getEstacion();
        modoLecturaSeleccionada = true;
        lecturaVisualizada = reg;
        cmbEstacion.setSelectedItem(ciudadActiva);
        actualizandoControles = false;
        lblTituloCiudadNivel3.setText("Ciudad 3D: " + ciudadActiva);
        actualizarPanelesCiudadanos();
    }

    private void abrirDialogoModificarMedicion(RegistroLluvia reg) {
        String entrada = JOptionPane.showInputDialog(
                this,
                "Distrito: " + reg.getEstacion()
                        + "\nFecha / Hora: " + reg.getFechaHora()
                        + "\nPrecipitación actual: " + formatoDecimal.format(reg.getMilimetros()) + " mm"
                        + "\n\nIngrese el nuevo valor de lluvia (mm):",
                formatoDecimal.format(reg.getMilimetros())
        );
        if (entrada == null) {
            return;
        }
        double nuevoMm;
        try {
            nuevoMm = Double.parseDouble(entrada.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido en milímetros (mm).", "Valor inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (nuevoMm < 0.0) {
            JOptionPane.showMessageDialog(this, "La precipitación no puede ser negativa.", "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (motor.editarMedicion(reg.getId(), nuevoMm)) {
            actualizandoControles = true;
            ciudadActiva = reg.getEstacion();
            modoLecturaSeleccionada = true;
            lecturaVisualizada = reg;
            cmbEstacion.setSelectedItem(ciudadActiva);
            actualizandoControles = false;

            mostrandoNivel3 = true;
            lblTituloCiudadNivel3.setText("Ciudad 3D: " + ciudadActiva);
            cardNavegacion.show(contenedorNavegacion, "CIUDAD_3D");
            actualizarVistas();
        }
    }

    private void accionExportarReporteTxt() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Reporte Meteorológico (.txt)");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        chooser.setSelectedFile(new File("reporte_lluvias_piura.txt"));

        int opcion = chooser.showSaveDialog(this);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".txt")) {
            archivo = new File(archivo.getAbsolutePath() + ".txt");
        }
        try {
            motor.exportarReporteCiudadanoTxt(archivo);
            JOptionPane.showMessageDialog(
                    this,
                    "Reporte guardado correctamente en:\n" + archivo.getAbsolutePath(),
                    "Exportación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar el archivo:\n" + ex.getMessage(),
                    "Error al exportar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void accionLimpiarTodosLosRegistros() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar todos los registros de lluvia, alertas pendientes y vaciar el archivo guardado?",
                "Limpiar todos los registros",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        motor.limpiarTodosLosRegistros();
        modoLecturaSeleccionada = false;
        lecturaVisualizada = null;
        sldProyeccionHoras.setValue(1);
        lblValorProyeccion.setText("1h");
        actualizarVistas();
        JOptionPane.showMessageDialog(
                this,
                "Todos los registros y alertas han sido eliminados correctamente.",
                "Limpieza Completada",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void sincronizarUmbralDesdeSpinner() {
        try {
            spnUmbralAlerta.commitEdit();
        } catch (Exception ex) {
        }
        Object valor = spnUmbralAlerta.getValue();
        if (valor instanceof Number) {
            double nuevoUmbral = ((Number) valor).doubleValue();
            if (nuevoUmbral > 0.0) {
                motor.setUmbralCriticoMm(nuevoUmbral);
                actualizarPanelesCiudadanos();
            }
        }
    }

    private void accionRegistrarMedicion() {
        sincronizarUmbralDesdeSpinner();
        String distrito = String.valueOf(cmbEstacion.getSelectedItem());
        String fechaHora = txtFechaHora.getText().trim();
        if (fechaHora.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese la fecha y hora de la medición.", "Dato requerido", JOptionPane.WARNING_MESSAGE);
            txtFechaHora.requestFocus();
            return;
        }
        double mm;
        try {
            mm = Double.parseDouble(txtMilimetros.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un valor numérico válido en milímetros (mm).", "Dato inválido", JOptionPane.ERROR_MESSAGE);
            txtMilimetros.requestFocus();
            return;
        }
        if (mm < 0.0) {
            JOptionPane.showMessageDialog(this, "La cantidad de lluvia no puede ser negativa.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        actualizandoControles = true;
        RegistroLluvia nuevo = motor.registrarMedicion(distrito, fechaHora, mm);
        ciudadActiva = distrito;
        modoLecturaSeleccionada = false;
        lecturaVisualizada = nuevo;
        avanzarFechaInput(fechaHora);
        actualizandoControles = false;

        lblTituloCiudadNivel3.setText("Ciudad 3D: " + ciudadActiva);
        actualizarVistas();
    }

    private void avanzarFechaInput(String fechaActual) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime dt = LocalDateTime.parse(fechaActual, formatter).plusHours(1);
            txtFechaHora.setText(dt.format(formatter));
        } catch (Exception ex) {
        }
    }

    private void accionSimularRafaga() {
        sincronizarUmbralDesdeSpinner();
        int totalCiudades = cmbEstacion.getItemCount();
        String[] ciudades = new String[totalCiudades];
        for (int i = 0; i < totalCiudades; i++) {
            ciudades[i] = String.valueOf(cmbEstacion.getItemAt(i));
        }
        motor.simularLecturasAleatorias(6, ciudades);
        ciudadActiva = String.valueOf(cmbEstacion.getSelectedItem());
        modoLecturaSeleccionada = false;
        lecturaVisualizada = obtenerUltimaLecturaEnFecha(ciudadActiva, obtenerFechaFiltroActual());
        if (lecturaVisualizada == null) {
            lecturaVisualizada = motor.obtenerUltimaLecturaCiudad(ciudadActiva);
        }
        lblTituloCiudadNivel3.setText("Ciudad 3D: " + ciudadActiva);
        actualizarVistas();
    }

    private void accionCargarDatosEjemplo() {
        sincronizarUmbralDesdeSpinner();
        String fechaBase = obtenerFechaFiltroActual();
        if (fechaBase.length() != 10) {
            fechaBase = "2026-11-15";
        }
        motor.registrarMedicion("Pariñas (Talara)", fechaBase + " 07:00", 2.1);
        motor.registrarMedicion("Paita", fechaBase + " 08:00", 6.2);
        motor.registrarMedicion("Sechura", fechaBase + " 09:00", 0.0);
        motor.registrarMedicion("Sullana", fechaBase + " 10:00", 11.4);
        motor.registrarMedicion("Piura", fechaBase + " 11:00", 17.8);
        motor.registrarMedicion("Catacaos", fechaBase + " 12:00", 8.9);
        motor.registrarMedicion("Tambogrande", fechaBase + " 13:00", 16.5);
        motor.registrarMedicion("Chulucanas", fechaBase + " 14:00", 33.4);
        motor.registrarMedicion("Ayabaca", fechaBase + " 15:00", 14.2);
        motor.registrarMedicion("Huancabamba", fechaBase + " 16:00", 26.8);

        ciudadActiva = String.valueOf(cmbEstacion.getSelectedItem());
        modoLecturaSeleccionada = false;
        lecturaVisualizada = obtenerUltimaLecturaEnFecha(ciudadActiva, fechaBase);
        lblTituloCiudadNivel3.setText("Ciudad 3D: " + ciudadActiva);
        actualizarVistas();
    }

    private void accionEliminarUltimoRegistro() {
        RegistroLluvia eliminado = motor.deshacerUltimaMedicion();
        if (eliminado == null) {
            JOptionPane.showMessageDialog(this, "No hay registros recientes disponibles para eliminar.", "Sin registros", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        modoLecturaSeleccionada = false;
        lecturaVisualizada = obtenerUltimaLecturaEnFecha(ciudadActiva, obtenerFechaFiltroActual());
        actualizarVistas();
        JOptionPane.showMessageDialog(
                this,
                "Se eliminó el último reporte ingresado:\n"
                        + eliminado.getEstacion() + " — " + eliminado.getFechaHora()
                        + " (" + formatoDecimal.format(eliminado.getMilimetros()) + " mm)",
                "Último Registro Eliminado",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void accionMarcarAlertaAtendida() {
        AlertaHidrica atendida = motor.atenderSiguienteAlerta();
        if (atendida == null) {
            JOptionPane.showMessageDialog(this, "No quedan alertas pendientes de atención.", "Sin alertas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        actualizarVistas();
        JOptionPane.showMessageDialog(
                this,
                "Alerta atendida en " + atendida.getEstacion() + " (" + atendida.getFechaHora() + "):\n"
                        + "Nivel de peligro: " + atendida.getNivel() + " (" + formatoDecimal.format(atendida.getMilimetros()) + " mm)\n"
                        + "Medida aplicada: " + atendida.getRecomendacion(),
                "Alerta Atendida",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void actualizarVistas() {
        actualizarTablaReportes();
        actualizarTablaAlertas();
        actualizarTablaHistorial(false);
        panelMapasPiura.setFechaFiltro(obtenerFechaFiltroActual());
        actualizarPanelesCiudadanos();
    }

    private void actualizarPanelesCiudadanos() {
        String fechaFiltro = obtenerFechaFiltroActual();
        double acumuladoDia = fechaFiltro.isEmpty()
                ? motor.calcularAcumuladoPorCiudad(ciudadActiva)
                : motor.calcularAcumuladoPorCiudadYFecha(ciudadActiva, fechaFiltro);

        int horasProyeccion = mostrandoNivel3 ? sldProyeccionHoras.getValue() : 1;
        double mmLluvia;
        double mmBaseBidon;
        String fuenteHud;

        if (modoLecturaSeleccionada && lecturaVisualizada != null) {
            mmLluvia = lecturaVisualizada.getMilimetros();
            mmBaseBidon = mmLluvia;
            fuenteHud = "Reporte (" + lecturaVisualizada.getFechaHora() + ")";
        } else {
            RegistroLluvia ultimaFecha = obtenerUltimaLecturaEnFecha(ciudadActiva, fechaFiltro);
            mmLluvia = ultimaFecha != null ? ultimaFecha.getMilimetros() : 0.0;
            mmBaseBidon = acumuladoDia;
            fuenteHud = fechaFiltro.isEmpty() ? "Acumulado total" : "Acumulado del día (" + fechaFiltro + ")";
        }

        double mmBidonProyectado = mmBaseBidon + (horasProyeccion > 1 ? mmLluvia * (horasProyeccion - 1) : 0.0);
        double alturaSueloCm = calcularAlturaSueloCm(mmBidonProyectado);

        String nivelRiesgo = obtenerNombreRiesgo(mmLluvia);
        String recomendacion = obtenerRecomendacionCiudadana(mmLluvia);
        Color colorRiesgo = obtenerColorRiesgo(mmLluvia);
        Color fondoRiesgo = obtenerFondoSuaveRiesgo(mmLluvia);

        String textoBanner = recomendacion;
        if (mmLluvia > 0.0) {
            textoBanner = recomendacion + " (Caen aprox. " + formatoDecimal.format(mmLluvia) + " L/m²).";
        }
        lblTituloRecomendacion.setText("Recomendación para la población — " + ciudadActiva + " (" + nivelRiesgo + ")");
        lblTituloRecomendacion.setForeground(colorRiesgo);
        lblTextoRecomendacion.setText(textoBanner);
        panelBannerRecomendacion.setBackground(fondoRiesgo);
        panelBannerRecomendacion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 5, 1, 1, colorRiesgo),
                new EmptyBorder(7, 10, 7, 10)
        ));

        lblKpiCiudadActiva.setText(ciudadActiva);
        lblKpiAcumuladoCiudad.setText("<html><center>"
                + formatoDecimal.format(acumuladoDia) + " mm<br>"
                + "<span style='font-size:10px;'>(" + formatoDecimal.format(acumuladoDia) + " L/m²)</span>"
                + "</center></html>");
        lblKpiAlturaSuelo.setText(formatoDecimal.format(alturaSueloCm) + " cm");
        lblKpiNivelRiesgo.setText(nivelRiesgo);
        lblKpiNivelRiesgo.setForeground(colorRiesgo);
        panelTarjetaRiesgo.setBackground(fondoRiesgo);
        panelTarjetaRiesgo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 1, 1, 1, colorRiesgo),
                new EmptyBorder(6, 8, 8, 8)
        ));
        lblKpiAlertasActivas.setText(String.valueOf(motor.getColaAlertasPendientes().getTamano()));

        panelCiudad3D.actualizarDatos(ciudadActiva, mmLluvia, mmBidonProyectado, motor.getUmbralCriticoMm(), horasProyeccion, fuenteHud);
        panelMapasPiura.refrescar();
    }

    private double calcularAlturaSueloCm(double mmTotales) {
        if (mmTotales <= 0.0) {
            return 0.0;
        }
        return Math.round(Math.min(150.0, mmTotales * 0.5) * 100.0) / 100.0;
    }

    private void actualizarTablaReportes() {
        actualizandoControles = true;
        modeloReportes.setRowCount(0);
        registrosVisiblesReportes.limpiar();

        Nodo<RegistroLluvia> actual = motor.getBitacoraCronologica().getCabeza();
        while (actual != null) {
            registrosVisiblesReportes.agregarInicio(actual.getDato());
            actual = actual.getSiguiente();
        }

        Nodo<RegistroLluvia> nodoOrdenado = registrosVisiblesReportes.getCabeza();
        while (nodoOrdenado != null) {
            RegistroLluvia reg = nodoOrdenado.getDato();
            modeloReportes.addRow(new Object[]{
                reg.getEstacion(),
                reg.getFechaHora(),
                formatoDecimal.format(reg.getMilimetros()),
                obtenerNombreRiesgo(reg.getMilimetros())
            });
            nodoOrdenado = nodoOrdenado.getSiguiente();
        }
        actualizandoControles = false;
    }

    private void actualizarTablaAlertas() {
        modeloAlertas.setRowCount(0);
        int cantidad = motor.getColaAlertasPendientes().getTamano();
        if (cantidad == 0) {
            lblResumenAlertas.setText("No existen alertas activas por encima del umbral configurado.");
        } else if (cantidad == 1) {
            lblResumenAlertas.setText("1 aviso de prevención activo pendiente de atención vecinal.");
        } else {
            lblResumenAlertas.setText(cantidad + " avisos de prevención activos pendientes de atención vecinal.");
        }

        Nodo<AlertaHidrica> actual = motor.getColaAlertasPendientes().getFrente();
        while (actual != null) {
            AlertaHidrica a = actual.getDato();
            modeloAlertas.addRow(new Object[]{
                a.getEstacion(),
                a.getFechaHora(),
                formatoDecimal.format(a.getMilimetros()),
                a.getNivel(),
                a.getRecomendacion()
            });
            actual = actual.getSiguiente();
        }
    }

    private void actualizarTablaHistorial(boolean mostrarAdvertencias) {
        Lista<RegistroLluvia> resultado;
        if ("RANGO".equals(modoHistorialActivo)) {
            double min;
            double max;
            try {
                min = Double.parseDouble(txtMinMm.getText().trim().replace(",", "."));
                max = Double.parseDouble(txtMaxMm.getText().trim().replace(",", "."));
            } catch (NumberFormatException ex) {
                if (mostrarAdvertencias) {
                    JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos para buscar por rango.", "Rango inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                modoHistorialActivo = "TODO";
                resultado = motor.getBitacoraCronologica();
                lblEstadoHistorial.setText("Historial completo de reportes (" + resultado.getTamano() + " registros)");
                llenarTablaHistorial(resultado);
                actualizarResumenCriticos(resultado);
                return;
            }
            if (min < 0.0 || max < 0.0 || min > max) {
                if (mostrarAdvertencias) {
                    JOptionPane.showMessageDialog(this, "Verifique que el rango desde/hasta sea válido y no negativo.", "Rango inválido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                modoHistorialActivo = "TODO";
                resultado = motor.getBitacoraCronologica();
                lblEstadoHistorial.setText("Historial completo de reportes (" + resultado.getTamano() + " registros)");
                llenarTablaHistorial(resultado);
                actualizarResumenCriticos(resultado);
                return;
            }
            resultado = motor.consultarPorRangoMm(min, max);
            lblEstadoHistorial.setText("Lluvias entre " + formatoDecimal.format(min) + " mm y " + formatoDecimal.format(max) + " mm: " + resultado.getTamano() + " episodio(s)");
        } else if ("ORDENADO".equals(modoHistorialActivo)) {
            resultado = motor.obtenerOrderingPorIntensidad();
            lblEstadoHistorial.setText("Historial ordenado de menor a mayor intensidad: " + resultado.getTamano() + " episodio(s)");
        } else if ("DISTRITO".equals(modoHistorialActivo)) {
            String distritoSel = String.valueOf(cmbFiltroDistrito.getSelectedItem());
            if ("Todos los distritos".equals(distritoSel)) {
                resultado = motor.getBitacoraCronologica();
                lblEstadoHistorial.setText("Historial completo de todos los distritos: " + resultado.getTamano() + " episodio(s)");
            } else {
                resultado = motor.filtrarPorEstacion(distritoSel);
                lblEstadoHistorial.setText("Historial en " + distritoSel + ": " + resultado.getTamano() + " episodio(s)");
            }
        } else {
            resultado = motor.getBitacoraCronologica();
            lblEstadoHistorial.setText("Historial completo de reportes (" + resultado.getTamano() + " registros)");
        }

        llenarTablaHistorial(resultado);
        actualizarResumenCriticos(resultado);
    }

    private void llenarTablaHistorial(Lista<RegistroLluvia> lista) {
        actualizandoControles = true;
        modeloHistorial.setRowCount(0);
        registrosVisiblesHistorial.limpiar();

        Nodo<RegistroLluvia> actual = lista.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            registrosVisiblesHistorial.agregarFinal(reg);
            modeloHistorial.addRow(new Object[]{
                reg.getEstacion(),
                reg.getFechaHora(),
                formatoDecimal.format(reg.getMilimetros()),
                obtenerNombreRiesgo(reg.getMilimetros())
            });
            actual = actual.getSiguiente();
        }
        actualizandoControles = false;
    }

    private void actualizarResumenCriticos(Lista<RegistroLluvia> listaAnalizada) {
        if (listaAnalizada.estaVacia()) {
            lblResumenCriticos.setText("Aún no se registran episodios de lluvia en esta consulta.");
            return;
        }
        RegistroLluvia[] top = new RegistroLluvia[3];
        int fuertesOExtremos = 0;

        Nodo<RegistroLluvia> actual = listaAnalizada.getCabeza();
        while (actual != null) {
            RegistroLluvia reg = actual.getDato();
            if (reg.getMilimetros() >= 7.6) {
                fuertesOExtremos++;
            }
            if (top[0] == null || reg.getMilimetros() > top[0].getMilimetros()) {
                top[2] = top[1];
                top[1] = top[0];
                top[0] = reg;
            } else if (top[1] == null || reg.getMilimetros() > top[1].getMilimetros()) {
                top[2] = top[1];
                top[1] = reg;
            } else if (top[2] == null || reg.getMilimetros() > top[2].getMilimetros()) {
                top[2] = reg;
            }
            actual = actual.getSiguiente();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Episodios fuertes o extremos (>= 7.6 mm): ").append(fuertesOExtremos).append("   |   Horarios más críticos: ");
        for (int i = 0; i < 3; i++) {
            if (top[i] != null) {
                if (i > 0) {
                    sb.append("  •  ");
                }
                sb.append(top[i].getEstacion())
                        .append(" (")
                        .append(top[i].getFechaHora())
                        .append(" — ")
                        .append(formatoDecimal.format(top[i].getMilimetros()))
                        .append(" mm)");
            }
        }
        lblResumenCriticos.setText(sb.toString());
    }

    private String obtenerNombreRiesgo(double mm) {
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

    private Color obtenerColorRiesgo(double mm) {
        if (mm <= 0.0) {
            return new Color(22, 163, 74);
        } else if (mm < 2.5) {
            return new Color(2, 132, 199);
        } else if (mm < 7.6) {
            return new Color(202, 138, 4);
        } else if (mm < 30.0) {
            return new Color(234, 88, 12);
        } else {
            return new Color(220, 38, 38);
        }
    }

    private Color obtenerFondoSuaveRiesgo(double mm) {
        if (mm <= 0.0) {
            return new Color(240, 253, 244);
        } else if (mm < 2.5) {
            return new Color(240, 249, 255);
        } else if (mm < 7.6) {
            return new Color(254, 252, 232);
        } else if (mm < 30.0) {
            return new Color(255, 247, 237);
        } else {
            return new Color(254, 242, 242);
        }
    }

    private class Gota {
        double x;
        double y;
        double largo;
        double velocidadY;
        boolean enPrimerPlano;
    }

    private class Onda {
        boolean activa;
        double x;
        double y;
        double radio;
        double radioMax;
        double velocidad;
        double opacidad;
    }

    private class Edificio3D {
        boolean izquierda;
        double profundidad;
        double alturaBase;
        Color colorFachada;
        Color colorLateral;
        Color colorTecho;
        int pisos;
    }

    private class Nube {
        double x;
        double y;
        double ancho;
        double velocidad;
    }

    private class PanelCiudad3D extends JPanel {

        private Gota[] gotas;
        private Onda[] ondas;
        private Edificio3D[] edificios;
        private Nube[] nubes;
        private Timer timerEscena;

        private String ciudad;
        private String fuenteHud;
        private double mmIntensidadObjetivo;
        private double mmIntensidadSuavizada;
        private double umbralCritico;
        private int proyeccionHoras;
        private double mmBidonObjetivo;
        private double mmBidonSuavizado;
        private double alturaSueloCmObjetivo;
        private double alturaSueloCmSuavizada;
        private double escalaBidonSuavizada;

        private double faseAgua;
        private double relampagoAlpha;
        private int cooldownRelampago;
        private double rayoX;

        public PanelCiudad3D() {
            setBackground(new Color(186, 230, 253));
            this.ciudad = "Piura";
            this.fuenteHud = "Acumulado del día";
            this.mmIntensidadObjetivo = 0.0;
            this.mmIntensidadSuavizada = 0.0;
            this.umbralCritico = 15.0;
            this.proyeccionHoras = 1;
            this.mmBidonObjetivo = 0.0;
            this.mmBidonSuavizado = 0.0;
            this.alturaSueloCmObjetivo = 0.0;
            this.alturaSueloCmSuavizada = 0.0;
            this.escalaBidonSuavizada = 300.0;
            this.faseAgua = 0.0;
            this.relampagoAlpha = 0.0;
            this.cooldownRelampago = 85;
            this.rayoX = 140.0;

            this.gotas = new Gota[220];
            for (int i = 0; i < gotas.length; i++) {
                gotas[i] = new Gota();
                gotas[i].x = Math.random() * 640.0;
                gotas[i].y = Math.random() * 440.0;
                gotas[i].largo = 14.0;
                gotas[i].velocidadY = 8.0;
                gotas[i].enPrimerPlano = (i % 2 == 0);
            }

            this.ondas = new Onda[42];
            for (int i = 0; i < ondas.length; i++) {
                ondas[i] = new Onda();
                ondas[i].activa = false;
            }

            this.nubes = new Nube[5];
            for (int i = 0; i < nubes.length; i++) {
                nubes[i] = new Nube();
                nubes[i].x = i * 135.0;
                nubes[i].y = 22.0 + (i % 3) * 16.0;
                nubes[i].ancho = 92.0 + (i % 3) * 26.0;
                nubes[i].velocidad = 0.25 + (i % 3) * 0.12;
            }

            this.edificios = new Edificio3D[6];
            Color[] fachadas = {
                new Color(226, 232, 240), new Color(254, 243, 199), new Color(241, 245, 249),
                new Color(254, 226, 226), new Color(224, 242, 254), new Color(243, 232, 255)
            };
            Color[] laterales = {
                new Color(148, 163, 184), new Color(217, 179, 110), new Color(186, 196, 210),
                new Color(205, 155, 155), new Color(145, 180, 205), new Color(185, 165, 208)
            };
            Color[] techos = {
                new Color(153, 27, 27), new Color(180, 83, 9), new Color(71, 85, 105),
                new Color(127, 29, 29), new Color(30, 58, 138), new Color(88, 28, 135)
            };
            double[] profundidades = {0.26, 0.52, 0.80, 0.26, 0.52, 0.80};
            double[] alturas = {185.0, 160.0, 215.0, 175.0, 205.0, 165.0};
            int[] pisos = {3, 2, 3, 2, 3, 2};
            for (int i = 0; i < edificios.length; i++) {
                edificios[i] = new Edificio3D();
                edificios[i].izquierda = i < 3;
                edificios[i].profundidad = profundidades[i];
                edificios[i].alturaBase = alturas[i];
                edificios[i].colorFachada = fachadas[i];
                edificios[i].colorLateral = laterales[i];
                edificios[i].colorTecho = techos[i];
                edificios[i].pisos = pisos[i];
            }

            timerEscena = new Timer(16, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isShowing()) {
                        actualizarFisicaEscena();
                        repaint();
                    }
                }
            });
            timerEscena.start();
        }

        public void actualizarDatos(String ciudad, double mmLluvia, double mmBidonTotal, double umbralCritico, int proyeccionHoras, String fuenteHud) {
            this.ciudad = ciudad;
            this.mmIntensidadObjetivo = Math.max(0.0, mmLluvia);
            this.mmBidonObjetivo = Math.max(0.0, mmBidonTotal);
            this.umbralCritico = Math.max(1.0, umbralCritico);
            this.proyeccionHoras = Math.max(1, proyeccionHoras);
            this.fuenteHud = fuenteHud != null ? fuenteHud : "Acumulado del día";
            this.alturaSueloCmObjetivo = calcularAlturaSueloCm(this.mmBidonObjetivo);
        }

        private void actualizarFisicaEscena() {
            mmIntensidadSuavizada += (mmIntensidadObjetivo - mmIntensidadSuavizada) * 0.10;
            if (Math.abs(mmIntensidadObjetivo - mmIntensidadSuavizada) < 0.02 && mmIntensidadObjetivo == 0.0) {
                mmIntensidadSuavizada = 0.0;
            }
            mmBidonSuavizado += (mmBidonObjetivo - mmBidonSuavizado) * 0.08;
            if (Math.abs(mmBidonObjetivo - mmBidonSuavizado) < 0.02 && mmBidonObjetivo == 0.0) {
                mmBidonSuavizado = 0.0;
            }
            alturaSueloCmSuavizada += (alturaSueloCmObjetivo - alturaSueloCmSuavizada) * 0.08;
            if (Math.abs(alturaSueloCmObjetivo - alturaSueloCmSuavizada) < 0.02 && alturaSueloCmObjetivo == 0.0) {
                alturaSueloCmSuavizada = 0.0;
            }

            double escalaObj = Math.max(300.0, Math.ceil(mmBidonObjetivo / 50.0) * 50.0);
            escalaBidonSuavizada += (escalaObj - escalaBidonSuavizada) * 0.08;

            faseAgua += mmIntensidadSuavizada > 0.05 ? (0.10 + Math.min(0.18, mmIntensidadSuavizada * 0.005)) : 0.04;

            int w = getWidth();
            int h = getHeight();
            if (w <= 60 || h <= 60) {
                return;
            }

            double escenaW = w - 168.0;
            for (int i = 0; i < nubes.length; i++) {
                nubes[i].x += nubes[i].velocidad * (1.0 + Math.min(3.2, mmIntensidadSuavizada * 0.09));
                if (nubes[i].x - nubes[i].ancho > escenaW + 40.0) {
                    nubes[i].x = -nubes[i].ancho - 20.0;
                }
            }

            if (mmIntensidadSuavizada >= 7.6) {
                if (relampagoAlpha > 0.0) {
                    relampagoAlpha = Math.max(0.0, relampagoAlpha - 0.06);
                }
                cooldownRelampago--;
                if (cooldownRelampago <= 0) {
                    relampagoAlpha = mmIntensidadSuavizada >= 30.0 ? 0.90 : 0.55;
                    rayoX = 45.0 + Math.random() * Math.max(40.0, escenaW - 90.0);
                    cooldownRelampago = (mmIntensidadSuavizada >= 30.0 ? 45 : 110) + (int) (Math.random() * 90);
                }
            } else {
                relampagoAlpha = 0.0;
            }

            int activas = obtenerCantidadGotas(mmIntensidadSuavizada);
            double viento = obtenerViento(mmIntensidadSuavizada);
            double vel = obtenerVelocidad(mmIntensidadSuavizada);
            double largo = obtenerLargo(mmIntensidadSuavizada);
            double vpY = h * 0.40;
            double bidonX = w - 84.0;
            double bidonRx = 36.0;
            double bidonYBase = h - 42.0;
            double bidonAlto = Math.max(130.0, h * 0.54);
            double bidonYAgua = bidonYBase - Math.min(1.0, mmBidonSuavizado / Math.max(1.0, escalaBidonSuavizada)) * (bidonAlto - 6.0);

            for (int i = 0; i < activas; i++) {
                Gota gt = gotas[i];
                gt.largo = largo * (0.82 + (i % 5) * 0.08);
                gt.velocidadY = vel * (0.88 + (i % 4) * 0.08);
                gt.x += viento * (0.85 + (i % 3) * 0.12);
                gt.y += gt.velocidadY;

                if (Math.abs(gt.x - bidonX) <= bidonRx - 4.0 && gt.y >= bidonYAgua && gt.enPrimerPlano) {
                    crearOnda(gt.x, bidonYAgua + (Math.random() - 0.5) * 6.0);
                    gt.x = Math.random() * (w + 60.0) - 50.0;
                    gt.y = -15.0 - Math.random() * 70.0;
                    continue;
                }

                double limiteImpacto = vpY + 28.0 + ((i % 10) / 10.0) * (h - vpY - 45.0);
                if (gt.y >= limiteImpacto || gt.x > w + 50.0) {
                    if (gt.y >= limiteImpacto && gt.x < escenaW && Math.random() < (mmIntensidadSuavizada >= 30.0 ? 0.45 : 0.22)) {
                        crearOnda(gt.x, gt.y);
                    }
                    gt.x = Math.random() * (w + 60.0) - 50.0;
                    gt.y = -15.0 - Math.random() * 70.0;
                }
            }

            for (int i = 0; i < ondas.length; i++) {
                Onda o = ondas[i];
                if (o.activa) {
                    o.radio += o.velocidad;
                    o.opacidad -= 0.032;
                    if (o.opacidad <= 0.0 || o.radio >= o.radioMax) {
                        o.activa = false;
                    }
                }
            }
        }

        private void crearOnda(double x, double y) {
            for (int i = 0; i < ondas.length; i++) {
                if (!ondas[i].activa) {
                    ondas[i].activa = true;
                    ondas[i].x = x;
                    ondas[i].y = y;
                    ondas[i].radio = 2.0;
                    ondas[i].radioMax = 14.0;
                    ondas[i].velocidad = 0.6;
                    ondas[i].opacidad = 0.85;
                    break;
                }
            }
        }

        private int obtenerCantidadGotas(double mm) {
            if (mm <= 0.05) {
                return 0;
            } else if (mm < 2.5) {
                return 34;
            } else if (mm < 7.6) {
                return 80;
            } else if (mm < 30.0) {
                return 145;
            } else {
                return 220;
            }
        }

        private float obtenerGrosor(double mm) {
            if (mm < 2.5) {
                return 1.0f;
            } else if (mm < 7.6) {
                return 1.7f;
            } else if (mm < 30.0) {
                return 2.5f;
            } else {
                return 3.4f;
            }
        }

        private double obtenerViento(double mm) {
            if (mm <= 0.05) {
                return 0.0;
            } else if (mm < 2.5) {
                return 0.35;
            } else if (mm < 7.6) {
                return 1.3;
            } else if (mm < 30.0) {
                return 3.0;
            } else {
                return 5.0;
            }
        }

        private double obtenerVelocidad(double mm) {
            if (mm < 2.5) {
                return 4.6;
            } else if (mm < 7.6) {
                return 7.8;
            } else if (mm < 30.0) {
                return 11.8;
            } else {
                return 16.0;
            }
        }

        private double obtenerLargo(double mm) {
            if (mm < 2.5) {
                return 8.5;
            } else if (mm < 7.6) {
                return 15.0;
            } else if (mm < 30.0) {
                return 23.0;
            } else {
                return 32.0;
            }
        }

        private Color mezclarColor(Color a, Color b, double t) {
            double f = Math.max(0.0, Math.min(1.0, t));
            int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * f);
            int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * f);
            int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * f);
            return new Color(r, g, bl);
        }

        private double px(double xBot, double t, double vpX) {
            return vpX + t * (xBot - vpX);
        }

        private double py(double t, double vpY, double yBot) {
            return vpY + t * (yBot - vpY);
        }

        private boolean esCiudadCostera() {
            return "Paita".equalsIgnoreCase(ciudad)
                    || "Colán".equalsIgnoreCase(ciudad)
                    || "Pariñas (Talara)".equalsIgnoreCase(ciudad)
                    || "Máncora".equalsIgnoreCase(ciudad)
                    || "Los Órganos".equalsIgnoreCase(ciudad)
                    || "Lobitos".equalsIgnoreCase(ciudad)
                    || "Sechura".equalsIgnoreCase(ciudad);
        }

        private boolean esCiudadSerranaOVerde() {
            return "Tambogrande".equalsIgnoreCase(ciudad)
                    || "Las Lomas".equalsIgnoreCase(ciudad)
                    || "Chulucanas".equalsIgnoreCase(ciudad)
                    || "Morropón".equalsIgnoreCase(ciudad)
                    || "Ayabaca".equalsIgnoreCase(ciudad)
                    || "Frías".equalsIgnoreCase(ciudad)
                    || "Huancabamba".equalsIgnoreCase(ciudad)
                    || "Canchaque".equalsIgnoreCase(ciudad)
                    || "Huarmaca".equalsIgnoreCase(ciudad);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            if (w <= 60 || h <= 60) {
                g2.dispose();
                return;
            }

            double escenaW = w - 168.0;
            double vpX = escenaW * 0.50;
            double vpY = h * 0.40;
            double yBot = h - 14.0;
            double xRoadL = escenaW * 0.25;
            double xRoadR = escenaW * 0.75;
            double xWalkL = escenaW * 0.08;
            double xWalkR = escenaW * 0.92;
            double curbHMax = 12.0;

            dibujarCieloYDetalleCiudad(g2, w, h, escenaW, vpY);
            dibujarCalleYVeredas3D(g2, vpX, vpY, yBot, xRoadL, xRoadR, xWalkL, xWalkR, curbHMax);
            dibujarGotas3D(g2, false);
            dibujarEdificiosYPostes3D(g2, vpX, vpY, yBot, xRoadL, xRoadR, xWalkL, xWalkR, curbHMax);
            dibujarAguaEnSuelo3D(g2, vpX, vpY, yBot, xRoadL, xRoadR, xWalkL, xWalkR, curbHMax);
            dibujarOndas3D(g2);
            dibujarReglaNivelSuelo(g2, yBot, curbHMax);
            dibujarGotas3D(g2, true);
            dibujarBidonPluviometro3D(g2, w, h);
            dibujarHUDCiudad3D(g2, escenaW);

            g2.dispose();
        }

        private void dibujarCieloYDetalleCiudad(Graphics2D g2, int w, int h, double escenaW, double vpY) {
            Color cieloSup;
            Color cieloInf;
            Color colorNube;

            if (mmIntensidadSuavizada <= 0.05) {
                cieloSup = new Color(56, 152, 236);
                cieloInf = new Color(186, 230, 253);
                colorNube = new Color(255, 255, 255, 220);
            } else if (mmIntensidadSuavizada < 7.6) {
                double t = mmIntensidadSuavizada / 7.6;
                cieloSup = mezclarColor(new Color(96, 145, 190), new Color(71, 85, 105), t);
                cieloInf = mezclarColor(new Color(195, 218, 238), new Color(148, 163, 184), t);
                colorNube = new Color(203, 213, 225, 225);
            } else if (mmIntensidadSuavizada < 30.0) {
                double t = (mmIntensidadSuavizada - 7.6) / 22.4;
                cieloSup = mezclarColor(new Color(51, 65, 85), new Color(22, 32, 50), t);
                cieloInf = mezclarColor(new Color(100, 116, 139), new Color(51, 65, 85), t);
                colorNube = new Color(90, 105, 125, 230);
            } else {
                cieloSup = new Color(11, 18, 34);
                cieloInf = new Color(30, 41, 59);
                colorNube = new Color(51, 65, 85, 238);
            }

            GradientPaint gradCielo = new GradientPaint(0, 0, cieloSup, 0, (float) vpY, cieloInf);
            g2.setPaint(gradCielo);
            g2.fillRect(0, 0, w, (int) Math.ceil(vpY) + 12);

            for (int i = 0; i < nubes.length; i++) {
                Nube n = nubes[i];
                g2.setColor(colorNube);
                double nh = n.ancho * 0.40;
                g2.fill(new Ellipse2D.Double(n.x, n.y, n.ancho * 0.56, nh));
                g2.fill(new Ellipse2D.Double(n.x + n.ancho * 0.22, n.y - nh * 0.30, n.ancho * 0.54, nh * 1.12));
                g2.fill(new Ellipse2D.Double(n.x + n.ancho * 0.45, n.y + nh * 0.08, n.ancho * 0.45, nh * 0.85));
            }

            if (relampagoAlpha > 0.01) {
                g2.setColor(new Color(240, 248, 255, Math.min(175, (int) (relampagoAlpha * 165))));
                g2.fillRect(0, 0, w, (int) Math.ceil(vpY));
                if (relampagoAlpha > 0.35) {
                    Stroke prev = g2.getStroke();
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.setColor(new Color(254, 249, 195, Math.min(255, (int) (relampagoAlpha * 255))));
                    Path2D.Double rayo = new Path2D.Double();
                    rayo.moveTo(rayoX, 18);
                    rayo.lineTo(rayoX - 12, 54);
                    rayo.lineTo(rayoX + 6, 58);
                    rayo.lineTo(rayoX - 16, 102);
                    g2.draw(rayo);
                    g2.setStroke(prev);
                }
            }

            if (esCiudadCostera()) {
                GradientPaint mar = new GradientPaint(0, (float) (vpY - 26), new Color(3, 105, 161), 0, (float) vpY, new Color(14, 165, 233));
                g2.setPaint(mar);
                g2.fillRect(0, (int) (vpY - 26), (int) escenaW + 10, 28);
                g2.setColor(new Color(224, 242, 254, 175));
                for (int i = 0; i < 6; i++) {
                    double wx = ((i * 75.0 + faseAgua * 10.0) % escenaW);
                    g2.draw(new Line2D.Double(wx, vpY - 14 + (i % 2) * 5, wx + 22, vpY - 14 + (i % 2) * 5));
                }
                g2.setColor(new Color(234, 179, 8, 190));
                g2.fillRect(0, (int) (vpY - 3), (int) escenaW + 10, 5);
            } else if (esCiudadSerranaOVerde()) {
                g2.setColor(new Color(21, 128, 61));
                Path2D.Double cerros = new Path2D.Double();
                cerros.moveTo(0, vpY);
                cerros.curveTo(escenaW * 0.22, vpY - 46, escenaW * 0.42, vpY - 18, escenaW * 0.62, vpY - 42);
                cerros.curveTo(escenaW * 0.80, vpY - 56, escenaW * 0.92, vpY - 20, escenaW + 10, vpY);
                cerros.closePath();
                g2.fill(cerros);

                g2.setColor(new Color(22, 163, 74));
                for (int i = 0; i < 7; i++) {
                    double ax = 30.0 + i * (escenaW / 7.5);
                    g2.fill(new Ellipse2D.Double(ax, vpY - 22, 26, 22));
                }
            } else {
                g2.setColor(new Color(14, 165, 233, 215));
                g2.fillRect(0, (int) (vpY - 20), (int) escenaW + 10, 20);
                g2.setColor(new Color(148, 163, 184));
                g2.fillRect(0, (int) (vpY - 26), (int) escenaW + 10, 7);
                for (int i = 0; i < 6; i++) {
                    double bx = i * (escenaW / 5.5) + 12.0;
                    g2.setColor(new Color(100, 116, 139));
                    g2.fillRect((int) bx, (int) (vpY - 20), 12, 18);
                }
            }

            g2.setColor(new Color(71, 85, 105));
            g2.fillRect(0, (int) vpY, w, h - (int) vpY);
        }

        private void dibujarCalleYVeredas3D(Graphics2D g2, double vpX, double vpY, double yBot, double xRoadL, double xRoadR, double xWalkL, double xWalkR, double curbHMax) {
            Path2D.Double pista = new Path2D.Double();
            pista.moveTo(px(xRoadL, 0.0, vpX), py(0.0, vpY, yBot));
            pista.lineTo(px(xRoadR, 0.0, vpX), py(0.0, vpY, yBot));
            pista.lineTo(xRoadR, yBot);
            pista.lineTo(xRoadL, yBot);
            pista.closePath();
            g2.setColor(new Color(51, 65, 85));
            g2.fill(pista);

            g2.setColor(new Color(250, 204, 21, 210));
            for (int i = 0; i < 7; i++) {
                double t1 = i * 0.14 + 0.05;
                double t2 = t1 + 0.08;
                if (t2 > 1.0) {
                    continue;
                }
                double w1 = Math.max(1.5, 4.0 * t1);
                double w2 = Math.max(2.0, 4.5 * t2);
                Path2D.Double linea = new Path2D.Double();
                linea.moveTo(vpX - w1, py(t1, vpY, yBot));
                linea.lineTo(vpX + w1, py(t1, vpY, yBot));
                linea.lineTo(vpX + w2, py(t2, vpY, yBot));
                linea.lineTo(vpX - w2, py(t2, vpY, yBot));
                linea.closePath();
                g2.fill(linea);
            }

            Path2D.Double veredaIzq = new Path2D.Double();
            veredaIzq.moveTo(px(xWalkL, 0.0, vpX), py(0.0, vpY, yBot));
            veredaIzq.lineTo(px(xRoadL, 0.0, vpX), py(0.0, vpY, yBot));
            veredaIzq.lineTo(xRoadL, yBot - curbHMax);
            veredaIzq.lineTo(xWalkL, yBot - curbHMax);
            veredaIzq.closePath();
            g2.setColor(new Color(203, 213, 225));
            g2.fill(veredaIzq);

            Path2D.Double bordeVeredaIzq = new Path2D.Double();
            bordeVeredaIzq.moveTo(px(xRoadL, 0.0, vpX), py(0.0, vpY, yBot));
            bordeVeredaIzq.lineTo(xRoadL, yBot - curbHMax);
            bordeVeredaIzq.lineTo(xRoadL, yBot);
            bordeVeredaIzq.lineTo(px(xRoadL, 0.0, vpX), py(0.0, vpY, yBot));
            bordeVeredaIzq.closePath();
            g2.setColor(new Color(148, 163, 184));
            g2.fill(bordeVeredaIzq);

            Path2D.Double veredaDer = new Path2D.Double();
            veredaDer.moveTo(px(xRoadR, 0.0, vpX), py(0.0, vpY, yBot));
            veredaDer.lineTo(px(xWalkR, 0.0, vpX), py(0.0, vpY, yBot));
            veredaDer.lineTo(xWalkR, yBot - curbHMax);
            veredaDer.lineTo(xRoadR, yBot - curbHMax);
            veredaDer.closePath();
            g2.setColor(new Color(203, 213, 225));
            g2.fill(veredaDer);

            Path2D.Double bordeVeredaDer = new Path2D.Double();
            bordeVeredaDer.moveTo(px(xRoadR, 0.0, vpX), py(0.0, vpY, yBot));
            bordeVeredaDer.lineTo(xRoadR, yBot - curbHMax);
            bordeVeredaDer.lineTo(xRoadR, yBot);
            bordeVeredaDer.lineTo(px(xRoadR, 0.0, vpX), py(0.0, vpY, yBot));
            bordeVeredaDer.closePath();
            g2.setColor(new Color(148, 163, 184));
            g2.fill(bordeVeredaDer);
        }

        private void dibujarEdificiosYPostes3D(Graphics2D g2, double vpX, double vpY, double yBot, double xRoadL, double xRoadR, double xWalkL, double xWalkR, double curbHMax) {
            boolean lucesEncendidas = mmIntensidadSuavizada >= 2.5;
            int[] ordenDibujo = {0, 3, 1, 4, 2, 5};

            for (int k = 0; k < ordenDibujo.length; k++) {
                Edificio3D ed = edificios[ordenDibujo[k]];
                double t1 = ed.profundidad - 0.16;
                double t2 = ed.profundidad + 0.06;
                double ySuelo1 = py(t1, vpY, yBot) - curbHMax * t1;
                double ySuelo2 = py(t2, vpY, yBot) - curbHMax * t2;
                double alt1 = ed.alturaBase * (0.35 + t1 * 0.65);
                double alt2 = ed.alturaBase * (0.35 + t2 * 0.65);

                if (ed.izquierda) {
                    double xCalle1 = px(xWalkL + (xRoadL - xWalkL) * 0.35, t1, vpX);
                    double xCalle2 = px(xWalkL + (xRoadL - xWalkL) * 0.35, t2, vpX);
                    double xExt2 = Math.max(4.0, xCalle2 - 68.0 * (0.4 + t2 * 0.6));

                    Path2D.Double ladoCalle = new Path2D.Double();
                    ladoCalle.moveTo(xCalle1, ySuelo1);
                    ladoCalle.lineTo(xCalle2, ySuelo2);
                    ladoCalle.lineTo(xCalle2, ySuelo2 - alt2);
                    ladoCalle.lineTo(xCalle1, ySuelo1 - alt1);
                    ladoCalle.closePath();
                    g2.setColor(ed.colorLateral);
                    g2.fill(ladoCalle);

                    Path2D.Double frente = new Path2D.Double();
                    frente.moveTo(xExt2, ySuelo2);
                    frente.lineTo(xCalle2, ySuelo2);
                    frente.lineTo(xCalle2, ySuelo2 - alt2);
                    frente.lineTo(xExt2, ySuelo2 - alt2);
                    frente.closePath();
                    g2.setColor(ed.colorFachada);
                    g2.fill(frente);

                    g2.setColor(ed.colorTecho);
                    g2.fillRect((int) xExt2 - 3, (int) (ySuelo2 - alt2 - 7), (int) (xCalle2 - xExt2 + 6), 8);

                    for (int p = 0; p < ed.pisos; p++) {
                        double wy = ySuelo2 - alt2 + 14.0 + p * 28.0 * (0.5 + t2 * 0.5);
                        double wx = xExt2 + (xCalle2 - xExt2) * 0.25;
                        g2.setColor(lucesEncendidas ? new Color(253, 224, 71) : new Color(100, 116, 139));
                        g2.fillRect((int) wx, (int) wy, (int) Math.max(6, 13 * t2), (int) Math.max(6, 13 * t2));
                    }

                    double pxPoste = px(xRoadL - 4.0, t2, vpX);
                    double pyPoste = py(t2, vpY, yBot) - curbHMax * t2;
                    double altoPoste = 96.0 * (0.45 + t2 * 0.55);
                    g2.setColor(new Color(51, 65, 85));
                    g2.fillRect((int) pxPoste, (int) (pyPoste - altoPoste), 3, (int) altoPoste);
                    if (lucesEncendidas) {
                        g2.setColor(new Color(253, 224, 71, 90));
                        g2.fillOval((int) pxPoste - 8, (int) (pyPoste - altoPoste - 6), 20, 14);
                    }
                    g2.setColor(lucesEncendidas ? new Color(254, 240, 138) : new Color(226, 232, 240));
                    g2.fillOval((int) pxPoste - 2, (int) (pyPoste - altoPoste - 3), 8, 5);
                } else {
                    double xCalle1 = px(xRoadR + (xWalkR - xRoadR) * 0.65, t1, vpX);
                    double xCalle2 = px(xRoadR + (xWalkR - xRoadR) * 0.65, t2, vpX);
                    double xExt2 = xCalle2 + 68.0 * (0.4 + t2 * 0.6);

                    Path2D.Double ladoCalle = new Path2D.Double();
                    ladoCalle.moveTo(xCalle1, ySuelo1);
                    ladoCalle.lineTo(xCalle2, ySuelo2);
                    ladoCalle.lineTo(xCalle2, ySuelo2 - alt2);
                    ladoCalle.lineTo(xCalle1, ySuelo1 - alt1);
                    ladoCalle.closePath();
                    g2.setColor(ed.colorLateral);
                    g2.fill(ladoCalle);

                    Path2D.Double frente = new Path2D.Double();
                    frente.moveTo(xCalle2, ySuelo2);
                    frente.lineTo(xExt2, ySuelo2);
                    frente.lineTo(xExt2, ySuelo2 - alt2);
                    frente.lineTo(xCalle2, ySuelo2 - alt2);
                    frente.closePath();
                    g2.setColor(ed.colorFachada);
                    g2.fill(frente);

                    g2.setColor(ed.colorTecho);
                    g2.fillRect((int) xCalle2 - 3, (int) (ySuelo2 - alt2 - 7), (int) (xExt2 - xCalle2 + 6), 8);

                    for (int p = 0; p < ed.pisos; p++) {
                        double wy = ySuelo2 - alt2 + 14.0 + p * 28.0 * (0.5 + t2 * 0.5);
                        double wx = xCalle2 + (xExt2 - xCalle2) * 0.45;
                        g2.setColor(lucesEncendidas ? new Color(253, 224, 71) : new Color(100, 116, 139));
                        g2.fillRect((int) wx, (int) wy, (int) Math.max(6, 13 * t2), (int) Math.max(6, 13 * t2));
                    }

                    double pxPoste = px(xRoadR + 2.0, t2, vpX);
                    double pyPoste = py(t2, vpY, yBot) - curbHMax * t2;
                    double altoPoste = 96.0 * (0.45 + t2 * 0.55);
                    g2.setColor(new Color(51, 65, 85));
                    g2.fillRect((int) pxPoste, (int) (pyPoste - altoPoste), 3, (int) altoPoste);
                    if (lucesEncendidas) {
                        g2.setColor(new Color(253, 224, 71, 90));
                        g2.fillOval((int) pxPoste - 8, (int) (pyPoste - altoPoste - 6), 20, 14);
                    }
                    g2.setColor(lucesEncendidas ? new Color(254, 240, 138) : new Color(226, 232, 240));
                    g2.fillOval((int) pxPoste - 2, (int) (pyPoste - altoPoste - 3), 8, 5);
                }
            }
        }

        private void dibujarAguaEnSuelo3D(Graphics2D g2, double vpX, double vpY, double yBot, double xRoadL, double xRoadR, double xWalkL, double xWalkR, double curbHMax) {
            if (alturaSueloCmSuavizada <= 0.15) {
                return;
            }
            double waterHMax = (Math.min(150.0, alturaSueloCmSuavizada) / 15.0) * curbHMax;
            boolean desbordaVereda = alturaSueloCmSuavizada > 15.0;
            double xIzqBot = desbordaVereda ? xWalkL * 0.4 : xRoadL;
            double xDerBot = desbordaVereda ? xWalkR + 30.0 : xRoadR;

            Path2D.Double laminaAgua = new Path2D.Double();
            laminaAgua.moveTo(px(xIzqBot, 0.0, vpX), py(0.0, vpY, yBot));
            laminaAgua.lineTo(px(xDerBot, 0.0, vpX), py(0.0, vpY, yBot));
            laminaAgua.lineTo(xDerBot, yBot - waterHMax);
            laminaAgua.lineTo(xIzqBot, yBot - waterHMax);
            laminaAgua.closePath();

            Color cAguaSup = alturaSueloCmSuavizada >= 30.0 ? new Color(14, 116, 144, 175) : new Color(14, 165, 233, 150);
            Color cAguaInf = alturaSueloCmSuavizada >= 30.0 ? new Color(7, 89, 133, 210) : new Color(2, 132, 199, 190);
            GradientPaint gradAgua = new GradientPaint(0, (float) vpY, cAguaSup, 0, (float) yBot, cAguaInf);
            g2.setPaint(gradAgua);
            g2.fill(laminaAgua);

            Stroke prev = g2.getStroke();
            g2.setStroke(new BasicStroke(1.6f));
            g2.setColor(new Color(224, 242, 254, 185));
            Path2D.Double oleaje = new Path2D.Double();
            for (int i = 0; i <= 24; i++) {
                double frac = i / 24.0;
                double ox = xIzqBot + frac * (xDerBot - xIzqBot);
                double oy = (yBot - waterHMax) + Math.sin(frac * 10.0 + faseAgua) * 2.2;
                if (i == 0) {
                    oleaje.moveTo(ox, oy);
                } else {
                    oleaje.lineTo(ox, oy);
                }
            }
            g2.draw(oleaje);
            g2.setStroke(prev);
        }

        private void dibujarReglaNivelSuelo(Graphics2D g2, double yBot, double curbHMax) {
            double reglaX = 18.0;
            double escalaCmMax = 150.0;
            double reglaAltoPx = (escalaCmMax / 15.0) * curbHMax;
            double reglaYTop = yBot - reglaAltoPx;

            g2.setColor(new Color(15, 23, 42, 215));
            g2.fillRoundRect(8, (int) reglaYTop - 22, 128, (int) reglaAltoPx + 32, 8, 8);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString("NIVEL EN SUELO (0-150cm)", 13, (int) reglaYTop - 9);

            g2.setColor(new Color(226, 232, 240));
            g2.draw(new Line2D.Double(reglaX, reglaYTop, reglaX, yBot));

            int[] marcasCm = {0, 15, 30, 60, 100, 150};
            String[] notas = {"0cm Pista", "15cm Vereda", "30cm Casas", "60cm (600mm)", "100cm (1000mm)", "150cm (1500mm)"};
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            for (int i = 0; i < marcasCm.length; i++) {
                double pyMarca = yBot - (marcasCm[i] / 15.0) * curbHMax;
                g2.setColor(marcasCm[i] == 15 ? new Color(250, 204, 21) : marcasCm[i] >= 30 ? new Color(248, 113, 113) : Color.WHITE);
                g2.draw(new Line2D.Double(reglaX - 4, pyMarca, reglaX + 5, pyMarca));
                g2.drawString(notas[i], (float) (reglaX + 7), (float) (pyMarca + 3));
            }

            double pyActual = yBot - (Math.min(escalaCmMax, alturaSueloCmSuavizada) / 15.0) * curbHMax;
            g2.setColor(new Color(56, 189, 248));
            g2.fillRect((int) reglaX - 4, (int) pyActual - 2, 10, 4);
        }

        private void dibujarGotas3D(Graphics2D g2, boolean primerPlano) {
            int activas = obtenerCantidadGotas(mmIntensidadSuavizada);
            if (activas <= 0) {
                return;
            }
            Stroke prev = g2.getStroke();
            g2.setStroke(new BasicStroke(obtenerGrosor(mmIntensidadSuavizada), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double viento = obtenerViento(mmIntensidadSuavizada);
            double vel = Math.max(1.0, obtenerVelocidad(mmIntensidadSuavizada));

            for (int i = 0; i < activas; i++) {
                Gota gt = gotas[i];
                if (gt.enPrimerPlano != primerPlano) {
                    continue;
                }
                g2.setColor(primerPlano ? new Color(224, 242, 254, 200) : new Color(186, 230, 253, 145));
                double dx = (viento / vel) * gt.largo;
                g2.draw(new Line2D.Double(gt.x, gt.y, gt.x + dx, gt.y + gt.largo));
            }
            g2.setStroke(prev);
        }

        private void dibujarOndas3D(Graphics2D g2) {
            Stroke prev = g2.getStroke();
            g2.setStroke(new BasicStroke(1.3f));
            for (int i = 0; i < ondas.length; i++) {
                Onda o = ondas[i];
                if (!o.activa) {
                    continue;
                }
                int alpha = Math.max(0, Math.min(255, (int) (o.opacidad * 210)));
                g2.setColor(new Color(224, 242, 254, alpha));
                g2.draw(new Ellipse2D.Double(o.x - o.radio, o.y - o.radio * 0.28, o.radio * 2.0, o.radio * 0.56));
            }
            g2.setStroke(prev);
        }

        private void dibujarBidonPluviometro3D(Graphics2D g2, int w, int h) {
            int panelX = w - 166;
            g2.setColor(new Color(15, 23, 42, 210));
            g2.fillRect(panelX, 0, 166, h);
            g2.setColor(new Color(71, 85, 105));
            g2.drawLine(panelX, 0, panelX, h);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("PLUVIÓMETRO (300 mm)", panelX + 16, 22);

            double cx = w - 83.0;
            double rx = 36.0;
            double ry = 10.0;
            double yBase = h - 42.0;
            double alto = Math.max(130.0, h * 0.54);
            double yTop = yBase - alto;

            double propAgua = Math.min(1.0, mmBidonSuavizado / Math.max(1.0, escalaBidonSuavizada));
            double altAgua = propAgua * (alto - 6.0);
            double yAgua = yBase - altAgua;

            g2.setColor(new Color(226, 242, 255, 45));
            g2.fillRect((int) (cx - rx), (int) yTop, (int) (rx * 2.0), (int) alto);

            if (altAgua > 1.5) {
                Color cAgua = obtenerColorRiesgo(mmIntensidadObjetivo > 0.0 ? mmIntensidadObjetivo : mmBidonSuavizado);
                GradientPaint grad = new GradientPaint(
                        (float) (cx - rx), 0, new Color(cAgua.getRed(), cAgua.getGreen(), cAgua.getBlue(), 195),
                        (float) (cx + rx), 0, new Color(cAgua.darker().getRed(), cAgua.darker().getGreen(), cAgua.darker().getBlue(), 220)
                );
                Path2D.Double cuerpoAgua = new Path2D.Double();
                cuerpoAgua.moveTo(cx - rx + 2, yAgua);
                cuerpoAgua.lineTo(cx - rx + 2, yBase);
                cuerpoAgua.append(new Arc2D.Double(cx - rx + 2, yBase - ry, (rx - 2) * 2.0, ry * 2.0, 180, 180, Arc2D.OPEN), true);
                cuerpoAgua.lineTo(cx + rx - 2, yAgua);
                cuerpoAgua.closePath();
                g2.setPaint(grad);
                g2.fill(cuerpoAgua);

                Path2D.Double superficie = new Path2D.Double();
                double amp = mmIntensidadSuavizada > 0.05 ? 2.4 : 0.4;
                for (int i = 0; i <= 32; i++) {
                    double ang = (2.0 * Math.PI * i) / 32.0;
                    double sx = cx + (rx - 2) * Math.cos(ang);
                    double sy = yAgua + ry * Math.sin(ang) + Math.sin(ang * 3.0 + faseAgua) * amp;
                    if (i == 0) {
                        superficie.moveTo(sx, sy);
                    } else {
                        superficie.lineTo(sx, sy);
                    }
                }
                superficie.closePath();
                g2.setColor(mezclarColor(cAgua, Color.WHITE, 0.40));
                g2.fill(superficie);
                g2.setColor(Color.WHITE);
                g2.draw(superficie);
            }

            Stroke prev = g2.getStroke();
            g2.setStroke(new BasicStroke(1.8f));
            g2.setColor(new Color(226, 242, 255, 200));
            g2.draw(new Line2D.Double(cx - rx, yTop, cx - rx, yBase));
            g2.draw(new Line2D.Double(cx + rx, yTop, cx + rx, yBase));
            g2.draw(new Ellipse2D.Double(cx - rx, yTop - ry, rx * 2.0, ry * 2.0));
            g2.draw(new Arc2D.Double(cx - rx, yBase - ry, rx * 2.0, ry * 2.0, 180, 180, Arc2D.OPEN));
            g2.setStroke(prev);

            int pasoMm = escalaBidonSuavizada > 350.0 ? 100 : 50;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            for (int mm = 0; mm <= (int) escalaBidonSuavizada; mm += pasoMm) {
                double pyMarca = yBase - (mm / escalaBidonSuavizada) * (alto - 6.0);
                g2.setColor(new Color(203, 213, 225));
                g2.draw(new Line2D.Double(cx - rx, pyMarca, cx - rx + 8, pyMarca));
                g2.drawString(String.valueOf(mm), (float) (cx - rx - 23), (float) (pyMarca + 3));
            }

            if (umbralCritico <= escalaBidonSuavizada) {
                double yUmbral = yBase - (umbralCritico / escalaBidonSuavizada) * (alto - 6.0);
                Stroke sOld = g2.getStroke();
                g2.setStroke(new BasicStroke(2.2f));
                g2.setColor(new Color(239, 68, 68));
                g2.draw(new Arc2D.Double(cx - rx - 2, yUmbral - ry, (rx + 2) * 2.0, ry * 2.0, 180, 180, Arc2D.OPEN));
                g2.setStroke(sOld);

                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.setColor(new Color(254, 202, 202));
                g2.drawString("Umbral", (float) (cx + rx + 4), (float) (yUmbral + 3));
            }

            g2.setColor(new Color(56, 189, 248));
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            String txtExacto = formatoDecimal.format(mmBidonObjetivo) + " mm (" + formatoDecimal.format(mmBidonObjetivo) + " L/m²)";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txtExacto, (float) Math.max(panelX + 4, cx - fm.stringWidth(txtExacto) / 2.0), (float) (h - 14));
        }

        private void dibujarHUDCiudad3D(Graphics2D g2, double escenaW) {
            String estadoClima = obtenerNombreRiesgo(mmIntensidadObjetivo);
            String linea1 = ciudad + "  •  Lluvia: " + formatoDecimal.format(mmIntensidadObjetivo) + " mm/h (" + formatoDecimal.format(mmIntensidadObjetivo) + " L/m²)  •  " + estadoClima;
            String linea2 = fuenteHud + "  |  Bidón: " + formatoDecimal.format(mmBidonObjetivo) + " mm (" + formatoDecimal.format(mmBidonObjetivo) + " L/m²)  |  Suelo: " + formatoDecimal.format(alturaSueloCmObjetivo) + " cm";

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm1 = g2.getFontMetrics();
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fm2 = g2.getFontMetrics();
            int boxW = Math.min((int) escenaW - 16, Math.max(fm1.stringWidth(linea1), fm2.stringWidth(linea2)) + 28);

            g2.setColor(new Color(15, 23, 42, 185));
            g2.fillRoundRect(10, 10, boxW, 44, 10, 10);

            g2.setColor(obtenerColorRiesgo(mmIntensidadObjetivo));
            g2.fillOval(18, 25, 9, 9);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(linea1, 20, 26);

            g2.setColor(new Color(226, 232, 240));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(linea2, 32, 44);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FrmPluviometro().setVisible(true);
            }
        });
    }
}