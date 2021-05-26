package com.example.persontracking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.*;

public class TrackingPerson extends Fragment {

    public MqttAndroidClient mqttAndroidClient;
    private String serverUri = null;
    private String HOST_IP_ADDRESS1 = "0.00.00";
    private final int PORT_NUM1 = 1883;
    final String clientId = "rb5_client";
    final String subscriptionTopic = "tracked";
    final String PUB_TOPIC = "track";
    private String payload;
    TextView dataReceived;
    Button button2;
    private TextInputEditText personNametext1;

    public TrackingPerson() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view1 = inflater.inflate(R.layout.fragment_trackingperson, container, false);
        dataReceived = (TextView) view1.findViewById(R.id.dataReceived);
        button2 = view1.findViewById(R.id.buttonTrackPerson);
        personNametext1 = view1.findViewById(R.id.edit_text2);

        // Get Ip address from IpConnection
        Intent intent = getActivity().getIntent();
        HOST_IP_ADDRESS1 = intent.getStringExtra(String.valueOf(R.string.host_ip_address));

        serverUri = "tcp://" + HOST_IP_ADDRESS1 + ":" + PORT_NUM1;

        startMqtt();

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payload = personNametext1.getText().toString();
                if (payload.isEmpty()) {
                    Toast.makeText(getContext(),"Please Enter Person Name",Toast.LENGTH_LONG).show();
                    payload = personNametext1.getText().toString();
                }
                if(!payload.isEmpty()) {
                    publishMessage();
                    button2.setEnabled(false);
                }
            }
        });
        return view1;
    }

    private void startMqtt() {
        mqttHelper(getContext());
        setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }
            @Override
            public void connectionLost(Throwable throwable) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.d("Debug", mqttMessage.toString());
                JSONObject json = new JSONObject(mqttMessage.toString());
                String name = json.getString("name");
                String location = json.getString("location");
                String score = json.getString("score");
                String message = "Person Tracking Information\n"+"name : "+name+"\n"+"location : "+location+"\n"+"score : "+score;
                dataReceived.setText(message);
                button2.setEnabled(true);
                personNametext1.setText(null);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    public void mqttHelper(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d("mqtt", s);
            }
            @Override
            public void connectionLost(Throwable throwable) {
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.d("Mqtt", mqttMessage.toString());
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Mqtt", "Subscribed!");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Mqtt", "Subscribed fail!");
                }
            });
        } catch (MqttException e) {
            Log.d("Mqtt", "Exception for subscribing");
            e.printStackTrace();
                    }
    }

    private void publishMessage() {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(PUB_TOPIC, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Mqtt", "publish succeed!");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("Mqtt", "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.d("Mqtt", "Exception for publishing");
            e.printStackTrace();
        }
    }

}