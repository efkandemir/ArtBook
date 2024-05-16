package com.efkan.artbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.efkan.artbook.databinding.RecyclerRowBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter <ArtAdapter.ArtHolder> {
    ArrayList<Art> artArrayList;
    public ArtAdapter(ArrayList<Art> artArrayList)
    {
        this.artArrayList=artArrayList;
    }
    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {   //burada bindingi bağlıyorduk.yani xml'imizi bağlama işlemini burada yapacağız.
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, @SuppressLint("RecyclerView") int position) {  //layout içerisinde hangi verileri göstermek istiyorsak burada gösteriyoruz.
     holder.binding.recyclerViewtextView.setText(artArrayList.get(position).name);
     holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent=new Intent(holder.itemView.getContext(), activity_art.class);
             intent.putExtra("info","old");
             intent.putExtra("id",artArrayList.get(position).id);
             holder.itemView.getContext().startActivity(intent);
         }
     });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
    private RecyclerRowBinding binding;
        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
