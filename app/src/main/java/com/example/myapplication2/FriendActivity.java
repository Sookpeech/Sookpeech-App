package com.example.myapplication2;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication2.api.RetrofitAPI;
import com.example.myapplication2.api.RetrofitClient;
import com.example.myapplication2.api.dto.FeedbacksData;
import com.example.myapplication2.api.dto.FriendsData;
import com.example.myapplication2.api.dto.UserInfoData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendActivity extends AppCompatActivity {
    final private String TAG = getClass().getSimpleName();

    static Long userId = MainActivity.userId;
    static RetrofitAPI retrofitAPI;

    LinearLayout friendlist_layout;
    FloatingActionButton fab_add_friends;

    FriendsData[] friends;
    ArrayList<String> friendStringList = new ArrayList<>();

    ArrayList<UserInfoData> userInfoDataList;
    TreeMap<Long, UserInfoData> userInfoDataMap = new TreeMap<Long, UserInfoData>();
//    UserInfoData[] userInfoDataList;
    ArrayList<View> customViewList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        setTitle("내 친구 목록");

        customViewList = new ArrayList<>();

        friendlist_layout = (LinearLayout) findViewById(R.id.friendlist_layout);

//        getUserInfo();

        // 친구 추가 버튼
        fab_add_friends = findViewById(R.id.fab_add_friends);
        fab_add_friends.show();
        fab_add_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendActivity.this, AddFriendActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                //startActivityForResult(intent, 0);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        customViewList = new ArrayList<>();

        // 임시, 확인용
        System.out.println("onResume() 실행");

//        getUserInfo();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode==0) {
//            if (resultCode==333) {  //친구 추가 성공
//                Log.d(TAG, "onActivityResult : list reload");
//                userInfoDataList = new ArrayList<>();
//
//                GetFriends getFriends = new GetFriends();
//                getFriends.execute();
//            }
//        }
//    }
//
//    private void getUserInfo(){
//        System.out.println("FriendActivity : getUserInfo Called");  //임시, 확인용
//
//        RetrofitClient retrofitClient = RetrofitClient.getInstance();
//
//        userInfoDataList = new ArrayList<>();
//
//        friends = null;
//
//        if (retrofitClient!=null){
//            retrofitAPI = RetrofitClient.getRetrofitAPI();
//            retrofitAPI.getUserInfo(userId).enqueue(new Callback<UserInfoData>() {
//                @Override
//                public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {
//                    UserInfoData userInfoData = response.body();
//                    if (userInfoData!=null){
//                        Log.d("GET_USERINFO", "GET SUCCESS");
//                        Log.d("GET_USERINFO", ">>>response.body()=" + response.body());
//
//                        System.out.println("friends (null) : " + friends);  //임시, 확인용
//                        friends = userInfoData.getFriends();
//                        System.out.println("friends : " + friends);  //임시, 확인용
//
//                        Long friend_id;
//                        // ...
//                        if (friends != null) {
//                            if (friends.length != 0) {
//                                //중복 제거
//                                TreeMap<Long, FriendsData> tmpFriendsMap = new TreeMap<Long, FriendsData>();
//                                for (FriendsData friend : friends)
//                                    tmpFriendsMap.put(friend.getId(), friend);
//
//
//                                int index = 0;
//                                for (Long nKey : tmpFriendsMap.keySet()) {
//                                    friend_id = tmpFriendsMap.get(nKey).getFriend_id();
//
//                                    getFriendUserInfo(friend_id);
//
//                                    index++;
//                                }
//                            }
//                            else {
//                                friendlist_layout.removeAllViews();
//                                LayoutInflater layoutInflater = LayoutInflater.from(FriendActivity.this);
//                                View customView = layoutInflater.inflate(R.layout.custom_textview, null);
//
//                                ((TextView)customView.findViewById(R.id.custom_textView)).setText("+ 버튼을 눌러 친구를 추가하세요.");
//                                friendlist_layout.addView(customView);
//                            }
//                        } else {
//                            System.out.println("friends is null...");
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<UserInfoData> call, Throwable t) {
//                    Log.d("GET_USERINFO", "GET FAILED");
//                }
//            });
//        }
//    }
//
//    private void getFriendUserInfo(Long user_id){
//        System.out.println("FriendActivity : getFriendUserInfo Called");  //임시, 확인용
//
//        RetrofitClient retrofitClient = RetrofitClient.getInstance();
//
//        if (retrofitClient!=null){
//            retrofitAPI = RetrofitClient.getRetrofitAPI();
//            retrofitAPI.getUserInfo(user_id).enqueue(new Callback<UserInfoData>() {
//                @Override
//                public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {
//                    UserInfoData userInfoData = response.body();
//                    if (userInfoData != null) {
//                        Log.d("GET_FRIEND_USERINFO", "GET SUCCESS");
//                        Log.d("GET_FRIEND_USERINFO", ">>>response.body()=" + response.body());
//
//                        userInfoDataList.add(userInfoData);
//
//                        //임시, 확인용
//                        for (int i = 0; i < userInfoDataList.size(); i++) {
//                            System.out.println("getFriendUserInfo : userInfoDataList.get(i).getId()="+userInfoDataList.get(i).getId());  //임시, 확인용
//                        }
//
////                        // 중복 추가 X
////                        boolean isDuplicated = false;
////                        Long id = userInfoData.getId();
////                        if (userInfoDataList != null) {
////                            for (int i = 0; i < userInfoDataList.size(); i++) {
////                                if ((userInfoDataList.get(i).getId()).equals(id)) {  //기존 id와 일치하면
////                                    isDuplicated = true;
////                                    break;
////                                }
////                            }
////                            if (!isDuplicated)
////                                userInfoDataList.add(userInfoData);
////                        }
//
//                        customViewList.add(createCustomView(userInfoData.getId()));
//                        setFriendsListView();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<UserInfoData> call, Throwable t) {
//                    Log.d("GET_FRIEND_USERINFO", "GET FAILED");
//                }
//            });
//        }
//    }
//
//    private View createCustomView(Long freind_id) {
//        LayoutInflater layoutInflater = LayoutInflater.from(FriendActivity.this);
//
//        View customView = layoutInflater.inflate(R.layout.custom_friend_info, null);
//        UserInfoData userInfoData = userInfoDataMap.get(freind_id);
//
//        Long id = userInfoData.getId();
//        String name = userInfoData.getName();
//        String email = userInfoData.getEmail();
//        String picture = userInfoData.getPicture();
//
//        ((LinearLayout) customView.findViewById(R.id.container)).setTag(id + ":" + email + ":" + name);
//        ((TextView) customView.findViewById(R.id.tv_name)).setText(name);
//        ((TextView) customView.findViewById(R.id.tv_id)).setText("id: " + id.intValue());
//        ((TextView) customView.findViewById(R.id.tv_email)).setText(email);
//        if (picture != null)
//            Glide.with(this).load(picture).into((ImageView) customView.findViewById(R.id.profile_image));
//
//        return customView;
//    }
//
//    private void setFriendsListView() {
//        friendlist_layout.removeAllViews();
//
//        for (View view : customViewList)
//            friendlist_layout.addView(view);
//    }

    // 친구 목록에서 특정 친구를 클릭했을 때
    public void onClickFriend(View view) {
        String tag = (String) view.getTag();
        String[] tag_split = tag.split(":");
        Long id = Long.valueOf(tag_split[0]);
        String name = tag_split[2];

        //Toast.makeText(this, "id: " + id + ", name: " + name, Toast.LENGTH_SHORT).show();  //임시, 확인용

        Intent intent = new Intent(FriendActivity.this, PublicPracticeList.class);
        intent.putExtra("friend_id", id);
        intent.putExtra("friend_name", name);
        startActivity(intent);
    }
}