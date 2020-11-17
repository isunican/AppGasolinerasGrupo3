package com.isunican.proyectobase;

import com.isunican.proyectobase.Model.Gasolinera;
import com.isunican.proyectobase.Presenter.PresenterGasolineras;
import com.isunican.proyectobase.Views.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Clase de prueba donde se realizan las pruebas unitarias correspondiente a la clase
 * PresenterGasolineras.
 *
 * @author Corocotta
 */
public class PresenterGasolinerasTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    MainActivity ac;


    // Objeto de la clase Presentergasolineras para implementar los siguientes metodos Test
    private PresenterGasolineras pr;
    // Gasolineras para utilizarlas posteriormente
    private ArrayList<Gasolinera> gasolineras;
    private String ruta = "datos_test.txt";
    private FileInputStream fis;
    private FileOutputStream fos;
    private File archivo;

    @Before
    public void setUp() {
        archivo = new File(ruta);
        try {
            archivo.createNewFile();
        } catch (IOException e) {
        }

        pr = new PresenterGasolineras();//se inicializa el presenter
        gasolineras = new ArrayList<>();//se crea lista de de tipo Gasolina para probar
        /*Se anhaden varias gasolineras a la lista para pasarselas al presenter y utilizarlas en la pruebas*/
        gasolineras.add(new Gasolinera(0, "Castro", "Cantabria", "Calle 1",
                0.91, 1.06, 1.11, 1.31, 1.01, "CEPSA"));
        gasolineras.add(new Gasolinera(0, "Viesgo", "Cantabria", "Calle 2",
                1.01, 1.09, 1.11, 1.26, 1.16, "Respol"));
        gasolineras.add(new Gasolinera(0, "Puente San Miguel", "Cantabria", "Calle 1",
                0.92, 1.12, 1.09, 1.26, 0.97, "Shell"));
        gasolineras.add(new Gasolinera(0, "Tanos", "Cantabria", "Calle 3",
                1.02, 1.21, 1.08, 1.35, 0.93, "Repsol"));
        gasolineras.add(new Gasolinera(0, "Ganzo", "Cantabria", "Calle 4",
                0.96, 0.99, 1.07, 1.15, 0.94, "CEPSA"));
        gasolineras.add(new Gasolinera(0, "Torrelavega", "Cantabria", "Calle 5",
                0.97, 1.11, 1.07, 1.10, 0.92, "Shell"));
        gasolineras.add(new Gasolinera(0, "Santander", "Cantabria", "Calle 6",
                0.90, 1.03, 1.07, 1.14, 0.97, "CEPSA"));
        pr.setGasolineras(gasolineras);

    }//setUp

    /*
     * Test para el metodo getPrecioGasolinera
     * Se comprobara si retorna correctamente el precio de cada tipo de combustible
     * de las gasolineras creadas anteriormente.
     */
    @Test
    public void getPrecioGasolinera() {
        try {
            assertEquals(0.0, pr.getPrecioCombustible(null, null), 0.000001);
            fail();
        } catch (NullPointerException e) {

        }

        assertEquals(0.91, pr.getPrecioCombustible("Gasóleo A", pr.getGasolineras().get(0)), 0.000001);

        assertEquals(1.06, pr.getPrecioCombustible("Gasolina 95", pr.getGasolineras().get(0)), 0.000001);

        assertEquals(1.11, pr.getPrecioCombustible("Gasolina 98", pr.getGasolineras().get(0)), 0.000001);

        assertEquals(1.31, pr.getPrecioCombustible("Biodiésel", pr.getGasolineras().get(0)), 0.000001);

        assertEquals(1.01, pr.getPrecioCombustible("Gasóleo Premium", pr.getGasolineras().get(0)), 0.000001);
    }

    @Test
    public void eliminarGasolinerasConPrecioNegativoTest() {
        //caso donde la cadena pasada esta vacia
        pr.eliminaGasolinerasConPrecioNegativo("");
        boolean noCambiaLista = gasolineras.size() == 7;
        //se comprueba que el metodo no realiza ningun cambio en la lista
        assertTrue(noCambiaLista);

        //caso donde una gasolinera tiene el gasoleao con precio negativo
        Gasolinera gasolineraConPrecioGasoleoNegativo = new Gasolinera(0, "Torrelavega", "Cantabria", "Calle 1",
                -0.3, 1.06, 1.07, 1.26, 0.97, "CEPSA");
        pr.eliminaGasolinerasConPrecioNegativo("Gasóleo A");
        //se comprueba que despues de ejecutar el metodo la gasoliner con precio negativo ya no esta en la lista
        assertFalse(gasolineras.contains(gasolineraConPrecioGasoleoNegativo));

        //caso donde una gasolinera tiene el gasolina 95 con precio negativo
        Gasolinera gasolineraConPrecioGasolina95Negativo = new Gasolinera(0, "Torrelavega", "Cantabria", "Calle 1",
                0.94, -0.91, 1.07, 1.26, 0.97, "CEPSA");
        pr.eliminaGasolinerasConPrecioNegativo("Gasolina 95");
        //se comprueba que despues de ejecutar el metodo la gasoliner con precio negativo ya no esta en la lista
        assertFalse(gasolineras.contains(gasolineraConPrecioGasolina95Negativo));

        //caso donde una gasolinera tiene la gasolina 98 con precio negativo
        Gasolinera gasolineraConPrecioGasolina98Negativo = new Gasolinera(0, "Torrelavega", "Cantabria", "Calle 1",
                0.94, -2.01, 1.07, 1.26, 0.97, "CEPSA");
        pr.eliminaGasolinerasConPrecioNegativo("Gasolina 98");
        //se comprueba que despues de ejecutar el metodo la gasoliner con precio negativo ya no esta en la lista
        assertFalse(gasolineras.contains(gasolineraConPrecioGasolina98Negativo));

        //caso donde una gasolinera tiene el biodiesel con precio negativo
        Gasolinera gasolineraConPrecioBiodieselNegativo = new Gasolinera(0, "Torrelavega", "Cantabria", "Calle 1",
                0.94, -4, 1.07, 1.26, 0.97, "CEPSA");
        pr.eliminaGasolinerasConPrecioNegativo("Biodiésel");
        //se comprueba que despues de ejecutar el metodo la gasoliner con precio negativo ya no esta en la lista
        assertFalse(gasolineras.contains(gasolineraConPrecioBiodieselNegativo));

        //caso donde una gasolinera tiene el gasoleao premium con precio negativo
        Gasolinera gasolineraConPrecioGasoleoPremiumNegativo = new Gasolinera(0, "Torrelavega", "Cantabria", "Calle 1",
                0.94, -0.0001, 1.07, 1.26, 0.97, "CEPSA");
        pr.eliminaGasolinerasConPrecioNegativo("Gasóleo Premium");
        //se comprueba que despues de ejecutar el metodo la gasoliner con precio negativo ya no esta en la lista
        assertFalse(gasolineras.contains(gasolineraConPrecioGasoleoPremiumNegativo));

        //caso donde el parametro pasado es nulo
        String parametro = null;
        //se comprueba que se lanza la excepcion NullPointerException
        try {
            pr.eliminaGasolinerasConPrecioNegativo(parametro);
            fail();
        } catch (NullPointerException e) {
        }
        //se comprueba que el metodo no realiza ningun cambio en la lista
        assertTrue(noCambiaLista);

    }//eliminarGasolinerasConPrecionNegativoTest


    /*
     * Test para el metodo ordenarGasolineras
     * Se comprobara si funciona correctamente ascender y descender dependiendo
     * del precio de las gasolineras creadas anteriormente.
     */
    @Test
    public void ordenarGasolineras() {


        try {
            pr.ordernarGasolineras(true, null);
            fail();
        } catch (NullPointerException e) {

        }

        pr.ordernarGasolineras(true, "Gasóleo A");
        assertEquals(0.90, pr.getGasolineras().get(0).getGasoleoA(), 0.00001);

        pr.ordernarGasolineras(false, "Gasóleo A");
        assertEquals(1.02, pr.getGasolineras().get(0).getGasoleoA(), 0.00001);

        pr.ordernarGasolineras(true, "Gasolina 95");
        assertEquals(0.99, pr.getGasolineras().get(0).getGasolina95(), 0.00001);

        pr.ordernarGasolineras(false, "Gasolina 95");
        assertEquals(1.21, pr.getGasolineras().get(0).getGasolina95(), 0.00001);

        pr.ordernarGasolineras(true, "Gasolina 98");
        assertEquals(1.07, pr.getGasolineras().get(0).getGasolina98(), 0.00001);

        pr.ordernarGasolineras(false, "Gasolina 98");
        assertEquals(1.11, pr.getGasolineras().get(0).getGasolina98(), 0.00001);

        pr.ordernarGasolineras(true, "Biodiésel");
        assertEquals(1.10, pr.getGasolineras().get(0).getBiodiesel(), 0.00001);

        pr.ordernarGasolineras(false, "Biodiésel");
        assertEquals(1.35, pr.getGasolineras().get(0).getBiodiesel(), 0.00001);

        pr.ordernarGasolineras(true, "Gasóleo Premium");
        assertEquals(0.92, pr.getGasolineras().get(0).getGasoleoPremium(), 0.00001);

        pr.ordernarGasolineras(false, "Gasóleo Premium");
        assertEquals(1.16, pr.getGasolineras().get(0).getGasoleoPremium(), 0.00001);
    }

    /**
     * @author Hamza Hamda
     */
    @Test
    public void leerCombustiblePorDefectoTest() {

        FileInputStream fisVacio;

        File archivo2 = new File("archivo2.txt");
        FileWriter myWriter = null;
        FileWriter myWriter2 = null;

        try {

            myWriter = new FileWriter(archivo);
            myWriter.write("Gasolina 98");
            myWriter.close();

            archivo2.createNewFile();
            myWriter2 = new FileWriter("archivo2.txt");

        } catch (IOException e) {
            fail("" + e.getStackTrace());
        }


        try {
            fis = new FileInputStream(ruta);
            fisVacio = new FileInputStream(ruta);
            when(ac.openFileInput(ruta)).thenReturn(fis);
            when(ac.openFileInput("err.txt")).thenThrow(new FileNotFoundException());
            when(ac.openFileInput("archivo2.txt")).thenReturn(fisVacio);
        } catch (IOException e) {
            fail("" + e.getStackTrace());
        }

        //UT.1a
        String combusitble = null;
        try {
            combusitble = pr.lecturaCombustiblePorDefecto(ac, ruta);
        } catch (IOException e) {
            fail();
        }
        assertEquals("Gasolina 98", combusitble);

        //UT.1b
        try {
            pr.lecturaCombustiblePorDefecto(ac, "err.txt");
            fail();
        } catch (IOException e) {
        }

        //UT.1c

        try {
            pr.lecturaCombustiblePorDefecto(ac, "archivo2.txt");
        } catch (IOException e) {
            fail();
        }


        //UT.1d
        try {
            myWriter2.write("Gas");
            myWriter2.close();
            pr.lecturaCombustiblePorDefecto(ac, "archivo2.txt");
        } catch (IOException e) {
            fail();
        }


        archivo.delete();
        archivo2.delete();
    }

    @Test
    public void escrituraCombustiblePorDefectoTest() {

        try {
            fis = new FileInputStream(ruta);
            fos = new FileOutputStream(ruta);
            when(ac.openFileOutput(ruta, android.content.Context.MODE_PRIVATE)).thenReturn(fos);
            when(ac.openFileOutput("err.txt", android.content.Context.MODE_PRIVATE)).thenThrow(new FileNotFoundException());
            when(ac.openFileInput(ruta)).thenReturn(fis);
        } catch (IOException e) {
        }

        //UT.2a
        try {
            pr.escrituraCombustiblePorDefecto("Gasóleo A",ac,ruta);
        }catch (IOException e){
        }
        String combusitble = null;
        try {
            combusitble = pr.lecturaCombustiblePorDefecto(ac, ruta);
        } catch (IOException e) {
            fail();
        }
        assertEquals("Gasóleo A", combusitble);

        //UT.2b
        try {
            pr.escrituraCombustiblePorDefecto("Gasóleo A", ac, "err.txt");
            fail();
        }catch (IOException e){
        }
    }

}//class

