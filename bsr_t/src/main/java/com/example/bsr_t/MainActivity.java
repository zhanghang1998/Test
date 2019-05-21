package com.example.bsr_t;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout rlContainer;
    private ImageView img;
    /*private TextView put_cart;*/
    private ImageView gwc;
    private int rlContainerMeasuredWidth;
    private PathMeasure mPathMeasure;

    private float[] mCurrentPosition = new float[2];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //playOpenAnim(0);
                add2Cart(img);
            }
        });

    }

    private void initView() {
        img = findViewById(R.id.img);
        rlContainer = findViewById(R.id.rlContainer);
        /*put_cart = findViewById(R.id.put_cart);*/
        gwc = findViewById(R.id.gwc);
    }
    public void playOpenAnim(int duration) {

        rlContainer.measure(0, 0);
        rlContainerMeasuredWidth = img.getMeasuredWidth();
        startAnim(img, 0, -rlContainerMeasuredWidth / 3, duration);
    }

    private void startAnim(final View view, final float startX, final float endX, int duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, endX - startX);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float currentX = (float) animation.getAnimatedValue();
                view.setTranslationX(currentX);

                float alpha = 0;
                float hudu = 0;
                float lenth = Math.abs(endX - startX);
                if (endX - startX > 0) {
                    // 向右滑动=====>1--0
                    alpha = (lenth - currentX) / lenth;
                    hudu = currentX * 360 / lenth;
                } else {
                    // 向左滑动====>0-1
                    alpha = Math.abs(currentX) / lenth;
                    hudu = 360 - (lenth - Math.abs(currentX)) * 360 / lenth;
                }

                view.setAlpha(alpha);
                view.setRotation(hudu);
                Log.e("tag", "view=====" + view.getId() + "=======currentX==========" + currentX);
            }
        });
        animator.start();
    }
    private void add2Cart(ImageView ivProductIcon) {

        // 一、创建执行动画的主题---ImageView(该图片就是执行动画的图片，从开始位置出发，经过一个抛物线（贝塞尔曲线）。)
        final ImageView imageView = new ImageView(MainActivity.this);
        imageView.setImageDrawable(ivProductIcon.getDrawable());
        // 将执行动画的图片添加到开始位置。
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(40, 40);
        rlContainer.addView(imageView, params);


        // 二、计算动画开始/结束点的坐标的准备工作
        // 得到父布局的起始点坐标（用于辅助计算动画开始/结束时的点的坐标）
        int[] parentLocation = new int[2];
        rlContainer.getLocationInWindow(parentLocation);
        // 得到商品图片的坐标（用于计算动画开始的坐标）
        int[] startLoc = new int[2];
        ivProductIcon.getLocationInWindow(startLoc);
        // 得到购物车图片的坐标(用于计算动画结束后的坐标)
        int[] endLoc = new int[2];
        gwc.getLocationInWindow(endLoc);

        // 三、计算动画开始结束的坐标
        // 开始掉落的商品的起始点：商品起始点-父布局起始点+该商品图片的一半
        float startX = startLoc[0] - parentLocation[0] + ivProductIcon.getWidth() / 2;
        float startY = startLoc[1] - parentLocation[1] + ivProductIcon.getHeight() / 2;
        //商品掉落后的终点坐标：购物车起始点-父布局起始点+购物车图片的1/5
        float toX = endLoc[0] - parentLocation[0] + gwc.getWidth() / 5;
        float toY = endLoc[1] - parentLocation[1];

        // 四、计算中间动画的插值坐标（贝塞尔曲线）（其实就是用贝塞尔曲线来完成起终点的过程）
        //开始绘制贝塞尔曲线
        Path path = new Path();
        //移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY);
        //使用二次萨贝尔曲线：注意第一个起始坐标越大，贝塞尔曲线的横向距离就会越大，一般按照下面的式子取即可
        path.quadTo((startX + toX) / 2, startY, toX, toY);
        //mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，
        // 如果是true，path会形成一个闭环
        mPathMeasure = new PathMeasure(path, false);

        //★★★属性动画实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        valueAnimator.setDuration(1000);
        // 匀速线性插值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 当插值计算进行时，获取中间的每个值，
                // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
                float value = (Float) animation.getAnimatedValue();
                // ★★★★★获取当前点坐标封装到mCurrentPosition
                // boolean getPosTan(float distance, float[] pos, float[] tan) ：
                // 传入一个距离distance(0<=distance<=getLength())，然后会计算当前距
                // 离的坐标点和切线，pos会自动填充上坐标，这个方法很重要。
                mPathMeasure.getPosTan(value, mCurrentPosition, null);//mCurrentPosition此时就是中间距离点的坐标值
                // 移动的商品图片（动画图片）的坐标设置为该中间点的坐标
                imageView.setTranslationX(mCurrentPosition[0]);
                imageView.setTranslationY(mCurrentPosition[1]);
            }
        });
        //  五、 开始执行动画
        valueAnimator.start();
        //  六、动画结束后的处理
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            //当动画结束后：
            @Override
            public void onAnimationEnd(Animator animation) {
                // 购物车的数量加1
                // 把移动的图片imageview从父布局里移除
                rlContainer.removeView(imageView);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }


}
