import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pibuddy.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.available_device_row.view.*

class PiAdapter(val IP: String): Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.Available_Device_Text_View.text = IP //Latest Message TextBody





    }

    override fun getLayout(): Int {
        return R.layout.available_device_row
    }
}