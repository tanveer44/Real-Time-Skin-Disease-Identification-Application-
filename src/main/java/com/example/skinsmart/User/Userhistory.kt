package com.example.skinsmart.User

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
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
import com.example.skinsmart.R
import com.example.skinsmart.User.Viewdoctors.hospitaluserAdapter
import com.example.skinsmart.databinding.ActivityUserhistoryBinding
import com.example.skinsmart.databinding.CardaddslotBinding
import com.example.skinsmart.databinding.CardhistoryuseBinding
import com.example.skinsmart.databinding.CarduserhospitalBinding
import com.example.skinsmart.model.Userresponse
import com.example.skinsmart.model.requestda
import com.example.skinsmart.model.requestresponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ymts0579.model.model.DefaultResponse
import com.ymts0579.model.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class Userhistory : AppCompatActivity() {
    private val b by lazy {
        ActivityUserhistoryBinding.inflate(layoutInflater)
    }
    var uemail=""
    private lateinit var progressDialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        getSharedPreferences("user", AppCompatActivity.MODE_PRIVATE).apply {
            uemail=getString("email","").toString()}


        readhistory(uemail)

    }

    inner class hospitaluserAdapter(var context: Context, var listdata: ArrayList<requestda>):
        RecyclerView.Adapter<hospitaluserAdapter.DataViewHolder>(){

        inner class DataViewHolder(val view: CardhistoryuseBinding) : RecyclerView.ViewHolder(view.root)



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
            return DataViewHolder(
               CardhistoryuseBinding.inflate(
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
                    tvdoctorinfo.text="Name $dname\n"+"Number $dnum"
                    tvdoctorinfo.visibility= View.GONE

                    if(statusPoint=="Pending"){
                        tvcost.visibility= View.GONE
                        tvustatus.visibility= View.GONE
                        tvcost.visibility= View.GONE
                        tvddescription.visibility= View.GONE
                    }else if(statusPoint=="Rejected"){
                        tvcost.visibility= View.GONE
                        tvustatus.visibility= View.GONE
                        tvcost.visibility= View.GONE
                        tvddescription.visibility= View.GONE
                    } else if(statusPoint=="Completed"){
                        if(feedback==""){
                            bntfeeback.visibility=View.VISIBLE
                        }else{
                            bntfeeback.visibility=View.GONE
                        }
                    }else{
                        bntfeeback.visibility=View.GONE
                    }

                    tvdoctordeatils.setOnClickListener {
                        tvdoctorinfo.visibility= View.VISIBLE
                        Handler().postDelayed({
                            tvdoctorinfo.visibility= View.GONE
                        },3500)

                    }

                    btnLocation.setOnClickListener {
                        startActivity(Intent(context,MapsActivity::class.java).apply {
                            putExtra("loc",daddress)
                        })

                    }


                    bntfeeback.setOnClickListener {

                        val dd= BottomSheetDialog(context)
                        dd.setContentView(R.layout.cardaddfeedback)
                        val etfeedback=dd.findViewById<EditText>(R.id.etfeedback)!!
                        val btnsubmit=dd.findViewById<Button>(R.id.btnsubmit)!!
                        val rating=dd.findViewById<RatingBar>(R.id.rating)!!
                        btnsubmit.setOnClickListener {
                            val rate=rating.rating.toString()
                            val feed=etfeedback.text.toString().trim()
                            if(rate=="0.0"){
                                Toast.makeText(context, "give your rating", Toast.LENGTH_SHORT).show()
                            }else if(feed.isEmpty()){
                                Toast.makeText(context, "Enter your Feedback", Toast.LENGTH_SHORT).show()
                            }else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    RetrofitClient.instance.Feedbackadd(rate,feed,requesId!!.toInt(),"Feedbackadd")
                                        .enqueue(object: Callback<DefaultResponse> {
                                            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                                                Toast.makeText(context, ""+t.message, Toast.LENGTH_SHORT).show()
                                            }
                                            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                                                Toast.makeText(context, "${response.body()!!.message}", Toast.LENGTH_SHORT).show()
                                                dd.dismiss()
                                            }
                                        })
                                }
                            }
                        }

                        dd.show()

                    }


                    holder.itemView.setOnClickListener {
                        if(statusPoint=="Accepted"){
                            val alertdialog= AlertDialog.Builder(context)
                            alertdialog.setTitle("Accept or Reject ")
                            alertdialog.setIcon(R.drawable.logo)

                            alertdialog.setMessage("Do you Want to Accept or Reject the Slot?")
                            alertdialog.setPositiveButton("Yes"){ alertdialog, which->
                                readtoaccept(requesId,"Accepted",uemail.toString())
                                alertdialog.dismiss()
                            }
                            alertdialog.setNegativeButton("No"){alertdialog,which->
                                readtoaccept(requesId,"Rejected",uemail.toString())
                                alertdialog.dismiss()
                            }
                            alertdialog.show()
                        }else{
                            Toast.makeText(context, "Your Request is $statusPoint", Toast.LENGTH_SHORT).show()
                        }



                    }

                }

            }
        }
        private fun readtoaccept(requesId: String?, status: String, email: String) {

                CoroutineScope(Dispatchers.IO).launch {
                    RetrofitClient.instance.updatestatuser(status,requesId!!.toInt(),"updatestatuser")
                        .enqueue(object: Callback<DefaultResponse> {
                            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                                Toast.makeText(context, ""+t.message, Toast.LENGTH_SHORT).show()
                            }
                            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                                readhistory(email)

                            }
                        })
                }


        }

        override fun getItemCount() = listdata.size
    }

    private fun readhistory(uemail: String) {
        val builder = AlertDialog.Builder(this,R.style.TransparentDialog)
        val inflater = this.layoutInflater
        builder.setView(inflater.inflate(R.layout.progressdialog, null))
        builder.setCancelable(false)

        progressDialog = builder.create()
        progressDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitClient.instance.getuserhistory(uemail,"getData")
                .enqueue(object : Callback<requestresponse> {
                    override fun onResponse(call: Call<requestresponse>, response: Response<requestresponse>) {

                        b.listhistory.adapter=hospitaluserAdapter(this@Userhistory,response.body()!!.user)
                        b.listhistory.layoutManager=LinearLayoutManager(this@Userhistory)
                        progressDialog.dismiss()
                    }

                    override fun onFailure(call: Call<requestresponse>, t: Throwable) {
                        Toast.makeText(this@Userhistory, "${t.message}", Toast.LENGTH_SHORT).show()

                    }

                })
        }




    }
}