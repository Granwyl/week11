package id.ac.week10;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    EditText etusn,etp;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etusn = findViewById(R.id.editTextUsername);
        etp = findViewById(R.id.editTextPassword);
        btn = findViewById(R.id.button);

    }

    public void buttonClick(View view) {
        login();
    }

    private void login() {
        //Toast.makeText(getApplicationContext(), "oke", Toast.LENGTH_SHORT).show();
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                getResources().getString(R.string.urlservice),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.i("response",s);
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            int code = jsonObject.getInt("code");
                            String message = jsonObject.getString("message");
                            if(code==1){
                                Intent z = new Intent(Login.this,MainActivity.class);
                                startActivity(z);
                                finish();
                            }
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("function","login");
                params.put("username",etusn.getText().toString());
                params.put("password",etp.getText().toString());
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}