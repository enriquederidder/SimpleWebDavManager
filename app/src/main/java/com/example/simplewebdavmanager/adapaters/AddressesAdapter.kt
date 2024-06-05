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

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ip_address, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder ViewHolder containing the View of the item to be displayed
     * @param position positidon of the item
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addresses[position]
        holder.addressTextView.text = address
    }

    /**
     * Returns the number of items in the adapter.
     *
     * @return
     */
    override fun getItemCount(): Int {
        return addresses.size
    }

    /**
     * Updates the list of IP addresses and notifies the adapter.
     *
     * @param newAddresses List of IP's
     */
    fun updateAddresses(newAddresses: List<String>) {
        addresses = newAddresses
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for the RecyclerView items.
     * And handles the click events on the items.
     *
     * @param itemView
     */
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

    /**
     * Interface for handling address selection events.
     *
     */
    interface OnAddressSelectedListener {
        fun onAddressSelected(address: String)
    }
}
