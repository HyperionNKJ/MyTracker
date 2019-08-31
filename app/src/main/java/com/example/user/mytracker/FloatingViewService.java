package com.example.user.mytracker;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class FloatingViewService extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    private EditText nameInput;
    private EditText amountInput;
    private RadioGroup trackerTypeRG;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,410, // Hard coded so it appears just above keyboard
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);


        //getting the collapsed and expanded view from the floating view
        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
        expandedView = mFloatingView.findViewById(R.id.layoutExpanded);

        //adding click listener to close button and expanded view
        mFloatingView.findViewById(R.id.closeButtonCollapsed).setOnClickListener(this);
        mFloatingView.findViewById(R.id.addButton).setOnClickListener(this);
        mFloatingView.findViewById(R.id.addButton).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopSelf();
                Intent intent = new Intent(FloatingViewService.this, TrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        });
        nameInput = mFloatingView.findViewById(R.id.et_name);
        amountInput = mFloatingView.findViewById(R.id.et_amount);
        trackerTypeRG = mFloatingView.findViewById(R.id.radioGroup);

        //adding an touch listener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        // cannot onTouchListener and onClickListener at the same time. So below act as onClickListener
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            } else {
                                collapsedView.setVisibility(View.VISIBLE);
                                expandedView.setVisibility(View.GONE);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.layoutCollapsed).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeButtonCollapsed:
                stopSelf();
                break;
            case R.id.addButton:
                validateInput();
                break;
        }
    }

    // For expenses amount, Only numbers, no negative, no decimals, no commas
    private void validateInput() {
        String inputAmount = amountInput.getText().toString();
        boolean isExpenses = isExpensesChecked();

        if (hasEmptyInput()) {
            Toast.makeText(this, "Please include both name and amount", Toast.LENGTH_SHORT).show();
        } else if (isExpenses && !inputAmount.matches("^ *[0-9]+ *$")) {
            Toast.makeText(this, "Amount should be a non-negative integer without comma", Toast.LENGTH_SHORT).show();
        } else if (!isExpenses && !inputAmount.matches("^ *[\\p{Alnum} ]+ *$")) {
            Toast.makeText(this, "Amount should be alphanumeric (including space)", Toast.LENGTH_SHORT).show();
        } else {
            addEntry();
        }
    }

    private boolean hasEmptyInput() {
        return nameInput.getText().toString().equals("") || amountInput.getText().toString().equals("");
    }

    private boolean isExpensesChecked() {
        return trackerTypeRG.getCheckedRadioButtonId() == R.id.rb_expenses;
    }

    private void addEntry() {
        String name = nameInput.getText().toString().trim();
        String amount = amountInput.getText().toString().trim();
        FileOutputStream fos = null;

        try {
            if (isExpensesChecked()) {
                fos = openFileOutput("Expenses List", MODE_APPEND);
                fos.write((name + " = " + amount + " == " + getDate() + "\n").getBytes());
                Toast.makeText(this, "Added to expenses list!\nYou bought '" + name + "' for " + amount + " KRW", Toast.LENGTH_LONG).show();
            } else {
                fos = openFileOutput("Shopping List", MODE_APPEND);
                fos.write((name + " = " + amount + "\n").getBytes());
                Toast.makeText(this, "Added to shopping list!\nYou will be buying " + amount + " '" + name + "'", Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getDate() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy").withLocale(Locale.KOREA);
        return formatter.print(new LocalDate());
    }
}