package sdk.facility.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum Region {
    URBAN("U", new String[]{"urban", "u"}),
    RURAL("R", new String[]{"rural", "r"});

    private static final Map<String, Region> LOOKUP = new HashMap<>();

    static {
        for (Region region : Region.values()) {
            for (String alias : region.aliases) {
                LOOKUP.put(alias.toLowerCase(), region);
            }
        }
    }

    private final String code;
    private final String[] aliases;

    @JsonCreator
    public static Region fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }
        Region region = LOOKUP.get(value.toLowerCase());
        if (region == null) {
            throw new IllegalArgumentException("Invalid region: " + value);
        }
        return region;
    }

    @JsonValue
    public String getCode() {
        return code;
    }
}
