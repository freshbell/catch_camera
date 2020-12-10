package com.work.catch_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static android.graphics.BitmapFactory.*;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.CustomViewHolder> {

    private ArrayList<MainData> arrayList;
    private Context context;

    public MainAdapter(ArrayList<MainData> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public MainAdapter(ArrayList<MainData> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MainAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list,parent,false);
        CustomViewHolder holder = new CustomViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, int position) {

        Bitmap bitmap = null;
        byte[] bytes = arrayList.get(position).getImage();

        bitmap = decodeByteArray(bytes,0,bytes.length);
        holder.image.setImageBitmap(bitmap);
        holder.location.setText(arrayList.get(position).getLocation());
        holder.position.setText(arrayList.get(position).getPosition());
        holder.date.setText(arrayList.get(position).getDate());

        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // String curName = holder.tv_name.getText().toString();
               // Toast.makeText(v.getContext(), "a", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnLongClickListener((new View.OnLongClickListener(){
            public boolean onLongClick(View v) {
                remove(holder.getAdapterPosition());
                return true;
            }
        }));
    }

    @Override
    public int getItemCount() {

        return (null != arrayList?arrayList.size():0);
    }

    public void remove(int position) {
        try {
            arrayList.remove(position);
            notifyItemRemoved(position);
        } catch(IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        protected ImageView image;
        protected TextView location;
        protected TextView position;
        protected TextView date;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.image = (ImageView)itemView.findViewById(R.id.log_image);
            this.location = (TextView)itemView.findViewById(R.id.log_location);
            this.position = (TextView)itemView.findViewById(R.id.log_position);
            this.date = (TextView)itemView.findViewById(R.id.log_date);
        }
    }
}
