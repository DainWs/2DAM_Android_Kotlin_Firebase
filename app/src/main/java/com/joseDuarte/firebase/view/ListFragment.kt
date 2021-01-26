package com.joseDuarte.firebase.view

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joseDuarte.firebase.MainActivity
import com.joseDuarte.firebase.R
import com.joseDuarte.firebase.firebase.FirebaseDB
import com.joseDuarte.firebase.objects.PasswordObject
import kotlinx.android.synthetic.main.add_dialog.view.*

/**
 * A fragment representing a list of Items.
 */
class ListFragment: Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.list_item_fragment, container, false)

        var list : MutableCollection<PasswordObject> = MainActivity.USER_DATA_LIST

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                adapter = object : MyPasswordsViewAdapter(list) {
                    override fun onLongClick(it: View, idView: TextView) {
                        val popupMenu = PopupMenu(view.context, it)
                        activity?.menuInflater?.inflate(R.menu.popup_menu, popupMenu.menu)
                        popupMenu.show()
                        popupMenu.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.edit_menu_item -> editDialog(idView.text.toString())
                                R.id.delete_menu_item -> FirebaseDB.removePasswordObjectFromFirebase(
                                    idView.text.toString()
                                )
                                else -> println()
                            }
                            true
                        }
                    }
                }

            }
        }
        return view
    }

    fun editDialog(idView: String) {
        if(context!=null) {
            val builder = AlertDialog.Builder(context!!)
            val inflater = this.layoutInflater
            val view = inflater.inflate(R.layout.add_dialog, null)

            view.password_name_textfield.setText(idView)
            view.password_name_textfield.isFocusable = false
            view.password_name_textfield.setOnKeyListener(null)
            view.show_toggle_btn.setOnClickListener {
                var passTextField = view.password_password_textfield
                if (passTextField.transformationMethod is PasswordTransformationMethod) {
                    passTextField.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                } else {
                    passTextField.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                }

            }

            builder.setView(view)
                .setPositiveButton(R.string.guardar) { dialog, _ ->
                    val idText: String = idView
                    val passText: String = view.password_password_textfield.text.toString()
                    if (passText.isNotEmpty()) {
                        FirebaseDB.editPasswordObjectFromFirebase(PasswordObject(idText, passText))
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            context,
                            R.string.password_rellenar_campo_password,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton(R.string.cancelar) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}