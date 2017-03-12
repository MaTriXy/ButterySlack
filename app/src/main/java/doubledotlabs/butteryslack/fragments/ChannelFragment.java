package doubledotlabs.butteryslack.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.afollestad.async.Action;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import doubledotlabs.butteryslack.data.ItemData;
import doubledotlabs.butteryslack.data.MessageItemData;

public class ChannelFragment extends ChatFragment {

    public static final String EXTRA_CHANNEL_ID = "doubledotlabs.butteryslack.EXTRA_CHANNEL_ID";

    private String channelId;
    private SlackChannel channel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        channelId = getArguments().getString(EXTRA_CHANNEL_ID);

        new Action<SlackChannel>() {
            @NonNull
            @Override
            public String id() {
                return "channel";
            }

            @Nullable
            @Override
            protected SlackChannel run() throws InterruptedException {
                return getButterySlack().session.findChannelById(channelId);
            }

            @Override
            protected void done(@Nullable SlackChannel result) {
                if (result != null) {
                    channel = result;
                    setTitle(result.getName());
                    registerListener();
                }
            }
        }.execute();
    }

    @Override
    boolean isMessageInChannel(SlackMessagePosted event) {
        return event.getChannel().getId().equals(channelId);
    }

    @Override
    void loadPage(final int page, final String timestamp) {
        new Action<List<ItemData>>() {
            @NonNull
            @Override
            public String id() {
                return "page" + page;
            }

            @Nullable
            @Override
            protected List<ItemData> run() throws InterruptedException {
                Map<String, String> params = new HashMap<>();
                params.put("channel", channelId);
                params.put("latest", timestamp);

                List<ItemData> messages = new ArrayList<>();

                String json = getButterySlack().session.postGenericSlackCommand(params, "channels.history").getReply().getPlainAnswer().toJSONString();
                JsonReader reader = new JsonReader(new StringReader(json));
                reader.setLenient(true);

                try {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        if (reader.nextName().equals("messages")) {
                            reader.beginArray();
                            while (reader.hasNext()) {
                                reader.beginObject();

                                String type = "", senderId = "", senderName = "", content = "", timestamp = "";
                                while (reader.hasNext()) {
                                    switch (reader.nextName()) {
                                        case "type":
                                            type = reader.nextString();
                                            break;
                                        case "user":
                                            senderId = reader.nextString();
                                            senderName = getButterySlack().session.findUserById(senderId).getUserName();
                                            break;
                                        case "text":
                                            content = reader.nextString();
                                            break;
                                        case "ts":
                                            timestamp = reader.nextString();
                                            break;
                                        default:
                                            reader.skipValue();
                                            break;
                                    }
                                }

                                if (type.equals("message")) {
                                    messages.add(new MessageItemData(getContext(), senderId, senderName, content, timestamp));
                                }
                                reader.endObject();
                            }
                            reader.endArray();
                        } else reader.skipValue();
                    }
                    reader.endObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return messages;
            }

            @Override
            protected void done(@Nullable List<ItemData> result) {
                if (result != null) {
                    onPageLoaded(result);
                }
            }
        }.execute();
    }

}
