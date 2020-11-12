package com.isunican.proyectobase.Views;

import com.isunican.proyectobase.Presenter.*;
import com.isunican.proyectobase.Model.*;
import com.isunican.proyectobase.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import java.util.List;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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

    PresenterGasolineras presenterGasolineras;

    // Vista de lista y adaptador para cargar datos en ella
    ListView listViewGasolineras;
    ArrayAdapter<Gasolinera> adapter;

    // Barra de progreso circular para mostar progeso de carga
    ProgressBar progressBar;

    // Swipe and refresh (para recargar la lista con un swipe)
    SwipeRefreshLayout mSwipeRefreshLayout;

    //Botones de filtro y ordenacion
    Button buttonFiltros;
    Button buttonOrden;


    /*Variables para modificar filtros y ordenaciones*/
    //orden ascendente por defecto
    final String[] buttonString = {PRECIO_ASC};
    final String[] idImgOrdernPrecio = {FLECHA_ARRIBA};
    String tipoCombustible = "Gasóleo A"; //Por defecto
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

        // Barra de progreso
        // https://materialdoc.com/components/progress/
        progressBar = new ProgressBar(MainActivity.this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout layout = findViewById(R.id.activity_precio_gasolina);
        layout.addView(progressBar, params);

        // Muestra el logo en el actionBar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.por_defecto_mod);


        // Swipe and refresh
        // Al hacer swipe en la lista, lanza la tarea asíncrona de carga de datos
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new CargaDatosGasolinerasTask(MainActivity.this).execute();
            }
        });

        // Al terminar de inicializar todas las variables
        // se lanza una tarea para cargar los datos de las gasolineras
        // Esto se ha de hacer en segundo plano definiendo una tarea asíncrona
        new CargaDatosGasolinerasTask(this).execute();

        //Añadir los listener a los botones
        buttonFiltros = findViewById(R.id.buttonFiltros);
        buttonOrden = findViewById(R.id.buttonOrden);
        buttonFiltros.setOnClickListener(this);
        buttonOrden.setOnClickListener(this);
    }


    /**
     * Menú action bar
     * <p>
     * Redefine métodos para el uso de un menú de tipo action bar.
     * <p>
     * onCreateOptionsMenu
     * Carga las opciones del menú a partir del fichero de recursos menu/menu.xml
     * <p>
     * onOptionsItemSelected
     * Define las respuestas a las distintas opciones del menú
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itemActualizar) {
            mSwipeRefreshLayout.setRefreshing(true);
            new CargaDatosGasolinerasTask(this).execute();
        } else if (item.getItemId() == R.id.itemInfo) {
            Intent myIntent = new Intent(MainActivity.this, InfoActivity.class);
            MainActivity.this.startActivity(myIntent);
        }
        return true;
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
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked Aceptar, save the item selected in the spinner
                    // If the user does not select nothing, don't do anything
                    if (!mSpinner.getSelectedItem().toString().equalsIgnoreCase("Tipo de Combustible")) {
                        tipoCombustible = mSpinner.getSelectedItem().toString();

                    }
                    refresca();
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
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

            final String[]  valorActualOrdenPrecio={PRECIO_ASC};
            final String[]  valorActualIconoPrecio={FLECHA_ARRIBA};
            final boolean[] ordenActual = {esAsc};
            mb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                }
            });


            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    buttonString[0] = valorActualOrdenPrecio[0];
                    idImgOrdernPrecio[0] = valorActualIconoPrecio[0];
                    esAsc= ordenActual[0];
                    refresca();
                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.setView(mView);
            builder.create();
            builder.show();

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
         * onPreExecute
         * <p>
         * Metodo ejecutado de forma previa a la ejecucion de la tarea definida en el metodo doInBackground()
         * Muestra un diálogo de progreso
         */
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
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

            // Si el progressDialog estaba activado, lo oculta
            progressBar.setVisibility(View.GONE);     // To Hide ProgressBar

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
                listViewGasolineras.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                        /* Obtengo el elemento directamente de su posicion,
                         * ya que es la misma que ocupa en la lista
                         */
                        Intent myIntent = new Intent(MainActivity.this, DetailActivity.class);
                        myIntent.putExtra(getResources().getString(R.string.pasoDatosGasolinera),
                                presenterGasolineras.getGasolineras().get(position));

                        myIntent.putExtra(getResources().getString(R.string.pasoTipoCombustible),
                                tipoCombustible);
                        MainActivity.this.startActivity(myIntent);

                    }
                });
            }
            catch(Exception e1){
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