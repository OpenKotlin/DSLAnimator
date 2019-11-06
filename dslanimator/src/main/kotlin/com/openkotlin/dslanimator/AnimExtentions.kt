package com.openkotlin.dslanimator

import android.animation.*
import android.view.animation.LinearInterpolator

fun animSet(creator: AnimSet.() -> Unit) = AnimSet().apply(creator).build()
fun valueAnim(creator: ValueAnim.() -> Unit) = ValueAnim().apply(creator).build()
fun objectAnim(creator: ObjectAnim.() -> Unit) = ObjectAnim().apply(creator).build()

class AnimSet {
    private val animList by lazy { mutableListOf<Anim>() }
    private val animatorSet = AnimatorSet()
    var duration = -1L
        set(value) {
            field = value
            animatorSet.duration = value
        }
        get() = animatorSet.duration

    var delay = 0L
        set(value) {
            require(value >= 0) { "The delay time is less than 0" }
            field = value
            animatorSet.startDelay = value
        }
        get() = animatorSet.startDelay

    var interpolator: TimeInterpolator = LinearInterpolator()
        set(value) {
            field = value
            animatorSet.interpolator = value
        }
        get() = animatorSet.interpolator

    fun valueAnim(animCreation: ValueAnim.() -> Unit) =
        ValueAnim().apply(animCreation).also { animList.add(it) }

    fun objectAnim(animCreation: ObjectAnim.() -> Unit) =
        ObjectAnim().apply(animCreation).also { animList.add(it) }

    fun build() = animatorSet.apply {
        playTogether(animList.map { anim ->
            anim.build()
        })
    }
}

abstract class Anim {
    abstract val animator: ValueAnimator
    abstract fun build(): ValueAnimator
    var delay: Long = 0
        set(value) {
            require(value >= 0) { "The delay time is less than 0" }
            field = value
            animator.startDelay = delay
        }
        get() = animator.startDelay
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

    var repeatMode: Int = -1
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

    fun keyframes(propertyCreation: KeyFramePropertyDelegate.() -> Unit) =
        KeyFramePropertyDelegate().apply(propertyCreation).run {
            build().let { propertyValuesHolder -> properties.add(propertyValuesHolder) }
        }
}

class ObjectAnimProperty {
    var propertyName: String? = null
    var values: Any? = null
    var typeEvaluator: TypeEvaluator<out Any>? = null
    fun build(): PropertyValuesHolder? = propertyName?.let { propertyName ->
        values?.let { values ->
            return when (values) {
                is IntArray -> PropertyValuesHolder.ofInt(propertyName, *values)
                is FloatArray -> PropertyValuesHolder.ofFloat(propertyName, *values)
                is Array<*> -> {
                    requireNotNull(typeEvaluator) { "Need an Evaluator" }
                    PropertyValuesHolder.ofObject(propertyName, typeEvaluator, *values)
                }
                else -> throw IllegalArgumentException("The value type is not supported")
            }
        }
    }
}

class KeyFramePropertyDelegate {
    private val frames = mutableListOf<Keyframe>()
    var propertyName: String? = null
    fun intKeyFrame(fraction: Float, additionalConfig: (Keyframe.() -> Unit)? = null): Keyframe =
        Keyframe.ofInt(fraction).apply {
            if (additionalConfig != null) additionalConfig()
        }.also { frames.add(it) }

    fun intKeyFrame(
        fraction: Float,
        value: Int,
        additionalConfig: (Keyframe.() -> Unit)? = null
    ): Keyframe =
        Keyframe.ofInt(fraction, value).apply {
            if (additionalConfig != null) additionalConfig()
        }.also { frames.add(it) }

    fun floatKeyFrame(fraction: Float, additionalConfig: (Keyframe.() -> Unit)? = null): Keyframe =
        Keyframe.ofFloat(fraction).apply {
            if (additionalConfig != null) additionalConfig()
        }.also { frames.add(it) }

    fun floatKeyFrame(
        fraction: Float,
        value: Float,
        additionalConfig: (Keyframe.() -> Unit)? = null
    ): Keyframe = Keyframe.ofFloat(fraction, value).apply {
        if (additionalConfig != null) additionalConfig()
    }.also { frames.add(it) }

    fun objectKeyFrame(fraction: Float, additionalConfig: (Keyframe.() -> Unit)? = null): Keyframe =
        Keyframe.ofObject(fraction).apply {
            if (additionalConfig != null) additionalConfig()
        }.also { frames.add(it) }

    fun objectKeyFrame(
        fraction: Float,
        value: Any,
        additionalConfig: (Keyframe.() -> Unit)? = null
    ): Keyframe = Keyframe.ofObject(fraction, value).apply {
        if (additionalConfig != null) additionalConfig()
    }.also { frames.add(it) }

    fun build(): PropertyValuesHolder =
        PropertyValuesHolder.ofKeyframe(propertyName, *frames.toTypedArray())
}


class ValueAnim : Anim() {

    override val animator = ValueAnimator()

    override fun build(): ValueAnimator = animator
    var values: Any? = null
        set(value) {
            field = value
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
}