package com.example.addsticker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import com.example.addsticker.adapter.FontPickerAdapter
import com.example.addsticker.bottomsheet.StickerBottomSheet
import com.example.addsticker.databinding.ActivityAddStickerBinding
import com.example.addsticker.rxbus.RxBus
import com.example.addsticker.rxbus.RxEvent
import com.rtugeek.android.colorseekbar.ColorSeekBar
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow


class AddStickerActivity : AppCompatActivity() {

    companion object {
        private const val STICKER = "STICKER"
        private const val EMOJI = "EMOJI"
        private const val CROP = "CROP"
        private const val TEXT = "TEXT"
        private const val BLUR = "BLUR"
        private const val TEXT_ALIGN_CENTER = "TEXT_ALIGN_CENTER"
        private const val TEXT_ALIGN_RIGHT = "TEXT_ALIGN_RIGHT"
        private const val TEXT_ALIGN_LEFT = "TEXT_ALIGN_LEFT"
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    private lateinit var binding: ActivityAddStickerBinding
    private lateinit var stickerBottomSheet: StickerBottomSheet
    private lateinit var imageUri: String
    private lateinit var orijinalImageUri: String
    private lateinit var orijinalFileName: String
    private var emojisIndex: Int? = null
    private var selectedItem: String? = null
    private val drawnEmojis = mutableListOf<Pair<Float, Float>>()
    private var fontList: ArrayList<Int> = arrayListOf()
    private var lastX = -1f
    private var lastY = -1f
    private var colorCodeTextView: Int = R.color.black
    private var fontId: Int = R.font.abraham_lincoln
    private var textAlignPosition: String = TEXT_ALIGN_CENTER
    private var currentTextView: TextView? = null
    private var isBackGroundAdd = false

    // custom drag + movable textview variable
    private var lastEvent: FloatArray? = null
    private var d = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f

    // eraser variable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stickerBottomSheet = StickerBottomSheet(this@AddStickerActivity)
        imageUri = intent.getStringExtra(MainActivity.PUT_EXTRA_IMAGE_URI).toString()
        orijinalImageUri = intent.getStringExtra(MainActivity.PUT_EXTRA_IMAGE_URI).toString()
        imageUri.let {
            orijinalFileName = getFileNameFromUri(this@AddStickerActivity, imageUri.toUri())
            binding.photoEditorView.setImageURI(Uri.parse(it))
        }
        initializeFontList()
        initEraser()
        initUI()
    }

    private fun initializeFontList() {
        fontList.add(R.font.abraham_lincoln)
        fontList.add(R.font.airship_27_regular)
        fontList.add(R.font.arvil_sans)
        fontList.add(R.font.bender_lnline)
        fontList.add(R.font.blanch_condensed)
        fontList.add(R.font.cubano_regular_webfont)
        fontList.add(R.font.franchise_bold)
        fontList.add(R.font.geared_slab)
        fontList.add(R.font.governor)
        fontList.add(R.font.haymaker)
        fontList.add(R.font.homestead_regular)
        fontList.add(R.font.liberator)
        fontList.add(R.font.maven_pro_light_200)
        fontList.add(R.font.mensch)
        fontList.add(R.font.muncie)
        fontList.add(R.font.sullivan_regular)
        fontList.add(R.font.tommaso)
        fontList.add(R.font.valencia_regular)
        fontList.add(R.font.vevey)

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEraser() {

    }


    @SuppressLint("CheckResult", "ClickableViewAccessibility", "ResourceAsColor")
    private fun initUI() {

        binding.ivErase.setOnClickListener {
            selectedItem = BLUR
            binding.ivErase.visibility = View.GONE
            binding.ivEraseClose.visibility = View.VISIBLE
        }

        binding.ivEraseClose.setOnClickListener {
            selectedItem = null
            binding.ivEraseClose.visibility = View.GONE
            binding.ivErase.visibility = View.VISIBLE
        }

        binding.textEditor.setOnClickListener {
            selectedItem = TEXT
            binding.removeEmojis.visibility = View.GONE
            binding.addEmojis.visibility = View.VISIBLE
            binding.emojisCardView.visibility = View.GONE
            fontId = R.font.abraham_lincoln
            openAddTextPopupWindow()
            textAlignPosition = TEXT_ALIGN_CENTER
            isBackGroundAdd = false
        }

        binding.colorSeekBar.setOnColorChangeListener { progress, color ->

        }

        binding.addSticker.setOnClickListener {
            selectedItem = STICKER
            emojisIndex = null
            binding.removeEmojis.visibility = View.GONE
            binding.addEmojis.visibility = View.VISIBLE
            binding.emojisCardView.visibility = View.GONE
            openBottomSheet()
        }

        binding.done.setOnClickListener {
            saveImageWithDrawable()
            setDrawable(-1)
        }

        binding.crop.setOnClickListener {
            selectedItem = selectedItem
            emojisIndex = null
            drawnEmojis.clear()
            startCrop(imageUri.toUri())
        }

        binding.smileEmoji.setOnClickListener {
            it.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = 0
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.heartEmoji.setOnClickListener {
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = 1
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.happyEmoji.setOnClickListener {
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = 2
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.fireEmoji.setOnClickListener {
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = 3
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.kissEmoji.setOnClickListener {
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = 4
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.ghostEmoji.setOnClickListener {
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = 5
            drawnEmojis.clear()
            selectedItem = CROP
            setDrawable(emojisIndex!!)
        }

        binding.cryEmoji.setOnClickListener {
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = 6
            drawnEmojis.clear()
            setDrawable(emojisIndex!!)
        }

        binding.removeEmojis.setOnClickListener {
            binding.removeEmojis.visibility = View.GONE
            binding.addEmojis.visibility = View.VISIBLE
            binding.emojisCardView.visibility = View.GONE
            emojisIndex = null
            selectedItem = null
            drawnEmojis.clear()
        }

        binding.clearAllView.setOnClickListener {
            binding.emojiContainer.removeAllViews()
            binding.photoEditorView.setImageURI(Uri.parse(orijinalImageUri))
            selectedItem = null
            emojisIndex = null
            binding.removeEmojis.visibility = View.GONE
            binding.addEmojis.visibility = View.VISIBLE
            binding.emojisCardView.visibility = View.GONE
            binding.ivEraseClose.visibility = View.GONE
            binding.ivErase.visibility = View.VISIBLE
            setDrawable(-1)
            imageUri = orijinalImageUri
        }

        binding.addEmojis.setOnClickListener {
            selectedItem = EMOJI
            binding.removeEmojis.visibility = View.VISIBLE
            binding.addEmojis.visibility = View.GONE
            binding.emojisCardView.visibility = View.VISIBLE
        }

        RxBus.listen(RxEvent.ItemClick::class.java).subscribe { event ->
            val bitmap = BitmapFactory.decodeResource(resources, event.itemId)

            val imageView = ImageView(this).apply {
                setImageBitmap(bitmap)
            }

            val imageViewParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
            }
            binding.emojiContainer.addView(imageView, imageViewParams)

            imageView.setOnTouchListener { v, event ->
                val view = v as ImageView
                view.bringToFront()
                viewTransformation(view, event)
                true
            }
            binding.done.visibility = View.VISIBLE
            stickerBottomSheet.dismiss()
        }

    }


    @SuppressLint("CheckResult", "MissingInflatedId")
    private fun openAddTextPopupWindow() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val addTextPopupWindowRootView: View = inflater.inflate(R.layout.add_text_popup_window, null)
        val addTextEditText = addTextPopupWindowRootView.findViewById<View>(R.id.add_text_edit_text) as EditText
        addTextEditText.requestFocus()
        val addTextDoneTextView = addTextPopupWindowRootView.findViewById<View>(R.id.add_text_done_tv) as TextView
        val colorPicker = addTextPopupWindowRootView.findViewById<View>(R.id.colorSeekBarInPopPop) as ColorSeekBar
        val textCenter = addTextPopupWindowRootView.findViewById<View>(R.id.textCenter) as ImageView
        val ivAddBackGround = addTextPopupWindowRootView.findViewById<View>(R.id.ivAddBackGround) as ImageView
        val etLinearLayout = addTextPopupWindowRootView.findViewById<View>(R.id.llEditTextView) as LinearLayout
        val fontPickerRecyclerView = addTextPopupWindowRootView.findViewById<View>(R.id.font_picker_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(this@AddStickerActivity, LinearLayoutManager.HORIZONTAL, false)
        fontPickerRecyclerView.layoutManager = layoutManager
        fontPickerRecyclerView.setHasFixedSize(true)
        val fontPickerAdapter = FontPickerAdapter(this@AddStickerActivity, fontList)
        fontPickerAdapter.setOnFontPickerClickListener(object : FontPickerAdapter.OnFontPickerClickListener {
            override fun onFontPickerClickListener(fontCode: Int) {
                fontId = fontCode
                val typeface = resources.getFont(fontCode)
                addTextEditText.typeface = typeface
            }
        })
        fontPickerRecyclerView.adapter = fontPickerAdapter

        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (firstVisiblePosition != RecyclerView.NO_POSITION) {
                        val viewAtPosition = layoutManager.findViewByPosition(firstVisiblePosition)
                        if (viewAtPosition != null) {
                            Log.d("ScrollListener", "First completely visible item position: $firstVisiblePosition")
                            fontId = fontList[firstVisiblePosition]
                            val typeface = resources.getFont(fontList[firstVisiblePosition])
                            addTextEditText.typeface = typeface
                            fontPickerAdapter.setSelectedPosition(firstVisiblePosition)
                        } else {
                            Log.d("ScrollListener", "No view found at position $firstVisiblePosition")
                        }
                    } else {
                        Log.d("ScrollListener", "No completely visible item found")
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Log.d("ScrollListener", "RecyclerView is dragging")
                }
            }
        }

        fontPickerRecyclerView.addOnScrollListener(scrollListener)

        colorPicker.setOnColorChangeListener { progress, color ->
            val newColorCode: String = decimalToHex(color)
            colorCodeTextView = color
            addTextEditText.setTextColor(Color.parseColor(newColorCode))
            addTextEditText.text = addTextEditText.text

            val textContent: String =
                if (addTextEditText.text.toString().startsWith(" ")) addTextEditText.text.toString().drop(1) else addTextEditText.text.toString()// remove first letter space
            addTextEditText.text = Editable.Factory.getInstance().newEditable(textContent) // remove first letter space
        }


        val pop = PopupWindow(this@AddStickerActivity)
        pop.contentView = addTextPopupWindowRootView
        pop.width = LinearLayout.LayoutParams.MATCH_PARENT
        pop.height = LinearLayout.LayoutParams.MATCH_PARENT
        pop.isFocusable = true
        pop.setBackgroundDrawable(null)
        pop.showAtLocation(addTextPopupWindowRootView, Gravity.TOP, 0, 0)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        addTextDoneTextView.setOnClickListener { view ->
            if (stringIsNotEmpty(addTextEditText.text.toString())) {
                addTextOnImage(addTextEditText.text.toString())
            }
            val inputMethodService = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodService.hideSoftInputFromWindow(view.windowToken, 0)
            pop.dismiss()
            selectedItem = null
        }
        textCenter.setOnClickListener {
            val textContent: String =
                if (addTextEditText.text.toString().startsWith(" ")) addTextEditText.text.toString().drop(1) else addTextEditText.text.toString() // remove first letter space
            addTextEditText.text = Editable.Factory.getInstance().newEditable(textContent) // remove first letter space
            when (etLinearLayout.gravity) {
                Gravity.CENTER -> {
                    etLinearLayout.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    addTextEditText.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    textAlignPosition = TEXT_ALIGN_RIGHT
                }
                Gravity.END or Gravity.CENTER_VERTICAL -> {
                    etLinearLayout.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    addTextEditText.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    textAlignPosition = TEXT_ALIGN_LEFT
                }
                else -> {
                    etLinearLayout.gravity = Gravity.CENTER
                    addTextEditText.gravity = Gravity.CENTER
                    textAlignPosition = TEXT_ALIGN_CENTER
                }
            }
        }
        ivAddBackGround.setOnClickListener {
            val textContent: String =
                if (addTextEditText.text.toString().startsWith(" ")) addTextEditText.text.toString().drop(1) else addTextEditText.text.toString() // remove first letter space
            addTextEditText.text = Editable.Factory.getInstance().newEditable(textContent) // remove first letter space
            isBackGroundAdd = !isBackGroundAdd
            if (isBackGroundAdd) {
                ivAddBackGround.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                addTextEditText.background = (ContextCompat.getDrawable(this, R.drawable.rounded_border))
            } else {
                ivAddBackGround.setColorFilter(ContextCompat.getColor(this, R.color.white))
                addTextEditText.setBackgroundColor(0)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addTextOnImage(content: String) {
        val textContent = if (content.startsWith(" ")) content.drop(1) else content // remove first letter space
        val textView = TextView(this).apply {
            text = textContent
            textSize = 26f
            colorCodeTextView?.let { setTextColor(it) }  // add text color
            textAlignment = when (textAlignPosition) {    // add text align
                TEXT_ALIGN_LEFT -> {
                    View.TEXT_ALIGNMENT_TEXT_START
                }
                TEXT_ALIGN_RIGHT -> {
                    View.TEXT_ALIGNMENT_TEXT_END
                }
                else -> View.TEXT_ALIGNMENT_CENTER
            }
            if (isBackGroundAdd) {
                setBackgroundResource(R.drawable.rounded_border) // add background
            }
            typeface = ResourcesCompat.getFont(context, fontId) // add font family
        }

        val textViewParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = when (textAlignPosition) {
                TEXT_ALIGN_LEFT -> Gravity.START or Gravity.CENTER_VERTICAL
                TEXT_ALIGN_RIGHT -> Gravity.END or Gravity.CENTER_VERTICAL
                else -> Gravity.CENTER or Gravity.CENTER_VERTICAL
            }
        }
        binding.emojiContainer.addView(textView, textViewParams)
        currentTextView = textView

        textView.setOnTouchListener { v, event ->
            val view = v as TextView
            view.bringToFront()
            viewTransformation(view, event)
            true
        }
    }

    private fun viewTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("Event", "MotionEvent.ACTION_DOWN")
                binding.deleteTv.visibility = View.VISIBLE
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY

                start.set(event.x, event.y)
                isOutSide = false
                mode = DRAG
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.d("Event", "MotionEvent.ACTION_POINTER_DOWN")
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }

                lastEvent = FloatArray(4)
                lastEvent?.set(0, event.getX(0))
                lastEvent?.set(1, event.getX(1))
                lastEvent?.set(2, event.getY(0))
                lastEvent?.set(3, event.getY(1))
                d = rotation(event)
            }
            MotionEvent.ACTION_UP -> {
                Log.d("Event", "MotionEvent.ACTION_UP")
                if (isViewOverlapping(view, binding.deleteTv)) {
                    binding.deleteTv.setColorFilter(R.color.red)
                    // Remove the text view from the layout
                    Log.d("Event", "Remove the text view from the layout")
                    colorCodeTextView = R.color.black
                    fontId = R.font.abraham_lincoln
                    binding.emojiContainer.removeView(view)
                }
                binding.deleteTv.visibility = View.GONE
                isZoomAndRotate = false
            }
            MotionEvent.ACTION_OUTSIDE -> {
                Log.d("Event", "MotionEvent.ACTION_OUTSIDE")
                isOutSide = true
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                Log.d("Event", " MotionEvent.ACTION_POINTER_UP")
                mode = NONE
                lastEvent = null

            }
            MotionEvent.ACTION_MOVE -> {
                Log.d("Event", "MotionEvent.ACTION_MOVE")
                if (isViewOverlapping(view, binding.deleteTv)) {
                    binding.deleteTv.setColorFilter(ContextCompat.getColor(this, R.color.red))
                } else {
                    binding.deleteTv.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                }
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false
                        view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate).setDuration(0).start()
                    }
                    if (mode == ZOOM && event.pointerCount == 2) {
                        val newDist1 = spacing(event)
                        if (newDist1 > 10f) {
                            val scale = newDist1 / oldDist * view.scaleX
                            view.scaleX = scale
                            view.scaleY = scale
                        }
                        lastEvent?.let {
                            val newRot = rotation(event)
                            view.rotation = view.rotation + (newRot - d)
                        }
                    }
                }
            }
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    private fun decimalToHex(decimalColor: Int): String {
        // Convert decimal color to hexadecimal and strip the '0x' prefix
        val hexColor = Integer.toHexString(decimalColor and 0xFFFFFF).toUpperCase()
        // Ensure the hex color is 6 characters long
        val paddedHexColor = hexColor.padStart(6, '0')
        // Add the '#' prefix
        return "#$paddedHexColor"
    }

    private fun isViewOverlapping(view: View, otherView: View): Boolean {
        val viewRect = Rect()
        val otherRect = Rect()
        view.getHitRect(viewRect)
        otherView.getHitRect(otherRect)
        return Rect.intersects(viewRect, otherRect)
    }

    private fun stringIsNotEmpty(string: String?): Boolean {
        if (string != null && string != "null") {
            if (string.trim { it <= ' ' } != "") {
                return true
            }
        }
        return false
    }

    private fun setDrawable(indexOfEmojis: Int) {
        when (indexOfEmojis) {
            0 -> {
                binding.smileEmoji.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
                binding.heartEmoji.background = null
                binding.happyEmoji.background = null
                binding.fireEmoji.background = null
                binding.kissEmoji.background = null
                binding.ghostEmoji.background = null
                binding.cryEmoji.background = null
            }
            1 -> {
                binding.smileEmoji.background = null
                binding.heartEmoji.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
                binding.happyEmoji.background = null
                binding.fireEmoji.background = null
                binding.kissEmoji.background = null
                binding.ghostEmoji.background = null
                binding.cryEmoji.background = null
            }
            2 -> {
                binding.smileEmoji.background = null
                binding.heartEmoji.background = null
                binding.happyEmoji.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
                binding.fireEmoji.background = null
                binding.kissEmoji.background = null
                binding.ghostEmoji.background = null
                binding.cryEmoji.background = null
            }
            3 -> {
                binding.smileEmoji.background = null
                binding.heartEmoji.background = null
                binding.happyEmoji.background = null
                binding.fireEmoji.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
                binding.kissEmoji.background = null
                binding.ghostEmoji.background = null
                binding.cryEmoji.background = null
            }
            4 -> {
                binding.smileEmoji.background = null
                binding.heartEmoji.background = null
                binding.happyEmoji.background = null
                binding.fireEmoji.background = null
                binding.kissEmoji.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
                binding.ghostEmoji.background = null
                binding.cryEmoji.background = null
            }
            5 -> {
                binding.smileEmoji.background = null
                binding.heartEmoji.background = null
                binding.happyEmoji.background = null
                binding.fireEmoji.background = null
                binding.kissEmoji.background = null
                binding.ghostEmoji.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
                binding.cryEmoji.background = null
            }
            6 -> {
                binding.smileEmoji.background = null
                binding.heartEmoji.background = null
                binding.happyEmoji.background = null
                binding.fireEmoji.background = null
                binding.kissEmoji.background = null
                binding.ghostEmoji.background = null
                binding.cryEmoji.background = ContextCompat.getDrawable(this, R.drawable.select_emoji_background)
            }
            else -> {
                binding.smileEmoji.background = null
                binding.heartEmoji.background = null
                binding.happyEmoji.background = null
                binding.fireEmoji.background = null
                binding.kissEmoji.background = null
                binding.ghostEmoji.background = null
                binding.cryEmoji.background = null
            }
        }
    }

    private fun openBottomSheet() {
        stickerBottomSheet.show(supportFragmentManager, StickerBottomSheet.TAG)
    }


    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "IMG_" + System.currentTimeMillis()))

        val options = UCrop.Options().apply {
            setCompressionQuality(70)
            setFreeStyleCropEnabled(true)
        }

        UCrop.of(uri, destinationUri).withOptions(options).withAspectRatio(1f, 1f).start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            data?.let {
                val resultUri = UCrop.getOutput(it)
                resultUri?.let { uri ->
                    binding.photoEditorView.setImageURI(uri)
                    selectedItem = null
                    saveImageToLocalStorage(uri)
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            data?.let {
                val cropError = UCrop.getError(it)
                cropError?.let { error ->
                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToLocalStorage(imageUri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val dir = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES)
            val demoDir = File(dir, "demo")
            if (!demoDir.exists()) {
                demoDir.mkdir()
            }
            val file = File(demoDir, "$orijinalFileName.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "Image saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // Function to get file name from URI
    @SuppressLint("Range")
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = ""
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

    private fun saveImageWithDrawable() {
        try {
            // Create a Bitmap with the same size as the PhotoEditorView
            val photoEditorView = binding.photoEditorView
            val emojiContainer = binding.emojiContainer

            // Measure and layout the emoji container to the dimensions of the photo editor view
            emojiContainer.measure(
                View.MeasureSpec.makeMeasureSpec(photoEditorView.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(photoEditorView.height, View.MeasureSpec.EXACTLY)
            )
            emojiContainer.layout(0, 0, emojiContainer.measuredWidth, emojiContainer.measuredHeight)

            // Create a Bitmap to combine both views
            val bitmap = Bitmap.createBitmap(photoEditorView.width, photoEditorView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw the PhotoEditorView content on the canvas
            photoEditorView.draw(canvas)

            // Draw the EmojiContainer content on the canvas
            emojiContainer.draw(canvas)

            // Create the directory if it doesn't exist
            val dir = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES)
            val demoDir = File(dir, "demo")
            if (!demoDir.exists()) {
                demoDir.mkdir()
            }

            // Create the output file
            val file = File(demoDir, orijinalFileName)
            val outputStream = FileOutputStream(file)

            // Compress the Bitmap and save it to the output file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            selectedItem = null
            emojisIndex = null
            drawnEmojis.clear()
            binding.addEmojis.visibility = View.VISIBLE
            binding.emojisCardView.visibility = View.GONE
            binding.removeEmojis.visibility = View.GONE
            binding.ivEraseClose.visibility = View.GONE
            binding.ivErase.visibility = View.VISIBLE
            imageUri = Uri.fromFile(file).toString()
            Toast.makeText(this, "Image with drawable saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image with drawable: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        Log.d("testing", "selectedItem == $selectedItem")
//        Log.d("testing", "x = $x  y = $y")
        if (selectedItem == BLUR) {
            applyBlurEfect(x.toInt()-50, y.toInt()-250)
        } else if (selectedItem == EMOJI) {
            if (emojisIndex != null) {
                val emoji = when (emojisIndex) {
                    0 -> R.drawable.smile
                    1 -> R.drawable.heart
                    2 -> R.drawable.laugh
                    3 -> R.drawable.burn
                    4 -> R.drawable.kiss
                    5 -> R.drawable.ghost
                    6 -> R.drawable.sad
                    else -> R.drawable.smile
                }

                // Define a threshold for drawing a new emoji (adjust as needed)
                val threshold = 80

                // Check if the new position is far enough from the last position
                val distance = kotlin.math.sqrt((x - lastX).pow(2) + (y - lastY).pow(2))

                if (lastX == -1f || lastY == -1f || distance > threshold) {
                    val imageView = ImageView(this).apply {
                        setImageResource(emoji)
                    }
                    val imageViewParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        leftMargin = x.toInt() - 50
                        topMargin = y.toInt() - 310
                    }
                    binding.emojiContainer.addView(imageView, imageViewParams)

                    // Store the position of the newly drawn emoji
                    drawnEmojis.add(Pair(x, y))

                    // Update the last drawn position
                    lastX = x
                    lastY = y
                }
            }
        } else {

        }

        return super.onTouchEvent(event)
    }

    private fun applyBlurEfect(x: Int, y: Int) {
        // Enable drawing cache
        binding.photoEditorView.isDrawingCacheEnabled = true

        // Create bitmap from drawing cache
        val bitmap = Bitmap.createBitmap(binding.photoEditorView.drawingCache)

        // Disable drawing cache after creating the bitmap
        binding.photoEditorView.isDrawingCacheEnabled = false

        // Define the radius of the blur effect
        val blurRadius = 25f

        // Create a RenderScript context
        val rsContext = RenderScript.create(this)

        // Calculate the region to apply the blur effect
        val startX = maxOf(0, x) // adjust as needed
        val startY = maxOf(0, y) // adjust as needed
        val width = minOf(bitmap.width - startX, 75) // adjust as needed
        val height = minOf(bitmap.height - startY, 75) // adjust as needed

        // Check if the calculated width or height is positive
        if (width > 0 && height > 0) {
            // Create a bitmap to store the region to be blurred
            val regionBitmap = Bitmap.createBitmap(bitmap, startX, startY, width, height)

            // Apply blur effect only to the region
            val blurredBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val blurInput = Allocation.createFromBitmap(rsContext, regionBitmap)
            val blurOutput = Allocation.createTyped(rsContext, blurInput.type)
            val blurScript = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
            blurScript.setRadius(blurRadius)
            blurScript.setInput(blurInput)
            blurScript.forEach(blurOutput)
            blurOutput.copyTo(blurredBitmap)

            // Update the ImageView with the blurred region
            val imageView = ImageView(this).apply {
                setImageBitmap(blurredBitmap)
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                    leftMargin = startX
                    topMargin = startY
                }
            }
            binding.emojiContainer.addView(imageView)
        } else {
            // Log or handle the case where width or height is not positive
        }

        // Destroy the RenderScript context to free resources
        rsContext.finish()
    }



}




