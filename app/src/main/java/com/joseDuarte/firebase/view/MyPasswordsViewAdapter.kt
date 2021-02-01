package com.joseDuarte.firebase.view

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.joseDuarte.firebase.R
import com.joseDuarte.firebase.objects.PasswordObject

abstract class MyPasswordsViewAdapter(
    private var values: Collection<PasswordObject>
) : RecyclerView.Adapter<MyPasswordsViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_fragment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values.toList()[position]
        holder.idView.text = item.id
        holder.contentView.text = item.password
        holder.start()
    }

    override fun getItemCount(): Int = values.size

    fun update(values: Collection<PasswordObject>) {
        this.values = values
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.id)
        val contentView: TextView = view.findViewById(R.id.content)
        private var showing: Boolean = false

        fun start() {
            view.setOnClickListener {
                if(showing){ contentView.transformationMethod = HideReturnsTransformationMethod.getInstance() }
                else{ contentView.transformationMethod = PasswordTransformationMethod.getInstance() }
                showing = !showing
            }

            view.setOnLongClickListener {
                onLongClick(it, idView)
                true
            }

            showing = false
            contentView.transformationMethod = HideReturnsTransformationMethod.getInstance()
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    abstract fun onLongClick(it: View, idView: TextView)

}