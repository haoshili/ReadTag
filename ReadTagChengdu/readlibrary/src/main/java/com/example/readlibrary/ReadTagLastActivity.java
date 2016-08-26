package com.example.readlibrary;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.atid.lib.dev.ATRfidManager;
import com.atid.lib.dev.ATRfidReader;
import com.atid.lib.dev.event.RfidReaderEventListener;
import com.atid.lib.dev.rfid.exception.ATRfidReaderException;
import com.atid.lib.dev.rfid.param.LockParam;
import com.atid.lib.dev.rfid.type.ActionState;
import com.atid.lib.dev.rfid.type.BankType;
import com.atid.lib.dev.rfid.type.ConnectionState;
import com.atid.lib.dev.rfid.type.LockType;
import com.atid.lib.dev.rfid.type.ResultCode;
import com.atid.lib.media.SoundPlayer;


/**
 * User: haoshengjun(872860796@qq.com)
 * Date: 2016-06-29
 * Time: 15:49
 */
public class ReadTagLastActivity extends BaseActivity implements RfidReaderEventListener {

    public ATRfidReader mReader = null;
    public boolean hasReasult = false;

    private String TAG = "ReadTidActivity+test";
    private int OUT_TIME = 1000;
    private SoundPlayer mSound;

    /**
     * 设定标签密
     */
    private static final int OFFSET_KILL_PASSWORD = 0;
    private static final int OFFSET_ACCESS_PASSWORD = 2;

    /**
     * 标签操作类别是否为读取标签
     */
    private boolean isRead;

    /***
     * 错误信息处理
     */
    private static int ERR_NOREADY = 1;
    private static String ERR_NOREADY_MSG = "读写器初始化失败";

    private static int ERR_NOTAG = 2;
    private static String ERR_NOTAG_MSG = "未感应到标签";

    private static int ERR_BUZY = 3;
    private static String ERR_BUZY_MSG = "读写器繁忙";

    private static int ERR_PWDERR = 4;
    private static String ERR_PWDERR_MSG = "读取epc密码错误";

    private static int ERR_MEMERY_OUT = 5;
    private static String ERR_MEMERY_OUT_MSG = "读取标签超出了位置";

    /**
     * 锁定标签的区域
     */
    public static int LOCK_KILL_PASSWORD = 0;
    public static int LOCK_ACCESS_PASSWORD = 1;
    public static int LOCK_EPC = 2;
    public static int LOCK_TID = 3;
    public static int LOCK_USER = 4;
    public static int LOCK_NO = 5;

    /**
     * 读取标签监听者
     */
    private ReadTagDataListenser readTagistenser;

    /**
     * 写入标签监听者
     */
    private WriteTagDataListenser writeTagDataListenser;

    //***************读写器模块初始化*********************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Reader
        if ((mReader = ATRfidManager.getInstance()) == null) {

            Log.e(TAG, "ERROR. onCreate() - Failed to get reader instance");
        }
        mSound = new SoundPlayer(this, R.raw.beep);
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

    //******************数据返回值******************

    @Override
    public void onReaderStateChanged(ATRfidReader atRfidReader, ConnectionState connectionState) {

    }

    @Override
    public void onReaderActionChanged(ATRfidReader atRfidReader, ActionState actionState) {

    }

    @Override
    public void onReaderReadTag(ATRfidReader atRfidReader, String s, float v) {

    }

    @Override
    public void onReaderResult(ATRfidReader atRfidReader, ResultCode resultCode, ActionState
            actionState, String epc, String data) {

        if (epc.trim().length() > 0) {
            hasReasult = true;
            handler.removeMessages(CHECK_REASULT);
            beepSuccess();
        }

        handleReasultData(resultCode, epc, data);
    }

    /**
     * 读取到标签之后的声音，如果想改变声音
     * <p>
     * 1. 将声音的资源文件放到raw中，将原来的资源文件覆盖即可。
     */
    private void beepSuccess() {
        mSound.play();
    }

    //******************对外公布的方法******************

    /**
     * 采用默认的设置读取标签
     * <p>
     * 该方法不再对外提供，用户只需要
     * <p>
     * 1.默认超时时间800ms
     * <p>
     * 2.默认的失败重读次数3次
     *
     * @return
     */
    private boolean readTid() {
        isRead = true;
        showLog("读取标签TID#####");
        bank = BankType.valueOf(BankType.TID.getValue());
        return excuteReader();
    }


    public void readTid(ReadTagDataListenser listenser) {
        readTid(6, listenser);
    }

    /**
     * 读取标签Tid
     *
     * @param length    读取标签的长度
     * @param listenser
     */
    public void readTid(int length, ReadTagDataListenser listenser) {
        READLENTH = length;
        this.readTagistenser = listenser;
        readTid();
    }

    /**
     * 读取epc
     * <p>
     * 该方法被废弃
     *
     * @return
     */
    private boolean readEpc() {
        isRead = true;
        showLog("读取标签EPC####");
        bank = BankType.valueOf(BankType.EPC.getValue());
        return excuteReader();
    }



    /**
     * 读取用户数据区，默认读取24位，从0开始
     *
     * @return
     */
    private boolean readUser() {
        isRead = true;
        showLog("读取标签User数据###");
        bank = BankType.valueOf(BankType.User.getValue());
        return excuteReader();


    }


    /**
     * 读取用户数据区域长度
     *
     * @param readLength 用户读取的长度
     * @return
     */
    private boolean readUser(int readLength) {
        isRead = true;
        this.READLENTH = readLength;
        showLog("读取标签User数据###");
        bank = BankType.valueOf(BankType.User.getValue());
        return excuteReader();
    }

    public void readUser(int readLength, ReadTagDataListenser listenser) {
        this.readTagistenser = listenser;
        readUser(readLength);
    }


    /**
     * 带密码读取epc
     *
     * @param pwd 密码
     * @return
     */
    private boolean readEpc(String pwd) {
        isRead = true;
        CURE_TIMES = 0;
        showLog("带密码读取EPC*****密码" + pwd);
        this.pwd = pwd;
        bank = BankType.valueOf(BankType.EPC.getValue());
        return excuteReader();
    }

    /**
     * 读取epc数据
     *
     * @param listenser
     */
    public void readEpc(ReadTagDataListenser listenser) {
        isRead = true;
        this.readTagistenser = listenser;
        readEpc();
    }

    public void readEpc(String pwd, ReadTagDataListenser listenser) {
        this.readTagistenser = listenser;
        readEpc(pwd);
    }



    /**
     * 写入epc数据
     *
     * @param offset
     * @param data
     * @param pwd
     * @return
     */
    private boolean writeEpc(int offset, String data, String pwd) {
        isRead = false;
        CURE_TIMES = 0;
        showLog("将数据写入epc" + offset + "data" + data + "pwd" + pwd);
        bank = BankType.valueOf(BankType.EPC.getValue());
        return excuteWriteAccess(offset, data, pwd);
    }

    /**
     * 写入数据到epc，不带密码
     *
     * @param offset
     * @param data
     * @param pwd
     * @param listenser
     */
    public void writeEpc(int offset, String data, String pwd, WriteTagDataListenser listenser) {
        this.writeTagDataListenser = listenser;
        writeEpc(offset, data, pwd);
    }

    public void writeEpc(int offset, String data, WriteTagDataListenser listenser) {
        writeEpc(offset, data, "", listenser);
    }

    public void writeEpc(String data, WriteTagDataListenser listenser) {
        writeEpc(1, data, "", listenser);
    }



    /**
     * 写入user数据
     *
     * @param offset
     * @param data
     * @param pwd
     * @return
     */
    private boolean writeUser(int offset, String data, String pwd) {
        isRead = false;
        CURE_TIMES = 0;
        showLog("将数据写入epc" + offset + "data" + data + "pwd" + pwd);
        bank = BankType.valueOf(BankType.User.getValue());
        return excuteWriteAccess(offset, data, pwd);
    }

    public void writeUser(int offset, String data, String pwd, WriteTagDataListenser listenser) {
        this.writeTagDataListenser = listenser;
        writeUser(offset, data, pwd);
    }

    public void writeUser(int offset, String data, WriteTagDataListenser listenser) {
        writeUser(offset, data, "", listenser);
    }

    /**
     * 写入user重载方法，从第0位开始写入
     *
     * @param data
     * @param listenser
     */
    public void writeUser(String data, WriteTagDataListenser listenser) {
        writeUser(0, data, "", listenser);
    }


    /**
     * 设置访问密码
     * <p>
     * 该功能用处的详细信息请参考功能说明文档。
     *
     * @param pwd
     * @param pwdLock
     * @param listenser
     */
    public void writeSetAccessPwd(String pwd, String pwdLock, WriteTagDataListenser listenser) {

        writeTagDataListenser = listenser;

        isRead = false;
        bank = BankType.Reserved;
        excuetSetPassWord(pwd, pwdLock);
    }


    /**
     * 锁定标签
     *
     * @param datas
     */
    public void writeLockData(int[] datas, String pwd, WriteTagDataListenser listenser) {
        isRead = false;
        this.writeTagDataListenser = listenser;
        excuteWriteLockData(getLockParamFromData(datas), pwd);
    }

    /**
     * 解锁标签
     *
     * @param datas
     * @param pwd
     * @param listenser
     */
    public void writeUnLockData(int[] datas, String pwd, WriteTagDataListenser listenser) {

        isRead = false;
        this.writeTagDataListenser = listenser;
        excuteWriteLockData(getUnLockParam(datas), pwd);
    }


    /**
     * 得到锁定的数据结果级
     *
     * @param datas
     * @return
     */
    private LockParam getLockParamFromData(int[] datas) {

        boolean[] reasult = new boolean[5];
        for (int i = 0; i < 5; i++) {
            if (checkHasData(datas, i)) {
                showLog("tag*******锁定标签的参数所在位置******" + i);

                reasult[i] = true;
            } else {
                reasult[i] = false;
            }
        }
        LockParam lockParam = new LockParam(
                reasult[0] ? LockType.PermaLock : LockType.NoChange
                , reasult[1] ? LockType.PermaLock : LockType.NoChange,
                reasult[2] ? LockType.PermaLock : LockType.NoChange,
                reasult[3] ? LockType.PermaLock : LockType.NoChange,
                reasult[4] ? LockType.PermaLock : LockType.NoChange);
        return lockParam;

    }

    /**
     * 解锁标签
     *
     * @param datas
     * @return
     */
    private LockParam getUnLockParam(int[] datas) {

        boolean[] reasult = new boolean[5];
        for (int i = 0; i < 5; i++) {
            if (checkHasData(datas, i)) {
                showLog("tag*******锁定标签的参数所在位置******" + i);
                reasult[i] = true;
            } else {
                reasult[i] = false;
            }
        }
        LockParam lockParam = new LockParam(
                reasult[0] ? LockType.Unlock : LockType.NoChange
                , reasult[1] ? LockType.Unlock : LockType.NoChange,
                reasult[2] ? LockType.Unlock : LockType.NoChange,
                reasult[3] ? LockType.Unlock : LockType.NoChange,
                reasult[4] ? LockType.Unlock : LockType.NoChange);
        return lockParam;

    }

    private boolean checkHasData(int[] datas, int i) {
        for (int j : datas) {
            if (j == i) {
                return true;
            }
        }
        return false;
    }

    //******************配置方法******************


    /**
     * 设置失败重读次数
     * <p>
     * 该功能主要针对带密码读取epc的
     *
     * @param times 失败重读次数
     */
    public void setReadTimes(int times) {
        FAILED_TRY = times;
    }

    /**
     * 设置读取标签的超时时间
     * <p>
     * 在标签读取的过程中，如果读不到标签，读写器会一直工作，设置超时间提示用户更换标签或者距离标签近一些等操作
     *
     * @param time 超时时间，默认时间未1000ms，单位为ms
     */
    public void setOutReadTime(int time) {
        if (time > OUT_TIME) {
            this.OUT_TIME = time;
        }
    }

    /**
     * 设置读取标签Tid的长度
     *
     * @param is128Lengths 是否是128位的标签
     */
    public void setReadTidLendth(boolean is128Lengths) {

        if (is128Lengths) {
            READLENTH = 8;
            showLog("设置读取tid的长度" + READLENTH);
        } else {
            READLENTH = 6;
            showLog("设置读取tid的长度" + READLENTH);
        }
    }

    public void setReadLength(int readLength) {
        READLENTH = readLength;
        showLog("设置读取tid的长度" + readLength);
    }

    //*******************私有工作方法**********************

    private BankType bank;
    private String pwd;

    /**
     * 128位的标签是8,96位标签的长度是6.默认情况先读取128，如果超出该标签长度，会设置为6
     **/
    private int READLENTH = 6;


    private boolean excuteReader() {

        //将该字段设置为false；
        hasReasult = false;

        if (mReader == null) {

            //未成功初始化
            onFailed(ERR_NOREADY_MSG, ERR_NOREADY);
            return false;

        } else {
            ResultCode res;
            if (mReader.getAction() == ActionState.Stop) {
                if ((res = mReader.readMemory6c(bank, 0, READLENTH, pwd)) == ResultCode.NoError) {
                    handler.sendEmptyMessageDelayed(CHECK_REASULT, OUT_TIME);
                    return true;
                } else {
                    showLog("第一次读取失败，进行重新读取");
                    return true;
                }
            } else {

                //读写器繁忙
                onFailed(ERR_BUZY_MSG, ERR_BUZY);
                return false;
            }

        }
    }

    private boolean excuteWriteAccess(int offset, String data, String pwd) {

        //将该字段设置为false；
        hasReasult = false;

        if (mReader == null) {
            showLog("读写器没有准备好");
            return false;
        } else {
            ResultCode res;
            if (mReader.getAction() == ActionState.Stop) {

                if ((res = mReader.writeMemory6c(bank, offset, data, pwd)) != ResultCode.NoError) {
                    handler.sendEmptyMessageDelayed(CHECK_REASULT, OUT_TIME);
                    return true;
                } else {
                    showLog("没有读取成功");
                    return false;
                }
            } else {
                showLog("读写器繁忙");
                onFailed(ERR_BUZY_MSG, ERR_BUZY);
                return false;
            }

        }
    }

    /**
     * 执行锁定标签
     *
     * @param param
     * @param password
     * @return
     */
    private boolean excuteWriteLockData(LockParam param, String password) {

        //将该字段设置为false；
        hasReasult = false;

        if (mReader == null) {
            showLog("读写器没有准备好");
            return false;
        } else {
            ResultCode res;

            if ((res = mReader.lock6c(param, password)) != ResultCode.NoError) {
                handler.sendEmptyMessageDelayed(CHECK_REASULT, OUT_TIME);
                return true;
            } else {

                return false;
            }
        }
    }


    /**
     * 对返回的数据结果进行处理
     * <p>
     * 1.读取tid时候，位数的判断
     * 2.判断返回结果是否为空
     *
     * @param resultCode 返回的数据结果代码
     * @param epc        每次读取标签都会返回epc数据
     * @param data       请求读取的目标数据
     */
    private void handleReasultData(ResultCode resultCode, String epc, String data) {
        showLog("显示结果data" + "*****" + data + "***epc***" + epc + "***&&&code&&&***" + resultCode.toString());

        if (isRead) {

            //返回结果，关闭结果查询系统
            if (bank == BankType.valueOf(BankType.TID.getValue())) {
                handlTidData(resultCode, epc, data);
            } else if (bank == BankType.valueOf(BankType.User.getValue())) {
                handUserData(resultCode, epc, data);
            } else {
                handlEpcData(resultCode, epc, data);
            }
        } else {

            //是对数据进行写入，
            if (bank == BankType.valueOf(BankType.User.getValue())) {
                handlerWriteUserReasult(resultCode, epc, data);
            } else if (bank == BankType.Reserved) {
                handlerSetAccessPwdReasult(resultCode, epc, data);
            } else if (bank == BankType.EPC) {
                handleWriteEpcReasult(resultCode, epc, data);
            } else {
                handWriteLockData(resultCode, epc, data);
            }

        }
    }


    /**
     * 设置标签的访问密码
     *
     * @param pwdSet  老的密码
     * @param pwdLock 新的密码
     * @return
     */
    private boolean excuetSetPassWord(String pwdSet, String pwdLock) {

        //将该字段设置为false；
        hasReasult = false;

        if (mReader == null) {

            //未成功初始化
            onFailed(ERR_NOREADY_MSG, ERR_NOREADY);
            return false;

        } else {
            ResultCode res;

            if ((res = mReader.writeMemory6c(bank, OFFSET_ACCESS_PASSWORD, pwdSet,
                    pwdLock)) != ResultCode.NoError) {

                return true;
            } else {
                //读写器繁忙,这个方法和其他的有些不同，刚开始会执行进入这块代码。
//                onFailed( ERR_BUZY_MSG, ERR_BUZY);
                return false;
            }

        }
    }


    /**
     * 处理写入epc数据的结果
     *
     * @param resultCode
     * @param epc
     * @param data
     */
    private void handleWriteEpcReasult(ResultCode resultCode, String epc, String data) {
        showLog("处理epc数据" + resultCode.toString());

        if (resultCode == ResultCode.NoError) {
            onSuccess("null");
        } else {
            onFailed(resultCode.toString(), resultCode.getCode());
        }
    }

    /**
     * 处理写入user数据区的结果
     *
     * @param resultCode
     * @param epc
     * @param data
     */
    private void handlerWriteUserReasult(ResultCode resultCode, String epc, String data) {
        showLog("处理user数据" + resultCode.toString());

        if (resultCode == ResultCode.NoError) {
            onSuccess("null");
        } else {
            onFailed(resultCode.toString(), resultCode.getCode());
        }
    }


    /**
     * 锁定标签的各个区域
     */
    private void handWriteLockData(ResultCode resultCode, String epc, String data) {

        if (resultCode == ResultCode.NoError) {
            onSuccess("null");
        } else {

            if (resultCode == ResultCode.MemoryOverrun) {
                onFailed(ERR_MEMERY_OUT_MSG, ERR_MEMERY_OUT);
            } else {
                onFailed("未处理的错误" + resultCode.toString(), resultCode.getCode());
            }
        }
    }


    /**
     * 处理设置访问密码的数据结果
     * <p>
     * 现在还没有对一些错误结果进行二次处理，有的还需要进行二次处理一下。
     *
     * @param resultCode
     * @param epc
     * @param data
     */
    private void handlerSetAccessPwdReasult(ResultCode resultCode, String epc, String data) {
        showLog("处理user数据" + resultCode.toString());

        if (resultCode == ResultCode.NoError) {
            onSuccess("null");
        } else {
            onFailed(resultCode.toString(), resultCode.getCode());
        }
    }


    /**
     * 处理返回的tid数据
     */
    private void handlTidData(ResultCode resultCode, String epc, String data) {
        handler.removeMessages(CHECK_REASULT);
        if (resultCode == ResultCode.MemoryOverrun) {
            //设置读取长度，重新读取tid，开启结果检测程序
            setReadTidLendth(false);
            handler.sendEmptyMessageDelayed(READ_TAG, 300);
            handler.sendEmptyMessageDelayed(CHECK_REASULT, 300 + OUT_TIME);
        } else {
            onSuccess(data);
        }
    }

    private int FAILED_TRY = 3;
    private int CURE_TIMES = 0;

    /**
     * 处理epc数据
     */
    private void handlEpcData(ResultCode resultCode, String epc, String data) {
        handler.removeMessages(CHECK_REASULT);

        //读取密码错误会出现该错误码，需要进行再次封装
        if (resultCode == ResultCode.OutOfRetries) {

            //如果出现错误，会进行多次读取，来确保得到的结果是正确的。可以通过设置FAILED_TRY的值来实现重读次数,默认设置3次
            if (CURE_TIMES >= FAILED_TRY) {

                onFailed(ERR_PWDERR_MSG, ERR_PWDERR);

            } else {

                handler.sendEmptyMessageDelayed(READ_TAG, 300);
                handler.sendEmptyMessageDelayed(CHECK_REASULT, 300 + OUT_TIME);
                CURE_TIMES++;
                showLog(FAILED_TRY + " ============未读取到结果===============" + CURE_TIMES);

            }
        } else {

            //获取到结果，返回数据
            onSuccess(data);
        }
    }

    /**
     * 处理user数据
     */
    private void handUserData(ResultCode resultCode, String epc, String data) {

        if (resultCode == ResultCode.NoError) {
            onSuccess(data);
        } else {

            if (resultCode == ResultCode.MemoryOverrun) {
                onFailed(ERR_MEMERY_OUT_MSG, ERR_MEMERY_OUT);
            } else {
                onFailed("未处理的错误" + resultCode.toString(), resultCode.getCode());
            }

        }
    }


    //*****************************Hanlder处理程序*******************************************

    private final static int READ_TAG = 1;
    private final static int CHECK_REASULT = 2;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case READ_TAG:
                    excuteReader();
                    break;

                case CHECK_REASULT:
                    checkReasult();
                default:
                    break;
            }
        }
    };

    /**
     * 延时检测是否读取到结果
     */
    private void checkReasult() {

        if (!hasReasult) {
            mReader.stop();

            //在这里不需要进行二次读取，因为读取标签采用的是一直读取这样的方式，如果经常出现未反应到标签这样的情况，可以考虑使用
            //把超时时间设置的更长一些，这样就可以了，
            onFailed(ERR_NOTAG_MSG, ERR_NOTAG);
        }

    }

    //*****************************结果通知**********************************************


    /**
     * 读写器成功调用
     *
     * @param data
     */
    private void onSuccess(String data) {
        if (data.equals("null")) {
            //如果为空，调用写标签的回调
            if (writeTagDataListenser != null) {
                writeTagDataListenser.onSuccess();
            }

        } else {
            //如果不为空，使用读标签的回调
            if (readTagistenser != null) {
                readTagistenser.onSuccess(data);
            }
        }
    }

    /**
     * 读写器失败调用
     */
    private void onFailed(String msg, int errorCode) {

        if (isRead) {
            if (readTagistenser != null) {

                readTagistenser.onFailed(msg, errorCode);
            }
        } else {
            if (writeTagDataListenser != null) {
                writeTagDataListenser.onFailed(msg, errorCode);
            }
        }
    }

    //*****************************帮助方法***********************************************

    public void showLog(String message) {
        //如果不想打印log，直接注释
        Log.i(TAG, message);
    }


    /**
     * 读取标签的返回借口
     */
    public interface ReadTagDataListenser {
        public void onSuccess(String data);

        public void onFailed(String msg, int errorCode);
    }

    /**
     * 写入标签的接口
     */
    public interface WriteTagDataListenser {
        public void onSuccess();

        public void onFailed(String msg, int errorCode);
    }

}