package etna.appmeteo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by erwan on 06/12/17.
 */

public class HisParser extends Application {
    private static String filePath;
    private File file;
    private Context context;

    public HisParser(String filePath) {
        this.context = getBaseContext();
        this.filePath = filePath;
        this.file = new File(this.filePath);
    }

    private Date stringToDate(String aDate,String aFormat) {

        if(aDate==null) return null;
        ParsePosition pos = new ParsePosition(0);
        SimpleDateFormat simpledateformat = new SimpleDateFormat(aFormat);
        Date stringDate = simpledateformat.parse(aDate, pos);
        return stringDate;

    }

    public void readFile() throws IOException, JSONException {
        BufferedReader br = new BufferedReader(new FileReader(this.file));
        String line;
        ArrayList<Date> hour = new ArrayList<Date>();
        ArrayList<Float> air_temperature = new ArrayList<Float>();
        ArrayList<Float> rel_humidity = new ArrayList<Float>();
        ArrayList<Float> air_pressure = new ArrayList<Float>();
        ArrayList<Float> local_ws_2min_mnm = new ArrayList<Float>();

        int counter = 0;
        while ((line = br.readLine()) != null) {
            String[] columnDetail = line.split("\t", -1);
            if (columnDetail.length > 1 && counter > 1) {
                try {
                    Date tmp_hour = this.stringToDate(columnDetail[0], "yyyy-MM-dd HH:mm:ss");
                    float  tmp_air_temp = Float.parseFloat(columnDetail[33]);
                    float  tmp_rel_humi = Float.parseFloat(columnDetail[35]);
                    float  tmp_air_pres = Float.parseFloat(columnDetail[38]);
                    float  tmp_local_ws = Float.parseFloat(columnDetail[3]);
//                    Log.d("DEBUG", "===============");

                    hour.add(tmp_hour);
                    air_temperature.add(tmp_air_temp);
                    rel_humidity.add(tmp_rel_humi);
                    air_pressure.add(tmp_air_pres);
                    local_ws_2min_mnm.add(tmp_local_ws);
                } catch (Exception e) {
//                    Log.d("DEBUG ERROR", e.getMessage());
                }

            }
            counter++;
        }
        System.out.println(Arrays.toString(hour.toArray()));

        br.close();
    }
}