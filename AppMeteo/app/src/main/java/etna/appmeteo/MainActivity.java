package etna.appmeteo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "TEST";
    private static final int READ_REQUEST_CODE = 42;

    private static ListView lv;

    private CharSequence selectedChoice;

    private static final int CREATE_REQUEST_CODE = 40;
    private static final int OPEN_REQUEST_CODE = 41;
    private static final int SAVE_REQUEST_CODE = 42;
    private static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    private static final String TARGET_PATH = "/storage/emulated/0/wtf";

    private void updateListView(){
        File dir = new File(TARGET_PATH);
        File[] filelist = dir.listFiles();
        if (filelist == null)
            return;
        String[] theNamesOfFiles = new String[filelist.length];
        if (theNamesOfFiles != null)
            for (int i = 0; i < theNamesOfFiles.length; i++)
                theNamesOfFiles[i] = filelist[i].getName();

        ArrayAdapter arrayAdapter =
                new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, theNamesOfFiles);
        lv.setAdapter(arrayAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile(view);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        lv = findViewById(R.id.list_item);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Log.i(TAG, "pos :"+ position +" id: "+1 );
                final Object selectedFromList =  (lv.getItemAtPosition(position));
                //System.out.println(selectedFromList.toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final CharSequence[] items = {"Show charts", "Delete file"};
                builder.setTitle("Actions");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Log.i(TAG, items[item].toString());
                        selectedChoice = items[item].toString();

                    }
                });
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    if (selectedChoice == "Show charts") {
                                        Log.i(TAG, ""+id);
                                        HisParser parsed = new HisParser(TARGET_PATH+"/"+selectedFromList.toString());
                                        parsed.readFile();
                                    } else if (selectedChoice == "Delete file") {
                                        FileManagement.deleteFile(TARGET_PATH+"/", selectedFromList.toString());
                                        Toast.makeText(MainActivity.this, selectedFromList.toString() + " deleted", Toast.LENGTH_SHORT).show();
                                        updateListView();
                                    }

                                } catch (Exception ex) {
                                    Log.e("ERROR : ", "err", ex);
                                    Toast.makeText(MainActivity.this, "Error ! Don't Save", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                /*Intent intent = new Intent(MainActivity.this, SendMessage.class);
                String message = "abc";
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);*/
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        updateListView();
        // Check permissions
        FileManagement.verifyStoragePermissions(MainActivity.this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        Uri currentUri = null;
        String fileName = null;
        String fileDate = null;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    currentUri = resultData.getData();
                    try {
                        fileName = getFileName(currentUri);
                        fileDate = getFileDate(currentUri);
                        //FileManagement Fm = new FileManagement();
                        FileManagement.copyFile(DOWNLOAD_PATH+"/", fileName, TARGET_PATH+"/", fileDate);
                        updateListView();
                    } catch (IOException e) {
                        // Handle error here
                    }
                }
            }
        }
    }

    public void openFile(View view)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    public String getFileDate(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String line = null;
        String date = null;
        for (int i = 0; i <=2 ; i++) {
            line = reader.readLine();
            if (i == 2)
                date = line.split("\t")[0];
        }
        inputStream.close();
        return date;
    }


    public String getFileName(Uri uri) {
        Cursor cursor = MainActivity.this.getContentResolver()
                .query(uri, null, null, null, null, null);
        String displayName = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return displayName;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        Log.i(TAG, "You clicked Item: " + id + " at position:" + position);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
