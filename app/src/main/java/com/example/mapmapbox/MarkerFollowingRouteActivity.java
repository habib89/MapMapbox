package com.example.mapmapbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.service.autofill.FieldClassification;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.amazonaws.RequestClientOptions;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.service.models.DirectionsFeature;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.Constants;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.commons.models.Position;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.LogRecord;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.api.directions.v5.DirectionsCriteria.OVERVIEW_FULL;
import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_DRIVING;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotationAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MarkerFollowingRouteActivity extends AppCompatActivity implements Callback<DirectionsResponse> {

    private static final String TAG = "MarkerFollowingRoute";
    private static final String DOT_SOURCE_ID = "dot-source-id";
    private static final String LINE_SOURCE_ID = "line-source-id";
    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    private int count = 0;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Marker marker;
    private MarkerOptions markerOptions;
    private Handler handler;
    private Runnable runnable;
    private Location userloacation;
    private GeoJsonSource dotGeoJsonSource;
    private ValueAnimator markerIconAnimator;
    private TurfMeasurement turfMeasurement;
    private LatLng markerIconCurrentLocation;
    private List<Point> routeCoordinateList;
    private List<LatLng> polyz;
    private Point destination;
    private Point origin;
    private float headDirection = 0f;
    private MapboxDirections client;
    private DirectionsRoute currentRoute;
    private NavigationRoute navigationRoute;
    private static final String DRIVING_ROUTE_POLYLINE_SOURCE_ID = "DRIVING_ROUTE_POLYLINE_SOURCE_ID";
    private List<Feature> drivingRoutePolyLineFeatureList;
    List<List<HashMap<String, String>>> result;

//    private final RerouteActivityLocationCallback callback = new RerouteActivityLocationCallback(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.accses_tokon));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_style_line_layer);

        // Initialize the mapboxMap view
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                MarkerFollowingRouteActivity.this.mapboxMap = mapboxMap;
                mapboxMap.getUiSettings().setRotateGesturesEnabled(true);
                mapboxMap.getUiSettings().setAttributionEnabled(false);
                mapboxMap.getUiSettings().setCompassEnabled(true);
                mapboxMap.getUiSettings().setLogoEnabled(false);
                mapboxMap.getUiSettings().setZoomGesturesEnabled(true);
                animateMarkerNew(userloacation,marker);
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        new LoadGeoJson(MarkerFollowingRouteActivity.this).execute();
                        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                        .target(mapboxMap.getCameraPosition().target)
                                        .zoom(3)
                                        .bearing(160)
                                        .tilt(45)
                                        .build()));

                    }
                });
            }
        });
    }

    /**
     * Add data to the map once the GeoJSON has been loaded
     *
     * @param featureCollection returned GeoJSON FeatureCollection from the async task
     */
    private void initData(@NonNull FeatureCollection featureCollection) {
        LineString lineString = (LineString) featureCollection.features().get(0).geometry();
        routeCoordinateList = lineString.coordinates();
        if (mapboxMap != null) {
            Style style = mapboxMap.getStyle();
            if (style != null) {
                initSources(style, featureCollection);
                initSymbolLayer(style,marker);
                initDotLinePath(style);
                initRunnable();
                animateMarkerNew(userloacation,marker);
//                rotateMarker(style,result);

            }
        }
    }

    /**
     * Set up the repeat logic for moving the icon along the route.
     */

    private void initRunnable() {

        // Animating the marker requires the use of both the ValueAnimator and a handler.
        // The ValueAnimator is used to move the marker between the GeoJSON points, this is
        // done linearly. The handler is used to move the marker along the GeoJSON points.

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {


                if ((routeCoordinateList.size() - 1 > count)) {
                    Point nextLocation = routeCoordinateList.get(count + 1);

                    if (markerIconAnimator != null && markerIconAnimator.isStarted()) {
                        markerIconCurrentLocation = (LatLng) markerIconAnimator.getAnimatedValue();
                        markerIconAnimator.cancel();
                    }

                    if (latLngEvaluator != null) {

                        markerIconAnimator = ObjectAnimator.ofObject(latLngEvaluator, count == 0 ?
                                        new LatLng(37.61501, -122.385374)
                                                : markerIconCurrentLocation,
                                        new LatLng(nextLocation.latitude(), nextLocation.longitude()))
                                .setDuration(300);



                        markerIconAnimator.setInterpolator(new LinearInterpolator());
                        markerIconAnimator.addUpdateListener(animatorUpdateListener);
                        markerIconAnimator.start();

                        if (mapboxMap.getStyle() != null){
                            GeoJsonSource source = mapboxMap.getStyle().getSourceAs(DRIVING_ROUTE_POLYLINE_SOURCE_ID);
                            if (source != null) {
                                source.setGeoJson(FeatureCollection.fromFeatures(drivingRoutePolyLineFeatureList));
                            }
                        }
                        //Keeping the current point count we are on.
                        count++;
                        // Once we finish we need to repeat the entire process by executing the
                        // handler again once the ValueAnimator is finished.



                        handler.postDelayed(this, 300);
                    }
                }
            }
        };
        handler.post(runnable);
    }

    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                    if (dotGeoJsonSource != null) {
                        dotGeoJsonSource.setGeoJson(Point.fromLngLat(
                                animatedPosition.getLongitude(), animatedPosition.getLatitude()));
//                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                        for (LatLng latLng :polyz)
//                            builder.include(latLng);
//                        LatLngBounds bounds = builder.build();
//                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
//                        mapboxMap.animateCamera(cameraUpdate);
                    }
                }
            };

    /**
     * Add various sources to the map.
     */
    private void initSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
        dotGeoJsonSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection);
        loadedMapStyle.addSource(dotGeoJsonSource);
        loadedMapStyle.addSource(new GeoJsonSource(LINE_SOURCE_ID, featureCollection));
    }

    /**
     * Add the marker icon SymbolLayer.
     */
    @SuppressLint("Range")
    private void initSymbolLayer(@NonNull Style loadedMapStyle, Marker marker) {


//        final LatLng startPosition = marker.getPosition();

        Bitmap compassNeedleSymbolLayerIcon = BitmapFactory.decodeResource(
                getResources(), R.drawable.icon_m32);
        loadedMapStyle.addImage("compass-needle-image-id", compassNeedleSymbolLayerIcon);

//        GeoJsonSource geoJsonSource = new GeoJsonSource("geojson-source",
//                Feature.fromGeometry(Point.fromLngLat(37.61501, -122.385374)));
//        loadedMapStyle.addSource(geoJsonSource);

        loadedMapStyle.addSource(new GeoJsonSource("line-source",
                FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                        LineString.fromLngLats(routeCoordinateList)
                )})));

        LatLng startPosition = null;
        SymbolLayer aircraftLayer = new SymbolLayer("symbol-layer-id", DOT_SOURCE_ID)
                .withProperties(
                        PropertyFactory.iconImage("compass-needle-image-id"),

                        PropertyFactory.iconRotate(Expression.get("")),
//                        PropertyFactory.iconRotate(getBearing(startPosition,
//                                new LatLng(destination.latitude(),
//                                        destination.longitude()))),
                        PropertyFactory.iconIgnorePlacement(true),
                        PropertyFactory.iconAllowOverlap(true)

                );

        if (aircraftLayer != null){
            aircraftLayer.getIconRotate();
            final Point TOWER_BRIDGE = Point.fromLngLat(-122.385374, 37.61501);
            final Point LONDON_EYE = Point.fromLngLat(-122.385374, 37.61501);

            // Run the points through the Turf Measurement method and receive the distance.
            TurfMeasurement.distance(TOWER_BRIDGE, LONDON_EYE, TurfConstants.UNIT_FEET);
            Log.d("TAG","MARKER ROTATE"+aircraftLayer);
        }



//        SymbolLayer aircraftLayer = new SymbolLayer("symbol-layer-id", DOT_SOURCE_ID)
//                .withProperties(
//                        PropertyFactory.iconImage("compass-needle-image-id"),
//                        PropertyFactory.iconRotate(0f),
//                        PropertyFactory.iconIgnorePlacement(true),
//                        PropertyFactory.iconAllowOverlap(true)
//                );

        loadedMapStyle.addLayer(aircraftLayer);



    }
//    public double getDirection() {
//        // getBearing() aligns with CoreGL
//        return markerIconAnimator.getBearing();
//    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .origin(origin)
                .destination(destination)
                .accessToken(Mapbox.getAccessToken())
                .build().getRoute(this);
    }
    public double headDirectionn (LatLng beginLatLng, LatLng endLatLng) {
        double f1 = Math.PI * beginLatLng.getLatitude() / 180;
        double f2 = Math.PI * endLatLng.getLongitude() / 180;
        double dl = Math.PI * (endLatLng.getLongitude() - beginLatLng.getLongitude()) / 180;
        return Math.atan2(Math.sin(dl) * Math.cos(f2) , Math.cos(f1)
                * Math.sin(f2) - Math.sin(f1)
                * Math.cos(f2) * Math.cos(dl));
    }
    private void animateMarkerNew(final Location destination, final Marker marker) {

        if (marker != null) {

            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

//            final float startRotation = marker.getRotation();
            final LatLngInterpolatorNew latLngInterpolator = new LatLngInterpolatorNew.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);

                        marker.setPosition(newPosition);

                        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                .target(newPosition)
                                .zoom(15.5f)
                                .build()));

//                        marker.setRotation(getBearing(startPosition, new LatLng(destination.getLatitude(), destination.getLongitude())));
                    } catch (Exception ex) {
                        //I don't care atm..
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    // if (mMarker != null) {
                    // mMarker.remove();
                    // }
                    // mMarker = googleMap.addMarker(new MarkerOptions().position(endPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));

                }
            });
            valueAnimator.start();
        }
    }

    @Override
    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
        Timber.d(call.request().url().toString());
        if (response.body() != null) {
            if (!response.body().routes().isEmpty()) {
                DirectionsRoute route = response.body().routes().get(0);
//                drawRoute(route);
//                resetLocationEngine(route);
//                navigation.startNavigation(route);
//                mapboxMap.addOnMapClickListener(this);
//                tracking = true;
            }
        }
    }

    @Override
    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

    }

    private interface LatLngInterpolatorNew {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolatorNew {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.getLatitude() - a.getLatitude()) * fraction + a.getLatitude();
                double lngDelta = b.getLongitude() - a.getLongitude();
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.getLongitude();
                return new LatLng(lat, lng);
            }
        }
    }
    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.getAltitude() - end.getLatitude());
        double lng = Math.abs(begin.getLongitude() - end.getLongitude());

        if (begin.getLatitude() < end.getLatitude() && begin.getLongitude() < end.getLongitude())
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.getLatitude() >= end.getLatitude() && begin.getLongitude() < end.getLongitude())
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.getLatitude() >= end.getLatitude() && begin.getLongitude() >= end.getLongitude())
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.getLatitude() < end.getLatitude() && begin.getLongitude() >= end.getLongitude())
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

//    public void rotateMarker(Style style,List<List<HashMap<String, String>>> result) {
//
//        PolylineOptions lineOptions = null;
//
//        if (result != null){
//            for (int i = 0; i < result.size(); i++){
////                pointF = new ArrayList<LatLng>();
//                lineOptions = new PolylineOptions();
//
//                final List<HashMap<String, String>> path = result.get(i);
//                for (int j = 0; j < path.size(); j++) {
//                    HashMap<String, String> point = path.get(j);
//                    if (j == 0) {
////                        estimateDistance = (String) point.get("distance");
//
//                        continue;
//                    }else if (j == 1){
////                        estimeteDuration = (String) point.get("duration");
//                        continue;
//                    }
//                    double lat = Double.parseDouble(point.get("lat"));
//                    double lng = Double.parseDouble(point.get("lng"));
//                    LatLng position = new LatLng(lat, lng);
//                    // points.clear();
////                    .add(position);
//                    }
//
//            }
//        }
//
//
//    }
    /**
     * Add the LineLayer for the marker icon's travel route.
     */
    private void initDotLinePath(@NonNull Style loadedMapStyle) {
//        loadedMapStyle.addLayer(new LineLayer("line-layer-id", LINE_SOURCE_ID)
//                .withProperties(
//                lineColor(Color.parseColor("#689f38")),
//                lineWidth(4f)
//        ));
        loadedMapStyle.addLayer(new LineLayer("line-layer-id", LINE_SOURCE_ID)
                .withProperties(
                        PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                        PropertyFactory.lineOpacity(.7f),
//
                PropertyFactory.lineWidth(3f),
                PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
        ));

    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // When the activity is resumed we restart the marker animating.
        if (handler != null && runnable != null) {
            handler.post(runnable);
        }
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
// Check if the marker is currently animating and if so, we pause the animation so we aren't
// using resources when the activities not in view.
        if (handler != null && runnable != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (markerIconAnimator != null) {
            markerIconAnimator.cancel();
        }
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * We want to load in the GeoJSON file asynchronous so the UI thread isn't handling the file
     * loading. The GeoJSON file we are using is stored in the assets folder, you could also get
     * this information from the Mapbox mapboxMap matching API during runtime.
     */
    private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

        private WeakReference<MarkerFollowingRouteActivity> weakReference;

        LoadGeoJson(MarkerFollowingRouteActivity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        protected FeatureCollection doInBackground(Void... voids) {
            try {
                MarkerFollowingRouteActivity activity = weakReference.get();
                if (activity != null) {
                    InputStream inputStream = activity.getAssets().open("matched_route.geojson");
                    return FeatureCollection.fromJson(convertStreamToString(inputStream));

                }
            } catch (Exception exception) {
                Timber.e(exception.toString());
            }
            return null;
        }

        static String convertStreamToString(InputStream is) {
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }

        @Override
        protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            MarkerFollowingRouteActivity activity = weakReference.get();
            if (activity != null && featureCollection != null) {
                activity.initData(featureCollection);
            }
        }
    }

    /**
     * Method is used to interpolate the SymbolLayer icon animation.
     */
    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };
    private static class RerouteActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<RerouteActivity> activityWeakReference;

        RerouteActivityLocationCallback(RerouteActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            RerouteActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) {
                    return;
                }
//                activity.updateLocation(location);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            Timber.e(exception);
        }
    }

    private void initRouteCoordinates() {
        // Create a list to store our line coordinates.
        routeCoordinateList = new ArrayList<>();
        routeCoordinateList.add(Point.fromLngLat(-118.39439114221236, 33.397676454651766));
        routeCoordinateList.add(Point.fromLngLat(-118.39421054012902, 33.39769799454838));
        routeCoordinateList.add(Point.fromLngLat(-118.39408583869053, 33.39761901490136));
        routeCoordinateList.add(Point.fromLngLat(-118.39388373635917, 33.397328225582285));
        routeCoordinateList.add(Point.fromLngLat(-118.39372033447427, 33.39728514560042));
        routeCoordinateList.add(Point.fromLngLat(-118.3930882271826, 33.39756875508861));
        routeCoordinateList.add(Point.fromLngLat(-118.3928216241072, 33.39759029501192));
        routeCoordinateList.add(Point.fromLngLat(-118.39227981785722, 33.397234885594564));
        routeCoordinateList.add(Point.fromLngLat(-118.392021814881, 33.397005125197666));
        routeCoordinateList.add(Point.fromLngLat(-118.39090810203379, 33.396814854409186));
        routeCoordinateList.add(Point.fromLngLat(-118.39040499623022, 33.39696563506828));
        routeCoordinateList.add(Point.fromLngLat(-118.39005669221234, 33.39703025527067));
        routeCoordinateList.add(Point.fromLngLat(-118.38953208616074, 33.39691896489222));
        routeCoordinateList.add(Point.fromLngLat(-118.38906338075398, 33.39695127501678));
        routeCoordinateList.add(Point.fromLngLat(-118.38891287901787, 33.39686511465794));
        routeCoordinateList.add(Point.fromLngLat(-118.38898167981154, 33.39671074380141));
        routeCoordinateList.add(Point.fromLngLat(-118.38984598978178, 33.396064537239404));
        routeCoordinateList.add(Point.fromLngLat(-118.38983738968255, 33.39582400356976));
        routeCoordinateList.add(Point.fromLngLat(-118.38955358640874, 33.3955978295119));
        routeCoordinateList.add(Point.fromLngLat(-118.389041880506, 33.39578092284221));
        routeCoordinateList.add(Point.fromLngLat(-118.38872797688494, 33.3957916930261));
        routeCoordinateList.add(Point.fromLngLat(-118.38817327048618, 33.39561218978703));
        routeCoordinateList.add(Point.fromLngLat(-118.3872530598711, 33.3956265500598));
        routeCoordinateList.add(Point.fromLngLat(-118.38653065153775, 33.39592811523983));
        routeCoordinateList.add(Point.fromLngLat(-118.38638444985126, 33.39590657490452));
        routeCoordinateList.add(Point.fromLngLat(-118.38638874990086, 33.395737842093304));
        routeCoordinateList.add(Point.fromLngLat(-118.38723155962309, 33.395027006653244));
        routeCoordinateList.add(Point.fromLngLat(-118.38734766096238, 33.394441819579285));
        routeCoordinateList.add(Point.fromLngLat(-118.38785936686516, 33.39403972556368));
        routeCoordinateList.add(Point.fromLngLat(-118.3880743693453, 33.393616088784825));
        routeCoordinateList.add(Point.fromLngLat(-118.38791956755958, 33.39331092541894));
        routeCoordinateList.add(Point.fromLngLat(-118.3874852625497, 33.39333964672257));
        routeCoordinateList.add(Point.fromLngLat(-118.38686605540683, 33.39387816940854));
        routeCoordinateList.add(Point.fromLngLat(-118.38607484627983, 33.39396792286514));
        routeCoordinateList.add(Point.fromLngLat(-118.38519763616081, 33.39346171215717));
        routeCoordinateList.add(Point.fromLngLat(-118.38523203655761, 33.393196040109466));
        routeCoordinateList.add(Point.fromLngLat(-118.3849955338295, 33.393023711860515));
        routeCoordinateList.add(Point.fromLngLat(-118.38355931726203, 33.39339708930139));
        routeCoordinateList.add(Point.fromLngLat(-118.38323251349217, 33.39305243325907));
        routeCoordinateList.add(Point.fromLngLat(-118.3832583137898, 33.39244928189641));
        routeCoordinateList.add(Point.fromLngLat(-118.3848751324406, 33.39108499551671));
        routeCoordinateList.add(Point.fromLngLat(-118.38390332123025, 33.39012280171983));
        routeCoordinateList.add(Point.fromLngLat(-118.38318091289693, 33.38941192035707));
        routeCoordinateList.add(Point.fromLngLat(-118.38271650753981, 33.3896129783018));
        routeCoordinateList.add(Point.fromLngLat(-118.38275090793661, 33.38902416443619));
        routeCoordinateList.add(Point.fromLngLat(-118.38226930238106, 33.3889451769069));
        routeCoordinateList.add(Point.fromLngLat(-118.38258750605169, 33.388420985121336));
        routeCoordinateList.add(Point.fromLngLat(-118.38177049662707, 33.388083490107284));
        routeCoordinateList.add(Point.fromLngLat(-118.38080728551597, 33.38836353925403));
        routeCoordinateList.add(Point.fromLngLat(-118.37928506795642, 33.38717870977523));
        routeCoordinateList.add(Point.fromLngLat(-118.37898406448423, 33.3873079646849));
        routeCoordinateList.add(Point.fromLngLat(-118.37935386875012, 33.38816247841951));
        routeCoordinateList.add(Point.fromLngLat(-118.37794345248027, 33.387810620840135));
        routeCoordinateList.add(Point.fromLngLat(-118.37546662390886, 33.38847843095069));
        routeCoordinateList.add(Point.fromLngLat(-118.37091717142867, 33.39114243958559));

    }
}
