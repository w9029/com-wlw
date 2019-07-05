package com.lkc.mqttDown;


import com.lkc.model.CustomMap.SingleMap;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Base64;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class DownCallback implements MqttCallback {
    private Lock lock;
    private Condition condition;
    private String UID;

    public DownCallback(Lock alock, Condition cond, String uid) {
        lock = alock;
        condition = cond;
        UID = uid;
    }

    //连接断开
    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("mqtt proxy : 连接断开!!!!!!!");
    }

    //发送信息成功时 回调
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("发送成功" + iMqttDeliveryToken.isComplete());
    }

    //接收信息成功时 回调
    //是个单线程操作 所以最好把 数据处理扔到线程里
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        lock.lock();

        Base64.Decoder decoder = Base64.getDecoder();
        String msg = new String(mqttMessage.getPayload());
        String message = new String(decoder.decode(msg));

        SingleMap singleMap = SingleMap.getInstance();
        singleMap.put(UID, message);
        System.out.println("收到：" + singleMap.get(UID));
        condition.signalAll();

        lock.unlock();
    }
}
