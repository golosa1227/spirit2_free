
    // GUI

package fm.a2d.sf;

import java.util.Locale;

import android.app.Activity;
import android.widget.Toast;
    
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.os.Bundle;
import android.net.Uri;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

public class gui_gui implements gui_gap {

  private static int    m_obinits = 1;

  private Activity      m_gui_act   = null;
  private Context       m_context   = null;
  private com_api       m_com_api   = null;



    // User Interface:
  private Animation     m_ani_button= null;

  // Text:
  private TextView      m_tv_rssi   = null;
  private TextView      m_tv_svc_count  = null;
  private TextView      m_tv_svc_phase  = null;
  private TextView      m_tv_svc_cdown  = null;
  private TextView      m_tv_pilot  = null;
  private TextView      m_tv_band   = null;
  private TextView      m_tv_freq   = null;

  // RDS data:
  private TextView      m_tv_picl   = null;
  private TextView      m_tv_ps     = null;
  private TextView      m_tv_ptyn   = null;
  private TextView      m_tv_rt     = null;

    // ImageView Buttons:
  private ImageView     m_iv_seekup = null;
  private ImageView     m_iv_seekdn = null;

  private ImageView     m_iv_prev   = null;
  private ImageView     m_iv_next   = null;

  private ImageView     m_iv_paupla = null;
  private ImageView     m_iv_stop   = null;
  private ImageView     m_iv_pause  = null;
  private ImageView     m_iv_output = null;                             // ImageView for Speaker/Headset toggle
  private ImageView     m_iv_volume = null;
  private ImageView     m_iv_record = null;
  private ImageView     m_iv_menu   = null;
  private ImageView     m_iv_pwr    = null;

    // Radio Group/Buttons:
  private RadioGroup    m_rg_band   = null;;
  private RadioButton   rb_band_us  = null;
  private RadioButton   rb_band_eu  = null;

    // Checkboxes:
  private CheckBox      cb_speaker  = null;


    // Presets: 16 = com_api.max_presets

  private int           m_presets_curr  = 0;
  private ImageButton[] m_preset_ib     = {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};   // 16 Preset Image Buttons
  private TextView   [] m_preset_tv     = {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};   // 16 Preset Text Views
  private String     [] m_preset_freq   = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};   //  Frequencies
  private String     [] m_preset_name   = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};   //  Names


  private int           pixel_width     = 480;
  private int           pixel_height    = 800;
  private float         pixel_density   = 1.5f;


    // Dial:
  private android.os.Handler delay_dial_handler  = null;
  private Runnable           delay_dial_runnable = null;

  private gui_dia       m_dial          = null;
  private long          last_rotate_time= 0;

  private double        freq_at_210 = 85200;
  private double        freq_percent_factor = 251.5;

  private int           last_dial_freq = -1;

    // Color:
  private int           lite_clr = Color.WHITE;
  private int           dark_clr = Color.GRAY;
  private int           blue_clr = Color.BLUE;


  private Dialog        daemon_start_dialog  = null;
  private Dialog        daemon_dialog = null;

  private String        last_rt = "";
  private int           last_audio_sessid_int = 0;


    // Code:

  public gui_gui (Context c, com_api the_com_api) {                     // Constructor
    com_uti.logd ("m_obinits: " + m_obinits++);

    m_context = c;
    m_gui_act = (Activity) c;
    m_com_api = the_com_api;
  }

    // Lifecycle API

  public boolean gap_state_set (String state) {
    boolean ret = false;
    if (state.equalsIgnoreCase ("start"))
      ret = gui_start ();
    else if (state.equalsIgnoreCase ("stop"))
      ret = gui_stop ();
    return (ret);
  }
  private boolean gui_stop () {
    //bcast_lstnr_stop ();
    gui_init = false;
    return (true);
  }

  private boolean gui_init = false;

  private boolean gui_start () {

    gui_init = false;

    com_uti.strict_mode_set (false);                                      // !! Hack for s2d comms to allow network activity on UI thread

    DisplayMetrics dm = new DisplayMetrics ();
    m_gui_act.getWindowManager ().getDefaultDisplay ().getMetrics (dm);
    pixel_width  = dm.widthPixels;
    pixel_height = dm.heightPixels;
    pixel_density = m_context.getResources ().getDisplayMetrics ().density;
    com_uti.logd ("pixel_width: " + pixel_width + "  pixel_height: " + pixel_height + "  pixel_density: " + pixel_density);


    m_gui_act.requestWindowFeature (Window.FEATURE_NO_TITLE);            // No title to save screen space
    m_gui_act.setContentView (R.layout.gui_gui_layout);                   // Main Layout


/*  Programmatic harder than XML (?? Should do before setContentView !! ??

//    LinearLayout main_linear_layout =  (LinearLayout) m_gui_act.findViewById (R.id.main_hll);
    LinearLayout                            gui_pg1_layout          =  (LinearLayout) m_gui_act.findViewById (R.id.gui_pg1_layout);
    //FrameLayout                             new_frame_layout        = (FrameLayout) m_gui_act.findViewById (R.id.new_fl);//new FrameLayout (m_context);
    FrameLayout                             new_frame_layout        = new FrameLayout (m_context);
    FrameLayout.LayoutParams new_frame_layout_params = new android.widget.FrameLayout.LayoutParams ((int) (pixel_width / pixel_density), ViewGroup.LayoutParams.MATCH_PARENT);
    //com_uti.loge ("gui_pg1_layout: " + gui_pg1_layout + "  new_frame_layout_params: " + new_frame_layout_params);
    new_frame_layout.addView (gui_pg1_layout, new_frame_layout_params);
//  To:     new_frame_layout    FrameLayout  View       with new_frame_layout_params FrameLayout.LayoutParams
//  add:    gui_pg1_layout   LinearLayout View

    LinearLayout gui_pg2_layout =  (LinearLayout) m_gui_act.findViewById (R.id.gui_pg2_layout);
    FrameLayout                 old_frame_layout = (FrameLayout) m_gui_act.findViewById (R.id.old_fl);//new FrameLayout (m_context);
    FrameLayout.LayoutParams    old_frame_layout_params = new android.widget.FrameLayout.LayoutParams ((int) (pixel_width / pixel_density), ViewGroup.LayoutParams.MATCH_PARENT);
    old_frame_layout.addView (gui_pg2_layout, old_frame_layout_params);

//    main_linear_layout.addView (gui_pg1_layout);
//    main_linear_layout.addView (gui_pg2_layout);
*/

    LinearLayout.LayoutParams frame_layout_params = new android.widget.LinearLayout.LayoutParams ((int) (pixel_width), ViewGroup.LayoutParams.MATCH_PARENT);

    FrameLayout new_fl_view = (FrameLayout) m_gui_act.findViewById (R.id.new_fl);
    new_fl_view.setLayoutParams (frame_layout_params);

    FrameLayout old_fl_view = (FrameLayout) m_gui_act.findViewById (R.id.old_fl);
    old_fl_view.setLayoutParams (frame_layout_params);

    dial_init ();                                                       // Initialize frequency disl

                                                                        // Set button animation
    m_ani_button = AnimationUtils.loadAnimation (m_context, R.anim.ani_button);

    m_tv_band = (TextView) m_gui_act.findViewById (R.id.tv_band);
    m_tv_pilot = (TextView)  m_gui_act.findViewById (R.id.tv_pilot);
    m_tv_rssi = (TextView)  m_gui_act.findViewById (R.id.tv_rssi);

    m_tv_svc_count = (TextView)  m_gui_act.findViewById (R.id.tv_svc_count);
    m_tv_svc_phase = (TextView)  m_gui_act.findViewById (R.id.tv_svc_phase);
    m_tv_svc_cdown = (TextView)  m_gui_act.findViewById (R.id.tv_svc_cdown);

    m_tv_picl = (TextView) m_gui_act.findViewById (R.id.tv_picl);
    m_tv_ps   = (TextView) m_gui_act.findViewById (R.id.tv_ps);
    m_tv_ptyn = (TextView) m_gui_act.findViewById (R.id.tv_ptyn);
    m_tv_rt   = (TextView) m_gui_act.findViewById (R.id.tv_rt);

    m_iv_seekdn = (ImageView) m_gui_act.findViewById (R.id.iv_seekdn);
    m_iv_seekdn.setOnClickListener (short_click_lstnr);

    m_iv_seekup = (ImageView) m_gui_act.findViewById (R.id.iv_seekup);
    m_iv_seekup.setOnClickListener (short_click_lstnr);

    m_iv_prev = (ImageView) m_gui_act.findViewById (R.id.iv_prev);
    m_iv_prev.setOnClickListener (short_click_lstnr);
    m_iv_prev.setId (R.id.iv_prev);

    m_iv_next = (ImageView) m_gui_act.findViewById (R.id.iv_next);
    m_iv_next.setOnClickListener (short_click_lstnr);
    m_iv_next.setId (R.id.iv_next);

    m_tv_freq = (TextView) m_gui_act.findViewById (R.id.tv_freq);
    m_tv_freq.setOnClickListener (short_click_lstnr);

    m_iv_paupla = (ImageView) m_gui_act.findViewById (R.id.iv_paupla);
    m_iv_paupla.setOnClickListener (short_click_lstnr);
    m_iv_paupla.setId (R.id.iv_paupla);

    m_iv_stop = (ImageView) m_gui_act.findViewById (R.id.iv_stop);
    m_iv_stop.setOnClickListener (short_click_lstnr);
    m_iv_stop.setId (R.id.iv_stop);

    m_iv_pause = (ImageView) m_gui_act.findViewById (R.id.iv_pause);
    m_iv_pause.setOnClickListener (short_click_lstnr);
    m_iv_pause.setId (R.id.iv_pause);

    m_iv_output = (ImageView) m_gui_act.findViewById (R.id.iv_output);
    m_iv_output.setOnClickListener (short_click_lstnr);
    m_iv_output.setId (R.id.iv_output);

    m_iv_volume = (ImageView) m_gui_act.findViewById (R.id.iv_volume);
    m_iv_volume.setOnClickListener (short_click_lstnr);
    m_iv_volume.setId (R.id.iv_volume);

    m_iv_record = (ImageView) m_gui_act.findViewById (R.id.iv_record);
    m_iv_record.setOnClickListener (short_click_lstnr);
    m_iv_record.setId (R.id.iv_record);

    m_iv_menu = (ImageView) m_gui_act.findViewById (R.id.iv_menu);
    m_iv_menu.setOnClickListener (short_click_lstnr);
    m_iv_menu.setId (R.id.iv_menu);

    rb_band_us = (RadioButton) m_gui_act.findViewById (R.id.rb_band_us);
    rb_band_eu = (RadioButton) m_gui_act.findViewById (R.id.rb_band_eu);

    cb_speaker = (CheckBox) m_gui_act.findViewById (R.id.cb_speaker);

    try {
      lite_clr = Color.parseColor ("#ffffffff");                        // lite like PS
      dark_clr = Color.parseColor ("#ffa3a3a3");                        // grey like RT
      blue_clr = Color.parseColor ("#ff32b5e5");                        // ICS Blue
    }
    catch (Throwable e) {
      e.printStackTrace ();
    };
    m_tv_rt.setTextColor (lite_clr);
    m_tv_ps.setTextColor (lite_clr);

    presets_setup (lite_clr);

    gui_pwr_update (false);

    long curr_time = com_uti.utc_ms_get ();
    long gui_start_first = com_uti.long_get (com_uti.prefs_get (m_context, "gui_start_first", ""));
    if (gui_start_first <= 0L) {
      gui_start_first = curr_time;
      com_uti.prefs_set (m_context, "gui_start_first", "" + curr_time);
    }

        // Set preferences defaults:
    com_uti.def_set (m_context, "tuner_band");
    com_uti.def_set (m_context, "tuner_freq");
    com_uti.def_set (m_context, "tuner_stereo");
    com_uti.def_set (m_context, "tuner_rds_state");
    com_uti.def_set (m_context, "tuner_rds_af_state");

    com_uti.def_set (m_context, "audio_output");
    com_uti.def_set (m_context, "audio_stereo");
    com_uti.def_set (m_context, "audio_record_directory");
    com_uti.def_set (m_context, "audio_record_filename");

    com_uti.def_set (m_context, "audio_digital_amp");
    com_uti.def_set (m_context, "audio_samplerate");
    com_uti.def_set (m_context, "audio_buffersize");
    com_uti.def_set (m_context, "audio_mode");
    com_uti.def_set (m_context, "audio_pseudo_source");

    com_uti.def_set (m_context, "gui_display_name");
    com_uti.def_set (m_context, "gui_display_type");

    com_uti.def_set (m_context, "service_action_media_pauseplay");
    com_uti.def_set (m_context, "service_action_media_previous");
    com_uti.def_set (m_context, "service_action_media_next");

    com_uti.def_set (m_context, "service_update_notification");
    com_uti.def_set (m_context, "service_notifremote_title");
    com_uti.def_set (m_context, "service_notifremote_text");

    com_uti.def_set (m_context, "service_update_gui");

    com_uti.def_set (m_context, "service_update_remote");

    com_uti.def_set (m_context, "service_plugin_device");
    com_uti.def_set (m_context, "service_plugin_tuner");
    com_uti.def_set (m_context, "service_plugin_audio");

    com_uti.def_set (m_context, "debug_debug");

    int gui_start_count = com_uti.prefs_get (m_context, "gui_start_count", 0);
    gui_start_count ++;
    com_uti.prefs_set (m_context, "gui_start_count", gui_start_count);

    m_rg_band = (RadioGroup) m_gui_act.findViewById (R.id.rg_band);

        // !! tuner_band_set() is now the first thing that starts svc_svc, if not already started

    if (gui_start_count <= 1) {                                         // If first 1 runs...
      String cc = com_uti.country_get (m_context).toUpperCase ();
      if (cc.equals ("US") || cc.equals ("CA") || cc.equals ("MX")) {   // If USA, Canada or Mexico
        com_uti.logd ("Setting band US");
        tuner_band_set ("US");                                          // Band = US
      }
      else {
        com_uti.logd ("Setting band EU");
        tuner_band_set ("EU");                                          // Else Band = EU
      }
    }

    if (! com_uti.quiet_file_get ("/dev/s2d_running"))                // If daemon not running
      m_gui_act.showDialog (DAEMON_START_DIALOG);                     // Show the Start dialog

    String band = com_uti.prefs_get (m_context, "tuner_band", "EU");
    tuner_band_set (band);
    if (m_com_api.tuner_band.equalsIgnoreCase ("US")) {
      rb_band_eu.setChecked (false);
      rb_band_us.setChecked (true);
    }

    load_prefs ();

    if (! m_com_api.tuner_state.equalsIgnoreCase ("Start"))             // If tuner not started...
      m_com_api.key_set ("audio_state", "start");                       // Start audio service (which starts tuner (and daemon) first, if not already started)

    gui_init = true;

    return (true);
  }


  private void dial_init () {

        // Dial Relative Layout:
    RelativeLayout freq_dial_relative_layout = (RelativeLayout) m_gui_act.findViewById (R.id.freq_dial);
    android.widget.RelativeLayout.LayoutParams lp_dial = new android.widget.RelativeLayout.LayoutParams (android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);   // WRAP_CONTENT
    //int dial_size = (pixel_width * 3) / 4;
    int dial_size = (pixel_width * 7) / 8;
    m_dial = new gui_dia (m_context, R.drawable.freq_dial_needle, -1, dial_size, dial_size); // Get dial instance/RelativeLayout view
    lp_dial.addRule (RelativeLayout.CENTER_IN_PARENT);
    freq_dial_relative_layout.addView (m_dial, lp_dial);

        // Dial internal Power Relative Layout:
    android.widget.RelativeLayout.LayoutParams lp_power;
    lp_power = new android.widget.RelativeLayout.LayoutParams (android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT, android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
    lp_power.addRule (RelativeLayout.CENTER_IN_PARENT);

    m_iv_pwr = new ImageView (m_context);
    m_iv_pwr.setImageResource (R.drawable.dial_power_off);
    freq_dial_relative_layout.addView (m_iv_pwr, lp_power);

    m_dial.listener_set (new gui_dia.gui_dia_listener () {                      // Setup listener for state_chngd() and dial_chngd()

      public boolean prev_go () {
        if (! m_com_api.tuner_state.equalsIgnoreCase ("start")) {
          com_uti.logd ("via gui_dia abort tuner_state: " + m_com_api.tuner_state);
          return (false);                                               // Not Consumed
        }
        ani (m_iv_prev);
        m_com_api.key_set ("tuner_freq", "down");
        return (true);                                                  // Consumed
      }
      public boolean next_go () {
        if (! m_com_api.tuner_state.equalsIgnoreCase ("start")) {
          com_uti.logd ("via gui_dia abort tuner_state: " + m_com_api.tuner_state);
          return (false);                                               // Not Consumed
        }
        ani (m_iv_next);
        m_com_api.key_set ("tuner_freq", "up");
        return (true);                                                  // Consumed
      }
      public boolean state_chngd () {
        com_uti.logd ("via gui_dia m_com_api.audio_state: " + m_com_api.audio_state);
        if (m_com_api.audio_state.equalsIgnoreCase ("start"))
          m_com_api.key_set ("tuner_state", "stop");
        else
          m_com_api.key_set ("audio_state", "start");
        return (true);                                                  // Consumed
      }
      public boolean freq_go () {
        com_uti.logd ("via gui_dia");
        if (! m_com_api.tuner_state.equalsIgnoreCase ("start")) {
          com_uti.logd ("via gui_dia abort tuner_state: " + m_com_api.tuner_state);
          return (false);                                               // Not Consumed
        }
        if (last_dial_freq < 87500 || last_dial_freq > 108000)
          return (false);                                               // Not Consumed
        m_com_api.key_set ("tuner_freq", "" + last_dial_freq);
        return (true);                                                  // Consumed
      }
      private int last_dial_freq = 0;
      public boolean dial_chngd (double angle) {
        if (! m_com_api.tuner_state.equalsIgnoreCase ("start")) {
          com_uti.logd ("via gui_dia abort tuner_state: " + m_com_api.tuner_state);
          return (false);                                               // Not Consumed
        }
        long curr_time = com_uti.tmr_ms_get ();
        int freq = dial_freq_get (angle);
        com_uti.logd ("via gui_dia angle: " + angle + "  freq: " + freq);
        freq += 25;
        freq /= 50;
        freq *= 50;
        if (freq < 87500 || freq > 108000)
          return (false);                                               // Not Consumed
        freq = com_uti.tnru_freq_enforce (freq);
        com_uti.logd ("via gui_dia freq: " + freq + "  curr_time: " + curr_time + "  last_rotate_time: " + last_rotate_time);
        dial_freq_set (freq);   // !! Better to set fast !!
        last_dial_freq = freq;

        if (delay_dial_handler != null) {
          if (delay_dial_runnable != null)
            delay_dial_handler.removeCallbacks (delay_dial_runnable);
        }
        else
          delay_dial_handler = new android.os.Handler ();

        delay_dial_runnable = new Runnable () {
          public void run() { 
            m_com_api.key_set ("tuner_freq", "" + last_dial_freq);
          } 
        };
        delay_dial_handler.postDelayed (delay_dial_runnable, 50);

        return (true);                                                  // Consumed
      }
    });
  }

  private int dial_freq_get (double angle) {
    double percent = (angle + 150) / 3;
    return ((int) (freq_percent_factor * percent + freq_at_210));
  }


  private void dial_freq_set (int freq) {
    if (last_dial_freq == freq)                                         // Optimize
      return;
    if (m_dial == null)
      return;
    last_dial_freq = freq;
    double percent = (freq - freq_at_210) / freq_percent_factor;
    double angle = (percent * 3) - 150;
    m_dial.dial_angle_set (angle);
  }





  private void presets_setup (int clr) {                                // 16 Max Preset Buttons hardcoded

        // Textviews:
    m_preset_tv [0] = (TextView) m_gui_act.findViewById (R.id.tv_preset_0);
    m_preset_tv [1] = (TextView) m_gui_act.findViewById (R.id.tv_preset_1);
    m_preset_tv [2] = (TextView) m_gui_act.findViewById (R.id.tv_preset_2);
    m_preset_tv [3] = (TextView) m_gui_act.findViewById (R.id.tv_preset_3);
    m_preset_tv [4] = (TextView) m_gui_act.findViewById (R.id.tv_preset_4);
    m_preset_tv [5] = (TextView) m_gui_act.findViewById (R.id.tv_preset_5);
    m_preset_tv [6] = (TextView) m_gui_act.findViewById (R.id.tv_preset_6);
    m_preset_tv [7] = (TextView) m_gui_act.findViewById (R.id.tv_preset_7);
    m_preset_tv [8] = (TextView) m_gui_act.findViewById (R.id.tv_preset_8);
    m_preset_tv [9] = (TextView) m_gui_act.findViewById (R.id.tv_preset_9);
    m_preset_tv [10]= (TextView) m_gui_act.findViewById (R.id.tv_preset_10);
    m_preset_tv [11]= (TextView) m_gui_act.findViewById (R.id.tv_preset_11);
    m_preset_tv [12]= (TextView) m_gui_act.findViewById (R.id.tv_preset_12);
    m_preset_tv [13]= (TextView) m_gui_act.findViewById (R.id.tv_preset_13);
    m_preset_tv [14]= (TextView) m_gui_act.findViewById (R.id.tv_preset_14);
    m_preset_tv [15]= (TextView) m_gui_act.findViewById (R.id.tv_preset_15);

        // Imagebuttons:
    m_preset_ib [0] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_0);
    m_preset_ib [1] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_1);
    m_preset_ib [2] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_2);
    m_preset_ib [3] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_3);
    m_preset_ib [4] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_4);
    m_preset_ib [5] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_5);
    m_preset_ib [6] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_6);
    m_preset_ib [7] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_7);
    m_preset_ib [8] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_8);
    m_preset_ib [9] = (ImageButton) m_gui_act.findViewById (R.id.ib_preset_9);
    m_preset_ib [10]= (ImageButton) m_gui_act.findViewById (R.id.ib_preset_10);
    m_preset_ib [11]= (ImageButton) m_gui_act.findViewById (R.id.ib_preset_11);
    m_preset_ib [12]= (ImageButton) m_gui_act.findViewById (R.id.ib_preset_12);
    m_preset_ib [13]= (ImageButton) m_gui_act.findViewById (R.id.ib_preset_13);
    m_preset_ib [14]= (ImageButton) m_gui_act.findViewById (R.id.ib_preset_14);
    m_preset_ib [15]= (ImageButton) m_gui_act.findViewById (R.id.ib_preset_15);

    for (int idx = 0; idx < com_api.max_presets; idx ++) {              // For all presets...
      m_preset_ib [idx].setOnClickListener     (preset_select_lstnr);   // Set click listener
      m_preset_ib [idx].setOnLongClickListener (preset_change_lstnr);   // Set long click listener
    }


    for (int idx = 0; idx < com_api.max_presets; idx ++) {               // For all presets...
      String name = com_uti.prefs_get (m_context, "service_preset_name_" + idx, "");
      String freq = com_uti.prefs_get (m_context, "service_preset_freq_" + idx, "");
      if (freq != null && ! freq.equals ("")) {                         // If non empty frequency (if setting exists)
        m_presets_curr = idx + 1;                                       // Update number of presets
        m_preset_freq [idx] = freq;
        m_preset_name [idx] = name;

        if (name != null)
          m_preset_tv [idx].setText (name);
        else //if (freq != null && ! freq.equals (""))
          m_preset_tv [idx].setText ("" + ((double) com_uti.int_get (freq)) / 1000);
        m_preset_ib [idx].setImageResource (R.drawable.transparent);
        //m_preset_ib [idx].setEnabled (false);
      }
      else {
        //m_presets_curr = idx + 1;                                     // DISABLED: Update number of presets (For blank ones now too)
        m_preset_freq [idx] = "";
        m_preset_name [idx] = "";
        //m_preset_ib [idx].setImageResource (R.drawable.btn_preset);
        ////m_preset_ib [idx].setEnabled (true);
      }
      m_preset_tv [idx].setTextColor (clr);
    }

    com_uti.logd ("m_presets_curr: " + m_presets_curr);
  }


  private void text_default () {
    //m_tv_svc_count.setText ("");
    //m_tv_svc_phase.setText  ("");
    //m_tv_svc_cdown.setText  ("");

    m_tv_pilot.setText      ("");
    m_tv_band.setText       ("");

    m_tv_rssi.setText       ("");
    m_tv_ps.setText         ("");
    m_tv_picl.setText       ("");
    m_tv_ptyn.setText       ("");
    m_tv_rt.setText         ("");
    m_tv_rt.setSelected     (true);                                     // Need setSelected() for marquis
  }


  private void eq_start () {
    int audio_sessid_int = com_uti.int_get (m_com_api.audio_sessid);
    com_uti.logd ("audio_sessid: " + m_com_api.audio_sessid + "  audio_sessid_int: " + audio_sessid_int);
    try {                                                               // Not every phone/ROM has EQ installed, especially stock ROMs
      Intent i = new Intent (AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
      i.putExtra (AudioEffect.EXTRA_AUDIO_SESSION, audio_sessid_int);         //The EXTRA_CONTENT_TYPE extra will help the control panel application customize both the UI layout and the default audio effect settings if none are already stored for the calling application
      m_gui_act.startActivityForResult (i, 0);
    }
    catch (Throwable e) {
      com_uti.loge ("exception");
    }
  }


    // Dialog methods:

  private static final int DAEMON_START_DIALOG    = 1;                    // Daemon start
  private static final int DAEMON_ERROR_DIALOG    = 2;                    // Daemon error
  private static final int TUNER_API_ERROR_DIALOG = 3;                    // Tuner API error
  private static final int TUNER_ERROR_DIALOG     = 4;                    // Tuner error
  private static final int FREQ_SET_DIALOG        = 5;                    // Frequency set
  private static final int GUI_MENU_DIALOG        = 6;                    // Menu
  private static final int GUI_ABOUT_DIALOG       = 7;                    // About
  private static final int PRESET_CHANGE_DIALOG   = 8;                    // Preset functions
  private static final int PRESET_RENAME_DIALOG   = 9;                    // Preset rename

  public Dialog gap_dialog_create (int id, Bundle args) {               // Create a dialog by calling specific *_dialog_create function    ; Triggered by showDialog (int id);
        //public DialogFragment gap_dialog_create (int id, Bundle args) {
    com_uti.logd ("id: " + id + "  args: " + args);
    Dialog ret = null;                                                  // DialogFragment ret = null;
    switch (id) {
      case DAEMON_START_DIALOG:
        ret = daemon_start_dialog_create    (id);
        break;
      case DAEMON_ERROR_DIALOG:
        ret = daemon_error_dialog_create    (id);
        break;
      case TUNER_API_ERROR_DIALOG:
        ret = tuner_api_error_dialog_create    (id);
        break;
      case TUNER_ERROR_DIALOG:
        ret = tuner_error_dialog_create    (id);
        break;
      case GUI_MENU_DIALOG:
        ret = gui_menu_dialog_create        (id);
        break;
      case GUI_ABOUT_DIALOG:
        ret = gui_about_dialog_create       (id);
        break;
      case FREQ_SET_DIALOG:
        ret = freq_set_dialog_create        (id);
        break;
      case PRESET_CHANGE_DIALOG:
        ret = preset_change_dialog_create   (id);
        break;
      case PRESET_RENAME_DIALOG:
        ret = preset_rename_dialog_create   (id);
        break;
    }
    //com_uti.logd ("dialog: " + ret);
    return (ret);
  }


  boolean free = true;

  private Dialog gui_about_dialog_create (final int id) {
    com_uti.logd ("id: " + id);

    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);

    dlg_bldr.setTitle ("SpiritF " + com_uti.app_version_get (m_context));

    String menu_msg = "Select EQ (Equalizer) or Cancel.";
    if (free)
      menu_msg = "Select Go Pro or Cancel.";

    if (com_uti.devnum == com_uti.DEV_OM7 || com_uti.devnum == com_uti.DEV_LG2 || com_uti.devnum == com_uti.DEV_XZ2) {
      if (free)
        menu_msg = "Select Go Pro, Install BT Shim, Remove BT Shim.";
      else
        menu_msg = "Select EQ (Equalizer), Install BT Shim, Remove BT Shim.";
    }

    dlg_bldr.setMessage (menu_msg);

    if (free) {
      dlg_bldr.setNegativeButton ("Go Pro", new DialogInterface.OnClickListener () {
        public void onClick (DialogInterface dialog, int whichButton) {
          purchase ("fm.a2d." + "s2");  // Avoid app name change
        }
      });
    }
    else {
      dlg_bldr.setNegativeButton ("EQ", new DialogInterface.OnClickListener () {
        public void onClick (DialogInterface dialog, int whichButton) {
          eq_start ();
        }
      });
    }

    boolean all_devices_shim = true;
    if (! all_devices_shim && com_uti.devnum != com_uti.DEV_OM7 && com_uti.devnum != com_uti.DEV_LG2 && com_uti.devnum != com_uti.DEV_XZ2) {
      dlg_bldr.setPositiveButton ("Cancel", new DialogInterface.OnClickListener () {     //
        public void onClick (DialogInterface dialog, int whichButton) {
        }
      });
      return (dlg_bldr.create ());
    }

                                                                        // Install Bluetooth Shim
    dlg_bldr.setNeutralButton ("IBTS", new DialogInterface.OnClickListener () {
      public void onClick (DialogInterface dialog, int whichButton) {
        if (com_uti.shim_files_operational_get ())
          Toast.makeText (m_context, "Shim file already installed. Reinstalling...", Toast.LENGTH_LONG).show ();
        else
          Toast.makeText (m_context, "Shim file installing...", Toast.LENGTH_LONG).show ();
        com_uti.shim_install ();
      }
    });

                                                                        // Remove Bluetooth Shim
    dlg_bldr.setPositiveButton ("RBTS", new DialogInterface.OnClickListener () {
      public void onClick (DialogInterface dialog, int whichButton) {
        if (com_uti.shim_files_operational_get ()) {
          Toast.makeText (m_context, "Shim file installed. Removing...", Toast.LENGTH_LONG).show ();
          com_uti.shim_remove ();
        }
        else
          Toast.makeText (m_context, "Shim file not installed !!!", Toast.LENGTH_LONG).show ();
      }
    });

    return (dlg_bldr.create ());
  }


  private Dialog gui_menu_dialog_create (final int id) {
    com_uti.logd ("id: " + id);

    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);
    dlg_bldr.setTitle ("SpiritF Menu");
    ArrayList <String> array_list = new ArrayList <String> ();
    array_list.add ("Set");
    if (free)
      array_list.add ("Go Pro");
    else
      array_list.add ("EQ");
    array_list.add ("Test");
    array_list.add ("Debug");
    array_list.add ("About");
    array_list.add ("Digital");
    array_list.add ("Analog");
    //array_list.add ("Sleep");

    dlg_bldr.setOnCancelListener (new DialogInterface.OnCancelListener () {
      public void onCancel (DialogInterface dialog) {
      }
    });

    String [] items = new String [array_list.size ()];
    array_list.toArray (items);

    dlg_bldr.setItems (items, new DialogInterface.OnClickListener () {
      public void onClick (DialogInterface dialog, int item) {
        com_uti.logd ("item: " + item);
        gap_menu_select (item);
      }

    });

    return (dlg_bldr.create ());
  }


    // Old style hardware Menu button stuff:

  private static final int MENU_SET     = 0;
  private static final int MENU_EQ      = 1;
  private static final int MENU_TEST    = 2;
  private static final int MENU_DEBUG   = 3;
  private static final int MENU_ABOUT   = 4;
  private static final int MENU_DIG     = 5;
  private static final int MENU_ANA     = 6;
  //private static final int MENU_SLEEP   = 7;

  public boolean gap_menu_create (Menu menu) {
    com_uti.logd ("menu: " + menu);
    try {
      menu.add (Menu.NONE, MENU_SET,    Menu.NONE,          "Set").setIcon (R.drawable.ic_menu_preferences);
      if (free)
        menu.add (Menu.NONE, MENU_EQ,   Menu.NONE,       "Go Pro");
      else
        menu.add (Menu.NONE, MENU_EQ,   Menu.NONE,          "EQ");
      menu.add (Menu.NONE, MENU_TEST,   Menu.NONE,         "Test");//.setIcon (R.drawable.ic_menu_view);
      menu.add (Menu.NONE, MENU_DEBUG,  Menu.NONE,        "Debug");//.setIcon (R.drawable.ic_menu_help);
      menu.add (Menu.NONE, MENU_ABOUT,  Menu.NONE,        "About");//.setIcon (R.drawable.ic_menu_info_details);
      menu.add (Menu.NONE, MENU_DIG,    Menu.NONE,      "Digital");
      menu.add (Menu.NONE, MENU_ANA,    Menu.NONE,       "Analog");
      //menu.add (Menu.NONE, MENU_SLEEP,  Menu.NONE,        "Sleep");//.setIcon (R.drawable.ic_lock_power_off);
    }
    catch (Throwable e) {
      com_uti.loge ("Exception: " + e);
      e.printStackTrace ();
    }
    return (true);
  }

  public boolean gap_menu_select (int itemid) {
    com_uti.logd ("itemid: " + itemid);                                 // When "Settings" is selected, after pressing Menu key
    switch (itemid) {
      case MENU_SET:
        m_gui_act.startActivity (new Intent (m_context, set_act.class));// Start Settings activity
        return (true);
      case MENU_EQ:
        if (free)
          purchase ("fm.a2d." + "s2");                                  // Avoid app name change
        else
          eq_start ();
        return (true);
      case MENU_TEST:
        m_gui_act.showDialog (FREQ_SET_DIALOG);
        return (true);
      case MENU_DEBUG:
        m_gui_act.showDialog (DAEMON_START_DIALOG);
        return (true);
      case MENU_ABOUT:
        m_gui_act.showDialog (GUI_ABOUT_DIALOG);
        return (true);
      case MENU_DIG:
        m_com_api.key_set ("audio_mode", "Digital");
        return (true);
      case MENU_ANA:
        m_com_api.key_set ("audio_mode", "Analog");
        return (true);
      //case MENU_SLEEP:
      //  return (true);
    }
    return (false);                                                     // Not consumed ?
  }


    // Root daemon stuff:

  private void daemon_start_dialog_dismiss () {
    com_uti.logd ("");
//    daemon_timeout_stop ();
    if (daemon_start_dialog != null)
      daemon_start_dialog.dismiss ();
    daemon_start_dialog = null;
  }

  private Dialog daemon_start_dialog_create (final int id) {
  //private DialogFragment daemon_start_dialog_create (final int id) {
    com_uti.logd ("id: " + id);

    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);

    dlg_bldr.setTitle ("SpiritF " + com_uti.app_version_get (m_context));

    boolean need_daemon = false;
    String start_msg = "";

    if (! com_uti.su_installed_get ()) {
      start_msg +=  "ERROR: NO SuperUser/SuperSU/Root  SpiritF REQUIRES Root.\n\n";
    }
    else if (com_uti.devnum == com_uti.DEV_UNK) {
      start_msg +=  "ERROR: Unknown Device. SpiritF REQUIRES International GS1/GS2/GS3/Note/Note2, HTC One, LG G2, Xperia Z+/Qualcomm.\n\n";
    }
    else if (com_uti.devnum == com_uti.DEV_LG2 || com_uti.devnum == com_uti.DEV_OM7 || com_uti.devnum == com_uti.DEV_XZ2) {
      need_daemon = true;
      start_msg +=  "SpiritF Root Daemon Starting...\n\n" +
        "HTC One, LG G2 & Sony Z2+ can take 7 seconds and may REBOOT on install.\n\n";
    }
    else {
      need_daemon = true;
      start_msg +=  "SpiritF Root Daemon Starting...\n\n";
    }

    if (need_daemon) {
      //daemon_timeout_start ();
    }

    dlg_bldr.setMessage (start_msg);

    daemon_start_dialog = dlg_bldr.create ();                           // Save so we can dismiss dialog later

    return (daemon_start_dialog);
  }

// m_gui_act.showDialog (DAEMON_ERROR_DIALOG);    // daemon_dialog    daemon_error_dialog_create

  //private int daemon_timeout = 15;                                      // Allow up to 15 seconds for SU prompt to be answered. SuperSU should time itself out by then.

  private Dialog daemon_error_dialog_create (final int id) {
    com_uti.logd ("id: " + id);
    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);
    dlg_bldr.setTitle ("SpiritF " + com_uti.app_version_get (m_context));
    String daemon_msg = "ERROR: Root Daemon did not start after " + (svc_tnr.daemon_start_timout_s) + "  seconds.\n\n" +
                        "SpiritF REQUIRES Root and did not get it.\n\n" +
                        "You need to tap System Settings-> About phone-> Build 7 times to enable System Settings-> Developer Options.\n\n" +
                        "Then in Developer Options you need to set Root access to Apps or Apps and ADB.\n\n" +
                        "Check SuperSU app or System Settings->Superuser and ensure SpiritF is enabled..\n\n";
    dlg_bldr.setMessage (daemon_msg);
    return (dlg_bldr.create ());
  }

  private Dialog tuner_api_error_dialog_create (final int id) {
    com_uti.logd ("id: " + id);
    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);
    dlg_bldr.setTitle ("SpiritF " + com_uti.app_version_get (m_context));
    String daemon_msg = "ERROR: Tuner API did not start.\n\n" +
                        "This device may need a kernel with a working FM driver.\n\n";
    dlg_bldr.setMessage (daemon_msg);
    return (dlg_bldr.create ());
  }

  private Dialog tuner_error_dialog_create (final int id) {
    com_uti.logd ("id: " + id);
    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);
    dlg_bldr.setTitle ("SpiritF " + com_uti.app_version_get (m_context));
    String daemon_msg = "ERROR: Tuner did not start.\n\n" +
                        "This device may need a kernel with a working FM driver.\n\n";
    dlg_bldr.setMessage (daemon_msg);
    return (dlg_bldr.create ());
  }

  void purchase (String app_name) {                                     // Purchase app_name: fm.a2d.sf

    try {
      m_gui_act.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("market://details?id=" + app_name)));                  // Market only
      return;   // Done
    }
    catch (android.content.ActivityNotFoundException e) {
      com_uti.loge ("There is no Google Play app installed");
    }
    catch (Throwable e) {
      e.printStackTrace ();
    }

    try {
      //m_gui_act.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("http://market.android.com/details?" + app_name))); // Choose browser or market (Browser only if no market)
      m_gui_act.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("https://play.google.com/store/apps/details?id=" + app_name))); // Choose browser or market (Browser only if no market)
    }
    catch (android.content.ActivityNotFoundException e) {
      com_uti.loge ("There is no browser or market app installed");
    }
    catch (Throwable e) {
      e.printStackTrace ();
    }
  }



    // Regional settings:
  private static final int        m_freq_lo   = 87500;
  private static final int        m_freq_hi   = 108000;

  private Dialog freq_set_dialog_create (final int id) {                   // Set new frequency
    com_uti.logd ("id: " + id);
    LayoutInflater factory = LayoutInflater.from (m_context);
    final View edit_text_view = factory.inflate (R.layout.edit_number, null);
    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);
    dlg_bldr.setTitle ("Set Frequency");
    dlg_bldr.setView (edit_text_view);
    dlg_bldr.setPositiveButton ("OK", new DialogInterface.OnClickListener () {

      public void onClick (DialogInterface dialog, int whichButton) {

        EditText edit_text = (EditText) edit_text_view.findViewById (R.id.edit_number);
        CharSequence newFreq = edit_text.getEditableText ();
        String nFreq = String.valueOf (newFreq);                        // Get entered text as String
        freq_set (nFreq);
      }
    });
    dlg_bldr.setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
      public void onClick (DialogInterface dialog, int whichButton) {
      }
    });                         
    return (dlg_bldr.create ());
  }

// _PRST
  private Dialog preset_rename_dialog_create (final int id) {                 // Rename preset
    com_uti.logd ("id: " + id);
    LayoutInflater factory = LayoutInflater.from (m_context);
    final View edit_text_view = factory.inflate (R.layout.edit_text, null);
    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);
    dlg_bldr.setTitle ("Preset Rename");
    dlg_bldr.setView (edit_text_view);
    dlg_bldr.setPositiveButton ("OK", new DialogInterface.OnClickListener () {
      public void onClick (DialogInterface dialog, int whichButton) {
        EditText edit_text = (EditText) edit_text_view.findViewById (R.id.edit_text);
        CharSequence new_name = edit_text.getEditableText ();
        String name = String.valueOf (new_name);
        preset_rename (cur_preset_idx, name);
      }
    });
    dlg_bldr.setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
      public void onClick (DialogInterface dialog, int whichButton) {
      }
    });
    return (dlg_bldr.create ());
  }


  private Dialog preset_change_dialog_create (final int id) {
    com_uti.logd ("id: " + id);
    AlertDialog.Builder dlg_bldr = new AlertDialog.Builder (m_context);
    dlg_bldr.setTitle ("Preset");
    ArrayList <String> array_list = new ArrayList <String> ();
    //array_list.add ("Tune");
    array_list.add ("Replace");
    array_list.add ("Rename");
    array_list.add ("Delete");

    dlg_bldr.setOnCancelListener (new DialogInterface.OnCancelListener () {
      public void onCancel (DialogInterface dialog) {
      }
    });
    String [] items = new String [array_list.size ()];
    array_list.toArray (items);

    dlg_bldr.setItems (items, new DialogInterface.OnClickListener () {
      public void onClick (DialogInterface dialog, int item) {

        switch (item) {
          case -1:                                                       // Tune to station
            com_uti.logd ("preset_change_dialog_create onClick Tune freq: " + m_preset_freq [cur_preset_idx]);
            com_uti.logd ("preset_change_dialog_create onClick Tune name: " + m_preset_name [cur_preset_idx]);
            freq_set ("" + m_preset_freq [cur_preset_idx]);             // Change to preset frequency
            break;
          case 0:                                                       // Replace preset with currently tuned station
            com_uti.logd ("preset_change_dialog_create onClick Replace freq: " + m_preset_freq [cur_preset_idx]);
            com_uti.logd ("preset_change_dialog_create onClick Replace name: " + m_preset_name [cur_preset_idx]);
            preset_set (cur_preset_idx);
            com_uti.logd ("preset_change_dialog_create onClick Replace freq: " + m_preset_freq [cur_preset_idx]);
            com_uti.logd ("preset_change_dialog_create onClick Replace name: " + m_preset_name [cur_preset_idx]);
            break;
          case 1:                                                       // Rename preset
            com_uti.logd ("preset_change_dialog_create onClick Rename freq: " + m_preset_freq [cur_preset_idx]);
            com_uti.logd ("preset_change_dialog_create onClick Rename name: " + m_preset_name [cur_preset_idx]);
            m_gui_act.showDialog (PRESET_RENAME_DIALOG);
            com_uti.logd ("preset_change_dialog_create onClick Rename freq: " + m_preset_freq [cur_preset_idx]);
            com_uti.logd ("preset_change_dialog_create onClick Rename name: " + m_preset_name [cur_preset_idx]);
            break;
          case 2:                                                       // Delete preset   !! Deletes w/ no confirmation
            com_uti.logd ("preset_change_dialog_create onClick Delete freq: " + m_preset_freq [cur_preset_idx]);
            com_uti.logd ("preset_change_dialog_create onClick Delete name: " + m_preset_name [cur_preset_idx]);
            preset_delete (cur_preset_idx);
            com_uti.logd ("preset_change_dialog_create onClick Delete freq: " + m_preset_freq [cur_preset_idx]);
            com_uti.logd ("preset_change_dialog_create onClick Delete name: " + m_preset_name [cur_preset_idx]);
            break;
          default:                                                      // Should not happen
            break;
        }

    }
  });

    return (dlg_bldr.create ());
  }


  private void freq_set (String nFreq) {
    if (TextUtils.isEmpty (nFreq)) {                                    // If an empty string...
      com_uti.loge ("nFreq: " + nFreq);
      return;
    }
    Float ffreq = 0f;
    try {
      ffreq = Float.valueOf (nFreq);
    }
    catch (Throwable e) {
      com_uti.loge ("ffreq = Float.valueOf (nFreq); failed");
      //e.printStackTrace ();
    }
    int freq = (int) (ffreq * 1000);
    if (freq < 0.1) {
      return;
    }
    else if (freq > 199999 && freq < 300000) {                          // Codes 200 000 - 2xx xxx - 299 999 = get/set private Broadcom/Qualcomm control
      m_com_api.key_set ("tuner_extension", "" + freq);
      com_uti.logd ("get/set private Qualcomm/V4L control: " + freq);
      return;
    }
    else if (freq < 46001 && freq > 45999) {                            // Code 46 = Remove libbt-hci.so BT shim
      com_uti.shim_remove ();
      return;
    }
    else if (freq < 47001 && freq > 46999) {                            // Code 47 = ?
      return;
    }
    else if (freq < 48001 && freq > 47999) {                            // Code 48 = Enable transmit
      com_uti.s2_tx_set (true);
      if (! com_uti.m_manufacturer.startsWith ("SONY"))
        Toast.makeText (m_context, "!!! TRANSMIT likely on SONY ONLY !!!", Toast.LENGTH_LONG).show ();
      return;
    }
    else if (freq < 49001 && freq > 48999) {                            // Code 49 = Disable transmit
      com_uti.s2_tx_set (false);
      return;
    }
    else if (freq < 7001 && freq > 6999) {                              // Code 7 = logs_email
      logs_email ();
      return;
    }
    else if (freq >= m_freq_lo * 10 && freq <= m_freq_hi * 10) {      // For 760 - 1080
      freq /= 10;
    }
    else if (freq >= m_freq_lo * 100 && freq <= m_freq_hi * 100) {    // For 7600 - 10800
      freq /= 100;
    }
    else if (freq >= m_freq_lo * 1000 && freq <= m_freq_hi * 1000) {  // For 76000 - 108000
      freq /= 1000;
    }
    if (freq >= m_freq_lo && freq <= m_freq_hi) {
      com_uti.logd ("Frequency changing to : " + freq + " KHz");
      m_com_api.key_set ("tuner_freq", "" + freq);
    }
    else {
      com_uti.loge ("Frequency invalid: " + ffreq);
    }
  }


    // :

  private void gui_pwr_update (boolean pwr) {                            // Enables/disables buttons based on power
    if (pwr) {
      //m_iv_stop.setImageResource (R.drawable.btn_stop);
      m_iv_stop.setImageResource (R.drawable.dial_power_on_250sc);
      m_iv_pwr.setImageResource  (R.drawable.dial_power_on);
    }
    else {
      //m_iv_stop.setImageResource (R.drawable.btn_play);
      m_iv_stop.setImageResource (R.drawable.dial_power_off_250sc);
      m_iv_pwr.setImageResource  (R.drawable.dial_power_off);
      text_default ();                                                  // Set all displayable text fields to initial OFF defaults
    }

                                                                        // Power button is always enabled
    m_iv_record.setEnabled  (pwr);
    m_iv_seekup.setEnabled  (pwr);
    m_iv_seekdn.setEnabled  (pwr);
    m_tv_rt.setEnabled      (pwr);

    for (int idx = 0; idx < com_api.max_presets; idx ++)                // For all presets...
      m_preset_ib [idx].setEnabled (pwr);
  }





    // CDown timeout stuff:     See m_com_api.service_update_send()

  private int svc_count     = 0;
  private int svc_cdown     = 0;
  private String svc_phase  = "";

  private android.os.Handler cdown_timeout_handler  = null;
  private Runnable           cdown_timeout_runnable = null;

  private void cdown_timeout_stop () {
    com_uti.logd ("svc_phase: " + svc_phase + "  svc_count: " + svc_count + "  svc_cdown: " + svc_cdown);
    if (cdown_timeout_handler != null) {
      if (cdown_timeout_runnable != null)
        cdown_timeout_handler.removeCallbacks (cdown_timeout_runnable);
    }
    cdown_timeout_handler  = null;
    cdown_timeout_runnable = null;

    m_com_api.service_update_send (null, "", "");  // Prevent future detections++

    m_tv_svc_count.setText  ("");
  }

  int update_interval = 2;  // 1 is too often
  private void cdown_timeout_start (String phase, int timeout) {
    svc_count  = timeout;
    svc_cdown  = timeout;
    svc_phase  = phase;
    com_uti.logd ("svc_phase: " + svc_phase + "  svc_count: " + svc_count + "  svc_cdown: " + svc_cdown);
    m_tv_svc_count.setText  ("" + svc_count);
    if (cdown_timeout_handler != null)                                  // If there is already a countdown in progress...
      cdown_timeout_stop ();                                            // Stop that countdown

    cdown_timeout_handler = new android.os.Handler ();                  // Create Handler to post delayed 
    cdown_timeout_runnable = new Runnable () {                          // Create runnable to be called and re-scheduled every update_interval seconds until timeout or success
      public void run() {
        svc_cdown -= update_interval;                                   // Count down
        com_uti.logd ("svc_phase: " + svc_phase + "  svc_count: " + svc_count + "  svc_cdown: " + svc_cdown);

        if (svc_cdown > 0) {                                            // If timeout not finished yet... Toast Update
          Toast.makeText (m_context, "" + svc_cdown + " " + svc_phase, Toast.LENGTH_SHORT).show ();
          m_tv_svc_cdown.setText  ("" + svc_cdown);                     // Display new countdown
          if (cdown_timeout_handler != null)                            // Schedule next callback/run in update_interval seconds
            cdown_timeout_handler.postDelayed (cdown_timeout_runnable, update_interval * 1000);
        } 
        else {                                                          // Else if timeout is finished... handle it w/ error code -777 = timeout
          cdown_error_handle (-777);
        }
      } 
    };
    if (cdown_timeout_handler != null)
      cdown_timeout_handler.postDelayed (cdown_timeout_runnable, update_interval * 1000);
  }

  private void cdown_error_handle (int err) {
    com_uti.logd ("svc_phase: " + svc_phase + "  svc_count: " + svc_count + "  svc_cdown: " + svc_cdown);
    cdown_timeout_stop ();
    svc_count = err;
    String err_str = svc_phase;
    if (err == -777)
      err_str = "TIMEOUT " + svc_phase;
    else if (! svc_phase.startsWith ("ERROR") && ! svc_phase.startsWith ("TIMEOUT"))
      err_str = "ERROR " + err  + " " + svc_phase;

    if (err_str.toLowerCase ().contains ("daemon")) {
    //if (m_com_api.service_phase.equalsIgnoreCase ("ERROR Starting Daemon") || m_com_api.service_phase.equalsIgnoreCase ("TIMEOUT Starting Daemon")) {
      com_uti.loge ("ERROR Daemon /dev/s2d_running: " + com_uti.quiet_file_get ("/dev/s2d_running"));
      daemon_start_dialog_dismiss ();
      //if (! com_uti.quiet_file_get ("/dev/s2d_running"))
        m_gui_act.showDialog (DAEMON_ERROR_DIALOG);
    }
    else if (err_str.toLowerCase ().contains ("tuner api")) {
    //else if (m_com_api.service_phase.equalsIgnoreCase ("ERROR Starting Tuner API")) {
      com_uti.loge ("ERROR Tuner API /dev/s2d_running: " + com_uti.quiet_file_get ("/dev/s2d_running"));
      daemon_start_dialog_dismiss ();
      m_gui_act.showDialog (TUNER_API_ERROR_DIALOG);
    }
    else if (err_str.toLowerCase ().contains ("tuner")) {
    //else if (m_com_api.service_phase.equalsIgnoreCase ("ERROR Starting Tuner")) {
      com_uti.loge ("ERROR Tuner /dev/s2d_running: " + com_uti.quiet_file_get ("/dev/s2d_running"));
      daemon_start_dialog_dismiss ();
      m_gui_act.showDialog (TUNER_ERROR_DIALOG);
    }
    else if (err_str.toLowerCase ().contains ("bluetooth")) {
    //else if (m_com_api.service_phase.equalsIgnoreCase ("ERROR Broadcom Bluetooth Init")) {
      com_uti.loge ("ERROR Broadcom Bluetooth /dev/s2d_running: " + com_uti.quiet_file_get ("/dev/s2d_running"));
      daemon_start_dialog_dismiss ();
    }

    com_uti.loge ("err_str: " + err_str);
    Toast.makeText (m_context, err_str, Toast.LENGTH_LONG).show ();

    if (m_tv_svc_phase != null) {
      m_tv_svc_phase.setText  (err_str);
    }
    else
      com_uti.loge ("m_tv_svc_phase == null  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    if (m_tv_svc_count != null)
      m_tv_svc_count.setText  ("");
    if (m_tv_svc_cdown != null)
      m_tv_svc_cdown.setText  ("");
  }


    // API Callback:

  public void gap_service_update (Intent intent) {
    //com_uti.logd ("");

    if (false) {//! gui_init) {
      com_uti.loge ("Sticky broadcast too early: gui_init: " + gui_init);
      return;
    }

                                                                        // If daemon is running and daemon start dialog is showing, dismiss dialog
    if (daemon_start_dialog != null && com_uti.quiet_file_get ("/dev/s2d_running")) {
      daemon_start_dialog_dismiss ();
    }

    // Power:
    if (m_com_api.tuner_state.equalsIgnoreCase ("start"))
      gui_pwr_update (true);
    else
      gui_pwr_update (false);


    // Debug / Phase Info:

    m_tv_svc_phase.setText  (m_com_api.service_phase);
    m_tv_svc_cdown.setText  (m_com_api.service_cdown);

    if (! m_com_api.service_cdown.equals ("")) {
      com_uti.logd ("Intent: " + intent + "  phase: " + m_com_api.service_phase +  "  cdown: " + m_com_api.service_cdown);

      if (m_com_api.service_cdown.equals ("0")) {                       // If Success...
        com_uti.logd ("Success m_com_api.service_cdown: " + m_com_api.service_cdown);
        cdown_timeout_stop ();
        //m_com_api.service_cdown = "";   // Prevent future detections
      }
      else {
        int cdown = com_uti.int_get (m_com_api.service_cdown);
        if (cdown > 0) {
          com_uti.logd ("cdown: " + cdown);
          cdown_timeout_start (m_com_api.service_phase, cdown);
        }
        else if (cdown == 0) {
          com_uti.loge ("cdown: " + cdown);
        }
        else if (cdown < 0) {
          com_uti.logd ("cdown: " + cdown);
          cdown_error_handle (cdown);
        }
      }
    }

    // Audio Session ID:

    int audio_sessid = com_uti.int_get (m_com_api.audio_sessid);
    if (audio_sessid != 0 && last_audio_sessid_int != audio_sessid) {                        // If audio session ID has changed...
      last_audio_sessid_int = audio_sessid;
      com_uti.logd ("m_com_api.audio_sessid: " + m_com_api.audio_sessid + "  audio_sessid: " + audio_sessid);
      if (audio_sessid == 0) {                                          // If no session, do nothing (or stop visual and EQ)
      }
      else {                                                            // Else if session...
      }
    }

    // Mode Buttons at bottom:

    // Mute/Unmute:
    if (m_com_api.audio_state.equalsIgnoreCase ("start"))
      m_iv_paupla.setImageResource (R.drawable.sel_pause);
    else
      m_iv_paupla.setImageResource (R.drawable.btn_play);

    // Speaker/Headset:
    if (m_com_api.audio_output.equalsIgnoreCase ("speaker")) {                                  // Else if speaker..., Pressing button goes to headset
      if (m_iv_output != null)
        m_iv_output.setImageResource (android.R.drawable.stat_sys_headset);//ic_volume_bluetooth_ad2p);
      cb_speaker.setChecked (true);
    }
    else {                                                              // Pressing button goes to speaker
      if (m_iv_output != null)
        m_iv_output.setImageResource (android.R.drawable.ic_lock_silent_mode_off);
      cb_speaker.setChecked (false);
    }

    // Record Start/Stop:
    if (m_com_api.audio_record_state.equalsIgnoreCase ("start")) {
      m_iv_record.setImageResource (R.drawable.btn_record_press);
    }
    else {
      m_iv_record.setImageResource (R.drawable.btn_record);
    }

    // Frequency:
    int ifreq = (int) (com_uti.double_get (m_com_api.tuner_freq) * 1000);
    ifreq = com_uti.tnru_freq_fix (ifreq + 25);                       // Must fix due to floating point rounding need, else 106.1 = 106.099

    String freq = null;
    if (ifreq >= 50000 && ifreq < 500000) {
      dial_freq_set (ifreq);
      freq = ("" + (double) ifreq / 1000);
    }
    if (freq != null) {
      m_tv_freq.setText (freq);
    }

    m_tv_rssi.setText (m_com_api.tuner_rssi);

    if (m_com_api.tuner_pilot.equalsIgnoreCase ("Mono"))
      m_tv_pilot.setText ("M");
    else if (m_com_api.tuner_pilot.equalsIgnoreCase ("Stereo"))
      m_tv_pilot.setText ("S");
    else
      m_tv_pilot.setText ("");

    m_tv_band.setText (m_com_api.tuner_band);

    m_tv_picl.setText (m_com_api.tuner_rds_picl);

    m_tv_ps.setText (m_com_api.tuner_rds_ps);

    m_tv_ptyn.setText (m_com_api.tuner_rds_ptyn);

    if (! last_rt.equalsIgnoreCase (m_com_api.tuner_rds_rt)) {
      last_rt = m_com_api.tuner_rds_rt;
      m_tv_rt.setText (m_com_api.tuner_rds_rt);
      m_tv_rt.setSelected (true);
    }

  }



    // UI buttons and other controls:

    // Presets:

  private void preset_delete (int idx) {
    com_uti.logd ("idx: " + idx);
    m_preset_tv [idx].setText ("");
    m_preset_freq [idx] = "";
    m_preset_name [idx] = "";
    m_preset_ib [idx].setImageResource (R.drawable.btn_preset);
    m_com_api.key_set ("service_preset_name_" + idx, "delete", "service_preset_freq_" + idx, "delete");   // !! Current implementation requires simultaneous
  }

  private void preset_rename (int idx, String name) {
    com_uti.logd ("idx: " + idx);
    m_preset_tv [idx].setText (name);
    m_preset_name [idx] = name;
    m_com_api.key_set ("service_preset_name_" + idx, name, "service_preset_freq_" + idx, m_preset_freq [idx]);   // !! Current implementation requires simultaneous
  }

  private void preset_set (int idx) {
    if (idx >= com_api.max_presets) {
      com_uti.loge ("idx: " + idx + "  com_api.max_presets: " + com_api.max_presets + "  m_presets_curr: " + m_presets_curr);
      return;
    }
    else if (idx > m_presets_curr) {
      com_uti.loge ("idx: " + idx + "  com_api.max_presets: " + com_api.max_presets + "  m_presets_curr: " + m_presets_curr);
      idx = m_presets_curr;
    }      
    else
      com_uti.logd ("idx: " + idx + "  com_api.max_presets: " + com_api.max_presets + "  m_presets_curr: " + m_presets_curr);

    String freq_text = m_com_api.tuner_freq;
    if (m_com_api.tuner_band.equalsIgnoreCase ("US")) {
      if (! m_com_api.tuner_rds_picl.equals (""))
        freq_text = m_com_api.tuner_rds_picl;
    }
    else {
      if (! m_com_api.tuner_rds_ps.trim ().equals (""))
        freq_text = m_com_api.tuner_rds_ps;
    }

    if (m_presets_curr < idx + 1)
      m_presets_curr = idx + 1;                                         // Update number of presets

    m_preset_tv [idx].setText ("" + freq_text);
    m_preset_name [idx] = freq_text;
    m_preset_freq [idx] = m_com_api.tuner_freq;
    m_com_api.key_set ("service_preset_name_" + idx, "" + freq_text, "service_preset_freq_" + idx, m_com_api.tuner_freq);   // !! Current implementation requires simultaneous
    m_preset_ib [idx].setImageResource (R.drawable.transparent);  // R.drawable.btn_preset
  }

  private View.OnClickListener preset_select_lstnr = new                    // Tap: Tune to preset
        View.OnClickListener () {
    public void onClick (View v) {
      ani (v);
      com_uti.logd ("view: " + v);

      for (int idx = 0; idx < com_api.max_presets; idx ++) {            // For all presets...
        if (v == m_preset_ib [idx]) {                                   // If this preset...
          com_uti.logd ("idx: " + idx);
          try {
            if (m_preset_freq [idx].equals (""))                        // If no preset yet...
              //com_uti.loge ("Must long press to press presets now, to avoid accidental sets");
              preset_set (idx);                                         // Set preset
            else
              freq_set ("" + m_preset_freq [idx]);                      // Else change to preset frequency
          }
          catch (Throwable e) {
            e.printStackTrace ();
          };
          return;
        }
      }

    }
  };

  private int cur_preset_idx = 0;
                                                                        // Long press/Tap and Hold: Show preset change options
  private View.OnLongClickListener preset_change_lstnr = new View.OnLongClickListener () {
    public boolean onLongClick (View v) {
      ani (v);
      com_uti.logd ("view: " + v);
      for (int idx = 0; idx < com_api.max_presets; idx ++) {            // For all presets...
        if (v == m_preset_ib [idx]) {                                   // If this preset...
          cur_preset_idx = idx;
          com_uti.logd ("idx: " + idx);
          if (m_preset_freq [idx].equals (""))                          // If no preset yet...
            preset_set (idx);                                           // Set preset
          else
            m_gui_act.showDialog (PRESET_CHANGE_DIALOG);                // Else show preset options dialog
          break;
        }
      }
      return (true);                                                    // Consume long click
    }
  };


    // Disabled eye-candy animation:

  private void ani (View v) {
    //if (v != null)
    //  v.startAnimation (m_ani_button);
  }


  private View.OnLongClickListener long_click_lstnr = new
        View.OnLongClickListener () {
    public boolean onLongClick (View v) {
      com_uti.logd ("view: " + v);
      ani (v);                                                          // Animate button
      if (v == m_iv_menu) {
        m_gui_act.startActivity (new Intent (m_context, set_act.class));// Start Settings activity
      }
      return (true);                                                    // Consume long click
    }
  };

  private View.OnClickListener short_click_lstnr = new View.OnClickListener () {
    public void onClick (View v) {

      com_uti.logd ("view: " + v);
      ani (v);                                                          // Animate button
      if (v == null) {
        com_uti.loge ("view: " + v);
      }

      else if (v == m_iv_menu)
        m_gui_act.showDialog (GUI_MENU_DIALOG);

      else if (v == m_iv_paupla)
        m_com_api.key_set ("audio_state", "Toggle");

      else if (v == m_iv_stop) {
        m_com_api.key_set ("tuner_state", "Toggle");
        //if (m_com_api.tuner_state.equalsIgnoreCase ("Start"))
        //  m_com_api.key_set ("tuner_state", "Stop");                      // Full power down/up
        //else
        //  m_com_api.key_set ("tuner_state", "Start");                     // Tuner only start, no audio
      }

      else if (v == m_iv_record)
        m_com_api.key_set ("audio_record_state", "toggle");

      else if (v == m_tv_freq)                                          // Frequency direct entry
        m_gui_act.showDialog (FREQ_SET_DIALOG);

      else if (v == m_iv_prev)
        m_com_api.key_set ("tuner_freq", "down");

      else if (v ==  m_iv_next)
        m_com_api.key_set ("tuner_freq", "up");

      else if (v == m_iv_volume) {
        AudioManager m_am = (AudioManager) m_context.getSystemService (Context.AUDIO_SERVICE);
        if (m_am != null) {
          int stream_vol = m_am.getStreamVolume (svc_aud.audio_stream);
          com_uti.logd ("stream_vol : " + stream_vol);                  // Set to current volume (no change) to display system volume change dialog for user input on screen
          m_am.setStreamVolume (svc_aud.audio_stream, stream_vol, AudioManager.FLAG_SHOW_UI);
        }
      }

      else if (v == m_iv_output) {
        if (m_com_api.audio_output.equalsIgnoreCase ("Speaker"))        // If speaker..., Pressing button goes to headset
          m_com_api.key_set ("audio_output", "Headset");
        else
          m_com_api.key_set ("audio_output", "Speaker");
      }

      else if (v == m_iv_seekdn) {                                      // Seek down
        if (com_uti.s2_tx_get ())                                       // Transmit navigates presets instead of seeking
          m_com_api.key_set ("service_seek_state", "down");
        else
          m_com_api.key_set ("tuner_seek_state", "down");
      }

      else if (v == m_iv_seekup) {                                      // Seek up
        if (com_uti.s2_tx_get ())                                       // Transmit navigates presets instead of seeking
          m_com_api.key_set ("service_seek_state", "up");
        else
          m_com_api.key_set ("tuner_seek_state", "up");
      }

    }
  };

    // Handle GUI clicks for 2nd page settings:

  public void gap_gui_clicked (View view) {                             // See res/layout/gui_pg2_layout.xml & gui_act
    int id = view.getId ();
    com_uti.logd ("id: " + id + "  view: " + view);
    switch (id) {

      case R.id.cb_test:
        break;

      case R.id.cb_visu:
        if (((CheckBox) view).isChecked ())
          visualizer_state_set ("Start");
        else
          visualizer_state_set ("Stop");
        break;

      case R.id.cb_tuner_stereo:
        cb_tuner_stereo (((CheckBox) view).isChecked ());
        break;

      case R.id.cb_audio_stereo:
        cb_audio_stereo (((CheckBox) view).isChecked ());
        break;

      case R.id.cb_af:
        cb_af (((CheckBox) view).isChecked ());
        break;

      case R.id.cb_speaker:
        if (((CheckBox) view).isChecked ())
          m_com_api.key_set ("audio_output", "Speaker");
        else
          m_com_api.key_set ("audio_output", "Headset");
        break;

      case R.id.rb_band_eu:
        tuner_band_set ("EU");
        rb_log (view, (RadioButton) view, ((RadioButton) view).isChecked ());
        break;

      case R.id.rb_band_us:
        tuner_band_set ("US");
        rb_log (view, (RadioButton) view, ((RadioButton) view).isChecked ());
        break;
    }
  }


    // Preferences:

  private void load_prefs () { 
    String value = "";
    value = com_uti.prefs_get (m_context, "audio_output", "");
    if (value.equalsIgnoreCase ("Speaker"))
      cb_speaker.setChecked (true);
    else
      cb_speaker.setChecked (false);

    value = com_uti.prefs_get (m_context, "tuner_stereo", "");
    if (value.equalsIgnoreCase ("Mono"))
      ((CheckBox) m_gui_act.findViewById (R.id.cb_tuner_stereo)).setChecked (false);
    else
      ((CheckBox) m_gui_act.findViewById (R.id.cb_tuner_stereo)).setChecked (true);

    value = com_uti.prefs_get (m_context, "audio_stereo", "");
    if (value.equalsIgnoreCase ("Mono"))
      ((CheckBox) m_gui_act.findViewById (R.id.cb_audio_stereo)).setChecked (false);
    else
      ((CheckBox) m_gui_act.findViewById (R.id.cb_audio_stereo)).setChecked (true);

        ((CheckBox) m_gui_act.findViewById (R.id.cb_visu)).setChecked (false);
  }

  private String visualizer_state_set (String state) {
    com_uti.logd ("state: " + state);
    if (state.equalsIgnoreCase ("Start")) {
      ((LinearLayout) m_gui_act.findViewById (R.id.vis)).setVisibility (View.VISIBLE);  //dial_init

      m_dial.setVisibility (View.INVISIBLE);
      m_iv_pwr.setVisibility (View.INVISIBLE);
      ((ImageView) m_gui_act.findViewById (R.id.frequency_bar)).setVisibility (View.INVISIBLE);

      int audio_sessid = com_uti.int_get (m_com_api.audio_sessid);
    }
    else {
      ((LinearLayout) m_gui_act.findViewById (R.id.vis)).setVisibility (View.INVISIBLE);//GONE);

      m_dial.setVisibility (View.VISIBLE);
      m_iv_pwr.setVisibility (View.VISIBLE);
      ((ImageView) m_gui_act.findViewById (R.id.frequency_bar)).setVisibility (View.VISIBLE);

    }
    return (state);                                                     // No error
  }

  void tuner_band_set (String band) {
    m_com_api.tuner_band = band;
    com_uti.tnru_band_set (band);                                            // To setup band values; different process than service
    m_com_api.key_set ("tuner_band", band);
  }

  private void cb_tuner_stereo (boolean checked) {
    com_uti.logd ("checked: " + checked);
    String val = "Stereo";
    if (! checked)
      val = "Mono";
    m_com_api.key_set ("tuner_stereo", val);
  }

  private void cb_audio_stereo (boolean checked) {
    com_uti.logd ("checked: " + checked);
    String val = "Stereo";
    if (! checked)
      val = "Mono";
    m_com_api.key_set ("audio_stereo", val);
  }

  private void cb_af (boolean checked) {
    com_uti.logd ("checked: " + checked);
    if (checked)
      m_com_api.key_set ("tuner_rds_af_state", "Start");
    else
      m_com_api.key_set ("tuner_rds_af_state", "Stop");
  }


    // Radio button logging for test/debug:

  private void rb_log (View view, RadioButton rbt, boolean checked) {
    int btn_id = m_rg_band.getCheckedRadioButtonId ();            // get selected radio button from radioGroup
    RadioButton rad_band_btn = (RadioButton) m_gui_act.findViewById (btn_id);            // find the radiobutton by returned id
    com_uti.logd ("view: " + view + "  rbt: " + rbt + "  checked: " + checked + "  rad btn text: " + "  btn_id: " + btn_id + "  rad_band_btn: " + rad_band_btn + 
                "  text: " + rad_band_btn.getText ());
  }


    // Debug logs Email:

  private boolean new_logs = true;
  String logfile = "/sdcard/bugreport.txt";

  String cmd_build (String cmd) {
    String cmd_head = " ; ";
    String cmd_tail = " >> " + logfile;
    return (cmd_head + cmd + cmd_tail);
  }

  String new_logs_cmd_get () {
    String cmd  = "rm " + logfile;

    cmd += cmd_build ("cat /data/data/fm.a2d.sf/shared_prefs/s2_prefs.xml");
    cmd += cmd_build ("id");
    cmd += cmd_build ("uname -a");
    cmd += cmd_build ("getprop");
    cmd += cmd_build ("ps");
    cmd += cmd_build ("lsmod");
                                                                        // !! Wildcard * can lengthen command line unexpectedly !!
    cmd += cmd_build ("modinfo /system/vendor/lib/modules/* /system/lib/modules/*");
    //cmd += cmd_build ("dumpsys");                                     // 11 seconds on MotoG
    cmd += cmd_build ("dumpsys audio");
    cmd += cmd_build ("dumpsys media.audio_policy");
    cmd += cmd_build ("dumpsys media.audio_flinger");

    cmd += cmd_build ("dmesg");

    cmd += cmd_build ("logcat -d -v time");

    cmd += cmd_build ("ls -lR /data/data/fm.a2d.sf /init* /sbin /firmware /data/anr /data/tombstones /dev /system /sys");

    return (cmd);
  }

  private boolean file_email (String subject, String filename) {        // See http://stackoverflow.com/questions/2264622/android-multiple-email-attachment-using-intent-question
    Intent i = new Intent (Intent.ACTION_SEND);
    i.setType ("message/rfc822");                                       // Doesn't work well: i.setType ("text/plain");
    i.putExtra (Intent.EXTRA_EMAIL  , new String []{"mikereidis@gmail.com"});
    i.putExtra (Intent.EXTRA_SUBJECT, subject);
    i.putExtra (Intent.EXTRA_TEXT   , "Please write write problem, device/model and ROM/version. Please ensure " + filename + " file is actually attached or send manually. Thanks ! Mike.");
    i.putExtra (Intent.EXTRA_STREAM, Uri.parse ("file://" + filename)); // File -> attachment
    try {
      m_gui_act.startActivity (Intent.createChooser (i, "Send email..."));
    }
    catch (android.content.ActivityNotFoundException e) {
      Toast.makeText (m_context, "No email. Manually send " + filename, Toast.LENGTH_LONG).show ();
    }
    //dlg_dismiss (DLG_WAIT);
    return (true);
  }

  private int long_logs_email () {
    String cmd = "bugreport > " + logfile;
    if (new_logs) {
      logfile = "/sdcard/spirit2log.txt";
      com_uti.daemon_set ("audio_alsa_log", "1");                       // Log ALSA controls
      cmd = new_logs_cmd_get ();
    }
    m_com_api.service_update_send (null, "Writing Debug Log", "60");      // Send Phase Update
    int ret = com_uti.sys_run (cmd, true);                              // Run "bugreport" and output to file
    m_com_api.service_update_send (null, "Sending Debug Log", "20");      // Send Phase Update

    String subject = "SpiritF " + com_uti.app_version_get (m_context);
    boolean bret = file_email (subject, logfile);                       // Email debug log file
    m_com_api.service_update_send (null, "Success Sending Debug Log", "0");// Send Success Update

    return (0);
  }

  private Timer logs_email_tmr = null;
  private class logs_email_tmr_hndlr extends java.util.TimerTask {
    public void run () {
      int ret = long_logs_email ();
      com_uti.logd ("done ret: " + ret);
    }
  }

  private int logs_email () {
    int ret = 0;
    logs_email_tmr = new Timer ("Logs Email", true);                    // One shot Poll timer for logs email
    if (logs_email_tmr != null) {
      logs_email_tmr.schedule (new logs_email_tmr_hndlr (), 10);        // Once after 0.01 seconds.
    }
    return (ret);
  }

}
