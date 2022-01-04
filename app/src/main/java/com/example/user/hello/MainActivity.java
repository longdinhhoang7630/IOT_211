package com.example.user.hello;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
//import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    //int i=0;
    //TextView output;
    MQTTHelper mqttHelper ;
    TextView txtTemp,txtHumid;
    ToggleButton btnLED;
    ProgressBar tempBar, humidBar;
    LineChart mpLineChart;
    ArrayList<Entry> tempData = new ArrayList<>();
    ArrayList<Entry> humidData = new ArrayList<>();
    Float timeTemp =0.0f;
    Float timeHumid =0.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtHumid = findViewById(R.id.txtHumidity);
        btnLED = findViewById(R.id.btnLED);
        tempBar = findViewById(R.id.temp_bar);
        humidBar = findViewById(R.id.humid_bar);

        mpLineChart = findViewById(R.id.lineChart);
        //createLineChart();


        //txtTemp.setText("40"+"°C");
        //txtHumid.setText("80"+"%");
        //output = findViewById(R.id.output);

        btnLED.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                //btnLED.setVisibility(View.INVISIBLE);
                if(check == true){
                    Log.d("mqtt",  "Button is checked");
                    sendDataMQTT("dhl2k/f/bbc-led", "1");
                }else{
                    Log.d("mqtt",  "Button is unchecked");
                    sendDataMQTT("dhl2k/f/bbc-led", "0");
                }
            }
        });
        //btnLED.setVisibility(View.VISIBLE);
        startMQTT();
        setupScheduler();
    }

   private void createLineChart(){
       //Log.d("Temp Data:",tempData.toString());
       //Log.d("Humid Data:",humidData.toString());
        LineDataSet line_temp = new LineDataSet(tempData,"Temperature data");
        LineDataSet line_humid = new LineDataSet(humidData,"Humidity data");

        line_temp.setLineWidth(4);
        line_temp.setColor(Color.rgb(166,6,22));
        line_temp.setDrawCircles(true);
        line_temp.setDrawCircleHole(false);
        line_temp.setCircleColor(Color.GREEN);
        //line_temp.setCircleColorHole(Color.GREEN);
        line_temp.setCircleRadius(4);
        line_temp.setValueTextSize(14);

        line_humid.setLineWidth(4);
        line_humid.setColor(Color.BLUE);
        line_humid.setDrawCircles(true);
        line_humid.setDrawCircleHole(false);
        line_humid.setCircleColor(Color.GREEN);
        //line_humid.setCircleColorHole(Color.GREEN);
        line_humid.setCircleRadius(4);
        line_humid.setValueTextSize(14);

        mpLineChart.setNoDataText("No Data");
        mpLineChart.setNoDataTextColor(Color.BLUE);
        mpLineChart.setDrawGridBackground(true);
        mpLineChart.setDrawBorders(true);
        mpLineChart.setBorderColor(Color.BLACK);
        mpLineChart.setBorderWidth(1);

        Legend legend = mpLineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(18);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setFormSize(30);
        legend.setXEntrySpace(20);
        legend.setFormToTextSpace(10);

        Description description = new Description();
        description.setText("Time");
        description.setTextColor(Color.BLUE);
        description.setTextSize(15);
        mpLineChart.setDescription(description);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(line_temp);
        dataSets.add(line_humid);
        LineData data = new LineData(dataSets);
        // data.setValueFormatter(new TempFormatter());
        //mpLineChart.animateXY(5000,5000, Easing.EasingOption.EaseInOutBounce, Easing.EasingOption.EaseInExpo);
        mpLineChart.setData(data);
        mpLineChart.invalidate();
    }
    private ArrayList<Entry> dataValues1(){
        ArrayList<Entry> dataValue = new ArrayList<>();
        dataValue.add(new Entry(0,20));
        dataValue.add(new Entry(1,24));
        dataValue.add(new Entry(2,2));
        dataValue.add(new Entry(3,10));
        dataValue.add(new Entry(4,28));
        dataValue.add(new Entry(5,30));
        dataValue.add(new Entry(7,18));
        dataValue.add(new Entry(10,40));
        return  dataValue;
    }

    private ArrayList<Entry> dataValues2(){
        ArrayList<Entry> dataValue = new ArrayList<>();
        dataValue.add(new Entry(0,12));
        dataValue.add(new Entry(2,16));
        dataValue.add(new Entry(3,23));
        dataValue.add(new Entry(5,38));
        dataValue.add(new Entry(7,5));
        dataValue.add(new Entry(10,80));
        return  dataValue;
    }

    /*private class TempFormatter implements IValueFormatter{

        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return v + "°C";
        }
    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    int waiting_period =0;
    boolean send_message_again = false;
    List<MQTTMessage1> list = new ArrayList<>();
    Timer aTimer = new Timer();
    private void setupScheduler(){

        TimerTask scheduler = new TimerTask() {
            @Override
            public void run() {
                //btnLED.setVisibility(View.VISIBLE);
                if(waiting_period > 0){
                    waiting_period --;
                    if(waiting_period == 0){
                        send_message_again = true;
                    }
                }
                if(send_message_again == true){
                    //sendDataMQTT("abc", "123");
                    Log.d("mqtt",  "Timer is executed");
                    Log.d("mqtt",  "Resent again" + list.get(0).topic.toString());
                    sendDataMQTT(list.get(0).topic, list.get(0).mess);
                    list.remove(0);
                }
            }
        };
        aTimer.schedule(scheduler,0, 1000);
    }

    private void sendDataMQTT(String topic, String value){
        waiting_period =0;
        send_message_again = false;
        MQTTMessage1 buffer = new MQTTMessage1();
        buffer.topic = topic; buffer.mess =value;
        list.add(buffer);

        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);

        }catch (MqttException e){
        }
    }

    private void startMQTT(){
        mqttHelper = new MQTTHelper(getApplicationContext(), "abcde");
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("mqtt",  "Connection is successful");
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Log.d("mqtt",  "Received:" + message.toString());
                //Log.d("mqtt",  "Topic:" + topic.toString());
               // Log.d("mqtt",  "Received:" + Integer.toString(i));
                //i++;
               // output.setText(message.toString());
                if(topic.equals("dhl2k/f/bbc-temp")){
                    String temp = String.format("%s °C",message.toString());
                    txtTemp.setText(temp);
                    tempBar.setProgress(Math.round(Float.parseFloat(message.toString())));
                   tempData.add(new Entry(timeTemp += 1, Float.parseFloat(message.toString())));
                   //createLineChart();
                    //Log.d("Temp Data:",tempData.toString());
                }
                if(topic.equals("dhl2k/f/bbc-humid")){
                    String humid = String.format("%s %%",message.toString());
                    txtHumid.setText(humid);
                    humidBar.setProgress(Math.round(Float.parseFloat(message.toString())));
                    humidData.add(new Entry(timeHumid += 1, Float.parseFloat(message.toString())));
                    createLineChart();
                    //Log.d("Humid Data:",humidData.toString());
                }
                if(topic.equals("dhl2k/f/bbc-led")){
                   /*if(message.toString().contains(list.get(0).mess)){ //message.toString().equals("1")
                        btnLED.setChecked(true);
                    }else{
                        btnLED.setChecked(false);
                    }*/
                    int btn= Integer.parseInt(message.toString());
                    /*if(message.toString().contains(list.get(0).mess)){
                        if(!list.isEmpty()) {
                            btnLED.setVisibility(View.VISIBLE);
                            send_message_again = true;
                            waiting_period = 0;
                            list.remove(0);
                        }
                    }*/
                    if (btn==0){
                        btnLED.setChecked(false);
                    }
                    else{
                        btnLED.setChecked(true);
                    }
                }
                /*if(topic.contains("abc") && message.toString().contains("123")){
                    waiting_period =0;
                    send_message_again = false;
                }*/
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public class MQTTMessage1{
        public String topic;
        public String mess;
    }
}
