package ai.wit.eval.wit_eval;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;

import ai.wit.sdk.IWitListener;
import ai.wit.sdk.Wit;
import ai.wit.sdk.model.WitOutcome;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.eclipse.paho.client.mqttv3.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements IWitListener {
    private static final int CONNECT_TIMEOUT = 2000;
    private static final String CLIENT_ID = "android_user";
    private static final String BROKER = "tcp://m10.cloudmqtt.com:14344";
    MqttAsyncClient client ;
    Wit _wit;
    private MqttConnectOptions getConnectOptions() {
        MqttConnectOptions connOpts = new MqttConnectOptions();

        connOpts.setCleanSession(true);
        connOpts.setConnectionTimeout(3);
        connOpts.setKeepAliveInterval(60);
        connOpts.setUserName("light");
        connOpts.setPassword("light".toCharArray());
        connOpts.setServerURIs(new String[]{});
        return connOpts;
    }
    public class Intension {
        public String intent = null;
        public float  confidence = 0;
        public Strings entities = null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String accessToken = "32T6P4CGX7VT3TXQTLHEIQUCM63Y4BPG";
        _wit = new Wit(accessToken, this);
        _wit.enableContextLocation(getApplicationContext());
        try {
            client = new MqttAsyncClient(BROKER, CLIENT_ID, null);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d("Mqtt", "connection lost");
                    cause.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.d("Mqtt message", message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { /* not used */ }
            });
            final IMqttToken connectToken = client.connect(getConnectOptions());
            connectToken.waitForCompletion(CONNECT_TIMEOUT);

            Log.d("Mqtt", "client_connected=" + client.isConnected());

        } catch (MqttException e) {
            Log.d("Mqtt", "could not create mqtt client");
            e.printStackTrace();
        }
    }
    public void Mqtt(String topic,String command){
        try {

            client.publish("/"+topic, new MqttMessage(command.getBytes()));
            client.subscribe("/"+topic, 1);
        } catch (MqttException e) {
            Log.d("Mqtt", "could not create mqtt client");
            e.printStackTrace();
        }
    }

    public void toggle(View v) {
        try {
            _wit.toggleListening();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void witDidGraspIntent(ArrayList<WitOutcome> witOutcomes, String messageId, Error error) {
        TextView jsonView = (TextView) findViewById(R.id.jsonView);
        jsonView.setMovementMethod(new ScrollingMovementMethod());
        Gson gson = new GsonBuilder().create();

        if (error != null) {
            jsonView.setText(error.getLocalizedMessage());
            return ;
        }
        String jsonOutput = gson.toJson(witOutcomes);
        jsonView.setText(jsonOutput);
        if (jsonOutput.contains("fanon")) {
            Log.d("Mqtt",jsonOutput);
            if (jsonOutput.contains("\"value\":\"on\"")){
                Mqtt("fan","on");
                Log.d("Mqtt fan","on");
            }
        }
        if (jsonOutput.contains("fanon")) {
            Log.d("Mqtt",jsonOutput);
            if (jsonOutput.contains("\"value\":\"off\"")){
                Mqtt("fan","off");
                Log.d("Mqtt fan","on");
            }
        }
        if (jsonOutput.contains("halllight")) {
            Log.d("Mqtt",jsonOutput);
            if (jsonOutput.contains("\"value\":\"on\"")){
                Mqtt("light","on");
                Log.d("Mqtt light","on");
            }
        }
         if (jsonOutput.contains("halllight")) {
            Log.d("Mqtt",jsonOutput);
            if (jsonOutput.contains("\"value\":\"off\"")){
                Mqtt("light","off");
                Log.d("Mqtt light","off");
            }
        }
        ((TextView) findViewById(R.id.txtText)).setText("Done!");
    }

    @Override
    public void witDidStartListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Witting...");
    }

    @Override
    public void witDidStopListening() {
        ((TextView) findViewById(R.id.txtText)).setText("Processing...");
    }

    @Override
    public void witActivityDetectorStarted() {
        ((TextView) findViewById(R.id.txtText)).setText("Listening");
    }

    @Override
    public String witGenerateMessageId() {
        return null;
    }

    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.wit_button, container, false);
        }
    }

}