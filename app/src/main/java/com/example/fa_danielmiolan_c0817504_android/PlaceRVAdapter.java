package com.example.fa_danielmiolan_c0817504_android;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class PlaceRVAdapter extends ListAdapter<Place, PlaceRVAdapter.ViewHolder> {
    private PlaceRVAdapter.OnItemClickListener listener;

    public PlaceRVAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Place> DIFF_CALLBACK = new DiffUtil.ItemCallback<Place>() {
        @Override
        public boolean areItemsTheSame(Place oldItem, Place newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Place oldItem, Place newItem) {
            return oldItem.getId() == newItem.getId();
        }
    };

    @NonNull
    @Override
    public PlaceRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_rv_item, parent, false);
        return new PlaceRVAdapter.ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceRVAdapter.ViewHolder holder, int position) {
        Place model = getPlaceAt(position);
        holder.tempID = model.getId();
        holder.addressTV.setText(model.getAddress());

        if (model.getStatus()) {
            holder.statusTV.setText("Visited");
        } else {
            holder.statusTV.setText("");
        }

        holder.latitudeTV.setText("" + model.getLatitude());
        holder.longitudeTV.setText("" + model.getLongitude());
    }

    public Place getPlaceAt(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        int tempID;
        TextView addressTV;
        TextView statusTV;
        TextView latitudeTV;
        TextView longitudeTV;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTV = itemView.findViewById(R.id.placeAddress);
            statusTV = itemView.findViewById(R.id.placeStatus);
            latitudeTV = itemView.findViewById(R.id.placeLat);
            longitudeTV = itemView.findViewById(R.id.placeLong);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Context context = v.getContext();

                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.putExtra("Place", getPlaceAt(position));
                    context.startActivity(intent);

                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Place model);
    }

    public void setOnItemClickListener(PlaceRVAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
