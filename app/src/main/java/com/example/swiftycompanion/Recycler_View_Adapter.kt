package com.example.swiftycompanion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(private val itemList: ArrayList<Model>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ModelViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.ModelViewHolder {

        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.skills, parent, false)
        return ModelViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ModelViewHolder, position: Int) {
        holder.itemText.text = itemList[position].name
        holder.itemText2.text = itemList[position].level
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var constraint: ConstraintLayout
        var itemText: TextView
        var itemText2: TextView

        init {
            constraint =
                itemView.findViewById(R.id.skills_layout_recycler_view) as ConstraintLayout
            itemText = itemView.findViewById(R.id.text_view_recycler_view) as TextView
            itemText2 = itemView.findViewById(R.id.text_view_recycler_view2) as TextView
        }
    }
}