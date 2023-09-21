package com.nuryadincjr.todolist.activity;

import static com.nuryadincjr.todolist.util.AppExecutors.getInstance;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.nuryadincjr.todolist.R;
import com.nuryadincjr.todolist.data.ToDo;
import com.nuryadincjr.todolist.data.ToDoDatabases;
import com.nuryadincjr.todolist.databinding.ActivityActionsBinding;
import com.nuryadincjr.todolist.pojo.Constaint;
import com.nuryadincjr.todolist.util.AdapterPreference;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ActionsActivity extends AppCompatActivity {

    private ActivityActionsBinding binding;
    private AdapterPreference adapterPreference;
    private ToDoDatabases databases;
    private ToDo toDo, data, dataView;
    private Menu menu;
    private boolean isPin, isArchip, isDelete, isInput;

    //for date picker
    private TextView textDate;
    private Button buttonDate;

    //for time picker
    private TextView textTime;
    private Button buttonTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_actions);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding = ActivityActionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databases = ToDoDatabases.getInstance(this);
        getAdapterPreference();


        data = getIntent().getParcelableExtra(Constaint.TITLE_CHANGE);
        dataView = getIntent().getParcelableExtra(Constaint.TITLE_VIW_ONLY);

        isData();

        onInputListener(binding.etDescription);
        onInputListener(binding.etTitle);

        //for date picker

        textDate = findViewById(R.id.dateText);
        buttonDate = findViewById(R.id.dateBtn);

        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openDateDialog();

            }
        });

        //for time picker
        textTime = findViewById(R.id.timeText);
        buttonTime = findViewById(R.id.timeBtn);

        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTimeDialog();
            }
        });

    }

    private void openDateDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                textDate.setText(String.valueOf(day) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year));

                /* Calendar c = Calendar.getInstance();
                c.set(Calendar.DAY_OF_MONTH, day);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.YEAR, year);
                //c.setTimeZone(TimeZone.getDefault());

                SimpleDateFormat format = new SimpleDateFormat("dd/mm/yyyy");
                String time = format.format(c.getTime());
                textDate.setText(time); */

            }
        }, 2023, 2, 3);

        datePickerDialog.show();
    }

    private void openTimeDialog(){
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int min) {

                //textTime.setText(String.valueOf(hour) + ":" + String.valueOf(min));

                /*if(hour > 12) hour -= 12;
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, min);
                c.setTimeZone(TimeZone.getDefault());
                SimpleDateFormat format = new SimpleDateFormat("k:mm a");
                String time = format.format(c.getTime());
                textTime.setText(time); */

                Time time = new Time(hour, min, 0);
                //little h uses 12 hour format and big H uses 24 hour format
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
                //format takes in a Date, and Time is a sublcass of Date
                String s = simpleDateFormat.format(time);
                textTime.setText(s);
            }
        }, 12, 00, false);
        timePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        menu.findItem(R.id.itemRestore).setVisible(false);
        this.menu = menu;

        if(data == null) menu.findItem(R.id.itemDeleteFix).setVisible(false);
        if(dataView != null) {
            menu.findItem(R.id.itemArsip).setVisible(false);
            menu.findItem(R.id.itemPin).setVisible(false);
            menu.findItem(R.id.itemDelete).setVisible(false);
            menu.findItem(R.id.itemDeleteFix).setVisible(true);
            menu.findItem(R.id.itemRestore).setVisible(true);
            menu.findItem(R.id.itemShare).setVisible(false);
        }

        isEdited(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.itemDelete:
                isDelete = true;
                onBackPressed();
                return true;
            case R.id.itemDeleteFix:
                if(data == null) data = dataView;
                getInstance().diskID().execute(() -> databases.toDoDao().delete(data));
                finish();
                return true;
            case R.id.itemRestore:
                dataView.setDelete(false);
                getInstance().diskID().execute(() -> databases.toDoDao().update(dataView));
                finish();
                return true;
            case R.id.itemShare:
                if(!data.getTitle().equals("") && !data.getDetails().equals(""))
                    adapterPreference.shareData("Title: " + data.getTitle() + "\n\n" + data.getDetails());
                else if(data.getTitle().equals("")) adapterPreference.shareData(data.getDetails());
                else if(data.getDetails().equals("")) adapterPreference.shareData(data.getTitle());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onInputListener(TextView view) {
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isInput = i2 > 0;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isEdited(menu);
            }
        });
    }

    private void isEdited(Menu menu) {
        if (isInput) {
            menu.findItem(R.id.itemDelete).setEnabled(true);
            menu.findItem(R.id.itemShare).setEnabled(true);
        } else {
            menu.findItem(R.id.itemDelete).setEnabled(false);
            menu.findItem(R.id.itemShare).setEnabled(false);
        }

        getTonggolButton(menu, R.id.itemArsip, R.layout.btn_archive,
                R.id.tbArchive, isArchip, Constaint.IS_ARCHIVE);
        getTonggolButton(menu, R.id.itemPin, R.layout.btn_pin,
                R.id.tb_Pin, isPin, Constaint.IS_PIN);
    }

    private void isData() {
        if(data != null) {
            getSupportActionBar().setTitle(Constaint.TITLE_CHANGE);
            String titile = data.getTitle();
            String description = data.getDetails();
            binding.etTitle.setText(titile);
            binding.etDescription.setText(description);
            isPin = data.isPin();
            isArchip = data.isArcip();
            isInput = !titile.isEmpty() || !description.isEmpty();

        } else if(dataView != null) {
            getSupportActionBar().setTitle(Constaint.TITLE_VIW_ONLY);
            binding.etTitle.setText(dataView.getTitle());
            binding.etDescription.setText(dataView.getDetails());
            binding.etTitle.setFocusable(false);
            binding.etDescription.setFocusable(false);

            isEditlable(binding.etTitle);
            isEditlable(binding.etDescription);
        } else getSupportActionBar().setTitle(Constaint.TITLE_ADD);
    }

    private void isEditlable(EditText editText) {
        editText.setOnClickListener(v -> Snackbar.make(v,
                getString(R.string.str_isedit), Snackbar.LENGTH_LONG)
                .setAction(Constaint.TITLE_RESTORE, view -> {
                    dataView.setDelete(false);
                    getInstance().diskID().execute(() -> databases.toDoDao().update(dataView));
                    finish();
                }).show());
    }

    private void actionCreateUpdate() {
        String titile = binding.etTitle.getText().toString();
        String description = binding.etDescription.getText().toString();

        if(isDelete) {
            isPin = false;
            isArchip = false;
        }

        toDo = new ToDo(0, titile, description, isPin, isArchip, isDelete, Constaint.time());

        if(dataView == null && (!titile.isEmpty() || !description.isEmpty())) {
            if(data != null) {
                toDo.setUid(data.getUid());

                if(!data.equals(toDo)) {
                    getInstance().diskID().execute(() -> databases.toDoDao().update(toDo));
                    Toast.makeText(this, getString(R.string.str_message_change), Toast.LENGTH_SHORT).show();
                }
            } else{
                getInstance().diskID().execute(() -> databases.toDoDao().insert(toDo));
                Toast.makeText(this, getString(R.string.str_message_save), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        actionCreateUpdate();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getTonggolButton(Menu menu, int findItem, int actionView,
                                  int findViewById, boolean isBoolean, String item) {

        ToggleButton tonggle = menu.findItem(findItem)
                .setActionView(actionView)
                .getActionView()
                .findViewById(findViewById);

        Log.d("LIA", String.valueOf(isInput));
        if(isInput) {
            tonggle.setEnabled(true);
            tonggle.setChecked(isBoolean);

            tonggle.setOnCheckedChangeListener((compoundButton, b) -> {
                if(item.equals(Constaint.IS_PIN)) {
                    isPin = b;
                    isArchip = false;

                    String message = "Pinned";
                    if(!isPin) message = "Unpinned";

                    Snackbar.make(compoundButton, message, Snackbar.LENGTH_LONG)
                            .setAction(Constaint.TITLE_RESTORE, null).show();

                } else if(item.equals(Constaint.IS_ARCHIVE)){
                    isArchip = b;
                    String message = "Archived";
                    if(isPin) {
                        message = "Archived and Unpinned";
                        isPin = false;
                    }

                    if(!isArchip) message = "Unarchived";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            });
        } else{
            tonggle.setEnabled(false);
        }
    }



    private void getAdapterPreference() {
        adapterPreference = new AdapterPreference(this) {
            @Override
            public void openMenuEditPopup(View view, ToDo toDo) {
            }

            @Override
            public void shareData(String value) {
                super.shareData(value);
            }
        };
    }
}