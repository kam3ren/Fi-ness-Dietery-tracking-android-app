package com.example.finess

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.finess.databinding.NavigationLayoutBinding

//Main class for fragment switching.
class MainActivity : AppCompatActivity() {

    private lateinit var binding: NavigationLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NavigationLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())

        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.home -> replaceFragment(HomeFragment())
                R.id.bmi -> replaceFragment(BmiFragment())
                R.id.water-> replaceFragment(WaterFragment())
                R.id.food -> replaceFragment(FoodFragment())

                else -> {}
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_view, fragment)
        fragmentTransaction.commit()
    }
}