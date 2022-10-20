package com.example.candydispenser.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.candydispenser.Model.Basket
import com.example.candydispenser.Model.Item
import com.example.candydispenser.R
import com.example.candydispenser.databinding.BasketItemsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class BasketAdapter(private val itemlist:ArrayList<Basket>):RecyclerView.Adapter<BasketAdapter.ViewHolder>() {

    class ViewHolder(val binding: BasketItemsBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=BasketItemsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=itemlist[position]

        holder.binding.bucketCount.text=item.count
        holder.binding.bucketTotal.text="Ksh.${item.total}"

        Firebase.database.getReference("Items").child(item.itemid!!).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val good=snapshot.getValue(Item::class.java)!!
                holder.binding.bucketName.text=good.name
                Picasso.get().load(good.image).placeholder(R.drawable.not_available).into(holder.binding.bucketImage)
                holder.binding.bucketPrice.text="@Ksh.${good.price}"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }
}