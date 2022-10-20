package com.example.candydispenser.Adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.candydispenser.Model.Basket
import com.example.candydispenser.Model.Item
import com.example.candydispenser.R
import com.example.candydispenser.databinding.HomeItemsBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class HomeAdapter(private val itemlist:ArrayList<Item>,private val context: Context):RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(val binding: HomeItemsBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=HomeItemsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=itemlist[position]

        holder.binding.itemName.text=item.name
        holder.binding.itemPrice.text="Ksh.${item.price}"
        Picasso.get().load(item.image).placeholder(R.drawable.not_available).into(holder.binding.imageItem)
        isAdded(holder.binding.basket,item.itemid,holder.binding.listCount,holder.binding.listTotal)

        var count=0
        var total=0
        holder.binding.listCount.setText("0")
        holder.binding.listTotal.text="Ksh.0"
        holder.binding.listDec.setOnClickListener {
            if (count>0){
                count--
                holder.binding.listCount.setText(count.toString())
                total=item.price!!.toInt() * count
                val msg=StringBuilder()
                msg.append("Ksh.")
                msg.append(total.toString())
                holder.binding.listTotal.text=msg
            }
        }
        holder.binding.listInc.setOnClickListener {
            count++
            holder.binding.listCount.setText(count.toString())
            total=item.price!!.toInt() * count
            val msg=StringBuilder()
            msg.append("Ksh.")
            msg.append(total.toString())
            holder.binding.listTotal.text=msg
        }
        holder.binding.listCount.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addInfo(p0.toString(),holder.binding.basket,item.itemid,item.price,item.name)
            }

        })
    }

    private fun isAdded(basket: MaterialButton, itemid: String?, listCount: EditText, listTotal: TextView) {
        val fUser=Firebase.auth.currentUser!!
        Firebase.database.getReference("Basket").child(fUser.uid).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(itemid!!).exists()){
                    basket.text="update"
                    val item=snapshot.child(itemid).getValue(Basket::class.java)!!
                    listCount.setText(item.count)
                    listTotal.text="Ksh.${item.total}"
                }else{
                    basket.text="add"
                    listCount.setText("0")
                    listTotal.text="Ksh.0"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun addInfo(
        count: String,
        basket: MaterialButton,
        itemid: String?,
        price: String?,
        name: String?
    ) {
        val fUser=Firebase.auth.currentUser!!
        basket.setOnClickListener {btn->
            if (basket.text.equals("add")){
                val total=count.toInt() * price!!.toInt()
                val items= hashMapOf<String,Any>()
                items["itemid"]=itemid!!
                items["count"]=count
                items["total"]=total.toString()
                Firebase.database.getReference("Basket").child(fUser.uid).child(itemid).setValue(items).addOnCompleteListener { task->
                    if (task.isSuccessful){
                        Snackbar.make(btn,"$name added to basket",Snackbar.LENGTH_LONG).show()
                    }
                }
            }else{
                val total=count.toInt() * price!!.toInt()
                val items= hashMapOf<String,Any>()
                items["itemid"]=itemid!!
                items["count"]=count
                items["total"]=total.toString()
                Firebase.database.getReference("Basket").child(fUser.uid).child(itemid).updateChildren(items).addOnCompleteListener { task->
                    if (task.isSuccessful){
                        Snackbar.make(btn,"$name updated successfully",Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return itemlist.size
    }
}