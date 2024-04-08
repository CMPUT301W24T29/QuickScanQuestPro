package com.example.quickscanquestpro;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

// This class is used to set the height of the listview based on the number of items in the listview
// This is used in the EventDetailsFragment.java file
// Code created with the help of the following stackoverflow question:
// https://stackoverflow.com/questions/52217728/listview-shows-only-one-item-in-a-scrollview
// Author: Prabal.PX
//       : https://stackoverflow.com/users/5803714/prabal-px
// Answer by: Parteek Singh Bedi
//          : https://stackoverflow.com/users/1000000/parteek-singh-bedi
public class ListViewHelper {
    public static void getListViewSize(ListView listView, ListAdapter listAdapter){
        ListAdapter adapter = listAdapter;
        if(adapter!=null){
            int totalHeight = 0;

            //setting list adapter in loop tp get final size
            for (int i=0; i<adapter.getCount(); i++){
                View listItem = adapter.getView(i, null, listView);
                listItem.measure(0,0);
                totalHeight += listItem.getMeasuredHeight();
            }
            //setting listview items in adapter
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() *
                    (adapter.getCount()-1));
            listView.setLayoutParams(params);

        }else{
            return;
        }
    }
}