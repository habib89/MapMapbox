package com.example.mapmapbox.until;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mapmapbox.InfoWindowSymbolLayerActivity;
import com.example.mapmapbox.R;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import static android.media.midi.MidiDeviceInfo.PROPERTY_NAME;

public class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {
    private static final String PROPERTY_CAPITAL = "capital";
    private static final String PROPERTY_SPEED = "capital";

    private final HashMap<String, View> viewMap = new HashMap<>();
    private final WeakReference<InfoWindowSymbolLayerActivity> activityRef;
    private final boolean refreshSource;

    GenerateViewIconTask(InfoWindowSymbolLayerActivity activity, boolean refreshSource) {
        this.activityRef = new WeakReference<>(activity);
        this.refreshSource = refreshSource;
    }
    GenerateViewIconTask(InfoWindowSymbolLayerActivity activity) {
        this(activity, true);
    }

    @SuppressLint("StringFormatInvalid")
    @SuppressWarnings("WrongThread")
    @Override
    protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
        InfoWindowSymbolLayerActivity activity = activityRef.get();
        if (activity != null) {
            HashMap<String, Bitmap> imagesMap = new HashMap<>();
            LayoutInflater inflater = LayoutInflater.from(activity);

            FeatureCollection featureCollection = params[0];

            for (Feature feature : featureCollection.features()) {

                BubbleLayout bubbleLayout = (BubbleLayout)
                        inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

                String name = feature.getStringProperty(PROPERTY_NAME);
                TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                titleTextView.setText(name);

                String style = feature.getStringProperty(PROPERTY_CAPITAL);
                TextView descriptionTextView = bubbleLayout.findViewById(R.id.vIdvms);
                descriptionTextView.setText(
                        String.format(activity.getString(R.string.capital), style));

                String idSped = feature.getStringProperty(PROPERTY_SPEED);
                TextView disidSped = bubbleLayout.findViewById(R.id.idSped);
                disidSped.setText(
                        String.format(activity.getString(R.string.capital), idSped));
                int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                bubbleLayout.measure(measureSpec, measureSpec);

                int measuredWidth = bubbleLayout.getMeasuredWidth();

                bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);
                SymbolGenerator symbolGenerator = new SymbolGenerator();
//                Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                Bitmap bitmap = symbolGenerator.generate(bubbleLayout);
                imagesMap.put(name, bitmap);
                viewMap.put(name, bubbleLayout);
            }

            return imagesMap;
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
        super.onPostExecute(bitmapHashMap);
        InfoWindowSymbolLayerActivity activity = activityRef.get();
        if (activity != null && bitmapHashMap != null) {
            activity.setImageGenResults(bitmapHashMap);
            if (refreshSource) {
                activity.refreshSource();
            }
        }
        Toast.makeText(activity, "tap_on_marker_instruction", Toast.LENGTH_SHORT).show();
    }

}
