package com.example.obdongle.base;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;


/**
 * 基础Act
 * Created by adolf_dong on 2016/5/27.
 */
public abstract class BaseAct extends FragmentActivity {

    private Toast mToast;
    private ProgressDialog progressDialog;
    public static final int ONE = 1;
    public static final int TWO = 2;
    /** 标示是否在显示提示 */
    private boolean mIsTip = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void showToat(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(content);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    /**
     * ProgressDialog交互
     *
     * @param title   标题
     * @param message 内容
     * @param cancel  是否可被取消
     */
    public void showProgressDialog(String title, String message, boolean cancel) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(cancel);
        progressDialog.show();
    }

    /**
     * 可点击dialog
     *
     * @param title           标题
     * @param message         显示信息
     * @param cancel          是否可按返回撤销
     * @param btnStr          按钮显示文字
     * @param onClickListener 按钮点击事件
     */
    public void showProgressDialogCanClick(String title, String message, boolean cancel, String btnStr, DialogInterface.OnClickListener onClickListener) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, btnStr, onClickListener);
        progressDialog.setCancelable(cancel);
        progressDialog.show();
    }

    /**
     * 改变ProgressDialog展示的内容
     *
     * @param message 想要改变的内容
     */
    public void setDialogMessage(String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
        }
    }

    /**
     * 设置dialog是否可被点击取消
     *
     * @param cancelAble
     */
    public void setDialogCancelAble(boolean cancelAble) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setCancelable(cancelAble);
        }
    }

    /**
     * 取消当前展示的ProgressDialog
     */
    public void disMissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    /**
     * alert点击事件监听器
     */
    public interface SimpleDialogLSN {
        /**
         * 确认按钮点击执行
         */
        void pOnClick();

        /**
         * 取消按钮点击执行
         */
        void nOnClick();
    }

    /**
     * 显示一般的alertdialog
     *
     * @param mode            ONE,单个按钮，TWO,两个按钮
     * @param title           标题
     * @param msg             显示信息
     * @param pString         确认按钮文字
     * @param nString         取消按钮文字
     * @param simpleDialogLSN 确认和取消的两个点击事件
     */
    public void showSimpleDialog(int mode, String title, String msg, String pString, String nString, final SimpleDialogLSN simpleDialogLSN) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(msg);
        switch (mode) {
            case TWO:
                alert.setNegativeButton(nString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (simpleDialogLSN!=null) {
                            simpleDialogLSN.nOnClick();
                        }
                    }
                });
            case ONE:
                alert.setPositiveButton(pString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (simpleDialogLSN!=null) {
                            simpleDialogLSN.pOnClick();
                        }
                    }
                });
                break;
        }
        alert.show();
    }


    public interface ItemDialogLSN {
        void onItemDialogClick(int which);
    }

    /**
     * 显示Item类型alertdialog
     *
     * @param title         标题
     * @param items         显示数据String[]
     * @param itemDialogLSN 点击事件
     */
    public void showItemDialog(String title, String[] items, final ItemDialogLSN itemDialogLSN) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                itemDialogLSN.onItemDialogClick(which);
            }
        });
        alert.show();
    }


}
