package sdk.base.utils;

public final class Constants {

    private Constants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    //Health-Lake Endpoints
    public static final String GET_PROFILES_URL = "/health-lake/get-profiles";

    //Facility Endpoints
    public static final String GET_FACILITIES_URL = "/facilities";
    public static final String REGISTER_FACILITY_URL = "/register-facility";
    public static final String UPDATE_FACILITY_URL = "/update-facility";
    public static final String DELETE_FACILITY_URL = "/facility";
    public static final String SEARCH_FACILITY_URL = "/search-facility";
    public static final String FACILITY_FOUND_MESSAGE = "Facility Found !!!";

    //Demographic Data Endpoints
    public static final String FETCH_STATES_URL = "/lgd-states";
    public static final String FETCH_SUB_DISTRICTS_URL = "/lgd-subdistricts";
    public static final String DISTRICT_CODE_PARAM = "?districtCode=%d";
    public static final String FETCH_ADDRESS_PROOF_URL = "/master-data/ADDRESS-PROOF";
    public static final String FETCH_OWNERSHIP_TYPE_URL = "/master-data/OWNER";
    public static final String FETCH_OWNERSHIP_SUBTYPE_URL = "/owner-subtype";
    public static final String FETCH_FACILITY_STATUS_URL = "/master-data/FAC-STATUS";
    public static final String FETCH_SYSTEM_OF_MEDICINE_URL = "/master-data/MEDICINE";
    public static final String FETCH_SPECIALITIES_URL = "/specialities";
    public static final String FETCH_FACILITY_TYPE_URL = "/facility-type";
    public static final String FETCH_FACILITY_SUBTYPE_URL = "/facility-subtypes";
    public static final String FETCH_SERVICE_TYPES_URL = "/master-data/TYPE-SERVICE";
    public static final String FETCH_GENERAL_INFO_OPTIONS_URL = "/master-data/GENERAL-INFO-OPTIONS";
    public static final String FETCH_IMAGING_CENTER_SERVICES_URL = "/master-data/IMAGING";

    //Google Credentials
    public static final String GOOGLE_LOCATION_URL = "https://maps.googleapis.com/maps/api/geocode/json";
}
