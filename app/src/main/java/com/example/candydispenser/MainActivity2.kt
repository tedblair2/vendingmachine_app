package com.example.candydispenser

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.candydispenser.Adapter.BasketAdapter
import com.example.candydispenser.Model.Basket
import com.example.candydispenser.databinding.ActivityMain2Binding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import kotlin.collections.ArrayList

class MainActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityMain2Binding
    lateinit var adapter: BasketAdapter
    lateinit var itemlist:ArrayList<Basket>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar=supportActionBar!!
        val color= ColorDrawable(Color.parseColor("#3F51B5"))
        actionbar.setBackgroundDrawable(color)
        addMenuProvider(object :MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                actionbar.setDisplayHomeAsUpEnabled(true)
                menuInflater.inflate(R.menu.basket_top,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    android.R.id.home->onBackPressed()

                    R.id.complete-> showAlert()
                }
                return true
            }
        })
        binding.recyclerBasket.setHasFixedSize(true)
        binding.recyclerBasket.layoutManager=LinearLayoutManager(this)
        itemlist= arrayListOf()
        adapter= BasketAdapter(itemlist)
        binding.recyclerBasket.adapter=adapter
        getItems()
    }

    private fun showAlert() {
        val alert=AlertDialog.Builder(this)
        alert.setTitle("Complete Order")
        alert.setMessage("Do you want to complete your order?")
        alert.setPositiveButton("Yes"){_,_->
            completeOrder("Order completed")
        }
        alert.setNegativeButton("No"){_,_->
            completeOrder("Basket has been emptied")
        }
        alert.create().show()
    }

    private fun completeOrder(msg:String) {
        val fUser=Firebase.auth.currentUser!!
        Firebase.database.getReference("Basket").child(fUser.uid).removeValue().addOnCompleteListener { task->
            if (task.isSuccessful){
                Snackbar.make(binding.recyclerBasket,msg,Snackbar.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@MainActivity2,task.exception!!.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getItems() {
        val fUser=Firebase.auth.currentUser!!
        Firebase.database.getReference("Basket").child(fUser.uid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemlist.clear()
                for (child in snapshot.children){
                    val item=child.getValue(Basket::class.java)!!
                    itemlist.add(item)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}