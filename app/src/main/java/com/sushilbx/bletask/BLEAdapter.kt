package com.sushilbx.bletask

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sushilbx.bletask.databinding.BleItemBinding

class BLEAdapter : ListAdapter<BluetoothDevice, BLEViewHolder>(diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BLEViewHolder {
        return BLEViewHolder(
            BleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BLEViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class BLEViewHolder(val b: BleItemBinding) : ViewHolder(b.root) {
    fun bind(model: BluetoothDevice) {
        val context = itemView.context
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        ) {
            if (model.name ==null){
                b.tvName.text = "Unknown Name"
            }else{
                b.tvName.text = model.name
            }

            b.tvDeviceAddress.text = model.address
            itemView.setOnClickListener {
                val intent = Intent(context,BLEDeviceActivity::class.java)
                intent.putExtra("blutoothdevice", model)
                context.startActivity(intent)
            }
        }
    }

}


val diff = object : DiffUtil.ItemCallback<BluetoothDevice>() {

    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem.toString() == newItem.toString()
    }

    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        return oldItem.toString() == newItem.toString()
    }
}

