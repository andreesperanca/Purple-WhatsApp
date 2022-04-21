package com.voltaire.whats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import com.squareup.picasso.Picasso
import com.voltaire.whats.databinding.ActivityShowImageBinding
import com.voltaire.whats.utils.Constants

class ShowImage : AppCompatActivity() {

    private lateinit var binding: ActivityShowImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityShowImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.showImageToolbar.title =
            if (intent.getStringExtra("TITLE") != null)
                intent.getStringExtra("TITLE")
            else "Visualização de imagem"

        binding.showImageToolbar.setNavigationOnClickListener {
            finish()
        }

        val url = if (intent.getStringExtra("URL") != null)
            intent.getStringExtra("URL")
            else
                Constants.URL_DEFAULT_PROFILE_PHOTO

            Picasso.get()
                .load(url)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(binding.userImageShow)
    }
}