package com.example.obdongle.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.example.obdongle.R;
import com.example.obdongle.constant.OBConstant;


@SuppressLint("NewApi")
public class ColorLampSettingPanel extends ViewGroup {
    private Bitmap mBitmap;
    private Point mPoint = new Point();
    private Bitmap yBitmap;
    private long oldTime = 0;
    private long mTime;
    private long minTime;


    public ColorLampSettingPanel(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public ColorLampSettingPanel(Context context) {
        super(context);
    }

    /**
     * 一般执行两个参数的构造方法
     */
    public ColorLampSettingPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 节点类型
     */
    private int type;

    /**
     * 设置页面show状态
     */
    public void setView(int type, int cool, int red, int green, int blue, long excutTime) {
        this.minTime = excutTime;
        this.type = type;
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pick_color_point).copy(Bitmap.Config.ARGB_8888, true);
        }
        switch (type) {
            case OBConstant.NodeType.IS_COLOR_LAMP:
                yBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.color_panel).copy(Bitmap.Config.ARGB_8888, true);
                break;
            case OBConstant.NodeType.IS_WARM_LAMP:
                yBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.warm_panel).copy(Bitmap.Config.ARGB_8888, true);
                break;
            case OBConstant.NodeType.IS_SIMPLE_LAMP:
                yBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.simple_panel).copy(Bitmap.Config.ARGB_8888, true);
                break;
        }
        //noinspection deprecation
        setBackgroundDrawable(new BitmapDrawable(yBitmap));
        initPoint(cool, red, green, blue);
    }


    /**
     * 重定位触点坐标
     */
    private void initPoint(int cool, int red, int green, int blue) {
        switch (type) {
            case OBConstant.NodeType.IS_SIMPLE_LAMP:
                int y1;
                int x1;
                y1 = TransDPPXUtil.getScreenHeight(getContext()) / 2 - yBitmap.getHeight() / 2;
                x1 = TransDPPXUtil.getScreenWidth(getContext()) / 2 - yBitmap.getWidth() / 2;
                mPoint.set(x1, y1);
                break;
            case OBConstant.NodeType.IS_WARM_LAMP:
                int x;
                int y;
                if (cool <= 255 && cool > 234) {
                    x = 114 - mBitmap.getWidth() / 2;
                    y = 68 - mBitmap.getHeight() / 2;
                } else if (cool <= 234 && cool > 213) {
                    x = 180 - mBitmap.getWidth() / 2;
                    y = 62 - mBitmap.getHeight() / 2;
                } else if (cool <= 213 && cool > 192) {
                    x = 240 - mBitmap.getWidth() / 2;
                    y = 75 - mBitmap.getHeight() / 2;
                } else if (cool <= 192 && cool > 171) {
                    x = 290 - mBitmap.getWidth() / 2;
                    y = 126 - mBitmap.getHeight() / 2;
                } else if (cool <= 171 && cool > 150) {
                    x = 304 - mBitmap.getWidth() / 2;
                    y = 182 - mBitmap.getHeight() / 2;
                } else if (cool <= 150 && cool > 129) {
                    x = 296 - mBitmap.getWidth() / 2;
                    y = 245 - mBitmap.getHeight() / 2;
                } else if (cool <= 129 && cool > 108) {
                    x = 242 - mBitmap.getWidth() / 2;
                    y = 288 - mBitmap.getHeight() / 2;
                } else if (cool <= 108 && cool > 87) {
                    x = 180 - mBitmap.getWidth() / 2;
                    y = 300 - mBitmap.getHeight() / 2;
                } else if (cool <= 87 && cool > 66) {
                    x = 120 - mBitmap.getWidth() / 2;
                    y = 286 - mBitmap.getHeight() / 2;
                } else if (cool <= 66 && cool > 45) {
                    x = 70 - mBitmap.getWidth() / 2;
                    y = 245 - mBitmap.getHeight() / 2;
                } else if (cool <= 45 && cool > 24) {
                    x = 50 - mBitmap.getWidth() / 2;
                    y = 180 - mBitmap.getHeight() / 2;
                } else if (cool <= 24 && cool >= 0) {
                    x = 70 - mBitmap.getWidth() / 2;
                    y = 110 - mBitmap.getHeight() / 2;
                } else {
                    x = getWidth() / 2 - mBitmap.getWidth() / 2;
                    y = getHeight() / 2 - mBitmap.getHeight() / 2;
                }
                mPoint.set(x, y);
                break;
            case OBConstant.NodeType.IS_COLOR_LAMP:
                int xC;
                int yC;
                if (red == 255 && green >= 204 && green <= 255 && blue == 0) {
                    xC = 104 - mBitmap.getWidth() / 2;
                    yC = 76 - mBitmap.getHeight() / 2;
                } else if (red == 255 && green >= 153 && green < 204 && blue == 0) {
                    xC = 121 - mBitmap.getWidth() / 2;
                    yC = 54 - mBitmap.getHeight() / 2;
                } else if (red == 255 && green >= 128 && green < 153 && blue == 0) {
                    xC = 162 - mBitmap.getWidth() / 2;
                    yC = 54 - mBitmap.getHeight() / 2;
                } else if (red == 255 && green >= 103 && green < 128 && blue == 0) {
                    xC = 200 - mBitmap.getWidth() / 2;
                    yC = 58 - mBitmap.getHeight() / 2;
                } else if (red == 255 && green >= 78 && green < 103 && blue == 0) {
                    xC = 231 - mBitmap.getWidth() / 2;
                    yC = 67 - mBitmap.getHeight() / 2;
                } else if (red == 255 && green >= 53 && green < 78 && blue == 0) {
                    xC = 262 - mBitmap.getWidth() / 2;
                    yC = 85 - mBitmap.getHeight() / 2;
                } else if (red == 255 && green >= 28 && green < 53 && blue == 0) {
                    xC = 288 - mBitmap.getWidth() / 2;
                    yC = 96 - mBitmap.getHeight() / 2;
                } else if (red == 255 && green >= 0 && green < 28 && blue == 0) {
                    xC = 302 - mBitmap.getWidth() / 2;
                    yC = 134 - mBitmap.getHeight() / 2;
                } else if (red <= 255 && red > 212 && green == 0 && blue >= 0 && blue < 43) {
                    xC = 311 - mBitmap.getWidth() / 2;
                    yC = 164 - mBitmap.getHeight() / 2;
                } else if (red <= 212 && red > 169 && green == 0 && blue >= 43 && blue < 86) {
                    xC = 310 - mBitmap.getWidth() / 2;
                    yC = 198 - mBitmap.getHeight() / 2;
                } else if (red <= 169 && red > 126 && green == 0 && blue >= 86 && blue < 129) {
                    xC = 306 - mBitmap.getWidth() / 2;
                    yC = 238 - mBitmap.getHeight() / 2;
                } else if (red <= 126 && red > 83 && green == 0 && blue >= 129 && blue < 172) {
                    xC = 289 - mBitmap.getWidth() / 2;
                    yC = 268 - mBitmap.getHeight() / 2;
                } else if (red <= 83 && red > 40 && green == 0 && blue >= 172 && blue < 215) {
                    xC = 268 - mBitmap.getWidth() / 2;
                    yC = 287 - mBitmap.getHeight() / 2;
                } else if (red <= 40 && red >= 0 && green == 0 && blue >= 215 && blue <= 255) {
                    xC = 229 - mBitmap.getWidth() / 2;
                    yC = 302 - mBitmap.getHeight() / 2;
                } else if (red == 0 && green <= 63 && green >= 0 && blue >= 172 && blue <= 255) {
                    xC = 201 - mBitmap.getWidth() / 2;
                    yC = 306 - mBitmap.getHeight() / 2;
                } else if (red == 0 && green <= 127 && green > 63 && blue >= 128 && blue < 172) {
                    xC = 162 - mBitmap.getWidth() / 2;
                    yC = 321 - mBitmap.getHeight() / 2;
                } else if (red == 0 && green <= 191 && green > 127 && blue >= 64 && blue < 128) {
                    xC = 132 - mBitmap.getWidth() / 2;
                    yC = 309 - mBitmap.getHeight() / 2;
                } else if (red == 0 && green <= 255 && green > 191 && blue >= 0 && blue < 64) {
                    xC = 94 - mBitmap.getWidth() / 2;
                    yC = 297 - mBitmap.getHeight() / 2;
                } else if (red >= 0 && red < 45 && green == 255 && blue == 0) {
                    xC = 63 - mBitmap.getWidth() / 2;
                    yC = 271 - mBitmap.getHeight() / 2;
                } else if (red >= 45 && red < 90 && green == 255 && blue == 0) {
                    xC = 54 - mBitmap.getWidth() / 2;
                    yC = 231 - mBitmap.getHeight() / 2;
                } else if (red >= 90 && red < 131 && green == 255 && blue == 0) {
                    xC = 50 - mBitmap.getWidth() / 2;
                    yC = 205 - mBitmap.getHeight() / 2;
                } else if (red >= 131 && red < 172 && green == 255 && blue == 0) {
                    xC = 47 - mBitmap.getWidth() / 2;
                    yC = 167 - mBitmap.getHeight() / 2;
                } else if (red >= 172 && red < 213 && green == 255 && blue == 0) {
                    xC = 57 - mBitmap.getWidth() / 2;
                    yC = 124 - mBitmap.getHeight() / 2;
                } else if (red >= 213 && red < 255 && green == 255 && blue == 0) {
                    xC = 70 - mBitmap.getWidth() / 2;
                    yC = 96 - mBitmap.getHeight() / 2;
                } else {
                    xC = getWidth() / 2 - mBitmap.getWidth() / 2;
                    yC = getHeight() / 2 - mBitmap.getHeight() / 2;
                }
                mPoint.set(xC, yC);
                break;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, mPoint.x, mPoint.y, null);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
    }


    /**
     * 申请父类不阻断事件
     */
    private void attemptClaimDrag() {
        ViewParent mParent = getParent();
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(true);
        }
    }


    //角度取色
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        double distanceX = event.getX() - getWidth() / 2;
        double distanceY = event.getY() - getHeight() / 2;
        double x = Math.pow(distanceX, 2);
        double y = Math.pow(distanceY, 2);
        double distance = Math.sqrt(x + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                attemptClaimDrag();
                break;
            case MotionEvent.ACTION_MOVE:
//                setColor(event, distanceX, distanceY, distance);
                break;
            case MotionEvent.ACTION_UP:
                setColor(event, distanceX, distanceY, distance);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (onColorChangeLsn != null) {
                            onColorChangeLsn.onUp();
                        }
                    }
                }).start();

                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    private void setColor(MotionEvent event, double distanceX, double distanceY, double distance) {
        if (distance < (getWidth() / 2 - mBitmap.getWidth() / 2)) {
            mPoint.set((int) event.getX() - mBitmap.getWidth() / 2, (int) event.getY() - mBitmap.getHeight() / 2);
            mTime = System.currentTimeMillis();
            if (mTime - oldTime > minTime) {
                switch (type) {
                    case OBConstant.NodeType.IS_COLOR_LAMP:
                        setColorLamp(event, distanceX, distanceY, distance);
                        break;
                    case OBConstant.NodeType.IS_WARM_LAMP:
                        setWarmLamp(event, distanceX, distanceY, distance);
                        break;
                }
            }
        }
    }

    /**
     * 设置暖色灯
     */
    private void setWarmLamp(MotionEvent event, double distanceX, double distanceY, double distance) {
        double cool = 0;
        // FIXME: 2016/7/7 暖色不用取值
//        double warm = 0;//第一象限,求角度+45度
        if (event.getX() >= getWidth() / 2 && event.getY() <= getHeight() / 2) {
            cool = 255 - 255 * (45 + Math.toDegrees(Math.asin((Math.abs(distanceX) / distance)))) / 360.0;
        }
        //第二象限，45度分界点，分情况，
        else if (event.getX() < getWidth() / 2 && event.getY() < getHeight() / 2) {
            if (Math.toDegrees(Math.asin(Math.asin((Math.abs(distanceX) / distance)))) <= 45) {
                if (Math.toDegrees(Math.asin(Math.asin((Math.abs(distanceX) / distance)))) >= 15) {
                    cool = 255;
                } else {
                    cool = 255 - 255 * (45 - Math.toDegrees(Math.asin(Math.asin((Math.abs(distanceX) / distance))))) / 360.0;
                }
                //大于45度也分情况获取暖色极限值作用范围
            } else if (Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) > 45) {
                if (Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) < 50) {
                    cool = 0;
                } else {
                    cool = Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) - 45;
                }
            }
        }
        //第三象限，求角度+180度
        else if (event.getX() < getWidth() / 2 && event.getY() > getHeight() / 2) {
            cool = 255 - 255 * (225 + Math.toDegrees(Math.asin((Math.abs(distanceX) / distance)))) / 360.0;
        }
        //第四象限，求角度+90度
        else if (event.getX() > getWidth() / 2 && event.getY() > getHeight() / 2) {
            cool = 255 - 255 * (135 + Math.toDegrees(Math.asin((Math.abs(distanceY) / distance)))) / 360.0;
        }
        oldTime = mTime;
        if (onColorChangeLsn != null) {
            onColorChangeLsn.onWarmChange((int) cool);
        }
    }

    /**
     * 设置彩色灯
     */
    private void setColorLamp(MotionEvent event, double distanceX, double distanceY, double distance) {
        double rColor = 0;
        double gColor = 0;
        double bColor = 0;
        if (distance > 3 * mBitmap.getWidth() / 2) {
            //第一象限
            if (event.getX() >= getWidth() / 2 && event.getY() <= getHeight() / 2) {
                rColor = 255;
                gColor = 153 * Math.toDegrees(Math.asin((Math.abs(distanceY) / distance))) / 90.0;
                bColor = 0;
            }
            //第二象限
            if (event.getX() < getWidth() / 2 && event.getY() < getHeight() / 2) {
                if (Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) < 30) {
                    rColor = 255;
                    gColor = 153.0 + 102 * Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) / 30.0;
                    bColor = 0;
                } else if (Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) > 30) {
                    rColor = 90.0 + 165 * Math.toDegrees(Math.asin((Math.abs(distanceY) / distance))) / 60.0;
                    gColor = 255;
                    bColor = 0;
                }
            }
            //第三象限
            if (event.getX() < getWidth() / 2 && event.getY() > getHeight() / 2) {
                if (Math.toDegrees(Math.asin((Math.abs(distanceY) / distance))) < 30) {
                    rColor = 90.0 - 90 * Math.toDegrees(Math.asin((Math.abs(distanceY) / distance))) / 30.0;
                    gColor = 255;
                    bColor = 0;
                } else if (Math.toDegrees(Math.asin((Math.abs(distanceY) / distance))) > 30) {
                    rColor = 0;
                    gColor = 255 * Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) / 60.0;
                    bColor = 255.0 - gColor;
                }
            }
            //第四象限
            if (event.getX() > getWidth() / 2 && event.getY() > getHeight() / 2) {
                rColor = 255 * Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) / 90.0;
                gColor = 0;
                bColor = 255.0 - 255 * Math.toDegrees(Math.asin((Math.abs(distanceX) / distance))) / 90.0;
            }
        } else {
            rColor = 255;
            gColor = 255;
            bColor = 255;
        }
        oldTime = mTime;
        if (onColorChangeLsn != null) {
            onColorChangeLsn.onColorChange((int) rColor, (int) gColor, (int) bColor);
        }
    }

    OnColorChangeLsn onColorChangeLsn;

    public void setOnColorChangeLsn(OnColorChangeLsn onColorChangeLsn) {
        this.onColorChangeLsn = onColorChangeLsn;
    }

    /**
     * 颜色变化回调接口
     */
    public interface OnColorChangeLsn {
        void onWarmChange(int cool);

        void onColorChange(int red, int green, int blue);

        void onUp();
    }


}
