package com.uubox.tools;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.uubox.padtool.R;
import com.uubox.views.KeyboardView;


/**
 * 按钮工具类。
 *
 * @author 李剑波
 * @date 2018/1/4
 */
public class BtnUtil {

    private static final String TAG = "cj";

    /**
     * @param btn 按钮类型
     * @return 按钮对应图片资源
     */
    public static Drawable getBtnDrawable(final KeyboardView.Btn btn, Context context) {

        if (btn == null) {
            return null;
        }

        int drawableId = 0;

        switch (btn) {
            /**
             * 鼠标
             */
            case Q:
                drawableId = R.mipmap.edit_q;
                break;
            case A:
                drawableId = R.mipmap.a;
                break;
            case B:
                drawableId = R.mipmap.b;
                break;
            case X:
                drawableId = R.mipmap.edit_x;
                break;
            case Y:
                drawableId = R.mipmap.edit_y;
                break;
            case L1:
                drawableId = R.mipmap.edit_lt;
                break;
            case R1:
                drawableId = R.mipmap.edit_rt;
                break;
            /*case L2:
                drawableId = R.mipmap.edit_lb;
                break;*/
            case R2:
                drawableId = R.mipmap.edit_rb;
                break;
            case L:
                drawableId = R.mipmap.icon_shangzuoyou_gao;
                break;
            case R:
                drawableId = R.mipmap.mouse;
                break;
            case UP:
                drawableId = R.mipmap.edit_up;
                break;
            case DOWN:
                drawableId = R.mipmap.edit_down;
                break;
            case LEFT:
                drawableId = R.mipmap.edit_left;
                break;
            case RIGHT:
                drawableId = R.mipmap.edit_right;
                break;
            case THUMBL:
                drawableId = R.mipmap.edit_lt;
                break;
            case THUMBR:
                drawableId = R.mipmap.edit_rt;
                break;
            case START:
//                drawableId = R.mipmap.start;
                break;
            case BACK:
                drawableId = R.mipmap.key_back;
                break;
            /**
             * 键盘
             */
            //多功能键
            case ESC:
                drawableId = R.mipmap.key_esc;
                break;
            case TAB:
                drawableId = R.mipmap.key_tab;
                break;
            case SHIFT_LEFT:
                drawableId = R.mipmap.key_shift_left;
                break;
            case CTRL_LEFT:
                drawableId = R.mipmap.key_ctrl;
                break;
            case WIN:
                drawableId = R.mipmap.key_win;
                break;
            case ALT_LEFT:
                drawableId = R.mipmap.key_alt;
                break;
            case SPACES:
                drawableId = R.mipmap.key_spaces;
                break;

            //数字
            case NUM_1:
                drawableId = R.mipmap.key_1;
                break;
            case NUM_2:
                drawableId = R.mipmap.key_2;
                break;
            case NUM_3:
                drawableId = R.mipmap.key_3;
                break;
            case NUM_4:
                drawableId = R.mipmap.key_4;
                break;
            case NUM_5:
                drawableId = R.mipmap.key_5;
                break;
            case NUM_6:
                drawableId = R.mipmap.key_6;
                break;
            case NUM_7:
                drawableId = R.mipmap.key_7;
                break;

            //鼠标
            case MOUSE_LEFT:
                drawableId = R.mipmap.key_mouse_left;
                break;
            case MOUSE_RIGHT:
                drawableId = R.mipmap.key_mouse_right;
                break;
            case MOUSE_IN:
                drawableId = R.mipmap.key_mouse_in;
                break;

            //侧键
            case MOUSE_SIDE_FRONT:
                drawableId = R.mipmap.key_mouse_side_front;
                break;
            case MOUSE_SIDE_BACK:
                drawableId = R.mipmap.key_mouse_side_back;
                break;

            //字母
            case KEY_Q:
                drawableId = R.mipmap.key_q;
                break;
            case KEY_W:
                drawableId = R.mipmap.key_w;
                break;
            case KEY_E:
                drawableId = R.mipmap.key_e;
                break;
            case KEY_R:
                drawableId = R.mipmap.key_r;
                break;
            case KEY_T:
                drawableId = R.mipmap.key_t;
                break;
            case KEY_Y:
                drawableId = R.mipmap.key_y;
                break;
            case KEY_A:
                drawableId = R.mipmap.key_a;
                break;
            case KEY_S:
                drawableId = R.mipmap.key_s;
                break;
            case KEY_D:
                drawableId = R.mipmap.key_d;
                break;
            case KEY_F:
                drawableId = R.mipmap.key_f;
                break;
            case KEY_G:
                drawableId = R.mipmap.key_g;
                break;
            case KEY_H:
                drawableId = R.mipmap.key_h;
                break;
            case KEY_Z:
                drawableId = R.mipmap.key_z;
                break;
            case KEY_X:
                drawableId = R.mipmap.key_x;
                break;
            case KEY_C:
                drawableId = R.mipmap.key_c;
                break;
            case KEY_V:
                drawableId = R.mipmap.key_v;
                break;
            case KEY_B:
                drawableId = R.mipmap.key_b;
                break;
            //12-20
            case F1:
                drawableId = R.mipmap.key_f1;
                break;
            case F2:
                drawableId = R.mipmap.key_f2;
                break;
            case F3:
                drawableId = R.mipmap.key_f3;
                break;
            case F4:
                drawableId = R.mipmap.key_f4;
                break;
            case F5:
                drawableId = R.mipmap.key_f5;
                break;
            case F6:
                drawableId = R.mipmap.key_f6;
                break;
            case F7:
                drawableId = R.mipmap.key_f7;
                break;
            case F8:
                drawableId = R.mipmap.key_f8;
                break;
            case F9:
                drawableId = R.mipmap.key_f9;
                break;
            case F10:
                drawableId = R.mipmap.key_f10;
                break;
            case F11:
                drawableId = R.mipmap.key_f11;
                break;
            case F12:
                drawableId = R.mipmap.key_f12;
                break;
            case NUM_8:
                drawableId = R.mipmap.key_8;
                break;
            case NUM_9:
                drawableId = R.mipmap.key_9;
                break;
            case NUM_0:
                drawableId = R.mipmap.key_0;
                break;
            case SUB:
                drawableId = R.mipmap.key_sub;
                break;
            case ADD:
                drawableId = R.mipmap.key_add;
                break;
            case ENTER:
                drawableId = R.mipmap.key_enter;
                break;
            case KEY_U:
                drawableId = R.mipmap.key_u;
                break;
            case KEY_I:
                drawableId = R.mipmap.key_i;
                break;
            case KEY_O:
                drawableId = R.mipmap.key_o;
                break;
            case KEY_P:
                drawableId = R.mipmap.key_p;
                break;
            case KEY_LEFT_BEGIN:
                drawableId = R.mipmap.key_left_begin;
                break;
            case KEY_RIGHT_END:
                drawableId = R.mipmap.key_right_end;
                break;
            case KEY_J:
                drawableId = R.mipmap.key_j;
                break;
            case KEY_K:
                drawableId = R.mipmap.key_k;
                break;
            case KEY_L:
                drawableId = R.mipmap.key_l;
                break;
            case KEY_MAOHAO:
                drawableId = R.mipmap.key_maohao;
                break;
            case KEY_YINHAO:
                drawableId = R.mipmap.key_yinhao;
                break;
            case KEY_FANXIEGANG:
                drawableId = R.mipmap.key_fanxiegang;
                break;
            case KEY_XIEGANG:
                drawableId = R.mipmap.key_xiegang;
                break;
            case KEY_DOUHAO:
                drawableId = R.mipmap.key_douhao;
                break;
            case KEY_JUHAO:
                drawableId = R.mipmap.key_juhao;
                break;
            case ALT_RIGHT:
                drawableId = R.mipmap.key_alt_right;
                break;
            case CTRL_RGHT:
                drawableId = R.mipmap.key_ctrl_right;
                break;
            case SHIFT_RIGHT:
                drawableId = R.mipmap.key_right_shift;
                break;
            case KEY_M:
                drawableId = R.mipmap.key_m;
                break;
            case CAPS:
                drawableId = R.mipmap.key_capslk;
                break;
            case KEY_N:
                drawableId = R.mipmap.key_n;
                break;
            case GUNLUN:
                //drawableId = R.mipmap.gunlun;
                break;
            default:
                break;
        }

        if (drawableId > 0) {
            Drawable drawable = context.getResources().getDrawable(drawableId);
            return drawable;
        } else {
            return null;
        }
    }

}
