package com.example.mapmapbox.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mapmapbox.R;
import com.example.mapmapbox.until.SingleRecyclerViewMatrixLocation;

import java.util.List;

public class MatrixApiLocationRecyclerViewAdapter extends
        RecyclerView.Adapter<MatrixApiLocationRecyclerViewAdapter.MyViewHolder> {


    private List<SingleRecyclerViewMatrixLocation> matrixLocationList;
    private Context context;

    public MatrixApiLocationRecyclerViewAdapter(Context context,
                                                List<SingleRecyclerViewMatrixLocation> matrixLocationList) {
        this.matrixLocationList = matrixLocationList;
        this.context = context;
    }
    @Override
    public MatrixApiLocationRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_matrix_card, parent, false);
        return new MatrixApiLocationRecyclerViewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MatrixApiLocationRecyclerViewAdapter.MyViewHolder holder, int position) {
        SingleRecyclerViewMatrixLocation singleRecyclerViewLocation = matrixLocationList.get(position);
//        holder.name.setText(singleRecyclerViewLocation.getName());

//        String finalDistance = singleRecyclerViewLocation.getDistanceFromOrigin()
//                == null ? "" : String.format(context.getString(R.string.miles_distance),
//                singleRecyclerViewLocation.getDistanceFromOrigin());
//        holder.distance.setText(finalDistance);
    }
    @Override
    public int getItemCount() {
        return matrixLocationList.size();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView distance;
        CardView singleCard;
        public MyViewHolder(@NonNull View view) {
            super(view);

            name = view.findViewById(R.id.boston_matrix_api_location_title_tv);
            distance = view.findViewById(R.id.boston_matrix_api_location_distance_tv);
            singleCard = view.findViewById(R.id.single_location_cardview);
        }
    }
}
