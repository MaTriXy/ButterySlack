package doubledotlabs.butteryslack.data;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ullink.slack.simpleslackapi.SlackChannel;

import doubledotlabs.butteryslack.R;
import doubledotlabs.butteryslack.fragments.ChannelFragment;

public class ChannelItemData extends ItemData {

    private SlackChannel channel;

    public ChannelItemData(Context context, SlackChannel channel) {
        super(context, new Identifier(channel.getName(), channel.getTopic()));
        this.channel = channel;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_channel, parent, false));
    }

    @Override
    public void onClick(View v) {
        Bundle args = new Bundle();
        args.putString(ChannelFragment.EXTRA_CHANNEL_ID, channel.getId());

        ChannelFragment fragment = new ChannelFragment();
        fragment.setArguments(args);
        ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).addToBackStack(null).commit();
    }
}
