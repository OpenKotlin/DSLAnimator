package com.openkotlin.kotlinisgreat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnDslAnim.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.apply {
            when(id) {
                R.id.btnDslAnim -> startActivity(Intent(this@MainActivity, DSLAnimatorActivity::class.java))
            }
        }
    }

}