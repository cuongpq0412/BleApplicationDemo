package com.example.bleapplicationdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bleapplicationdemo.databinding.ItemBleBinding;

public class BleAdapter extends RecyclerView.Adapter<BleAdapter.BleViewHolder> {
    private final IBle inter;

    public BleAdapter(IBle inter) {
        this.inter = inter;
    }

    @NonNull
    @Override
    public BleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBleBinding binding = ItemBleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BleViewHolder holder, int position) {
        CustomBluetoothDevice data = inter.data(position);
        holder.binding.setItem(data);
        holder.binding.btnPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inter.onPairDevice(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inter.count();
    }

    public interface IBle {
        int count();

        CustomBluetoothDevice data(int position);

        void onPairDevice(CustomBluetoothDevice data);
    }

    static class BleViewHolder extends RecyclerView.ViewHolder {
        ItemBleBinding binding;

        public BleViewHolder(ItemBleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
