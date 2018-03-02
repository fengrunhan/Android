package com.example.fengrunhan.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Looper;
import android.provider.MediaStore;
import android.security.KeyPairGeneratorSpec;
import android.security.KeyStoreParameter;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.zxing.activity.CaptureActivity;
import com.utils.CommonUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Calendar;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;


public class MainActivity extends AppCompatActivity {

    public String TAG = "Keystore";
    public static MainActivity ins;
    TextView tv;

    Button bt1;
    Button bt2;
    Button bt3;
    Button bt4;
    EditText et1, et2, et3;

    //打开扫描界面请求码
    private int REQUEST_CODE = 0x01;
    //扫描成功返回码
    private int RESULT_OK = 0xA1;

    static
    {
        System.loadLibrary("security");
    }

    public native String hello (String a);
    public native String octo_start(String ip, int port, String user);

    public native String octo_callfor(String ip, int port, String user);
    public native String octo_verify(String ip, int port, String user, String filename, int filenamelength);

    public native String octo_show(String filename, int filenamelength, String user);

    public native String octo_env(String ip, int port, String information, String user);

    public native boolean octo_init();
    public native boolean octo_check();

    public native boolean setVerifyFlag();
    public native int getVerifyFlag();

    public native boolean setCallFlag();
    public native int getCallFlag();

    public String tmp(String filename, int filenamelength, String user){
        return octo_show(filename, filenamelength, user);
    }

    public int getVerifyFlag_tmp(){
        return getVerifyFlag();
    }

    public boolean setVerifyFlag_tmp()
    {
        return setVerifyFlag();
    }

    public static boolean debug = true;


    String ip = "192.168.2.249";
    //String ip = "192.168.0.107";
    //String ip = "192.168.0.107";
    //String ip = "121.43.166.69";

    int port = 8888;
    //private static final String FILENAME = "publickey.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ins = this;
        tv = (TextView)this.findViewById(R.id.tv);


        bt1 = (Button)this.findViewById(R.id.bt1);

        bt2 = (Button)this.findViewById(R.id.bt2);
        bt3 = (Button)this.findViewById(R.id.bt3);
        bt4 = (Button)this.findViewById(R.id.bt4);

        et1 = (EditText)this.findViewById(R.id.et1);
        et2 = (EditText)this.findViewById(R.id.et2);
        et3 = (EditText)this.findViewById(R.id.et3);



        if(debug)
        {
            et1.setVisibility(View.INVISIBLE);
            et2.setVisibility(View.INVISIBLE);
        }

        String user = getUser();
        if(!user.equals("NULL"))
        {
            et3.setText(user);
        }





        //hello("abc");
        //encrypt();
        //decrypt();

        //KeyStore();

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("LBD", "click1");
                //String hel = hello("hello");
                //write(hel);
                //tv.setText(read());
                //Log.e("LBD", "写入成功");
                start();
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("LBD", "click2");
                setCallFlag();
                String callfor = octo_callfor(ip,port,getUser());
                boolean flag = true;
                while (flag)
                {
                    int getCall = getCallFlag();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(getCall == 0)
                    {
                        continue;
                    }
                    else if(getCall == 1)
                    {
                        flag = false;
                        setCallFlag();
                        Toast.makeText(getApplicationContext(), "买票成功！", Toast.LENGTH_SHORT).show();
                    }
                    else if(getCall == 2)
                    {
                        flag = false;
                        setCallFlag();
                        Toast.makeText(getApplicationContext(), "买票失败！", Toast.LENGTH_SHORT).show();
                    }
                    else if(getCall == 3 || getCall == 4)
                    {
                        flag = false;
                        setCallFlag();
                        Toast.makeText(getApplicationContext(), "网络错误，请重试！", Toast.LENGTH_SHORT).show();
                    }
                }
                Log.e("LBD callfor", callfor);
            }
        });


        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String verify = octo_verify(ip,port,getUser(),"hello");
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });


        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CommonUtil.isCameraCanUse()){
                    Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }else{
                    Toast.makeText(getApplicationContext(),"请打开此应用的摄像头权限",Toast.LENGTH_SHORT).show();
                }
            }
        });


        if(!checkServerPK()){
            return;
        }
        if(octo_init())
        {
            Toast.makeText(this, "SDK初始化成功", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "SDK初始化失败", Toast.LENGTH_SHORT).show();
        }
    }



    private void KeyStore()
    {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            String alias = "key3";
            int nBefore = keyStore.size();
            if (!keyStore.containsAlias(alias)) {
                Calendar notBefore = Calendar.getInstance();
                Calendar notAfter = Calendar.getInstance();
                notAfter.add(Calendar.YEAR, 1);
                //KeyPairGeneratorSpec.Builder builder = new KeyPairGeneratorSpec.Builder(this);
                //builder.setAlias("");

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias(alias)
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .setKeySize(2048)
                        .setSubject(new X500Principal("CN=test"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .build();


                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);
                KeyPair keyPair = generator.generateKeyPair();
            }
            int nAfter = keyStore.size();
            Log.v(TAG, "Before = " + nBefore + " After = " + nAfter);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            Log.v(TAG, "private key = " + privateKey.toString());
            Log.v(TAG, "public key = " + publicKey.toString());

            String plainText = "This text is supposed to be a secret.";
            Log.v(TAG, "plainText = " + plainText);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            Cipher outCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            outCipher.init(Cipher.DECRYPT_MODE, privateKey);

            CipherOutputStream cipherOutputStream =
                    new CipherOutputStream(
                            outputStream, inCipher);
            cipherOutputStream.write(plainText.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte[] result = outputStream.toByteArray();
            Log.i(TAG, "KeyStore: " + new String(result));


            CipherInputStream cipherInputStream =
                    new CipherInputStream(new ByteArrayInputStream(result),
                            outCipher);
            byte [] roundTrippedBytes = new byte[1000];

            int index = 0;
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                roundTrippedBytes[index] = (byte)nextByte;
                index++;
            }
            String roundTrippedString = new String(roundTrippedBytes, 0, index, "UTF-8");
            Log.v(TAG, "round tripped string = " + roundTrippedString);

        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                InvalidAlgorithmParameterException | KeyStoreException |
                CertificateException | IOException | UnrecoverableEntryException |
                NoSuchPaddingException | InvalidKeyException | UnsupportedOperationException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    protected boolean checkServerPK()
    {
        File f = new File("/data/data/com.example.fengrunhan.client/pkserver.txt");
        if(!f.exists())
        {
            Toast.makeText(this, "需要服务器公钥", Toast.LENGTH_SHORT).show();
        }
        return f.exists();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (resultCode == RESULT_OK) { //RESULT_OK = -1
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("qr_scan_result");
            //将扫描出的信息显示出来
            Log.d("code", scanResult);
            Log.d("code", scanResult.length()+"");


            /*String decodedstr = "";
            decodedstr = new String(Base64.decode(scanResult.getBytes(), Base64.DEFAULT));
            Log.d("decode", decodedstr);
            Log.d("decode", decodedstr.length()+"");*/

            String verifyResult = "";

            try {
                setVerifyFlag();
                verifyResult = octo_verify(ip, port, getUser(), scanResult, scanResult.length());
                if(verifyResult.equals("ScanError"))
                {
                    Toast.makeText(this, "二维码扫描错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean flag = true;
                while(flag)
                {
                    Thread.sleep(500);
                    int getFlag = getVerifyFlag();
                    Log.i("code", "onActivityResult: Checking flag" + getFlag);
                    if(getFlag == 0)
                    {
                        continue;
                    }
                    else if (getFlag == 1)
                    {
                        flag = false;
                        setVerifyFlag();
                        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                        intent.putExtra("info", verifyResult);
                        startActivity(intent);
                    }
                    else if(getFlag == 2)
                    {
                        flag = false;
                        setVerifyFlag();
                        Toast.makeText(this, "验证失败！", Toast.LENGTH_SHORT).show();
                    }
                    else if(getFlag == 3 || getFlag == 4)
                    {
                        flag = false;
                        setVerifyFlag();
                        Toast.makeText(this, "网络错误，请重试！", Toast.LENGTH_SHORT).show();
                    }
                    else if(getFlag == 5)
                    {
                        flag = false;
                        setVerifyFlag();
                        Toast.makeText(this, "二维码超时！", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }


    private void start()
    {
        //String ip = "43.241.44.88";
        if(!checkServerPK()){
            return;
        }
        if(!debug)
        {
            port = getPort();
            if(port == 0)
            {
                Toast.makeText(this, "请输入正确端口号", Toast.LENGTH_SHORT).show();
                return;
            }
            ip = et1.getText().toString().trim();
            if(ip.equals(""))
            {
                Toast.makeText(this, "请输入IP地址", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(!octo_check())
        {
            Toast.makeText(this, "请等待上一次请求处理完成后再点击", Toast.LENGTH_SHORT).show();
            return;
        }
        



        String user = et3.getText().toString().trim();
        if(user.equals(""))
        {
            Toast.makeText(this, "请输入用户标识", Toast.LENGTH_SHORT).show();
            return;
        }
        if(user.contains("|"))
        {
            Toast.makeText(this, "USER标识不合法", Toast.LENGTH_SHORT).show();
            return;
        }
        saveUser(user);

        String shid = octo_start(ip, port, user);
        Log.e("LBD SHID", shid);
        //tv.setText("本次业务ID：" + shid);
        Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
    }

    private Integer getPort()
    {
        Integer port;
        try {
            port = Integer.valueOf(et2.getText().toString());
        }catch (Exception e)
        {
            e.printStackTrace();
            port = 0;
        }
        return port;
    }

    private String getUser()
    {
        SharedPreferences settings = getSharedPreferences("octo_tmp", 0);
        String user = settings.getString("user", "NULL");
        return user;
    }

    private void saveUser(String user)
    {
        SharedPreferences settings = getSharedPreferences("octo_tmp", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("user", user);
        editor.commit();
    }


}


