package com.teko.autitosalto

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle

class MainActivity : Activity() {
    private lateinit var gameView: CarJumpView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = CarJumpView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resumeGameAudio()
    }

    override fun onPause() {
        gameView.pauseGameAudio()
        super.onPause()
    }

    @Deprecated("Legacy back callback for Activity")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!::gameView.isInitialized) {
            super.onBackPressed()
            return
        }

        if (!gameView.shouldHandleBackToMenu()) {
            super.onBackPressed()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Estas seguro?")
            .setMessage("Vas a volver al menu.")
            .setPositiveButton("Si") { _, _ ->
                gameView.returnToMenu()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
