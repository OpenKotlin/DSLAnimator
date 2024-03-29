package com.openkotlin.dslanimator

import android.animation.*
import android.animation.Animator.AnimatorListener
import android.view.View

fun animSet(creator: AnimSet.() -> Unit) = AnimSet().apply(creator).build()
fun valueAnim(creator: ValueAnim.() -> Unit) = ValueAnim().apply(creator).build()
fun objectAnim(creator: ObjectAnim.() -> Unit) = ObjectAnim().apply(creator).build()

abstract class Anim {
    abstract val animator: Animator
    abstract fun build(): Animator
    var startDelay: Long = 0
        set(value) {
            require(value >= 0) { "The delay time is less than 0" }
            field = value
            animator.startDelay = startDelay
        }

    var duration: Long = 0
        set(value) {
            animator.duration = value
            field = value
        }

    var interpolator: TimeInterpolator? = null
        set(value) {
            animator.interpolator = value
            field = value
        }

    private var onRepeatAction: ((Animator?) -> Unit)? = null

    private var onEndAction: ((Animator?) -> Unit)? = null

    private var onCancelAction: ((Animator?) -> Unit)? = null

    private var onStartAction: ((Animator?) -> Unit)? = null

    private var onPauseAction: ((Animator?) -> Unit)? = null

    private var onResumeAction: ((Animator?) -> Unit)? = null

    private var animatorListener: AnimatorListener? = null

    private var animatorPauseListener: Animator.AnimatorPauseListener? = null

    private fun createAnimatorListener() = object: AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                onRepeatAction?.invoke(animation)
            }

            override fun onAnimationEnd(animation: Animator?) {
                onEndAction?.invoke(animation)
            }

            override fun onAnimationCancel(animation: Animator?) {
                onCancelAction?.invoke(animation)
            }

            override fun onAnimationStart(animation: Animator?) {
                onStartAction?.invoke(animation)
            }

        }

    private fun createAnimatorPauseListener() = object: Animator.AnimatorPauseListener {
        override fun onAnimationPause(animation: Animator?) {
            onPauseAction?.invoke(animation)
        }

        override fun onAnimationResume(animation: Animator?) {
            onResumeAction?.invoke(animation)
        }
    }

    fun onRepeat(action: (Animator?) -> Unit) {
        if (animatorListener == null) animatorListener = createAnimatorListener().also { animator.addListener(it) }
        onRepeatAction = action
    }

    fun onEnd(action: (Animator?) -> Unit) {
        if (animatorListener == null) animatorListener = createAnimatorListener().also { animator.addListener(it) }
        onEndAction = action
    }

    fun onCancel(action: (Animator?) -> Unit) {
        if (animatorListener == null) animatorListener = createAnimatorListener().also { animator.addListener(it) }
        onCancelAction = action
    }

    fun onStart(action: (Animator?) -> Unit) {
        if (animatorListener == null) animatorListener = createAnimatorListener().also { animator.addListener(it) }
        onStartAction = action
    }

    fun onPause(action: (Animator?) -> Unit) {
        if (animatorPauseListener == null) animatorPauseListener = createAnimatorPauseListener().also { animator.addPauseListener(it) }
        onPauseAction = action
    }

    fun onResume(action: (Animator?) -> Unit) {
        if (animatorPauseListener == null) animatorPauseListener = createAnimatorPauseListener().also { animator.addPauseListener(it) }
        onResumeAction = action
    }
}

class AnimSet : Anim() {
    private val animList by lazy { mutableListOf<Anim>() }
    override val animator = AnimatorSet()
    var style = PLAY_TOGETHER
    var target: View? = null

    fun valueAnim(animCreation: ValueAnim.() -> Unit) =
        ValueAnim().apply(animCreation).also { animList.add(it) }

    fun objectAnim(animCreation: ObjectAnim.() -> Unit) =
        ObjectAnim().apply(animCreation).also { objectAnim ->
            this@AnimSet.target?.let { animSetTarget ->
                if (objectAnim.target == null) objectAnim.target = animSetTarget
            }
            animList.add(objectAnim)
        }

    fun animSet(animCreation: AnimSet.() -> Unit) =
        AnimSet().apply(animCreation).also { animList.add(it) }

    override fun build() = animator.apply {
        when(style){
            PLAY_TOGETHER -> playTogether(animList.map { anim ->
                anim.build()
            })
            PLAY_SEQUENTIALLY -> playSequentially(animList.map { anim ->
                anim.build()
            })
        }
    }

    companion object {
        const val PLAY_TOGETHER = 1
        const val PLAY_SEQUENTIALLY = 2
    }
}

open class ValueAnim : Anim() {

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

    var repeatMode: Int = -1
        set(value) {
            animator.repeatMode = value
            field = repeatMode
        }

    var repeatCount: Int = 0
        set(value) {
            animator.repeatCount = value
            field = repeatCount
        }

    var evaluator: TypeEvaluator<*>? = null
        set(value) {
            animator.setEvaluator(value)
            field = value
        }

    fun evaluator(eval: (fraction: Float, startValue: Any, endValue: Any) -> Any) =
        animator.setEvaluator(eval)

    fun doOnValueUpdated(ac: (Any) -> Unit) = animator.addUpdateListener { animation ->
        animation.animatedValue?.let { ac.invoke(it) }
    }
}

class ObjectAnim : ValueAnim() {

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
    private var typeEvaluator: TypeEvaluator<out Any>? = null
    fun evaluator(eval: (fraction: Float, startValue: Any, endValue: Any) -> Any) {
        typeEvaluator = TypeEvaluator(eval)
    }

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
            additionalConfig?.invoke(this)
        }.also { frames.add(it) }

    fun intKeyFrame(
        fraction: Float,
        value: Int,
        additionalConfig: (Keyframe.() -> Unit)? = null
    ): Keyframe =
        Keyframe.ofInt(fraction, value).apply {
            additionalConfig?.invoke(this)
        }.also { frames.add(it) }

    fun floatKeyFrame(fraction: Float, additionalConfig: (Keyframe.() -> Unit)? = null): Keyframe =
        Keyframe.ofFloat(fraction).apply {
            additionalConfig?.invoke(this)
        }.also { frames.add(it) }

    fun floatKeyFrame(
        fraction: Float,
        value: Float,
        additionalConfig: (Keyframe.() -> Unit)? = null
    ): Keyframe = Keyframe.ofFloat(fraction, value).apply {
        additionalConfig?.invoke(this)
    }.also { frames.add(it) }

    fun objectKeyFrame(fraction: Float, additionalConfig: (Keyframe.() -> Unit)? = null): Keyframe =
        Keyframe.ofObject(fraction).apply {
            additionalConfig?.invoke(this)
        }.also { frames.add(it) }

    fun objectKeyFrame(
        fraction: Float,
        value: Any,
        additionalConfig: (Keyframe.() -> Unit)? = null
    ): Keyframe = Keyframe.ofObject(fraction, value).apply {
        additionalConfig?.invoke(this)
    }.also { frames.add(it) }

    fun build(): PropertyValuesHolder =
        PropertyValuesHolder.ofKeyframe(propertyName, *frames.toTypedArray())
}


