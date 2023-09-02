package com.sunnyweather.android

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import com.sunnyweather.android.ui.weather.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }

    lateinit var binding: ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorview = window.decorView
        decorview.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.lng.isEmpty()){
            viewModel.lng = intent.getStringExtra("lng") ?: ""
        }
        if (viewModel.lat.isEmpty()){
            viewModel.lat = intent.getStringExtra("lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("placeName") ?: ""
        }

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null){
                showWeatherInfo(weather)
            }else{
                Toast.makeText(this, "无法获得天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
        binding.swipeRefresh.setColorSchemeResources(androidx.appcompat.R.color.primary_dark_material_light)
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener { refreshWeather() }
        binding.now.navBtn.setOnClickListener{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
//        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
//            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onDrawerOpened(drawerView: View) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onDrawerClosed(drawerView: View) {
//                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
//            }
//
//            override fun onDrawerStateChanged(newState: Int) {
//            }
//
//        })
    }

    private fun refreshWeather(){
        viewModel.refreshWeather(viewModel.lng, viewModel.lat)
        binding.swipeRefresh.isRefreshing = true
    }
    private fun showWeatherInfo(weather: Weather) {
        binding.now.placeName.text = viewModel.placeName

        val realtime = weather.realtime
        val daily = weather.daily

        //fill now
        binding.now.currentTemp.text = "${realtime.temperature.toInt()} ℃"
        binding.now.currentSky.text = getSky(realtime.skycon).info
        binding.now.currentAQI.text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        binding.now.root.setBackgroundResource(getSky(realtime.skycon).bg)

        //fill forecast
        binding.forecast.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, binding.forecast.forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            binding.forecast.forecastLayout.addView(view)
        }
        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        binding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        binding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        binding.lifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        binding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
    }

}

