package com.isunican.proyectobase.Views;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.isunican.proyectobase.Presenter.*;
import com.isunican.proyectobase.Model.*;
import com.isunican.proyectobase.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/*
------------------------------------------------------------------
    Vista principal

    Presenta los datos de las gasolineras en formato lista.

------------------------------------------------------------------
*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PRECIO_ASC = "Precio (asc)";
    public static final String FLECHA_ARRIBA = "flecha_arriba";
    public static final String DRAWABLE = "drawable";
    public static final String CANCELAR = "Cancelar";
    public static final String FICHERO = "datos.txt";
    public static final String FICHERO_UBICACION = "datosUbicacion.txt";

    PresenterGasolineras presenterGasolineras;

    // Vista de lista y adaptador para cargar datos en ella
    ListView listViewGasolineras;
    ArrayAdapter<Gasolinera> adapter;
    // Swipe and refresh (para recargar la lista con un swipe)
    SwipeRefreshLayout mSwipeRefreshLayout;

    //Botones de filtro y ordenacion
    Button buttonFiltros;
    Button buttonOrden;
    ImageButton config;
    ImageView menu;
    Button buttonConfig;
    Button buttonUbicacion;

    TextInputLayout textInputLatitud;
    TextInputLayout textInputLongitud;
    TextView labelLongitud;
    TextView labelLatitud;
    Button buttonCancelar;
    Button buttonEstablecer;




    //DRAWER LAYOUT
    DrawerLayout drawerLayout;

    /*Variables para modificar filtros y ordenaciones*/
    //orden ascendente por defecto
    final String[] buttonString = {PRECIO_ASC};
    final String[] idImgOrdernPrecio = {FLECHA_ARRIBA};

    String tipoCombustible = "Gasóleo A"; //Por defecto

    // Coordenadas por defecto
    String latitud = "43.350223552917";
    String longitud = "-4.052258920907";
    String coordenada = latitud + " " + longitud;
    String newCoordenada;


    boolean esAsc = true; //Por defecto ascendente

    Activity ac = this;


    /**
     * onCreate
     * <p>
     * Crea los elementos que conforman la actividad
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.presenterGasolineras = new PresenterGasolineras();

        try {
            //Lectura inicial del tipo de combustible por defecto
            tipoCombustible = presenterGasolineras.lecturaCombustiblePorDefecto(this, FICHERO);
        } catch(Exception e) {
            try {
                presenterGasolineras.escrituraCombustiblePorDefecto("Gasóleo A", this, FICHERO);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }catch (IOException exc){
                exc.printStackTrace();
            } catch (PresenterGasolineras.CombustibleNoExistente combustibleNoExistente) {
                combustibleNoExistente.printStackTrace();
            }
        }

        try {
            tipoCombustible = presenterGasolineras.lecturaCombustiblePorDefecto(this, FICHERO);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            //Lectura inicial de las coordenadas por defecto
            coordenada = presenterGasolineras.lecturaCoordenadaPorDefecto(this, FICHERO);

        } catch(Exception e) {
            try {
                presenterGasolineras.escrituraCoordenadaPorDefecto("43.350223552917 -4.052258920907", this, FICHERO);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }catch (IOException exc){
                exc.printStackTrace();
            } catch (PresenterGasolineras.CoordenadaNoExistente coordenadaNoExistente) {
                coordenadaNoExistente.printStackTrace();
            }
        }

        try {
            coordenada = presenterGasolineras.lecturaCoordenadaPorDefecto(this, FICHERO);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newCoordenada = coordenada;

        drawerLayout = findViewById(R.id.drawer_layout);

        // Muestra el logo en el actionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.por_defecto_mod);
        getSupportActionBar().hide();

        // Swipe and refresh
        // Al hacer swipe en la lista, lanza la tarea asíncrona de carga de datos
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> new CargaDatosGasolinerasTask(MainActivity.this).execute());

        // Al terminar de inicializar todas las variables
        // se lanza una tarea para cargar los datos de las gasolineras
        // Esto se ha de hacer en segundo plano definiendo una tarea asíncrona
        new CargaDatosGasolinerasTask(this).execute();

        //Añadir los listener a los botones

        buttonFiltros = findViewById(R.id.buttonFiltros);
        buttonOrden = findViewById(R.id.buttonOrden);
        config = findViewById(R.id.info);
        menu = findViewById(R.id.menuNav);
        buttonConfig = findViewById(R.id.btnConfiguracion);
        buttonUbicacion = findViewById(R.id.btnUbicacion);

        buttonFiltros.setOnClickListener(this);
        buttonOrden.setOnClickListener(this);
        config.setOnClickListener(this);
        menu.setOnClickListener(this);
        buttonConfig.setOnClickListener(this);
        buttonUbicacion.setOnClickListener(this);

    }


    public void clickMenu() {
        openDrawer(drawerLayout);
    }

    public void clickConfiguracion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set the dialog title
        builder.setTitle("Configuración");
        // Specify the list array, the items to be selected by default (null for none),

        // Vista escondida del nuevo layout para los diferentes spinners a implementar para los filtros
        View mView = getLayoutInflater().inflate(R.layout.combustible_por_defecto_layout, null);

        final Spinner mSpinner = (Spinner) mView.findViewById(R.id.combustible_por_defecto);// New spinner object
        final TextView comb = mView.findViewById(R.id.porDefecto);
        try {
            comb.setText("Combustible actual: "+presenterGasolineras.lecturaCombustiblePorDefecto(ac, FICHERO));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // El spinner creado contiene todos los items del array de Strings "operacionesArray"
        final ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.operacionesArray)){
            @Override
            public boolean isEnabled(int position){
                boolean habilitado;
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    habilitado = false;
                }
                else
                {
                    habilitado = true;
                }
                return habilitado;
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        // Al abrir el spinner la lista se abre hacia abajo
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapterSpinner);

        // Set the action buttons
        builder.setPositiveButton("Aplicar", (dialog, id) -> {
            // User clicked Aceptar, save the item selected in the spinner
            // If the user does not select nothing, don't do anything
            if (!mSpinner.getSelectedItem().toString().equalsIgnoreCase("Combustible")) {
                tipoCombustible = mSpinner.getSelectedItem().toString();
                try {
                    presenterGasolineras.escrituraCombustiblePorDefecto(mSpinner.getSelectedItem().toString(), ac, FICHERO);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException ex){
                    ex.printStackTrace();
                } catch (PresenterGasolineras.CombustibleNoExistente combustibleNoExistente) {
                    combustibleNoExistente.printStackTrace();
                }
                try {
                    tipoCombustible = presenterGasolineras.lecturaCombustiblePorDefecto(ac, FICHERO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            closeDrawer(drawerLayout);
            refresca();
        });
        builder.setNegativeButton(CANCELAR, (dialog, id) -> {
            dialog.dismiss();
            //closeDrawer(drawerLayout);
        });
        builder.setView(mView);
        builder.create();
        builder.show();
    }

    /**
     * Segunda opcion en la barra superior de la izquierda para poder anhadir una
     * ubicacion como punto de partida habitual
     */
    public void clickUbicacion() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ubicación");

        // Vista escondida del nuevo layout para las diferentes celdas a implementar para los filtros
        View mView = getLayoutInflater().inflate(R.layout.anhadir_ubicacion_punto_partida_layout, null);
        builder.setView(mView);
        AlertDialog dialog = builder.create();


        // Campos necesarios para comprobar todos los casos de error existentes
        textInputLatitud = mView.findViewById(R.id.layout_latitud);
        textInputLongitud = mView.findViewById(R.id.layout_longitud);
        labelLatitud = mView.findViewById(R.id.labelLatitud);
        labelLongitud = mView.findViewById(R.id.labelLongitud);

        buttonCancelar = mView.findViewById(R.id.btn_cancelar);
        buttonEstablecer = mView.findViewById(R.id.btn_establecer);


        final TextView comb = mView.findViewById(R.id.ubicacionPorDefecto);

        try {
            comb.setText("Ubicación actual: " + presenterGasolineras.lecturaCoordenadaPorDefecto(ac, FICHERO_UBICACION));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Boton Establecer
        buttonEstablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateLatitud() | !validateLongitud()) {
                    return;
                } else {

                    latitud = textInputLatitud.getEditText().getText().toString().trim();
                    longitud = textInputLongitud.getEditText().getText().toString().trim();
                    //coordenada = latitud + " " + longitud;
                    newCoordenada = latitud + " " + longitud;


                    try {
                        //presenterGasolineras.escrituraCoordenadaPorDefecto(coordenada, ac, FICHERO_UBICACION);
                        presenterGasolineras.escrituraCoordenadaPorDefecto(newCoordenada, ac, FICHERO_UBICACION);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    }catch (IOException ex){
                        ex.printStackTrace();

                    } catch (PresenterGasolineras.CoordenadaNoExistente coordenadaNoExistente) {
                    coordenadaNoExistente.printStackTrace();
                    }

                    try {
                        //coordenada = presenterGasolineras.lecturaCoordenadaPorDefecto(ac, FICHERO_UBICACION);
                        newCoordenada = presenterGasolineras.lecturaCoordenadaPorDefecto(ac, FICHERO_UBICACION);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    dialog.dismiss();
                    closeDrawer(drawerLayout);
                    refresca();
                }
            }

        });

        // Boton Cancelar
        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //closeDrawer(drawerLayout);
            }

        });

        dialog.show();
    }

    /**
     * Comprobacion de los diferentes casos de error a la hora de introducir la latitud
     * @return false si el campo latitud esta vacio, si contiene caracteres erroneos,
     * si tiene mas de un punto o de un guión, si tiene mas de 15 caracteres,
     * y si la latitud no esta comprendida entre -90 y 90.
     * Y retorna true en caso contrario.
     */
    private boolean validateLatitud() {

        // Se almacena el contenido del campo latitud
        String latitud = textInputLatitud.getEditText().getText().toString().trim();

        // Se comprueba que no este vacio
        if (latitud.isEmpty()) {
            textInputLatitud.setError("La latitud no puede estar vacia");
            labelLatitud.setTextColor(Color.RED);
            return false;
        }

        // Se comprueba si existe algun caracter erroneo
        // Solo se permiten valores entre el 0 y el 9, el punto y el guion
        Pattern p = Pattern.compile("[^0-9.-]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(latitud);
        boolean esIncorrecto = m.find();

        if (esIncorrecto) {
            textInputLatitud.setError("Existen caracteres erróneos");
            labelLatitud.setTextColor(Color.RED);
            return false;
        }

        // Se comprueba que solo hay una ocurrencia tanto par el punto como para el guion
        int numPuntos = 0;
        int numGuion = 0;
        for(int i=0 ; i<latitud.length(); i++){
            if(latitud.charAt(i) == '.'){
                numPuntos++;
            }

            if(latitud.charAt(i) == '-'){
                numGuion++;
            }
        }

        // Solo se permite una ocurrencia del punto
        if (numPuntos > 1) {
            textInputLatitud.setError("Solo puede haber un punto");
            labelLatitud.setTextColor(Color.RED);
            return false;
        }

        // Solo se permite una ocurrencia del guion
        if (numGuion > 1) {
            textInputLatitud.setError("Solo puede haber un guión");
            labelLatitud.setTextColor(Color.RED);
            return false;
        }

        // Se comprueba que en caso de que haya un guion, este esta en la primera posicion
        if (numGuion == 1 && latitud.charAt(0) != '-') {
            textInputLatitud.setError("El guion tiene que ser el primer caracter");
            labelLatitud.setTextColor(Color.RED);
            return false;
        }

        /*
         * Una vez comprobados los casos anteriores, se puede almacenar el numero insertado (formato correcto)
         * para las posteriores comprobaciones a realizar
         */
        double numLatitud = Double.parseDouble(textInputLatitud.getEditText().getText().toString());

        // Se comprueba que la latitud no contenga mas de 15 caracteres
        if (latitud.length() > 15) {
            textInputLatitud.setError("Latitud demasiado larga");
            labelLatitud.setTextColor(Color.RED);
            return false;

        // Se comprueba que la latitud este comprendida entre -90 y 90
        } else if (numLatitud < -90 || numLatitud > 90) {
            textInputLatitud.setError("La latitud debe ser entre -90 y 90");
            labelLatitud.setTextColor(Color.RED);
            return false;

        // Caso correcto
        } else  {
            textInputLatitud.setError(null);
            labelLatitud.setTextColor(Color.GRAY);
            return true;
        }

    }

    /**
     * Comprobacion de los diferentes casos de error a la hora de introducir la longitud
     * @return false si el campo longitud esta vacio, si contiene caracteres erroneos,
     * si tiene mas de un punto o de un guión, si tiene mas de 15 caracteres,
     * y si la longitud no esta comprendida entre -180 y 180.
     * Y retorna true en caso contrario.
     */
    private boolean validateLongitud() {

        // Se almacena el contenido del campo longitud
        String longitud = textInputLongitud.getEditText().getText().toString().trim();

        // Se comprueba que no este vacio
        if (longitud.isEmpty()) {
            textInputLongitud.setError("La longitud no puede estar vacia");
            labelLongitud.setTextColor(Color.RED);
            return false;

        }

        // Se comprueba si existe algun caracter erroneo
        // Solo se permiten valores entre el 0 y el 9, el punto y el guion
        Pattern p = Pattern.compile("[^0-9.-]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(longitud);
        boolean esIncorrecto = m.find();

        if (esIncorrecto) {
            textInputLongitud.setError("Existen caracteres erróneos");
            labelLongitud.setTextColor(Color.RED);
            return false;
        }

        // Se comprueba que solo hay una ocurrencia tanto par el punto como para el guion
        int numPuntos = 0;
        int numGuion = 0;
        for(int i =0 ; i<longitud.length(); i++){
            if(longitud.charAt(i) == '.'){
                numPuntos++;
            }

            if(longitud.charAt(i) == '-'){
                numGuion++;
            }
        }

        // Solo se permite una ocurrencia del punto
        if (numPuntos > 1) {
            textInputLongitud.setError("Solo puede haber un punto");
            labelLongitud.setTextColor(Color.RED);
            return false;
        }

        // Solo se permite una ocurrencia del guion
        if (numGuion > 1) {
            textInputLongitud.setError("Solo puede haber un guión");
            labelLongitud.setTextColor(Color.RED);
            return false;
        }

        // Se comprueba que en caso de que haya un guion, este esta en la primera posicion
        if (numGuion == 1 && longitud.charAt(0) != '-') {
            textInputLongitud.setError("El guion tiene que ser el primer caracter");
            labelLongitud.setTextColor(Color.RED);
            return false;
        }

        /*
         * Una vez comprobados los casos anteriores, se puede almacenar el numero insertado (formato correcto)
         * para las posteriores comprobaciones a realizar
         */
        double numLongitud = Double.parseDouble(textInputLongitud.getEditText().getText().toString());

        // Se comprueba que la longitud no contenga mas de 15 caracteres
        if (longitud.length() > 15) {
            textInputLongitud.setError("Longitud demasiado larga");
            labelLongitud.setTextColor(Color.RED);
            return false;

        // Se comprueba que la longitud este comprendida entre -180 y 180
        }  else if (numLongitud < -180 || numLongitud > 180) {
            textInputLongitud.setError("La latitud debe ser entre -180 y 180");
            labelLongitud.setTextColor(Color.RED);
            return false;

        // Caso correcto
        } else {
            textInputLongitud.setError(null);
            labelLongitud.setTextColor(Color.GRAY);
            return true;
        }

    }


    public static void openDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    private static void closeDrawer(DrawerLayout drawerLayout){
        //close drawer layout
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        closeDrawer(drawerLayout);
    }

    public void onClick(View v) {

        if (v.getId() == R.id.buttonFiltros) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Set the dialog title
            builder.setTitle("Filtros");
            // Specify the list array, the items to be selected by default (null for none),

            // Vista escondida del nuevo layout para los diferentes spinners a implementar para los filtros
            View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

            final TextView txtComb = mView.findViewById(R.id.combustibleSeleccionado);
            txtComb.setText(this.tipoCombustible);
            final Spinner mSpinner = (Spinner) mView.findViewById(R.id.spinner);    // New spinner object
            // El spinner creado contiene todos los items del array de Strings "operacionesArray"
            ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_spinner_item,
                    getResources().getStringArray(R.array.operacionesArray));
            // Al abrir el spinner la lista se abre hacia abajo
            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapterSpinner);

            // Set the action buttons
            builder.setPositiveButton("Aceptar", (dialog, id) -> {
                // User clicked Aceptar, save the item selected in the spinner
                // If the user does not select nothing, don't do anything
                if (!mSpinner.getSelectedItem().toString().equalsIgnoreCase("Tipo de Combustible")) {
                    tipoCombustible = mSpinner.getSelectedItem().toString();

                }
                refresca();
            });
            builder.setNegativeButton(CANCELAR, (dialog, id) -> dialog.dismiss());
            builder.setView(mView);
            builder.create();
            builder.show();

        } else if (v.getId() == R.id.buttonOrden) {
            //comienzo de ordenar
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Vista escondida del nuevo layout para los diferentes spinners a implementar para los filtros
            View mView = getLayoutInflater().inflate(R.layout.ordenar_layout, null);


            builder.setTitle("Ordenar");
            final Button mb = (Button) mView.findViewById(R.id.buttonprecio);
            final ImageView imgOrdenPrecio = mView.findViewById(R.id.iconoOrdenPrecio);

            mb.setText(buttonString[0]);
            imgOrdenPrecio.setImageResource(getResources().getIdentifier(idImgOrdernPrecio[0],
                    DRAWABLE, getPackageName()));

            final String[]  valorActualOrdenPrecio={ buttonString[0]};
            final String[]  valorActualIconoPrecio={idImgOrdernPrecio[0]};
            final boolean[] ordenActual = {esAsc};
            mb.setOnClickListener(v1 -> {
                ordenActual [0] = !ordenActual[0];
                if (ordenActual[0]) {
                    valorActualIconoPrecio[0] = FLECHA_ARRIBA;
                    valorActualOrdenPrecio[0] = PRECIO_ASC;
                } else {
                    valorActualIconoPrecio[0]= "flecha_abajo";
                    valorActualOrdenPrecio[0] = "Precio (des)";

                }
                imgOrdenPrecio.setImageResource(getResources().getIdentifier(valorActualIconoPrecio[0],
                        DRAWABLE, getPackageName()));
                mb.setText( valorActualOrdenPrecio[0]);
            });


            builder.setPositiveButton("Aceptar", (dialog, id) -> {
                buttonString[0] = valorActualOrdenPrecio[0];
                idImgOrdernPrecio[0] = valorActualIconoPrecio[0];
                esAsc= ordenActual[0];
                refresca();
            });

            builder.setNegativeButton(CANCELAR, (dialog, id) -> dialog.dismiss());
            builder.setView(mView);
            builder.create();
            builder.show();

        } else if(v.getId() == R.id.info) {
            //Creating the instance of PopupMenu
            PopupMenu popup = new PopupMenu(MainActivity.this, config);
            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.menu, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.itemActualizar) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    new CargaDatosGasolinerasTask(MainActivity.this).execute();
                } else if (item.getItemId() == R.id.itemInfo) {
                    Intent myIntent = new Intent(MainActivity.this, InfoActivity.class);
                    MainActivity.this.startActivity(myIntent);
                }
                return true;
            });
            popup.show(); //showing popup menu

        } else if (v.getId() == R.id.menuNav) {
            this.clickMenu();

        } else if (v.getId() == R.id.btnConfiguracion) {
            this.clickConfiguracion();

        } else if (v.getId() == R.id.btnUbicacion) {
            this.clickUbicacion();
        }
    }

    private void refresca() {
        //Refrescar automáticamente la lista de gasolineras
        mSwipeRefreshLayout.setRefreshing(true);
        new CargaDatosGasolinerasTask(ac).execute();
    }


    /**
     * CargaDatosGasolinerasTask
     * <p>
     * Tarea asincrona para obtener los datos de las gasolineras
     * en segundo plano.
     * <p>
     * Redefinimos varios métodos que se ejecutan en el siguiente orden:
     * onPreExecute: activamos el dialogo de progreso
     * doInBackground: solicitamos que el presenter cargue los datos
     * onPostExecute: desactiva el dialogo de progreso,
     * muestra las gasolineras en formato lista (a partir de un adapter)
     * y define la acción al realizar al seleccionar alguna de ellas
     * <p>
     * http://www.sgoliver.net/blog/tareas-en-segundo-plano-en-android-i-thread-y-asynctask/
     */
    private class CargaDatosGasolinerasTask extends AsyncTask<Void, Void, Boolean> {

        Activity activity;

        /**
         * Constructor de la tarea asincrona
         *
         * @param activity
         */
        public CargaDatosGasolinerasTask(Activity activity) {
            this.activity = activity;
        }


        /**
         * doInBackground
         * <p>
         * Tarea ejecutada en segundo plano
         * Llama al presenter para que lance el método de carga de los datos de las gasolineras
         *
         * @param params
         * @return boolean
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            return presenterGasolineras.cargaDatosGasolineras();
        }

        /**
         * onPostExecute
         * <p>
         * Se ejecuta al finalizar doInBackground
         * Oculta el diálogo de progreso.
         * Muestra en una lista los datos de las gasolineras cargadas,
         * creando un adapter y pasándoselo a la lista.
         * Define el manejo de la selección de los elementos de la lista,
         * lanzando con una intent una actividad de detalle
         * a la que pasamos un objeto Gasolinera
         *
         * @param res
         */
        @Override
        protected void onPostExecute(Boolean res) {
            Toast toast;

            mSwipeRefreshLayout.setRefreshing(false);

            // Si se ha obtenido resultado en la tarea en segundo plano
            if (Boolean.TRUE.equals(res)) {
                //Recorrer el array adapter para que no muestre las gasolineras con precios negativos
                presenterGasolineras.eliminaGasolinerasConPrecioNegativo(tipoCombustible);
                //ordenacion
                presenterGasolineras.ordernarGasolineras(esAsc, tipoCombustible);
                // Definimos el array adapter
                adapter = new GasolineraArrayAdapter(activity, 0, presenterGasolineras.getGasolineras());

                // Obtenemos la vista de la lista
                listViewGasolineras = findViewById(R.id.listViewGasolineras);

                // Cargamos los datos en la lista
                if (!presenterGasolineras.getGasolineras().isEmpty()) {
                    // datos obtenidos con exito
                    listViewGasolineras.setAdapter(adapter);

                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.datos_exito), Toast.LENGTH_LONG);

                        if (coordenada != newCoordenada) {

                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.ubicacion_establecida), Toast.LENGTH_LONG);
                            coordenada = newCoordenada;
                        }

                } else {
                    // los datos estan siendo actualizados en el servidor, por lo que no son actualmente accesibles
                    // sucede en torno a las :00 y :30 de cada hora
                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.datos_no_accesibles), Toast.LENGTH_LONG);
                }
            } else {
                // error en la obtencion de datos desde el servidor
                if (isNetworkConnected()) {
                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.datos_no_obtenidos), Toast.LENGTH_LONG);
                }
                else{
                    adapter.clear();
                    toast=Toast.makeText(getApplicationContext(),"No hay conexión a internet",Toast.LENGTH_LONG);
                }

            }


            // Muestra el mensaje del resultado de la operación en un toast
            if (toast != null) {

                toast.show();
            }

            /*
             * Define el manejo de los eventos de click sobre elementos de la lista
             * En este caso, al pulsar un elemento se lanzará una actividad con una vista de detalle
             * a la que le pasamos el objeto Gasolinera sobre el que se pulsó, para que en el
             * destino tenga todos los datos que necesita para mostrar.
             * Para poder pasar un objeto Gasolinera mediante una intent con putExtra / getExtra,
             * hemos tenido que hacer que el objeto Gasolinera implemente la interfaz Parcelable
             */

            try {
                listViewGasolineras.setOnItemClickListener((a, v, position, id) -> {

                    /* Obtengo el elemento directamente de su posicion,
                     * ya que es la misma que ocupa en la lista
                     */
                    Intent myIntent = new Intent(MainActivity.this, DetailActivity.class);
                    myIntent.putExtra(getResources().getString(R.string.pasoDatosGasolinera),
                            presenterGasolineras.getGasolineras().get(position));

                    myIntent.putExtra(getResources().getString(R.string.pasoTipoCombustible),
                            tipoCombustible);
                    MainActivity.this.startActivity(myIntent);

                });
            } catch(Exception e1) {
                e1.getStackTrace();
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////
        }

        private boolean isNetworkConnected() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
        }
    }


    /*
    ------------------------------------------------------------------
        GasolineraArrayAdapter

        Adaptador para inyectar los datos de las gasolineras
        en el listview del layout principal de la aplicacion
    ------------------------------------------------------------------
    */
    class GasolineraArrayAdapter extends ArrayAdapter<Gasolinera> {

        private Context context;
        private List<Gasolinera> listaGasolineras;

        // Constructor
        public GasolineraArrayAdapter(Context context, int resource, List<Gasolinera> objects) {
            super(context, resource, objects);
            this.context = context;
            this.listaGasolineras = objects;
        }

        // Llamado al renderizar la lista
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // Obtiene el elemento que se está mostrando
            Gasolinera gasolinera = listaGasolineras.get(position);

            // Indica el layout a usar en cada elemento de la lista
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_gasolinera, null);

            // Asocia las variables de dicho layout
            ImageView logo = view.findViewById(R.id.imageViewLogo);
            TextView rotulo = view.findViewById(R.id.textViewRotulo);
            TextView direccion = view.findViewById(R.id.textViewDireccion);
            TextView labelGasolina = view.findViewById(R.id.textViewTipoGasolina);
            TextView precio = view.findViewById(R.id.textViewGasoleoA);

            // Y carga los datos del item
            rotulo.setText(gasolinera.getRotulo());
            direccion.setText(gasolinera.getDireccion());
            labelGasolina.setText(tipoCombustible);
            double precioCombustible = presenterGasolineras.getPrecioCombustible(tipoCombustible, gasolinera);
            precio.setText(precioCombustible + getResources().getString(R.string.moneda));

            // Se carga el icono
            cargaIcono(gasolinera, logo);


            // Si las dimensiones de la pantalla son menores
            // reducimos el texto de las etiquetas para que se vea correctamente
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            if (displayMetrics.widthPixels < 720) {
                TextView tv = view.findViewById(R.id.textViewTipoGasolina);
                RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams) tv.getLayoutParams());
                params.setMargins(15, 0, 0, 0);
                tv.setTextSize(11);
                TextView tmp;
                tmp = view.findViewById(R.id.textViewGasolina95Label);
                tmp.setTextSize(11);
                tmp = view.findViewById(R.id.textViewGasoleoA);
                tmp.setTextSize(11);
                tmp = view.findViewById(R.id.textViewGasolina95);
                tmp.setTextSize(11);
            }

            return view;
        }

        private void cargaIcono(Gasolinera gasolinera, ImageView logo) {
            // carga icono

            String rotuleImageID = gasolinera.getRotulo().toLowerCase();

            // Tengo que protegerme ante el caso en el que el rotulo solo tiene digitos.
            // En ese caso getIdentifier devuelve esos digitos en vez de 0.
            int imageID = context.getResources().getIdentifier(rotuleImageID,
                    DRAWABLE, context.getPackageName());

            if (imageID == 0 || TextUtils.isDigitsOnly(rotuleImageID)) {
                imageID = context.getResources().getIdentifier(getResources().getString(R.string.pordefecto),
                        DRAWABLE, context.getPackageName());
            }
            logo.setImageResource(imageID);

        }
    }
}








