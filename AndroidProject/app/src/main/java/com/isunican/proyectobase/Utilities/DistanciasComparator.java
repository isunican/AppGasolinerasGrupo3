package com.isunican.proyectobase.Utilities;

import com.isunican.proyectobase.Model.Gasolinera;
import java.util.Comparator;

/**
 * Comparador
 *
 * @author Hamza Hamda
 */
public class DistanciasComparator implements Comparator<Gasolinera> {
    private double latitud;
    private double longitud;
    private boolean asc;

    /**
     * Constructor del comparador donde se le indica la latitud y longitud que se
     * van a utilizar para calcular la distancia con respecto a las gasolineras
     * y en base a dicha distancia establecer un orden. ademas se indica el tipo
     * de orden, ascendente o descendente.
     * @param latitud
     * @param longitud
     * @param asc
     */
    public DistanciasComparator(double latitud, double longitud, boolean asc) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.asc=asc;
    }

    @Override
    public int compare(Gasolinera g1, Gasolinera g2) {
        double distancia1 = CalculaDistancia.distanciaEntreDosCoordenadas(latitud, longitud, g1.getLatitud(), g1.getLongitud());
        double distancia2 = CalculaDistancia.distanciaEntreDosCoordenadas(latitud, longitud, g2.getLatitud(), g2.getLongitud());

        if (asc) {//orden ascendente
            if (distancia1 > distancia2) {
                return 1;
            } else {
                return -1;
            }
        } else {//orden descendente
            if (distancia1 > distancia2) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
