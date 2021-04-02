package com.madderate.levelmeasure.page

import android.os.Bundle
import com.madderate.levelmeasure.base.BaseActivity
import com.madderate.levelmeasure.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}