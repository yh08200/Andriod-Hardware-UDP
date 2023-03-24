package com.mj.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Register extends AppCompatActivity {
    //数据库对象
    public Sqlhelper sqlhelper;
    //用户
    private EditText m_txtUser;
    //密码
    private  EditText m_txtPwd;
    //确认密码
    private  EditText m_txtPwd_again;
    //确定按钮
    private Button m_btnSure;

    private Spinner m_spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        m_txtUser =findViewById(R.id.txtUser_register);
        m_txtPwd=findViewById(R.id.txtPwd_register);
        m_txtPwd_again=findViewById(R.id.txtPwd_register_Again);
        m_btnSure=findViewById(R.id.btnSure);
        m_spinner =findViewById(R.id.spinnerUnit_register);
        sqlhelper = new Sqlhelper(Register.this, "MyDataBase.db", null, 1);
        m_btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _userName = m_txtUser.getText().toString().trim();
                String _pwd = m_txtPwd.getText().toString().trim();
                String _pwd_again = m_txtPwd_again.getText().toString().trim();
                String _type =(String) m_spinner.getSelectedItem();
                if(_userName.isEmpty() || _pwd.isEmpty()||_pwd_again.isEmpty()) {
                   Toast.makeText(Register.this,"请输入账号或者密码",Toast.LENGTH_SHORT).show();
                   return;
                }
                if(!_pwd.equals(_pwd_again)){
                    Toast.makeText(Register.this,"二次密码不一致",Toast.LENGTH_SHORT).show();
                    return;
                }
                SQLiteDatabase r_db = sqlhelper.getReadableDatabase();
                Cursor cursor = r_db.query("userTable", new String[]{"name"}, "name=?", new String[]{_userName}, null, null, null);
                if (cursor.getCount() >0) {
                    Toast.makeText(Register.this, "用户："+_userName+"已存在", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase w_db = sqlhelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("name", _userName);
                values.put("pwd", _pwd);
                values.put("userType", _type);
                long res= w_db.insert("userTable", null, values);
                if (res>0) {
                    Intent intent=new Intent(Register.this,Login.class);
                    startActivity(intent);
                }
            }
        });
    }
}