package com.loyola.robotics.knightwatch;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

@TargetApi(11)
public class MainActivity extends ActionBarActivity {

    // TODO: all these EXTRA names should go in the strings.xml file
    public final static String EXTRA_MATCH_NUM = "com.loyola.robotics.knightwatch.MATCH_NUM_MSG";
    public final static String EXTRA_ALLIANCE_COLOUR = "com.loyola.robotics.knightwatch.EXTRA_ALLIANCE_COLOUR";
    public final static String EXTRA_CHOOSE_FILES = "com.loyola.robotics.knightwatch.EXTRA_CHOOSE_FILES";
    public final static String EXTRA_AVE_CYCLE_TIMES = "com.loyola.robotics.knightwatch.EXTRA_AVE_CYCLE_TIMES";

    Intent globalIntent;


    static MainActivity me;

    File matchResultsFile;
    boolean matchResultsLoaded = false;
    File knightWatchDataFile;
    boolean knightWatchLoaded = false;

    /** <matchAllianceKey, {RobotEvent} **/
    Hashtable<String, Vector<RobotEvent>> matches;

    /** < str(teamNum), {matchAllianceKey}> **/
    Hashtable<String, Vector<String>> matchesPlayedByTeam;

    /** <matchAllianceKey, str(score)> **/
    Hashtable<String, String> matchScores;


    Button loadMatchButton;
    TextView loadMatchResultsTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.loadMatchButton = (Button) findViewById(R.id.loadMatchResultsBtn);
        this.loadMatchResultsTV = (TextView) findViewById(R.id.loadMatchResultsTV);

        if (loadMatchButton == null)
            Toast.makeText(this, "WTF!?!?!?!?!", Toast.LENGTH_SHORT).show();

        globalIntent = new Intent();

        Intent sender = getIntent();
        if( sender.hasExtra( getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE) ) ) {
            globalIntent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE), sender.getStringExtra( getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE ) ) );
            loadMatchResultsFile();
        }
        if ( sender.hasExtra( getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE) ) ) {
            globalIntent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE), sender.getStringExtra( getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE) ) );
            loadKnightWatchDataFile();
        }

        me = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFiles();
    }

    public void loadFiles() {

        // match results file
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE) ) ) {
            // force a re-load of the files always
//            if( !matchResultsLoaded ) {
                loadMatchResultsFile();
//            }
        }

        // KnightWatch file
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)) ) {
//            if( !knightWatchLoaded ) {
                loadKnightWatchDataFile();
//            }
        }

        // buttons
        if (globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)) && matchResultsLoaded ) {
            loadMatchButton.setText("Reload");
            File file = new File(globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));
            loadMatchResultsTV.setText("Loaded '"+ file.getName() + "'");
        }
        if (globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)) && knightWatchLoaded ) {
            ((Button) findViewById(R.id.loadKnightWatchDataBtn)).setText("Reload");
            File file = new File(globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));
            ((TextView) findViewById(R.id.loadKnightWatchDataTV)).setText("Loaded '"+
                    file.getName()+"'");

            findViewById(R.id.bestCyclesBtn).setEnabled(true);
        }

        // if both files are loaded, activate the Team Stats button
        if(matchResultsLoaded && knightWatchLoaded) {
            findViewById(R.id.lookupTeamBtn).setEnabled(true);
        }
    }

    public void lookupTeamBtn(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage("Which team do you want stats for?");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    int teamNo = Integer.parseInt(input.getText().toString());

                    // TODO: check that this is a team we have data for
                    if( matchesPlayedByTeam.get(""+teamNo) == null) {
                        Toast.makeText(me, "No data for team "+teamNo, Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // These are things that could be needed further on
                    String strTeam = ""+teamNo;
                    Vector<String> matchesPlayed = matchesPlayedByTeam.get(strTeam);
                    Vector<Vector<RobotEvent>> allRobotEvents = new Vector<Vector<RobotEvent>>();
                    int wins=0, losses=0, ties = 0;
                    for(String matchKey : matchesPlayed) {
                        Vector<RobotEvent> match = matches.get(matchKey);
                        if(match != null)
                            allRobotEvents.add( match );

                        int myScore = Integer.parseInt(matchScores.get(matchKey));
                        int theirScore = Integer.parseInt(matchScores.get(getOtherAllianceKey(matchKey)));

                        if(myScore > theirScore)
                            wins++;
                        else if (myScore < theirScore)
                            losses++;
                        else
                            ties++;

                    }

                    Vector<Vector<CycleAnalyzer.Cycle>> allCycles = CycleAnalyzer.extractAllCycles(allRobotEvents);

                    double[] ptsPerS = CycleAnalyzer.computePtsPerSecond(allCycles);
                    int[] counts = CycleAnalyzer.computeCounts(allCycles);
                    double[] times = CycleAnalyzer.computeAverageTimes(allCycles);



                    Intent intent = new Intent(me, TeamStatsPage.class);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TEAM_CYCLE_COUNTS), counts);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TEAM_NO), teamNo);
                    intent.putExtra(getResources().getString(R.string.EXTRA_PTS_PER_S), ptsPerS);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TIMES), times);
                    intent.putExtra(getResources().getString(R.string.EXTRA_WINS), wins);
                    intent.putExtra(getResources().getString(R.string.EXTRA_LOSSES), losses);
                    intent.putExtra(getResources().getString(R.string.EXTRA_TIES), ties);

                    startActivity(intent);

                } catch (Exception e) {
                    return;
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();


    }

    /** Called when the user clicks the ScoutRed button */
    public void scoutRed(View view) {

        EditText matchNumField = (EditText) findViewById(R.id.match_num_field);
        String matchNum = matchNumField.getText().toString();
        if(matchNum.equals("")) return;

        Intent intent = new Intent(this, AutoScouting.class);
        intent.putExtra(EXTRA_MATCH_NUM, matchNum);
        intent.putExtra(EXTRA_ALLIANCE_COLOUR, getString(R.string.red_alliance) );

        // copy the file names, if they are there
        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));

        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));

        startActivity(intent);
    }

    /** Called when the user clicks the ScoutBlue button */
    public void scoutBlue(View view) {

        EditText matchNumField = (EditText) findViewById(R.id.match_num_field);
        String matchNum = matchNumField.getText().toString();
        if(matchNum.equals("")) return;

        Intent intent = new Intent(this, AutoScouting.class);
        intent.putExtra(EXTRA_MATCH_NUM, matchNum);
        intent.putExtra(EXTRA_ALLIANCE_COLOUR, getString(R.string.blue_alliance) );

        // copy the file names, if they are there
        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)));

        if(globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)))
            intent.putExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE),
                    globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)));

        startActivity(intent);
    }


    public void loadMatchFileBtn(View view) {

        if( matchResultsLoaded ) {
            loadMatchResultsFile();
        } else {
            Intent intent = new Intent(this, FileChooser.class);
            intent.putExtras(globalIntent);
            intent.putExtra(FileChooser.EXTRA_TO_RETURN, getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE));
            intent.putExtra(EXTRA_CHOOSE_FILES, true);
            startActivity(intent);
        }
    }

    public void loadKnightWatchFileBtn(View view) {

        if ( globalIntent.hasExtra( getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE) ) ) {
            loadKnightWatchDataFile();
        } else {
            Intent intent = new Intent(this, FileChooser.class);
            intent.putExtras(globalIntent);
            intent.putExtra(FileChooser.EXTRA_TO_RETURN, getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE));
            intent.putExtra(EXTRA_CHOOSE_FILES, true);
            startActivity(intent);
        }
    }

    public void bestCyclesBtn(View view) {

        Vector<Vector<CycleAnalyzer.Cycle>> allCycles = new Vector<Vector<CycleAnalyzer.Cycle>>();
        for( String matchName : matches.keySet()) {
            allCycles.add(CycleAnalyzer.extractCycles(matches.get(matchName)));
        }

        double[] ptsPerS = CycleAnalyzer.computePtsPerSecond(allCycles);
        double[] times = CycleAnalyzer.computeAverageTimes(allCycles);

        Intent intent = new Intent(this, DispBestCycles.class);
        intent.putExtra(EXTRA_AVE_CYCLE_TIMES, ptsPerS);
        startActivity(intent);
    }

    private String getMatchAllianceKey(int matchNum, String alliance) {
        return matchNum + alliance;
    }

    private String getOtherAllianceKey( String matchKey ) {
        if( matchKey.contains("red") )
            return matchKey.replace("red", "blue");

        if( matchKey.contains("blue") )
            return matchKey.replace("blue", "red");

       return null;
    }


    private void loadMatchResultsFile() {
        String fileName;
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE)) ) {
            fileName = globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_MATCH_RESULTS_FILE));
        } else {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        matchesPlayedByTeam = new Hashtable<String, Vector<String>>();
        matchScores = new Hashtable<String, String>();
        try {
            String line;
            line = br.readLine();

            while (line != null) {

                String[] cells = line.split(",");

                try {
                    // ignore the first column cause it's the time of day of the match
                    int matchNum = Integer.parseInt( cells[1] );
                    int red1 = Integer.parseInt( cells[2] );
                    int red2 = Integer.parseInt( cells[3] );
                    int red3 = Integer.parseInt( cells[4] );
                    int blue1 = Integer.parseInt( cells[5] );
                    int blue2 = Integer.parseInt( cells[6] );
                    int blue3= Integer.parseInt( cells[7] );
                    int redScore = Integer.parseInt( cells[8] );
                    int blueScore = Integer.parseInt( cells[9] );

                    if (matchesPlayedByTeam.containsKey(red1+"")) {
                        matchesPlayedByTeam.get( red1+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+red1, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(red2+"")) {
                        matchesPlayedByTeam.get( red2+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+red2, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(red3+"")) {
                        matchesPlayedByTeam.get( red3+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+red3, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(blue1+"")) {
                        matchesPlayedByTeam.get( blue1+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+blue1, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(blue2+"")) {
                        matchesPlayedByTeam.get( blue2+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+blue2, vec);
                    }
                    if (matchesPlayedByTeam.containsKey(blue3+"")) {
                        matchesPlayedByTeam.get( blue3+"").add(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );

                    } else {
                        Vector<String> vec = new Vector<String>();
                        vec.add( getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE) );
                        matchesPlayedByTeam.put( ""+blue3, vec);
                    }

                    matchScores.put(getMatchAllianceKey(matchNum, RobotEvent.STR_RED_ALLIANCE), ""+redScore);
                    matchScores.put(getMatchAllianceKey(matchNum, RobotEvent.STR_BLUE_ALLIANCE), ""+blueScore);

                } catch( Exception e) {
                    Toast.makeText(this, "Error: Match Results file may be corrupted.", Toast.LENGTH_SHORT).show();
                    return;
                }
                line = br.readLine();
            }

            br.close();
        } catch(IOException e) {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        matchResultsLoaded = true;
    }

    private void loadKnightWatchDataFile() {

        String fileName;
        if( globalIntent.hasExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE)) ) {
            fileName = globalIntent.getStringExtra(getResources().getString(R.string.EXTRA_KNIGHT_WATCH_DATA_FILE));
        } else {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Error loading Match Results file.", Toast.LENGTH_SHORT).show();
            return;
        }

        matches = new Hashtable<String, Vector<RobotEvent>>();
        try {
            String line = null;
            line = br.readLine();

            while (line != null) {

                String[] cells = line.split(",");
                int matchNum = Integer.parseInt(cells[0].replace(RobotEvent.STR_MATCHNUM, ""));
                String alliance = cells[1].replace(RobotEvent.STR_ALLIANCE, "");

                Vector<RobotEvent> events = new Vector<RobotEvent>();

                // -1 cause the last one will be a comma with nothing after it
                for(int i=2; i<cells.length-1; i++) {
                    String eventType = cells[i].substring(cells[i].indexOf('{') + 1, cells[i].indexOf(';'));

                    if( eventType.equals( RobotEvent.STR_LG_SCORE) || eventType.equals( RobotEvent.STR_HG_SCORE) ) {
                        int minutes = Integer.parseInt(cells[i].substring(cells[i].indexOf(";") + 1, cells[i].indexOf(":")));
                        int seconds = Integer.parseInt(cells[i].substring(cells[i].indexOf(":") + 1, cells[i].indexOf("|") ));
                        String strAssis = cells[i].substring(cells[i].indexOf("|") + 1, cells[i].indexOf("}"));
                        int assists = Integer.parseInt(strAssis.replace(RobotEvent.STR_NUM_ASSIS, ""));
                         events.add( new RobotEvent(eventType, minutes, seconds, assists));

                    } else {
                        int minutes = Integer.parseInt(cells[i].substring(cells[i].indexOf(";") + 1, cells[i].indexOf(":")));
                        int seconds = Integer.parseInt(cells[i].substring(cells[i].indexOf(":") + 1, cells[i].indexOf("}")));
                        events.add( new RobotEvent(eventType, minutes, seconds));
                    }

                }

                matches.put(getMatchAllianceKey(matchNum, alliance), events);

                line = br.readLine();
            }

            br.close();
        } catch(Exception e) {
            Toast.makeText(this, "Error loading KnightWatch file.", Toast.LENGTH_SHORT).show();
            return;
        }

        knightWatchLoaded = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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


//    private static class BestCyclesFrag extends Fragment {
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            // Inflate the layout for this fragment
//            return inflater.inflate(R.layout.fragment_best_cycles, container, false);
//        }
//    }
}
