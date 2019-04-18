package com.example.mapmapbox;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.PluralsRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;
import java.util.Map;

public class QueryFeatureActivity extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapClickListener {

    private MapView mapView;
    private Marker featureMarker;
    private MapboxMap mapboxMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.accses_tokon));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_info_window_symbol_layer);

        mapView = findViewById(R.id.mapView);
//        Button button = (Button)findViewById(R.id.button);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

//        mapboxMap.setStyle(new Style.Builder().fromUrl("https://www.mapbox.com/android-docs/files/mapbox-raster-v8.json"));
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"tes",Toast.LENGTH_SHORT).show();
//                mapboxMap.setStyle(new Style.Builder().fromUrl("asset://local_style_file.json"));
//            }
//        });



    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        QueryFeatureActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapboxMap.addOnMapClickListener(QueryFeatureActivity.this);
                Toast.makeText(QueryFeatureActivity.this,
                        getString(R.string.click_on_map_instruction), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        LatLng center = mapboxMap.getCameraPosition().target;
//        PointF pixel = null;
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(center);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel,"water");

        if (features.size() > 0) {
            Feature feature = features.get(0);

            String property;

            StringBuilder stringBuilder = new StringBuilder();
            if (feature.properties() != null) {
                for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
                    stringBuilder.append(String.format("%s - %s", entry.getKey(), entry.getValue()));
                    stringBuilder.append(System.getProperty("line.separator"));
                }

                featureMarker = mapboxMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(getString(R.string.query_feature_marker_title))
                        .snippet(stringBuilder.toString())
                );

            } else {
                property = getString(R.string.query_feature_marker_snippet);
                featureMarker = mapboxMap.addMarker(new MarkerOptions()
                        .position(point)
                        .snippet(property)
                );
            }
        } else {
            featureMarker = mapboxMap.addMarker(new MarkerOptions()
                    .position(point)
                    .snippet(getString(R.string.query_feature_marker_snippet))
            );
        }
        mapboxMap.selectMarker(featureMarker);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}