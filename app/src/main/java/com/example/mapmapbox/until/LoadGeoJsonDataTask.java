package com.example.mapmapbox.until;

import android.content.Context;
import android.os.AsyncTask;

import com.example.mapmapbox.InfoWindowSymbolLayerActivity;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import timber.log.Timber;

public class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {
    private static final String PROPERTY_SELECTED = "selected";


        private final WeakReference<InfoWindowSymbolLayerActivity> activityRef;

        public LoadGeoJsonDataTask(InfoWindowSymbolLayerActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {
            InfoWindowSymbolLayerActivity activity = activityRef.get();

            if (activity == null) {
                return null;
            }

            String geoJson = loadGeoJsonFromAsset(activity, "us_west_coast.geojson");
            return FeatureCollection.fromJson(geoJson);
        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            InfoWindowSymbolLayerActivity activity = activityRef.get();
            if (featureCollection == null || activity == null) {
                return;
            }

            for (Feature singleFeature : featureCollection.features()) {
                singleFeature.addBooleanProperty(PROPERTY_SELECTED, false);
            }

            activity.setUpData(featureCollection);

            new GenerateViewIconTask(activity).execute(featureCollection);
        }

        public String loadGeoJsonFromAsset(Context context, String filename) {
            try {
// Load GeoJSON file from local asset folder
                InputStream is = context.getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                return new String(buffer, "UTF-8");
            } catch (Exception exception) {
                Timber.e("Exception loading GeoJSON: %s", exception.toString());
                exception.printStackTrace();
//            throw new RuntimeException(exception);
                return null;
            }
        }

}
