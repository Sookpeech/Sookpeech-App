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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.example.myapplication2.api.RetrofitAPI;
import com.example.myapplication2.api.dto.Post;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import okhttp3.ResponseBody;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context_main = this;

        // For Record Activity
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "외장 메모리가 마운트 되지 않았습니다.", Toast.LENGTH_LONG).show();
        } else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        checkDangerousPermissions();


        tabWidget = (TabWidget)findViewById(android.R.id.tabs);
        login_btn = (Button)findViewById(R.id.login_button);

        myTabHost = getTabHost();

//        Intent intent1 = new Intent(MainActivity.this, RecordActivity.class);
//        Intent intent2 = new Intent(MainActivity.this, CommunityActivity.class);
        Intent intent2 = new Intent(MainActivity.this, ListActivity.class);
        Intent intent3 = new Intent(MainActivity.this, HomeActivity.class);
        Intent intent4 = new Intent(MainActivity.this, FriendActivity.class);
        Intent intent5 = new Intent(MainActivity.this, SettingsActivity.class);

        //Tab 추가
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

        myTabHost.setCurrentTab(1);  //메인 Tab 지정

        //set Retrofit for Login POST
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") //배포 전 local 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitAPI = retrofit.create(RetrofitAPI.class);

        //로그인 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        //로그인 한 적 있을 경우 silentSignIn 실행
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

        // 로그인 버튼 클릭 시
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

            // TODO: 성공적으로 로그인 한 경우, UI 업데이트
            if (account!=null){
                Log.d("LOGIN", "success");
                idToken = account.getIdToken();
                personName = account.getDisplayName();
                Log.d("idToken=", idToken);
                Log.d("personName=", personName);
                login_btn.setVisibility(View.GONE);

                //send ID Token to server and validate
                retrofitAPI.postData(new Post(idToken, -1L)).enqueue(new Callback<Post>() {
                    @Override
                    public void onResponse(Call<Post> call, Response<Post> response) {
                        Log.d("POST", "not successful yet");
                        if (response.isSuccessful()){
                            Log.d("POST", "POST Success!");
                            Log.d("POST", ">>>user_id="+response.body().getUser_id().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<Post> call, Throwable t) {
                        Log.d("POST", "POST Failed");
                        Log.d("POST", t.getMessage());
                    }
                });

            }
        } catch (ApiException e){
            String TAG = "MainActivity";
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //TODO: 로그인 되어 있지 않은 경우 UI
            login_btn.setVisibility(View.VISIBLE);
        }
    }

//    //-- 권한 요청 --//
//    private void checkDangerousPermissions() {
//        String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO };
//
//        for (int i = 0; i < permissions.length; i++) {
//            int permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
//            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {  // 권한이 허용되지 않은 경우
//                // 권한을 재요청 하는 경우 ('다시 묻지 않기' 체크박스가 자동 추가됨)
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
//                    ActivityCompat.requestPermissions(this, permissions, 1);
//                }
//                // 권한을 처음 요청하는 경우
//                else {
//                    ActivityCompat.requestPermissions(this, permissions, 1);
//                }
//            } else {
//                break;
//            }
//        }
//    }
//
//    // 사용자의 권한 승인/거절에 대한 대응 (요청이 거절되면 grantResults는 null값)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == 1) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(this, "권한 요청을 승인해야 사용 가능합니다.", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "권한 있음", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

}