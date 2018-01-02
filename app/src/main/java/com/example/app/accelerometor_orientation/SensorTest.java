package com.example.app.accelerometor_orientation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class SensorTest extends Activity implements SensorEventListener {

    SensorManager sensorManager = null;
    SensorEvent sensorEvent = null;

    //for accelerometer values
    TextView outputX;
    TextView outputY;
    TextView outputZ;

    //for orientation values
    TextView outputX2;
    TextView outputY2;
    TextView outputZ2;

    //Send button
    Button button;

    //MQTT
    static String MQTTHOST = "tcp://m23.cloudmqtt.com:15660";
    static String USERNAME = "uzjzhqyg";
    static String PASSWORD = "1TAG9wh0TTpa";
    MqttAndroidClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.activity_sensor_test);

        //just some textviews, for data output
        outputX = (TextView) findViewById(R.id.textView01);
        outputY = (TextView) findViewById(R.id.textView02);
        outputZ = (TextView) findViewById(R.id.textView03);

        outputX2 = (TextView) findViewById(R.id.textView04);
        outputY2 = (TextView) findViewById(R.id.textView05);
        outputZ2 = (TextView) findViewById(R.id.textView06);

        button = (Button) findViewById(R.id.button);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(SensorTest.this,"connected!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(SensorTest.this,"connection Failed!", Toast.LENGTH_LONG).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), sensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
    }

    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            switch (event.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    outputX.setText("x:"+Float.toString(event.values[0]));
                    outputY.setText("y:"+Float.toString(event.values[1]));
                    outputZ.setText("z:"+Float.toString(event.values[2]));
                    break;
                case Sensor.TYPE_ORIENTATION:
                    outputX2.setText("x:"+Float.toString(event.values[0]));
                    outputY2.setText("y:"+Float.toString(event.values[1]));
                    outputZ2.setText("z:"+Float.toString(event.values[2]));
                    sensorEvent = event;
                    break;

            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void publish(View view){
        SensorEvent event = sensorEvent;

        String topic = "sensor/data";
        String message = Float.toString(event.values[0])+"/"+Float.toString(event.values[1])+"/"+Float.toString(event.values[2])+"/2";
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
