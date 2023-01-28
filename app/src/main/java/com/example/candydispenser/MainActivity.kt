package com.example.candydispenser

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.candydispenser.Adapter.HomeAdapter
import com.example.candydispenser.Model.Basket
import com.example.candydispenser.Model.Item
import com.example.candydispenser.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var adapter:HomeAdapter
    lateinit var itemlist:ArrayList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar=supportActionBar!!
        val color=ColorDrawable(Color.parseColor("#3F51B5"))
        actionbar.setBackgroundDrawable(color)

        val coins= listOf(10,20,40)

        val completeadapter=ArrayAdapter(this,R.layout.drop_down_items,coins)
        (binding.coins.editText as? AutoCompleteTextView)?.setAdapter(completeadapter)

        binding.recyclerItems.setHasFixedSize(true)
        binding.recyclerItems.layoutManager=LinearLayoutManager(this)
        itemlist= arrayListOf()
        adapter= HomeAdapter(itemlist,this@MainActivity)
        binding.recyclerItems.adapter=adapter
        getItems(itemlist)

        binding.view.setOnClickListener {
            val alert=AlertDialog.Builder(this)
            alert.setTitle("Insufficient Balance")
            alert.setMessage("You have to add more money inorder to continue")
            alert.setPositiveButton("Ok"){_,_->
            }
            alert.show()
        }
        var total=0
        //getTotal(0)
        //binding.balance.text="Ksh.0"
        binding.balance.text="Ksh.${total.toString()}"
        binding.finalBalance.text="Ksh.0"
        binding.total.text="Ksh.0"
        binding.clear.setOnClickListener {
            total=0
            binding.balance.text="Ksh.${total.toString()}"
            getTotal(total)
        }
        binding.coinsInsert.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                total += p0.toString().toInt()
                if (total > 49) {
                    binding.view.visibility = View.GONE
                }else{
                    binding.view.visibility=View.VISIBLE
                }
                binding.balance.text="Ksh.${total.toString()}"
                binding.finalBalance.text="Ksh.${total.toString()}"
                getTotal(total)
            }
            override fun afterTextChanged(p0: Editable?) {

            }

        })
        addMenuProvider(object :MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.top_menu,menu)
                val search=menu.findItem(R.id.search)
                val searchview=search.actionView as androidx.appcompat.widget.SearchView
                searchview.queryHint="Search..."
                searchview.setOnQueryTextListener(object :androidx.appcompat.widget.SearchView.OnQueryTextListener{
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if(newText!!.isNotEmpty()){
                            searchItem(newText)
                        }else{
                            getItems(itemlist)
                        }
                        return true
                    }

                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if(menuItem.itemId == R.id.basket_icon){
                    startActivity(Intent(this@MainActivity,MainActivity2::class.java))
                }
                return true
            }

        })
    }

    private fun searchItem(newText: String?) {
        val query=Firebase.database.getReference("Items").orderByChild("name").startAt(newText?.uppercase())
            .endAt(newText?.lowercase()+"\uf8ff")
        query.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemlist.clear()
                for(child in snapshot.children){
                    val item=child.getValue(Item::class.java)!!
                    itemlist.add(item)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getTotal(total: Int?) {
        val fuser=Firebase.auth.currentUser!!
        Firebase.database.getReference("Basket").child(fuser.uid).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list= arrayListOf<Int>()
                for (child in snapshot.children){
                    val item=child.getValue(Basket::class.java)!!
                    val amount= item.total!!.toInt()
                    list.add(amount)
                }
                val sum=list.sum()
                val balance=total!!-sum
                binding.total.text="Ksh.${sum.toString()}"
                binding.finalBalance.text="Ksh.${balance.toString()}"
                if (total < 50 || balance <=0) {
                    binding.view.visibility = View.VISIBLE
                }else{
                    binding.view.visibility=View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getItems(itemlist: ArrayList<Item>) {
        Firebase.database.getReference("Items").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemlist.clear()
                for (child in snapshot.children){
                    val item=child.getValue(Item::class.java)!!
                    itemlist.add(item)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    override fun onStart() {
        super.onStart()
        val fUser=Firebase.auth.currentUser
        if (fUser == null){
            Firebase.auth.signInAnonymously()
        }
    }
}