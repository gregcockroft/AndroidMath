[![](https://jitpack.io/v/gregcockroft/AndroidMath.svg)](https://jitpack.io/#gregcockroft/AndroidMath)

# AndroidMath

- [x] Rendering of LaTeX Math inside of an Android View
- [x] No dependency on WebView
- [x] Easy to use.

<img src="./img/phonescreen.png" width="320">


Installation from the command line
----------------------------------

```
git clone https://github.com/gregcockroft/AndroidMath.git

cd AndroidMath
./gradlew installDebug
```

Installation for Android Studio
----------------------------------

Clone this project, run CDep.
CDep pulls in the freetype dependency.


```
git clone https://github.com/gregcockroft/AndroidMath.git
cd AndroidMath/mathdisplaylib
./cdep 
```

Open the project in Android Studio 


Using library in your app
-------------------------

* [HelloAndroidMath](https://github.com/gregcockroft/HelloAndroidMath) For a complete simple project

This is using jitpack.io
Add below lines to root's build.gradle

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add below lines to apps's build.gradle

```groovy
dependencies {
	        implementation 'com.github.gregcockroft:AndroidMath:ALPHA'
	}
	
```

```xml
<ConstraintLayout ...>

    <TextView
        android:id="@+id/description"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello Math!"
        app:layout_constraintBottom_toTopOf="@+id/mathview"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
      
    <com.agog.mathdisplay.MTMathView
        android:id="@+id/mathview"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="56dp"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@id/description" />


</ConstraintLayout>
```

```kotlin
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        mathview.latex = "x = \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a}"
    }
	
```



#### API
Look at [MTMathView](https://rawgit.com/gregcockroft/AndroidMath/master/mathdisplaylib/doc/com.agog.mathdisplay/-m-t-math-view/index.html)
This is the central access class.

#### Credits:


* [iosMath](https://github.com/kostub/iosMath) This project is a Kotlin port for Android of the iosMath project 
* [Freetype](https://www.freetype.org/) is used for rendering glyphs and font metrics.
* [Freetype jni](https://github.com/mlomb/freetype-jni) was copied as a starting point to access the native  freetype library.




## Related Projects

* [MathView](https://github.com/kexanie/MathView) uses [MathJax](http://www.mathjax.org/) in a WebView to render math on Android.

For people looking for things beyond just rendering math, there are two
related projects:

* [MathEditor](https://github.com/kostub/MathEditor): A WYSIWYG editor
  for math equations on iOS.
* [MathSolver](https://github.com/kostub/MathSolver): A library for
  solving math equations.
  
## License

AndroidMath is available under the MIT license. See the [LICENSE](./LICENSE)
file for more info.

### Fonts
This distribution contains the following fonts. These fonts are
licensed as follows:
* Latin Modern Math: 
    [GUST Font License](./mathdisplaylib/src/main/assets/fonts/GUST-FONT-LICENSE.txt)
* Tex Gyre Termes:
    [GUST Font License](./mathdisplaylib/src/main/assets/fonts/GUST-FONT-LICENSE.txt)
* [XITS Math](https://github.com/khaledhosny/xits-math):
    [Open Font License](./mathdisplaylib/src/main/assets/fonts/OFL.txt)
