package com.thalmichackd.myowhip;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import ai.api.AIConfiguration;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends ActionBarActivity implements AIListener {

    private Toast mToast;
    private Vector3 lastGyro;
    private long cooldownTimestamp;
    private MediaPlayer punchCrack;
    private TextView resultTextView;
    private AIService aiService;
    private String CLIENT_ACCESS_TOKEN = "d2f72c6df1c646b68accd642f1b64410";
    private String SUBSCRIPTION_KEY = "547af05e-1b49-471d-936b-829974d6f7f4";
    private MediaPlayer mikeTyson;
    private EditText punch;
    private int counter = 0;
    private ImageView leftBoxingGlove;
    Button btnStop;
    TextView textViewTime;

    private DeviceListener mListener = new AbstractDeviceListener() {

        @Override
        public void onConnect(Myo myo, long timestamp) {

            showToast(myo.getName() + " Connected ");
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            showToast(myo.getName() + " Disconnected");
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            //mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
            leftBoxingGlove.setImageResource(R.drawable.left_boxing_glove);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            //mTextView.setText(R.string.hello_world);
            leftBoxingGlove.setImageResource(R.drawable.left_boxing_glove);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }

            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            leftBoxingGlove.setRotation(roll);
            leftBoxingGlove.setRotationX(pitch);
            leftBoxingGlove.setRotationY(yaw);
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    //mTextView.setText(getString(R.string.hello_world));
                    leftBoxingGlove.setImageResource(R.drawable.left_boxing_glove);
                    break;
                case REST:
                case DOUBLE_TAP:
                    //int restTextId = R.string.hello_world;
                    leftBoxingGlove.setImageResource(R.drawable.left_boxing_glove);
                    switch (myo.getArm()) {
                        case LEFT:
                            //restTextId = R.string.arm_left;
                            leftBoxingGlove.setImageResource(R.drawable.left_boxing_glove);
                            break;
                        case RIGHT:
                            //restTextId = R.string.arm_right;
                            leftBoxingGlove.setImageResource(R.drawable.left_boxing_glove);
                            break;
                    }
                    //mTextView.setText(getString(restTextId));
                    leftBoxingGlove.setImageResource(R.drawable.left_boxing_glove);
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }

        @Override
        public void onGyroscopeData (Myo myo, long timestamp, Vector3 gyro){
            double val = Math.abs(gyro.x() - lastGyro.x());

            if(val > 200 && timestamp > cooldownTimestamp){
                showToast("MY STYLE IS IMPETUOUS! MY DEFENSE IS IMPREGNABLE! & I`M JUST FEROCIOUS!");
                cooldownTimestamp = timestamp + 800;
                punchCrack.start();
                counter++;
                punch.setText(Integer.toString(counter));
            }
            lastGyro = gyro;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize widgets
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        leftBoxingGlove = (ImageView) findViewById(R.id.imageLeftGlove);
        punch = (EditText) findViewById(R.id.editPunch);

        // Initialize MediaPlayer
        punchCrack = MediaPlayer.create(this, R.raw.punch_crack);

        // Initialize timestamp and gyroscope
        cooldownTimestamp = 0;
        lastGyro = new Vector3();

        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up speech recognition system
        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                SUBSCRIPTION_KEY, AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        // Initialize AI service
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }

    public class CounterClass extends CountDownTimer {

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {

            textViewTime.setText("K-O!!!");
        }

        @Override
        public void onTick(long millisUntilFinished) {

            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            System.out.println(hms);
            textViewTime.setText(hms);
        }
    }

    public void listenButtonOnClick(final View view) {

        aiService.startListening();
    }

    public void onResult(final AIResponse response) {
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        // Play Mike Tyson mp3
        mikeTyson = MediaPlayer.create(this, R.raw.hit_em_mike);
        mikeTyson.start();

        // Activate 3 min timer
        final CounterClass timer = new CounterClass(180000,1000);
        timer.start();

        // Stop timer
        btnStop = (Button)findViewById(R.id.btnStop);
        textViewTime  = (TextView)findViewById(R.id.textViewTime);
        textViewTime.setText("00:03:00");

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                mikeTyson.stop();
                counter = 0;
                punch.setText(Integer.toString(counter));
            }
        });
    }

    @Override
    public void onError(final AIError error) {
        resultTextView.setText(error.toString());
    }

    @Override
    public void onListeningStarted() {}

    @Override
    public void onListeningCanceled() {}

    @Override
    public void onListeningFinished() {}

    @Override
    public void onAudioLevel(final float level) {}

    @Override
    protected void onResume(){
        super.onResume();
        Hub hub = Hub.getInstance();
        hub.addListener(mListener);
    }

    @Override
    protected void onPause(){
        super.onPause();
        Hub hub = Hub.getInstance();
        hub.removeListener(mListener);
    }

    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        } else {
            mToast.setText(text);
        }
        mToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Hub.getInstance().removeListener(mListener);
        if (isFinishing()) {
            Hub.getInstance().shutdown();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void onScanActionSelected() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }
}
