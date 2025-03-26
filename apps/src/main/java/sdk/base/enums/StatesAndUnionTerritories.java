package sdk.base.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum StatesAndUnionTerritories {
    ANDHRAPRADESH("Andhra Pradesh"),
    ARUNACHALPRADESH("Arunachal Pradesh"),
    ASSAM("Assam"),
    BIHAR("Bihar"),
    CHATTISGARH("Chattisgarh"),
    CHHATTISGARH("Chattisgarh"),
    GOA("Goa"),
    GUJARAT("Gujarat"),
    HARYANA("Haryana"),
    HIMACHALPRADESH("Himachal Pradesh"),
    JHARKHAND("Jharkhand"),
    KARNATAKA("Karnataka"),
    KERALA("Kerala"),
    MADHYAPRADESH("Madhya Pradesh"),
    MAHARASHTRA("Maharashtra"),
    MANIPUR("Manipur"),
    MEGHALAYA("Meghalaya"),
    MIZORAM("Mizoram"),
    NAGALAND("Nagaland"),
    ODISHA("Odisha"),
    PUNJAB("Punjab"),
    RAJASTHAN("Rajasthan"),
    SIKKIM("Sikkim"),
    TAMILNADU("Tamil Nadu"),
    TELANGANA("Telangana"),
    TRIPURA("Tripura"),
    UTTARPRADESH("Uttar Pradesh"),
    UTTARAKHAND("Uttarakhand"),
    WESTBENGAL("West Bengal"),
    ANDAMANANDNICOBARS("Andaman and Nicobar"),
    LAKSHADWEEP("Lakshadweep"),
    DELHI("Delhi"),
    DADRAHAVELI("Dadra and Nagar Haveli and Daman & Diu"),
    JAMMUANDKASHMIR("Jammu and Kashmir"),
    CHANDIGARH("Chandigarh"),
    LADAKH("Ladakh"),
    PUDUCHERRY("Puducherry"),
    UNKNOWN("Unknown");

    private final String state;

    StatesAndUnionTerritories(String state) {
        this.state = state;
    }

    @JsonCreator
    public static StatesAndUnionTerritories fromString(String state) {
        for (StatesAndUnionTerritories st : StatesAndUnionTerritories.values()) {
            if (st.state.equalsIgnoreCase(state)) {  // Case-insensitive comparison
                return st;
            }
        }
        return UNKNOWN;
    }

    @JsonValue
    public String getState() {
        return state;
    }


}

