package com.singletonku.ar_metaverse_sns.navigation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.singletonku.ar_metaverse_sns.R
import com.singletonku.ar_metaverse_sns.databinding.FragmentArBinding


class ArFragment : Fragment() {

    lateinit var binding : FragmentArBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentArBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragArBtn.setOnClickListener {
            Log.d("ar", "실행직전")
            try {
                val arIntent =
                    activity?.packageManager?.getLaunchIntentForPackage("com.maxst.vpssdk")
                startActivity(arIntent)
            }
            catch (err : Exception){
                Toast.makeText(this@ArFragment.requireContext(), "AR 앱을 설치해주세요", Toast.LENGTH_LONG)
            }
        }
    }


}