package com.ityun.weixin.myapplication.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;
import com.ityun.weixin.myapplication.R;
import com.ityun.weixin.myapplication.base.App;
import com.ityun.weixin.myapplication.base.BaseFragment;
import com.ityun.weixin.myapplication.bean.Friend;
import com.ityun.weixin.myapplication.bean.User;
import com.ityun.weixin.myapplication.event.IMNewFriendEvent;
import com.ityun.weixin.myapplication.im.IMModel;
import com.ityun.weixin.myapplication.listener.AdapterItemOnClickListener;
import com.ityun.weixin.myapplication.ui.chat.ChatActivity;
import com.ityun.weixin.myapplication.ui.fragment.adapter.FriendAdapter;
import com.ityun.weixin.myapplication.ui.friend.SearContract;
import com.ityun.weixin.myapplication.ui.friend.SearchPrensenter;
import com.ityun.weixin.myapplication.util.SpUtil;
import com.ityun.weixin.myapplication.view.SideBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2018/2/12 0012.
 */

public class FriendFragment extends BaseFragment implements SearContract.SearchFriendView {

    @BindView(R.id.touch_sidebar)
    TextView touch_sidebar;

    @BindView(R.id.friend_sidebar)
    SideBar friend_sidebar;

    @BindView(R.id.friend_recycle)
    RecyclerView friend_recycle;

    @BindView(R.id.load_view)
    RelativeLayout load_view;

    FriendAdapter adapter;

    private SearContract.Presenter prensenter;

    private List<Friend> mlist = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);
        friend_sidebar.setTextView(touch_sidebar);
        prensenter = new SearchPrensenter(this);
        return view;
    }

    @Override
    public void requestData() {
        adapter = new FriendAdapter(getActivity());
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        friend_recycle.setLayoutManager(manager);
        friend_recycle.setAdapter(adapter);
        adapter.setData(mlist);
        adapter.notifyDataSetChanged();
        List<Friend> friendList = App.getInstance().getFriends();
        if (friendList != null && friendList.size() != 0) {
            User friendUser = friendList.get(0).getFriendUser();
            if (friendUser.getUsername().equals(SpUtil.getUser().getUsername())) {
                mlist.clear();
                updataFriends(friendList);
            } else {
                prensenter.searchFriend();
            }
        } else {
            prensenter.searchFriend();
        }

        adapter.setFriendItemOnClick(new AdapterItemOnClickListener() {
            @Override
            public void OnClick(int position) {
                Friend friend = mlist.get(position);
                User user = friend.getFriendUser();
                Bundle bundle = new Bundle();
                bundle.putSerializable("friend", user);
                startActivity(ChatActivity.class, bundle);
            }

            @Override
            public void OnClickObj(Object object) {

            }
        });
        mlist.clear();
    }

    @Override
    public void searchSucess(final List<Friend> mlistFriend) {
        mlist.clear();
        load_view.setVisibility(View.GONE);
        friend_recycle.setVisibility(View.VISIBLE);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updataFriends(mlistFriend);
                if (mlistFriend != null && mlistFriend.size() != 0) {
                    SpUtil.saveFriends(mlistFriend);
                }
            }
        });
    }


    private void updataFriends(List<Friend> mlistFriend) {
        List<String> sort = new ArrayList<>();
        for (int i = 0; i < mlistFriend.size(); i++) {
            Friend friend = mlistFriend.get(i);
            String username = friend.getFriendUser().getNickname();
            if (username != null) {
                String pinyin = Pinyin.toPinyin(username.charAt(0));
                String aString = pinyin.substring(0, 1).toUpperCase();
                if (aString.matches("[A-Z]")) {
                    if (!sort.contains(aString)) {
                        sort.add(aString);
                    }
                    friend.setPinyin(aString);
                } else {
                    if (!sort.contains("#")) {
                        sort.add("#");
                    }
                    friend.setPinyin("#");
                }
                mlist.add(friend);
            }
        }
        App.getInstance().setFriends(mlistFriend);
        adapter.setData(mlist);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void searchError() {
        load_view.setVisibility(View.GONE);
        friend_recycle.setVisibility(View.VISIBLE);
        adapter.setData(mlist);
        adapter.notifyDataSetChanged();
    }

    /**
     * 启动指定Activity
     *
     * @param target
     * @param bundle
     */
    public void startActivity(Class<? extends Activity> target, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(getActivity(), target);
        if (bundle != null)
            intent.putExtra(getActivity().getPackageName(), bundle);
        getActivity().startActivity(intent);
    }

    @Subscribe
    public void friendEvent(IMNewFriendEvent newFriendEvent) {
        if (newFriendEvent.getResult().equals("hasAdd")) {
            prensenter.searchFriend();
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
