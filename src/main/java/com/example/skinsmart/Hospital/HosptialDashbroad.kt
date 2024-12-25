package com.example.skinsmart.Hospital


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.telephony.gsm.SmsManager
import android.telephony.gsm.SmsManager.getDefault
import android.text.InputFilter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grihagully.model.RetrofitClient
import com.example.skinsmart.Login
import com.example.skinsmart.MainActivity
import com.example.skinsmart.R
import com.example.skinsmart.User.Userhistory.hospitaluserAdapter
import com.example.skinsmart.databinding.ActivityHosptialDashbroadBinding
import com.example.skinsmart.databinding.CarddoccostBinding
import com.example.skinsmart.databinding.CardhistoryuseBinding
import com.example.skinsmart.databinding.CardhoshostoryBinding
import com.example.skinsmart.databinding.CardprofileBinding
import com.example.skinsmart.logout
import com.example.skinsmart.model.requestda
import com.example.skinsmart.model.requestresponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ymts0579.model.model.DefaultResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HosptialDashbroad : AppCompatActivity() {
    private val b by lazy {
        ActivityHosptialDashbroadBinding.inflate(layoutInflater)
    }
    private val bind by lazy {
        CardprofileBinding.inflate(layoutInflater)
    }
    var id=0
    var name=""
    var num=""
    var email=""
    var address=""
    var city=""
    var pass=""
    var type=""
    var status=""
    var descrip=""
    var timing=""
    private lateinit var progressDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE).apply {
            id=getInt("id",0)
            name=getString("name","").toString()
            num=getString("num","").toString()
            email=getString("email","").toString()
            address=getString("address","").toString()
            city=getString("city","").toString()
            pass=getString("pass","").toString()
            type=getString("type","").toString()
            status=getString("status","").toString()
            descrip=getString("descrip","").toString()
            timing=getString("timing","").toString()

        }
        readvieworders(email)

        bind.etname.setText(name)
        bind.etnum.setText(num)
        bind.etemail.setText(email)
        bind.etaddress.setText(address)
        bind.etcity.setText(city)
        bind.etpassword.setText(pass)
        bind.ettiming.setText(timing)
        bind.etdescri.setText(descrip)

        bind.etcity.setFilters(arrayOf<InputFilter>(InputFilter { source, start, end, dest, dstart, dend ->
            if (source.length > 0 && dstart == 0) {
                val v = CharArray(source.length)
                TextUtils.getChars(source, 0, source.length, v, 0)
                v[0] = v[0].uppercaseChar()
                return@InputFilter String(v)
            }
            null
        }
        ))

        val k=arrayOf("choose your choice","Available","Not Available")

        ArrayAdapter(this@HosptialDashbroad,
            android.R.layout.simple_list_item_checked, k).apply {
            bind.spinstatus.adapter=this
        }
        k.forEachIndexed { index, s ->
            if(s==status){
                bind.spinstatus.setSelection(index,true)
            }
        }

        b.tvname.text="WElcome $name"
        b.imageView4.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(this,b.imageView4)
            popupMenu.menuInflater.inflate(R.menu.menu,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.action_profile ->{
                        popupMenu.dismiss()
                        BottomSheetDialog(this).apply {
                            setContentView(bind.root)
                            /// profile
                            bind.btnupdateprofile.setOnClickListener {
                                val name1=bind.etname.text.toString().trim()
                                val num1=bind.etnum.text.toString().trim()
                                val email1=bind.etemail.text.toString().trim()
                                val address1=bind.etaddress.text.toString().trim()
                                val city1=bind.etcity.text.toString().trim()
                                val password1=bind.etpassword.text.toString().trim()
                                val timing1=bind.ettiming.text.toString().trim()
                                val descrip1=bind.etdescri.text.toString().trim()
                                val status1=bind.spinstatus.selectedItem.toString()

                                if(name1.isEmpty()){
                                    bind.etname.error="Enter your Name"
                                }else if(num1.isEmpty()){
                                    bind.etnum.error="Enter your Number"
                                }else if(email1.isEmpty()){
                                    bind.etemail.error="Enter your Email"
                                }else if(address1.isEmpty()){
                                    bind.etaddress.error="Enter your Address"
                                }else if(city1.isEmpty()){
                                    bind.etcity.error="Enter your city"
                                }else if(password1.isEmpty()){
                                    bind.etpassword.error="Enter your Password"
                                }else if(timing1.isEmpty()){
                                    bind.ettiming.error="Enter your timings"
                                }else if(descrip1.isEmpty()){
                                    bind.etnum.error="Enter your Description"
                                } else if(num1.count()!=10){
                                    bind.etnum.error="Enter your Number properly"
                                }else if(status1=="choose your choice"){
                                    Toast.makeText(this@HosptialDashbroad, "choose your choice", Toast.LENGTH_SHORT).show()
                                }else{
                                    CoroutineScope(Dispatchers.IO).launch {
                                        RetrofitClient.instance.updateprofile(id,name1,num1,email1,address1,city1,password1,type,status1,descrip1,timing1,"update")
                                            .enqueue(object: Callback<DefaultResponse> {
                                                override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                                                    Toast.makeText(this@HosptialDashbroad, ""+t.message, Toast.LENGTH_SHORT).show()
                                                }
                                                override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                                                    Toast.makeText(this@HosptialDashbroad, "${response.body()!!.message}", Toast.LENGTH_SHORT).show()
                                                    getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE).edit().apply {
                                                        putInt("id",id)
                                                        putString("name",name1)
                                                        putString("num",num1)
                                                        putString("email",email1)
                                                        putString("address",address1)
                                                        putString("city",city1)
                                                        putString("pass",password1)
                                                        putString("type",type)
                                                        putString("status",status1)
                                                        putString("descrip",descrip1)
                                                        putString("timing",timing1)
                                                        apply()
                                                    }
                                                    dismiss()
                                                }
                                            })
                                    }
                                }
                            }
                            /// profile
                            show()
                        }

                    }

                    R.id.action_Logout -> {logout()
                    popupMenu.dismiss()}

                }
                true
            })
            popupMenu.show()

        }

    }



// data view adaptr
   inner  class hospitalorderAdapter(var context: Context, var listdata: ArrayList<requestda>):
        RecyclerView.Adapter<hospitalorderAdapter.DataViewHolder>(){

        inner class DataViewHolder(val view: CardhoshostoryBinding) : RecyclerView.ViewHolder(view.root)



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
            return DataViewHolder(
                CardhoshostoryBinding.inflate(
                    LayoutInflater.from(context),parent,
                    false))
        }

        override fun onBindViewHolder(holder: DataViewHolder, @SuppressLint("RecyclerView") position:Int) {
            with(holder.view){

                listdata[position].apply {
                    tvdate.text=date
                    tvslot.text=slot
                    tvusedescri.text=udescrip
                    tvustatus.text=usstatus
                    tvdstatus.text=statusPoint
                    tvcost.text=cost
                    tvddescription.text=ddescrip
                    tvdoctorinfo.text="Name $name\n"+"Number $num"
                    tvdoctorinfo.visibility= View.GONE
                    linearfeed.visibility=View.GONE
                    tvfeedback.text=feedback

                    var floaft=0.0f
                    rating!!.forEach {
                        if(it!=' '&&it.isDigit()||it=='.'){
                            floaft=it.toFloat()
                        }
                    }
                    ratingbar.isIndeterminate=true
                    ratingbar.rating=floaft


                    if(statusPoint=="Pending"){
                        tvcost.visibility= View.GONE
                        tvustatus.visibility= View.GONE
                        tvcost.visibility= View.GONE
                        tvddescription.visibility= View.GONE
                        btncompleted.visibility=View.GONE
                    }else if(statusPoint=="Rejected"){
                        tvcost.visibility= View.GONE
                        tvustatus.visibility= View.GONE
                        tvcost.visibility= View.GONE
                        tvddescription.visibility= View.GONE
                        btncompleted.visibility=View.GONE
                    }


                    if(feedback==""){
                        linearfeed.visibility=View.GONE
                    }else{
                        linearfeed.visibility=View.VISIBLE
                    }

                    if(statusPoint=="Accepted") {
                        if(usstatus=="Accepted"){
                            btncompleted.visibility=View.VISIBLE
                        }else {
                            btncompleted.visibility=View.GONE
                        }
                    }else{
                        btncompleted.visibility=View.GONE
                    }


                    tvdoctordeatils.setOnClickListener {
                        tvdoctorinfo.visibility= View.VISIBLE
                        Handler().postDelayed({
                            tvdoctorinfo.visibility= View.GONE
                        },3500)

                    }


                    holder.itemView.setOnClickListener {
                        if(status=="Pending"){
                            val alertdialog= AlertDialog.Builder(context)
                            alertdialog.setTitle("Accept or Reject ")
                            alertdialog.setIcon(R.drawable.logo)
                            alertdialog.setCancelable(false)
                            alertdialog.setMessage("Do you Want to Accept or Reject the Slot?")
                            alertdialog.setPositiveButton("Yes"){ alertdialog, which->
                                readtoaccept(requesId,"Accepted",demail.toString(),num.toString())
                                alertdialog.dismiss()
                            }
                            alertdialog.setNegativeButton("No"){alertdialog,which->
                                readtoaccept(requesId,"Rejected",demail.toString(),num.toString())
                                alertdialog.dismiss()
                            }
                            alertdialog.show()
                        }else{
                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
                        }
                        

                    }

                    btncompleted.setOnClickListener {
                        readtoaccept(requesId,"Completed",demail.toString(),num.toString())
                    }



                }

            }


        }



    private val bind by lazy {
        CarddoccostBinding.inflate(layoutInflater)
    }
    private fun readtoaccept(requesId: String?, status: String, demail:String,unum:String) {
        if(status=="Accepted"){
            BottomSheetDialog(context).apply {
                setContentView(bind.root)

                bind.btnadcost.setOnClickListener {
                    val cost=bind.etcost.text.toString().trim()
                    val descri=bind.etdescription.text.toString().trim()

                    if(cost.isEmpty()){
                        bind.etcost.error="Enter your Cost"
                    }else if(descri.isEmpty()){
                        bind.etdescription.error="Enter Your Description"
                    }else{
                        CoroutineScope(Dispatchers.IO).launch {
                            RetrofitClient.instance.updatestatusdoc(status,cost,descri,requesId!!.toInt(),"updatestatusdoc")
                                .enqueue(object: Callback<DefaultResponse> {
                                    override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                                        Toast.makeText(context, ""+t.message, Toast.LENGTH_SHORT).show()
                                    }
                                    override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                                        readvieworders(demail)
                                        dismiss()
                                        if (TextUtils.isDigitsOnly(unum)) {
                                            val smsManager: SmsManager = getDefault()
                                            smsManager.sendTextMessage(unum, null, "your Request is $status", null, null)

                                        }
                                    }
                                })
                        }
                    }

                }

                show()
            }
        }else{
            CoroutineScope(Dispatchers.IO).launch {
                RetrofitClient.instance.updatestatusdocreject(status,status,id,"updatestatusdocreject")
                    .enqueue(object: Callback<DefaultResponse> {
                        override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                            Toast.makeText(context, ""+t.message, Toast.LENGTH_SHORT).show()
                        }
                        override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                            readvieworders(demail)
                            if (TextUtils.isDigitsOnly(unum)) {
                                val smsManager: SmsManager = getDefault()
                                smsManager.sendTextMessage(unum, null, "your Request is $status", null, null)

                            }
                        }
                    })
            }
        }

    }

    override fun getItemCount() = listdata.size
    }




// get the part
    private fun readvieworders(email: String) {
        val builder = AlertDialog.Builder(this,R.style.TransparentDialog)
        val inflater = this.layoutInflater
        builder.setView(inflater.inflate(R.layout.progressdialog, null))
        builder.setCancelable(false)

        progressDialog = builder.create()
        progressDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitClient.instance.getdoctororders(email,"vieworders")
                .enqueue(object : Callback<requestresponse> {
                    override fun onResponse(call: Call<requestresponse>, response: Response<requestresponse>) {

                        b.listorder.adapter=hospitalorderAdapter(this@HosptialDashbroad,response.body()!!.user)
                        b.listorder.layoutManager=LinearLayoutManager(this@HosptialDashbroad)
                        Toast.makeText(this@HosptialDashbroad, "${response.body()!!.user},$email", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }

                    override fun onFailure(call: Call<requestresponse>, t: Throwable) {
                        Toast.makeText(this@HosptialDashbroad, "${t.message}", Toast.LENGTH_SHORT).show()

                    }

                })
        }

    }
}