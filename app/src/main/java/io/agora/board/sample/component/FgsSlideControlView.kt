package io.agora.board.sample.component

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.isVisible
import io.agora.board.forge.sample.R
import io.agora.board.forge.sample.databinding.FgsSlideControlViewBinding
import io.agora.board.forge.slide.SlideApplication
import io.agora.board.forge.slide.SlideApplicationLayout
import io.agora.board.sample.util.toggleVisibility

class FgsSlideControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private var slideApplication: SlideApplication? = null

    private val scrollView: View
    private val handleView: ImageView

    private val binding: FgsSlideControlViewBinding =
        FgsSlideControlViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        scrollView = binding.scrollView
        handleView = binding.handle

        binding.footerViewSwitch.setOnCheckedChangeListener { _, isChecked: Boolean ->
            val slideAppLayout = slideApplication?.getView() as? SlideApplicationLayout
            slideAppLayout?.setFooterVisibility(isChecked)
        }

        binding.clear.setOnClickListener {

        }

        binding.handle.setOnClickListener {
            scrollView.toggleVisibility()
            updateHandleView()
        }

        updateHandleView()
        setupEdgeMarginTest()
    }

    private fun setupEdgeMarginTest() {
        val marginSeekBar = binding.changeEdgeMargin
        marginSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        marginSeekBar.max = dp2px(32f) * 100
    }

    private fun updateHandleView() {
        handleView.setImageResource(
            if (scrollView.isVisible) R.drawable.fgs_ic_arrow_up else R.drawable.fgs_ic_arrow_down
        )
    }

    fun attachApp(slideApplication: SlideApplication) {
        this.slideApplication = slideApplication
    }

    fun detachApp() {
        this.slideApplication = null
    }

    protected fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpVal, context.resources.displayMetrics
        ).toInt()
    }
}
