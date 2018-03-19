package com.ityun.weixin.myapplication.ui.friend;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ityun.weixin.myapplication.R;
import com.ityun.weixin.myapplication.base.BaseActivity;
import com.ityun.weixin.myapplication.bean.Friend;
import com.ityun.weixin.myapplication.bean.NewFriend;
import com.ityun.weixin.myapplication.bean.User;
import com.ityun.weixin.myapplication.db.Config;
import com.ityun.weixin.myapplication.db.NewFriendManager;
import com.ityun.weixin.myapplication.im.IMModel;
import com.ityun.weixin.myapplication.model.UserModel;
import com.ityun.weixin.myapplication.ui.friend.adapter.NewFriendAdapter;
import com.ityun.weixin.myapplication.view.LoadDialog;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Administrator on 2018/3/12 0012.
 */

public class NewFriendActivity extends BaseActivity implements SearContract.View {

    @BindView(R.id.new_friend_msg_list)
    public RecyclerView new_friend_msg_list;

    NewFriendAdapter adapter;

    private List<NewFriend> mlist;

    private Dialog dialog;

    private SearchPrensenter prensenter;

    private int nowPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfriend);
        ButterKnife.bind(this);
        dialog = new LoadDialog(this).setText("正在操作").build();
        prensenter = new SearchPrensenter(this);
        adapter = new NewFriendAdapter(this);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        new_friend_msg_list.setLayoutManager(manager);
        new_friend_msg_list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        new_friend_msg_list.setAdapter(adapter);
        mlist = new ArrayList<>();
        adapter.setData(mlist);
        mlist.addAll(NewFriendManager.getInstance(this).getAllNewFriend());
        adapter.notifyDataSetChanged();
        adapter.setOnItemAgreeClickListener(new NewFriendAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                prensenter.searchById(mlist.get(position).getUid());
                nowPosition=position;
                dialog.show();
            }
        });
    }

    @Override
    public void searchSucess(final User user) {
        IMModel.getInstance().sendAgreeFriendMessage(user, new IMModel.OnMessageListener() {
            @Override
            public void onSucess(final BmobIMMessage message) {
                UserModel.getInstance().addNewFriend(user, new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if(e==null||e.getErrorCode()==Config.STATUS_HAS_ADD)
                        {
                            NewFriend newFriend=mlist.get(nowPosition);
                            NewFriendManager.getInstance(NewFriendActivity.this).updateNewFriend(newFriend, Config.STATUS_VERIFIED);
                            mlist.clear();
                            mlist.addAll(NewFriendManager.getInstance(NewFriendActivity.this).getAllNewFriend());
                            adapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                });
            }
            @Override
            public void onFail(BmobException e) {
                Toast(R.string.error_add);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void searchFail() {
        Toast(R.string.error_add);
        dialog.dismiss();
    }

    @Override
    public void searchError() {
        Toast(R.string.error_add);
        dialog.dismiss();
    }
}
