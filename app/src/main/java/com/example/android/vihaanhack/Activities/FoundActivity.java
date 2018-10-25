package com.example.android.vihaanhack.Activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.android.vihaanhack.Models.Lost;
import com.example.android.vihaanhack.R;
import com.firebase.client.Firebase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kairos.Kairos;
import com.kairos.KairosListener;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class FoundActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "FoundActivity";
    EditText etDescribe,etClothes;
    Button btnSubmit;
    FloatingActionButton btnMic,btnStop;
    ImageView pHolder,imageView;
    private static final int CAMERA_REQUEST = 212;
    Bitmap bitmap;
    MediaRecorder mRecorder;
    MediaPlayer mPlayer;
    ProgressDialog progressDialog;
    boolean mStartRecording = true;
    CoordinatorLayout coordinatorLayout;
    private static String mFileName = null;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList<String> lostClothes;
    ArrayList<String> doubtful;



    Uri filepath;

    String app_id = "da94c708";
    String api_key = "c3b72e509462f155a8e5d2381222bb92";
    Kairos myKairos;
    KairosListener listener;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        lostClothes = new ArrayList<>();
        doubtful = new ArrayList<>();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("lost");

        Firebase.setAndroidContext(this);

        progressDialog = new ProgressDialog(this);


        myKairos = new Kairos();
        //found activity done

// set authentication

        myKairos.setAuthentication(this, app_id, api_key);

        // Create an instance of the KairosListener

        listener = new KairosListener() {

            @Override
            public void onSuccess(final String response) {
                // your code here!
                Log.d("KAIROS DEMO", response);

                if (response.equals("{\"Errors\":[{\"Message\":\"no faces found in the image\",\"ErrCode\":5002}]}"))
                {
                    checkElse();
                }



                progressDialog.dismiss();



                databaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Lost lost = dataSnapshot.getValue(Lost.class);
                        if(response.contains(lost.getLostName()))
                        {
                            Log.d(TAG, "onChildAdded: " + lost.getLostName());
                            SmsManager smsManager = SmsManager.getDefault();
                            Log.d(TAG, "onChildAdded: " + lost.getLostMob());
                            Log.d(TAG, "onChildAdded: "+lost.getLostClothes());
                            smsManager.sendTextMessage(lost.getLostMob(),null,"Found Team Hope",null,null);

                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

//                Toast.makeText(FoundActivity.this,response, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(String response) {
                // your code here!
                Log.d(TAG, "onFail: ");
                Log.d("KAIROS DEMO", response);
                checkElse();
            }
        };

        // Record to the external cache directory for visibility


        btnMic = (FloatingActionButton) findViewById(R.id.record);
        btnStop=(FloatingActionButton) findViewById(R.id.stop);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        etDescribe = (EditText) findViewById(R.id.description);
        etClothes = (EditText) findViewById(R.id.clothes);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        btnStop.setVisibility(View.INVISIBLE);

        pHolder = (ImageView) findViewById(R.id.placeholder);
        imageView = (ImageView) findViewById(R.id.image);
        pHolder.setOnClickListener(this);
        imageView.setOnClickListener(this);
        btnMic.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        imageView.setVisibility(View.GONE);
    }

    private void checkElse() {
        String a = etClothes.getText().toString();
        Log.d(TAG, "checkElse: ");
        doubtful.clear();
        int c=0;
        for (int i=0;i<lostClothes.size();++i){
            String[] splited = a.split("\\s+");
            for (int j=0;j<splited.length;++j){
                if (lostClothes.get(i).contains(splited[j])){
                    c++;
                }
                if (c>splited.length/2){
                    doubtful.add(lostClothes.get(i));
                }
            }
        }
        if (doubtful.size()>0){
            for (int k=0;k<doubtful.size();++k){
                Log.d(TAG, "checkElse: "+doubtful.get(k));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                lostClothes.clear();
                for(DataSnapshot lostDataSnapshot : dataSnapshot.getChildren()){
                    Lost lostguy = lostDataSnapshot.getValue(Lost.class);
                    Log.d(TAG, "onDataChange: "+lostguy.getLostClothes());
                    lostClothes.add(lostguy.getLostClothes());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == pHolder){
            pHolder.setVisibility(View.INVISIBLE);
//            imageView.setVisibility(View.VISIBLE);
            openCamera();
        }
        if (view == btnMic){
            btnMic.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.VISIBLE);
            mStartRecording = true;
            onRecord(mStartRecording);
        }
        if (view == btnStop){
            mStartRecording=false;
            btnMic.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.INVISIBLE);
            onRecord(mStartRecording);
        }
        if (view == btnSubmit) {
            if (submitData()){
                progressDialog.setMessage("Searching ...");
                progressDialog.show();
                recognise();


            }
        }
    }

    private void recognise() {

        String galleryId = "People";
        String selector = "FULL";
        String threshold = "0.75";
        String minHeadScale = "0.25";
        String maxNumResults = "25";
        try {
            myKairos.recognize(bitmap,
                    galleryId,
                    selector,
                    threshold,
                    minHeadScale,
                    maxNumResults,
                    listener);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private boolean submitData() {
        if (etClothes.getText().toString()==""){
            Snackbar snackbar  = Snackbar.make(coordinatorLayout,"Please Enter the Clothes!",Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        }
        if (bitmap == null){
            Snackbar snackbar  = Snackbar.make(coordinatorLayout,"Please provide the Image!",Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        }

        return true;
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        Long tsLong = System.currentTimeMillis()/1000;
//        String ts = tsLong.toString();
//        File f = new File(Environment.getExternalStorageDirectory(),"Found"+ts+".jpg");
//        filepath = Uri.fromFile(f);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, filepath);
        startActivityForResult(cameraIntent,CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult: ");
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
//            Log.d(TAG, "onActivityResult: "+data);
//            filepath = data.getData();
//            Log.d(TAG, "onActivityResult: "+data.getExtras().get("data"));
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
//            imageView.setImageURI(filepath);
  //          imageView.setImageBitmap(bitmap);
    //        imageView.setVisibility(View.VISIBLE);
//            photoKaUri = intent.getData();
//            Log.d(TAG, "onActivityResult: " + filepath.toString());
            imageView.setVisibility(View.VISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }




    private void startRecording() {
        mRecorder = new MediaRecorder();
        mFileName = getExternalCacheDir().getAbsolutePath();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        mFileName += "Found"+ts+".3gp";
        Log.d(TAG, "startRecording: "+mFileName);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

}
