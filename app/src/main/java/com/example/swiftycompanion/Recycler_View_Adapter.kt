package com.example.swiftycompanion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class Recycler_View_Adapter(val itemList: ArrayList<Model>) :
    RecyclerView.Adapter<Recycler_View_Adapter.ModelViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Recycler_View_Adapter.ModelViewHolder {

        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.skills, parent, false)
        return ModelViewHolder(v)
    }

    override fun onBindViewHolder(holder: Recycler_View_Adapter.ModelViewHolder, position: Int) {
        holder.ItemText.setText(itemList[position].name)
        holder.ItemText2.setText(itemList[position].level)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ConstraintLayt: ConstraintLayout
        var ItemText: TextView
        var ItemText2: TextView

        init {
            ConstraintLayt =
                itemView.findViewById(R.id.skills_layout_recycler_view) as ConstraintLayout
            ItemText = itemView.findViewById(R.id.text_view_recycler_view) as TextView
            ItemText2 = itemView.findViewById(R.id.text_view_recycler_view2) as TextView
        }
    }
}