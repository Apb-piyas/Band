package lrandomdev.com.online.mp3player.helpers;

/**
 * Created by Lrandom on 4/23/18.
 */

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
