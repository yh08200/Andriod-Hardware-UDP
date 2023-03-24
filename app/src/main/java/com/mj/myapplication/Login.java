package com.mj.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Login extends AppCompatActivity implements View.OnClickListener{
    //数据库对象
    public Sqlhelper sqlhelper;
    //用户
    private EditText m_txtUser;
    //密码
    private  EditText m_txtPwd;
    //登录按钮
    private Button m_btnLogin;
    //注册按钮
    private  Button m_btnRegister;
    //用户类别
    private Spinner m_spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        m_txtUser =findViewById(R.id.txtUser);
        m_txtPwd=findViewById(R.id.txtPwd);
        m_btnLogin=findViewById(R.id.btnLogin);
        m_btnRegister=findViewById(R.id.btnRegister);
        m_spinner=findViewById(R.id.spinnerUnit);

        m_btnLogin.setOnClickListener(this);
        m_btnRegister.setOnClickListener(this);
        sqlhelper = new Sqlhelper(Login.this, "MyDataBase.db", null, 1);
    }

    //点击事件集合
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                LoginApp();
                break;
            case  R.id.btnRegister:
                RegisterApp();
                break;

        }
    }

    //注册APP
    private void RegisterApp() {
        Intent intent=new Intent(Login.this,Register.class);
        startActivity(intent);
    }

    //登录APP
    private void LoginApp() {
        String _userName = m_txtUser.getText().toString().trim();
        String _pwd = m_txtPwd.getText().toString().trim();
        String _type =(String) m_spinner.getSelectedItem();
        if (_userName.isEmpty() || _pwd.isEmpty()) {
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
            return;
        }
        SQLiteDatabase _db = sqlhelper.getReadableDatabase();
        Cursor cursor = _db.query("userTable", null, "name=?", new String[]{_userName}, null, null, null);
        if (null == cursor || cursor.getCount() == 0) {
            Toast.makeText(this, "未找到用户"+_userName+",请先注册", Toast.LENGTH_SHORT).show();
            return;
        }
        cursor.moveToFirst();
        @SuppressLint("Range")
        String username= cursor.getString(cursor.getColumnIndex("name"));
        @SuppressLint("Range")
        String pwd= cursor.getString(cursor.getColumnIndex("pwd"));
        @SuppressLint("Range")
        String type=cursor.getString(cursor.getColumnIndex("userType"));

        if(_userName.equals(username)&&_pwd.equals(pwd)&&_type.equals(type))
        {
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("type", type);
            startActivity(intent);
        }else{
            Toast.makeText(this, "请检查账号密码或用户类型", Toast.LENGTH_SHORT).show();
            return;
        }
        cursor.close();
    }
}