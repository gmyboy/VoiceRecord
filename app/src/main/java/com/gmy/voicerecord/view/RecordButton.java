package com.gmy.voicerecord.view;

import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmy.voicerecord.R;


/**
 * 录音按钮
 *
 * @author GMY
 * @mail 2275964276@qq.com
 * @date 2015年6月2日
 */
public class RecordButton extends Button {

    private static final int MIN_RECORD_TIME = 3; // 最短录音时间，单位秒
    private static final int MAX_RECORD_TIME = 60; // 最长录音时间，单位秒
    private static final int RECORD_OFF = 0; // 不在录音
    private static final int RECORD_ON = 1; // 正在录音

    private Dialog mRecordDialog;
    private IRecordButton mAudioRecorder;
    private Thread mRecordThread;
    private RecordListener listener;

    private int recordState = 0; // 录音状态
    private float recodeTime = 0.0f; // 录音时长，如果录音时间太短则录音失败
    private double voiceValue = 0.0; // 录音的音量值
    private boolean isCanceled = false; // 是否取消录音
    private float downY;

    private TextView dialogTextView;
    private ImageView dialogImg;
    private TextView record_time_txt;
    private Context mContext;
    // 终止线程的标志位
    private boolean isStop = false;

    public RecordButton(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        this.setText("按住说话");
        if (mRecordDialog == null) {
            mRecordDialog = new Dialog(mContext, R.style.ViewDialog);
            mRecordDialog.setContentView(R.layout.dialog_record);
            dialogImg = (ImageView) mRecordDialog
                    .findViewById(R.id.record_dialog_img);
            record_time_txt = (TextView) mRecordDialog
                    .findViewById(R.id.record_time_txt);
            dialogTextView = (TextView) mRecordDialog
                    .findViewById(R.id.record_dialog_txt);
        }
    }

    public void setAudioRecord(IRecordButton record) {
        this.mAudioRecorder = record;
    }

    public void setRecordListener(RecordListener listener) {
        this.listener = listener;
    }

    // 录音时显示Dialog
    private void showVoiceDialog(int flag) {

        switch (flag) {
            case 1:
                dialogImg.setImageResource(R.mipmap.record_cancel);
                dialogTextView.setText("松开手指可取消录音");
                this.setText("松开手指 取消录音");
                break;

            default:
                dialogImg.setImageResource(R.mipmap.record_animate_01);
                dialogTextView.setText("向上滑动可取消录音");
                this.setText("松开手指 完成录音");
                break;
        }
        dialogTextView.setTextSize(14);
        mRecordDialog.show();
    }

    // 录音时间太短时Toast显示
    private void showWarnToast(String toastText) {
        Toast toast = new Toast(mContext);
        View warnView = LayoutInflater.from(mContext).inflate(
                R.layout.toast_warn, null);
        toast.setView(warnView);
        toast.setGravity(Gravity.CENTER, 0, 0);// 起点位置为中间
        toast.show();
    }

    // 开启录音计时线程
    private void callRecordTimeThread() {
        mRecordThread = new Thread(recordThread);
        mRecordThread.start();
    }

    // 录音Dialog图片随录音音量大小切换 (为了省事，就随机动了）

    private void setDialogImage() {
        if (voiceValue < 600.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_01);
        } else if (voiceValue > 600.0 && voiceValue < 1000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_02);
        } else if (voiceValue > 1000.0 && voiceValue < 1200.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_03);
        } else if (voiceValue > 1200.0 && voiceValue < 1400.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_04);
        } else if (voiceValue > 1400.0 && voiceValue < 1600.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_05);
        } else if (voiceValue > 1600.0 && voiceValue < 1800.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_06);
        } else if (voiceValue > 1800.0 && voiceValue < 2000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_07);
        } else if (voiceValue > 2000.0 && voiceValue < 3000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_08);
        } else if (voiceValue > 3000.0 && voiceValue < 4000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_09);
        } else if (voiceValue > 4000.0 && voiceValue < 6000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_10);
        } else if (voiceValue > 6000.0 && voiceValue < 8000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_11);
        } else if (voiceValue > 8000.0 && voiceValue < 10000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_12);
        } else if (voiceValue > 10000.0 && voiceValue < 12000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_13);
        } else if (voiceValue > 12000.0) {
            dialogImg.setImageResource(R.mipmap.record_animate_14);
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis((long) (recodeTime * 1000));
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "mm:ss");
        // 超时停止线程
        if (recodeTime > MAX_RECORD_TIME) {
            isCanceled = true;
            this.setText("按住说话");
        }
        record_time_txt.setText(format.format(gc.getTime()));
    }

    // 录音线程
    private Runnable recordThread = new Runnable() {

        @Override
        public void run() {
            recodeTime = 0.0f;
            while (recordState == RECORD_ON) {
                {
                    try {
                        Thread.sleep(100);
                        recodeTime += 0.1;
                        // 获取音量，更新dialog
                        if (!isCanceled) {
                            voiceValue = mAudioRecorder.getAmplitude();
                            recordHandler.sendEmptyMessage(1);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler recordHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setDialogImage();
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下按钮

                try {
                    if (recordState != RECORD_ON) {
                        showVoiceDialog(0);
                        downY = event.getY();
                        if (mAudioRecorder != null) {
                            mAudioRecorder.ready();
                            recordState = RECORD_ON;
                            mAudioRecorder.start();
                            callRecordTimeThread();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "请检查录音权限是否开启", Toast.LENGTH_SHORT)
                            .show();
                    mRecordDialog.dismiss();
                    e.printStackTrace();
                }

                break;
            case MotionEvent.ACTION_MOVE: // 滑动手指
                float moveY = event.getY();
                if (downY - moveY > 50) {
                    isCanceled = true;
                    // 暂定录音，及时暂停

                    // try {
                    // mRecordThread.wait();
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    showVoiceDialog(1);
                }
                if (downY - moveY < 20) {
                    isCanceled = false;
                    // 重新开始录音 计时
                    // mRecordThread.notify();
                    showVoiceDialog(0);
                }
                break;
            case MotionEvent.ACTION_UP: // 松开手指
                if (recordState == RECORD_ON) {
                    recordState = RECORD_OFF;
                    if (mRecordDialog.isShowing()) {
                        mRecordDialog.dismiss();
                    }
                    mAudioRecorder.stop();
                    mRecordThread.interrupt();
                    voiceValue = 0.0;
                    if (isCanceled) {// 取消录音
                        mAudioRecorder.deleteOldFile();
                    } else {
                        if (recodeTime < MIN_RECORD_TIME) {
                            showWarnToast("时间太短  录音失败");
                            mAudioRecorder.deleteOldFile();
                        } else if (recodeTime > MAX_RECORD_TIME) {
                            showWarnToast("录音时间不能超过 60s");
                            mAudioRecorder.deleteOldFile();
                        } else {
                            if (listener != null) {
                                listener.recordEnd(mAudioRecorder.getFilePath());
                            }
                            mAudioRecorder.complite(recodeTime);
                        }
                    }
                    isCanceled = false;
                    this.setText("按住说话");
                }
                break;
            //避免第一次录音询问权限时，影响触摸效果
            case MotionEvent.ACTION_CANCEL:
                recordState = RECORD_OFF;
                if (mRecordDialog.isShowing()) {
                    mRecordDialog.dismiss();
                }
                mAudioRecorder.stop();
                mRecordThread.interrupt();
                voiceValue = 0.0;
                isCanceled = false;
                this.setText("按住说话");
                break;
        }
        return true;
    }

    public interface RecordListener {
        public void recordEnd(String filePath);
    }
}