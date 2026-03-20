package com.teko.autitosalto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.Shader
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.SystemClock
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class CarJumpView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    companion object {
        private const val PRO_MODE_LIVES = 3
        private const val DAMAGE_SHIELD_DURATION = 1.1f
        private const val SHIELD_POWER_DURATION = 7.5f
        private const val BASE_BOSS_TRIGGER_SCORE = 160
        private const val BASE_BOSS_MAX_HEALTH = 210
        private const val GAME_TITLE = "ASTRORUSH"
        private const val PIXEL_FONT_HEIGHT = 7
        private val PIXEL_FONT = mapOf(
            ' ' to listOf("000", "000", "000", "000", "000", "000", "000"),
            '!' to listOf("00100", "00100", "00100", "00100", "00100", "00000", "00100"),
            '-' to listOf("00000", "00000", "00000", "11111", "00000", "00000", "00000"),
            '.' to listOf("00000", "00000", "00000", "00000", "00000", "00110", "00110"),
            ':' to listOf("00000", "00100", "00100", "00000", "00100", "00100", "00000"),
            '?' to listOf("01110", "10001", "00001", "00010", "00100", "00000", "00100"),
            '0' to listOf("01110", "10001", "10011", "10101", "11001", "10001", "01110"),
            '1' to listOf("00100", "01100", "00100", "00100", "00100", "00100", "01110"),
            '2' to listOf("01110", "10001", "00001", "00010", "00100", "01000", "11111"),
            '3' to listOf("11110", "00001", "00001", "01110", "00001", "00001", "11110"),
            '4' to listOf("00010", "00110", "01010", "10010", "11111", "00010", "00010"),
            '5' to listOf("11111", "10000", "10000", "11110", "00001", "00001", "11110"),
            '6' to listOf("01110", "10000", "10000", "11110", "10001", "10001", "01110"),
            '7' to listOf("11111", "00001", "00010", "00100", "01000", "01000", "01000"),
            '8' to listOf("01110", "10001", "10001", "01110", "10001", "10001", "01110"),
            '9' to listOf("01110", "10001", "10001", "01111", "00001", "00001", "01110"),
            'A' to listOf("01110", "10001", "10001", "11111", "10001", "10001", "10001"),
            'B' to listOf("11110", "10001", "10001", "11110", "10001", "10001", "11110"),
            'C' to listOf("01111", "10000", "10000", "10000", "10000", "10000", "01111"),
            'D' to listOf("11110", "10001", "10001", "10001", "10001", "10001", "11110"),
            'E' to listOf("11111", "10000", "10000", "11110", "10000", "10000", "11111"),
            'F' to listOf("11111", "10000", "10000", "11110", "10000", "10000", "10000"),
            'G' to listOf("01111", "10000", "10000", "10111", "10001", "10001", "01110"),
            'H' to listOf("10001", "10001", "10001", "11111", "10001", "10001", "10001"),
            'I' to listOf("11111", "00100", "00100", "00100", "00100", "00100", "11111"),
            'J' to listOf("00111", "00010", "00010", "00010", "00010", "10010", "01100"),
            'K' to listOf("10001", "10010", "10100", "11000", "10100", "10010", "10001"),
            'L' to listOf("10000", "10000", "10000", "10000", "10000", "10000", "11111"),
            'M' to listOf("10001", "11011", "10101", "10101", "10001", "10001", "10001"),
            'N' to listOf("10001", "11001", "10101", "10011", "10001", "10001", "10001"),
            'O' to listOf("01110", "10001", "10001", "10001", "10001", "10001", "01110"),
            'P' to listOf("11110", "10001", "10001", "11110", "10000", "10000", "10000"),
            'Q' to listOf("01110", "10001", "10001", "10001", "10101", "10010", "01101"),
            'R' to listOf("11110", "10001", "10001", "11110", "10100", "10010", "10001"),
            'S' to listOf("01111", "10000", "10000", "01110", "00001", "00001", "11110"),
            'T' to listOf("11111", "00100", "00100", "00100", "00100", "00100", "00100"),
            'U' to listOf("10001", "10001", "10001", "10001", "10001", "10001", "01110"),
            'V' to listOf("10001", "10001", "10001", "10001", "10001", "01010", "00100"),
            'W' to listOf("10001", "10001", "10001", "10101", "10101", "10101", "01010"),
            'X' to listOf("10001", "10001", "01010", "00100", "01010", "10001", "10001"),
            'Y' to listOf("10001", "10001", "01010", "00100", "00100", "00100", "00100"),
            'Z' to listOf("11111", "00001", "00010", "00100", "01000", "10000", "11111")
        )
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(110, 255, 230, 180)
        style = Paint.Style.STROKE
    }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val objectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val objectCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 255, 255, 255)
        strokeWidth = 4f
    }
    private val hudPanelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 9, 12, 28) }
    private val hudTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
    }
    private val accentTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9BE7FF")
        textAlign = Paint.Align.LEFT
    }
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val flashPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bulletPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFE082") }
    private val bulletGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(110, 255, 245, 157) }
    private val absorbTrailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val absorbGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val enemyShotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF6E6E") }
    private val enemyShotGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(110, 255, 120, 120) }
    private val coverOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(170, 4, 7, 20) }
    private val coverPanelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(205, 8, 14, 34) }
    private val coverStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(210, 126, 230, 255)
        style = Paint.Style.STROKE
    }
    private val coverTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFE082")
        textAlign = Paint.Align.CENTER
        setShadowLayer(24f, 0f, 0f, Color.argb(180, 31, 98, 255))
    }
    private val coverSubtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val coverBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
    }
    private val coverChipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(235, 255, 91, 91) }
    private val coverChipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val pixelTextPaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
    }
    private val pixelShadowPaint = Paint().apply {
        isAntiAlias = false
        style = Paint.Style.FILL
        color = Color.argb(110, 5, 10, 25)
    }
    private val inactiveLifePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 75
    }

    private val rocketBitmap: Bitmap = decodeBitmapResource(R.drawable.nave)
    private val enemyBitmaps = Level.values().associate { level ->
        level.number to decodeBitmapResource(powerupDrawableFor(level.number))
    }
    private val currentEnemyBitmap: Bitmap
        get() = enemyBitmaps[currentLevel.number] ?: enemyBitmaps[Level.ONE.number]!!
    private val bossBitmaps = Level.values().associate { level ->
        level.number to decodeBitmapResource(bossDrawableFor(level.number))
    }
    private val currentBossBitmap: Bitmap
        get() = bossBitmaps[currentLevel.number] ?: bossBitmaps[Level.ONE.number]!!
    private val stageBackgroundMap = mapOf(
        Level.ONE.number to R.drawable.mundo1a,
        Level.TWO.number to R.drawable.mission_level_2,
        Level.THREE.number to R.drawable.mission_level_3,
        Level.FOUR.number to R.drawable.mission_level_4,
        Level.FIVE.number to R.drawable.mission_level_5,
        Level.SIX.number to R.drawable.mission_level_6,
        Level.SEVEN.number to R.drawable.mission_level_7,
        Level.EIGHT.number to R.drawable.mission_level_8,
        Level.NINE.number to R.drawable.mission_level_9,
        Level.TEN.number to R.drawable.mission_level_10,
        Level.ELEVEN.number to R.drawable.mission_level_11,
        Level.TWELVE.number to R.drawable.mission_level_12
    )
    private val stageBitmaps = stageBackgroundMap.mapValues { (_, resId) ->
        decodeBitmapResource(resId)
    }
    private val currentStageBitmap: Bitmap
        get() = stageBitmaps[currentLevel.number] ?: stageBitmaps[Level.ONE.number]!!
    private val powerBitmap: Bitmap = decodeBitmapResource(R.drawable.poderes1)
    private val shieldBubbleBitmap: Bitmap = decodeBitmapResource(R.drawable.burbujaescudo)
    private val shieldOverlayBitmap: Bitmap = decodeBitmapResource(R.drawable.escudo1)
    private val extraLifeBitmap: Bitmap = decodeBitmapResource(R.drawable.rayo)
    private val shieldOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val levelMusicRes = mapOf(
        Level.ONE.number to R.raw.cancion,
        Level.TWO.number to R.raw.nivel_2,
        Level.THREE.number to R.raw.nivel_3,
        Level.FOUR.number to R.raw.nivel_4,
        Level.FIVE.number to R.raw.nivel_5,
        Level.SIX.number to R.raw.nivel_6,
        Level.SEVEN.number to R.raw.nivel_7,
        Level.EIGHT.number to R.raw.nivel_8,
        Level.NINE.number to R.raw.nivel_9,
        Level.TEN.number to R.raw.nivel_10,
        Level.ELEVEN.number to R.raw.nivel_11,
        Level.TWELVE.number to R.raw.nivel_12
    )
    private var backgroundMusic: MediaPlayer? = null
    private val bossMusic = MediaPlayer.create(context, R.raw.cancion2audio)?.apply {
        isLooping = true
        setVolume(0.42f, 0.42f)
    }
    private val shootSoundId = soundPool.load(context, R.raw.disparos, 1)
    private val rockSoundId = soundPool.load(context, R.raw.choque, 1)
    private val tapSoundId = soundPool.load(context, R.raw.tap_tap, 1)
    private val missileSoundId = soundPool.load(context, R.raw.misilsonido, 1)
    private val shieldActiveSoundId = soundPool.load(context, R.raw.escudoactivo, 1)
    private val extraLifeSoundId = soundPool.load(context, R.raw.vidanueva, 1)
    private val kidsModeButton = RectF()
    private val proModeButton = RectF()
    private val testModeButton = RectF()
    private val testKidsButton = RectF()
    private val testProButton = RectF()
    private val testBackButton = RectF()
    private val testMissionButtons = List(Level.values().size) { RectF() }

    private val stars = MutableList(90) { Star() }
    private val spaceObjects = MutableList(6) { SpaceObject() }
    private val enemyShips = MutableList(3) { EnemyShip() }
    private val particles = MutableList(260) { Particle(active = false) }
    private val bullets = MutableList(48) { Bullet(active = false) }
    private val absorptions = MutableList(30) { AbsorptionShard(active = false) }
    private val enemyShots = MutableList(28) { EnemyShot(active = false) }
    private val bossEnemy = BossEnemy()

    private var rocketX = 0f
    private var rocketY = 0f
    private var rocketWidth = 0f
    private var rocketHeight = 0f
    private var targetX = 0f
    private var targetY = 0f
    private var touchActive = false
    private var firingActive = false
    private var lastFrameTime = 0L
    private var initialized = false
    private var worldOffset = 0f
    private var score = 0
    private var missionScore = 0
    private var bestScore = 0
    private var combo = 0
    private var pulseTime = 0f
    private var flashTime = 0f
    private var shakeTime = 0f
    private var exhaustTimer = 0f
    private var bulletCooldown = 0f
    private var turboTime = 0f
    private var lastRocketTapTime = 0L
    private var rocketTapStreak = 0
    private var shootToneCooldown = 0f
    private var impactToneCooldown = 0f
    private var shotToneIndex = 0
    private var missilePowerTime = 0f
    private var shieldPowerTime = 0f
    private var damageShieldTime = 0f
    private var remainingLives = PRO_MODE_LIVES
    private var selectedMode = GameMode.KIDS
    private var currentLevel = Level.ONE
    private var testLaunchMode = GameMode.PRO
    private var levelElapsedTime = 0f
    private var screenState = ScreenState.COVER
    private var bossSpawned = false
    private var bossDefeated = false
    private var levelClearTimer = 0f
    private var bossMusicPrimed = false
    private val activeShootStreams = mutableListOf<Int>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return

        backgroundPaint.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#050816"), Color.parseColor("#0D1630"), Shader.TileMode.CLAMP)
        nebulaPaint.shader = RadialGradient(
            w * 0.28f,
            h * 0.24f,
            max(w, h) * 0.55f,
            intArrayOf(Color.argb(170, 98, 62, 180), Color.argb(110, 18, 104, 171), Color.argb(0, 18, 104, 171)),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        planetPaint.shader = RadialGradient(
            w * 0.84f,
            h * 0.2f,
            min(w, h) * 0.15f,
            intArrayOf(Color.parseColor("#FFD180"), Color.parseColor("#FF8A65"), Color.parseColor("#D84315")),
            floatArrayOf(0.1f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        ringPaint.strokeWidth = min(w, h) * 0.012f
        updateCoverButtons()
        initializeGame()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (screenState != ScreenState.PLAYING) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if (screenState == ScreenState.LEVEL_CLEAR) {
                    continueAfterLevelClear()
                    return true
                }
                return handleCoverTouch(event.x, event.y)
            }
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchActive = true
                firingActive = true
                updateTargetFromTouch(event.x, event.y)
                if (isTouchOnRocket(event.x, event.y)) handleRocketTap()
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                touchActive = true
                firingActive = true
                updateTargetFromTouch(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchActive = false
                firingActive = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!initialized) return

        val now = SystemClock.elapsedRealtime()
        val deltaSeconds = if (lastFrameTime == 0L) 0.016f else ((now - lastFrameTime) / 1000f).coerceAtMost(0.032f)
        lastFrameTime = now

        when (screenState) {
            ScreenState.PLAYING -> updateGame(deltaSeconds)
            ScreenState.COVER, ScreenState.TEST_MODE, ScreenState.GAME_OVER, ScreenState.LEVEL_CLEAR -> updateCoverAmbient(deltaSeconds)
        }
        drawGame(canvas)
        when (screenState) {
            ScreenState.COVER -> drawCover(canvas)
            ScreenState.TEST_MODE -> drawTestMode(canvas)
            ScreenState.GAME_OVER -> drawGameOver(canvas)
            ScreenState.LEVEL_CLEAR -> drawLevelClear(canvas)
            ScreenState.PLAYING -> Unit
        }
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pauseGameAudio()
        stopActiveShootSounds()
        backgroundMusic?.release()
        bossMusic?.release()
        soundPool.release()
    }

    fun resumeGameAudio() {
        syncMusic()
    }

    fun pauseGameAudio() {
        backgroundMusic?.let { if (it.isPlaying) it.pause() }
        bossMusic?.let { if (it.isPlaying) it.pause() }
    }

    fun shouldHandleBackToMenu(): Boolean {
        return screenState != ScreenState.COVER
    }

    fun returnToMenu() {
        initializeGame()
    }

    private fun decodeBitmapResource(resourceId: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        return BitmapFactory.decodeResource(resources, resourceId, options)
    }

    private fun primeBossMusic() {
        val player = bossMusic ?: return
        if (bossMusicPrimed) return
        try {
            player.setVolume(0f, 0f)
            player.start()
            player.pause()
            player.seekTo(0)
            player.setVolume(0.42f, 0.42f)
            bossMusicPrimed = true
        } catch (_: IllegalStateException) {
        }
    }

    private fun syncMusic() {
        val normalPlayer = backgroundMusic
        val bossPlayer = bossMusic
        if (bossEnemy.active) {
            normalPlayer?.let { if (it.isPlaying) it.pause() }
            bossPlayer?.let { if (!it.isPlaying) it.start() }
        } else {
            bossPlayer?.let { if (it.isPlaying) it.pause() }
            normalPlayer?.let { if (!it.isPlaying) it.start() }
        }
    }

    private fun levelMusicResource(level: Level): Int {
        return levelMusicRes[level.number] ?: levelMusicRes[Level.ONE.number]!!
    }

    private fun prepareBackgroundMusic(level: Level) {
        backgroundMusic?.run {
            if (isPlaying) stop()
            release()
        }
        backgroundMusic = MediaPlayer.create(context, levelMusicResource(level))?.apply {
            isLooping = true
            setVolume(0.38f, 0.38f)
        }
    }

    private fun handleCoverTouch(x: Float, y: Float): Boolean {
        return when (screenState) {
            ScreenState.TEST_MODE -> handleTestModeTouch(x, y)
            else -> handleMainMenuTouch(x, y)
        }
    }

    private fun updateCoverButtons() {
        val buttonWidth = width * 0.78f
        val left = (width - buttonWidth) * 0.5f
        val top = height * 0.4f
        val buttonHeight = height * 0.112f
        val spacing = height * 0.026f
        kidsModeButton.set(left, top, left + buttonWidth, top + buttonHeight)
        proModeButton.set(left, top + buttonHeight + spacing, left + buttonWidth, top + buttonHeight * 2f + spacing)
        testModeButton.set(left, proModeButton.bottom + spacing, left + buttonWidth, proModeButton.bottom + spacing + buttonHeight)

        val testToggleTop = height * 0.3f
        val toggleWidth = width * 0.33f
        val toggleHeight = height * 0.092f
        testKidsButton.set(width * 0.1f, testToggleTop, width * 0.1f + toggleWidth, testToggleTop + toggleHeight)
        testProButton.set(width * 0.57f, testToggleTop, width * 0.57f + toggleWidth, testToggleTop + toggleHeight)

        val missionColumns = 3
        val missionTop = height * 0.34f
        val missionGapX = width * 0.03f
        val missionGapY = height * 0.018f
        val missionWidth = width * 0.26f
        val missionHeight = height * 0.088f
        val missionStartLeft = width * 0.08f
        testMissionButtons.forEachIndexed { index, rect ->
            val row = index / missionColumns
            val column = index % missionColumns
            val rectLeft = missionStartLeft + column * (missionWidth + missionGapX)
            val rectTop = missionTop + row * (missionHeight + missionGapY)
            rect.set(rectLeft, rectTop, rectLeft + missionWidth, rectTop + missionHeight)
        }
        testBackButton.set(width * 0.2f, height * 0.81f, width * 0.8f, height * 0.905f)
        coverStrokePaint.strokeWidth = width * 0.006f
    }

    private fun handleMainMenuTouch(x: Float, y: Float): Boolean {
        return when {
            kidsModeButton.contains(x, y) -> {
                startGame(GameMode.KIDS)
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                playTapSound(0.95f, 1.04f)
                true
            }
            proModeButton.contains(x, y) -> {
                startGame(GameMode.PRO)
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                playTapSound(1f, 0.92f)
                true
            }
            testModeButton.contains(x, y) -> {
                screenState = ScreenState.TEST_MODE
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                playTapSound(0.9f, 1.1f)
                true
            }
            else -> true
        }
    }

    private fun handleTestModeTouch(x: Float, y: Float): Boolean {
        val tappedMissionIndex = testMissionButtons.indexOfFirst { it.contains(x, y) }
        return when {
            testKidsButton.contains(x, y) -> {
                testLaunchMode = GameMode.KIDS
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                playTapSound(0.9f, 1.06f)
                true
            }
            testProButton.contains(x, y) -> {
                testLaunchMode = GameMode.PRO
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                playTapSound(0.95f, 0.96f)
                true
            }
            tappedMissionIndex >= 0 -> {
                startGame(testLaunchMode, Level.values()[tappedMissionIndex])
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                playTapSound(1f, 0.98f)
                true
            }
            testBackButton.contains(x, y) -> {
                screenState = ScreenState.COVER
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                playTapSound(0.82f, 0.94f)
                true
            }
            else -> true
        }
    }

    private fun resetBossState() {
        bossEnemy.active = false
        bossEnemy.x = width * 0.5f
        bossEnemy.y = -height * 1.2f
        bossEnemy.width = width * 0.62f
        val bossBitmap = currentBossBitmap
        bossEnemy.height = bossEnemy.width * (bossBitmap.height.toFloat() / bossBitmap.width.toFloat())
        bossEnemy.maxHealth = bossMaxHealthFor(currentLevel)
        bossEnemy.health = bossEnemy.maxHealth
        bossEnemy.shotCooldown = 1.1f
        bossEnemy.time = 0f
        bossEnemy.attackTime = 0f
        bossEnemy.rechargeTime = 0f
        bossEnemy.vulnerable = false
        bossSpawned = false
        bossDefeated = false
        levelClearTimer = 0f
        bossMusic?.seekTo(0)
        syncMusic()
    }

    private fun initializeGame() {
        currentLevel = Level.ONE
        prepareBackgroundMusic(currentLevel)
        testLaunchMode = GameMode.PRO
        rocketWidth = width * 0.24f
        rocketHeight = rocketWidth * (rocketBitmap.height.toFloat() / rocketBitmap.width.toFloat())
        rocketX = width * 0.5f
        rocketY = height * 0.78f
        targetX = rocketX
        targetY = rocketY
        score = 0
        combo = 0
        worldOffset = 0f
        pulseTime = 0f
        flashTime = 0f
        shakeTime = 0f
        exhaustTimer = 0f
        bulletCooldown = 0f
        turboTime = 0f
        lastRocketTapTime = 0L
        rocketTapStreak = 0
        shootToneCooldown = 0f
        impactToneCooldown = 0f
        shotToneIndex = 0
        missilePowerTime = 0f
        shieldPowerTime = 0f
        damageShieldTime = 0f
        touchActive = false
        firingActive = false
        lastFrameTime = 0L
        initialized = true
        resetStars()
        resetObjects()
        resetEnemies()
        resetBossState()
        primeBossMusic()
        particles.forEach { it.active = false }
        bullets.forEach { it.active = false }
        absorptions.forEach { it.active = false }
        enemyShots.forEach { it.active = false }
        remainingLives = PRO_MODE_LIVES
        screenState = ScreenState.COVER
        resumeGameAudio()
        invalidate()
    }

    private fun startGame(mode: GameMode, level: Level = Level.ONE) {
        selectedMode = mode
        currentLevel = level
        prepareBackgroundMusic(currentLevel)
        remainingLives = PRO_MODE_LIVES
        screenState = ScreenState.PLAYING
        initializeGameWorld(resetRunStats = true)
    }

    private fun continueAfterLevelClear() {
        val nextLevel = nextLevelAfter(currentLevel)
        if (nextLevel == null) {
            returnToMenu()
            return
        }
        currentLevel = nextLevel
        prepareBackgroundMusic(currentLevel)
        screenState = ScreenState.PLAYING
        initializeGameWorld(resetRunStats = false)
    }

    private fun initializeGameWorld(resetRunStats: Boolean) {
        rocketX = width * 0.5f
        rocketY = height * 0.78f
        targetX = rocketX
        targetY = rocketY
        if (resetRunStats) {
            score = 0
            remainingLives = PRO_MODE_LIVES
        }
        missionScore = 0
        levelElapsedTime = 0f
        combo = 0
        worldOffset = 0f
        pulseTime = 0f
        flashTime = 0f
        shakeTime = 0f
        exhaustTimer = 0f
        bulletCooldown = 0f
        turboTime = 0f
        lastRocketTapTime = 0L
        rocketTapStreak = 0
        shootToneCooldown = 0f
        impactToneCooldown = 0f
        shotToneIndex = 0
        missilePowerTime = 0f
        shieldPowerTime = 0f
        damageShieldTime = 0f
        touchActive = false
        firingActive = false
        lastFrameTime = 0L
        resetStars()
        resetObjects()
        resetEnemies()
        resetBossState()
        primeBossMusic()
        particles.forEach { it.active = false }
        bullets.forEach { it.active = false }
        absorptions.forEach { it.active = false }
        enemyShots.forEach { it.active = false }
        resumeGameAudio()
        invalidate()
    }

    private fun nextLevelAfter(level: Level): Level? {
        val nextIndex = level.ordinal + 1
        return Level.values().getOrNull(nextIndex)
    }

    private fun bossTriggerScoreFor(level: Level): Int {
        return BASE_BOSS_TRIGGER_SCORE + (level.number - 1) * 40
    }

    private fun bossMaxHealthFor(level: Level): Int {
        return BASE_BOSS_MAX_HEALTH + (level.number - 1) * 55
    }

    private fun currentWorldBitmap(): Bitmap {
        return currentStageBitmap
    }

    private fun missionObjectSpeedMultiplier(level: Level): Float {
        return 1f + (level.number - 1) * 0.11f
    }

    private fun missionEnemySpeedMultiplier(level: Level): Float {
        return 1f + (level.number - 1) * 0.12f
    }

    private fun missionCometChance(level: Level): Float {
        return when (level.number) {
            1 -> 0f
            2 -> 0.18f
            3 -> 0.24f
            4 -> 0.30f
            5 -> 0.36f
            6 -> 0.42f
            else -> (0.42f + (level.number - 6) * 0.04f).coerceAtMost(0.7f)
        }
    }

    private fun missionBossIntroCooldown(level: Level): Float {
        return max(0.72f, 1.2f - (level.number - 1) * 0.08f)
    }

    private fun missionBossVolleyCooldown(level: Level): Float {
        return max(0.22f, 0.46f - (level.number - 1) * 0.035f)
    }

    private fun missionBossAttackWindow(level: Level): Float {
        return 2.8f + (level.number - 1) * 0.12f
    }

    private fun missionBossRechargeWindow(level: Level): Float {
        return max(1.45f, 2.4f - (level.number - 1) * 0.15f)
    }

    private fun missionBossWidth(level: Level): Float {
        return width * (0.68f + (level.number - 1) * 0.012f)
    }

    private fun missionBossTargetX(level: Level): Float {
        return width * 0.5f
    }

    private fun missionBossVerticalSpeed(level: Level): Float {
        return 1.4f + (level.number - 1) * 0.12f
    }

    private fun missionBossVerticalAmplitude(level: Level): Float {
        return height * (0.16f + (level.number - 1) * 0.012f)
    }

    private fun missionBossDrift(level: Level): Float {
        return if (level.number < 3) 0f else width * (0.025f + (level.number - 3) * 0.012f)
    }

    private fun missionBackgroundTint(level: Level): Int {
        return when (level.number) {
            1 -> Color.argb(0, 0, 0, 0)
            2 -> Color.argb(72, 255, 88, 88)
            3 -> Color.argb(64, 64, 190, 255)
            4 -> Color.argb(88, 255, 170, 90)
            5 -> Color.argb(84, 168, 85, 247)
            6 -> Color.argb(86, 94, 234, 255)
            7 -> Color.argb(92, 255, 196, 100)
            8 -> Color.argb(90, 255, 140, 198)
            9 -> Color.argb(90, 90, 225, 150)
            10 -> Color.argb(88, 255, 82, 147)
            11 -> Color.argb(92, 120, 255, 200)
            12 -> Color.argb(94, 255, 215, 110)
            else -> Color.argb(104, 255, 64, 129)
        }
    }

    private fun missionHint(level: Level): String {
        return when (level.number) {
            1 -> if (selectedMode == GameMode.PRO) "Rayo amarillo = vida extra" else "Toma la burbuja azul para activar escudo"
            2 -> "Mision 2: llegan cometas rojos"
            3 -> "Mision 3: mas fuego enemigo"
            4 -> "Mision 4: boss con ataques largos"
            5 -> "Mision 5: velocidad de locos"
            6 -> "Mision 6: asteroides ionizados y boss vivaz"
            7 -> "Mision 7: oleadas de cometas sincronizados"
            8 -> "Mision 8: el boss se teletransporta sin aviso"
            9 -> "Mision 9: drones verdes disparan misiles"
            10 -> "Mision 10: controla el turbo, todo acelera"
            11 -> "Mision 11: rutas estrechas y misiles cruzados"
            12 -> "Mision 12: resiste sin tregua, es el final"
            else -> "Mision ${level.number}: prepárate para lo inesperado"
        }
    }

    private fun missionCardDescription(level: Level): String {
        return when (level.number) {
            1 -> "Inicio base para coger ritmo."
            2 -> "Cometas rápidos y boss más duro."
            3 -> "Naves agresivas y lluvia de disparos."
            4 -> "Escenario caliente con boss persistente."
            5 -> "Velocidad alta y poco descanso."
            6 -> "Asteroides ionizados y boss vivaz."
            7 -> "Cometas sincronizados y misiles guiados."
            8 -> "Pantalla se altera con boss errante."
            9 -> "Oleadas de drones y orbes enemigos."
            10 -> "Carrera rápida con fuego cruzado constante."
            11 -> "Corredor estrecho con misiles conectados."
            12 -> "Maratón final con boss titánico sin tregua."
            else -> "Ruta extendida con enemigos impredecibles."
        }
    }

    private fun missionBossMinDelay(level: Level): Float {
        return when (level.number) {
            1 -> 18f
            2 -> 20f
            3 -> 22f
            4 -> 24f
            5 -> 25f
            6 -> 27f
            else -> 27f + (level.number - 6) * 2f
        }
    }

    private fun bossDrawableFor(levelNumber: Int): Int {
        return when (levelNumber) {
            1 -> R.drawable.boss_level_1
            2 -> R.drawable.boss_level_2
            3 -> R.drawable.boss_level_3
            4 -> R.drawable.boss_level_4
            5 -> R.drawable.boss_level_5
            6 -> R.drawable.boss_level_6
            7 -> R.drawable.boss_level_7
            8 -> R.drawable.boss_level_8
            9 -> R.drawable.boss_level_9
            10 -> R.drawable.boss_level_10
            11 -> R.drawable.boss_level_11
            12 -> R.drawable.boss_level_12
            else -> R.drawable.boss_level_1
        }
    }

    private fun powerupDrawableFor(levelNumber: Int): Int {
        return when (levelNumber) {
            1 -> R.drawable.powerup_level_1
            2 -> R.drawable.powerup_level_2
            3 -> R.drawable.powerup_level_3
            4 -> R.drawable.powerup_level_4
            5 -> R.drawable.powerup_level_5
            6 -> R.drawable.powerup_level_6
            7 -> R.drawable.powerup_level_7
            8 -> R.drawable.powerup_level_8
            9 -> R.drawable.powerup_level_9
            10 -> R.drawable.powerup_level_10
            11 -> R.drawable.powerup_level_11
            12 -> R.drawable.powerup_level_12
            else -> R.drawable.powerup_level_1
        }
    }

    private fun addScore(points: Int) {
        score += points
        missionScore += points
        bestScore = max(bestScore, score)
    }

    private fun updateTargetFromTouch(x: Float, y: Float) {
        val minX = rocketWidth * 0.5f
        val maxX = width - rocketWidth * 0.5f
        val minY = rocketHeight * 0.55f
        val maxY = height - rocketHeight * 0.5f
        targetX = x.coerceIn(minX, maxX)
        targetY = y.coerceIn(minY, maxY)
    }

    private fun isTouchOnRocket(x: Float, y: Float): Boolean {
        return x in (rocketX - rocketWidth * 0.55f)..(rocketX + rocketWidth * 0.45f) &&
            y in (rocketY - rocketHeight * 0.5f)..(rocketY + rocketHeight * 0.5f)
    }

    private fun handleRocketTap() {
        val now = SystemClock.elapsedRealtime()
        rocketTapStreak = if (now - lastRocketTapTime <= 320L) rocketTapStreak + 1 else 1
        lastRocketTapTime = now

        if (rocketTapStreak >= 3) {
            rocketTapStreak = 0
            turboTime = 2.4f
            flashTime = max(flashTime, 0.16f)
            shakeTime = max(shakeTime, 0.12f)
            playTapSound(1.0f, 1.08f)
            repeat(14) {
                spawnParticle(
                    x = rocketX + (Random.nextFloat() - 0.5f) * rocketWidth * 0.4f,
                    y = rocketY - rocketHeight * 0.1f,
                    speed = width * (0.12f + Random.nextFloat() * 0.3f),
                    angle = 250f + Random.nextFloat() * 40f,
                    life = 0.18f + Random.nextFloat() * 0.2f,
                    radius = width * (0.004f + Random.nextFloat() * 0.01f),
                    color = Color.parseColor("#80D8FF")
                )
            }
        } else {
            playTapSound(0.75f, 1f)
        }
    }

    private fun updateGame(deltaSeconds: Float) {
        turboTime = (turboTime - deltaSeconds).coerceAtLeast(0f)
        missilePowerTime = (missilePowerTime - deltaSeconds).coerceAtLeast(0f)
        shieldPowerTime = (shieldPowerTime - deltaSeconds).coerceAtLeast(0f)
        damageShieldTime = (damageShieldTime - deltaSeconds).coerceAtLeast(0f)
        shootToneCooldown = (shootToneCooldown - deltaSeconds).coerceAtLeast(0f)
        impactToneCooldown = (impactToneCooldown - deltaSeconds).coerceAtLeast(0f)
        val speedMultiplier = if (turboTime > 0f) 1.9f else 1f
        levelElapsedTime += deltaSeconds

        worldOffset += height * 0.36f * deltaSeconds * speedMultiplier
        pulseTime = (pulseTime + deltaSeconds * 6f) % (Math.PI.toFloat() * 2f)
        flashTime = (flashTime - deltaSeconds).coerceAtLeast(0f)
        shakeTime = (shakeTime - deltaSeconds).coerceAtLeast(0f)
        exhaustTimer += deltaSeconds
        bulletCooldown = (bulletCooldown - deltaSeconds).coerceAtLeast(0f)

        if (levelClearTimer > 0f) {
            levelClearTimer = (levelClearTimer - deltaSeconds).coerceAtLeast(0f)
            updateParticles(deltaSeconds)
            if (levelClearTimer <= 0f) {
                screenState = ScreenState.LEVEL_CLEAR
                touchActive = false
                firingActive = false
                stopActiveShootSounds()
            }
            return
        }

        if (!touchActive) {
            targetX = width * (0.5f + sin(worldOffset * 0.003f) * 0.08f)
            targetY = height * (0.78f + sin(worldOffset * 0.0018f) * 0.025f)
        }

        val easing = 1f - exp(-deltaSeconds * (7.5f + if (turboTime > 0f) 4.5f else 0f))
        rocketX += (targetX - rocketX) * easing
        rocketY += (targetY - rocketY) * easing

        val exhaustInterval = if (turboTime > 0f) 0.012f else 0.02f
        if (exhaustTimer >= exhaustInterval) {
            exhaustTimer = 0f
            spawnExhaust()
        }

        val missionFireFactor = (1f - (currentLevel.number - 1) * 0.04f).coerceAtLeast(0.74f)
        val fireInterval = (if (turboTime > 0f) 0.075f else 0.13f) * missionFireFactor
        if (firingActive && bulletCooldown <= 0f) {
            bulletCooldown = fireInterval
            val burstAmount = when {
                turboTime > 0f && currentLevel.number >= 4 -> 4
                turboTime > 0f -> 3
                currentLevel.number >= 5 -> 3
                else -> 2
            }
            spawnBurst(burstAmount)
            playShootTone()
        }

        updateStars(deltaSeconds)
        updateBullets(deltaSeconds, speedMultiplier)
        updateObjects(deltaSeconds)
        maybeSpawnBoss()
        updateBoss(deltaSeconds)
        updateEnemies(deltaSeconds)
        updateEnemyShots(deltaSeconds)
        updateAbsorptions(deltaSeconds)
        updateParticles(deltaSeconds)
    }

    private fun updateCoverAmbient(deltaSeconds: Float) {
        pulseTime = (pulseTime + deltaSeconds * 1.8f) % (Math.PI.toFloat() * 2f)
        flashTime = (flashTime - deltaSeconds).coerceAtLeast(0f)
        stars.forEach { star ->
            star.twinkle += deltaSeconds * star.twinkleSpeed
        }
    }

    private fun updateStars(deltaSeconds: Float) {
        stars.forEach { star ->
            star.y += star.speed * deltaSeconds
            star.twinkle += deltaSeconds * star.twinkleSpeed
            if (star.y - star.radius > height) resetStar(star, true)
        }
    }

    private fun updateObjects(deltaSeconds: Float) {
        val rocketRect = rocketBounds()

        spaceObjects.forEach { obj ->
            obj.y += obj.speed * deltaSeconds
            obj.rotation += obj.rotationSpeed * deltaSeconds
            obj.pulse += deltaSeconds * obj.pulseSpeed
            obj.collisionCooldown = (obj.collisionCooldown - deltaSeconds).coerceAtLeast(0f)
            if (obj.kind == SpaceKind.COMET) {
                obj.x += sin(obj.pulse * 1.35f) * width * 0.065f * deltaSeconds
                obj.x = obj.x.coerceIn(width * 0.1f, width * 0.9f)
            }

            if (obj.y - obj.radius > height) resetObject(obj, true)

            val objRect = RectF(obj.x - obj.radius, obj.y - obj.radius, obj.x + obj.radius, obj.y + obj.radius)
            if (obj.collisionCooldown <= 0f && RectF.intersects(rocketRect, objRect)) {
                obj.collisionCooldown = 0.7f
                handleRocketCollision(obj)
                if (!isHazard(obj.kind)) return@forEach
            }

            bullets.forEach { bullet ->
                if (!bullet.active || !isHazard(obj.kind)) return@forEach
                val bulletRect = RectF(bullet.x - bullet.radius, bullet.y - bullet.radius, bullet.x + bullet.radius, bullet.y + bullet.radius)
                if (RectF.intersects(bulletRect, objRect)) {
                    bullet.active = false
                    destroyHazard(obj)
                    return@forEach
                }
            }
        }
    }

    private fun updateEnemies(deltaSeconds: Float) {
        if (bossEnemy.active) return
        enemyShips.forEach { enemy ->
            enemy.y += enemy.speed * deltaSeconds
            enemy.shotCooldown = (enemy.shotCooldown - deltaSeconds).coerceAtLeast(0f)
            if (enemy.y - enemy.height > height) resetEnemy(enemy, true)

            val enemyRect = RectF(
                enemy.x - enemy.width * 0.5f,
                enemy.y - enemy.height * 0.5f,
                enemy.x + enemy.width * 0.5f,
                enemy.y + enemy.height * 0.5f
            )
            bullets.forEach { bullet ->
                if (!bullet.active) return@forEach
                val bulletRect = RectF(
                    bullet.x - bullet.radius,
                    bullet.y - bullet.radius,
                    bullet.x + bullet.radius,
                    bullet.y + bullet.radius
                )
                if (RectF.intersects(enemyRect, bulletRect)) {
                    bullet.active = false
                    destroyEnemy(enemy)
                    return@forEach
                }
            }

            if (enemy.shotCooldown <= 0f) {
                enemy.shotCooldown = 0.9f + Random.nextFloat() * 1.2f
                spawnEnemyShot(enemy)
            }
        }
    }

    private fun maybeSpawnBoss() {
        if (bossSpawned || bossDefeated || screenState != ScreenState.PLAYING) return
        if (levelElapsedTime < missionBossMinDelay(currentLevel)) return
        if (missionScore < bossTriggerScoreFor(currentLevel)) return
        bossSpawned = true
        bossEnemy.active = true
        bossEnemy.maxHealth = bossMaxHealthFor(currentLevel)
        bossEnemy.health = bossEnemy.maxHealth
        bossEnemy.width = missionBossWidth(currentLevel)
        val bossBitmap = currentBossBitmap
        bossEnemy.height = bossEnemy.width * (bossBitmap.height.toFloat() / bossBitmap.width.toFloat())
        bossEnemy.x = width * 0.5f
        bossEnemy.y = -bossEnemy.height * 1.2f
        bossEnemy.shotCooldown = missionBossIntroCooldown(currentLevel)
        bossEnemy.time = 0f
        bossEnemy.attackTime = missionBossAttackWindow(currentLevel)
        bossEnemy.rechargeTime = 0f
        bossEnemy.vulnerable = false
        enemyShips.forEach { it.y = -height * 1.2f }
        flashTime = max(flashTime, 0.18f)
        shakeTime = max(shakeTime, 0.1f)
        syncMusic()
    }

    private fun updateBoss(deltaSeconds: Float) {
        if (!bossEnemy.active) return

        bossEnemy.time += deltaSeconds
        bossEnemy.shotCooldown = (bossEnemy.shotCooldown - deltaSeconds).coerceAtLeast(0f)
        val targetX = missionBossTargetX(currentLevel)
        val bossDrift = sin(bossEnemy.time * 0.6f) * missionBossDrift(currentLevel)
        val targetY = height * 0.31f + sin(bossEnemy.time * missionBossVerticalSpeed(currentLevel)) * missionBossVerticalAmplitude(currentLevel)
        val easing = 1f - exp(-deltaSeconds * (2.6f + (currentLevel.number - 1) * 0.18f))
        bossEnemy.x += (targetX + bossDrift - bossEnemy.x) * easing
        bossEnemy.y += (targetY - bossEnemy.y) * easing

        val bossRect = RectF(
            bossEnemy.x - bossEnemy.width * 0.42f,
            bossEnemy.y - bossEnemy.height * 0.42f,
            bossEnemy.x + bossEnemy.width * 0.42f,
            bossEnemy.y + bossEnemy.height * 0.42f
        )

        bullets.forEach { bullet ->
            if (!bullet.active) return@forEach
            val bulletRect = RectF(
                bullet.x - bullet.radius,
                bullet.y - bullet.radius,
                bullet.x + bullet.radius,
                bullet.y + bullet.radius
            )
            if (RectF.intersects(bossRect, bulletRect)) {
                bullet.active = false
                if (bossEnemy.vulnerable) {
                    damageBoss(if (bullet.isMissile) 2 else 1)
                } else {
                    spawnBossBlockedEffect(bulletRect.centerX(), bulletRect.centerY())
                }
                return@forEach
            }
        }

        if (RectF.intersects(rocketBounds(), bossRect)) {
            applyRocketDamage()
        }

        if (bossEnemy.rechargeTime > 0f) {
            bossEnemy.rechargeTime = (bossEnemy.rechargeTime - deltaSeconds).coerceAtLeast(0f)
            bossEnemy.vulnerable = true
            if ((bossEnemy.time * 12f).toInt() % 2 == 0) {
                spawnParticle(
                    bossEnemy.x - bossEnemy.width * 0.24f + Random.nextFloat() * bossEnemy.width * 0.2f,
                    bossEnemy.y + (Random.nextFloat() - 0.5f) * bossEnemy.height * 0.35f,
                    width * (0.03f + Random.nextFloat() * 0.08f),
                    Random.nextFloat() * 360f,
                    0.1f + Random.nextFloat() * 0.14f,
                    width * (0.002f + Random.nextFloat() * 0.005f),
                    listOf(Color.parseColor("#7DF9FF"), Color.parseColor("#B388FF"), Color.WHITE).random()
                )
            }
            if (bossEnemy.rechargeTime <= 0f) {
                bossEnemy.vulnerable = false
                bossEnemy.attackTime = missionBossAttackWindow(currentLevel)
                bossEnemy.shotCooldown = missionBossVolleyCooldown(currentLevel)
            }
            return
        }

        bossEnemy.attackTime = (bossEnemy.attackTime - deltaSeconds).coerceAtLeast(0f)
        bossEnemy.vulnerable = false
        if (bossEnemy.shotCooldown <= 0f) {
            bossEnemy.shotCooldown = missionBossVolleyCooldown(currentLevel)
            spawnBossVolley()
        }
        if (bossEnemy.attackTime <= 0f) {
            bossEnemy.rechargeTime = missionBossRechargeWindow(currentLevel)
            bossEnemy.shotCooldown = 99f
        }
    }

    private fun spawnBossBlockedEffect(x: Float, y: Float) {
        repeat(4) {
            spawnParticle(
                x,
                y,
                width * (0.03f + Random.nextFloat() * 0.07f),
                Random.nextFloat() * 360f,
                0.08f + Random.nextFloat() * 0.12f,
                width * (0.002f + Random.nextFloat() * 0.004f),
                listOf(Color.parseColor("#FFAB91"), Color.WHITE).random()
            )
        }
    }

    private fun damageBoss(amount: Int) {
        if (!bossEnemy.active) return
        bossEnemy.health = (bossEnemy.health - amount).coerceAtLeast(0)
        flashTime = max(flashTime, 0.06f)
        repeat(8) {
            spawnParticle(
                bossEnemy.x + (Random.nextFloat() - 0.5f) * bossEnemy.width * 0.35f,
                bossEnemy.y + (Random.nextFloat() - 0.5f) * bossEnemy.height * 0.35f,
                width * (0.06f + Random.nextFloat() * 0.14f),
                Random.nextFloat() * 360f,
                0.14f + Random.nextFloat() * 0.16f,
                width * (0.003f + Random.nextFloat() * 0.008f),
                listOf(Color.parseColor("#7DF9FF"), Color.parseColor("#FF8A80"), Color.WHITE).random()
            )
        }
        if (bossEnemy.health <= 0) {
            destroyBoss()
        }
    }

    private fun destroyBoss() {
        bossEnemy.active = false
        bossDefeated = true
        addScore(20 + (currentLevel.number - 1) * 4)
        combo = min(combo + 6 + (currentLevel.number - 1), 99)
        flashTime = max(flashTime, 0.24f)
        shakeTime = max(shakeTime, 0.22f)
        levelClearTimer = 2.8f
        enemyShots.forEach { it.active = false }
        repeat(120) {
            spawnParticle(
                bossEnemy.x + (Random.nextFloat() - 0.5f) * bossEnemy.width * 0.55f,
                bossEnemy.y + (Random.nextFloat() - 0.5f) * bossEnemy.height * 0.55f,
                width * (0.18f + Random.nextFloat() * 0.46f),
                Random.nextFloat() * 360f,
                0.25f + Random.nextFloat() * 0.5f,
                width * (0.005f + Random.nextFloat() * 0.018f),
                listOf(
                    Color.parseColor("#80D8FF"),
                    Color.parseColor("#FFD180"),
                    Color.parseColor("#FF8A65"),
                    Color.WHITE
                ).random()
            )
        }
        syncMusic()
    }

    private fun updateEnemyShots(deltaSeconds: Float) {
        val rocketRect = rocketBounds()
        enemyShots.forEach { shot ->
            if (!shot.active) return@forEach
            shot.x += shot.vx * deltaSeconds
            shot.y += shot.vy * deltaSeconds
            shot.life -= deltaSeconds
            val shotRect = RectF(shot.x - shot.radius, shot.y - shot.radius, shot.x + shot.radius, shot.y + shot.radius)
            if (RectF.intersects(rocketRect, shotRect)) {
                spawnEnemyImpact(shot.x, shot.y)
                shot.active = false
                applyRocketDamage()
                return@forEach
            }
            if (shot.life <= 0f || shot.y - shot.radius > height) shot.active = false
        }
    }

    private fun updateBullets(deltaSeconds: Float, speedMultiplier: Float) {
        bullets.forEach { bullet ->
            if (!bullet.active) return@forEach
            bullet.x += bullet.vx * deltaSeconds * speedMultiplier
            bullet.y += bullet.vy * deltaSeconds
            bullet.life -= deltaSeconds
            if (bullet.life <= 0f || bullet.y + bullet.radius < -height * 0.1f) bullet.active = false
        }
    }

    private fun updateAbsorptions(deltaSeconds: Float) {
        absorptions.forEach { shard ->
            if (!shard.active) return@forEach
            shard.life -= deltaSeconds
            shard.progress = (shard.progress + deltaSeconds / shard.duration).coerceIn(0f, 1f)
            if (shard.life <= 0f || shard.progress >= 1f) {
                shard.active = false
                return@forEach
            }
            val targetXNow = rocketX
            val targetYNow = rocketY - rocketHeight * 0.3f
            shard.x = shard.startX + (targetXNow - shard.startX) * shard.progress + sin(shard.arcPhase) * width * 0.035f * (1f - shard.progress)
            shard.y = shard.startY + (targetYNow - shard.startY) * shard.progress - shard.arcHeight * (1f - shard.progress)
            shard.radius = shard.baseRadius * (1f - shard.progress * 0.65f)
        }
    }

    private fun updateParticles(deltaSeconds: Float) {
        particles.forEach { particle ->
            if (!particle.active) return@forEach
            particle.life -= deltaSeconds
            if (particle.life <= 0f) {
                particle.active = false
                return@forEach
            }
            particle.x += particle.vx * deltaSeconds
            particle.y += particle.vy * deltaSeconds
            particle.vx *= 0.985f
            particle.vy *= 0.985f
            particle.radius *= 0.992f
        }
    }

    private fun rocketBounds(): RectF {
        return RectF(rocketX - rocketWidth * 0.25f, rocketY - rocketHeight * 0.42f, rocketX + rocketWidth * 0.25f, rocketY + rocketHeight * 0.42f)
    }

    private fun handleRocketCollision(obj: SpaceObject) {
        when (obj.kind) {
            SpaceKind.CRYSTAL -> absorbCrystal(obj)
            SpaceKind.POWERUP -> activateMissilePower(obj)
            SpaceKind.SHIELD -> activateShieldPower(obj)
            SpaceKind.EXTRA_LIFE -> activateExtraLife(obj)
            SpaceKind.ROCK, SpaceKind.COMET -> handleHazardCollision(obj)
        }
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun handleHazardCollision(obj: SpaceObject) {
        val colors = hazardPalette(obj.kind)
        flashTime = 0.22f
        shakeTime = 0.18f
        repeat(26) {
            spawnParticle(obj.x, obj.y, width * (0.12f + Random.nextFloat() * 0.48f), Random.nextFloat() * 360f, 0.25f + Random.nextFloat() * 0.45f, width * (0.004f + Random.nextFloat() * 0.015f), colors.random())
        }
        repeat(12) {
            spawnParticle(rocketX - rocketWidth * 0.18f, rocketY, width * (0.08f + Random.nextFloat() * 0.22f), 150f + Random.nextFloat() * 100f, 0.18f + Random.nextFloat() * 0.28f, width * (0.003f + Random.nextFloat() * 0.01f), Color.WHITE)
        }
        playRockImpactTone()
        if (selectedMode == GameMode.KIDS) {
            addScore(1)
            combo = min(combo + 1, 99)
        } else {
            combo = 0
            applyRocketDamage()
        }
        resetObject(obj, true)
    }

    private fun applyRocketDamage() {
        if (screenState != ScreenState.PLAYING || damageShieldTime > 0f || selectedMode == GameMode.KIDS) return
        if (shieldPowerTime > 0f) {
            flashTime = max(flashTime, 0.16f)
            shakeTime = max(shakeTime, 0.08f)
            repeat(12) {
                spawnParticle(
                    rocketX,
                    rocketY,
                    width * (0.08f + Random.nextFloat() * 0.16f),
                    Random.nextFloat() * 360f,
                    0.12f + Random.nextFloat() * 0.16f,
                    width * (0.003f + Random.nextFloat() * 0.007f),
                    listOf(Color.parseColor("#7DF9FF"), Color.WHITE, Color.parseColor("#B388FF")).random()
                )
            }
            playCrystalAbsorbTone()
            return
        }
        damageShieldTime = DAMAGE_SHIELD_DURATION
        remainingLives = (remainingLives - 1).coerceAtLeast(0)
        combo = 0
        flashTime = max(flashTime, 0.24f)
        shakeTime = max(shakeTime, 0.22f)
        repeat(18) {
            spawnParticle(
                rocketX,
                rocketY,
                width * (0.1f + Random.nextFloat() * 0.22f),
                Random.nextFloat() * 360f,
                0.12f + Random.nextFloat() * 0.18f,
                width * (0.003f + Random.nextFloat() * 0.009f),
                listOf(Color.parseColor("#FF6E6E"), Color.parseColor("#FFD180"), Color.WHITE).random()
            )
        }
        if (remainingLives <= 0) {
            triggerGameOver()
        }
    }

    private fun triggerGameOver() {
        screenState = ScreenState.GAME_OVER
        touchActive = false
        firingActive = false
        stopActiveShootSounds()
    }

    private fun absorbCrystal(obj: SpaceObject) {
        addScore(3)
        combo = min(combo + 2, 99)
        flashTime = max(flashTime, 0.18f)
        shakeTime = max(shakeTime, 0.08f)
        repeat(7) { index -> spawnAbsorptionShard(obj.x, obj.y, obj.radius, index) }
        repeat(16) {
            spawnParticle(obj.x, obj.y, width * (0.1f + Random.nextFloat() * 0.2f), Random.nextFloat() * 360f, 0.18f + Random.nextFloat() * 0.18f, width * (0.004f + Random.nextFloat() * 0.009f), listOf(Color.parseColor("#64FFDA"), Color.parseColor("#80D8FF"), Color.WHITE).random())
        }
        playCrystalAbsorbTone()
        resetObject(obj, true)
    }

    private fun destroyHazard(obj: SpaceObject) {
        val colors = hazardPalette(obj.kind)
        addScore(2)
        combo = min(combo + 1, 99)
        flashTime = max(flashTime, 0.1f)
        repeat(20) {
            spawnParticle(obj.x, obj.y, width * (0.16f + Random.nextFloat() * 0.36f), Random.nextFloat() * 360f, 0.18f + Random.nextFloat() * 0.32f, width * (0.004f + Random.nextFloat() * 0.012f), colors.random())
        }
        playRockDestroyTone()
        resetObject(obj, true)
    }
    private fun destroyEnemy(enemy: EnemyShip) {
        addScore(4)
        combo = min(combo + 2, 99)
        flashTime = max(flashTime, 0.14f)
        shakeTime = max(shakeTime, 0.1f)
        repeat(24) {
            spawnParticle(
                enemy.x,
                enemy.y,
                width * (0.14f + Random.nextFloat() * 0.3f),
                Random.nextFloat() * 360f,
                0.16f + Random.nextFloat() * 0.28f,
                width * (0.004f + Random.nextFloat() * 0.012f),
                listOf(Color.parseColor("#FF6E6E"), Color.parseColor("#FFD180"), Color.WHITE).random()
            )
        }
        playRockDestroyTone()
        resetEnemy(enemy, true)
    }

    private fun spawnEnemyImpact(x: Float, y: Float) {
        flashTime = max(flashTime, 0.08f)
        repeat(14) {
            spawnParticle(x, y, width * (0.08f + Random.nextFloat() * 0.24f), Random.nextFloat() * 360f, 0.12f + Random.nextFloat() * 0.2f, width * (0.003f + Random.nextFloat() * 0.009f), listOf(Color.parseColor("#FF6E6E"), Color.parseColor("#FFD180"), Color.WHITE).random())
        }
        playRockImpactTone()
    }

            private fun playShootTone() {
        if (shootToneCooldown > 0f) return
        val missileActive = missilePowerTime > 0f
        shootToneCooldown = when {
            missileActive -> 0.11f
            turboTime > 0f -> 0.055f
            else -> 0.09f
        }
        val soundId = if (missileActive) missileSoundId else shootSoundId
        val rate = when {
            missileActive -> if (shotToneIndex++ % 2 == 0) 0.94f else 1.02f
            turboTime > 0f -> if (shotToneIndex++ % 2 == 0) 1.14f else 1.03f
            shotToneIndex++ % 3 == 0 -> 0.96f
            else -> 1f
        }
        val volume = when {
            missileActive -> 0.92f
            turboTime > 0f -> 0.82f
            else -> 0.68f
        }
        val streamId = soundPool.play(soundId, volume, volume, 1, 0, rate)
        if (streamId != 0) {
            activeShootStreams.add(streamId)
            if (activeShootStreams.size > 8) {
                soundPool.stop(activeShootStreams.removeAt(0))
            }
        }
    }

    private fun stopActiveShootSounds() {
        activeShootStreams.forEach { soundPool.stop(it) }
        activeShootStreams.clear()
    }

    private fun playRockImpactTone() {
        if (impactToneCooldown > 0f) return
        impactToneCooldown = 0.09f
        soundPool.play(rockSoundId, 0.9f, 0.9f, 1, 0, 0.92f)
    }

    private fun playRockDestroyTone() {
        if (impactToneCooldown > 0f) return
        impactToneCooldown = 0.07f
        soundPool.play(rockSoundId, 1f, 1f, 1, 0, 1.04f)
    }

    private fun playCrystalAbsorbTone() {
        if (impactToneCooldown > 0f) return
        impactToneCooldown = 0.06f
        soundPool.play(tapSoundId, 0.68f, 0.68f, 1, 0, 1.18f)
    }

        private fun playTapSound(volume: Float = 0.85f, rate: Float = 1f) {
        soundPool.play(tapSoundId, volume, volume, 1, 0, rate)
    }

    private fun activateMissilePower(obj: SpaceObject) {
        missilePowerTime = 7.5f
        flashTime = max(flashTime, 0.2f)
        shakeTime = max(shakeTime, 0.1f)
        repeat(18) {
            spawnParticle(
                obj.x,
                obj.y,
                width * (0.1f + Random.nextFloat() * 0.22f),
                Random.nextFloat() * 360f,
                0.2f + Random.nextFloat() * 0.24f,
                width * (0.004f + Random.nextFloat() * 0.01f),
                listOf(Color.parseColor("#7CFFCB"), Color.parseColor("#80D8FF"), Color.WHITE).random()
            )
        }
        soundPool.play(missileSoundId, 0.95f, 0.95f, 1, 0, 1.08f)
        resetObject(obj, true)
    }

    private fun activateShieldPower(obj: SpaceObject) {
        shieldPowerTime = max(shieldPowerTime, SHIELD_POWER_DURATION)
        flashTime = max(flashTime, 0.18f)
        shakeTime = max(shakeTime, 0.08f)
        repeat(18) {
            spawnParticle(
                obj.x,
                obj.y,
                width * (0.08f + Random.nextFloat() * 0.18f),
                Random.nextFloat() * 360f,
                0.18f + Random.nextFloat() * 0.22f,
                width * (0.004f + Random.nextFloat() * 0.01f),
                listOf(Color.parseColor("#7DF9FF"), Color.WHITE, Color.parseColor("#80D8FF")).random()
            )
        }
        soundPool.play(shieldActiveSoundId, 0.95f, 0.95f, 1, 0, 1f)
        resetObject(obj, true)
    }

    private fun activateExtraLife(obj: SpaceObject) {
        if (selectedMode == GameMode.PRO) {
            remainingLives = (remainingLives + 1).coerceAtMost(PRO_MODE_LIVES)
        }
        flashTime = max(flashTime, 0.2f)
        shakeTime = max(shakeTime, 0.08f)
        repeat(20) {
            spawnParticle(
                obj.x,
                obj.y,
                width * (0.08f + Random.nextFloat() * 0.2f),
                Random.nextFloat() * 360f,
                0.18f + Random.nextFloat() * 0.24f,
                width * (0.004f + Random.nextFloat() * 0.01f),
                listOf(Color.parseColor("#FFE082"), Color.parseColor("#FFF59D"), Color.WHITE).random()
            )
        }
        soundPool.play(extraLifeSoundId, 0.95f, 0.95f, 1, 0, 1f)
        resetObject(obj, true)
    }

    private fun spawnExhaust() {
        val angle = 90f + (Random.nextFloat() - 0.5f) * 40f
        spawnParticle(
            rocketX + (Random.nextFloat() - 0.5f) * rocketWidth * 0.3f,
            rocketY + rocketHeight * 0.42f,
            width * (0.08f + Random.nextFloat() * 0.18f),
            angle,
            0.2f + Random.nextFloat() * 0.25f,
            width * (0.005f + Random.nextFloat() * 0.012f),
            listOf(Color.parseColor("#FFF59D"), Color.parseColor("#FF8A65"), Color.parseColor("#80D8FF")).random()
        )
    }

    private fun spawnBurst(amount: Int) {
        repeat(amount) { index ->
            val spread = if (amount == 1) 0f else (index - (amount - 1) / 2f) * 7.5f
            spawnBullet(spread)
        }
    }

            private fun spawnBullet(angleOffset: Float) {
        val bullet = bullets.firstOrNull { !it.active } ?: return
        val radians = Math.toRadians(angleOffset.toDouble())
        val missileActive = missilePowerTime > 0f
        bullet.active = true
        bullet.isMissile = missileActive
        bullet.x = rocketX
        bullet.y = rocketY - rocketHeight * 0.5f
        bullet.vx = width * (if (missileActive) 0.12f else 0.08f) * sin(radians).toFloat()
        bullet.vy = -height * (if (missileActive) 1.28f else 1.05f) * cos(radians).toFloat()
        bullet.life = if (missileActive) 1.2f else 0.95f
        bullet.radius = width * if (missileActive) 0.015f else 0.0105f
        spawnParticle(
            bullet.x,
            bullet.y,
            width * (if (missileActive) 0.11f else 0.05f) + Random.nextFloat() * width * 0.08f,
            80f + Random.nextFloat() * 40f,
            0.08f + Random.nextFloat() * 0.12f,
            width * ((if (missileActive) 0.005f else 0.003f) + Random.nextFloat() * (if (missileActive) 0.007f else 0.006f)),
            if (missileActive) Color.parseColor("#FF8A65") else Color.parseColor("#FFF59D")
        )
    }

    private fun spawnEnemyShot(enemy: EnemyShip) {
        val speedMultiplier = 1f + (currentLevel.number - 1) * 0.09f
        val horizontalAim = (rocketX - enemy.x) * (0.2f + Random.nextFloat() * 0.3f) * speedMultiplier
        val verticalSpeed = height * (0.45f + Random.nextFloat() * 0.12f) * speedMultiplier
        spawnEnemyShot(
            originX = enemy.x,
            originY = enemy.y + enemy.height * 0.45f,
            velocityX = horizontalAim,
            velocityY = verticalSpeed,
            radius = width * 0.012f,
            life = 3f,
            isBoss = false
        )
    }

    private fun spawnBossVolley() {
        val originY = bossEnemy.y + bossEnemy.height * 0.45f
        val spreadTargets = when {
            currentLevel.number >= 6 -> listOf(-0.38f, -0.28f, -0.18f, -0.08f, 0f, 0.08f, 0.18f, 0.28f, 0.38f)
            currentLevel.number >= 4 -> listOf(-0.34f, -0.22f, -0.12f, -0.04f, 0.04f, 0.12f, 0.22f, 0.34f)
            currentLevel.number >= 2 -> listOf(-0.31f, -0.2f, -0.1f, 0f, 0.1f, 0.2f, 0.31f)
            else -> listOf(-0.28f, -0.16f, -0.08f, 0f, 0.08f, 0.16f, 0.28f)
        }
        spreadTargets.forEach { spread ->
            spawnEnemyShot(
                originX = bossEnemy.x + bossEnemy.width * spread * 0.6f,
                originY = originY,
                velocityX = (rocketX - (bossEnemy.x + bossEnemy.width * spread * 0.6f)) * (0.4f + Random.nextFloat() * 0.2f + (currentLevel.number - 1) * 0.02f),
                velocityY = height * (0.65f + Random.nextFloat() * 0.2f + (currentLevel.number - 1) * 0.03f),
                radius = width * (0.02f + (currentLevel.number - 1) * 0.0008f),
                life = 3.1f + (currentLevel.number - 1) * 0.14f,
                isBoss = true
            )
        }
    }

    private fun isHazard(kind: SpaceKind): Boolean {
        return kind == SpaceKind.ROCK || kind == SpaceKind.COMET
    }

    private fun hazardPalette(kind: SpaceKind): List<Int> {
        return when (kind) {
            SpaceKind.COMET -> listOf(Color.parseColor("#FF8A65"), Color.parseColor("#FF5252"), Color.parseColor("#FFE082"))
            else -> listOf(Color.parseColor("#FFCC80"), Color.parseColor("#BCAAA4"), Color.parseColor("#FFE082"))
        }
    }

    private fun spawnEnemyShot(
        originX: Float,
        originY: Float,
        velocityX: Float,
        velocityY: Float,
        radius: Float,
        life: Float,
        isBoss: Boolean
    ) {
        val shot = enemyShots.firstOrNull { !it.active } ?: return
        shot.active = true
        shot.x = originX
        shot.y = originY
        shot.vx = velocityX
        shot.vy = velocityY
        shot.radius = radius
        shot.life = life
        shot.isBoss = isBoss
    }

    private fun spawnAbsorptionShard(originX: Float, originY: Float, radius: Float, index: Int) {
        val shard = absorptions.firstOrNull { !it.active } ?: return
        shard.active = true
        shard.startX = originX + (Random.nextFloat() - 0.5f) * radius * 1.5f
        shard.startY = originY + (Random.nextFloat() - 0.5f) * radius * 1.5f
        shard.x = shard.startX
        shard.y = shard.startY
        shard.progress = 0f
        shard.duration = 0.32f + Random.nextFloat() * 0.18f
        shard.life = shard.duration
        shard.baseRadius = width * (0.007f + Random.nextFloat() * 0.01f)
        shard.radius = shard.baseRadius
        shard.arcHeight = height * (0.035f + Random.nextFloat() * 0.05f)
        shard.arcPhase = index * 0.8f + Random.nextFloat() * 0.7f
        shard.color = if (index % 2 == 0) Color.parseColor("#64FFDA") else Color.parseColor("#80D8FF")
    }

    private fun spawnParticle(x: Float, y: Float, speed: Float, angle: Float, life: Float, radius: Float, color: Int) {
        val particle = particles.firstOrNull { !it.active } ?: return
        val radians = Math.toRadians(angle.toDouble())
        particle.active = true
        particle.x = x
        particle.y = y
        particle.vx = cos(radians).toFloat() * speed
        particle.vy = sin(radians).toFloat() * speed
        particle.life = life
        particle.maxLife = life
        particle.radius = radius
        particle.color = color
    }

    private fun drawGame(canvas: Canvas) {
        val shakeX = if (shakeTime > 0f) (Random.nextFloat() - 0.5f) * width * 0.015f else 0f
        val shakeY = if (shakeTime > 0f) (Random.nextFloat() - 0.5f) * height * 0.012f else 0f
        canvas.save()
        canvas.translate(shakeX, shakeY)
        drawWorldBackground(canvas)
        drawObjects(canvas)
        drawEnemyShips(canvas)
        drawBoss(canvas)
        drawBullets(canvas)
        drawEnemyShots(canvas)
        drawAbsorptions(canvas)
        drawParticles(canvas)
        drawRocket(canvas)
        canvas.restore()
        if (screenState == ScreenState.PLAYING) {
            drawHud(canvas)
        }
        drawFlash(canvas)
    }

    private fun drawWorldBackground(canvas: Canvas) {
        val bitmap = currentWorldBitmap()
        if (bitmap.width == 0 || bitmap.height == 0) return
        val widthRatio = width.toFloat() / bitmap.width.toFloat()
        val heightRatio = height.toFloat() / bitmap.height.toFloat()
        val scale = max(widthRatio, heightRatio)
        val scaledWidth = bitmap.width * scale
        val scaledHeight = bitmap.height * scale
        val offsetX = (width - scaledWidth) * 0.5f
        val loopOffset = if (scaledHeight <= 0f) 0f else (worldOffset * 0.32f) % scaledHeight
        val drawTop = loopOffset - scaledHeight

        if (currentLevel.number >= 2) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
            drawStars(canvas)
            if (currentLevel.number >= 3) {
                drawPlanet(canvas)
            }
            pulsePaint.color = missionBackgroundTint(currentLevel)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pulsePaint)
            backgroundBitmapPaint.alpha = (235 - (currentLevel.number - 2) * 8).coerceAtLeast(188)
        } else {
            backgroundBitmapPaint.alpha = 255
        }

        canvas.drawBitmap(
            bitmap,
            null,
            RectF(offsetX, drawTop, offsetX + scaledWidth, drawTop + scaledHeight),
            backgroundBitmapPaint
        )
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(offsetX, drawTop + scaledHeight, offsetX + scaledWidth, drawTop + scaledHeight * 2f),
            backgroundBitmapPaint
        )
    }

    private fun drawStars(canvas: Canvas) {
        stars.forEach { star ->
            val alpha = (120 + ((sin(star.twinkle) + 1f) * 0.5f * 135f)).toInt().coerceIn(80, 255)
            starPaint.color = star.color
            starPaint.alpha = alpha
            canvas.drawCircle(star.x, star.y, star.radius, starPaint)
        }
    }

    private fun drawPlanet(canvas: Canvas) {
        val cx = width * 0.84f
        val cy = height * 0.2f
        val radius = min(width, height) * 0.13f
        canvas.drawCircle(cx, cy, radius, planetPaint)
        canvas.drawOval(RectF(cx - radius * 1.35f, cy - radius * 0.36f, cx + radius * 1.35f, cy + radius * 0.36f), ringPaint)
    }

        private fun drawObjects(canvas: Canvas) {
        spaceObjects.forEach { obj ->
            canvas.save()
            canvas.translate(obj.x, obj.y)
            canvas.rotate(obj.rotation)
            val pulseRadius = obj.radius * (1.2f + sin(obj.pulse) * 0.08f)
            pulsePaint.color = when (obj.kind) {
                SpaceKind.CRYSTAL -> Color.argb(90, 128, 216, 255)
                SpaceKind.POWERUP -> Color.argb(110, 124, 255, 203)
                SpaceKind.SHIELD -> Color.argb(130, 125, 249, 255)
                SpaceKind.EXTRA_LIFE -> Color.argb(120, 255, 226, 130)
                SpaceKind.COMET -> Color.argb(110, 255, 138, 101)
                SpaceKind.ROCK -> Color.argb(60, 255, 164, 64)
            }
            canvas.drawCircle(0f, 0f, pulseRadius, pulsePaint)
            when (obj.kind) {
                SpaceKind.ROCK -> {
                    objectPaint.color = Color.parseColor("#8D6E63")
                    objectCorePaint.color = Color.parseColor("#BCAAA4")
                    val r = obj.radius
                    val path = android.graphics.Path().apply {
                        moveTo(-r * 0.9f, -r * 0.2f)
                        lineTo(-r * 0.3f, -r)
                        lineTo(r * 0.55f, -r * 0.72f)
                        lineTo(r, 0f)
                        lineTo(r * 0.4f, r * 0.86f)
                        lineTo(-r * 0.75f, r * 0.6f)
                        close()
                    }
                    canvas.drawPath(path, objectPaint)
                    canvas.drawCircle(-r * 0.15f, -r * 0.12f, r * 0.2f, objectCorePaint)
                    canvas.drawCircle(r * 0.22f, r * 0.18f, r * 0.13f, objectCorePaint)
                }
                SpaceKind.CRYSTAL -> {
                    objectPaint.color = Color.parseColor("#64FFDA")
                    objectCorePaint.color = Color.WHITE
                    val r = obj.radius
                    val path = android.graphics.Path().apply {
                        moveTo(0f, -r)
                        lineTo(r * 0.78f, -r * 0.08f)
                        lineTo(r * 0.35f, r)
                        lineTo(-r * 0.34f, r * 0.78f)
                        lineTo(-r * 0.86f, -r * 0.06f)
                        close()
                    }
                    canvas.drawPath(path, objectPaint)
                    canvas.drawLine(0f, -r, -r * 0.1f, r * 0.78f, objectCorePaint)
                    canvas.drawLine(0f, -r, r * 0.28f, r * 0.92f, objectCorePaint)
                }
                SpaceKind.POWERUP -> {
                    val bubbleRect = RectF(-obj.radius, -obj.radius, obj.radius, obj.radius)
                    canvas.drawCircle(0f, 0f, obj.radius * 1.05f, pulsePaint)
                    canvas.drawBitmap(powerBitmap, null, bubbleRect, null)
                }
                SpaceKind.SHIELD -> {
                    val bubbleRect = RectF(-obj.radius, -obj.radius, obj.radius, obj.radius)
                    canvas.drawCircle(0f, 0f, obj.radius * 1.08f, pulsePaint)
                    canvas.drawBitmap(shieldBubbleBitmap, null, bubbleRect, null)
                }
                SpaceKind.EXTRA_LIFE -> {
                    val bubbleRect = RectF(-obj.radius, -obj.radius, obj.radius, obj.radius)
                    canvas.drawCircle(0f, 0f, obj.radius * 1.06f, pulsePaint)
                    canvas.drawBitmap(extraLifeBitmap, null, bubbleRect, null)
                }
                SpaceKind.COMET -> {
                    objectPaint.color = Color.parseColor("#FF7043")
                    objectCorePaint.color = Color.parseColor("#FFE082")
                    val r = obj.radius
                    pulsePaint.color = Color.argb(95, 255, 112, 67)
                    canvas.drawOval(RectF(-r * 2.3f, -r * 0.38f, -r * 0.35f, r * 0.38f), pulsePaint)
                    val path = android.graphics.Path().apply {
                        moveTo(-r * 0.3f, -r * 0.84f)
                        lineTo(r * 0.5f, -r * 0.2f)
                        lineTo(r * 0.22f, r * 0.92f)
                        lineTo(-r * 0.68f, r * 0.28f)
                        close()
                    }
                    canvas.drawPath(path, objectPaint)
                    canvas.drawCircle(r * 0.08f, 0f, r * 0.2f, objectCorePaint)
                }
            }
            canvas.restore()
        }
    }

    private fun drawEnemyShips(canvas: Canvas) {
        val enemyBitmap = currentEnemyBitmap
        enemyShips.forEach { enemy ->
            canvas.drawBitmap(enemyBitmap, null, RectF(enemy.x - enemy.width * 0.6f, enemy.y - enemy.height * 0.6f, enemy.x + enemy.width * 0.6f, enemy.y + enemy.height * 0.6f), null)
        }
    }

    private fun drawBoss(canvas: Canvas) {
        if (!bossEnemy.active) return
        val pulse = 1f + sin(bossEnemy.time * 2.1f) * 0.03f
        val bossBitmap = currentBossBitmap
        val halfWidth = bossEnemy.width * 0.5f * pulse
        val halfHeight = bossEnemy.height * 0.5f * pulse
        if (bossEnemy.vulnerable) {
            pulsePaint.color = Color.argb(110, 125, 249, 255)
            canvas.drawCircle(
                bossEnemy.x,
                bossEnemy.y,
                max(halfWidth, halfHeight) * (0.95f + sin(bossEnemy.time * 8f) * 0.08f),
                pulsePaint
            )
        }
        canvas.save()
        canvas.translate(bossEnemy.x, bossEnemy.y)
        canvas.rotate(90f)
        canvas.drawBitmap(bossBitmap, null, RectF(-halfWidth, -halfHeight, halfWidth, halfHeight), null)
        canvas.restore()
    }

    private fun drawParticles(canvas: Canvas) {
        particles.forEach { particle ->
            if (!particle.active) return@forEach
            val ratio = (particle.life / particle.maxLife).coerceIn(0f, 1f)
            particlePaint.color = particle.color
            particlePaint.alpha = (ratio * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(particle.x, particle.y, particle.radius, particlePaint)
        }
    }

            private fun drawBullets(canvas: Canvas) {
        bullets.forEach { bullet ->
            if (!bullet.active) return@forEach
            if (bullet.isMissile) {
                bulletGlowPaint.color = Color.argb(130, 255, 138, 101)
                bulletPaint.color = Color.parseColor("#FFD180")
                canvas.drawCircle(bullet.x, bullet.y + bullet.radius * 0.9f, bullet.radius * 1.9f, bulletGlowPaint)
                canvas.drawRoundRect(
                    RectF(
                        bullet.x - bullet.radius * 0.75f,
                        bullet.y - bullet.radius * 1.7f,
                        bullet.x + bullet.radius * 0.75f,
                        bullet.y + bullet.radius * 1.8f
                    ),
                    bullet.radius,
                    bullet.radius,
                    bulletPaint
                )
                bulletPaint.color = Color.parseColor("#FF7043")
                canvas.drawCircle(bullet.x, bullet.y - bullet.radius * 1.15f, bullet.radius * 0.65f, bulletPaint)
            } else {
                bulletGlowPaint.color = Color.argb(110, 255, 245, 157)
                bulletPaint.color = Color.parseColor("#FFE082")
                canvas.drawCircle(bullet.x, bullet.y, bullet.radius * 1.8f, bulletGlowPaint)
                canvas.drawCircle(bullet.x, bullet.y, bullet.radius, bulletPaint)
            }
        }
    }

    private fun drawEnemyShots(canvas: Canvas) {
        enemyShots.forEach { shot ->
            if (!shot.active) return@forEach
            if (shot.isBoss) {
                enemyShotGlowPaint.color = Color.argb(150, 255, 170, 90)
                enemyShotPaint.color = Color.parseColor("#FFD180")
            } else {
                enemyShotGlowPaint.color = Color.argb(110, 255, 120, 120)
                enemyShotPaint.color = Color.parseColor("#FF6E6E")
            }
            canvas.drawCircle(shot.x, shot.y, shot.radius * 1.9f, enemyShotGlowPaint)
            canvas.drawCircle(shot.x, shot.y, shot.radius, enemyShotPaint)
        }
    }

    private fun drawAbsorptions(canvas: Canvas) {
        absorptions.forEach { shard ->
            if (!shard.active) return@forEach
            val alpha = ((1f - shard.progress) * 255).toInt().coerceIn(0, 255)
            absorbGlowPaint.color = shard.color
            absorbGlowPaint.alpha = alpha / 2
            absorbTrailPaint.color = shard.color
            absorbTrailPaint.alpha = alpha
            canvas.drawCircle(shard.x, shard.y, shard.radius * 2.1f, absorbGlowPaint)
            canvas.drawCircle(shard.x, shard.y, shard.radius, absorbTrailPaint)
            canvas.drawLine(shard.x, shard.y, rocketX, rocketY - rocketHeight * 0.3f, absorbTrailPaint)
        }
    }

    private fun drawRocket(canvas: Canvas) {
        val shieldPulse = if (damageShieldTime > 0f) 0.05f else 0f
        val powerShieldPulse = if (shieldPowerTime > 0f) 0.08f else 0f
        val pulse = 1f + sin(pulseTime) * ((if (turboTime > 0f) 0.06f else 0.03f) + if (absorptions.any { it.active }) 0.04f else 0f + if (firingActive) 0.03f else 0f + shieldPulse + powerShieldPulse)
        canvas.save()
        canvas.translate(rocketX, rocketY)
        canvas.scale(pulse, pulse)
        if (shieldPowerTime > 0f) {
            val shieldSize = max(rocketWidth, rocketHeight) * 0.78f
            val alpha = (170 + ((sin(pulseTime * 1.8f) + 1f) * 0.5f * 60f)).toInt().coerceIn(150, 235)
            shieldOverlayPaint.alpha = alpha
            canvas.drawBitmap(
                shieldOverlayBitmap,
                null,
                RectF(-shieldSize, -shieldSize, shieldSize, shieldSize),
                shieldOverlayPaint
            )
        }
        canvas.rotate(-90f)
        canvas.drawBitmap(rocketBitmap, null, RectF(-rocketWidth * 0.5f, -rocketHeight * 0.5f, rocketWidth * 0.5f, rocketHeight * 0.5f), null)
        if (turboTime > 0f || absorptions.any { it.active } || firingActive || damageShieldTime > 0f) {
            pulsePaint.color = when {
                damageShieldTime > 0f -> Color.argb(110, 255, 110, 110)
                turboTime > 0f -> Color.argb(110, 128, 216, 255)
                firingActive -> Color.argb(90, 255, 224, 130)
                else -> Color.argb(100, 100, 255, 218)
            }
            canvas.drawCircle(rocketWidth * 0.05f, 0f, rocketWidth * 0.5f, pulsePaint)
        }
        canvas.restore()
    }

    private fun drawHud(canvas: Canvas) {
        val panel = RectF(width * 0.05f, height * 0.035f, width * 0.95f, height * 0.205f)
        canvas.drawRoundRect(panel, 32f, 32f, hudPanelPaint)
        val leftX = panel.left + width * 0.035f
        val rightX = panel.left + panel.width() * 0.57f
        val row1Top = panel.top + panel.height() * 0.16f
        val row2Top = panel.top + panel.height() * 0.44f
        val row3Top = panel.top + panel.height() * 0.72f
        val leftColumnWidth = panel.width() * 0.34f
        val rightColumnWidth = panel.width() * 0.24f
        val row1Size = pixelSizeForWidth("ROCAS:$score", leftColumnWidth, width * 0.0065f)
        val row2Size = min(
            pixelSizeForWidth("MEJOR:$bestScore", leftColumnWidth, width * 0.0051f),
            pixelSizeForWidth("NIVEL:${currentLevel.number}", rightColumnWidth, width * 0.0051f)
        )
        val comboSize = pixelSizeForWidth("COMBO:X$combo", panel.width() * 0.42f, width * 0.0058f)

        drawPixelText(canvas, "ROCAS:$score", leftX, row1Top, row1Size, Color.WHITE)
        if (selectedMode == GameMode.PRO) {
            val livesLabelSize = pixelSizeForWidth("VIDAS", panel.width() * 0.16f, width * 0.0038f)
            drawPixelText(canvas, "VIDAS", rightX, panel.top + panel.height() * 0.09f, livesLabelSize, Color.parseColor("#C9F4FF"))
            drawLivesIndicator(canvas, rightX, panel.top + panel.height() * 0.2f)
        } else {
            val freeSize = pixelSizeForWidth("LIBRE", panel.width() * 0.16f, width * 0.0046f)
            drawPixelText(canvas, "LIBRE", rightX, row1Top, freeSize, Color.parseColor("#C9F4FF"))
        }
        drawPixelText(canvas, "MEJOR:$bestScore", leftX, row2Top, row2Size, Color.parseColor("#9BE7FF"))
        drawPixelText(canvas, "NIVEL:${currentLevel.number}", rightX, row2Top, row2Size, Color.parseColor("#9BE7FF"))
        drawPixelText(canvas, "COMBO:X$combo", panel.centerX(), row3Top, comboSize, Color.parseColor("#FFE082"), Paint.Align.CENTER)
        if (bossEnemy.active) {
            drawBossHealthBar(canvas, panel.bottom + height * 0.008f)
        }
        hudTextPaint.textAlign = Paint.Align.CENTER
        hudTextPaint.textSize = width * 0.037f
        val helperText = when {
            bossEnemy.active && bossEnemy.vulnerable -> "Boss recargando: dispara ahora"
            bossEnemy.active -> "Boss final atacando"
            shieldPowerTime > 0f -> "Escudo absorbido activo"
            damageShieldTime > 0f -> "Escudo temporal activo"
            missilePowerTime > 0f -> "Poder misil activo"
            absorptions.any { it.active } -> "Cristal absorbido: energia cargando"
            firingActive && turboTime > 0f -> "Rafaga turbo activa"
            firingActive -> "Mantienes pulsado: rafaga activa"
            else -> missionHint(currentLevel)
        }
        canvas.drawText(helperText, width * 0.5f, height * 0.94f, hudTextPaint)
        hudTextPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawBossHealthBar(canvas: Canvas, topY: Float) {
        val barWidth = width * 0.5f
        val barHeight = height * 0.014f
        val left = width * 0.25f
        val healthRect = RectF(left, topY, left + barWidth, topY + barHeight)
        val healthRatio = (bossEnemy.health / bossEnemy.maxHealth.toFloat()).coerceIn(0f, 1f)
        val healthFillRect = RectF(healthRect.left, healthRect.top, healthRect.left + healthRect.width() * healthRatio, healthRect.bottom)

        pulsePaint.color = Color.argb(180, 12, 18, 30)
        canvas.drawRoundRect(healthRect, barHeight, barHeight, pulsePaint)
        pulsePaint.color = Color.parseColor("#FF8A65")
        canvas.drawRoundRect(healthFillRect, barHeight, barHeight, pulsePaint)
        val healthLabel = "BOSS VIDA"
        val healthLabelSize = pixelSizeForWidth(healthLabel, width * 0.3f, width * 0.0042f)
        drawPixelText(canvas, healthLabel, width * 0.5f, topY - height * 0.018f, healthLabelSize, Color.parseColor("#FFE082"), Paint.Align.CENTER)

        if (!bossEnemy.vulnerable) return

        val rechargeTop = healthRect.bottom + height * 0.01f
        val rechargeRect = RectF(left, rechargeTop, left + barWidth, rechargeTop + barHeight)
        val rechargeRatio = (bossEnemy.rechargeTime / missionBossRechargeWindow(currentLevel)).coerceIn(0f, 1f)
        val rechargeFillRect = RectF(rechargeRect.left, rechargeRect.top, rechargeRect.left + rechargeRect.width() * rechargeRatio, rechargeRect.bottom)
        pulsePaint.color = Color.argb(180, 12, 18, 30)
        canvas.drawRoundRect(rechargeRect, barHeight, barHeight, pulsePaint)
        pulsePaint.color = Color.parseColor("#7DF9FF")
        canvas.drawRoundRect(rechargeFillRect, barHeight, barHeight, pulsePaint)
        val rechargeLabel = "RECARGA"
        val rechargeLabelSize = pixelSizeForWidth(rechargeLabel, width * 0.22f, width * 0.0038f)
        drawPixelText(canvas, rechargeLabel, width * 0.5f, rechargeTop - height * 0.016f, rechargeLabelSize, Color.parseColor("#9BE7FF"), Paint.Align.CENTER)
    }

    private fun drawLivesIndicator(canvas: Canvas, startX: Float, topY: Float) {
        val iconWidth = width * 0.06f
        val iconHeight = iconWidth * (rocketBitmap.height.toFloat() / rocketBitmap.width.toFloat())
        val spacing = iconWidth * 0.18f

        repeat(PRO_MODE_LIVES) { index ->
            val left = startX + index * (iconWidth + spacing)
            val top = topY
            val paint = if (index < remainingLives) null else inactiveLifePaint
            canvas.drawBitmap(
                rocketBitmap,
                null,
                RectF(left, top, left + iconWidth, top + iconHeight),
                paint
            )
        }
    }

    private fun drawCover(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), coverOverlayPaint)
        drawCoverHeader(canvas, GAME_TITLE, "ELIGE COMO QUIERES JUGAR")
        drawModeCard(
            canvas = canvas,
            rect = kidsModeButton,
            title = "MODO NINOS",
            description = "No mueren. Juega sin perder vidas.",
            badge = "SIN LIMITE"
        )
        drawModeCard(
            canvas = canvas,
            rect = proModeButton,
            title = "MODO PRO",
            description = "Ahora si tendran nivel de vida.",
            badge = "3 VIDAS"
        )
        drawModeCard(
            canvas = canvas,
            rect = testModeButton,
            title = "MODO TEST",
            description = "Entra directo al nivel que quieras probar.",
            badge = "ATAJO"
        )
        drawRankingSummary(canvas, testModeButton.bottom + height * 0.014f)
        val footerSize = pixelSizeForWidth("USA TEST SI QUIERES SALTAR DE NIVEL", width * 0.92f, width * 0.005f)
        drawPixelText(canvas, "USA TEST SI QUIERES SALTAR DE NIVEL", width * 0.5f, height * 0.94f, footerSize, Color.WHITE, Paint.Align.CENTER)
    }

    private fun drawTestMode(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), coverOverlayPaint)
        drawCoverHeader(canvas, "MODO TEST", "ELIGE MISION Y DIFICULTAD")
        drawModeCard(
            canvas = canvas,
            rect = testKidsButton,
            title = "NINOS",
            description = "Prueba sin quedarte sin vidas.",
            badge = if (testLaunchMode == GameMode.KIDS) "ACTIVO" else "TOCA"
        )
        drawModeCard(
            canvas = canvas,
            rect = testProButton,
            title = "PRO",
            description = "Prueba el balance real de cada nivel.",
            badge = if (testLaunchMode == GameMode.PRO) "ACTIVO" else "TOCA"
        )
        Level.values().forEachIndexed { index, level ->
            drawModeCard(
                canvas = canvas,
                rect = testMissionButtons[index],
                title = "MISION ${level.number}",
                description = missionCardDescription(level),
                badge = if (level.number <= 2) "LISTA" else "NUEVA"
            )
        }
        drawModeCard(
            canvas = canvas,
            rect = testBackButton,
            title = "VOLVER",
            description = "Regresa al menu principal.",
            badge = "MENU"
        )
        val footer = "MODO ACTUAL:${testLaunchMode.label}"
        val footerSize = pixelSizeForWidth(footer, width * 0.68f, width * 0.0052f)
        drawPixelText(canvas, footer, width * 0.5f, height * 0.92f, footerSize, Color.parseColor("#C9F4FF"), Paint.Align.CENTER)
    }

    private fun drawGameOver(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), coverOverlayPaint)
        drawCoverHeader(canvas, "SIN VIDAS", "TU MEJOR PUNTAJE SIGUE GUARDADO")
        val scoreSize = pixelSizeForWidth("ROCAS DESTRUIDAS:$score", width * 0.82f, width * 0.007f)
        drawPixelText(canvas, "ROCAS DESTRUIDAS:$score", width * 0.5f, height * 0.34f, scoreSize, Color.WHITE, Paint.Align.CENTER)
        drawModeCard(
            canvas = canvas,
            rect = kidsModeButton,
            title = "MODO NINOS",
            description = "Vuelves sin morir y sin limite de vidas.",
            badge = "RELAX"
        )
        drawModeCard(
            canvas = canvas,
            rect = proModeButton,
            title = "MODO PRO",
            description = "Reintenta con $PRO_MODE_LIVES vidas.",
            badge = "DESAFIO"
        )
        drawModeCard(
            canvas = canvas,
            rect = testModeButton,
            title = "MODO TEST",
            description = "Salta directo al nivel que quieras ajustar.",
            badge = "RAPIDO"
        )
        drawRankingSummary(canvas, testModeButton.bottom + height * 0.014f)
        val footerSize = pixelSizeForWidth("ELIGE COMO QUIERES VOLVER", width * 0.82f, width * 0.0055f)
        drawPixelText(canvas, "ELIGE COMO QUIERES VOLVER", width * 0.5f, height * 0.94f, footerSize, Color.WHITE, Paint.Align.CENTER)
    }

    private fun drawLevelClear(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), coverOverlayPaint)
        val nextLevel = nextLevelAfter(currentLevel)
        val totalMissions = Level.values().size
        val title = if (nextLevel == null) "NIVEL ${currentLevel.number} COMPLETADO" else "NIVEL ${currentLevel.number} LISTO"
        val subtitle = if (nextLevel == null) "YA TIENES $totalMissions MISIONES BASE" else "TOCA PARA ENTRAR A LA MISION ${nextLevel.number}"
        drawCoverHeader(canvas, title, subtitle)
        val scoreSize = pixelSizeForWidth("PUNTAJE FINAL:$score", width * 0.8f, width * 0.0064f)
        drawPixelText(canvas, "PUNTAJE FINAL:$score", width * 0.5f, height * 0.36f, scoreSize, Color.WHITE, Paint.Align.CENTER)
        val bestSize = pixelSizeForWidth("MEJOR REGISTRO:$bestScore", width * 0.82f, width * 0.0052f)
        drawPixelText(canvas, "MEJOR REGISTRO:$bestScore", width * 0.5f, height * 0.42f, bestSize, Color.parseColor("#C9F4FF"), Paint.Align.CENTER)
        val levelLine = if (nextLevel == null) "FIN DE LA RUTA ACTUAL" else "SIGUE: MISION ${nextLevel.number}"
        val levelLineSize = pixelSizeForWidth(levelLine, width * 0.72f, width * 0.0055f)
        drawPixelText(canvas, levelLine, width * 0.5f, height * 0.5f, levelLineSize, Color.parseColor("#FFE082"), Paint.Align.CENTER)
        val footerText = if (nextLevel == null) "TOCA PARA VOLVER AL MENU" else "TOCA PARA CONTINUAR"
        val footerSize = pixelSizeForWidth(footerText, width * 0.82f, width * 0.0055f)
        drawPixelText(canvas, footerText, width * 0.5f, height * 0.86f, footerSize, Color.parseColor("#FFE082"), Paint.Align.CENTER)
    }

    private fun drawCoverHeader(canvas: Canvas, title: String, subtitle: String) {
        val titlePixelSize = pixelSizeForWidth(title, width * 0.82f, width * 0.016f)
        val subtitlePixelSize = pixelSizeForWidth(subtitle, width * 0.84f, width * 0.0064f)
        drawPixelText(canvas, title, width * 0.5f, height * 0.1f, titlePixelSize, Color.parseColor("#FFE082"), Paint.Align.CENTER, Color.argb(180, 31, 98, 255))
        drawPixelText(canvas, subtitle, width * 0.5f, height * 0.235f, subtitlePixelSize, Color.WHITE, Paint.Align.CENTER)
    }

    private fun drawModeCard(canvas: Canvas, rect: RectF, title: String, description: String, badge: String) {
        canvas.drawRoundRect(rect, 44f, 44f, coverPanelPaint)
        canvas.drawRoundRect(rect, 44f, 44f, coverStrokePaint)
        val chipWidth = rect.width() * 0.28f
        val chipHeight = rect.height() * 0.28f
        val chip = RectF(rect.right - chipWidth - width * 0.04f, rect.top + rect.height() * 0.18f, rect.right - width * 0.04f, rect.top + rect.height() * 0.18f + chipHeight)
        canvas.drawRoundRect(chip, chipHeight * 0.5f, chipHeight * 0.5f, coverChipPaint)
        val badgeSize = pixelSizeForWidth(badge, chip.width() * 0.8f, width * 0.0043f)
        drawPixelText(canvas, badge, chip.centerX(), chip.top + chip.height() * 0.22f, badgeSize, Color.WHITE, Paint.Align.CENTER)
        val titleSize = pixelSizeForWidth(title, rect.width() * 0.52f, width * 0.0075f)
        drawPixelText(canvas, title, rect.left + width * 0.05f, rect.top + rect.height() * 0.2f, titleSize, Color.WHITE)
        coverSubtitlePaint.textAlign = Paint.Align.LEFT
        coverSubtitlePaint.textSize = width * 0.038f
        canvas.drawText(description, rect.left + width * 0.05f, rect.top + rect.height() * 0.68f, coverSubtitlePaint)
        coverSubtitlePaint.textAlign = Paint.Align.CENTER
    }

    private fun drawRankingSummary(canvas: Canvas, topY: Float) {
        val title = "RANKING"
        val scoreLine = "MAXIMO POR PARTIDA:$bestScore"
        val titleSize = pixelSizeForWidth(title, width * 0.34f, width * 0.0058f)
        val scoreSize = pixelSizeForWidth(scoreLine, width * 0.72f, width * 0.0046f)

        drawPixelText(canvas, title, width * 0.5f, topY, titleSize, Color.parseColor("#FFE082"), Paint.Align.CENTER)
        drawPixelText(canvas, scoreLine, width * 0.5f, topY + height * 0.03f, scoreSize, Color.parseColor("#C9F4FF"), Paint.Align.CENTER)
    }

    private fun livesLabel(): String {
        return if (selectedMode == GameMode.KIDS) "Vidas: infinitas" else "Vidas: $remainingLives"
    }

    private fun livesHudLabel(): String {
        return if (selectedMode == GameMode.KIDS) "VIDAS:INF" else "VIDAS:$remainingLives"
    }

    private fun pixelSizeForWidth(text: String, maxWidth: Float, preferredPixelSize: Float): Float {
        val unitCount = pixelTextUnitCount(text).coerceAtLeast(1)
        return min(preferredPixelSize, maxWidth / unitCount)
    }

    private fun pixelTextUnitCount(text: String): Int {
        val normalized = text.uppercase()
        if (normalized.isEmpty()) return 0

        var units = 0
        normalized.forEachIndexed { index, char ->
            units += pixelGlyph(char).firstOrNull()?.length ?: 5
            if (index != normalized.lastIndex) units += 1
        }
        return units
    }

    private fun drawPixelText(
        canvas: Canvas,
        text: String,
        x: Float,
        topY: Float,
        pixelSize: Float,
        color: Int,
        align: Paint.Align = Paint.Align.LEFT,
        shadowColor: Int = Color.argb(110, 5, 10, 25)
    ) {
        val normalized = text.uppercase()
        if (normalized.isEmpty()) return

        val totalWidth = pixelTextUnitCount(normalized) * pixelSize
        var startX = when (align) {
            Paint.Align.CENTER -> x - totalWidth * 0.5f
            Paint.Align.RIGHT -> x - totalWidth
            else -> x
        }
        val shadowOffset = max(1f, pixelSize * 0.42f)

        normalized.forEachIndexed { index, char ->
            val glyph = pixelGlyph(char)
            drawPixelGlyph(canvas, glyph, startX + shadowOffset, topY + shadowOffset, pixelSize, shadowColor, pixelShadowPaint)
            drawPixelGlyph(canvas, glyph, startX, topY, pixelSize, color, pixelTextPaint)
            startX += glyph.first().length * pixelSize
            if (index != normalized.lastIndex) {
                startX += pixelSize
            }
        }
    }

    private fun drawPixelGlyph(
        canvas: Canvas,
        glyph: List<String>,
        startX: Float,
        startY: Float,
        pixelSize: Float,
        color: Int,
        paint: Paint
    ) {
        paint.color = color
        glyph.forEachIndexed { row, line ->
            line.forEachIndexed { column, bit ->
                if (bit != '1') return@forEachIndexed
                val left = startX + column * pixelSize
                val top = startY + row * pixelSize
                canvas.drawRect(left, top, left + pixelSize, top + pixelSize, paint)
            }
        }
    }

    private fun pixelGlyph(char: Char): List<String> {
        return PIXEL_FONT[char.uppercaseChar()] ?: PIXEL_FONT.getValue('?')
    }

    private fun drawFlash(canvas: Canvas) {
        if (flashTime <= 0f) return
        val alpha = (flashTime / 0.22f * 160).toInt().coerceIn(0, 160)
        flashPaint.color = Color.argb(alpha, 255, 245, 220)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flashPaint)
    }

    private fun resetStars() {
        stars.forEachIndexed { index, star ->
            resetStar(star, false)
            star.y = height * (index / stars.size.toFloat())
        }
    }

    private fun resetStar(star: Star, fromTop: Boolean) {
        star.x = Random.nextFloat() * width
        star.y = if (fromTop) -Random.nextFloat() * height * 0.35f else Random.nextFloat() * height
        star.radius = width * (0.002f + Random.nextFloat() * 0.007f)
        star.speed = height * (0.05f + Random.nextFloat() * 0.35f)
        star.twinkle = Random.nextFloat() * Math.PI.toFloat() * 2f
        star.twinkleSpeed = 1f + Random.nextFloat() * 5f
        star.color = listOf(Color.WHITE, Color.parseColor("#FFF59D"), Color.parseColor("#B3E5FC"), Color.parseColor("#FFCCBC")).random()
    }

    private fun resetObjects() {
        spaceObjects.forEachIndexed { index, obj ->
            resetObject(obj, false)
        }
    }

    private fun resetObject(obj: SpaceObject, fromTop: Boolean) {
        val roll = Random.nextFloat()
        obj.kind = when {
            selectedMode == GameMode.PRO && roll > 0.97f -> SpaceKind.EXTRA_LIFE
            missionCometChance(currentLevel) > 0f && roll > 1f - missionCometChance(currentLevel) -> SpaceKind.COMET
            roll > 0.885f -> SpaceKind.SHIELD
            roll > 0.765f -> SpaceKind.POWERUP
            roll > 0.3f -> SpaceKind.ROCK
            else -> SpaceKind.CRYSTAL
        }
        obj.radius = when (obj.kind) {
            SpaceKind.ROCK -> width * (0.042f + Random.nextFloat() * 0.04f)
            SpaceKind.CRYSTAL -> width * (0.03f + Random.nextFloat() * 0.028f)
            SpaceKind.POWERUP -> width * (0.05f + Random.nextFloat() * 0.018f)
            SpaceKind.SHIELD -> width * (0.052f + Random.nextFloat() * 0.016f)
            SpaceKind.EXTRA_LIFE -> width * (0.048f + Random.nextFloat() * 0.014f)
            SpaceKind.COMET -> width * (0.038f + Random.nextFloat() * 0.024f)
        }
        obj.x = width * (0.1f + Random.nextFloat() * 0.8f)
        obj.y = if (fromTop) -Random.nextFloat() * height * 0.35f else height * (0.14f + Random.nextFloat() * 0.4f)
        obj.speed = when (obj.kind) {
            SpaceKind.COMET -> height * (0.34f + Random.nextFloat() * 0.28f) * missionObjectSpeedMultiplier(currentLevel)
            else -> height * (0.16f + Random.nextFloat() * 0.3f) * missionObjectSpeedMultiplier(currentLevel)
        }
        obj.rotation = Random.nextFloat() * 360f
        obj.rotationSpeed = if (obj.kind == SpaceKind.COMET) -220f + Random.nextFloat() * 440f else -110f + Random.nextFloat() * 220f
        obj.pulse = Random.nextFloat() * Math.PI.toFloat() * 2f
        obj.pulseSpeed = 1.5f + Random.nextFloat() * 4f
        obj.collisionCooldown = 0f
    }

    private fun resetEnemies() {
        enemyShips.forEachIndexed { index, enemy ->
            resetEnemy(enemy, false)
        }
    }

    private fun resetEnemy(enemy: EnemyShip, fromTop: Boolean) {
        enemy.width = width * (0.14f + Random.nextFloat() * 0.035f + (currentLevel.number - 1) * 0.005f)
        val enemyBitmap = currentEnemyBitmap
        enemy.height = enemy.width * (enemyBitmap.height.toFloat() / enemyBitmap.width.toFloat())
        enemy.x = width * (0.15f + Random.nextFloat() * 0.7f)
        enemy.y = if (fromTop) -enemy.height - Random.nextFloat() * height * 0.25f else height * (0.14f + Random.nextFloat() * 0.35f)
        enemy.speed = height * (0.18f + Random.nextFloat() * 0.18f) * missionEnemySpeedMultiplier(currentLevel)
        enemy.waveAmplitude = width * (0.04f + Random.nextFloat() * 0.05f + (currentLevel.number - 1) * 0.006f)
        enemy.waveSpeed = 1.6f + Random.nextFloat() * 1.8f + (currentLevel.number - 1) * 0.12f
        enemy.time = Random.nextFloat() * 10f
        enemy.shotCooldown = max(0.24f, 0.5f + Random.nextFloat() * 1.1f - (currentLevel.number - 1) * 0.08f)
    }

    private data class Star(
        var x: Float = 0f,
        var y: Float = 0f,
        var radius: Float = 0f,
        var speed: Float = 0f,
        var twinkle: Float = 0f,
        var twinkleSpeed: Float = 0f,
        var color: Int = Color.WHITE
    )

    private data class SpaceObject(
        var x: Float = 0f,
        var y: Float = 0f,
        var radius: Float = 0f,
        var speed: Float = 0f,
        var rotation: Float = 0f,
        var rotationSpeed: Float = 0f,
        var pulse: Float = 0f,
        var pulseSpeed: Float = 0f,
        var collisionCooldown: Float = 0f,
        var kind: SpaceKind = SpaceKind.ROCK
    )

    private data class EnemyShip(
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Float = 0f,
        var height: Float = 0f,
        var speed: Float = 0f,
        var waveAmplitude: Float = 0f,
        var waveSpeed: Float = 0f,
        var shotCooldown: Float = 0f,
        var time: Float = 0f
    )

    private data class BossEnemy(
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Float = 0f,
        var height: Float = 0f,
        var health: Int = BASE_BOSS_MAX_HEALTH,
        var maxHealth: Int = BASE_BOSS_MAX_HEALTH,
        var shotCooldown: Float = 0f,
        var attackTime: Float = 0f,
        var rechargeTime: Float = 0f,
        var vulnerable: Boolean = false,
        var time: Float = 0f,
        var active: Boolean = false
    )

    private data class Particle(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var life: Float = 0f,
        var maxLife: Float = 0f,
        var radius: Float = 0f,
        var color: Int = Color.WHITE,
        var active: Boolean = false
    )

    private data class Bullet(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var life: Float = 0f,
        var radius: Float = 0f,
        var isMissile: Boolean = false,
        var active: Boolean = false
    )

    private data class EnemyShot(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var life: Float = 0f,
        var radius: Float = 0f,
        var isBoss: Boolean = false,
        var active: Boolean = false
    )

    private data class AbsorptionShard(
        var x: Float = 0f,
        var y: Float = 0f,
        var startX: Float = 0f,
        var startY: Float = 0f,
        var progress: Float = 0f,
        var duration: Float = 0.4f,
        var life: Float = 0f,
        var radius: Float = 0f,
        var baseRadius: Float = 0f,
        var arcHeight: Float = 0f,
        var arcPhase: Float = 0f,
        var color: Int = Color.WHITE,
        var active: Boolean = false
    )

    private enum class SpaceKind {
        ROCK,
        CRYSTAL,
        POWERUP,
        SHIELD,
        EXTRA_LIFE,
        COMET
    }

    private enum class Level(val number: Int, val label: String) {
        ONE(1, "NIVEL 1"),
        TWO(2, "NIVEL 2"),
        THREE(3, "NIVEL 3"),
        FOUR(4, "NIVEL 4"),
        FIVE(5, "NIVEL 5"),
        SIX(6, "NIVEL 6"),
        SEVEN(7, "NIVEL 7"),
        EIGHT(8, "NIVEL 8"),
        NINE(9, "NIVEL 9"),
        TEN(10, "NIVEL 10"),
        ELEVEN(11, "NIVEL 11"),
        TWELVE(12, "NIVEL 12")
    }

    private enum class GameMode(val label: String) {
        KIDS("NINOS"),
        PRO("PRO")
    }

    private enum class ScreenState {
        COVER,
        TEST_MODE,
        PLAYING,
        GAME_OVER,
        LEVEL_CLEAR
    }
}
























