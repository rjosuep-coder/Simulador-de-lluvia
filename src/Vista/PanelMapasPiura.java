package Vista;

import Logica.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author RJp
 */
public class PanelMapasPiura extends JPanel {

    private MotorPluviometro motor;
    private String fechaFiltro;
    private Consumer<String> alSeleccionarDistrito;
    private DecimalFormat formatoDecimal;

    private CardLayout cardLayout;
    private JPanel contenedorCards;
    private PanelProvincias panelProvincias;
    private PanelDistritos panelDistritos;
    private JButton btnVolverProvincias;
    private JLabel lblTituloProvincia;
    private JLabel lblFechaNivel2;

    private final String[] nombresProvincias = {
        "Talara", "Paita", "Sechura", "Sullana",
        "Piura", "Ayabaca", "Morropón", "Huancabamba"
    };

    private final double[][][] coordsProvincias = {
        {
            {0.16, 0.25}, {0.21, 0.28}, {0.24, 0.34}, {0.21, 0.40}, {0.24, 0.46},
            {0.10, 0.47}, {0.04, 0.43}, {0.06, 0.35}, {0.10, 0.29}
        },
        {
            {0.10, 0.47}, {0.21, 0.46}, {0.23, 0.52}, {0.26, 0.55}, {0.25, 0.61},
            {0.19, 0.65}, {0.11, 0.61}, {0.11, 0.54}, {0.08, 0.50}
        },
        {
            {0.19, 0.65}, {0.31, 0.69}, {0.45, 0.74}, {0.61, 0.79}, {0.45, 0.88},
            {0.37, 0.97}, {0.25, 0.93}, {0.15, 0.88}, {0.12, 0.82}, {0.17, 0.78},
            {0.24, 0.73}, {0.18, 0.66}
        },
        {
            {0.21, 0.28}, {0.36, 0.28}, {0.42, 0.24}, {0.44, 0.30}, {0.48, 0.34},
            {0.53, 0.37}, {0.50, 0.41}, {0.38, 0.46}, {0.30, 0.53}, {0.26, 0.60},
            {0.25, 0.52}, {0.21, 0.46}, {0.24, 0.40}
        },
        {
            {0.26, 0.60}, {0.30, 0.53}, {0.38, 0.46}, {0.50, 0.41}, {0.56, 0.42},
            {0.60, 0.46}, {0.58, 0.52}, {0.47, 0.56}, {0.48, 0.61}, {0.57, 0.68},
            {0.64, 0.74}, {0.61, 0.79}, {0.45, 0.74}, {0.31, 0.69}, {0.19, 0.65}
        },
        {
            {0.50, 0.35}, {0.57, 0.31}, {0.66, 0.34}, {0.76, 0.35}, {0.85, 0.40},
            {0.91, 0.49}, {0.85, 0.55}, {0.79, 0.57}, {0.70, 0.54}, {0.59, 0.52},
            {0.60, 0.46}, {0.53, 0.39}
        },
        {
            {0.47, 0.56}, {0.58, 0.52}, {0.70, 0.54}, {0.79, 0.57}, {0.78, 0.66},
            {0.79, 0.71}, {0.71, 0.72}, {0.64, 0.74}, {0.57, 0.68}, {0.48, 0.61}
        },
        {
            {0.79, 0.57}, {0.85, 0.55}, {0.91, 0.49}, {0.98, 0.53}, {0.92, 0.63},
            {0.94, 0.72}, {0.90, 0.78}, {0.93, 0.83}, {0.88, 0.85}, {0.81, 0.80},
            {0.73, 0.77}, {0.71, 0.72}, {0.79, 0.71}, {0.78, 0.66}
        }
    };

    private final String[][] distritosPorProvincia = {
        {"Pariñas (Talara)", "Máncora", "Los Órganos", "El Alto", "Lobitos", "La Brea"},
        {"Paita", "Colán", "La Huaca", "Vichayal", "Amotape", "Tamarindo", "Arenal"},
        {"Sechura", "Vice", "Bernal", "Cristo Nos Valga", "Bellavista de la Unión", "Rinconada Llicuar"},
        {"Sullana", "Bellavista", "Marcavelica", "Querecotillo", "Lancones", "Ignacio Escudero", "Miguel Checa", "Salitral"},
        {"Piura", "Castilla", "Veintiséis de Octubre", "Catacaos", "Tambogrande", "Las Lomas", "La Unión", "La Arena", "Cura Mori", "El Tallán"},
        {"Ayabaca", "Frías", "Sapillica", "Paimas", "Suyo", "Montero", "Pacaipampa", "Lagunas", "Jililí", "Sicchez"},
        {"Chulucanas", "Morropón", "La Matanza", "Buenos Aires", "Salitral (Morropón)", "Santo Domingo", "Chalaco", "Yamango", "San Juan de Bigote", "Santa Catalina de Mossa"},
        {"Huancabamba", "Canchaque", "Huarmaca", "El Carmen de la Frontera", "Sondorillo", "Sóndor", "San Miguel de El Faique", "Lalaquiz"}
    };

    private final double[][][] coordsDistritos = {
        {
            {0.11, 0.43}, {0.15, 0.28}, {0.13, 0.33}, {0.19, 0.34}, {0.09, 0.38}, {0.19, 0.44}
        },
        {
            {0.14, 0.60}, {0.12, 0.54}, {0.21, 0.60}, {0.13, 0.49}, {0.18, 0.49}, {0.21, 0.52}, {0.22, 0.56}
        },
        {
            {0.21, 0.78}, {0.23, 0.69}, {0.43, 0.77}, {0.36, 0.87}, {0.37, 0.74}, {0.30, 0.72}
        },
        {
            {0.30, 0.44}, {0.37, 0.42}, {0.28, 0.34}, {0.38, 0.36}, {0.40, 0.30}, {0.24, 0.42}, {0.27, 0.51}, {0.45, 0.37}
        },
        {
            {0.41, 0.54}, {0.52, 0.53}, {0.31, 0.55}, {0.49, 0.61}, {0.44, 0.47}, {0.53, 0.45}, {0.24, 0.65}, {0.32, 0.61}, {0.39, 0.66}, {0.50, 0.71}
        },
        {
            {0.81, 0.42}, {0.62, 0.51}, {0.66, 0.45}, {0.57, 0.41}, {0.56, 0.35}, {0.60, 0.47}, {0.82, 0.50}, {0.72, 0.52}, {0.65, 0.38}, {0.73, 0.38}
        },
        {
            {0.55, 0.57}, {0.64, 0.61}, {0.55, 0.64}, {0.64, 0.68}, {0.75, 0.70}, {0.63, 0.55}, {0.75, 0.58}, {0.75, 0.63}, {0.72, 0.67}, {0.70, 0.56}
        },
        {
            {0.85, 0.60}, {0.77, 0.74}, {0.87, 0.80}, {0.90, 0.54}, {0.81, 0.66}, {0.90, 0.65}, {0.83, 0.74}, {0.76, 0.69}
        }
    };

    public PanelMapasPiura(MotorPluviometro motor, String fechaFiltro, Consumer<String> alSeleccionarDistrito) {
        this.motor = motor;
        this.fechaFiltro = fechaFiltro != null ? fechaFiltro : "";
        this.alSeleccionarDistrito = alSeleccionarDistrito;
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        this.formatoDecimal = new DecimalFormat("0.00", simbolos);

        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        contenedorCards = new JPanel(cardLayout);

        panelProvincias = new PanelProvincias();

        JPanel wrapperNivel2 = new JPanel(new BorderLayout());
        JPanel barraSuperiorNivel2 = new JPanel(new BorderLayout(8, 0));
        barraSuperiorNivel2.setBackground(new Color(241, 245, 249));
        barraSuperiorNivel2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)),
                new EmptyBorder(6, 10, 6, 10)
        ));

        btnVolverProvincias = crearBotonDiscreto("← Volver a Provincias");
        btnVolverProvincias.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                volverAProvincias();
            }
        });

        lblTituloProvincia = new JLabel("Provincia de Piura", SwingConstants.CENTER);
        lblTituloProvincia.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTituloProvincia.setForeground(new Color(15, 41, 66));

        lblFechaNivel2 = new JLabel(obtenerEtiquetaFecha(), SwingConstants.RIGHT);
        lblFechaNivel2.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblFechaNivel2.setForeground(new Color(71, 85, 105));

        barraSuperiorNivel2.add(btnVolverProvincias, BorderLayout.WEST);
        barraSuperiorNivel2.add(lblTituloProvincia, BorderLayout.CENTER);
        barraSuperiorNivel2.add(lblFechaNivel2, BorderLayout.EAST);

        panelDistritos = new PanelDistritos();
        wrapperNivel2.add(barraSuperiorNivel2, BorderLayout.NORTH);
        wrapperNivel2.add(panelDistritos, BorderLayout.CENTER);

        contenedorCards.add(panelProvincias, "PROVINCIAS");
        contenedorCards.add(wrapperNivel2, "DISTRITOS");
        cardLayout.show(contenedorCards, "PROVINCIAS");

        add(contenedorCards, BorderLayout.CENTER);
    }

    public void setFechaFiltro(String fechaFiltro) {
        this.fechaFiltro = fechaFiltro != null ? fechaFiltro : "";
        if (lblFechaNivel2 != null) {
            lblFechaNivel2.setText(obtenerEtiquetaFecha());
        }
        refrescar();
    }

    public String getFechaFiltro() {
        return fechaFiltro;
    }

    public void volverAProvincias() {
        cardLayout.show(contenedorCards, "PROVINCIAS");
        panelProvincias.repaint();
    }

    public void refrescar() {
        panelProvincias.repaint();
        panelDistritos.repaint();
    }

    private String obtenerEtiquetaFecha() {
        String limpia = fechaFiltro != null ? fechaFiltro.trim() : "";
        if (limpia.length() > 10) {
            limpia = limpia.substring(0, 10);
        }
        return limpia.isEmpty() ? "Todas las fechas" : "Fecha: " + limpia;
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
        btn.setBorder(new EmptyBorder(6, 11, 6, 11));
        return btn;
    }

    private double obtenerMmDistritoEnFecha(String distrito) {
        if (motor == null || distrito == null) {
            return 0.0;
        }
        String fecha = fechaFiltro != null ? fechaFiltro.trim() : "";
        double mm = fecha.isEmpty()
                ? motor.calcularAcumuladoPorCiudad(distrito)
                : motor.calcularAcumuladoPorCiudadYFecha(distrito, fecha);
        if ("Piura".equals(distrito)) {
            mm += fecha.isEmpty()
                    ? motor.calcularAcumuladoPorCiudad("Piura - Miraflores")
                    : motor.calcularAcumuladoPorCiudadYFecha("Piura - Miraflores", fecha);
        } else if ("Castilla".equals(distrito)) {
            mm += fecha.isEmpty()
                    ? motor.calcularAcumuladoPorCiudad("Castilla - Corpac")
                    : motor.calcularAcumuladoPorCiudadYFecha("Castilla - Corpac", fecha);
        }
        return Math.round(mm * 100.0) / 100.0;
    }

    private double obtenerRiesgoMaximoProvincia(int indiceProvincia) {
        String[] distritos = distritosPorProvincia[indiceProvincia];
        double maxMm = 0.0;
        for (int i = 0; i < distritos.length; i++) {
            double mm = obtenerMmDistritoEnFecha(distritos[i]);
            if (mm > maxMm) {
                maxMm = mm;
            }
        }
        return maxMm;
    }

    private Color obtenerColorRiesgo(double mm) {
        if (mm <= 0.0) {
            return new Color(34, 197, 94);
        } else if (mm < 2.5) {
            return new Color(56, 189, 248);
        } else if (mm < 7.6) {
            return new Color(250, 204, 21);
        } else if (mm < 30.0) {
            return new Color(249, 115, 22);
        } else {
            return new Color(239, 68, 68);
        }
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

    private Point2D.Double calcularCentroidePoligono(Polygon p) {
        if (p.npoints == 0) {
            return new Point2D.Double(0, 0);
        }
        double sumX = 0.0;
        double sumY = 0.0;
        for (int i = 0; i < p.npoints; i++) {
            sumX += p.xpoints[i];
            sumY += p.ypoints[i];
        }
        return new Point2D.Double(sumX / p.npoints, sumY / p.npoints);
    }

    private class PanelProvincias extends JPanel {

        private int provinciaHover;

        public PanelProvincias() {
            this.provinciaHover = -1;
            setBackground(new Color(224, 242, 254));

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int w = getWidth();
                    int h = getHeight();
                    int nuevo = -1;
                    for (int i = 0; i < nombresProvincias.length; i++) {
                        if (construirPoligonoProvincia(i, w, h).contains(e.getPoint())) {
                            nuevo = i;
                            break;
                        }
                    }
                    if (nuevo != provinciaHover) {
                        provinciaHover = nuevo;
                        setCursor(provinciaHover >= 0 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    provinciaHover = -1;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int w = getWidth();
                    int h = getHeight();
                    for (int i = 0; i < nombresProvincias.length; i++) {
                        if (construirPoligonoProvincia(i, w, h).contains(e.getPoint())) {
                            abrirNivel2Provincia(i);
                            break;
                        }
                    }
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void abrirNivel2Provincia(int indiceProvincia) {
            panelDistritos.setProvincia(indiceProvincia);
            lblTituloProvincia.setText("Provincia de " + nombresProvincias[indiceProvincia] + " — Distritos");
            lblFechaNivel2.setText(obtenerEtiquetaFecha());
            cardLayout.show(contenedorCards, "DISTRITOS");
            panelDistritos.repaint();
        }

        private Polygon construirPoligonoProvincia(int indice, int w, int h) {
            double[][] pts = coordsProvincias[indice];
            int[] x = new int[pts.length];
            int[] y = new int[pts.length];
            for (int i = 0; i < pts.length; i++) {
                x[i] = (int) Math.round(pts[i][0] * w);
                y[i] = (int) Math.round(pts[i][1] * h);
            }
            return new Polygon(x, y, pts.length);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            if (w <= 40 || h <= 40) {
                g2.dispose();
                return;
            }

            GradientPaint gradMar = new GradientPaint(0, 0, new Color(14, 116, 144), (float) (w * 0.25), 0, new Color(56, 189, 248));
            g2.setPaint(gradMar);
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(241, 245, 249));
            Path2D.Double continente = new Path2D.Double();
            continente.moveTo(w * 0.16, 0);
            continente.lineTo(w, 0);
            continente.lineTo(w, h);
            continente.lineTo(w * 0.37, h);
            continente.lineTo(w * 0.12, h * 0.82);
            continente.lineTo(w * 0.08, h * 0.50);
            continente.lineTo(w * 0.04, h * 0.43);
            continente.lineTo(w * 0.16, 0);
            continente.closePath();
            g2.fill(continente);

            g2.setColor(new Color(224, 242, 254, 210));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("MAR DE", (int) (w * 0.02), (int) (h * 0.65));
            g2.drawString("GRAU", (int) (w * 0.03), (int) (h * 0.68));

            g2.setColor(new Color(100, 116, 139));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("TUMBES", (int) (w * 0.21), (int) (h * 0.19));
            g2.drawString("ECUADOR", (int) (w * 0.60), (int) (h * 0.22));
            g2.drawString("LAMBAYEQUE", (int) (w * 0.53), (int) (h * 0.94));

            for (int i = 0; i < nombresProvincias.length; i++) {
                Polygon p = construirPoligonoProvincia(i, w, h);
                double mmMax = obtenerRiesgoMaximoProvincia(i);
                Color cRiesgo = obtenerColorRiesgo(mmMax);
                int alpha = provinciaHover == i ? 225 : 175;
                g2.setColor(new Color(cRiesgo.getRed(), cRiesgo.getGreen(), cRiesgo.getBlue(), alpha));
                g2.fill(p);

                Stroke prev = g2.getStroke();
                g2.setStroke(new BasicStroke(provinciaHover == i ? 3.0f : 1.5f));
                g2.setColor(provinciaHover == i ? new Color(15, 23, 42) : Color.WHITE);
                g2.draw(p);
                g2.setStroke(prev);
            }

            for (int i = 0; i < nombresProvincias.length; i++) {
                Polygon p = construirPoligonoProvincia(i, w, h);
                Point2D.Double centro = calcularCentroidePoligono(p);
                double cx = centro.x;
                double cy = centro.y;
                if (i == 2) {
                    cx = w * 0.29;
                    cy = h * 0.81;
                } else if (i == 4) {
                    cx = w * 0.43;
                    cy = h * 0.59;
                } else if (i == 7) {
                    cx = w * 0.84;
                    cy = h * 0.69;
                }

                double mmMax = obtenerRiesgoMaximoProvincia(i);
                String nombre = nombresProvincias[i];
                String dato = formatoDecimal.format(mmMax) + " mm";

                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int bw = Math.max(fm.stringWidth(nombre), 52) + 12;
                int bh = 28;

                g2.setColor(new Color(15, 23, 42, provinciaHover == i ? 220 : 170));
                g2.fillRoundRect((int) (cx - bw / 2.0), (int) (cy - bh / 2.0), bw, bh, 8, 8);

                g2.setColor(Color.WHITE);
                g2.drawString(nombre, (float) (cx - fm.stringWidth(nombre) / 2.0), (float) (cy - 1));

                g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                g2.setColor(new Color(226, 232, 240));
                g2.drawString(dato, (float) (cx - fm.stringWidth(dato) / 2.0), (float) (cy + 10));
            }

            g2.setColor(new Color(15, 41, 66, 225));
            g2.fillRoundRect(12, 10, w - 24, 34, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("Mapa Departamental de Piura — " + obtenerEtiquetaFecha(), 22, 31);

            if (provinciaHover >= 0) {
                double mmHover = obtenerRiesgoMaximoProvincia(provinciaHover);
                String info = nombresProvincias[provinciaHover] + ": " + obtenerNombreRiesgo(mmHover) + " (" + formatoDecimal.format(mmHover) + " mm)";
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int iw = fm.stringWidth(info) + 18;
                g2.setColor(new Color(15, 23, 42, 215));
                g2.fillRoundRect(w - iw - 18, 15, iw, 24, 8, 8);
                g2.setColor(obtenerColorRiesgo(mmHover));
                g2.drawString(info, w - iw - 9, 31);
            }

            g2.dispose();
        }
    }

    private class PanelDistritos extends JPanel {

        private int provinciaIndice;
        private int distritoHover;
        private double fasePulso;
        private Timer timerPulso;

        public PanelDistritos() {
            this.provinciaIndice = 4;
            this.distritoHover = -1;
            this.fasePulso = 0.0;
            setBackground(new Color(241, 245, 249));

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int w = getWidth();
                    int h = getHeight();
                    String[] distritos = distritosPorProvincia[provinciaIndice];
                    int nuevo = -1;
                    for (int i = distritos.length - 1; i >= 0; i--) {
                        if (obtenerHitboxDistrito(i, w, h).contains(e.getPoint())) {
                            nuevo = i;
                            break;
                        }
                    }
                    if (nuevo != distritoHover) {
                        distritoHover = nuevo;
                        setCursor(distritoHover >= 0 ? new Cursor(Cursor.HAND_CURSOR) : new Cursor(Cursor.DEFAULT_CURSOR));
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    distritoHover = -1;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int w = getWidth();
                    int h = getHeight();
                    String[] distritos = distritosPorProvincia[provinciaIndice];
                    for (int i = distritos.length - 1; i >= 0; i--) {
                        if (obtenerHitboxDistrito(i, w, h).contains(e.getPoint())) {
                            if (alSeleccionarDistrito != null) {
                                alSeleccionarDistrito.accept(distritos[i]);
                            }
                            break;
                        }
                    }
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);

            timerPulso = new Timer(45, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isShowing()) {
                        fasePulso += 0.08;
                        if (fasePulso > Math.PI * 200.0) {
                            fasePulso = 0.0;
                        }
                        repaint();
                    }
                }
            });
            timerPulso.start();
        }

        public void setProvincia(int provinciaIndice) {
            this.provinciaIndice = provinciaIndice;
            this.distritoHover = -1;
        }

        private double[] calcularTransformacion(int w, int h) {
            double[][] pts = coordsProvincias[provinciaIndice];
            double minX = pts[0][0];
            double maxX = pts[0][0];
            double minY = pts[0][1];
            double maxY = pts[0][1];
            for (int i = 1; i < pts.length; i++) {
                if (pts[i][0] < minX) {
                    minX = pts[i][0];
                }
                if (pts[i][0] > maxX) {
                    maxX = pts[i][0];
                }
                if (pts[i][1] < minY) {
                    minY = pts[i][1];
                }
                if (pts[i][1] > maxY) {
                    maxY = pts[i][1];
                }
            }
            double spanX = Math.max(0.05, maxX - minX);
            double spanY = Math.max(0.05, maxY - minY);
            double padX = 52.0;
            double padY = 42.0;
            double availW = Math.max(80.0, w - padX * 2.0);
            double availH = Math.max(80.0, h - padY * 2.0);
            double scale = Math.min(availW / spanX, availH / spanY);
            double offsetX = padX + (availW - spanX * scale) / 2.0;
            double offsetY = padY + (availH - spanY * scale) / 2.0;
            return new double[]{minX, minY, scale, offsetX, offsetY};
        }

        private Polygon construirPoligonoAmpliado(int w, int h) {
            double[] t = calcularTransformacion(w, h);
            double[][] pts = coordsProvincias[provinciaIndice];
            int[] px = new int[pts.length];
            int[] py = new int[pts.length];
            for (int i = 0; i < pts.length; i++) {
                px[i] = (int) Math.round(t[3] + (pts[i][0] - t[0]) * t[2]);
                py[i] = (int) Math.round(t[4] + (pts[i][1] - t[1]) * t[2]);
            }
            return new Polygon(px, py, pts.length);
        }

        private Point2D.Double obtenerPosicionDistrito(int idxDistrito, int w, int h) {
            double[] t = calcularTransformacion(w, h);
            double[] coord = coordsDistritos[provinciaIndice][idxDistrito];
            double sx = t[3] + (coord[0] - t[0]) * t[2];
            double sy = t[4] + (coord[1] - t[1]) * t[2];
            return new Point2D.Double(sx, sy);
        }

        private RoundRectangle2D.Double obtenerHitboxDistrito(int idxDistrito, int w, int h) {
            Point2D.Double p = obtenerPosicionDistrito(idxDistrito, w, h);
            return new RoundRectangle2D.Double(p.x - 52, p.y - 22, 104, 38, 10, 10);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            if (w <= 40 || h <= 40) {
                g2.dispose();
                return;
            }

            if (provinciaIndice <= 2) {
                GradientPaint mar = new GradientPaint(0, 0, new Color(186, 230, 253), (float) (w * 0.45), 0, new Color(241, 245, 249));
                g2.setPaint(mar);
                g2.fillRect(0, 0, w, h);
            }

            Polygon poligonoProv = construirPoligonoAmpliado(w, h);
            double mmMaxProv = obtenerRiesgoMaximoProvincia(provinciaIndice);
            Color colorBase = obtenerColorRiesgo(mmMaxProv);

            g2.setColor(new Color(15, 23, 42, 28));
            Polygon sombra = new Polygon(poligonoProv.xpoints, poligonoProv.ypoints, poligonoProv.npoints);
            sombra.translate(5, 6);
            g2.fill(sombra);

            g2.setColor(new Color(colorBase.getRed(), colorBase.getGreen(), colorBase.getBlue(), 48));
            g2.fill(poligonoProv);

            Stroke prev = g2.getStroke();
            g2.setStroke(new BasicStroke(2.4f));
            g2.setColor(new Color(51, 65, 85));
            g2.draw(poligonoProv);
            g2.setStroke(prev);

            String[] distritos = distritosPorProvincia[provinciaIndice];
            double umbral = motor != null ? motor.getUmbralCriticoMm() : 15.0;

            for (int i = 0; i < distritos.length; i++) {
                if (i == distritoHover) {
                    continue;
                }
                dibujarMarcadorDistrito(g2, distritos[i], i, w, h, umbral, false);
            }

            if (distritoHover >= 0 && distritoHover < distritos.length) {
                dibujarMarcadorDistrito(g2, distritos[distritoHover], distritoHover, w, h, umbral, true);
            }

            g2.setColor(new Color(71, 85, 105));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
            g2.drawString("Haz clic en un distrito para abrir su simulación 3D", 14, h - 12);

            g2.dispose();
        }

        private void dibujarMarcadorDistrito(Graphics2D g2, String nombre, int idx, int w, int h, double umbral, boolean hover) {
            Point2D.Double pos = obtenerPosicionDistrito(idx, w, h);
            double mm = obtenerMmDistritoEnFecha(nombre);
            Color cRiesgo = obtenerColorRiesgo(mm);

            if (mm >= umbral) {
                Stroke prev = g2.getStroke();
                double prog = (fasePulso * 0.35) % 1.0;
                double rad = 8.0 + prog * 18.0;
                int alpha = Math.max(0, (int) ((1.0 - prog) * 190));
                g2.setStroke(new BasicStroke(2.0f));
                g2.setColor(new Color(220, 38, 38, alpha));
                g2.draw(new Ellipse2D.Double(pos.x - rad, pos.y - rad, rad * 2.0, rad * 2.0));
                g2.setStroke(prev);
            }

            String textoMm = formatoDecimal.format(mm) + " mm";
            g2.setFont(new Font("SansSerif", Font.BOLD, hover ? 11 : 10));
            FontMetrics fm = g2.getFontMetrics();
            int pillW = Math.max(fm.stringWidth(nombre), fm.stringWidth(textoMm)) + 16;
            int pillH = 26;
            int pillX = (int) Math.round(pos.x - pillW / 2.0);
            int pillY = (int) Math.round(pos.y - pillH - 7);

            if (pillX < 4) {
                pillX = 4;
            } else if (pillX + pillW > w - 4) {
                pillX = w - pillW - 4;
            }
            if (pillY < 4) {
                pillY = (int) Math.round(pos.y + 8);
            }

            g2.setColor(hover ? new Color(15, 23, 42, 235) : new Color(255, 255, 255, 232));
            g2.fillRoundRect(pillX, pillY, pillW, pillH, 9, 9);

            Stroke prev = g2.getStroke();
            g2.setStroke(new BasicStroke(hover ? 2.2f : 1.4f));
            g2.setColor(cRiesgo);
            g2.drawRoundRect(pillX, pillY, pillW, pillH, 9, 9);
            g2.setStroke(prev);

            g2.setColor(hover ? Color.WHITE : new Color(15, 23, 42));
            g2.drawString(nombre, pillX + (pillW - fm.stringWidth(nombre)) / 2, pillY + 12);

            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            fm = g2.getFontMetrics();
            g2.setColor(hover ? cRiesgo.brighter() : cRiesgo.darker());
            g2.drawString(textoMm, pillX + (pillW - fm.stringWidth(textoMm)) / 2, pillY + 23);

            int rDot = hover ? 7 : 5;
            g2.setColor(cRiesgo);
            g2.fillOval((int) Math.round(pos.x - rDot), (int) Math.round(pos.y - rDot), rDot * 2, rDot * 2);
            g2.setColor(Color.WHITE);
            g2.drawOval((int) Math.round(pos.x - rDot), (int) Math.round(pos.y - rDot), rDot * 2, rDot * 2);
        }
    }
}