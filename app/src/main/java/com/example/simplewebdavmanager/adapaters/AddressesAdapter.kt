package com.example.simplewebdavmanager.adapaters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplewebdavmanager.R

/**
 * Adapter for the RecyclerView in the ConnectionDetailsFragment
 *
 * @property addresses List of IP addresses
 * @property listener OnAddressSelectedListener to handle address selection
 */
class AddressesAdapter(
    private var addresses: List<String>,
    private val listener: OnAddressSelectedListener
) : RecyclerView.Adapter<AddressesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ip_address, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addresses[position]
        holder.addressTextView.text = address
    }

    override fun getItemCount(): Int {
        return addresses.size
    }

    fun updateAddresses(newAddresses: List<String>) {
        addresses = newAddresses
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addressTextView: TextView = itemView.findViewById(R.id.textViewIpAddress)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val address = addresses[position]
                    listener.onAddressSelected(address)
                }
            }
        }
    }

    interface OnAddressSelectedListener {
        fun onAddressSelected(address: String)
    }
}
