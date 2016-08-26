package com.example.readlibrary;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.ResultCode;


/**
 * 作者：郝生军
 *
 * 下面的该类最早写于15年8月，和五粮液手机现在使用的保持同步。
 *
 *
 */
public class ReadTagActivity extends BaseActivity implements RfidReaderEventListener {

    public ATRfidReader mReader = null;
    String TAG = "ReadTidActivity";


    public void setReadLength(int readLength) {
        ReadLength = readLength;
        Log.i("test",readLength+"et_count");
    }

    //96位TID，96位EPC
    public final int LENGTH1 = 6;
    //128位TID，24位EPC
    public final int LENGTH2 = 8;

    public int ReadLength = LENGTH2;
    private ReadTagListenser listenser;
    private String pwd = "";
    private int readTidOutTime = 800;
    private int readEpcOutTime = 800;
    private boolean isStop = false;

    public void setListenster(ReadTagListenser listenser) {
        this.listenser = listenser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Reader
        if ((mReader = ATRfidManager.getInstance()) == null) {
            listenser.onNoReady();
            Log.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        if (mReader != null) ATRfidManager.wakeUp();
        super.onStart();
    }

    @Override
    protected void onResume() {

        try {
            if (mReader == null) {

            } else {
                mReader.setEventListener(this);
                mReader.setPower(260);
            }

        } catch (ATRfidReaderException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mReader != null) mReader.removeEventListener(this);
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (mReader != null) ATRfidManager.sleep();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mReader != null) ATRfidManager.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onReaderActionChanged(ATRfidReader arg0, ActionState arg1) {
        listenser.onReaderActionChanged(arg1);
    }

    @Override
    public void onReaderReadTag(ATRfidReader arg0, String arg1, float arg2) {
        listenser.onReaderReadTag(arg1, arg2);
    }

    private boolean hasFirstReasult = false;
    private boolean hasSecondReasult = false;

    private int readEpcCount = 0;

    @Override
    public void onReaderResult(ATRfidReader arg0, ResultCode arg1, ActionState arg2, String epc, String data) {

        if (arg1 == ResultCode.OutOfRetries && readEpcCount == 0) {
            handReader.removeMessages(CHECK);

            readEpcCount = 2;
            readEpc(pwd, 700);

        } else {

            readEpcCount = 0;

            if (readType == 1) {
                hasFirstReasult = true;
            } else {
                hasSecondReasult = true;
            }

            handReader.removeMessages(CHECK);
            result = epc;
            listenser.onReaderResult(data, epc, arg1);
        }
    }

    @Override
    public void onReaderStateChanged(ATRfidReader arg0, ConnectionState arg1) {
        // TODO Auto-generated method stub
        listenser.onReaderStateChanged(arg1);
    }

    /**
     * 模拟手机重新启动
     */
    public void restartMobile() {

        // close
        mReader.removeEventListener(this);
        ATRfidManager.sleep();
        ATRfidManager.onDestroy();

        // open
        if ((mReader = ATRfidManager.getInstance()) == null) {
            Log.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
        }
        ATRfidManager.wakeUp();
        mReader.setEventListener(this);
    }

    // /// 方法封装的 //////////////////////

    private String result = "";
    /**
     * 读取标签类别
     */
    private int readType = 0;
    /**
     * 返回结果
     */
    private static final int CHECK = 0;
    private static final int STOP = 1;
    private static final int RESTART = 2;
    private static final int RESULT_CHECK = 3;

    private static final int READ_FIRST = 4;
    private static final int READ_SECOND = 5;

    private static final int CHECK_RESULT_F = 6;
    private static final int CHECK_RESULT_S = 7;

    Handler handReader = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CHECK:
                    checkReasult();
                    break;

                case STOP:
                    stop();
                    break;

                case RESTART:
                    reStart();
                    break;

                case RESULT_CHECK:
                    break;

                case READ_FIRST:
                    // readTag(1, "");
                    readTagOfTid();
                    break;

                case READ_SECOND:
                    readTagOfEpc(2, pwd);

                    readType = 2;
                    break;

                case CHECK_RESULT_F:
                    resultCheckFirst();
                    break;
                case CHECK_RESULT_S:
                    resultCheckSecond();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    /**
     * 第二次读取到的标签数据为空的数目
     */
    private int readNullCount = 0;

    // 测试的次数，第一次和第二次分别执行不同的命令
    private int checkCount = 0;
    private boolean hasResult = false;

    public boolean readTid() {
        return readTid(800);
    }

    /**
     * 读取标签的tid
     */
    public boolean readTid(int readTidTime) {
        hasFirstReasult = false;
        isStop = false;
        readType = 1;
        if (readTidTime > 700) {
            this.readTidOutTime = readTidTime;
        }
        readNullCount = 0;
        ResultCode res;
        checkCount = 0;

        BankType bank = BankType.valueOf(BankType.TID.getValue());

        if (mReader.getAction() == ActionState.Stop) {
            if ((res = mReader.readMemory6c(bank, 0, ReadLength, "")) != ResultCode.NoError) {
                result = "";
                return false;
            } else {
                // delay(200, CHECK);
                delay(readTidOutTime, CHECK);
                return true;
            }
        } else {
            // delay(200, CHECK);
            delay(readTidOutTime, CHECK);
            return false;
        }

    }

    public boolean readEpc(String pwd) {
        return readEpc(pwd, 800);
    }

    /**
     * 读取标签的epc
     *
     * @param pwd 密码
     */
    public boolean readEpc(String pwd, int readEpcTime) {

//		Toast.makeText(getApplicationContext(), pwd, 1).show();
        hasSecondReasult = false;
        isStop = false;
        if (readEpcTime > 700) {
            this.readEpcOutTime = readEpcTime;
        }
        this.pwd = pwd;
        readType = 2;
        readNullCount = 0;
        ResultCode res;
        checkCount = 0;
        BankType bank = BankType.valueOf(BankType.EPC.getValue());

        checkCount = 0;

        if (mReader.getAction() == ActionState.Stop) {
            if ((res = mReader.readMemory6c(bank, 0, ReadLength, pwd)) != ResultCode.NoError) {
                result = "";
                return false;
            } else {

                delay(readEpcOutTime, CHECK);

                return true;
            }
        } else {
            // delay(200, CHECK);
            delay(readEpcOutTime, CHECK);
            return false;
        }

    }

    /**
     * 停止读标签
     */
    public void stopReader() {

        if (mReader.getAction() != ActionState.Stop) {
            mReader.stop();
        }
    }

    /**
     * 读取标签
     *
     * @param times
     * @param pwd
     */
    private void readTagOfEpc(int times, String pwd) {

        if (!isStop) {
            ResultCode res;
            checkCount = 0;
            BankType bank = BankType.valueOf(BankType.EPC.getValue());
            checkCount = 0;
            if (mReader.getAction() == ActionState.Stop) {
                if ((res = mReader.readMemory6c(bank, 0, 7, pwd)) != ResultCode.NoError) {
                    result = "";
                    return;
                }
            }

            delay(200, CHECK);
            // delay(150, CHECK_RESULT_S);
        }
    }


    private void readTagOfTid() {

        if (!isStop) {
            ResultCode res;
            checkCount = 0;
            BankType bank = BankType.valueOf(BankType.TID.getValue());
            if (mReader.getAction() == ActionState.Stop) {
                if ((res = mReader.readMemory6c(bank, 0, ReadLength, "")) != ResultCode.NoError) {
                    result = "";
                    return;
                }
            }

            delay(200, CHECK);
            // delay(150, CHECK_RESULT);
        }
    }

    /**
     * 延时执行命令
     *
     * @param time   延时的时间
     * @param action 需要执行的命令
     */
    private void delay(final int time, final int action) {
        handReader.sendEmptyMessageDelayed(action, time);
    }

    /**
     * 检测读写器的状态
     */
    private void checkReader() {

        System.out.println("chekc_father");
        checkCount++;
        if (mReader.getAction() != ActionState.Stop) {
            if (checkCount == 1) {
                // Toast.makeText(this, "关闭", 0).show();
                delay(100, STOP);
            } else if (checkCount == 2) {
                // Toast.makeText(this, "重启", 0).show();
                delay(300, RESTART);
            }
        } else {
            // 读写器初始化成功
            // Toast.makeText(this, "设备已经就绪", 0).show();
            // changeButton();
        }
    }

    /**
     * 读写器关闭
     */
    private void stop() {
        mReader.stop();
        // handReader.sendEmptyMessage(CHECK);
        delay(100, CHECK);
    }

    /**
     * 重启RFID模块
     */
    private void reStart() {
        restartMobile();
        handReader.sendEmptyMessage(CHECK);
    }

    /**
     * 检测结果是否为空
     */
    private void checkReasult() {
        if (mReader.getAction() != ActionState.Stop) {
            mReader.stop();
            listenser.onNullReasult();
        }
    }

    /**
     * 检测第一次结果
     */
    private void resultCheckFirst() {

        if (!hasFirstReasult) {
            listenser.onNullReasult();
        }
    }

    /**
     * 检测第二次结果
     */
    private void resultCheckSecond() {

        if (!hasSecondReasult) {
            listenser.onNullReasult();
        }
    }

    public interface ReadTagListenser {

        public void onNullReasult();

        public void onNoReady();

        public void onReaderResult(String data, String epc, ResultCode code);

        public void onReaderStateChanged(ConnectionState arg1);

        public void onReaderReadTag(String arg1, float arg2);

        public void onReaderActionChanged(ActionState arg1);
    }
}
