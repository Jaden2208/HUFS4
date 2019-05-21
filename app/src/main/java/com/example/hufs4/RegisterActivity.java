package com.example.hufs4;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private String userName;
    private String userID;
    private String userPassword;
    private String userPassword2;
    private AlertDialog dialog;
    private boolean validate = false;

    String name="";
    String sha512PW = null;


    static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Whale/1.4.64.6 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        final EditText nameText = (EditText) findViewById(R.id.nameText);
        final EditText idText = (EditText) findViewById(R.id.idText);
        final EditText passwordText = (EditText) findViewById(R.id.passwordText);
        final EditText passwordText2 = (EditText) findViewById(R.id.passwordText2);

        final CheckBox confirmCheckBox = (CheckBox) findViewById(R.id.confirmCheckBox);

        userName = nameText.getText().toString();


        final Button validateButton = (Button) findViewById(R.id.validateButton);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = idText.getText().toString();
                if (validate) {
                    return;
                }
                if (userID.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디는 빈 칸일 수 없습니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            Log.d("check", String.valueOf(success));
                            if (success) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("사용 가능한 아이디입니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();
                                idText.setEnabled(false);
                                validate = true;
                                idText.setBackgroundColor(getResources().getColor(R.color.colorGray));
                                validateButton.setBackgroundColor(getResources().getColor(R.color.colorGray));
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("사용할 수 없는 아이디입니다.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                ValidateRequest validateRequest = new ValidateRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(validateRequest);
                Log.d("아이디쳌", String.valueOf(queue));
            }
        });

        Button registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                userID = idText.getText().toString();
                userPassword = passwordText.getText().toString();
                userPassword2 = passwordText2.getText().toString();

                new doit().execute();

                if(!validate){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("먼저 중복 체크를 해주세요.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if(!userPassword.equals(userPassword2)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호가 일치하지 않습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                if(userID.equals("") || userPassword.equals("") || userPassword2.equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("빈 칸 없이 입력해주세요.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }

                if(!confirmCheckBox.isChecked()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("주의사항 확인 후에 체크해주세요!")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                Log.d("파싱", "직전");

                if(!userName.equals(name)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("이름, 학번 또는 비밀번호가 e-class 정보와 일치하지 않습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }


                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("회원등록에 성공했습니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();
                                finish();

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("회원 등록에 실패했습니다.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                RegisterRequest registerRequest = new RegisterRequest(userID, sha512PW, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);
            }
        });
    }


    @Override
    protected  void onStop() {
        super.onStop();
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    private class doit extends AsyncTask<Void, Void, Void> {

        Elements words;
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("SSS백그라운드", "실행");

            try {
                Log.d("SSS트라이", "실행");
                Log.d("SSS비밀번호", userPassword);
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                Log.d("SSS비밀번호", userPassword);
                byte[] digest = md.digest(userPassword.getBytes());
                StringBuilder sb = new StringBuilder();
                for (int i=0; i<digest.length; i++){
                    sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
                }
                sha512PW = String.valueOf(sb);
                Log.d("SSS해시", sha512PW);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            String loginURL = "https://eclass2.hufs.ac.kr:4443/ilos/lo/login.acl?usr_id=" + userID +
                    "&usr_pwd=" + sha512PW;

            // 로그인(POST) - HTTPS
            Connection.Response response1 = null;

            try {
                response1 = Jsoup.connect(loginURL)
                        .userAgent(userAgent)
                        .timeout(3000)
                        .method(Connection.Method.POST)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //System.out.println(response1.statusMessage());
            // 로그인 성공 후 얻은 쿠키.
            // 쿠키 중 TSESSION이라는 값을 확인할 수 있다.
            Map<String, String> loginCookie = response1.cookies();
            Log.d("SSS쿠키", String.valueOf(loginCookie));
            try {
                Document doc = Jsoup
                        .connect("http://eclass2.hufs.ac.kr:8181/ilos/main/main_form.acl")
                        .cookies(loginCookie)
                        .get();
                words = doc.select("strong[class=site-font-color]");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            name = words.text();

        }
    }
}
