package com.loyola.robotics.knightwatch;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;


public class TeleopScouting extends ActionBarActivity {

    public final int AUTO_MODE = -1;
    public final int NEW_CYCLE = 0;
    public final int LOOSE = 1;
    public final int POSSESSED = 2;
    public final int TRUSS_SHOT = 3;

    private boolean trussCompleted = false;
    private boolean ballIsLoose = true;
    private String allianceColour;
    private String matchNum;

    private TextView timerTextView;
    private long startTime = 0;

    private Intent srcIntent;

    private Vector<RobotEvent> events = new Vector<RobotEvent>();

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_teleop_scouting);

        Intent intent = getIntent();
        srcIntent = intent;
        allianceColour = intent.getStringExtra(MainActivity.EXTRA_ALLIANCE_COLOUR);
        matchNum = intent.getStringExtra(MainActivity.EXTRA_MATCH_NUM);

        TextView allianceBanner = (TextView) findViewById(R.id.allianceColourTV);

        if( allianceColour.equals( getString(R.string.red_alliance) ) ) {
            allianceBanner.setText("Red Alliance");
            allianceBanner.setBackgroundResource(R.color.red_alliance);
            ((TextView) findViewById(R.id.clock)).setBackgroundResource(R.color.red_alliance);

        } else if ( allianceColour.equals( getString(R.string.blue_alliance) ) ) {
            allianceBanner.setText("Blue Alliance");
            allianceBanner.setBackgroundResource(R.color.blue_alliance);
            ((TextView) findViewById(R.id.clock)).setBackgroundResource(R.color.blue_alliance);
        } else {
            allianceBanner.setText("!Error!");
        }

        startTime = System.currentTimeMillis();

        // Display a timer
        timerTextView = (TextView) findViewById(R.id.clock);

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);


        setState(AUTO_MODE);

    }


    public void setState(int state) {
        switch (state) {
            case AUTO_MODE:
                ((Button) findViewById(R.id.allAutoBallsBtn)).setEnabled(true);
                ((Button) findViewById(R.id.pickupBtn)).setEnabled(false);
                ((Button) findViewById(R.id.passbtn)).setEnabled(false);
                ((Button) findViewById(R.id.droppedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.trussBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchSameBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchNewBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchFailedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.LGScoreBtn)).setEnabled(false);
                ((Button) findViewById(R.id.LGFailedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.HGScoreBtn)).setEnabled(false);
                ((Button) findViewById(R.id.HGFailedBtn)).setEnabled(false);
                break;
            case NEW_CYCLE:
                ballIsLoose = true;
                trussCompleted = false;
                ((Button) findViewById(R.id.allAutoBallsBtn)).setEnabled(false);
                ((Button) findViewById(R.id.pickupBtn)).setEnabled(true);
                ((Button) findViewById(R.id.passbtn)).setEnabled(false);
                ((Button) findViewById(R.id.droppedBtn)).setEnabled(true);
                ((Button) findViewById(R.id.trussBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchSameBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchNewBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchFailedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.LGScoreBtn)).setEnabled(false);
                ((Button) findViewById(R.id.LGFailedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.HGScoreBtn)).setEnabled(false);
                ((Button) findViewById(R.id.HGFailedBtn)).setEnabled(false);
                break;
            case LOOSE:
                ballIsLoose = true;
                ((Button) findViewById(R.id.allAutoBallsBtn)).setEnabled(false);
                ((Button) findViewById(R.id.pickupBtn)).setEnabled(true);
                ((Button) findViewById(R.id.passbtn)).setEnabled(false);
                ((Button) findViewById(R.id.droppedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.trussBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchSameBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchNewBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchFailedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.LGScoreBtn)).setEnabled(true);
                ((Button) findViewById(R.id.LGFailedBtn)).setEnabled(true);
                ((Button) findViewById(R.id.HGScoreBtn)).setEnabled(false);
                ((Button) findViewById(R.id.HGFailedBtn)).setEnabled(false);
                break;
            case POSSESSED:
                ballIsLoose = false;
                ((Button) findViewById(R.id.allAutoBallsBtn)).setEnabled(false);
                ((Button) findViewById(R.id.pickupBtn)).setEnabled(false);
                ((Button) findViewById(R.id.passbtn)).setEnabled(true);
                ((Button) findViewById(R.id.droppedBtn)).setEnabled(true);
                if (! trussCompleted ) {
                    ((Button) findViewById(R.id.trussBtn)).setEnabled(true);
                }
                ((Button) findViewById(R.id.catchSameBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchNewBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchFailedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.LGScoreBtn)).setEnabled(true);
                ((Button) findViewById(R.id.LGFailedBtn)).setEnabled(true);
                ((Button) findViewById(R.id.HGScoreBtn)).setEnabled(true);
                ((Button) findViewById(R.id.HGFailedBtn)).setEnabled(true);
                break;
            case TRUSS_SHOT:
                ballIsLoose = true;
                trussCompleted = true;
                ((Button) findViewById(R.id.allAutoBallsBtn)).setEnabled(false);
                ((Button) findViewById(R.id.pickupBtn)).setEnabled(false);
                ((Button) findViewById(R.id.passbtn)).setEnabled(false);
                ((Button) findViewById(R.id.droppedBtn)).setEnabled(true);
                ((Button) findViewById(R.id.trussBtn)).setEnabled(false);
                ((Button) findViewById(R.id.catchSameBtn)).setEnabled(true);
                ((Button) findViewById(R.id.catchNewBtn)).setEnabled(true);
                ((Button) findViewById(R.id.catchFailedBtn)).setEnabled(true);
                ((Button) findViewById(R.id.LGScoreBtn)).setEnabled(false);
                ((Button) findViewById(R.id.LGFailedBtn)).setEnabled(false);
                ((Button) findViewById(R.id.HGScoreBtn)).setEnabled(false);
                ((Button) findViewById(R.id.HGFailedBtn)).setEnabled(false);
                break;
        }

       setPossessedLbl(state);
    }

    public void setPossessedLbl(int state) {
        TextView lbl = (TextView) findViewById(R.id.possessionLBL);

        switch(state) {
            case AUTO_MODE:
                lbl.setText("Clear all auto mode balls.");
                lbl.setBackgroundResource(R.color.neutral_alliance);
                break;
            case NEW_CYCLE:
                lbl.setText("New Cycle!");
                lbl.setBackgroundResource(R.color.neutral_alliance);
                break;
            case LOOSE:
                lbl.setText("Ball is Loose!");
                lbl.setBackgroundResource(R.color.neutral_alliance);
                break;
            case POSSESSED:
                lbl.setText("Ball is Possessed!");
                if (allianceColour.equals( getString(R.string.red_alliance))) {
                    lbl.setBackgroundResource(R.color.red_alliance);
                } else {
                    lbl.setBackgroundResource(R.color.blue_alliance);

                }
                break;
            case TRUSS_SHOT:
                lbl.setText("Will they catch it?");
                lbl.setBackgroundResource(R.color.neutral_alliance);
                break;
        }
    }

    public void autoBallsCleared(View view) {
        events.add( new RobotEvent(RobotEvent.STR_AUTO_BALLS_CLEARED, System.currentTimeMillis() - startTime));

        setState(NEW_CYCLE);
    }

    public void pickup(View view) {
        events.add( new RobotEvent(RobotEvent.STR_PICKUP, System.currentTimeMillis() - startTime));

        setState(POSSESSED);
    }

    public void pass(View view) {
        events.add( new RobotEvent(RobotEvent.STR_PASS, System.currentTimeMillis() - startTime));
        setState(LOOSE);
    }

    public void dropped(View view) {
        events.add( new RobotEvent(RobotEvent.STR_DROPPED, System.currentTimeMillis() - startTime));

        setState(LOOSE);
    }

    public void truss(View view) {
        events.add( new RobotEvent(RobotEvent.STR_TRUSS_THROW, System.currentTimeMillis() - startTime));

        setState(TRUSS_SHOT);
    }

    public void catch_same(View view) {
        events.add( new RobotEvent(RobotEvent.STR_CATCH_BY_SAME_BOT, System.currentTimeMillis() - startTime));

        setState(POSSESSED);
    }

    public void catch_new(View view) {
        events.add( new RobotEvent(RobotEvent.STR_CATCH_BY_NEW_BOT, System.currentTimeMillis() - startTime));

        setState(POSSESSED);
    }

    public void catch_failed(View view) {
        events.add( new RobotEvent(RobotEvent.STR_CATCH_FAILED, System.currentTimeMillis() - startTime));

        setState(LOOSE);
    }

    public void LG_score(View view) {
        events.add( new RobotEvent(RobotEvent.STR_LG_SCORE, System.currentTimeMillis() - startTime));

        setState(NEW_CYCLE);
    }

    public void LG_missed(View view) {
        events.add( new RobotEvent(RobotEvent.STR_LG_MISS, System.currentTimeMillis() - startTime));

        setState(LOOSE);
    }

    public void HG_score(View view) {
        events.add( new RobotEvent(RobotEvent.STR_HG_SCORE, System.currentTimeMillis() - startTime));

        setState(NEW_CYCLE);
    }

    public void HG_missed(View view) {
        events.add( new RobotEvent(RobotEvent.STR_HG_MISS, System.currentTimeMillis() - startTime));

        setState(LOOSE);
    }

    public void saveFile(View view) {
        // figure out how to write a file


        File outfile = new File(getResources().getString(R.string.default_knightWatchData_location));
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(outfile, true));
            bw.append(RobotEvent.STR_MATCHNUM+matchNum + ",");
            if( allianceColour.equals( getString(R.string.red_alliance) ) ) {
                bw.append(RobotEvent.STR_ALLIANCE+RobotEvent.STR_RED_ALLIANCE+", ");
            } else {
                bw.append(RobotEvent.STR_ALLIANCE+RobotEvent.STR_BLUE_ALLIANCE+", ");
            }
            for (Iterator<RobotEvent> robEvIter = events.iterator(); robEvIter.hasNext();)
                bw.append( robEvIter.next().toString() );
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {

        }

        Toast.makeText(getBaseContext(),"Data written '"+outfile.getName()+"'", Toast.LENGTH_SHORT).show();


        // call Main
        Intent intent = new Intent(this, MainActivity.class);

        // copy the file names, if they are there
        if(srcIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE),
                    srcIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));
        else
            intent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE),
                    getResources().getString(R.string.default_knightWatchData_location));

        if(srcIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE),
                    srcIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));


        startActivity(intent);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.teleop_scouting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_teleop_scouting, container, false);
            return rootView;
        }
    }

}
