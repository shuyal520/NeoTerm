package io.neoterm.customize.completion.widget

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import io.neoterm.R
import io.neoterm.backend.TerminalColors
import io.neoterm.customize.color.ColorSchemeManager
import io.neoterm.frontend.completion.model.CompletionCandidate
import io.neoterm.view.TerminalView

/**
 * @author kiva
 */
class CandidatePopupWindow(val context: Context) {
    var candidates: List<CompletionCandidate>? = null
    var popupWindow: PopupWindow? = null
    var wantsToFinish = false
    var candidateAdapter: CandidateAdapter? = null

    fun show(terminalView: TerminalView) {
        if (popupWindow == null && !wantsToFinish) {
            popupWindow = createPopupWindow()
        }

        candidateAdapter?.notifyDataSetChanged()
        if (!(popupWindow?.isShowing ?: false)) {
            popupWindow?.showAtLocation(terminalView, Gravity.BOTTOM.and(Gravity.START),
                    terminalView.cursorAbsX,
                    terminalView.cursorAbsY)
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    private fun createPopupWindow(): PopupWindow {
        val popupWindow = PopupWindow(context)
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        val contentView = LayoutInflater.from(context).inflate(R.layout.popup_auto_complete, null, false)
        val candidateListView = contentView.findViewById(R.id.popup_complete_candidate_list) as ListView
        candidateAdapter = CandidateAdapter(this)
        candidateListView.adapter = candidateAdapter

        popupWindow.contentView = contentView
        return popupWindow
    }

    fun cleanup() {
        wantsToFinish = true
        popupWindow = null
        candidateAdapter = null
        candidates = null
    }

    class CandidateAdapter(val candidatePopupWindow: CandidatePopupWindow) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val viewHolder: CandidateViewHolder =
                    if (convertView != null) {
                        convertView.tag as CandidateViewHolder
                    } else {
                        convertView = LayoutInflater.from(candidatePopupWindow.context)
                                .inflate(R.layout.item_complete_candidate, null, false)
                        val viewHolder = CandidateViewHolder(convertView)
                        convertView.tag = viewHolder
                        viewHolder
                    }

            val candidate = getItem(position) as CompletionCandidate
            viewHolder.apply {
                display.text = candidate.displayName
                if (candidate.description != null) {
                    splitView.visibility = View.VISIBLE
                    description.visibility = View.VISIBLE
                    description.text = candidate.description
                } else {
                    splitView.visibility = View.GONE
                    description.visibility = View.GONE
                }
            }
            return convertView!!
        }

        override fun getItem(position: Int): Any? {
            return candidatePopupWindow.candidates?.get(position)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return candidatePopupWindow.candidates?.size ?: 0
        }
    }

    class CandidateViewHolder(rootView: View) {
        val display: TextView = rootView.findViewById(R.id.complete_display) as TextView
        val description: TextView = rootView.findViewById(R.id.complete_description) as TextView
        val splitView: View = rootView.findViewById(R.id.complete_split)

        init {
            val colorScheme = ColorSchemeManager.getCurrentColorScheme()
            val textColor = TerminalColors.parse(colorScheme.foregroundColor)
            display.setTextColor(textColor)
            description.setTextColor(textColor)
        }
    }
}