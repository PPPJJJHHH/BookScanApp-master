package me.myds.g2u.bookscanapp;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

abstract public class BaseRecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    abstract public void bindData (T data);

    public T getItemData (BaseRecyclerAdapter adapter) {
        return (T) adapter.list.get(getLayoutPosition());
    }
}