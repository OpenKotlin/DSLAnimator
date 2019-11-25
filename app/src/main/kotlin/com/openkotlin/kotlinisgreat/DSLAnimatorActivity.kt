package com.openkotlin.kotlinisgreat

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.openkotlin.dslanimator.animSet
import com.openkotlin.dslanimator.objectAnim
import com.openkotlin.dslanimator.valueAnim
import kotlinx.android.synthetic.main.acrivity_dsl_animator.*

class DSLAnimatorActivity: AppCompatActivity(), View.OnClickListener {
    private val valueAnimator by lazy {  buildValueAnimator() }
    private val objectAnimator by lazy { buildObjectAnimator() }
    private val animatorSet by lazy { buildAnimatorSet() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acrivity_dsl_animator)
        btnAnimatorSet.setOnClickListener(this)
        btnValueAnimator.setOnClickListener(this)
        btnObjectAnimator.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.apply {
            when(id) {
                R.id.btnValueAnimator -> startValueAnim()
                R.id.btnObjectAnimator -> startObjectAnim()
                R.id.btnAnimatorSet -> startAnimSet()
            }
        }
    }

    private fun cancelAllAnim() {
        if (valueAnimator.isRunning) {
            valueAnimator.cancel()
        }
        if (objectAnimator.isRunning) {
            objectAnimator.cancel()
        }
        if (animatorSet.isRunning) {
            animatorSet.cancel()
        }
    }

    private fun startValueAnim() {
        cancelAllAnim()
        valueAnimator.start()
    }

    private fun startObjectAnim() {
        cancelAllAnim()
        objectAnimator.start()
    }

    private fun startAnimSet() {
        cancelAllAnim()
        animatorSet.start()
    }

    private fun buildValueAnimator() =
        valueAnim {
            duration = 1000L
            values = floatArrayOf(0f, 360f)
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            doOnValueUpdated {
                ivLoading.rotation = it as Float
            }
        }

    private fun buildObjectAnimator() =
        objectAnim {
            target = ivLoading
            duration = 1000L
            interpolator = AccelerateInterpolator()
            property {
                propertyName = "rotation"
                values = floatArrayOf(0f, 360f)
            }
            property {
                propertyName = "scaleX"
                values = floatArrayOf(1f, 1.1f, 1.3f, 1.1f, 1f)
            }
            property {
                propertyName = "scaleY"
                values = floatArrayOf(1f, 1.1f, 1.3f, 1.1f, 1f)
            }
        }

    private fun buildAnimatorSet() =
        animSet {
            duration = 1000L
            interpolator = BounceInterpolator()
            valueAnim {
                values = floatArrayOf(0f, 1f)
                doOnValueUpdated {
                    ivLoading.alpha = it as Float
                }
            }
            objectAnim {
                target = ivLoading
                keyframes {
                    propertyName = "rotation"
                    floatKeyFrame(0f, 0f)
                    floatKeyFrame(0.4f, 180f)
                    floatKeyFrame(1f, 360f)
                }
            }
        }
}