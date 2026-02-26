package io.agora.board.ui.sample.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import io.agora.board.forge.RoomCallback
import io.agora.board.forge.RoomError
import io.agora.board.forge.ui.sample.R
import io.agora.board.forge.ui.sample.databinding.FgsPageIndicatorLayoutBinding
import io.agora.board.forge.whiteboard.SimpleWhiteboardListener
import io.agora.board.forge.whiteboard.WhiteboardApplication
import io.agora.board.forge.whiteboard.WhiteboardListener

class FgsPageIndicatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private var whiteboardApp: WhiteboardApplication? = null
    private var binding: FgsPageIndicatorLayoutBinding

    init {
        inflate(context, R.layout.fgs_page_indicator_layout, this)
        binding = FgsPageIndicatorLayoutBinding.bind(this)

        binding.btnNextPage.setOnClickListener {
            whiteboardApp?.indexedNavigation?.nextPage(EmptyRoomCallback())
        }

        binding.btnPrevPage.setOnClickListener {
            whiteboardApp?.indexedNavigation?.prevPage(EmptyRoomCallback())
        }

        binding.btnAddPage.setOnClickListener {
            whiteboardApp?.indexedNavigation?.currentPageIndex(object : RoomCallback<Int> {
                override fun onSuccess(index: Int) {
                    whiteboardApp?.indexedNavigation?.insertPage(index, object : RoomCallback<Int> {
                        override fun onSuccess(result: Int) {
                            Log.e("removePage", "onSuccess")
                        }

                        override fun onFailure(error: RoomError) {
                            Log.e("removePage", "onFailure")
                        }
                    })
                }

                override fun onFailure(error: RoomError) {
                    Log.e("currentPageIndex", "onFailure")
                }
            })
        }

        binding.btnRemovePage.setOnClickListener {
            whiteboardApp?.indexedNavigation?.currentPageIndex(object : RoomCallback<Int> {
                override fun onSuccess(result: Int) {
                    whiteboardApp?.indexedNavigation?.removePage(result, object : RoomCallback<Int> {
                        override fun onSuccess(result: Int) {
                            Log.e("removePage", "onSuccess")
                        }

                        override fun onFailure(error: RoomError) {
                            Log.e("removePage", "onFailure")
                        }
                    })
                }

                override fun onFailure(error: RoomError) {
                    Log.e("currentPageIndex", "onFailure")
                }
            })
        }
    }

    private val whiteboardListener: WhiteboardListener = object : SimpleWhiteboardListener() {
        @SuppressLint("SetTextI18n")
        override fun onPageInfoUpdate(whiteboard: WhiteboardApplication, activePageIndex: Int, pageCount: Int) {
            binding.tvPageIndicator.text = "${activePageIndex + 1}/$pageCount"
        }
    }

    fun attachWhiteboard(application: WhiteboardApplication?) {
        if (this.whiteboardApp != null) {
            whiteboardApp?.removeListener(whiteboardListener)
        }

        this.whiteboardApp = application
        this.whiteboardApp?.addListener(whiteboardListener)

        whiteboardApp?.indexedNavigation?.currentPageIndex(object : RoomCallback<Int> {
            override fun onSuccess(index: Int) {
                whiteboardApp?.indexedNavigation?.pageCount(object : RoomCallback<Int> {
                    @SuppressLint("SetTextI18n")
                    override fun onSuccess(count: Int) {
                        binding.tvPageIndicator.text = "${index + 1}/${count}"
                    }

                    override fun onFailure(error: RoomError) {}
                })
            }

            override fun onFailure(error: RoomError) {}
        })
    }

    fun detachWhiteboard() {
        whiteboardApp?.removeListener(whiteboardListener)
        whiteboardApp = null
    }
}
