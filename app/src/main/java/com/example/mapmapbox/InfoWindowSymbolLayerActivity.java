package com.example.mapmapbox;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapmapbox.until.LoadGeoJsonDataTask;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static android.graphics.Color.rgb;
import static android.media.midi.MidiDeviceInfo.PROPERTY_NAME;
import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.e;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static java.security.AccessController.getContext;

/**
 * Use a SymbolLayer to show a BubbleLayout above a SymbolLayer icon. This is a more performant
 * way to show the BubbleLayout that appears when using the MapboxMap.addMarker() method.
 */
public class InfoWindowSymbolLayerActivity extends AppCompatActivity implements
        OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    private static final String MARKER_IMAGE_ID = "my-marker-image";
    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String PROPERTY_NAME = "name";

    private MapView mapView;
    private MapboxMap mapboxMap;
    private GeoJsonSource source;
    private FeatureCollection featureCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.accses_tokon));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_info_window_symbol_layer);

// Initialize the map view
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
//                addClusteredGeoJsonSource(style);
                mapboxMap.getUiSettings().setAttributionEnabled(false);
                mapboxMap.getUiSettings().setLogoEnabled(false);

                new LoadGeoJsonDataTask(InfoWindowSymbolLayerActivity.this).execute();
                mapboxMap.addOnMapClickListener(InfoWindowSymbolLayerActivity.this);
            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
    }


    public void addClusteredGeoJsonSource(Style loadedMapStyle) {
        try {
            loadedMapStyle.addSource(
                    // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
                    // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
                    new GeoJsonSource("earthquakes",
                            new URL("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
                            new GeoJsonOptions()
                                    .withCluster(true)
                                    .withClusterMaxZoom(9)
                                    .withClusterRadius(50)
                    )
            );
        } catch (MalformedURLException malformedUrlException) {
//                    Log.e("dataClusterActivity", "Check the URL " + malformedUrlException.getMessage());
        }
        int[][] layers = new int[][]{
                new int[]{150, ContextCompat.getColor(InfoWindowSymbolLayerActivity.this, R.color.mapboxRed)},
                new int[]{20, ContextCompat.getColor(InfoWindowSymbolLayerActivity.this, R.color.mapboxGreen)},
                new int[]{0, ContextCompat.getColor(InfoWindowSymbolLayerActivity.this, R.color.mapbox_blue)}
        };
        SymbolLayer unclustered = new SymbolLayer("unclustered-points", "earthquakes");
        unclustered.setProperties(
                iconImage(MARKER_IMAGE_ID),
                iconSize(
                        division(get("mag"), literal(4.0f)
                        )
                ),
                iconColor(
                        interpolate(exponential(1), get("mag"),
                                stop(2.0, rgb(0, 255, 0)),
                                stop(4.5, rgb(0, 0, 255)),
                                stop(7.0, rgb(255, 0, 0))
                        )
                )
        );
        loadedMapStyle.addLayer(unclustered);

        for (int i = 0; i < layers.length; i++) {
            //Add clusters' circles
            CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(18f)
            );

            Expression pointCount = toNumber(get("point_count"));
            // Add a filter to the cluster layer that hides the circles based on "point_count"
            circles.setFilter(
                    i == 0
                            ? all(has("point_count"),
                            gte(pointCount, literal(layers[i][0]))
                    ) : all(has("point_count"),
                            gt(pointCount, literal(layers[i][0])),
                            lt(pointCount, literal(layers[i - 1][0]))
                    )
            );
            loadedMapStyle.addLayer(circles);
        }
        SymbolLayer count = new SymbolLayer("count", "earthquakes");
        count.setProperties(
                textField(Expression.toString(get("point_count"))),
                textSize(12f),
                textColor(Color.WHITE),
                textIgnorePlacement(true),
                textAllowOverlap(true)

        );
        loadedMapStyle.addLayer(count);
    }
//
    /**
     * Sets up all of the sources and layers needed for this example
     *
     * @param collection the FeatureCollection to set equal to the globally-declared FeatureCollection
     */
    public void setUpData(final FeatureCollection collection) {
        featureCollection = collection;
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                setupSource(style);
                setUpImage(style);
                setUpMarkerLayer(style);
                setUpInfoWindowLayer(style);
//                addClusteredGeoJsonSource(style);
            }
        }
    }

    /**
     * Adds the GeoJSON source to the map
     */
    private void setupSource(@NonNull Style loadedStyle) {
        source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
        loadedStyle.addSource(source);
    }

    /**
     * Adds the marker image to the map for use as a SymbolLayer icon
     */
    private void setUpImage(@NonNull Style loadedStyle) {
        loadedStyle.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                this.getResources(), R.drawable.icon_m32));
    }

    /**
     * Updates the display of data on the map after the FeatureCollection has been modified
     */
    public void refreshSource() {
        if (source != null && featureCollection != null) {
            source.setGeoJson(featureCollection);
        }
    }

    /**
     * Setup a layer with maki icons, eg. west coast city.
     */
    private void setUpMarkerLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        iconImage(MARKER_IMAGE_ID),
                        iconAllowOverlap(true)

                ));

    }

    /**
     * Setup a layer with Android SDK call-outs
     * <p>
     * name of the feature is used as key for the iconImage
     * </p>
     */
    public static double computeHeading(LatLng from, LatLng to) {
//        return TurfMeasurement.bearing(

//                Position.fromCoordinates(from.getLongitude(), from.getLatitude()),
//                Position.fromCoordinates(to.getLongitude(), to.getLatitude())
//        );
        return 0;
    }
    private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        /* show image with id title based on the value of the name feature property */
                        iconImage("{name}"),

                        /* set anchor of icon to bottom-left */
                        iconAnchor(ICON_ANCHOR_BOTTOM),

                        /* all info window and marker image to appear at the same time*/
                        iconAllowOverlap(true),

                        /* offset the info window to be above the marker */
                        iconOffset(new Float[]{-2f, -25f})
                )
                /* add a filter to show only when selected feature property is true */
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
    }

    /**
     * This method handles click events for SymbolLayer symbols.
     * <p>
     * When a SymbolLayer icon is clicked, we moved that feature to the selected state.
     * </p>
     *
     * @param screenPoint the point on screen clicked
     */

    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            List<Feature> featureList = featureCollection.features();
            for (int i = 0; i < featureList.size(); i++) {
                if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                    if (featureSelectStatus(i)) {
                        setFeatureSelectState(featureList.get(i), false);
                    } else {
                        setSelected(i);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set a feature selected state.
     *
     * @param index the index of selected feature
     */
    private void setSelected(int index) {
        Feature feature = featureCollection.features().get(index);
        setFeatureSelectState(feature, true);
        refreshSource();
    }

    /**
     * Selects the state of a feature
     *
     * @param feature the feature to be selected.
     */
    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
        refreshSource();
    }

    /**
     * Checks whether a Feature's boolean "selected" property is true or false
     *
     * @param index the specific Feature's index position in the FeatureCollection's list of Features.
     * @return true if "selected" is true. False if the boolean property is false.
     */
    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }

    /**
     * Invoked when the bitmaps have been generated from a view.
     */
    public void setImageGenResults(HashMap<String, Bitmap> imageMap) {
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
// calling addImages is faster as separate addImage calls for each bitmap.
                style.addImages(imageMap);
            }
        }
    }

/**
 * AsyncTask to load data from the assets folder.
 */


/**
 * AsyncTask to generate Bitmap from Views to be used as iconImage in a SymbolLayer.
 * <p>
 * Call be optionally be called to update the underlying data source after execution.
 * </p>
 * <p>
 * Generating Views on background thread sius_west_coast.geojsonnce we are not going to be adding them to the view hierarchy.
 * </p>PROPERTY_SELECTED
 */

    /**
     * Utility class to generate Bitmaps for Symbol.
     */

//    private static class SymbolGenerator {
//
//        /**
//         * Generate a Bitmap from an Android SDK View.
//         *
//         * @param view the View to be drawn to a Bitmap
//         * @return the generated bitmap
//         */
//        static Bitmap generate(@NonNull View view) {
//            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//            view.measure(measureSpec, measureSpec);
//
//            int measuredWidth = view.getMeasuredWidth();
//            int measuredHeight = view.getMeasuredHeight();
//
//            view.layout(0, 0, measuredWidth, measuredHeight);
//            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
//            bitmap.eraseColor(Color.TRANSPARENT);
//            Canvas canvas = new Canvas(bitmap);
//            view.draw(canvas);
//            return bitmap;
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }

//    private static class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {
//
//        private final HashMap<String, View> viewMap = new HashMap<>();
//        private final WeakReference<InfoWindowSymbolLayerActivity> activityRef;
//        private final boolean refreshSource;
//
//        GenerateViewIconTask(InfoWindowSymbolLayerActivity activity, boolean refreshSource) {
//            this.activityRef = new WeakReference<>(activity);
//            this.refreshSource = refreshSource;
//        }
//
//        GenerateViewIconTask(InfoWindowSymbolLayerActivity activity) {
//            this(activity, false);
//        }
//
//        @SuppressLint("StringFormatInvalid")
//        @SuppressWarnings("WrongThread")
//        @Override
//        protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
//            InfoWindowSymbolLayerActivity activity = activityRef.get();
//            if (activity != null) {
//                HashMap<String, Bitmap> imagesMap = new HashMap<>();
//                LayoutInflater inflater = LayoutInflater.from(activity);
//
//                FeatureCollection featureCollection = params[0];
//
//                for (Feature feature : featureCollection.features()) {
//
//                    BubbleLayout bubbleLayout = (BubbleLayout)
//                            inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);
//
//                    String name = feature.getStringProperty(PROPERTY_NAME);
//                    TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
//                    titleTextView.setText(name);
//
//                    String style = feature.getStringProperty(PROPERTY_CAPITAL);
//                    TextView descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description);
//                    descriptionTextView.setText(
//                            String.format(activity.getString(R.string.capital), style));
//
//                    int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//                    bubbleLayout.measure(measureSpec, measureSpec);
//
//                    int measuredWidth = bubbleLayout.getMeasuredWidth();
//
//                    bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);
//
//                    Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
//                    imagesMap.put(name, bitmap);
//                    viewMap.put(name, bubbleLayout);
//                }
//
//                return imagesMap;
//            } else {
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
//            super.onPostExecute(bitmapHashMap);
//            InfoWindowSymbolLayerActivity activity = activityRef.get();
//            if (activity != null && bitmapHashMap != null) {
//                activity.setImageGenResults(bitmapHashMap);
//                if (refreshSource) {
//                    activity.refreshSource();
//                }
//            }
//            Toast.makeText(activity, "tap_on_marker_instruction", Toast.LENGTH_SHORT).show();
//        }
//    }

//    public class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {
//        private static final String PROPERTY_SELECTED = "selected";
//
//
//        private final WeakReference<InfoWindowSymbolLayerActivity> activityRef;
//
//        public LoadGeoJsonDataTask(InfoWindowSymbolLayerActivity activity) {
//            this.activityRef = new WeakReference<>(activity);
//        }
//
//        @Override
//        protected FeatureCollection doInBackground(Void... params) {
//            InfoWindowSymbolLayerActivity activity = activityRef.get();
//
//            if (activity == null) {
//                return null;
//            }
//
//            String geoJson = loadGeoJsonFromAsset(activity, "us_west_coast.geojson");
//            return FeatureCollection.fromJson(geoJson);
//        }
//
//        @Override
//        protected void onPostExecute(FeatureCollection featureCollection) {
//            super.onPostExecute(featureCollection);
//            InfoWindowSymbolLayerActivity activity = activityRef.get();
//            if (featureCollection == null || activity == null) {
//                return;
//            }
//
//// This example runs on the premise that each GeoJSON Feature has a "selected" property,
//// with a boolean value. If your data's Features don't have this boolean property,
//// add it to the FeatureCollection 's features with the following code:
//            for (Feature singleFeature : featureCollection.features()) {
//                singleFeature.addBooleanProperty(PROPERTY_SELECTED, false);
//            }
//
//            activity.setUpData(featureCollection);
//
//            new InfoWindowSymbolLayerActivity.GenerateViewIconTask(activity).execute(featureCollection);
//        }
//
//        public String loadGeoJsonFromAsset(Context context, String filename) {
//            try {
//// Load GeoJSON file from local asset folder
//
//                InputStream is = context.getAssets().open(filename);
//                int size = is.available();
//                byte[] buffer = new byte[size];
//                is.read(buffer);
//                is.close();
//                return new String(buffer, "UTF-8");
//            } catch (Exception exception) {
//                Timber.e("Exception loading GeoJSON: %s", exception.toString());
//                exception.printStackTrace();
////            throw new RuntimeException(exception);
//                return null;
//            }
//        }
//
//    }
}