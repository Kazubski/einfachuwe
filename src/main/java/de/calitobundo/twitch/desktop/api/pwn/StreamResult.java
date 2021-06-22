package de.calitobundo.twitch.desktop.api.pwn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamResult {

    public Map<String, String> urls;
    public boolean success;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("success", success)
               // .append("urls", urls)
                .toString();
    }
}