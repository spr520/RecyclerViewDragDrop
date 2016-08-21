package com.spr.ninegridrecyclerview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Spr";
    public static final int NUM_COLUMNS = 3;

    private TextView timerText;
    private TextView feedbackText;
    private Button[] buttons;
    private Boolean bad_move = false;
    private static final Integer[] goal = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8};

    private ArrayList<Integer> cells = new ArrayList<Integer>();
    int gridPaddingLeft = -1;
    int gridPaddingTop = -1;
    int gridPixel = -1;

    // lunch photo
    private int PHOTO_RESULT_CODE = 100;
    private Context mContext;

    private int tsec=0,csec=0,cmin=0;
    private boolean startflag=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridPaddingLeft = getResources().getDimensionPixelSize(R.dimen.grid_layout_padding_left);
        gridPaddingTop = getResources().getDimensionPixelSize(R.dimen.grid_layout_padding_top);
        gridPixel = getResources().getDimensionPixelSize(R.dimen.grid_size);
        buttons = findButtons();
        mContext = getApplicationContext();

        for (int i = 0; i < 9; i++) {
            this.cells.add(i);
        }
//        randomCell();


        timerText = (TextView) findViewById(R.id.Timer);
        feedbackText = (TextView) findViewById(R.id.FeedbackText);

        for (int i = 1; i < 9; i++) {
            buttons[i].setOnTouchListener(new MyTouchListener());
        }


        timerText.setText("0");
        feedbackText.setText(R.string.game_feedback_text);

        Timer timer01 =new Timer();
        timer01.schedule(task, 0,1000);
        startflag = false;


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPhotoSelect();
            }
        });

    }

    private TimerTask task = new TimerTask() {

        @Override
        public void run() {
            if (startflag){
                tsec++;
                Message message = new Message();
                message.what =1;
                handler.sendMessage(message);
            }

        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case 1:
                    csec=tsec%60;
                    cmin=tsec/60;
                    String s="";
                    if(cmin <10){
                        s="0"+cmin;
                    }else{
                        s=""+cmin;
                    }
                    if(csec < 10){
                        s=s+":0"+csec;
                    }else{
                        s=s+":"+csec;
                    }
                    timerText.setText(s);
                    break;

            }
        }
    };

    private void randomCell() {
        Collections.shuffle(this.cells); //random cells array

        //check order
        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = i; j < 9; j++) {
                if(cells.get(i) > cells.get(j)) {
                    count++;
                }
            }
            if(cells.get(i) == 0) {
                count += (i % NUM_COLUMNS) + (i / NUM_COLUMNS);
            }
        }
        Log.d(TAG,"before re-order cells= " + cells);
        if((count % 2)== 1) {
            Log.d(TAG," re-order");
            int tmp = cells.get(8);
            cells.set(8, cells.get(6));
            cells.set(6, tmp);
        }
        Log.d(TAG,"after re-order cells= " + cells);


        fill_grid();
        tsec=0;
        timerText.setText("00:00");
        startflag = true;
    }

    private void startPhotoSelect() {
        Intent launchIntent = new Intent(Intent.ACTION_GET_CONTENT);
        launchIntent.setType("image/*");
        startActivityForResult(launchIntent, PHOTO_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    final Uri uri = data.getData();
                    if(uri != null) {
                        try {
                            InputStream is = mContext.getContentResolver().openInputStream(uri);
                            Bitmap srcBitmap = BitmapFactory.decodeStream(is);
                            fillGridPhoto(srcBitmap);
                            try {
                                is.close();
                            } catch (IOException t) {
                                Log.w(TAG, "Failed close input strean, uri=" + uri);
                            }
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fillGridPhoto(Bitmap srcBitmap) {
        int srcHeight = srcBitmap.getHeight();
        int srcWidth = srcBitmap.getWidth();
        int squareLength = srcHeight > srcWidth ? srcWidth : srcHeight;
        int squareX = (srcWidth - squareLength) / 2;
        int squareY = (srcHeight - squareLength) / 2;
        Bitmap squareBitmap = Bitmap.createBitmap(srcBitmap, squareX, squareY , squareLength, squareLength);
        squareBitmap = Bitmap.createScaledBitmap(squareBitmap, gridPixel * 3, gridPixel * 3, true);

        for (int i = 1; i < 9; i++) {
            int index = i - 1;
            int x = (index % 3) * gridPixel;
            int y = (index / 3) * gridPixel;
            int width = gridPixel;
            int height = gridPixel;
            Bitmap gridBitmap = Bitmap.createBitmap(squareBitmap, x, y, width, height);


            buttons[i].setBackground(new BitmapDrawable(mContext.getResources(), gridBitmap));

        }

        randomCell();
    }

    private int _xDelta;
    private int _yDelta;

    private int _startDragX;
    private int _startDragY;

    private final class MyTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    _yDelta = Y - lParams.topMargin;
                    _startDragX = lParams.leftMargin;
                    _startDragY = lParams.topMargin;
                    break;
                case MotionEvent.ACTION_UP:
                    if (isAllowChangePos(_startDragX, _startDragY, (Button) view)) {
                        makeMove((Button) view);
                    } else {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                        layoutParams.leftMargin = _startDragX;
                        layoutParams.topMargin = _startDragY;
                        view.setLayoutParams(layoutParams);
                    }
                    if (bad_move) {
                        // reset to org position

                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(!isAllowMove((Button) view)) {
                        return true;
                    }

                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                    int newX = X - _xDelta;
                    int newY = Y - _yDelta;

                    calcLeftMargin(_startDragX, newX, layoutParams);

                    calcTopMargin(_startDragY, newY, layoutParams);
                    view.setLayoutParams(layoutParams);
                    break;
            }
            return true;
        }
    }

    private boolean isAllowChangePos(int startDragX , int startDragY, Button button) {
        boolean isAllowChange = false;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)button.getLayoutParams();
        int targetX = params.leftMargin;
        int targetY = params.topMargin;

        if (Math.abs(startDragX - targetX) > (gridPixel / 2) ||
                Math.abs(startDragY - targetY) > (gridPixel / 2)) {
            isAllowChange = true;
        }

        return isAllowChange;
    }

    private void calcLeftMargin(int startDragX, int targetX , FrameLayout.LayoutParams buttonParams) {

        FrameLayout.LayoutParams buttonOParams = (FrameLayout.LayoutParams)buttons[0].getLayoutParams();
        int button0X = buttonOParams.leftMargin;

        int moveOffsetX = targetX - startDragX;
        int allowOffset = button0X - startDragX;

        if (allowOffset > 0 && moveOffsetX > 0) {
            if (allowOffset > moveOffsetX) {
                buttonParams.leftMargin = targetX;
            } else {
                buttonParams.leftMargin = button0X;
            }
        } else if ( allowOffset > 0 && moveOffsetX < 0) {
            buttonParams.leftMargin = startDragX;
        }

        if (allowOffset < 0 && moveOffsetX < 0) {
            if (allowOffset < moveOffsetX) {
                buttonParams.leftMargin = targetX;
            } else {
                buttonParams.leftMargin = button0X;
            }
        } else if ( allowOffset < 0 && moveOffsetX > 0) {
            buttonParams.leftMargin = startDragX;
        }


    }

    private void calcTopMargin(int startDragY, int targetY , FrameLayout.LayoutParams buttonParams) {

        FrameLayout.LayoutParams buttonOParams = (FrameLayout.LayoutParams)buttons[0].getLayoutParams();
        int button0Y = buttonOParams.topMargin;

        int moveOffsetY = targetY - startDragY;
        int allowOffset = button0Y - startDragY;

        if (allowOffset > 0 && moveOffsetY > 0) {
            if (allowOffset > moveOffsetY) {
                buttonParams.topMargin = targetY;
            } else {
                buttonParams.topMargin = button0Y;
            }
        } else if ( allowOffset > 0 && moveOffsetY < 0) {
            buttonParams.topMargin = startDragY;
        }

        if (allowOffset < 0 && moveOffsetY < 0) {
            if (allowOffset < moveOffsetY) {
                buttonParams.topMargin = targetY;
            } else {
                buttonParams.topMargin = button0Y;
            }
        } else if ( allowOffset < 0 && moveOffsetY > 0) {
            buttonParams.topMargin = startDragY;
        }


    }


    public Button[] findButtons() {
        Button[] b = new Button[9];

        b[0] = (Button) findViewById(R.id.Button00);
        b[1] = (Button) findViewById(R.id.Button01);
        b[2] = (Button) findViewById(R.id.Button02);
        b[3] = (Button) findViewById(R.id.Button03);
        b[4] = (Button) findViewById(R.id.Button04);
        b[5] = (Button) findViewById(R.id.Button05);
        b[6] = (Button) findViewById(R.id.Button06);
        b[7] = (Button) findViewById(R.id.Button07);
        b[8] = (Button) findViewById(R.id.Button08);
        return b;
    }

    public boolean isAllowMove(final Button b) {
        int b_text, b_pos, zuk_pos;
        boolean isAllowMove = false;
        b_text = Integer.parseInt((String) b.getText());
        b_pos = find_pos(b_text);
        zuk_pos = find_pos(0);
        switch (zuk_pos) {
            case (0):
                if (b_pos == 1 || b_pos == 3)
                    isAllowMove = true;
                break;
            case (1):
                if (b_pos == 0 || b_pos == 2 || b_pos == 4)
                    isAllowMove = true;
                break;
            case (2):
                if (b_pos == 1 || b_pos == 5)
                    isAllowMove = true;
                break;
            case (3):
                if (b_pos == 0 || b_pos == 4 || b_pos == 6)
                    isAllowMove = true;
                break;
            case (4):
                if (b_pos == 1 || b_pos == 3 || b_pos == 5 || b_pos == 7)
                    isAllowMove = true;
                break;
            case (5):
                if (b_pos == 2 || b_pos == 4 || b_pos == 8)
                    isAllowMove = true;
                break;
            case (6):
                if (b_pos == 3 || b_pos == 7)
                    isAllowMove = true;
                break;
            case (7):
                if (b_pos == 4 || b_pos == 6 || b_pos == 8)
                    isAllowMove = true;
                break;
            case (8):
                if (b_pos == 5 || b_pos == 7)
                    isAllowMove = true;
                break;
        }
        return isAllowMove;
    }

    public void makeMove(final Button b) {
        bad_move = true;
        int b_text, b_pos, zuk_pos;
        b_text = Integer.parseInt((String) b.getText());
        b_pos = find_pos(b_text);
        zuk_pos = find_pos(0);
        switch (zuk_pos) {
            case (0):
                if (b_pos == 1 || b_pos == 3)
                    bad_move = false;
                break;
            case (1):
                if (b_pos == 0 || b_pos == 2 || b_pos == 4)
                    bad_move = false;
                break;
            case (2):
                if (b_pos == 1 || b_pos == 5)
                    bad_move = false;
                break;
            case (3):
                if (b_pos == 0 || b_pos == 4 || b_pos == 6)
                    bad_move = false;
                break;
            case (4):
                if (b_pos == 1 || b_pos == 3 || b_pos == 5 || b_pos == 7)
                    bad_move = false;
                break;
            case (5):
                if (b_pos == 2 || b_pos == 4 || b_pos == 8)
                    bad_move = false;
                break;
            case (6):
                if (b_pos == 3 || b_pos == 7)
                    bad_move = false;
                break;
            case (7):
                if (b_pos == 4 || b_pos == 6 || b_pos == 8)
                    bad_move = false;
                break;
            case (8):
                if (b_pos == 5 || b_pos == 7)
                    bad_move = false;
                break;
        }

        if (bad_move == true) {
            feedbackText.setText("Move Not Allowed");
            return;
        } else {
            feedbackText.setText("Move OK");
            cells.remove(b_pos);
            cells.add(b_pos, 0);
            cells.remove(zuk_pos);
            cells.add(zuk_pos, b_text);
        }


        fill_grid();

        for (int i = 0; i < 8; i++) {
            if (cells.get(i) != goal[i + 1]) {
                return;
            }
        }
        startflag=false;
        feedbackText.setText("we have a winner");
    }

    public void fill_grid() {
        for (int i = 0; i < 9; i++) {
            int text = cells.get(i);
            FrameLayout.LayoutParams absParams =
                    (FrameLayout.LayoutParams) buttons[text].getLayoutParams();
            switch (i) {
                case (0):

                    absParams.leftMargin = gridPaddingLeft;
                    absParams.topMargin = gridPaddingTop;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (1):

                    absParams.leftMargin = gridPaddingLeft + gridPixel;
                    absParams.topMargin = gridPaddingTop;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (2):

                    absParams.leftMargin = gridPaddingLeft + gridPixel * 2;
                    absParams.topMargin = gridPaddingTop;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (3):

                    absParams.leftMargin = gridPaddingLeft;
                    absParams.topMargin = gridPaddingTop + gridPixel;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (4):

                    absParams.leftMargin = gridPaddingLeft + gridPixel;
                    absParams.topMargin = gridPaddingTop + gridPixel;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (5):

                    absParams.leftMargin = gridPaddingLeft + gridPixel * 2;
                    absParams.topMargin = gridPaddingTop + gridPixel;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (6):

                    absParams.leftMargin = gridPaddingLeft;
                    absParams.topMargin = gridPaddingTop + gridPixel * 2;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (7):

                    absParams.leftMargin = gridPaddingLeft + gridPixel;
                    absParams.topMargin = gridPaddingTop  + gridPixel * 2;
                    buttons[text].setLayoutParams(absParams);
                    break;
                case (8):

                    absParams.leftMargin = gridPaddingLeft + gridPixel * 2;
                    absParams.topMargin = gridPaddingTop + gridPixel * 2;
                    buttons[text].setLayoutParams(absParams);
                    break;


            }


        }

    }

    public int find_pos(int element) {
        int i = 0;
        for (i = 0; i < 9; i++) {
            if (cells.get(i) == element) {
                break;
            }
        }
        return i;
    }
}
