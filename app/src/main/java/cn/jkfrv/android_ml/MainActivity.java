package cn.jkfrv.android_ml;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.contentcapture.DataRemovalRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // 申请权限的集合，同时要在AndroidManifest.xml中申请，Android 6以上需要动态申请权限
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
//            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
//            Manifest.permission.WRITE_SETTINGS

    };
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();
    //文件路径
    private String path;
    private Button predict,btn_label;
    private TextView result,filepath;
    private String url="http://192.168.43.21:5001/upload";
    File txt,f;

    private static final MediaType MEDIA_TYPE_FILE = MediaType.parse("multipart/form-data");
    private static final String TAG = "MainActivity";

    OkHttpClient client = new OkHttpClient();//避免多次生成实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_label = findViewById(R.id.btn_label);
        btn_label.setOnClickListener(new View.OnClickListener() {  //到打标签的界面
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LabelActivity.class);
                startActivity(intent);
            }
        });

        filepath = findViewById(R.id.filepath); //要上传的文件路径
        //6.0获取多个权限
        mPermissionList.clear(); // 动态申请权限
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        //未授予的权限为空，表示都授予了
        if (mPermissionList.isEmpty()) {
            ///storage/emulated/0/acc.csv
            path = "/storage/emulated/0/upload/label0.db";
            filepath.setText(path);
            txt = new File(path);
            boolean fileExist = fileIsExists(path);
            if(fileExist){ //判断文件是否存在
                Toast.makeText(MainActivity.this,"文件存在"+f.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(MainActivity.this,"文件不存在"+path,Toast.LENGTH_LONG).show();
            }

        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }


        predict = findViewById(R.id.predict);
        result = findViewById(R.id.result);
        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //点击上传文件
                try {
                    uploadFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void uploadFile(String path) throws IOException {  //用uploadFile函数上传文件
        File file1 = new File(path);
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("label","label1.db",RequestBody.create(MediaType.parse("multipart/form-data"),file1))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30,TimeUnit.SECONDS)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: "+e.getLocalizedMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,"上传失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String res = response.body().string();
                Log.d(TAG, "onResponse: "+res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(res);
                    }
                });
            }
        });
    }

    public boolean fileIsExists(String strFile) {
        try {
            f = new File(strFile);
            if (!f.exists()) {
                return false;

            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}