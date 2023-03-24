package com.mj.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //UPD客户端
    public static UDPClient mUDPClient;
    //消息处理句柄
    private final MyHandler myHandler = new MyHandler(this);
    //发送状态
    private int SendState = 0;
    //重连次数 10次连不上就离线
    private int ConnectedCount = 10;
    //是否连接
    private boolean isConnectedFlag;
    //连接备份
    private boolean isConnectedFlagBak;
    //数据库对象
    public Sqlhelper sqlhelper;
    //在线文本
    private TextView m_lblLink;
    //消息文本
    private TextView m_lblMsg;
    //类别
    private TextView m_lbltype;
    //发送A指令按钮
    private Button m_btnSendA;
    //发送B指令按钮
    private Button m_btnSendB;
    //发送C指令按钮
    private Button m_btnSendC;
    //发送D指令按钮
    private Button m_btnSendD;
    //发送E指令按钮
    private Button m_btnSendE;
    //分享按钮
    private Button m_btnShare;
    //表格布局
    private TableLayout m_tableLayout;
    //日期格式化
    private  SimpleDateFormat  formatter  =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //对象集合
    private  List<lm_value> lst=new ArrayList<>();
    @Override
    protected void onResume() {
        super.onResume();
        Systick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_lblLink =findViewById(R.id.lblLink);
        m_lbltype=findViewById(R.id.lbltype);
        //m_lblMsg = findViewById(R.id.txtMessage);
        m_btnSendA = findViewById(R.id.btnSendA);
        m_btnSendB = findViewById(R.id.btnSendB);
        m_btnSendC = findViewById(R.id.btnSendC);
        m_btnSendD = findViewById(R.id.btnSendD);
        m_btnSendE = findViewById(R.id.btnSendE);
        m_btnShare = findViewById(R.id.btnShare);
        m_tableLayout=findViewById(R.id.table);
        isConnectedFlag = false;
        isConnectedFlagBak = true;
        sqlhelper = new Sqlhelper(MainActivity.this, "MyDataBase.db", null, 1);
        mUDPClient = new UDPClient();
        mUDPClient.Receiver();
        Socket_Observer socket_Observer = new Socket_Observer(mUDPClient);//建立观察者
        Intent intent = getIntent();
        String _type = intent.getStringExtra("type");
        m_lbltype.setText(_type);
        if(_type.equals("学生")) m_btnShare.setVisibility(View.GONE);
        else  m_btnShare.setVisibility(View.VISIBLE);
        m_btnShare.setOnClickListener(this);
        m_btnSendA.setOnClickListener(this);
//        SQLiteDatabase w_db = sqlhelper.getWritableDatabase();
//        for (int i = 0; i < 10; i++) {
//            ContentValues values = new ContentValues();
//            values.put("tempValue", 1.5f*i);
//            values.put("humidityValue", 2.5f*i);
//            values.put("lightValue", 3.5f*i);
//            values.put("smokeConcValue", 4.5f*i);
//            values.put("time", formatter.format(new Date(System.currentTimeMillis())));
//            long res= w_db.insert("valueTable", null, values);
//            Log.d("插入结果",res+"");
//        }
        GetAllTableData();

    }

    //点击事件集合
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShare:
                ShareData();
                break;
            case  R.id.btnSendA:
                SendA();
                break;
        }
    }
    //发送指令A
    private void SendA() {
        String str = "A" + "\r\n";
        mUDPClient.send(str.getBytes(), str.length());
        Toast.makeText(MainActivity.this, "已下发A指令", Toast.LENGTH_SHORT).show();
    }

    //分享数据
    private void ShareData() {
        if(lst.size()==0) {
            Toast.makeText(MainActivity.this, "请先获取记录", Toast.LENGTH_SHORT).show();
            return;
        }
        //CSV标题
        String title;
        String fileName = formatter.format(new Date(System.currentTimeMillis()));
        //CSV每行list
        List<String> listStrings = new ArrayList<>();
        //CSV合并后的list
        List<String> mergeList = new ArrayList<>();
        title="温度值,湿度值,光强值,烟雾浓度值,时间";
        for (int i = 0; i < lst.size(); i++) {
            lm_value _record=lst.get(i);
            listStrings.add(_record.tempValue+","+_record.humidityValue+
                    ","+_record.lightValue+","+_record.smokeConcValue+","+_record.time);
        }
        mergeList.add(title);
        for (String value : listStrings) {
            mergeList.add(value);
        }
        //分享CSV文件
        FileShareUtils.shareCsvFile(this, mergeList,fileName);
    }

    private  void RefreshData(lm_value item)
    {
        TableRow row = new TableRow(getApplicationContext());
        TextView txt_Temp=(TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);

        TextView txt_Humidity = (TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);
        TextView txt_light = (TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);
        TextView txt_SomkeConc =(TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);// new TextView(getApplicationContext());
        //11 TextView txt_Time =(TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);

        String _tempValue=item.tempValue;
        String _humidityValue=item.humidityValue;
        String _lightValue=item.lightValue;
        String _smokeConcValue=item.smokeConcValue;
        txt_Temp.setText(_tempValue);
        row.addView(txt_Temp);
        txt_Humidity.setText(_humidityValue);
        row.addView(txt_Humidity);
        txt_light.setText(_lightValue);
        row.addView(txt_light);
        txt_SomkeConc.setText(_smokeConcValue);
        row.addView(txt_SomkeConc);
        m_tableLayout.addView(row);
        lst.add(item);
    }

    //获取所有表格数据
    private  void GetAllTableData() {
        SQLiteDatabase _db = sqlhelper.getReadableDatabase();
        Cursor cursor = _db.query("valueTable", null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lst.clear();
            for (int i = 0; i < cursor.getCount(); i++) {
                TableRow row = new TableRow(getApplicationContext());

                TextView txt_Temp=(TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);

                TextView txt_Humidity = (TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);
                TextView txt_light = (TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);
                TextView txt_SomkeConc =(TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);// new TextView(getApplicationContext());
                //11 TextView txt_Time =(TextView)getLayoutInflater().inflate(R.layout.textviewtemplate, null);

                String _tempValue=cursor.getString(cursor.getColumnIndexOrThrow("tempValue"));
                String _humidityValue=cursor.getString(cursor.getColumnIndexOrThrow("humidityValue"));
                String _lightValue=cursor.getString(cursor.getColumnIndexOrThrow("lightValue"));
                String _smokeConcValue=cursor.getString(cursor.getColumnIndexOrThrow("smokeConcValue"));
                //11 String _time=cursor.getString(cursor.getColumnIndexOrThrow("time"));
                lm_value lmValue=new lm_value();
                lmValue.tempValue=_tempValue;
                lmValue.humidityValue=_humidityValue;
                lmValue.lightValue=_lightValue;
                lmValue.smokeConcValue=_smokeConcValue;
                //11 lmValue.time=_time;
                lst.add(lmValue);

                txt_Temp.setText(_tempValue);
                row.addView(txt_Temp);
                txt_Humidity.setText(_humidityValue);
                row.addView(txt_Humidity);
                txt_light.setText(_lightValue);
                row.addView(txt_light);
                txt_SomkeConc.setText(_smokeConcValue);
                row.addView(txt_SomkeConc);
                //11 txt_Time.setText(_time);
                //11 row.addView(txt_Time);
                m_tableLayout.addView(row);
                cursor.moveToNext();
            }
        }
    }

    //获取括号中的数值
    public  List<String> extractMessageByRegular(String msg){
        List<String> list=new ArrayList<String>();
        Pattern p = Pattern.compile("\\((.*?\\))");
        Matcher m = p.matcher(msg);
        while(m.find()){
            list.add(m.group().substring(1, m.group().length()-1));
        }
        return list;
    }
    //定时器
    private void Systick() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // (1) 使用handler发送消息
                Message message = new Message();
                message.what = 0;
                myHandler.sendMessage(message);
            }
        }, 1000, 2000);//二秒一次
    }
    //观察者-----网络接收
    public class Socket_Observer implements Observer {
        private Observable observable;
        public Socket_Observer(Observable observable) {
            // 构造器需要Observable作为参数
            this.observable = observable;
            observable.addObserver(this);
        }
        public void display() {
            // 将数据显示在布告板上
            }
        @Override
        public void update(Observable o, Object arg) {
            // 当被观察者有更新使触发
            Message message = new Message();
            if (o instanceof UDPClient) {
                String tempread;
                tempread = (String) arg;
                System.out.println("显示：" + tempread);
                if (tempread.substring(0, 3).equals("STA")) {
                    message.what = 8;
                    List<String> templist=  extractMessageByRegular(tempread);
                    message.obj = templist;
                    myHandler.sendMessage(message);
                }
            }
            isConnectedFlag = true;
            ConnectedCount = 10;
        }
    }
    //自定义句柄
    private class MyHandler extends android.os.Handler {
        private WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity != null) {
                    switch (msg.what) {
                        case 0:
                            systick_program();
                            break;
                        case 8:
                            isConnectedFlag=true;
                            List<String> _lst=(List<String>)msg.obj;
                            String tempValue=_lst.get(0);
                            String humidityValue=_lst.get(1);
                            String lightValue=_lst.get(2);
                            String smokeConcValue=_lst.get(3);
                            String _time=formatter.format(new Date(System.currentTimeMillis()));
                            m_btnSendB.setText("温度值："+tempValue); //32
                            m_btnSendC.setText("湿度值："+humidityValue); // 80
                            m_btnSendD.setText("光强值："+lightValue); // 1.5*10^6
                            m_btnSendE.setText("烟雾浓度值："+smokeConcValue); //1000ppm
                            if(Float.valueOf(tempValue)>32)
                                Toast.makeText(MainActivity.this, "温度超过32，异常", Toast.LENGTH_LONG).show();
                            if(Float.valueOf(humidityValue)>80)
                                Toast.makeText(MainActivity.this, "湿度超过80，异常", Toast.LENGTH_LONG).show();
                            if(Float.valueOf(lightValue)>(1.5f*10*10*10*10*10*10))
                                Toast.makeText(MainActivity.this, "光强超过1.5兆帕，异常", Toast.LENGTH_LONG).show();
                            if(Float.valueOf(smokeConcValue)>1000)
                                Toast.makeText(MainActivity.this, "烟雾浓度超过1000ppm，异常", Toast.LENGTH_LONG).show();
                            SQLiteDatabase w_db = sqlhelper.getWritableDatabase();
                            ContentValues values = new ContentValues();
                            values.put("tempValue", tempValue);
                            values.put("humidityValue", humidityValue);
                            values.put("lightValue", lightValue);
                            values.put("smokeConcValue", smokeConcValue);
                            values.put("time", _time);
                            long res= w_db.insert("valueTable", null, values);
                            lm_value _value=new lm_value();
                            _value.time=_time;
                            _value.lightValue=lightValue;
                            _value.tempValue=tempValue;
                            _value.humidityValue=humidityValue;
                            _value.smokeConcValue=smokeConcValue;
                            RefreshData(_value);
                            break;
                        default:
                            break;

                }
            }
        }
    }
    //判断是否在线
    public  void systick_program() {
        ConnectedCount = ConnectedCount - 1;
        if (ConnectedCount == 0) {
            isConnectedFlag = false;
        }
        if ((isConnectedFlag != isConnectedFlagBak)) {
            if (isConnectedFlag) {
                this.m_lblLink.setText("在线.");
                this.m_lblLink.setTextColor(Color.BLUE);
            } else {
                this.m_lblLink.setText("离线.");
                this.m_lblLink.setTextColor(Color.RED);
            }
        }
        isConnectedFlagBak = isConnectedFlag;

        String tick;
        switch (SendState) {
            case 0:
                tick = "L0" + ConnectedCount + "\r\n";
                mUDPClient.send(tick.getBytes(), tick.length());
                break;
        }
    }
    //追加新内容
    public void addMessage(final String strMsg) {
        final String strText = strMsg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //已有的log
                String strMsgs = m_lblMsg.getText().toString().trim();
                if (strMsgs.equals("")) {
                    strMsgs = strText;
                } else {
                    strMsgs += "\r\n" + strText;
                }
                //刷新添加新的log
                m_lblMsg.setText(strMsgs);
                //log View自动滚动
                m_lblMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        int scrollAmount = m_lblMsg.getLayout().getLineTop(m_lblMsg.getLineCount()) - m_lblMsg.getHeight();
                        if (scrollAmount > 0)
                            m_lblMsg.scrollTo(0, scrollAmount);
                        else
                            m_lblMsg.scrollTo(0, 0);
                    }
                });
            }
        });
    }
}