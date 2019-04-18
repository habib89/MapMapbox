package com.example.mapmapbox;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DialogTitle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.cluster.clustering.ClusterManagerPlugin;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.graphics.Color.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static java.nio.file.Paths.get;

public class MainActivity extends AppCompatActivity {
            private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
            private static final String MARKER_IMAGE_ID = "MARKER_IMAGE_ID";
            private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
            private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
            private static final String PROPERTY_SELECTED = "selected";
            private static final String PROPERTY_NAME = "name";
            private static final String PROPERTY_CAPITAL = "capital";
            private static final int MULTIPLE_PERMISSION_REQUEST_CODE = 11;
            private static final int MARKERS_CNT = 150;
            private MapView mapView;
            private Mapbox mapbox;
            private MapboxMap map;
            private Runnable updater;
            private ClusterManagerPlugin<POICluster> clusterManagerPlugin;
            private List<POICluster> poiClusterList = new ArrayList<>();
            private FeatureCollection featureCollection;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                Mapbox.getInstance(this, getString(R.string.accses_tokon));
                setContentView(R.layout.activity_main);
                mapView = (MapView)findViewById(R.id.mapView);
//                mapView.setStyleUrl(Style.MAPBOX_STREETS);
                checkAndRequestPermissions();
//                mapView.setCenterCoordinate(new LatLng(40.73581, -73.99155));
                mapView.onSaveInstanceState(savedInstanceState);

                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        new LoadGeoJsonDataTask(MainActivity.this).execute();

                        map = mapboxMap;
                        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {

                                    @Override
                                    public void onStyleLoaded(@NonNull Style style) {
                                        addClusteredGeoJsonSource(style);
                                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                                12.099, -79.045), 3));
                                        List<Feature> featureList = featureCollection.features();
                                        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("store-location-source-id");
                                        if (source != null) {
                                            source.setGeoJson(FeatureCollection.fromFeatures(featureList));
                                        }
                                        if (featureList != null) {
                                            for (int x = 0; x < featureList.size(); x++) {

                                            }
                                        }



                                        style.addImage("cross-icon-id", BitmapUtils.getBitmapFromDrawable(
                                                getResources().getDrawable(R.drawable.icon_m32)));


                                        Toast.makeText(MainActivity.this, "tes",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

//                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
//                                12.099, -79.045), 4));
//                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
//                                12.099, -79.045), 4));
//                        clusterManagerPlugin = new ClusterManagerPlugin<>(getApplication(), map);
//
//                        mapboxMap.addOnCameraIdleListener(clusterManagerPlugin);
//
//                        if (poiClusterList.size() > 0) {
//                            poiClusterList.clear();
//                        }
//                        IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
//                        Icon iconRed = null, iconPurple = null, iconBlue = null;
//                        iconRed = iconFactory.fromResource(R.drawable.pin_red);
//                        iconPurple = iconFactory.fromResource(R.drawable.pin_purple);
//                        iconBlue = iconFactory.fromResource(R.drawable.pin_blue);
//
//
//                        poiClusterList.add(new POICluster(27.176670, 78.008075, "Agra", "India", iconRed));
//                        poiClusterList.add(new POICluster(28.700987, 77.279359, "New Delhi", "India", iconPurple));
//                        poiClusterList.add(new POICluster(23.406714, 76.151514, "Indore", "India", iconBlue));
//                        poiClusterList.add(new POICluster(26.316970, 78.107080, "Gwalior", "India", iconRed));
//                        poiClusterList.add(new POICluster(32.011725, 76.766748, "Himachal", "India", iconPurple));
//                        poiClusterList.add(new POICluster(34.095468, 75.975733, "Kashmir", "India", iconBlue));
//
//                        if (poiClusterList != null && poiClusterList.size() > 0) {
//                            clusterManagerPlugin.addItems(poiClusterList);
//                        }
////                        addClusteredGeoJsonSource(mapboxMap);
////                        initLayerIcons();
//                        Toast.makeText(MainActivity.this, "mapclaster",
//                                Toast.LENGTH_SHORT).show();
//                        final Handler timerHandler = new Handler();
//                        updater = new Runnable() {
//                            @Override
//                            public void run() {
////                                clearMap();
////                                addMarkers();
//                                timerHandler.postDelayed(updater,1000);
//                            }
//                        };
//                        timerHandler.post(updater);
//                        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
//                                12.099, -79.045), 3));
//                        mapboxMap.addImage("cross-icon-id", BitmapUtils.getBitmapFromDrawable(
//                                getResources().getDrawable(R.drawable.ic_marker_24dp)));
//                        mapboxMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(41.885,-87.679))
//                                                        .title("Tes")
//                                                        .snippet("disin"));
//                    MarkerOptions options = new MarkerOptions();
//                    options.title("tes disni");
//
//                    IconFactory iconfactory = IconFactory.getInstance(MainActivity.this);
//                        Icon icon = iconfactory.fromResource(R.drawable.mapbox_marker_icon_default);
//
//                        options.icon(icon);
//
//                    options.position(new LatLng(48.3367,87.679));
//                    mapboxMap.addMarker(options);
                    }

//                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {
                        return handleClickIcon(map.getProjection().toScreenLocation(point));
                    }
                    private boolean featureSelectStatus(int index){
                        if (featureCollection == null){
                            return false;
                        }
                        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_CAPITAL);
                    }
                    private void setFeatureSelectState (Feature feature, boolean selectedState){
                        feature.properties().addProperty(PROPERTY_SELECTED,selectedState);
                    }
                    private void setSelected(int selected){
                        Feature feature = featureCollection.features().get(selected);
                        setFeatureSelectState(feature,true);
                    }

                    private boolean handleClickIcon(PointF screenPoint) {
                        List<Feature> features = map.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);
                        if (!features.isEmpty()) {
                            String name = features.get(0).getStringProperty(PROPERTY_NAME);
                            List<Feature> featureList = featureCollection.features();
                            for (int i = 0; i < featureList.size(); i++) {
                                if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                                    if (featureSelectStatus(i)) {
                                        setFeatureSelectState(featureList.get(i), false);
                                    }else {
                                        setSelected(i);
                                    }
                                }
                            }

                        }
                        return true;
                    }

                });

//                Mapbox.getInstance(this, "pk.eyJ1IjoiYXNtYWxoYWJpYiIsImEiOiJjanJoZ2FmbnIwZGNhNGJvNHYxYTIyODFjIn0.kobya_rBIhsBeMVvdXa-TA");

            }

            private static class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {
                private final MainActivity activityRef;
                private final HashMap<String, View> viewMap = new HashMap<>();
                private final boolean refreshSource;



                private LoadGeoJsonDataTask(MainActivity activityRef,boolean refreshSource) {
                    this.activityRef = activityRef;
                    this.refreshSource = refreshSource;
                }
                LoadGeoJsonDataTask(MainActivity activity) {
                    this(activity, false);

                }

                @Override
                protected FeatureCollection doInBackground(Void... voids) {
                    return null;
                }
            }

            public void setUpData(final FeatureCollection collection) {
//              MainActivity activity = activityRef.get();
                featureCollection = collection;
            }
            private void checkAndRequestPermissions() {

                int LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

                List<String> listPermissionsNeeded = new ArrayList<>();

                if (LOCATION != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
//                    setupMap();
                }

                if (!listPermissionsNeeded.isEmpty()) {
                    ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new
                            String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSION_REQUEST_CODE);
                }
            }
            public void addMarkers() {
                for (int i=0; i<MARKERS_CNT; i++) {
                    Random r = new Random();
                    double lat = r.nextDouble() * 50;
                    double lon = r.nextDouble() * 50;
                    IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                    Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_marker_24dp);
//                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//                    Icon icon = iconFactory.fromBitmap(bitmap);
//                    Marker marker = mapBoxMap.addMarker(new MarkerOptions()
//                            .position(new LatLng(lat, lon))
//                            .icon(icon));
                }
            }
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
                switch (requestCode) {
                    case MULTIPLE_PERMISSION_REQUEST_CODE: {

                        Map<String, Integer> perms = new HashMap<>();

                        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

                        if (grantResults.length > 0) {
                            for (int i = 0; i < permissions.length; i++)
                                perms.put(permissions[i], grantResults[i]);

                            if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                                setupMap();

                            } else {

                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                                    showDialogOK(getResources().getString(R.string.some_req_permissions));
                                } else {
//                                    explain(getResources().getString(R.string.open_settings));
                                }
                            }
                        }
                    }
                    break;

                    default:
                        break;

                }
            }
            @Override
            public void onStart() {
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
            public void onStop() {
                super.onStop();
                mapView.onStop();
            }

            @Override
            public void onLowMemory() {
                super.onLowMemory();
                mapView.onLowMemory();
            }

            @Override
            protected void onDestroy() {
                super.onDestroy();
                mapView.onDestroy();
            }
            @Override
            protected void onSaveInstanceState(Bundle outState) {
                super.onSaveInstanceState(outState);
                mapView.onSaveInstanceState(outState);
            }
            private void initLayerIcons() {
//                map.addImage("single-quake-icon-id", BitmapUtils.getBitmapFromDrawable(
//                        getResources().getDrawable(R.drawable.ic_marker_24dp)));
//                map.addImage("quake-triangle-icon-id", BitmapUtils.getBitmapFromDrawable(
//                        getResources().getDrawable(R.drawable.ic_marker_24dp)));
            }
                private void addClusteredGeoJsonSource(MapboxMap mapboxMap) {

        // Add a new source from our GeoJSON data and set the 'cluster' option to true.
//        try {
//            mapboxMap.addSource(
//                    // Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
//                    // 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
//                    new GeoJsonSource("earthquakes",
//                            new URL("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
//                            new GeoJsonOptions()
//                                    .withCluster(true)
//                                    .withClusterMaxZoom(15) // Max zoom to cluster points on
//                                    .withClusterRadius(20) // Use small cluster radius for the hotspots look
//                    )
//            );
//        } catch (MalformedURLException malformedUrlException) {
//            Log.e("CreateHotspotsActivity", "Check the URL " + malformedUrlException.getMessage());
//        }

        // Use the earthquakes source to create four layers:
        // three for each cluster category, and one for unclustered points

        // Each point range gets a different fill color.
//        final int[][] layers = new int[][] {
//                new int[] {150, Color.parseColor("#E55E5E")},
//                new int[] {20, Color.parseColor("#F9886C")},
//                new int[] {0, Color.parseColor("#FBB03B")}
//        };

//        CircleLayer unclustered = new CircleLayer("unclustered-points", "earthquakes");
//        unclustered.setProperties(
//                circleColor(Color.parseColor("#FBB03B")),
//                circleRadius(20f),
//                circleBlur(1f));
//        unclustered.setFilter(Expression.neq(get("cluster"), literal(true)));
////        mapboxMap.addLayerBelow(unclustered, "building");
//
//        for (int i = 0; i < layers.length; i++) {
//            CircleLayer circles = new CircleLayer("cluster-" + i, "earthquakes");
//            circles.setProperties(
//                    circleColor(layers[i][1]),
//                    circleRadius(70f),
//                    circleBlur(1f)
//            );
//            Expression pointCount = toNumber(get("point_count"));
//            circles.setFilter(
//                    i == 0
//                            ? Expression.gte(pointCount, literal(layers[i][0])) :
//                            Expression.all(
//                                    Expression.gte(pointCount, literal(layers[i][0])),
//                                    Expression.lt(pointCount, literal(layers[i - 1][0]))
//                            )
//            );
////            mapboxMap.addLayerBelow(circles, "building");
//        }





    }
    private void addClusteredGeoJsonSource(@NonNull Style loadedMapStyle) {

// Add a new source from the GeoJSON data and set the 'cluster' option to true.
        try {
            loadedMapStyle.addSource(
// Point to GeoJSON data. This example visualizes all M1.0+ earthquakes from
// 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
                    new GeoJsonSource("earthquakes",
                            new URL("https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson"),
                            new GeoJsonOptions()
                                    .withCluster(true)
                                    .withClusterMaxZoom(15)
                                    .withClusterRadius(20)
                    )
            );
        } catch (MalformedURLException malformedUrlException) {
            Log.e("dataClusterActivity", "Check the URL " + malformedUrlException.getMessage());
        }


// Use the earthquakes GeoJSON source to create three layers: One layer for each cluster category.
// Each point range gets a different fill color.
        int[][] layers = new int[][] {
                new int[] {150, ContextCompat.getColor(this, R.color.mapboxRed)},
                new int[] {20, ContextCompat.getColor(this, R.color.mapboxGreen)},
                new int[] {0, ContextCompat.getColor(this, R.color.mapbox_blue)}
        };

//Creating a marker layer for single data points
        SymbolLayer unclustered = new SymbolLayer("unclustered-points", "earthquakes");

        unclustered.setProperties(
                iconImage("cross-icon-id"),
                iconSize(
                        division(
                                get("mag"), literal(4.0f)
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

        //Add the count labels
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

}

