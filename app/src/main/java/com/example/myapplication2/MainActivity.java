package com.example.myapplication2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.example.myapplication2.api.RetrofitAPI;
import com.example.myapplication2.api.RetrofitClient;
import com.example.myapplication2.api.dto.LoginRequestDto;
import com.example.myapplication2.api.dto.UserInfoData;
import com.example.myapplication2.api.objects.UserIdObject;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends TabActivity {
    static String EXTERNAL_STORAGE_PATH = "";

    public static Context context_main;
    public static TabHost myTabHost = null;
    TabHost.TabSpec spec;

    static TabWidget tabWidget;
    static Button login_btn;

    private final int RC_SIGN_IN = 123;
    GoogleSignInClient mGoogleSignInClient;

    static String personName;
    static String idToken;

    static RetrofitAPI retrofitAPI;
    static UserIdObject userIdObject;

    static Intent intent2, intent3, intent4, intent5;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context_main = this;

        // For Record Activity
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "?????? ???????????? ????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
        } else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        checkDangerousPermissions();

        //????????? ?????? ??????
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        //????????? ??? ??? ?????? ?????? silentSignIn ??????
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(
                this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        Log.d("LOGIN", "silentSignIn");
                        handleSignInResult(task);
                    }
                }
        );


        tabWidget = (TabWidget)findViewById(android.R.id.tabs);
        login_btn = (Button)findViewById(R.id.login_button);

        myTabHost = getTabHost();

//        Intent intent1 = new Intent(MainActivity.this, RecordActivity.class);
//        Intent intent2 = new Intent(MainActivity.this, CommunityActivity.class);
        intent2 = new Intent(MainActivity.this, ListActivity.class);
        intent3 = new Intent(MainActivity.this, HomeActivity.class);
        intent4 = new Intent(MainActivity.this, FriendActivity.class);
        intent5 = new Intent(MainActivity.this, SettingsActivity.class);

        //Tab ??????
//        spec = myTabHost.newTabSpec("Record").setIndicator("RECORD").setContent(intent1);
//        myTabHost.addTab(spec);

        spec = myTabHost.newTabSpec("Community").setIndicator("COMMUNITY").setContent(intent2);
        myTabHost.addTab(spec);

        spec = myTabHost.newTabSpec("Home").setIndicator("HOME").setContent(intent3);
        myTabHost.addTab(spec);

        spec = myTabHost.newTabSpec("Friend").setIndicator("FRIEND").setContent(intent4);
        myTabHost.addTab(spec);

        spec = myTabHost.newTabSpec("Settings").setIndicator("SETTINGS").setContent(intent5);
        myTabHost.addTab(spec);

        myTabHost.setCurrentTab(1);  //?????? Tab ??????

        // ????????? ?????? ?????? ???
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }  // end of onCreate

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.d("LOGIN", "OnClickSignIn");
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try{
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // TODO: ??????????????? ????????? ??? ??????, UI ????????????
            if (account!=null){
                Log.d("LOGIN", "success");
                idToken = account.getIdToken();
                personName = account.getDisplayName();
                Log.d("idToken=", idToken);
                Log.d("personName=", personName);
                login_btn.setVisibility(View.GONE);

                postUserLogin(idToken);

            }
        } catch (ApiException e){
            String TAG = "MainActivity";
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //TODO: ????????? ?????? ?????? ?????? ?????? UI
            login_btn.setVisibility(View.VISIBLE);

        }
    }

//    //-- ?????? ?????? --//
//    private void checkDangerousPermissions() {
//        String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO };
//
//        for (int i = 0; i < permissions.length; i++) {
//            int permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {  // ????????? ???????????? ?????? ??????
//                // ????????? ????????? ?????? ?????? ('?????? ?????? ??????' ??????????????? ?????? ?????????)
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
//                    ActivityCompat.requestPermissions(this, permissions, 1);
//                }
//                // ????????? ?????? ???????????? ??????
//                else {
//                    ActivityCompat.requestPermissions(this, permissions, 1);
//                }
//            } else {
//                break;
//            }
//        }
//    }
//
//    // ???????????? ?????? ??????/????????? ?????? ?????? (????????? ???????????? grantResults??? null???)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == 1) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, permissions[i] + " ????????? ?????????.", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(this, "?????? ????????? ???????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
//                }
//                return;
//            }
//        }
//    }


    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.VIBRATE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "?????? ??????", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    //send ID Token to server and validate
    private void postUserLogin(String idToken){
        RetrofitClient retrofitClient = RetrofitClient.getInstance();

        if (retrofitClient!=null){
            retrofitAPI = RetrofitClient.getRetrofitAPI();
            retrofitAPI.postLoginToken(new LoginRequestDto(idToken, -1L)).enqueue(new Callback<LoginRequestDto>() {
                @Override
                public void onResponse(Call<LoginRequestDto> call, Response<LoginRequestDto> response) {
                    Log.d("POST", "not success yet");
                    if (response.isSuccessful()){
                        Log.d("POST", "POST Success!");
                        Log.d("POST", ">>>user_id="+response.body().getUser_id().toString());
                        userIdObject = new UserIdObject(response.body().getUser_id());
                        getUserInfo(userIdObject);
                    }
                }

                @Override
                public void onFailure(Call<LoginRequestDto> call, Throwable t) {
                    Log.d("POST", "POST Failed");
                    Log.d("POST", t.getMessage());
                }
            });
        }
    }

    //get user info by user_id
    private void getUserInfo(UserIdObject userIdObject){
        RetrofitClient retrofitClient = RetrofitClient.getInstance();

        if (retrofitClient!=null){
            retrofitAPI = RetrofitClient.getRetrofitAPI();
            retrofitAPI.getUserInfo(userIdObject.getId()).enqueue(new Callback<UserInfoData>() {
                @Override
                public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {
                    UserInfoData userInfoData = response.body();
                    if (userInfoData!=null){
                        Log.d("GET_USERINFO", "GET SUCCESS");
                        Log.d("GET_USERINFO", response.body().getEmail());

                        putInfoToIntents(response.body());
                    }
                }

                @Override
                public void onFailure(Call<UserInfoData> call, Throwable t) {
                    Log.d("GET_USERINFO", "GET FAILED");
                }
            });
        }
    }

    private void putInfoToIntents(UserInfoData userInfoData){
        Log.d("PUT_INTENTS", userInfoData.getEmail());
        // TODO: intent3 - HomeActivity <- Practices
        intent3.putExtra("practices", userInfoData.getPractices());

        // TODO: intent4 - FriendActivity <- Friends

        // intent5 - SettingsActivity <- basic Userinfo
        intent5.putExtra("name", userInfoData.getName());
        intent5.putExtra("email", userInfoData.getEmail());
        intent5.putExtra("picture", userInfoData.getPicture());
        intent5.putExtra("point", String.valueOf(userInfoData.getPoint()));
    }

}