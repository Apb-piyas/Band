package lrandomdev.com.online.mp3player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.LineChartView;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.sdsmdg.harjot.crollerTest.Croller;

import java.util.ArrayList;
import java.util.HashMap;

import lrandomdev.com.online.mp3player.services.ServicePlayer;


/**
 * Created by Lrandom on 4/12/18.
 */

public class ActivityEqualizer extends ActivityParent{
    ServicePlayer audioPlayerService;
    Spinner spnPreset;
    ArrayList<HashMap<Integer,String>> presets=new ArrayList<HashMap<Integer, String>>();
    VerticalSeekBar[] seekBarFinal;
    LineSet dataset;
    LineChartView lineChartView;
    Paint paint;
    float[] points;
    int y = 0;
    BassBoost bassBoost;
    PresetReverb presetReverb;
    TextView tvFreq;
    Croller bassBoostCtrl,presetReverbCtrl;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this,
                ServicePlayer.class);
        getApplicationContext().bindService(intent, serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.equalizer));

        spnPreset=(Spinner)findViewById(R.id.spnPreset);
        lineChartView = (LineChartView)findViewById(R.id.lineChart);
        paint=new Paint();
        dataset=new LineSet();
        bassBoostCtrl=(Croller)findViewById(R.id.gaugeBass);
        presetReverbCtrl=(Croller)findViewById(R.id.gauge3D);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {


        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            audioPlayerService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            audioPlayerService = ((ServicePlayer.PlayerBinder) service)
                    .getService();

            final Equalizer equalizer = new Equalizer(0,audioPlayerService.getAudioSessionId());
            equalizer.setEnabled(true);

            final short lowerEqualizerBandLevel= equalizer.getBandLevelRange()[0];
            final short upperEqualizerBandLevel=equalizer.getBandLevelRange()[1];


            final int numberOfBands=equalizer.getNumberOfBands();
            final int numberOfPresets=equalizer.getNumberOfPresets();
            ArrayList<String> names = new ArrayList<String>();

            seekBarFinal= new VerticalSeekBar[numberOfBands];
            points = new float[numberOfBands];

            for (int i =0 ; i < numberOfPresets; i ++){
                HashMap hash =new HashMap();
                hash.put(i,equalizer.getPresetName((short)i));
                presets.add(hash);
                names.add(equalizer.getPresetName((short)i));
            }
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<String>(ActivityEqualizer.this,
                            android.R.layout.simple_list_item_1,names);
            spnPreset.setAdapter(adapter);

            for ( short i = 0 ; i < numberOfBands;i++){
                final short equalizerBandIndex=i;
                VerticalSeekBar seekBar = new VerticalSeekBar(ActivityEqualizer.this);
                TextView textView = new TextView(ActivityEqualizer.this);
                switch (i){
                    case 0:
                        seekBar=(VerticalSeekBar) findViewById(R.id.equalizer1);
                        tvFreq=(TextView)findViewById(R.id.tv1);
                        break;

                    case 1:
                        seekBar=(VerticalSeekBar) findViewById(R.id.equalizer2);
                        tvFreq=(TextView)findViewById(R.id.tv2);
                        break;

                    case 2:
                        seekBar=(VerticalSeekBar) findViewById(R.id.equalizer3);
                        tvFreq=(TextView)findViewById(R.id.tv3);
                        break;

                    case 3:
                        seekBar=(VerticalSeekBar)findViewById(R.id.equalizer4);
                        tvFreq=(TextView)findViewById(R.id.tv4);
                        break;

                    case 4:
                        seekBar=(VerticalSeekBar)findViewById(R.id.equalizer5);
                        tvFreq=(TextView)findViewById(R.id.tv5);
                        break;
                }



                seekBar.setId(i);
                seekBar.setMax(upperEqualizerBandLevel-lowerEqualizerBandLevel);
                seekBarFinal[i]=seekBar;
                seekBar.setProgress(equalizer.getBandLevel(equalizerBandIndex)-lowerEqualizerBandLevel);

                points[i]=equalizer.getBandLevel(equalizerBandIndex)-lowerEqualizerBandLevel;
                dataset.addPoint((equalizer.getCenterFreq(equalizerBandIndex)/1000)+" Hz",points[i]);
                tvFreq.setText(equalizer.getCenterFreq(equalizerBandIndex)/1000+" Hz");

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        equalizer.setBandLevel(equalizerBandIndex,(short)(progress+lowerEqualizerBandLevel));
                        points[seekBar.getId()]=equalizer.getBandLevel(equalizerBandIndex)-lowerEqualizerBandLevel;
                        dataset.updateValues(points);
                        lineChartView.notifyDataUpdate();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }//end for

            dataset.setSmooth(true);
            dataset.setThickness(5);
            lineChartView.setXAxis(false);
            lineChartView.setYAxis(false);

            lineChartView.setYLabels(AxisRenderer.LabelPosition.NONE);
            lineChartView.setXLabels(AxisRenderer.LabelPosition.NONE);
            //lineChartView.setGrid(ChartView.GridType.NONE, 7, 10, paint);

            lineChartView.setAxisBorderValues(-300, 3300);

            lineChartView.addData(dataset);
            lineChartView.show();

            spnPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0) {
                        equalizer.usePreset((short) (position-1));
                        for (short i = 0; i < numberOfBands; i++) {
                            seekBarFinal[i].setProgress(equalizer.getBandLevel(i) - lowerEqualizerBandLevel);
                            points[i] = equalizer.getBandLevel(i) - lowerEqualizerBandLevel;
                        }
                        dataset.updateValues(points);
                        lineChartView.notifyDataUpdate();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            try{
                bassBoost=new BassBoost(0,audioPlayerService.getAudioSessionId());
                bassBoost.setEnabled(false);
                if (bassBoost.getStrengthSupported())
                {
                    short word1 = bassBoost.getRoundedStrength();
                    bassBoost.setStrength(word1);
                    Log.e("FAPF","FAP");
                }

               // BassBoost.Settings bassboostSettingTemp=bassBoost.getProperties();
               // bassboostSettingTemp.strength=(1000/19);
               // bassBoost.setProperties(bassboostSettingTemp);
               // audioPlayerService.setAuxEffectSendLevel(1.0f);

                presetReverb=new PresetReverb(0,audioPlayerService.getAudioSessionId());
                presetReverb.setPreset(PresetReverb.PRESET_NONE);
                presetReverb.setEnabled(false);
                audioPlayerService.setAuxEffectSendLevel(1.0f);

//                int x = 0;
//                if (bassBoost != null) {
//                    try {
//                        x = ((bassBoost.getRoundedStrength() * 19) / 1000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (presetReverb != null) {
//                    try {
//                        y = (presetReverb.getPreset() * 19) / 6;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (x == 0) {
//                    bassBoostCtrl.setProgress(1);
//                } else {
//                    bassBoostCtrl.setProgress(x);
//                }
//
//                if (y == 0) {
//                    presetReverbCtrl.setProgress(1);
//                } else {
//                    presetReverbCtrl.setProgress(y);
//                }
            }catch (Exception e){
                e.printStackTrace();
        }

            bassBoostCtrl.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                @Override
                public void onProgressChanged(int progress) {
                    Log.e("FAP","FAP");
                    //short bassStrength = (short) (((float) 1000 / 19) * (progress));
                    try {
                        bassBoost.setStrength((short) progress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            presetReverbCtrl.setOnProgressChangedListener(new Croller.onProgressChangedListener() {
                @Override
                public void onProgressChanged(int progress) {
                    short reverbPreset = (short) ((progress * 6) / 19);
                    try {
                        presetReverb.setPreset(reverbPreset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    y = progress;
                }
            });

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
