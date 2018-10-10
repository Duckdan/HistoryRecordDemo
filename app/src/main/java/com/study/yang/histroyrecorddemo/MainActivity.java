package com.study.yang.histroyrecorddemo;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TABLENAME = "user";
    /**
     * 必须添加if not exists,否则第二次进入的时候会报该表已经创建的提示
     */
    private static final String CREATETABLE = "CREATE TABLE if not exists " + TABLENAME +
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT,account TEXT,pwd TEXT,createdTime" +
            " TimeStamp NOT NULL DEFAULT (datetime('now','localtime')))";


    private EditText etCode;
    private EditText etAccount;
    private EditText etPwd;
    private AlertDialog helpDialog;
    private CheckBox cbChecked;

    private List<HotspotUserBean> hubs = null;
    private ListPopupWindow lpw;
    private AccountAdapter accountAdapter;

    private ColorDrawable dividerDrawable = new ColorDrawable(Color.GRAY);
    private ImageButton ibRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvSkipLogin = (TextView) findViewById(R.id.tv_skip_login);
        etCode = (EditText) findViewById(R.id.et_code);
        ImageButton ibHelp = (ImageButton) findViewById(R.id.ib_help);
        etAccount = (EditText) findViewById(R.id.et_account);
        FrameLayout flRecord = (FrameLayout) findViewById(R.id.fl_record);
        ibRecord = (ImageButton) findViewById(R.id.ib_record);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        cbChecked = (CheckBox) findViewById(R.id.cb_checked);
        TextView tvLogin = (TextView) findViewById(R.id.tv_login);

        tvSkipLogin.setOnClickListener(this);
        ibHelp.setOnClickListener(this);
        flRecord.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        createTable();
    }

    /**
     * 创建表
     */
    private void createTable() {
        SQLiteDatabase userDB = openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        userDB.execSQL(CREATETABLE);
        userDB.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_skip_login:
                onBackPressed();
                break;
            case R.id.ib_help:
                showHelpDialog();
                break;
            case R.id.fl_record:
                showHistoryRecord();
                break;
            case R.id.tv_login:
                login();
                break;
        }
    }

    /**
     * 显示帮助对话框
     */
    private void showHelpDialog() {
        if (helpDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog);
            View inflate = LayoutInflater.from(this).inflate(R.layout.login_help_layout, null);
            inflate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    helpDialog.dismiss();
                }
            });
            helpDialog = builder.create();
            helpDialog.show();
            Window window = helpDialog.getWindow();
            window.setContentView(inflate);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            int widthPixels = getResources().getDisplayMetrics().widthPixels;
            lp.width = (int) (widthPixels * 0.8);
            window.setAttributes(lp);
        } else {
            helpDialog.show();
        }
    }

    /**
     * 显示历史记录
     */
    private void showHistoryRecord() {
        queryData();
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etAccount.getWindowToken(), 0, null);
        if (lpw == null) {
            lpw = new ListPopupWindow(this);
            lpw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HotspotUserBean hub = hubs.get(position);
                    String code = hub.getCode();
                    etCode.setText(code);
                    etCode.setSelection(code.length());
                    String account = hub.getAccount();
                    etAccount.setText(account);
                    etAccount.setSelection(account.length());
                    String pwd = hub.getPwd();
                    if (!TextUtils.isEmpty(pwd)) {
                        etPwd.setText(pwd);
                        etPwd.setSelection(pwd.length());
                        cbChecked.setChecked(true);
                    }
                    dismiss();
                }
            });
            accountAdapter = new AccountAdapter(this, hubs);
            ColorDrawable backgroundDrawable = new ColorDrawable(Color.WHITE);
            lpw.setBackgroundDrawable(backgroundDrawable);
            lpw.setAdapter(accountAdapter);
            lpw.setWidth(etAccount.getWidth());
            //设置lpw锚点，表示lpw跟随哪一个控件联系在一起，必须添加该方法否则会报错
            lpw.setAnchorView(etAccount);
            lpw.setDropDownGravity(Gravity.BOTTOM);
            lpw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    ibRecord.setBackgroundResource(R.drawable.denglu_xia);
                }
            });
        } else {
            //当数据发生改变的时候刷新适配器
            accountAdapter.notifyDataSetChanged();
        }
        //控制提示框的高度
        if (hubs != null && hubs.size() > 5) {
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            lpw.setHeight((int) (0.2 * heightPixels));
        } else {
            lpw.setHeight(ListPopupWindow.WRAP_CONTENT);
        }
        lpw.show();
        if (lpw.isShowing()) {
            ibRecord.setBackgroundResource(R.drawable.denglu_shang);
        }
        //必须添加在show()方法之后，否则getListView()方法获取不到ListView实例
        ListView listView = lpw.getListView();
        listView.setDivider(dividerDrawable);
        listView.setDividerHeight(2);
    }


    private void dismiss() {
        lpw.dismiss();
    }

    /**
     * 查询数据
     */
    private void queryData() {
        SQLiteDatabase userDB = openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        Cursor cursor = userDB.rawQuery("select * FROM " + TABLENAME + " order by createdTime desc", null);
        if (cursor != null && cursor.getCount() > 0) {
            if (hubs == null) {   //首次查询时
                hubs = new ArrayList<>();
            } else {   //继续查询
                hubs.clear();
            }
            cursor.moveToFirst();
            //循环读取数据
            while (!cursor.isAfterLast()) {
                //获得当前行的标签
                int codeIndex = cursor.getColumnIndex("code");
                //获得商户代码
                String code = cursor.getString(codeIndex);

                int accountIndex = cursor.getColumnIndex("account");
                //获取账号
                String account = cursor.getString(accountIndex);

                int pwdIndex = cursor.getColumnIndex("pwd");
                String pwd = cursor.getString(pwdIndex);
                HotspotUserBean hub = new HotspotUserBean();

                hub.setAccount(account);
                hub.setCode(code);
                hub.setPwd(pwd);

                hubs.add(hub);
                //游标移到下一行
                cursor.moveToNext();
            }
            cursor.close();
        }
        userDB.close();
    }


    /**
     * 登录
     */
    private void login() {
        String code = etCode.getText().toString().trim();
        String account = etAccount.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "商户代码不允许为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "账号不允许为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "密码不允许为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String result = code + "#" + account + "#" + pwd;
        //TODO 处理登录
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        insertOrUpdateData(code, account, cbChecked.isChecked() ? pwd : "");
    }

    /**
     * 插入数据
     *
     * @param code
     * @param account
     * @param pwd
     */
    private void insertOrUpdateData(String code, String account, String pwd) {
        SQLiteDatabase userDB = openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        Cursor cursor = userDB.rawQuery("select * FROM " + TABLENAME + " where account=" + account, null);

        ContentValues cv = new ContentValues();
        cv.put("code", code);
        cv.put("account", account);
        cv.put("pwd", pwd);

        if (cursor != null && cursor.getCount() > 0) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
            cv.put("createdTime", date);
            userDB.update(TABLENAME, cv, "account=?", new String[]{account});
        } else {
            userDB.insert(TABLENAME, null, cv);
        }
        userDB.close();
    }

    /**
     * 删除数据
     *
     * @param hub
     */
    public void deleteData(HotspotUserBean hub) {
        SQLiteDatabase userDB = openOrCreateDatabase("user.db", MODE_PRIVATE, null);
        userDB.delete(TABLENAME, "account=?", new String[]{hub.getAccount()});
        userDB.close();
        //数据完全清理完成之后，ListPopupWindow消失
        if (hubs != null && hubs.size() == 0) {
            dismiss();
        }
    }
}
