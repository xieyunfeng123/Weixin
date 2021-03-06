package com.ityun.weixin.myapplication.ui.album.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ityun.weixin.myapplication.R;
import com.ityun.weixin.myapplication.util.DensityUtil;
import com.ityun.weixin.myapplication.util.addpic.LocalMedia;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2018/1/23 0023.
 */

public class AlbumAdapter extends BaseAdapter {

    Context context;

    List<LocalMedia> mlist;

    public AlbumAdapter(Context context) {
        this.context=context;
    }

    public void setData(List<LocalMedia> mlist) {
        this.mlist=mlist;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        int size=null!=mlist?mlist.size():0;
        return  size;
    }

    @Override
    public Object getItem(int position) {
        return mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder=null;
        if(convertView==null)
        {
            convertView= LayoutInflater.from(context).inflate(R.layout.item_add_img,null);
            holder=new Holder();
            holder.imageView=convertView.findViewById(R.id.item_img);
            convertView.setTag(holder);
        }
        else
        {
            holder= (Holder) convertView.getTag();
        }
        if(mlist.get(position).getType()==1)
        {
            int  padding= DensityUtil.dip2px(context,25);
            holder.imageView.setPadding(padding,padding,padding,padding);
            Glide.with(context).load(R.mipmap.camare).centerCrop().thumbnail(0.2f).into(holder.imageView);
        }
        else
        {
            holder.imageView.setPadding(0,0,0,0);
            Glide.with(context).load(new File(mlist.get(position).getPath())).thumbnail(0.2f).into(holder.imageView);
        }

        return convertView;
    }

    class Holder
    {
        ImageView imageView;
    }
}
