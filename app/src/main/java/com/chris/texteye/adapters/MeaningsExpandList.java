package com.chris.texteye.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.chris.texteye.R;

import java.util.HashMap;
import java.util.List;

public class MeaningsExpandList extends BaseExpandableListAdapter {
    private Context context;
    private List<String> list_data_header;
    private HashMap<String, List<String>> list_hash_map;

    public MeaningsExpandList(Context context, List<String> list_data_header, HashMap<String, List<String>> list_hash_map) {
        this.context = context;
        this.list_data_header = list_data_header;
        this.list_hash_map = list_hash_map;
    }

    @Override
    public int getGroupCount() {
        return list_data_header.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return list_hash_map.get(list_data_header.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return list_data_header.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return list_hash_map.get(list_data_header.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String header_title = (String)getGroup(groupPosition);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.meanings_expand_list_group, null);
        }

        TextView list_header = convertView.findViewById(R.id.lblListHeader);
        list_header.setText(header_title);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String child_text = (String)getChild(groupPosition, childPosition);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.meanings_expand_list_item, null);
        }

        TextView text_list_child = convertView.findViewById(R.id.lblListItem);
        text_list_child.setText(child_text);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition){
        return true;
    }
}
