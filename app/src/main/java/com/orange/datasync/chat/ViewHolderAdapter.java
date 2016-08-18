package com.orange.datasync.chat;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ViewHolderAdapter extends ArrayAdapter<Message> {

    private ChatActivity mContext;

    private LayoutInflater mInflater;

    public ViewHolderAdapter(ChatActivity context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder {
        TextView name;
        TextView longtext;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_message, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.longtext = (TextView) convertView
                    .findViewById(R.id.longtext);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Message data = getItem(position);
        String name = data.name;
        if(name != null && name.length() > 0) {
            name = ChatActivity.capitalize(name.replaceAll("\\..*", ""));
        }
        holder.name.setText(name != null && name.length() > 0 ? name : "Anonymous");
        holder.longtext.setText(data.text);

        if(data.name != null && data.name.equals(mContext.getUserName())) {
            holder.name.setGravity(Gravity.END);
            holder.longtext.setGravity(Gravity.END);
        } else {
            holder.name.setGravity(Gravity.START);
            holder.longtext.setGravity(Gravity.START);
        }

        return convertView;

    }
}


