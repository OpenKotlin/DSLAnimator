# DSL Animator
DSL Animator is a library which can let you write the animator in a DSL style which has a more clearly structure to read and easier to write. Just like building block.

### Code samples:

- ValueAnimator
```kotlin
val valueAnimator =
    valueAnim {
        duration = 1000L
        values = floatArrayOf(0f, 360f)
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        doOnValueUpdated {
            ivLoading.rotation = it as Float
        }
    }
valueAnimator.start()
```
- ObjectAnimator
```kotlin
val objectAnimator = 
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
    }
objectAnimator.start()
```
- AnimatorSet
```kotlin
val animatorSet = 
    animSet {
        duration = 1000L
        interpolator = BounceInterpolator()
        valueAnim {
            values = floatArrayOf(0f, 1f)
            evaluator { fraction, startValue, endValue ->
                (endValue as Float - startValue as Float) * fraction
            }
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
animatorSet.start()
```

### Screenshots

- ValueAnimator
<img src="./art/dsl_animator_value.gif" width="40%"/>

- ObjectAnimator
<img src="./art/dsl_animator_obj.gif" width="40%"/>

- AnimatorSet
<img src="./art/dsl_animator_set.gif" width="40%"/>

## License

```
Copyright [2019] [OpenKotlin]  
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.   
You may obtain a copy of the License at       
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
