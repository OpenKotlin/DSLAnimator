package com.openkotlin.dslanimator

import android.animation.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator

fun animSet(creator: AnimSet.() -> Unit) = AnimSet().apply(creator).build()
fun valueAnim(creator: ValueAnim.() -> Unit) = ValueAnim().apply(creator).build()
fun objectAnim(creator: ObjectAnim.() -> Unit) = ObjectAnim().apply(creator).build()

private fun testCreator() {
    animSet {
        valueAnim {
            duration = 300L
            repeatMode = ValueAnimator.INFINITE
            repeatCount = 3
            values = arrayOf(1, 2, 3)
            interpolator = AccelerateInterpolator()
            action {

            }
        }
        objectAnim {
            target = //view
            property {
                propertyName = "scaleX"
                values = floatArrayOf(1f, 1.1f, 1.5f, 1.3f)
            }
        }
    }
}

class AnimSet {
    private val animList by lazy { mutableListOf<Anim>() }

    var delay = 0L
        set(value) {
            require(value >= 0) { "The delay time is less than 0" }
            field = value
        }

    fun valueAnim(animCreation: ValueAnim.() -> Unit) =
        ValueAnim().apply(animCreation).also { animList.add(it) }

    fun objectAnim(animCreation: ObjectAnim.() -> Unit) =
        ObjectAnim().apply(animCreation).also { animList.add(it) }

    fun build() = AnimatorSet().apply {
        playTogether(animList.map {
            it.build()
        })
        if (delay > 0) startDelay = delay
    }
}

abstract class Anim {
    abstract val animator: ValueAnimator
    abstract fun build(): ValueAnimator
    var duration: Long = 0
        get() = animator.duration
        set(value) {
            animator.duration = value
            field = value
        }

    var interpolator: TimeInterpolator = LinearInterpolator()
        set(value) {
            animator.interpolator = value
            field = value
        }
        get() = animator.interpolator

    var repeatMode: Int = ValueAnimator.INFINITE
        set(value) {
            animator.repeatMode = value
            field = repeatMode
        }
        get() = animator.repeatMode

    var repeatCount: Int = 0
        set(value) {
            animator.repeatCount = value
            field = repeatCount
        }
        get() = animator.repeatCount

    var values: Any? = null
        set(value) {
            field = value
            animator.setObjectValues()
            value?.let {
                intArrayOf()
                when (it) {
                    is FloatArray -> animator.setFloatValues(*it)
                    is IntArray -> animator.setIntValues(*it)
                    is Array<*> -> animator.setObjectValues(*it)
                    else -> throw IllegalArgumentException("The value type is not supported")
                }
            }
        }

    fun action(ac: (Any) -> Unit) = animator.addUpdateListener { animation ->
        animation.animatedValue?.let { ac.invoke(it) }
    }

    fun evaluator(eval: (fraction: Float, startValue: Any, endValue: Any) -> Any) =
        animator.setEvaluator(eval)

}

class ObjectAnim : Anim() {

    override val animator = ObjectAnimator()

    override fun build(): ValueAnimator {
        if (properties.isNotEmpty()) {
            animator.setValues(*properties.toTypedArray())
        }
        return animator
    }

    private val properties by lazy { mutableListOf<PropertyValuesHolder>() }

    var target: Any? = null
        set(value) {
            animator.target = value
            field = value
        }
        get() = animator.target

    fun property(propertyCreation: ObjectAnimProperty.() -> Unit) =
        ObjectAnimProperty().apply(propertyCreation).run {
            build()?.let { propertyValuesHolder -> properties.add(propertyValuesHolder) }
        }

}

class ObjectAnimProperty {
    var propertyName: String? = null
    var values: Any? = null
    fun build(): PropertyValuesHolder? = propertyName?.let { propertyName ->
        values?.let { values ->
            return when (values) {
                is IntArray -> PropertyValuesHolder.ofInt(propertyName, *values)
                is FloatArray -> PropertyValuesHolder.ofFloat(propertyName, *values)
                else -> throw IllegalArgumentException("The value type is not supported")
            }
        }
    }
}

class ValueAnim : Anim() {

    override val animator = ValueAnimator()

    override fun build(): ValueAnimator = animator

    var action: ((Any) -> Unit)? = null
        set(value) {
            field = value
            animator.addUpdateListener { valueAnimator ->
                valueAnimator.animatedValue?.let { value?.invoke(it) }
            }
        }


}