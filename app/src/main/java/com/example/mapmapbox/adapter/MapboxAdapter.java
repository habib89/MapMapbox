package com.example.mapmapbox.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.mapmapbox.model.IndividualLocation;

import java.util.List;

public class MapboxAdapter extends RecyclerView.Adapter<MapboxAdapter.ViewHolder> {

    private List<IndividualLocation> listOfLocations;
    private Context context;
    private int selectedTheme;
//    private static ClickListener clickListener;
    private Drawable emojiForCircle = null;
    private Drawable backgroundCircle = null;
    private int upperCardSectionColor = 0;

    private int locationNameColor = 0;
    private int locationAddressColor = 0;
    private int locationPhoneNumColor = 0;
    private int locationPhoneHeaderColor = 0;
    private int locationHoursColor = 0;
    private int locationHoursHeaderColor = 0;
    private int locationDistanceNumColor = 0;
    private int milesAbbreviationColor = 0;

    public MapboxAdapter(List<IndividualLocation> styles,
                                       Context context, int selectedTheme) {
        this.context = context;
        this.listOfLocations = styles;
        this.selectedTheme = selectedTheme;
//        this.clickListener = cardClickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
